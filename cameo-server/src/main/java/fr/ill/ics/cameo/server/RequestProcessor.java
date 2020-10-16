/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.exception.ApplicationAlreadyExecuting;
import fr.ill.ics.cameo.exception.IdNotFoundException;
import fr.ill.ics.cameo.exception.MaxNumberOfApplicationsReached;
import fr.ill.ics.cameo.exception.StreamNotPublishedException;
import fr.ill.ics.cameo.exception.UnknownApplicationException;
import fr.ill.ics.cameo.exception.UnknownPublisherException;
import fr.ill.ics.cameo.exception.UnmanagedApplicationException;
import fr.ill.ics.cameo.manager.Application;
import fr.ill.ics.cameo.manager.ApplicationConfig;
import fr.ill.ics.cameo.manager.ApplicationInfo;
import fr.ill.ics.cameo.manager.ApplicationState;
import fr.ill.ics.cameo.manager.ConfigManager;
import fr.ill.ics.cameo.manager.Log;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.manager.PortInfo;
import fr.ill.ics.cameo.manager.StatusInfo;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.server.Server.Version;

/**
 * 
 */
public class RequestProcessor {
	
	/**
	 * verify connection
	 * @param manager 
	 * 
	 */
	public Msg processSync(Manager manager) {
		
		Log.logger().fine("Received Sync message");
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("OK");
		
		// Send sync message for synchronizing subscribers.
		manager.sendStatus(-1, "", ApplicationState.UNKNOWN, ApplicationState.UNKNOWN, -1);
		
		return reply;
	}
	
	public Msg processSyncStream(JSONObject request, Manager manager) {

		Log.logger().fine("Received SyncStream message");
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("OK");
		
		// Get the publisher.
		Zmq.Socket publisher = manager.getStreamPublisher(JSON.getString(request, Message.SyncStreamRequest.NAME));
		
		// Publish a SYNCSTREAM event.
		if (publisher != null) {
			JSONObject event = new JSONObject();
			
			// Synchronize the publisher as it is accessed by the stream threads.
			Manager.publishSynchronized(publisher, Message.Event.SYNCSTREAM, Message.serialize(event));
		}
		
		return reply;
	}

	/**
	 * start command
	 * 
	 * @param message
	 * @return
	 */
	public Msg processStartRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Start request");
		
		try {
			// Convert the args.
			String[] args = null;
			JSONArray list = JSON.getArray(request, Message.StartRequest.ARGS);
			if (list != null) {
				args = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					args[i] = (String)list.get(i);
				}
			}
			
