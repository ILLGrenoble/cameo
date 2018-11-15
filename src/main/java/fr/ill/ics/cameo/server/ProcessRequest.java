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
import fr.ill.ics.cameo.proto.Messages;
import fr.ill.ics.cameo.proto.Messages.AllAvailableCommand;
import fr.ill.ics.cameo.proto.Messages.AllAvailableResponse;
import fr.ill.ics.cameo.proto.Messages.ApplicationInfoListResponse;
import fr.ill.ics.cameo.proto.Messages.ConnectCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPortCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPublisherCommand;
import fr.ill.ics.cameo.proto.Messages.CreatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.GetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.IsAliveCommand;
import fr.ill.ics.cameo.proto.Messages.IsAliveResponse;
import fr.ill.ics.cameo.proto.Messages.KillCommand;
import fr.ill.ics.cameo.proto.Messages.OutputCommand;
import fr.ill.ics.cameo.proto.Messages.PublisherResponse;
import fr.ill.ics.cameo.proto.Messages.RemovePortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestPortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;
import fr.ill.ics.cameo.proto.Messages.SendParametersCommand;
import fr.ill.ics.cameo.proto.Messages.SetResultCommand;
import fr.ill.ics.cameo.proto.Messages.SetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.ShowAllCommand;
import fr.ill.ics.cameo.proto.Messages.ShowStreamCommand;
import fr.ill.ics.cameo.proto.Messages.StartCommand;
import fr.ill.ics.cameo.proto.Messages.StartedUnmanagedCommand;
import fr.ill.ics.cameo.proto.Messages.StopCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatedUnmanagedCommand;

/**
 * 
 */
public class ProcessRequest {

	/**
	 * verify connection
	 * @param manager 
	 * 
	 */
	public Msg processInit(Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received Init message");
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("Connection OK");
		
		// send sync message for synchronizing subscribers
		manager.sendStatus(-1, "", ApplicationState.UNKNOWN, ApplicationState.UNKNOWN);
		
		return reply;
	}

