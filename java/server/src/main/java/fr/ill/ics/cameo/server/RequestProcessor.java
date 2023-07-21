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

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.exception.IdNotFoundException;
import fr.ill.ics.cameo.exception.KeyAlreadyExistsException;
import fr.ill.ics.cameo.exception.MaxGlobalNumberOfApplicationsReached;
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
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

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
		
		Log.logger().finest("Received Sync message");
		
		// Send sync message for synchronizing subscribers.
		manager.sendStatus(-1, "", ApplicationState.NIL, ApplicationState.NIL, -1);
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, 0);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	public void processSyncStream(JsonObject request, Msg reply, Manager manager) {

		Log.logger().finest("Received SyncStream request " + request);
		
		// Get the publisher.
		String applicationName = JSON.getString(request, Messages.SyncStreamRequest.NAME);
		Zmq.Socket publisher = manager.getStreamPublisher(applicationName);
		
		// Publish a SYNCSTREAM event.
		if (publisher != null) {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add(Messages.TYPE, Messages.SYNC_STREAM);

			// Get the topic id.
			String topicId = StringId.from(Messages.Event.STREAM, applicationName);
			
			// Synchronize the publisher as it is accessed by the stream threads.
			Manager.publishSynchronized(publisher, topicId, Messages.serialize(builder.build()));
		}
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, 0);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	/**
	 * start command
	 * @param reply 
	 * 
	 * @param message
	 * @return
	 */
	public void processStartRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received Start request " + request);
		
		try {
			// Convert the args.
			String[] args = null;
			JsonArray list = JSON.getArray(request, Messages.StartRequest.ARGS);
			if (list != null) {
				args = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					args[i] = list.getString(i);
				}
			}
			
			JsonObject starterObject = JSON.getObject(request, Messages.ApplicationIdentity.STARTER);
			
			int starterProxyPort = 0;
			boolean starterLinked = false;
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
				
				starterProxyPort = JSON.getInt(request, Messages.StartRequest.STARTER_PROXY_PORT);
				starterLinked = JSON.getBoolean(request, Messages.StartRequest.STARTER_LINKED);
			}
			
			// Start the application.
			Application application = manager.startApplication(JSON.getString(request, Messages.StartRequest.NAME), 
																args,
																starter, 
																starterProxyPort,
																starterLinked);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, application.getId());
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (UnknownApplicationException | MaxNumberOfApplicationsReached | MaxGlobalNumberOfApplicationsReached e) {
			
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, Long.valueOf(-1));
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());

			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	/**
	 * showAll command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processAppsRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received Apps request " + request);
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JsonObjectBuilder appInfoBuilder = Json.createObjectBuilder();
			appInfoBuilder.add(Messages.ApplicationInfo.ID, application.getId());
			appInfoBuilder.add(Messages.ApplicationInfo.ARGS, application.getArgs());
			appInfoBuilder.add(Messages.ApplicationInfo.NAME, application.getName());
			appInfoBuilder.add(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
			appInfoBuilder.add(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
			appInfoBuilder.add(Messages.ApplicationInfo.PID, application.getPid());
			
			arrayBuilder.add(appInfoBuilder.build());
		}
		responseBuilder.add(Messages.ApplicationInfoListResponse.APPLICATION_INFO, arrayBuilder.build());
	
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	public void processSetStopHandlerRequest(JsonObject request, Msg reply, Manager manager) {
	
		Log.logger().finest("Received SetStopHandler request" + request);
		
		try {
			manager.setApplicationStopHandler(JSON.getInt(request, Messages.SetStopHandlerRequest.ID), JSON.getInt(request, Messages.SetStopHandlerRequest.STOPPING_TIME));

			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
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
	public void processStopRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().fine("Received Stop request " + request);
		
		try {
			String applicationName = manager.stopApplication(JSON.getInt(request, Messages.StopRequest.ID), JSON.getBoolean(request, Messages.StopRequest.LINK));

			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	/**
	 * kill command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processKillRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received Kill request " + request);
		
		try {
			String applicationName = manager.killApplication(JSON.getInt(request, Messages.StopRequest.ID));

			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	/**
	 * connect command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processConnectRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received Connect request " + request);
		
		String applicationName = JSON.getString(request, Messages.ConnectRequest.NAME);
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JsonObjectBuilder appInfoBuilder = Json.createObjectBuilder();
			
			// Filtering on the application name.
			if (applicationName.equals(application.getName())) {
				appInfoBuilder.add(Messages.ApplicationInfo.ID, application.getId());
				appInfoBuilder.add(Messages.ApplicationInfo.ARGS, application.getArgs());
				appInfoBuilder.add(Messages.ApplicationInfo.NAME, application.getName());
				appInfoBuilder.add(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				appInfoBuilder.add(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				appInfoBuilder.add(Messages.ApplicationInfo.PID, application.getPid());
				
				arrayBuilder.add(appInfoBuilder);
			}
		}
		responseBuilder.add(Messages.ApplicationInfoListResponse.APPLICATION_INFO, arrayBuilder.build());
	
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	public void processConnectWithIdRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received ConnectWithId request " + request);
		
		int applicationId = JSON.getInt(request, Messages.ConnectWithIdRequest.ID);
		
		LinkedList<ApplicationInfo> list = manager.getApplicationInfos();
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		Iterator<ApplicationInfo> it = list.iterator();
		while (it.hasNext()) {
			ApplicationInfo application = it.next();
			
			JsonObjectBuilder appInfoBuilder = Json.createObjectBuilder();
			
			// Filtering on the application name.
			if (applicationId == application.getId()) {
				appInfoBuilder.add(Messages.ApplicationInfo.ID, application.getId());
				appInfoBuilder.add(Messages.ApplicationInfo.ARGS, application.getArgs());
				appInfoBuilder.add(Messages.ApplicationInfo.NAME, application.getName());
				appInfoBuilder.add(Messages.ApplicationInfo.APPLICATION_STATE, application.getApplicationState());
				appInfoBuilder.add(Messages.ApplicationInfo.PAST_APPLICATION_STATES, application.getPastApplicationStates());
				appInfoBuilder.add(Messages.ApplicationInfo.PID, application.getPid());
				
				arrayBuilder.add(appInfoBuilder.build());
			}
		}
		responseBuilder.add(Messages.ApplicationInfoListResponse.APPLICATION_INFO, arrayBuilder.build());
	
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	/**
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processOutputPortWithIdRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received OutputPortWithId request " + request);
				
		try {
			int port = manager.getStreamPort(JSON.getInt(request, Messages.OutputPortWithIdRequest.ID));
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, port);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (java.lang.ArrayIndexOutOfBoundsException | IdNotFoundException | UnknownApplicationException | StreamNotPublishedException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}
	
	/**
	 * isAlive command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processIsAliveRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received IsAlive request " + request);
		
		boolean isAlive = manager.isAlive(JSON.getInt(request, Messages.IsAliveRequest.ID));
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.IsAliveResponse.IS_ALIVE, isAlive);
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	/**
	 * sent parameters
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public void processWriteInputRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received WriteInput request " + request);
		
		// Convert the parameters.
		JsonArray list = JSON.getArray(request, Messages.WriteInputRequest.PARAMETERS);
		
		String[] inputArray = new String[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			inputArray[i] = list.getString(i);
		}
		
		try {
			manager.writeToInputStream((JSON.getInt(request, Messages.WriteInputRequest.ID)), inputArray);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException | UnregisteredApplicationException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	/**
	 * StatusCommand
	 * 
	 * @return
	 */
	public void processStatusRequest(Msg reply) {
				
		Log.logger().finest("Received Status request");
		
		int port = ConfigManager.getInstance().getStreamPort();
				
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, port);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	public void processListRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received List request " + request);
		
		List<ApplicationConfig> list = manager.getAvailableApplications();
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		Iterator<ApplicationConfig> it = list.iterator();
		while (it.hasNext()) {
			ApplicationConfig application = (ApplicationConfig) it.next();

			JsonObjectBuilder configBuilder = Json.createObjectBuilder();
			configBuilder.add(Messages.ApplicationConfig.NAME, application.getName());
			configBuilder.add(Messages.ApplicationConfig.DESCRIPTION, application.getDescription());
			configBuilder.add(Messages.ApplicationConfig.RUNS_SINGLE, application.runSingle());
			configBuilder.add(Messages.ApplicationConfig.MULTIPLE, application.runMaxApplications());
			configBuilder.add(Messages.ApplicationConfig.RESTART, application.isRestart());
			configBuilder.add(Messages.ApplicationConfig.STARTING_TIME, application.getStartingTime());
			configBuilder.add(Messages.ApplicationConfig.STOPPING_TIME, application.getStoppingTime());
			
			arrayBuilder.add(configBuilder.build());
		}
		
		responseBuilder.add(Messages.ApplicationConfigListResponse.APPLICATION_CONFIG, arrayBuilder.build());
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public void processOutputPortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received OuputPort request " + request);
		
		int port = manager.getApplicationStreamPort(JSON.getString(request, Messages.OutputRequest.NAME));
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, port);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	public void processResponderProxyPortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received ResponderProxyPort request " + request);
		
		int port = ConfigManager.getInstance().getResponderProxyPort();
				
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, port);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	public void processPublisherProxyPortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received PublisherProxyPort request " + request);
		
		int port = ConfigManager.getInstance().getPublisherProxyPort();
				
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, port);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	public void processSubscriberProxyPortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received SubscriberProxyPort request " + request);
		
		int port = ConfigManager.getInstance().getSubscriberProxyPort();
				
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.RequestResponse.VALUE, port);
		responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	public void processSetStatusRequest(JsonObject request, Msg reply, Manager manager) {

		Log.logger().finest("Received SetStatus request " + request);
		
		int applicationId = JSON.getInt(request, Messages.SetStatusRequest.ID);
		int state = JSON.getInt(request, Messages.SetStatusRequest.APPLICATION_STATE);
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		
		try {
			boolean done = manager.setApplicationStateFromClient(applicationId, state);
					
			if (done) {
				responseBuilder.add(Messages.RequestResponse.VALUE, 0);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			}
			else {
				responseBuilder.add(Messages.RequestResponse.VALUE, -1);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, "Cannot set the state");
			}
		}
		catch (IdNotFoundException e) {
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
		}
				
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
	public void processGetStatusRequest(JsonObject request, Msg reply, Manager manager) {

		Log.logger().finest("Received GetStatus request " + request);
		
		int applicationId = JSON.getInt(request, Messages.GetStatusRequest.ID);
		
		StatusInfo status = manager.getApplicationState(applicationId);
	
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.StatusEvent.ID, status.getId());
		responseBuilder.add(Messages.StatusEvent.NAME, status.getName());
		responseBuilder.add(Messages.StatusEvent.APPLICATION_STATE, status.getApplicationState());
		responseBuilder.add(Messages.StatusEvent.PAST_APPLICATION_STATES, status.getPastApplicationStates());
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	

	public void processSetResultRequest(JsonObject request, byte[] data, Msg reply, Manager manager) {
		
		Log.logger().finest("Received SetResult request " + request);
		
		int applicationId = JSON.getInt(request, Messages.SetResultRequest.ID);

		try {
			manager.setApplicationResult(applicationId, data);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processAttachUnregisteredRequest(JsonObject request, Msg reply, Manager manager) {

		Log.logger().finest("Received AttachUnregistered request " + request);
		
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
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, applicationId);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (MaxNumberOfApplicationsReached | MaxGlobalNumberOfApplicationsReached e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));			
		} 
	}

	public void processDetachUnregisteredRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received DetachUnregistered request " + request);
		
		int applicationId = JSON.getInt(request, Messages.DetachUnregisteredRequest.ID);
		
		try {
			String applicationName = manager.setUnregisteredApplicationTerminated(applicationId);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, applicationName);
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}		
	}

	public void processVersion(Version version, Msg reply) {
		
		Log.logger().finest("Received Version request");
		
		// Return the reply.
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add(Messages.VersionResponse.MAJOR, version.major);
		responseBuilder.add(Messages.VersionResponse.MINOR, version.minor);
		responseBuilder.add(Messages.VersionResponse.REVISION, version.revision);
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}

	public void processStoreKeyValue(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received StoreKeyValue request " + request);
		
		int applicationId = JSON.getInt(request, Messages.StoreKeyValueRequest.ID);
		String key = JSON.getString(request, Messages.StoreKeyValueRequest.KEY);
		String value = JSON.getString(request, Messages.StoreKeyValueRequest.VALUE);
		
		try {
			manager.storeKeyValue(applicationId, key, value);
						
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (KeyAlreadyExistsException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -2);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
						
			reply.add(Messages.serialize(responseBuilder.build()));	
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}		
	}

	public void processGetKeyValue(JsonObject request, Msg reply, Manager manager) {

		Log.logger().finest("Received GetKeyValue request " + request);
		
		int applicationId = JSON.getInt(request, Messages.GetKeyValueRequest.ID);
		String key = JSON.getString(request, Messages.GetKeyValueRequest.KEY);
		
		try {
			String value = manager.getKeyValue(applicationId, key);
			
			if (value != null) {
				// Return the reply.
				JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
				responseBuilder.add(Messages.RequestResponse.VALUE, 0);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, value);
				
				reply.add(Messages.serialize(responseBuilder.build()));
			}
			else {
				// Return the reply.
				JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
				responseBuilder.add(Messages.RequestResponse.VALUE, -2);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				reply.add(Messages.serialize(responseBuilder.build()));
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processRemoveKeyValue(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received RemoveKey request " + request);
		
		int applicationId = JSON.getInt(request, Messages.RemoveKeyRequest.ID);
		String key = JSON.getString(request, Messages.RemoveKeyRequest.KEY);
		
		try {
			boolean exists = manager.removeKey(applicationId, key);
			
			if (exists) {
				// Return the reply.
				JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
				responseBuilder.add(Messages.RequestResponse.VALUE, 0);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
				
				reply.add(Messages.serialize(responseBuilder.build()));
			}
			else {
				// Return the reply.
				JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
				responseBuilder.add(Messages.RequestResponse.VALUE, -2);
				responseBuilder.add(Messages.RequestResponse.MESSAGE, "Key is undefined");
				
				reply.add(Messages.serialize(responseBuilder.build()));
			}
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processRequestPortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received RequestPort request " + request);
		
		int applicationId = JSON.getInt(request, Messages.RequestPortRequest.ID);
		
		try {
			// Request a port.
			int port = manager.requestPort(applicationId);

			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, port);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processPortUnavailableRequest(JsonObject request, Msg reply, Manager manager) {
				
		Log.logger().finest("Received PortUnavailable request " + request);
		
		int applicationId = JSON.getInt(request, Messages.PortUnavailableRequest.ID);
		int port = JSON.getInt(request, Messages.PortUnavailableRequest.PORT);

		try {
			// Set the port unavailable.
			manager.setPortUnavailable(applicationId, port);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processReleasePortRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received ReleasePort request " + request);
		
		int applicationId = JSON.getInt(request, Messages.ReleasePortRequest.ID);
		int port = JSON.getInt(request, Messages.ReleasePortRequest.PORT);
		
		try {
			// Release the port.
			manager.releasePort(applicationId, port);
			
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, 0);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, "OK");
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
			responseBuilder.add(Messages.RequestResponse.VALUE, -1);
			responseBuilder.add(Messages.RequestResponse.MESSAGE, e.getMessage());
			
			reply.add(Messages.serialize(responseBuilder.build()));
		}
	}

	public void processPortsRequest(JsonObject request, Msg reply, Manager manager) {
		
		Log.logger().finest("Received Ports request " + request);
		
		List<PortInfo> list = manager.getPortList();
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		Iterator<PortInfo> it = list.iterator();
		while (it.hasNext()) {
			PortInfo port = it.next();
			
			JsonObjectBuilder portInfoBuilder = Json.createObjectBuilder();
			portInfoBuilder.add(Messages.PortInfo.PORT, port.getPort());
			portInfoBuilder.add(Messages.PortInfo.STATUS, port.getStatus());
			portInfoBuilder.add(Messages.PortInfo.OWNER, port.getApplicationNameId());
			
			arrayBuilder.add(portInfoBuilder.build());
		}
		
		responseBuilder.add(Messages.PortInfoListResponse.PORT_INFO, arrayBuilder.build());
		
		reply.add(Messages.serialize(responseBuilder.build()));
	}
	
}