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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.manager.ConfigManager;
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.proto.Messages.AllAvailableCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPortCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPublisherCommand;
import fr.ill.ics.cameo.proto.Messages.CreatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.GetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.IsAliveCommand;
import fr.ill.ics.cameo.proto.Messages.KillCommand;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.OutputCommand;
import fr.ill.ics.cameo.proto.Messages.RemovePortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestPortCommand;
import fr.ill.ics.cameo.proto.Messages.SendParametersCommand;
import fr.ill.ics.cameo.proto.Messages.SetResultCommand;
import fr.ill.ics.cameo.proto.Messages.SetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.ShowAllCommand;
import fr.ill.ics.cameo.proto.Messages.ShowStreamCommand;
import fr.ill.ics.cameo.proto.Messages.StartCommand;
import fr.ill.ics.cameo.proto.Messages.StopCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatePublisherCommand;

public class Server {

	private static ZContext context;

	public static void main(String[] args) {
		
		// verify args
		if (args.length < 1) {
			showVersion();
			System.out.printf("Usage: <XML config file>\n");
			System.exit(1);
		}
		
		// start manager
		final Manager manager = new Manager(args[0]);

		context = new ZContext();
		Socket server = context.createSocket(ZMQ.REP);
		server.bind(ConfigManager.getInstance().getEndpoint());

		// wait command (listen)
		LogInfo.getInstance().getLogger().fine("Service is ready at " + ConfigManager.getInstance().getEndpoint());
				
		manager.initStreamSockets(context);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				LogInfo.getInstance().getLogger().fine("Exited");
			}
		}));
		
		while (true) {

			ZMsg message = null;
			ZMsg reply = null;
			
			try {
				message = ZMsg.recvMsg(server);
	
				if (message == null) {
					break;
				}
	
				// check there are not 2 frames
				if (message.size() != 2) {
					System.err.println("Unexpected number of frames, should be 2");
					continue;
				}
				// 2 frames, get first frame (type)
				byte[] typeData = message.getFirst().getData();
				// get last frame
				byte[] messageData = message.getLast().getData();
				ProcessRequest process = new ProcessRequest();
				
				// dispatch message
				MessageType type = MessageType.parseFrom(typeData);
				
				if (type.getType() == Type.INIT) {
					reply = process.processInit(manager);
					
				} else if (type.getType() == Type.START) {
					reply = process.processStartCommand(StartCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.SHOWALL) {
					reply = process.processShowAllCommand(ShowAllCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.STOP) {
					reply = process.processStopCommand(StopCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.KILL) {
					reply = process.processKillCommand(KillCommand.parseFrom(messageData), manager);
					
				} else if (type.getType() == Type.CONNECT) {
					reply = process.processConnectCommand(ConnectCommand.parseFrom(messageData), manager);
					
				} else if (type.getType() == Type.SHOW) {
					reply = process.processShowStreamCommand(ShowStreamCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.ISALIVE) {
					reply = process.processIsAliveCommand(IsAliveCommand.parseFrom(messageData), manager);
				
				} else if (type.getType() == Type.SENDPARAMETERS) {
					reply = process.processSendParametersCommand(SendParametersCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.STATUS) {
					reply = process.processStatusCommand();

				} else if (type.getType() == Type.ALLAVAILABLE) {
					reply = process.processAllAvailableCommand(AllAvailableCommand.parseFrom(messageData), manager);
					
				} else if (type.getType() == Type.OUTPUT) {
					reply = process.processOutputCommand(OutputCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.SETSTATUS) {
					reply = process.processSetStatusCommand(SetStatusCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.GETSTATUS) {
					reply = process.processGetStatusCommand(GetStatusCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.SETRESULT) {
					reply = process.processSetResultCommand(SetResultCommand.parseFrom(messageData), manager);

				// Port
				} else if (type.getType() == Type.REQUESTPORT) {
					reply = process.processRequestPortCommand(RequestPortCommand.parseFrom(messageData), manager);
				
				} else if (type.getType() == Type.CONNECTPORT) {
					reply = process.processConnectPortCommand(ConnectPortCommand.parseFrom(messageData), manager);
					
				} else if (type.getType() == Type.REMOVEPORT) {
					reply = process.processRemovePortCommand(RemovePortCommand.parseFrom(messageData), manager);
					
				// Publisher/Subscriber
				} else if (type.getType() == Type.CREATEPUBLISHER) {
					reply = process.processCreatePublisherCommand(CreatePublisherCommand.parseFrom(messageData), manager);

				} else if (type.getType() == Type.TERMINATEPUBLISHER) {
					reply = process.processTerminatePublisherCommand(TerminatePublisherCommand.parseFrom(messageData), manager);					
					
				} else if (type.getType() == Type.CONNECTPUBLISHER) {
					reply = process.processConnectPublisherCommand(ConnectPublisherCommand.parseFrom(messageData), manager);
				
				} else {
					System.err.println("unknown message type " + type.getType());
					message.send(server);
				}
				
				// send to the client
				if (reply != null) {
					reply.send(server);
				}
				
			} catch (InvalidProtocolBufferException e) {
				System.err.println("problem in parsing of message");
				
			} finally {
				
				if (message != null) {
					message.destroy();
				}	
				
				if (reply != null) {
					reply.destroy();
				}
				
				// requesting gc
				System.gc();
			}
			
		}
		
		if (Thread.currentThread().isInterrupted()) {
			System.out.printf("interrupted\n");
		}
		
		context.close();
	}

	private static void showVersion() {
		try {
			Enumeration<URL> resources = Server.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			
			while (resources.hasMoreElements()) {
			    
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attributes = manifest.getMainAttributes();
				
				System.out.println("Cameo server version " + attributes.getValue("Specification-Version") + "-" + attributes.getValue("Build-Timestamp"));
				
				return;
			}
		
		} catch (IOException E) {
	      // handle
	    }
	}
}