	/**
	 * start command
	 * 
	 * @param message
	 * @return
	 */
	public Msg processStartCommand(StartCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received StartCommand message");
		
		Application application = null;		
		
		try {
			String[] args = null;
			if (!message.getArgsList().isEmpty()) {
				List<String> list = message.getArgsList();
				args = new String[list.size()];
				Iterator<String> it = list.iterator();
				int i = 0;
				while (it.hasNext()) {
					String element = (String) it.next();
					args[i] = element;
					i++;
				}

			}
			
			// run application
			application = manager.startApplication(message.getName(), args, message.getInstanceReference());
			
		} catch (UnknownApplicationException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder()
												.setValue(-1)
												.setMessage(e.getMessage())
												.build();
			reply.add(response.toByteArray());
			return reply;
			
		} catch (MaxNumberOfApplicationsReached e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder()
												.setValue(-1)
												.setMessage(e.getMessage())
												.build();
			reply.add(response.toByteArray());
			return reply;
			
		} catch (ApplicationAlreadyRunning e) {
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
												.setValue(application.getId())
												.setMessage("OK")
												.build();
		reply.add(response.toByteArray());
		return reply;

	}

	/**
	 * showAll command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processShowAllCommand(ShowAllCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received ShowAllCommand message");
		Zmq.Msg reply = new Zmq.Msg();
		LinkedList<ApplicationInfo> list = manager.showApplicationMap();
		if (list.isEmpty()) {
			ApplicationInfoListResponse response = ApplicationInfoListResponse.newBuilder().build();
			reply.add(response.toByteArray());
			return reply;
		} else {
			LinkedList<Messages.ApplicationInfo> protoList = new LinkedList<Messages.ApplicationInfo>();
			Iterator<ApplicationInfo> it = list.iterator();
			while (it.hasNext()) {
				ApplicationInfo application = it.next();
				
				Messages.ApplicationInfo applicationList = Messages.ApplicationInfo.newBuilder()
														.setId(application.getId())
														.setArgs(application.getArgs())
														.setName(application.getName())
														.setApplicationState(application.getApplicationState())
														.setPastApplicationStates(application.getPastApplicationStates())
														.setPid(application.getPid())
														.build();
				protoList.add(applicationList);

			}
			ApplicationInfoListResponse response = ApplicationInfoListResponse.newBuilder().addAllApplicationInfo(protoList).build();
			reply.add(response.toByteArray());
		}
		return reply;
	}

	/**
	 * stop command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processStopCommand(StopCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received StopCommand message");
		
		try {
			String applicationName = manager.stopApplication(message.getId());

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

	/**
	 * kill command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processKillCommand(KillCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received KillCommand message");
		
		try {
			String applicationName = manager.killApplication(message.getId());
			
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

	/**
	 * connect command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processConnectCommand(ConnectCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received ConnectCommand message");
		Zmq.Msg reply = new Zmq.Msg();
		LinkedList<ApplicationInfo> list = manager.showApplicationMap();
		if (list.isEmpty()) {
			ApplicationInfoListResponse response = ApplicationInfoListResponse.newBuilder().build();
			reply.add(response.toByteArray());
			return reply;
		} else {
			LinkedList<Messages.ApplicationInfo> protoList = new LinkedList<Messages.ApplicationInfo>();
			Iterator<ApplicationInfo> it = list.iterator();
			while (it.hasNext()) {
				ApplicationInfo application = it.next();
				
				// filtering on the application name
				if (message.getName().equals(application.getName())) {
					
					Messages.ApplicationInfo applicationList = Messages.ApplicationInfo.newBuilder()
															.setId(application.getId())
															.setArgs(application.getArgs())
															.setName(application.getName())
															.setApplicationState(application.getApplicationState())
															.setPastApplicationStates(application.getPastApplicationStates())
															.setPid(application.getPid())
															.build();
					protoList.add(applicationList);
				}

			}
			ApplicationInfoListResponse response = ApplicationInfoListResponse.newBuilder().addAllApplicationInfo(protoList).build();
			reply.add(response.toByteArray());
		}
		return reply;
	}
	
	/**
	 * ShowCommand
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processShowStreamCommand(ShowStreamCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received ShowStreamCommand message");
		try {
			int port = manager.showStream(message.getId());
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(port).setMessage("OK").build();
			reply.add(response.toByteArray());
			return reply;
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
		} catch (IdNotFoundException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
		} catch (UnknownApplicationException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
		} catch (StreamNotPublishedException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
		}

	}

	/**
	 * StatusCommand
	 * 
	 * @return
	 */
	public Msg processStatusCommand() {
		LogInfo.getInstance().getLogger().fine("Received StatusCommand message");
		int port = ConfigManager.getInstance().getStreamPort();
		Zmq.Msg reply = new Zmq.Msg();
		RequestResponse response = RequestResponse.newBuilder().setValue(port).setMessage("OK").build();
		reply.add(response.toByteArray());
		return reply;
	}
	
	/**
	 * isAlive command
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processIsAliveCommand(IsAliveCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received IsAliveCommand message");
		boolean isAlive = manager.isAlive(message.getId());
		IsAliveResponse response = IsAliveResponse.newBuilder().setIsAlive(isAlive).build();
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
		return reply;
	}

	/**
	 * sent parameters
	 * 
	 * @param message
	 * @param manager
	 * @return
	 */
	public Msg processSendParametersCommand(SendParametersCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received SendParametersCommand message");

		List<String> list = message.getParametersList();
		String[] parametersArray = new String[list.size()];
		Iterator<String> it = list.iterator();
		int i = 0;
		while (it.hasNext()) {
			String element = (String) it.next();
			parametersArray[i] = element;
			i++;
		}
		try {
			manager.writeToInputStream(message.getId(), parametersArray);
			
		} catch (IdNotFoundException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
			
		} catch (UnmanagedApplicationException e) {
			Zmq.Msg reply = new Zmq.Msg();
			RequestResponse response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
			reply.add(response.toByteArray());
			return reply;
		}
		
		Zmq.Msg reply = new Zmq.Msg();
		RequestResponse response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
		reply.add(response.toByteArray());
		return reply;
	}

	public Msg processAllAvailableCommand(AllAvailableCommand message, Manager manager) {
		LogInfo.getInstance().getLogger().fine("Received AllAvailableCommand message");
		Zmq.Msg reply = new Zmq.Msg();
		
		List<ApplicationConfig> list = manager.getAvailableApplications();
		if (list.isEmpty()) {
			AllAvailableResponse response = AllAvailableResponse.newBuilder().build();
			reply.add(response.toByteArray());
			return reply;
		} else {
			LinkedList<Messages.ApplicationConfig> protoList = new LinkedList<Messages.ApplicationConfig>();
			Iterator<ApplicationConfig> it = list.iterator();
			while (it.hasNext()) {
				ApplicationConfig application = (ApplicationConfig) it.next();

				Messages.ApplicationConfig protoApplication = Messages.ApplicationConfig.newBuilder()
															.setName(application.getName())
															.setDescription(application.getDescription())
															.setRunsSingle(application.runsSingle())
															.setRestart(application.isRestart())
															.setStartingTime(application.getStartingTime())
															.setRetries(application.getRetries())
															.setStoppingTime(application.getStoppingTime())
															.build();
				protoList.add(protoApplication);

			}
			AllAvailableResponse response = AllAvailableResponse.newBuilder().addAllApplicationConfig(protoList).build();
			reply.add(response.toByteArray());
		}
		return reply;
	}
	
	/**
	 * OutputCommand
	 * 
	 * @return
	 */
	public Msg processOutputCommand(OutputCommand command, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received OuputCommand message");
		
		int port = manager.getApplicationStreamPort(command.getName());
		
		Zmq.Msg reply = new Zmq.Msg();
		RequestResponse response = RequestResponse.newBuilder().setValue(port).setMessage("OK").build();
		reply.add(response.toByteArray());
		
		return reply;
	}

	public Msg processSetStatusCommand(SetStatusCommand command, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received SetStatusCommand message");
		
		int applicationId = command.getId();
		int state = command.getApplicationState();
		
		RequestResponse response = null;
		
		try {
			boolean done = manager.setApplicationStateFromClient(applicationId, state);
		
			if (done) {
				response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
			} else {
				response = RequestResponse.newBuilder().setValue(-1).setMessage("Cannot set the state").build();
			}
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
		}
		
		Zmq.Msg reply = new Zmq.Msg();
		
		reply.add(response.toByteArray());
		return reply;
	}
	
	public Msg processGetStatusCommand(GetStatusCommand command, Manager manager) {

		LogInfo.getInstance().getLogger().fine("Received GetStatusCommand message");
		
		int applicationId = command.getId();
			
		Messages.StatusEvent protoStatus = null;
		
		try {
			protoStatus = manager.getApplicationState(applicationId);
			
		} catch (IdNotFoundException e) {
			// do nothing
		}
		
		Zmq.Msg reply = new Zmq.Msg();
		
		reply.add(protoStatus.toByteArray());
		return reply;
	}
	

	public Msg processSetResultCommand(SetResultCommand command, Manager manager) {
		
		LogInfo.getInstance().getLogger().fine("Received SetResultCommand message");
		
		int applicationId = command.getId();
		ByteString data = command.getData();
		
		RequestResponse response = null;
		
		try {
			manager.setApplicationResult(applicationId, data);
			response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
			
		} catch (IdNotFoundException e) {
			response = RequestResponse.newBuilder().setValue(-1).setMessage(e.getMessage()).build();
		}
		
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