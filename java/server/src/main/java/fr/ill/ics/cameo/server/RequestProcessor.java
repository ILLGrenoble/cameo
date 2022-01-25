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
import fr.ill.ics.cameo.exception.KeyAlreadyExistsException;
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
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.server.Server.Version;
import fr.ill.ics.cameo.strings.ApplicationIdentity;
import fr.ill.ics.cameo.strings.Endpoint;

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
		
		// Send sync message for synchronizing subscribers.
		manager.sendStatus(-1, "", ApplicationState.UNKNOWN, ApplicationState.UNKNOWN, -1);
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, 0);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		return Converter.reply(response);
	}
	
	public Msg processSyncStream(JSONObject request, Manager manager) {

		Log.logger().fine("Received SyncStream message");
		
		// Get the publisher.
		Zmq.Socket publisher = manager.getStreamPublisher(JSON.getString(request, Messages.SyncStreamRequest.NAME));
		
		// Publish a SYNCSTREAM event.
		if (publisher != null) {
			JSONObject event = new JSONObject();
			
			// Synchronize the publisher as it is accessed by the stream threads.
			Manager.publishSynchronized(publisher, Messages.Event.SYNCSTREAM, Messages.serialize(event));
		}
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, 0);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		return Converter.reply(response);
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
			JSONArray list = JSON.getArray(request, Messages.StartRequest.ARGS);
			if (list != null) {
				args = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					args[i] = (String)list.get(i);
				}
			}
			
			JSONObject starterObject = JSON.getObject(request, Messages.ApplicationIdentity.STARTER);
			ApplicationIdentity starter = null;
			if (starterObject != null) {
				String endpoint = JSON.getString(starterObject, Messages.ApplicationIdentity.SERVER);
				try {
					starter = new ApplicationIdentity(JSON.getString(starterObject, Messages.ApplicationIdentity.NAME),
														JSON.getInt(starterObject, Messages.ApplicationIdentity.ID),
														Endpoint.parse(endpoint));
				}
				catch (Exception e) {
					Log.logger().severe("Cannot parse request starter endpoint " + endpoint);
				}
			}
			
			// Start the application.
			Application application = manager.startApplication(JSON.getString(request, Messages.StartRequest.NAME), 
																args,
																starter);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, application.getId());
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (UnknownApplicationException | MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, Long.valueOf(-1));
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());

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
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			applicationInfo.put(Messages.ApplicationInfo.ID, application.getId());
			applicationInfo.put(Messages.ApplicationInfo.ARGS, application.getArgs());
			applicationInfo.put(Messages.ApplicationInfo.NAME, application.getName());
			applicationInfo.put(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
			applicationInfo.put(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
			applicationInfo.put(Messages.ApplicationInfo.PID, application.getPid());
			
			array.add(applicationInfo);
		}
		response.put(Messages.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
		return Converter.reply(response);
	}

	public Msg processSetStopHandlerRequest(JSONObject request, Manager manager) {
	
		Log.logger().fine("Received SetStopHandler request");
		
		try {
			manager.setApplicationStopHandler(JSON.getInt(request, Messages.SetStopHandlerRequest.ID), JSON.getInt(request, Messages.SetStopHandlerRequest.STOPPING_TIME));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
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
			String applicationName = manager.stopApplication(JSON.getInt(request, Messages.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
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
			String applicationName = manager.killApplication(JSON.getInt(request, Messages.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
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
		
		String applicationName = JSON.getString(request, Messages.ConnectRequest.NAME);
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			
			// Filtering on the application name.
			if (applicationName.equals(application.getName())) {
				applicationInfo.put(Messages.ApplicationInfo.ID, application.getId());
				applicationInfo.put(Messages.ApplicationInfo.ARGS, application.getArgs());
				applicationInfo.put(Messages.ApplicationInfo.NAME, application.getName());
				applicationInfo.put(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				applicationInfo.put(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				applicationInfo.put(Messages.ApplicationInfo.PID, application.getPid());
				
				array.add(applicationInfo);
			}
		}
		response.put(Messages.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
		return Converter.reply(response);
	}

	public Msg processConnectWithIdRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ConnectWithId request");
		
		int applicationId = JSON.getInt(request, Messages.ConnectWithIdRequest.ID);
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JSONObject applicationInfo = new JSONObject();
			
			// Filtering on the application name.
			if (applicationId == application.getId()) {
				applicationInfo.put(Messages.ApplicationInfo.ID, application.getId());
				applicationInfo.put(Messages.ApplicationInfo.ARGS, application.getArgs());
				applicationInfo.put(Messages.ApplicationInfo.NAME, application.getName());
				applicationInfo.put(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				applicationInfo.put(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				applicationInfo.put(Messages.ApplicationInfo.PID, application.getPid());
				
				array.add(applicationInfo);
			}
		}
		response.put(Messages.ApplicationInfoListResponse.APPLICATION_INFO, array);
	
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
			int port = manager.getStreamPort(JSON.getInt(request, Messages.OutputPortWithIdRequest.ID));
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, port);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException | IdNotFoundException | UnknownApplicationException | StreamNotPublishedException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
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
		
		boolean isAlive = manager.isAlive(JSON.getInt(request, Messages.IsAliveRequest.ID));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.IsAliveResponse.IS_ALIVE, isAlive);
		
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
		JSONArray list = JSON.getArray(request, Messages.WriteInputRequest.PARAMETERS);
		
		String[] inputArray = new String[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			inputArray[i] = (String)list.get(i);
		}
		
		try {
			manager.writeToInputStream((JSON.getInt(request, Messages.WriteInputRequest.ID)), inputArray);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException | UnmanagedApplicationException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
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
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
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
			config.put(Messages.ApplicationConfig.NAME, application.getName());
			config.put(Messages.ApplicationConfig.DESCRIPTION, application.getDescription());
			config.put(Messages.ApplicationConfig.RUNS_SINGLE, application.runsSingle());
			config.put(Messages.ApplicationConfig.RESTART, application.isRestart());
			config.put(Messages.ApplicationConfig.STARTING_TIME, application.getStartingTime());
			config.put(Messages.ApplicationConfig.STOPPING_TIME, application.getStoppingTime());
			
			array.add(config);
		}
		
		response.put(Messages.ApplicationConfigListResponse.APPLICATION_CONFIG, array);
		
		return Converter.reply(response);
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public Msg processOutputPortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received OuputPort request");
		
		int port = manager.getApplicationStreamPort(JSON.getString(request, Messages.OutputRequest.NAME));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		return Converter.reply(response);
	}

	public Msg processSetStatusRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received SetStatus request");
		
		int applicationId = JSON.getInt(request, Messages.SetStatusRequest.ID);
		int state = JSON.getInt(request, Messages.SetStatusRequest.APPLICATION_STATE);
		
		// Return the reply.
		JSONObject response = new JSONObject();
		
		try {
			boolean done = manager.setApplicationStateFromClient(applicationId, state);
					
			if (done) {
				response.put(Messages.RequestResponse.VALUE, 0);
				response.put(Messages.RequestResponse.MESSAGE, "OK");
			}
			else {
				response.put(Messages.RequestResponse.VALUE, -1);
				response.put(Messages.RequestResponse.MESSAGE, "Cannot set the state");
			}
		}
		catch (IdNotFoundException e) {
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
		}
				
		return Converter.reply(response);
	}
	
	public Msg processGetStatusRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received GetStatus request");
		
		int applicationId = JSON.getInt(request, Messages.GetStatusRequest.ID);
		
		StatusInfo status = manager.getApplicationState(applicationId);
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.StatusEvent.ID, status.getId());
		response.put(Messages.StatusEvent.NAME, status.getName());
		response.put(Messages.StatusEvent.APPLICATION_STATE, status.getApplicationState());
		response.put(Messages.StatusEvent.PAST_APPLICATION_STATES, status.getPastApplicationStates());
		
		return Converter.reply(response);
	}
	

	public Msg processSetResultRequest(JSONObject request, byte[] data, Manager manager) {
		
		Log.logger().fine("Received SetResult request");
		
		int applicationId = JSON.getInt(request, Messages.SetResultRequest.ID);

		try {
			manager.setApplicationResult(applicationId, data);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}


	public Msg processRequestPortV0Request(JSONObject request, Manager manager) {

		Log.logger().fine("Received RequestPortV0 request");
		
		int applicationId = JSON.getInt(request, Messages.RequestPortV0Request.ID);
		String portName = JSON.getString(request, Messages.RequestPortV0Request.NAME);
		
		try {
			int port = manager.requestPortForApplication(applicationId, portName);
			if (port != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, port);
				response.put(Messages.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -1);
				response.put(Messages.RequestResponse.MESSAGE, "The port already exists");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processConnectPortV0Request(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ConnectPortV0 request");
		
		int applicationId = JSON.getInt(request, Messages.ConnectPortV0Request.ID);
		String portName = JSON.getString(request, Messages.ConnectPortV0Request.NAME);
		
		try {
			int port = manager.connectPortForApplication(applicationId, portName);
			if (port != -1) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, port);
				response.put(Messages.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -1);
				response.put(Messages.RequestResponse.MESSAGE, "The port does not exist");
				
				return Converter.reply(response);
			}
			
		} catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRemovePortV0Request(JSONObject request, Manager manager) {

		Log.logger().fine("Received RemovePortV0 request");
		
		int applicationId = JSON.getInt(request, Messages.RemovePortV0Request.ID);
		String portName = JSON.getString(request, Messages.RemovePortV0Request.NAME);
		
		try {
			boolean done = manager.removePortForApplication(applicationId, portName);
			if (done) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, 0);
				response.put(Messages.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -1);
				response.put(Messages.RequestResponse.MESSAGE, "Cannot remove the port");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}	
	}

	public Msg processAttachUnmanagedRequest(JSONObject request, Manager manager) {

		Log.logger().fine("Received StartedUnmanaged request");
		
		int applicationId = 0;
		String name = JSON.getString(request, Messages.AttachUnmanagedRequest.NAME);
		
		try {
			// Set the PID if it is passed.
			if (request.containsKey(Messages.AttachUnmanagedRequest.PID)) {
				int pid = JSON.getInt(request, Messages.AttachUnmanagedRequest.PID);
				applicationId = manager.newStartedUnmanagedApplication(name, pid);
			}
			else {
				applicationId = manager.newStartedUnmanagedApplication(name);
			}
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, applicationId);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);			
		} 
	}

	public Msg processDetachUnmanagedRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received TerminatedUnmanaged request");
		
		int applicationId = JSON.getInt(request, Messages.DetachUnmanagedRequest.ID);
		
		try {
			String applicationName = manager.setUnmanagedApplicationTerminated(applicationId);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}		
	}

	public Msg processVersion(Version version) {
		
		Log.logger().fine("Received Version request");
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.VersionResponse.MAJOR, version.major);
		response.put(Messages.VersionResponse.MINOR, version.minor);
		response.put(Messages.VersionResponse.REVISION, version.revision);
		
		return Converter.reply(response);
	}

	public Msg processStoreKeyValue(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received StoreKeyValue request");
		
		int applicationId = JSON.getInt(request, Messages.StoreKeyValueRequest.ID);
		String key = JSON.getString(request, Messages.StoreKeyValueRequest.KEY);
		String value = JSON.getString(request, Messages.StoreKeyValueRequest.VALUE);
		
		try {
			manager.storeKeyValue(applicationId, key, value);
						
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (KeyAlreadyExistsException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -2);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
						
			return Converter.reply(response);	
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}		
	}

	public Msg processGetKeyValue(JSONObject request, Manager manager) {

		Log.logger().fine("Received GetKeyValue request");
		
		int applicationId = JSON.getInt(request, Messages.GetKeyValueRequest.ID);
		String key = JSON.getString(request, Messages.GetKeyValueRequest.KEY);
		
		try {
			String value = manager.getKeyValue(applicationId, key);
			
			if (value != null) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, 0);
				response.put(Messages.RequestResponse.MESSAGE, value);
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -2);
				response.put(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRemoveKeyValue(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received RemoveKey request");
		
		int applicationId = JSON.getInt(request, Messages.RemoveKeyRequest.ID);
		String key = JSON.getString(request, Messages.RemoveKeyRequest.KEY);
		
		try {
			boolean exists = manager.removeKey(applicationId, key);
			
			if (exists) {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, 0);
				response.put(Messages.RequestResponse.MESSAGE, "OK");
				
				return Converter.reply(response);
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -2);
				response.put(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				return Converter.reply(response);
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processRequestPortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received RequestPort request");
		
		int applicationId = JSON.getInt(request, Messages.RequestPortRequest.ID);
		
		try {
			// Request a port.
			int port = manager.requestPort(applicationId);

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, port);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processPortUnavailableRequest(JSONObject request, Manager manager) {
				
		Log.logger().fine("Received PortUnavailable request");
		
		int applicationId = JSON.getInt(request, Messages.PortUnavailableRequest.ID);
		int port = JSON.getInt(request, Messages.PortUnavailableRequest.PORT);

		try {
			// Set the port unavailable.
			manager.setPortUnavailable(applicationId, port);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			return Converter.reply(response);
		}
	}

	public Msg processReleasePortRequest(JSONObject request, Manager manager) {
		
		Log.logger().fine("Received ReleasePort request");
		
		int applicationId = JSON.getInt(request, Messages.ReleasePortRequest.ID);
		int port = JSON.getInt(request, Messages.ReleasePortRequest.PORT);
		
		try {
			// Release the port.
			manager.releasePort(applicationId, port);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			return Converter.reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
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
			portInfo.put(Messages.PortInfo.PORT, port.getPort());
			portInfo.put(Messages.PortInfo.STATUS, port.getStatus());
			portInfo.put(Messages.PortInfo.OWNER, port.getApplicationNameId());
			
			array.add(portInfo);
		}
		
		response.put(Messages.PortInfoListResponse.PORT_INFO, array);
		
		return Converter.reply(response);
	}
	
}