			// Start the application.
			Application application = manager.startApplication(JSON.getString(request, Message.StartRequest.NAME), 
																args, 
																JSON.getString(request, Message.StartRequest.INSTANCE_REFERENCE));
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, application.getId());
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (UnknownApplicationException | MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, Long.valueOf(-1));
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());

			return Converter.reply(response);
		}
	}

	/**
	 * showAll command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processAppsRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Apps request");
		
		LinkedList<ApplicationInfo> list = manager.showApplicationMap();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			applicationInfo.put(Message.ApplicationInfo.ID, application.getId());
			applicationInfo.put(Message.ApplicationInfo.ARGS, application.getArgs());
			applicationInfo.put(Message.ApplicationInfo.NAME, application.getName());
			applicationInfo.put(Message.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
			applicationInfo.put(Message.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
			applicationInfo.put(Message.ApplicationInfo.PID, application.getPid());
			
			array.add(applicationInfo);
		}
		response.put(Message.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
		return Converter.reply(response);
	}

	/**
	 * stop command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processStopRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Stop request");
		
		try {
			String applicationName = manager.stopApplication(JSON.getInt(request, Message.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	/**
	 * kill command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processKillRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Kill request");
		
		try {
			String applicationName = manager.killApplication(JSON.getInt(request, Message.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	/**
	 * connect command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processConnectRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Connect request");
		
		String applicationName = JSON.getString(request, Message.ConnectRequest.NAME);
		
		LinkedList<ApplicationInfo> list = manager.showApplicationMap();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			
			// Filtering on the application name.
			if (applicationName.equals(application.getName())) {
				applicationInfo.put(Message.ApplicationInfo.ID, application.getId());
				applicationInfo.put(Message.ApplicationInfo.ARGS, application.getArgs());
				applicationInfo.put(Message.ApplicationInfo.NAME, application.getName());
				applicationInfo.put(Message.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				applicationInfo.put(Message.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				applicationInfo.put(Message.ApplicationInfo.PID, application.getPid());
				
				array.add(applicationInfo);
			}
		}
		response.put(Message.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
		return Converter.reply(response);
	}

	public Msg processConnectWithIdRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ConnectWithId request");
		
		int applicationId = JSON.getInt(request, Message.ConnectWithIdRequest.ID);
		
		LinkedList<ApplicationInfo> list = manager.showApplicationMap();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			
			// Filtering on the application name.
			if (applicationId == application.getId()) {
				applicationInfo.put(Message.ApplicationInfo.ID, application.getId());
				applicationInfo.put(Message.ApplicationInfo.ARGS, application.getArgs());
				applicationInfo.put(Message.ApplicationInfo.NAME, application.getName());
				applicationInfo.put(Message.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				applicationInfo.put(Message.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				applicationInfo.put(Message.ApplicationInfo.PID, application.getPid());
				
				array.add(applicationInfo);
			}
		}
		response.put(Message.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
		return Converter.reply(response);
	}
	
	/**
	 * ShowCommand
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processOutputPortWithIdRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received OutputPortWithId request");
				
		try {
			int port = manager.getStreamPort(JSON.getInt(request, Message.OutputPortWithIdRequest.ID));
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, port);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException | IdNotFoundException | UnknownApplicationException | StreamNotPublishedException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}
	
	/**
	 * isAlive command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processIsAliveRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received IsAlive request");
		
		boolean isAlive = manager.isAlive(JSON.getInt(request, Message.IsAliveRequest.ID));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.IsAliveResponse.IS_ALIVE, isAlive);
		
		return Converter.reply(response);
	}

	/**
	 * sent parameters
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processWriteInputRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received WriteInput request");
		
		// Convert the parameters.
		JSONArray list = JSON.getArray(request, Message.WriteInputRequest.PARAMETERS);
		
		String[] inputArray = new String[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			inputArray[i] = (String)list.get(i);
		}
		
		try {
			manager.writeToInputStream((JSON.getInt(request, Message.WriteInputRequest.ID)), inputArray);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException | UnmanagedApplicationException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	/**
	 * StatusCommand
	 * 
	 * @return
	 */
	public Msg processStatusRequest() {
		
		Log.logger().fine("Received Status request");
		
		int port = ConfigManager.getInstance().getStreamPort();
				
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.RequestResponse.VALUE, port);
		response.put(Message.RequestResponse.MESSAGE, "OK");
		
		return Converter.reply(response);
	}
	
	public Msg processListRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received List request");
		
		List<ApplicationConfig> list = manager.getAvailableApplications();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationConfig> it = list.iterator();
		while (it.hasNext()) {
			ApplicationConfig application = (ApplicationConfig) it.next();

			JSONObject config = new JSONObject();
			config.put(Message.ApplicationConfig.NAME, application.getName());
			config.put(Message.ApplicationConfig.DESCRIPTION, application.getDescription());
			config.put(Message.ApplicationConfig.RUNS_SINGLE, application.runsSingle());
			config.put(Message.ApplicationConfig.RESTART, application.isRestart());
			config.put(Message.ApplicationConfig.STARTING_TIME, application.getStartingTime());
			config.put(Message.ApplicationConfig.STOPPING_TIME, application.getStoppingTime());
			
			array.add(config);
		}
		
		response.put(Message.ListResponse.APPLICATION_CONFIG, array);
		
		return Converter.reply(response);
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public Msg processOutputPortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received OuputPort request");
		
		int port = manager.getApplicationStreamPort(JSON.getString(request, Message.OutputRequest.NAME));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.RequestResponse.VALUE, port);
		response.put(Message.RequestResponse.MESSAGE, "OK");
		
		return Converter.reply(response);
	}

	public Msg processSetStatusRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received SetStatus request");
		
		int applicationId = JSON.getInt(request, Message.SetStatusRequest.ID);
		int state = JSON.getInt(request, Message.SetStatusRequest.APPLICATION_STATE);
		
		// Return the reply.
		JSONObject response = new JSONObject();
		
		try {
			boolean done = manager.setApplicationStateFromClient(applicationId, state);
					
			if (done) {
				response.put(Message.RequestResponse.VALUE, 0);
				response.put(Message.RequestResponse.MESSAGE, "OK");
			}
			else {
				response.put(Message.RequestResponse.VALUE, -1);
				response.put(Message.RequestResponse.MESSAGE, "Cannot set the state");
			}
		}
		catch (IdNotFoundException e) {
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
		}
				
		return Converter.reply(response);
	}
	
	public Msg processGetStatusRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received GetStatus request");
		
		int applicationId = JSON.getInt(request, Message.GetStatusRequest.ID);
		
		StatusInfo status = manager.getApplicationState(applicationId);
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.StatusEvent.ID, status.getId());
		response.put(Message.StatusEvent.NAME, status.getName());
		response.put(Message.StatusEvent.APPLICATION_STATE, status.getApplicationState());
		response.put(Message.StatusEvent.PAST_APPLICATION_STATES, status.getPastApplicationStates());
		
		return Converter.reply(response);
	}
	

	public Msg processSetResultRequest(JSONObject request, byte[] data, Manager manager) {
		
		Log.logger().fine("Received SetResult request");
		
		int applicationId = JSON.getInt(request, Message.SetResultRequest.ID);

		try {
			manager.setApplicationResult(applicationId, data);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}


	public Msg processRequestPortV0Request(JSONObject request, Manager manager) {

		Log.logger().fine("Received RequestPortV0 request");
		
		int applicationId = JSON.getInt(request, Message.RequestPortV0Request.ID);
		String portName = JSON.getString(request, Message.RequestPortV0Request.NAME);
		
		try {
			int port = manager.requestPortForApplication(applicationId, portName);
			if (port != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, port);
				response.put(Message.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -1);
				response.put(Message.RequestResponse.MESSAGE, "The port already exists");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processConnectPortV0Request(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ConnectPortV0 request");
		
		int applicationId = JSON.getInt(request, Message.ConnectPortV0Request.ID);
		String portName = JSON.getString(request, Message.ConnectPortV0Request.NAME);
		
		try {
			int port = manager.connectPortForApplication(applicationId, portName);
			if (port != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, port);
				response.put(Message.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -1);
				response.put(Message.RequestResponse.MESSAGE, "The port does not exist");
				
				return Converter.reply(response);
			}
			
		} catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRemovePortV0Request(JSONObject request, Manager manager) {

		Log.logger().fine("Received RemovePortV0 request");
		
		int applicationId = JSON.getInt(request, Message.RemovePortV0Request.ID);
		String portName = JSON.getString(request, Message.RemovePortV0Request.NAME);
		
		try {
			boolean done = manager.removePortForApplication(applicationId, portName);
			if (done) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, 0);
				response.put(Message.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -1);
				response.put(Message.RequestResponse.MESSAGE, "Cannot remove the port");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}	
	}
	
	public Msg processCreatePublisherRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received CreatePublisher request");
		
		int applicationId = JSON.getInt(request, Message.CreatePublisherRequest.ID);
		String publisherName = JSON.getString(request, Message.CreatePublisherRequest.NAME);
		
		try {
			int numberOfSubscribers = JSON.getInt(request, Message.CreatePublisherRequest.NUMBER_OF_SUBSCRIBERS);
			int[] ports = manager.createPublisherForApplication(applicationId, publisherName, numberOfSubscribers);
			
			if (ports[0] != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.PublisherResponse.PUBLISHER_PORT, ports[0]);
				response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, ports[1]);
				response.put(Message.PublisherResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.PublisherResponse.PUBLISHER_PORT, -1);
				response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, -1);
				response.put(Message.PublisherResponse.MESSAGE, "The publisher already exists");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.PublisherResponse.PUBLISHER_PORT, -1);
			response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, -1);
			response.put(Message.PublisherResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}	
	}

	public Msg processTerminatePublisherRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received TerminatePublisher request");
		
		int applicationId = JSON.getInt(request, Message.TerminatePublisherRequest.ID);
		String publisherName = JSON.getString(request, Message.TerminatePublisherRequest.NAME);
		
		try {
			boolean done = manager.terminatePublisherForApplication(applicationId, publisherName);
			if (done) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, 0);
				response.put(Message.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -1);
				response.put(Message.RequestResponse.MESSAGE, "Cannot terminate the publisher");
				
				return Converter.reply(response);
			}
			
		} catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}	
	}

	public Msg processConnectPublisherRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ConnectPublisher request");
		
		int applicationId = JSON.getInt(request, Message.ConnectPublisherRequest.APPLICATION_ID);
		String publisherName = JSON.getString(request, Message.ConnectPublisherRequest.PUBLISHER_NAME);
		
		try {
			 Application.Publisher publisher = manager.getPublisherForApplication(applicationId, publisherName);
			 int[] ports = manager.getPublisherPortsForApplication(applicationId, publisherName);
			 
			 if (ports[0] != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.PublisherResponse.PUBLISHER_PORT, ports[0]);
				response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, ports[1]);
				response.put(Message.PublisherResponse.NUMBER_OF_SUBSCRIBERS, publisher.numberOfSubscribers);
				response.put(Message.PublisherResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.PublisherResponse.PUBLISHER_PORT, -1);
				response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, -1);
				response.put(Message.PublisherResponse.NUMBER_OF_SUBSCRIBERS, -1);
				response.put(Message.PublisherResponse.MESSAGE, "The publisher does not exist");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException | UnknownPublisherException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.PublisherResponse.PUBLISHER_PORT, -1);
			response.put(Message.PublisherResponse.SYNCHRONIZER_PORT, -1);
			response.put(Message.PublisherResponse.NUMBER_OF_SUBSCRIBERS, -1);
			response.put(Message.PublisherResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processAttachUnmanagedRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received StartedUnmanaged request");
		
		int applicationId = 0;
		String name = JSON.getString(request, Message.AttachUnmanagedRequest.NAME);
		
		try {
			// Set the PID if it is passed.
			if (request.containsKey(Message.AttachUnmanagedRequest.PID)) {
				int pid = JSON.getInt(request, Message.AttachUnmanagedRequest.PID);
				applicationId = manager.newStartedUnmanagedApplication(name, pid);
			}
			else {
				applicationId = manager.newStartedUnmanagedApplication(name);
			}
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, applicationId);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);			
		} 
	}

	public Msg processDetachUnmanagedRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received TerminatedUnmanaged request");
		
		int applicationId = JSON.getInt(request, Message.DetachUnmanagedRequest.ID);
		
		try {
			String applicationName = manager.setUnmanagedApplicationTerminated(applicationId);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}		
	}

	public Msg processVersion(Version version) {
		
		Log.logger().fine("Received Version request");
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.VersionResponse.MAJOR, version.major);
		response.put(Message.VersionResponse.MINOR, version.minor);
		response.put(Message.VersionResponse.REVISION, version.revision);
		
		return Converter.reply(response);
	}

	public Msg processStoreKeyValue(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received StoreKeyValue request");
		
		int applicationId = JSON.getInt(request, Message.StoreKeyValueRequest.ID);
		String key = JSON.getString(request, Message.StoreKeyValueRequest.KEY);
		String value = JSON.getString(request, Message.StoreKeyValueRequest.VALUE);
		
		try {
			manager.storeKeyValue(applicationId, key, value);
						
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}		
	}

	public Msg processGetKeyValue(JSONObject request, Manager manager) {

		Log.logger().fine("Received GetKeyValue request");
		
		int applicationId = JSON.getInt(request, Message.GetKeyValueRequest.ID);
		String key = JSON.getString(request, Message.GetKeyValueRequest.KEY);
		
		try {
			String value = manager.getKeyValue(applicationId, key);
			
			if (value != null) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, 0);
				response.put(Message.RequestResponse.MESSAGE, value);
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -2);
				response.put(Message.RequestResponse.MESSAGE, "Key is undefined");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRemoveKeyValue(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received RemoveKey request");
		
		int applicationId = JSON.getInt(request, Message.RemoveKeyRequest.ID);
		String key = JSON.getString(request, Message.RemoveKeyRequest.KEY);
		
		try {
			boolean exists = manager.removeKey(applicationId, key);
			
			if (exists) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, 0);
				response.put(Message.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Message.RequestResponse.VALUE, -2);
				response.put(Message.RequestResponse.MESSAGE, "Key is undefined");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRequestPortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received RequestPort request");
		
		int applicationId = JSON.getInt(request, Message.RequestPortRequest.ID);
		
		try {
			// Request a port.
			int port = manager.requestPort(applicationId);

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, port);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processUnavailablePortRequest(JSONObject request, Manager manager) {
				
		Log.logger().fine("Received UnavailablePort request");
		
		int applicationId = JSON.getInt(request, Message.UnavailablePortRequest.ID);
		int port = JSON.getInt(request, Message.UnavailablePortRequest.PORT);

		try {
			// Set the port unavailable.
			manager.setPortUnavailable(applicationId, port);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processReleasePortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ReleasePort request");
		
		int applicationId = JSON.getInt(request, Message.ReleasePortRequest.ID);
		int port = JSON.getInt(request, Message.ReleasePortRequest.PORT);
		
		try {
			// Release the port.
			manager.releasePort(applicationId, port);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processPortsRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received Ports request");
		
		List<PortInfo> list = manager.getPortList();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<PortInfo> it = list.iterator();
		while (it.hasNext()) {
			PortInfo port = it.next();
			
			JSONObject portInfo = new JSONObject();
			portInfo.put(Message.PortInfo.PORT, port.getPort());
			portInfo.put(Message.PortInfo.STATUS, port.getStatus());
			portInfo.put(Message.PortInfo.APPLICATION_NAME_ID, port.getApplicationNameId());
		}
		
		response.put(Message.PortInfoListResponse.PORT_INFO, array);
		
		return Converter.reply(response);
	}
	
}