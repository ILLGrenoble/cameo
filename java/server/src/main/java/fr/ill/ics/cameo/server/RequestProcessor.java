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
import fr.ill.ics.cameo.exception.UnregisteredApplicationException;
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
import fr.ill.ics.cameo.strings.StringId;

/**
 * 
 */
public class RequestProcessor {
	
	/**
	 * verify connection
	 * @param reply 
	 * @param manager 
	 * 
	 */
	public void processSync(Msg reply, Manager manager) {
		
		Log.logger().fine("Received Sync message");
		
		// Send sync message for synchronizing subscribers.
		manager.sendStatus(-1, "", ApplicationState.UNKNOWN, ApplicationState.UNKNOWN, -1);
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, 0);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}
	
	public void processSyncStream(JSONObject request, Msg reply, Manager manager) {

		Log.logger().fine("Received SyncStream message");
		
		// Get the publisher.
		String applicationName = JSON.getString(request, Messages.SyncStreamRequest.NAME);
		Zmq.Socket publisher = manager.getStreamPublisher(applicationName);
		
		// Publish a SYNCSTREAM event.
		if (publisher != null) {
			JSONObject event = new JSONObject();
			event.put(Messages.TYPE, Messages.SYNC_STREAM);

			// Get the topic id.
			String topicId = StringId.from(Messages.Event.STREAM, applicationName);
			
			// Synchronize the publisher as it is accessed by the stream threads.
			Manager.publishSynchronized(publisher, topicId, Messages.serialize(event));
		}
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, 0);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}

	/**
	 * start command
	 * @param reply 
	 * 
	 * @param message
	 * @return
	 */
	public void processStartRequest(JSONObject request, Msg reply, Manager manager) {
		
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
			
			int starterProxyPort = 0;
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
				
				starterProxyPort = JSON.getInt(request, Messages.ApplicationIdentity.STARTER_PROXY_PORT);
			}
			
			// Start the application.
			Application application = manager.startApplication(JSON.getString(request, Messages.StartRequest.NAME), 
																args,
																starter, 
																starterProxyPort);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, application.getId());
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (UnknownApplicationException | MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, Long.valueOf(-1));
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());

			reply.add(Messages.serialize(response));
		}
	}

	/**
	 * showAll command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processAppsRequest(JSONObject request, Msg reply, Manager manager) {
		
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
	
		reply.add(Messages.serialize(response));
	}

	public void processSetStopHandlerRequest(JSONObject request, Msg reply, Manager manager) {
	
		Log.logger().fine("Received SetStopHandler request");
		
		try {
			manager.setApplicationStopHandler(JSON.getInt(request, Messages.SetStopHandlerRequest.ID), JSON.getInt(request, Messages.SetStopHandlerRequest.STOPPING_TIME));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	
	/**
	 * stop command
	 * @param reply 
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processStopRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received Stop request");
		
		try {
			String applicationName = manager.stopApplication(JSON.getInt(request, Messages.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	/**
	 * kill command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processKillRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received Kill request");
		
		try {
			String applicationName = manager.killApplication(JSON.getInt(request, Messages.StopRequest.ID));

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	/**
	 * connect command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processConnectRequest(JSONObject request, Msg reply, Manager manager) {
		
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
	
		reply.add(Messages.serialize(response));
	}

	public void processConnectWithIdRequest(JSONObject request, Msg reply, Manager manager) {
		
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
	
		reply.add(Messages.serialize(response));
	}
	
	/**
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processOutputPortWithIdRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received OutputPortWithId request");
				
		try {
			int port = manager.getStreamPort(JSON.getInt(request, Messages.OutputPortWithIdRequest.ID));
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, port);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (java.lang.ArrayIndexOutOfBoundsException | IdNotFoundException | UnknownApplicationException | StreamNotPublishedException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}
	
	/**
	 * isAlive command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processIsAliveRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received IsAlive request");
		
		boolean isAlive = manager.isAlive(JSON.getInt(request, Messages.IsAliveRequest.ID));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.IsAliveResponse.IS_ALIVE, isAlive);
		
		reply.add(Messages.serialize(response));
	}

	/**
	 * sent parameters
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processWriteInputRequest(JSONObject request, Msg reply, Manager manager) {
		
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
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException | UnregisteredApplicationException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	/**
	 * StatusCommand
	 * 
	 * @return
	 */
	public void processStatusRequest(Msg reply) {
		
		Log.logger().fine("Received Status request");
		
		int port = ConfigManager.getInstance().getStreamPort();
				
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}
	
	public void processListRequest(JSONObject request, Msg reply, Manager manager) {
		
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
		
		reply.add(Messages.serialize(response));
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public void processOutputPortRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received OuputPort request");
		
		int port = manager.getApplicationStreamPort(JSON.getString(request, Messages.OutputRequest.NAME));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}
	
	/**
	 * 
	 * @return
	 */
	public void processPublisherProxyPortRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received PublisherProxyPort request");
		
		int port = ConfigManager.getInstance().getPublisherProxyPort();
				
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}

	public void processSubscriberProxyPortRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received SubscriberProxyPort request");
		
		int port = ConfigManager.getInstance().getSubscriberProxyPort();
				
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, port);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(response));
	}
	
	public void processSetStatusRequest(JSONObject request, Msg reply, Manager manager) {

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
				
		reply.add(Messages.serialize(response));
	}
	
	public void processGetStatusRequest(JSONObject request, Msg reply, Manager manager) {

		Log.logger().fine("Received GetStatus request");
		
		int applicationId = JSON.getInt(request, Messages.GetStatusRequest.ID);
		
		StatusInfo status = manager.getApplicationState(applicationId);
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.StatusEvent.ID, status.getId());
		response.put(Messages.StatusEvent.NAME, status.getName());
		response.put(Messages.StatusEvent.APPLICATION_STATE, status.getApplicationState());
		response.put(Messages.StatusEvent.PAST_APPLICATION_STATES, status.getPastApplicationStates());
		
		reply.add(Messages.serialize(response));
	}
	

	public void processSetResultRequest(JSONObject request, byte[] data, Msg reply, Manager manager) {
		
		Log.logger().fine("Received SetResult request");
		
		int applicationId = JSON.getInt(request, Messages.SetResultRequest.ID);

		try {
			manager.setApplicationResult(applicationId, data);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processAttachUnregisteredRequest(JSONObject request, Msg reply, Manager manager) {

		Log.logger().fine("Received AttachUnregistered request");
		
		int applicationId = 0;
		String name = JSON.getString(request, Messages.AttachUnregisteredRequest.NAME);
		
		try {
			// Set the PID if it is passed.
			if (request.containsKey(Messages.AttachUnregisteredRequest.PID)) {
				int pid = JSON.getInt(request, Messages.AttachUnregisteredRequest.PID);
				applicationId = manager.newStartedUnregisteredApplication(name, pid);
			}
			else {
				applicationId = manager.newStartedUnregisteredApplication(name);
			}
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, applicationId);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (MaxNumberOfApplicationsReached | ApplicationAlreadyExecuting e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));			
		} 
	}

	public void processDetachUnregisteredRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received DetachUnregistered request");
		
		int applicationId = JSON.getInt(request, Messages.DetachUnregisteredRequest.ID);
		
		try {
			String applicationName = manager.setUnregisteredApplicationTerminated(applicationId);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, 0);
			response.put(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}		
	}

	public void processVersion(Version version, Msg reply) {
		
		Log.logger().fine("Received Version request");
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.VersionResponse.MAJOR, version.major);
		response.put(Messages.VersionResponse.MINOR, version.minor);
		response.put(Messages.VersionResponse.REVISION, version.revision);
		
		reply.add(Messages.serialize(response));
	}

	public void processStoreKeyValue(JSONObject request, Msg reply, Manager manager) {
		
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
			
			reply.add(Messages.serialize(response));
		}
		catch (KeyAlreadyExistsException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -2);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
						
			reply.add(Messages.serialize(response));	
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}		
	}

	public void processGetKeyValue(JSONObject request, Msg reply, Manager manager) {

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
				
				reply.add(Messages.serialize(response));
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -2);
				response.put(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				reply.add(Messages.serialize(response));
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processRemoveKeyValue(JSONObject request, Msg reply, Manager manager) {
		
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
				
				reply.add(Messages.serialize(response));
			}
			else {
				// Return the reply.
				JSONObject response = new JSONObject();
				response.put(Messages.RequestResponse.VALUE, -2);
				response.put(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				reply.add(Messages.serialize(response));
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processRequestPortRequest(JSONObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received RequestPort request");
		
		int applicationId = JSON.getInt(request, Messages.RequestPortRequest.ID);
		
		try {
			// Request a port.
			int port = manager.requestPort(applicationId);

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, port);
			response.put(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processPortUnavailableRequest(JSONObject request, Msg reply, Manager manager) {
				
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
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processReleasePortRequest(JSONObject request, Msg reply, Manager manager) {
		
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
			
			reply.add(Messages.serialize(response));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Messages.RequestResponse.VALUE, -1);
			response.put(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(response));
		}
	}

	public void processPortsRequest(JSONObject request, Msg reply, Manager manager) {
		
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
		
		reply.add(Messages.serialize(response));
	}
	
}