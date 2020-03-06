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

import com.google.protobuf.ByteString;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.exception.ApplicationAlreadyRunning;
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
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.manager.StatusInfo;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.proto.Messages.ConnectPortCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPublisherCommand;
import fr.ill.ics.cameo.proto.Messages.CreatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.PublisherResponse;
import fr.ill.ics.cameo.proto.Messages.RemovePortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestPortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;
import fr.ill.ics.cameo.proto.Messages.SetResultCommand;
import fr.ill.ics.cameo.proto.Messages.StartedUnmanagedCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatedUnmanagedCommand;

/**
 * 
 */
public class RequestProcessor {

	public Msg reply(JSONObject response) {
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toJSONString().getBytes(Message.CHARSET));
		
		return reply;
	}
	
	/**
	 * verify connection
	 * @param manager 
	 * 
	 */
	public Msg processSync(Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received Sync message");
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("Connection OK");
		
		// Send sync message for synchronizing subscribers.
		manager.sendStatus(-1, "", ApplicationState.UNKNOWN, ApplicationState.UNKNOWN);
		
		return reply;
	}

	/**
	 * start command
	 * 
	 * @param message
	 * @return
	 */
	public Msg processStartRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received Start request");
		
		try {
			// Convert the args.
			String[] args = null;
			JSONArray list = (JSONArray)request.get(Message.StartRequest.ARGS);
			if (list != null) {
				args = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					args[i] = (String)list.get(i);
				}
			}
			
			// Start the application.
			Application application = manager.startApplication((String)request.get(Message.StartRequest.NAME), args, (String)request.get(Message.StartRequest.INSTANCE_REFERENCE));
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, application.getId());
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return reply(response);
		}
		catch (UnknownApplicationException | MaxNumberOfApplicationsReached | ApplicationAlreadyRunning e) {
			
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, Long.valueOf(-1));
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());

			return reply(response);
		}
	}

	/**
	 * showAll command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processShowAllRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received ShowAll request");
		
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
	
		return reply(response);
	}

	/**
	 * stop command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processStopRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received Stop request");
		
		try {
			String applicationName = manager.stopApplication(((Long)request.get(Message.StopRequest.ID)).intValue());

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, applicationName);
			
			return reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return reply(response);
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
		
		LogInfo.getInstance().getLogger().fine("Received Kill request");
		
		try {
			String applicationName = manager.killApplication(((Long)request.get(Message.StopRequest.ID)).intValue());

			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, applicationName);
			
			return reply(response);
		}
		catch (IdNotFoundException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return reply(response);
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
		
		LogInfo.getInstance().getLogger().fine("Received Connect request");
		
		String applicationName = (String)request.get(Message.ConnectRequest.NAME);
		
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
	
		return reply(response);
	}
	
	/**
	 * ShowCommand
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processShowStreamRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received ShowStream request");
				
		try {
			int port = manager.showStream(((Long)request.get(Message.ShowStreamRequest.ID)).intValue());
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, port);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return reply(response);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException | IdNotFoundException | UnknownApplicationException | StreamNotPublishedException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return reply(response);
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
		
		LogInfo.getInstance().getLogger().fine("Received IsAlive request");
		
		boolean isAlive = manager.isAlive(((Long)request.get(Message.IsAliveRequest.ID)).intValue());
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.IsAliveResponse.IS_ALIVE, isAlive);
		
		return reply(response);
	}

	/**
	 * sent parameters
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processSendParametersRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received SendParameters request");
		
		// Convert the parameters.
		JSONArray list = (JSONArray)request.get(Message.SendParametersRequest.PARAMETERS);
		
		String[] parametersArray = new String[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			parametersArray[i] = (String)list.get(i);
		}
		
		try {
			manager.writeToInputStream(((Long)request.get(Message.SendParametersRequest.ID)).intValue(), parametersArray);
			
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, 0);
			response.put(Message.RequestResponse.MESSAGE, "OK");
			
			return reply(response);
			
		} catch (IdNotFoundException | UnmanagedApplicationException e) {
			// Return the reply.
			JSONObject response = new JSONObject();
			response.put(Message.RequestResponse.VALUE, -1);
			response.put(Message.RequestResponse.MESSAGE, e.getMessage());
			
			return reply(response);
		}
	}

	/**
	 * StatusCommand
	 * 
	 * @return
	 */
	public Msg processStatusRequest() {
		
		LogInfo.getInstance().getLogger().fine("Received Status request");
		
		int port = ConfigManager.getInstance().getStreamPort();
				
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.RequestResponse.VALUE, port);
		response.put(Message.RequestResponse.MESSAGE, "OK");
		
		return reply(response);
	}
	
	public Msg processAllAvailableRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received AllAvailable request");
		
		List<ApplicationConfig> list = manager.getAvailableApplications();
		
		JSONObject response = new JSONObject();
		JSONArray array = new JSONArray();
		
		Iterator<ApplicationConfig> it = list.iterator();
		while (it.hasNext()) {
			ApplicationConfig application = (ApplicationConfig) it.next();

			JSONObject applicationInfo = new JSONObject();
			applicationInfo.put(Message.ApplicationConfig.NAME, application.getName());
			applicationInfo.put(Message.ApplicationConfig.DESCRIPTION, application.getDescription());
			applicationInfo.put(Message.ApplicationConfig.RUNS_SINGLE, application.runsSingle());
			applicationInfo.put(Message.ApplicationConfig.RESTART, application.isRestart());
			applicationInfo.put(Message.ApplicationConfig.STARTING_TIME, application.getStartingTime());
			applicationInfo.put(Message.ApplicationConfig.RETRIES, application.getRetries());
			applicationInfo.put(Message.ApplicationConfig.STOPPING_TIME, application.getStoppingTime());
			
			array.add(applicationInfo);
		}
		
		response.put(Message.AllAvailableResponse.APPLICATION_CONFIG, array);
		
		return reply(response);
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public Msg processOutputRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received Ouput request");
		
		int port = manager.getApplicationStreamPort((String)request.get(Message.OutputRequest.NAME));
		
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.RequestResponse.VALUE, port);
		response.put(Message.RequestResponse.MESSAGE, "OK");
		
		return reply(response);
	}

	public Msg processSetStatusRequest(JSONObject request, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received SetStatus request");
		
		int applicationId = ((Long)request.get(Message.SetStatusRequest.ID)).intValue();
		int state = ((Long)request.get(Message.SetStatusRequest.APPLICATION_STATE)).intValue();
		
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
				
		return reply(response);
	}
	
	public Msg processGetStatusRequest(JSONObject request, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received GetStatus request");
		
		int applicationId = ((Long)request.get(Message.GetStatusRequest.ID)).intValue();
		
		StatusInfo status = manager.getApplicationState(applicationId);
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.StatusEvent.ID, status.getId());
		response.put(Message.StatusEvent.NAME, status.getName());
		response.put(Message.StatusEvent.APPLICATION_STATE, status.getApplicationState());
		response.put(Message.StatusEvent.PAST_APPLICATION_STATES, status.getPastApplicationStates());
		
		return reply(response);
	}
	

	public Msg processSetResultRequest(JSONObject request, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received SetResult request TO IMPLEMENT");
		
		int applicationId = ((Long)request.get(Message.SetResultRequest.ID)).intValue();
		
//		ByteString data = command.getData();
//		
		RequestResponse response = null;
//		
//		try {
//			manager.setApplicationResult(applicationId, data);
//			response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
//			
//		} catch (IdNotFoundException e) {
//			response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
//		}
		
		Zmq.Msg reply = new Zmq.Msg();
		
		reply.add(response.toByteArray());
		return reply;
	}


	public Msg processRequestPortCommand(RequestPortCommand message, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received RequestPortCommand message");
		
		int applicationId = message.getId();
		String portName = message.getName();
		
		RequestResponse response = null;
		try {
			int port = manager.requestPortForApplication(applicationId, portName);
			if (port != -1) {
				response = RequestResponse.newBuilder()
								.setValue(port)
								.setMessage("OK").build();
			} else {
				response = RequestResponse.newBuilder()
								.setValue(-1)
								.setMessage("The port already exists").build();
			}
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder()
								.setValue(-1)
								.setMessage(e.getMessage()).build();
		}
			
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processConnectPortCommand(ConnectPortCommand message, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received ConnectPortCommand message");
		
		int applicationId = message.getId();
		String portName = message.getName();
		
		RequestResponse response = null;
		try {
			int port = manager.connectPortForApplication(applicationId, portName);
			if (port != -1) {
				response = RequestResponse.newBuilder()
								.setValue(port)
								.setMessage("OK").build();
			} else {
				response = RequestResponse.newBuilder()
								.setValue(-1)
								.setMessage("The port does not exist").build();
			}
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder()
								.setValue(-1)
								.setMessage(e.getMessage()).build();
		}
			
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processRemovePortCommand(RemovePortCommand message, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received RemovePortCommand message");
		
		int applicationId = message.getId();
		String portName = message.getName();
		
		RequestResponse response = null;
		
		try {
			boolean done = manager.removePortForApplication(applicationId, portName);
			if (done) {
				response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
			} else {
				response = RequestResponse.newBuilder().setValue(-1).setMessage("Cannot remove the port").build();
			}
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
		}	
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;
	}
	
	public Msg processCreatePublisherCommand(CreatePublisherCommand message, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received CreatePublisherCommand message");
		
		int applicationId = message.getId();
		String publisherName = message.getName();
		
		PublisherResponse response = null;
		
		try {
			int[] ports = manager.createPublisherForApplication(applicationId, publisherName, message.getNumberOfSubscribers());
			if (ports[0] != -1) {
				response = PublisherResponse.newBuilder()
								.setPublisherPort(ports[0])
								.setSynchronizerPort(ports[1])
								.setMessage("OK").build();
			} else {
				response = PublisherResponse.newBuilder()
								.setPublisherPort(-1)
								.setSynchronizerPort(-1)
								.setMessage("The publisher already exists").build();
			}
			
		} catch (IdNotFoundException e) {
			response = PublisherResponse.newBuilder()
								.setPublisherPort(-1)
								.setSynchronizerPort(-1)
								.setMessage(e.getMessage()).build();
		}	
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processTerminatePublisherCommand(TerminatePublisherCommand message, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received TerminatePublisherCommand message");
		
		int applicationId = message.getId();
		String publisherName = message.getName();
		
		RequestResponse response = null;
		
		try {
			boolean done = manager.terminatePublisherForApplication(applicationId, publisherName);
			if (done) {
				response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
			} else {
				response = RequestResponse.newBuilder().setValue(-1).setMessage("Cannot terminate the publisher").build();
			}
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
		}	
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processConnectPublisherCommand(ConnectPublisherCommand message, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received ConnectPublisherCommand message");
		
		PublisherResponse response = null;
		
		try {
			 Application.Publisher publisher = manager.getPublisherForApplication(message.getApplicationId(), message.getPublisherName());
			 int[] ports = manager.getPublisherPortsForApplication(message.getApplicationId(), message.getPublisherName());
			 
			 
			 if (ports[0] != -1) {
				response = PublisherResponse.newBuilder().setMessage("OK")
														.setPublisherPort(ports[0])
														.setSynchronizerPort(ports[1])
														.setNumberOfSubscribers(publisher.numberOfSubscribers)
														.build();
			} else {
				response = PublisherResponse.newBuilder()
								.setPublisherPort(-1)
								.setSynchronizerPort(-1)
								.setNumberOfSubscribers(-1)
								.setMessage("The publisher does not exist").build();
			}
		} catch (IdNotFoundException e) {
			response = PublisherResponse.newBuilder()
								.setMessage(e.getMessage())
								.setPublisherPort(-1)
								.setSynchronizerPort(-1)
								.setNumberOfSubscribers(-1)
								.build();
			
		} catch (UnknownPublisherException e) {
			response = PublisherResponse.newBuilder()
								.setMessage(e.getMessage())
								.setPublisherPort(-1)
								.setSynchronizerPort(-1)
								.setNumberOfSubscribers(-1)
								.build();
		}
				
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		
		return reply;		
	}

	public Msg processStartedUnmanagedCommand(StartedUnmanagedCommand message, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received StartedUnmanagedCommand message");
		
		int applicationId = 0;
		
		try {
			if (message.hasPid()) {
				applicationId = manager.newStartedUnmanagedApplication(message.getName(), message.getPid());
			} else {
				applicationId = manager.newStartedUnmanagedApplication(message.getName());	
			}
			
		} catch (MaxNumberOfApplicationsReached | ApplicationAlreadyRunning e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder()
												.setValue(-1)
												.setMessage(e.getMessage())
												.build();
			reply.add(response.toByteArray());
			return reply;
		} 
		
		Zmq.Msg reply = new Zmq.Msg();
		RequestResponse response = RequestResponse.newBuilder()
												.setValue(applicationId)
												.setMessage("OK")
												.build();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processTerminatedUnmanagedCommand(TerminatedUnmanagedCommand message, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received TerminatedUnmanagedCommand message");
		
		try {
			String applicationName = manager.setUnmanagedApplicationTerminated(message.getId());

			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(0).setMessage(applicationName).build();
			reply.add(response.toByteArray());
			
			return reply;
			
		} catch (IdNotFoundException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			
			return reply;
		}		
	}
	
}