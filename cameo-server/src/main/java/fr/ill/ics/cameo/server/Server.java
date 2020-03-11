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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.manager.ConfigManager;
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;

public class Server {

	private Zmq.Context context;
	private String configFileName;
	private InputStream configStream;

	public Server(String configFileName) {
		this.configFileName = configFileName;
	}

	public Server(InputStream configStream) {
		this.configStream = configStream;
	}

	public void run() {
		
		// Start the manager.
		final Manager manager;
		
		if (configFileName != null) {
			manager = new Manager(configFileName);
		}
		else {
			manager = new Manager(configStream);
		}

		// Create the context.
		context = new Zmq.Context();
		Zmq.Socket server = context.createSocket(Zmq.REP);
		server.bind(ConfigManager.getInstance().getEndpoint());

		LogInfo.getInstance().getLogger().fine("Service is ready at " + ConfigManager.getInstance().getEndpoint());

		// Init the stream sockets.
		manager.initStreamSockets(context);

		// Create a shutdown hook.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

				manager.killAllApplications();
				LogInfo.getInstance().getLogger().fine("Exited");
			}
		}));

		// Create the JSON parser.
		JSONParser parser = new JSONParser();
		
		// Create the request processor.
		RequestProcessor process = new RequestProcessor();
		
		// Wait for the requests.
		while (true) {

			Zmq.Msg message = null;
			Zmq.Msg reply = null;

			try {
				message = Zmq.Msg.recvMsg(server);

				if (message == null) {
					break;
				}

				// Get the first frame.
				byte[] data = message.getFirstData();
				
				// Get the JSON request object.
				JSONObject request = (JSONObject)parser.parse(Message.parseString(data));
				
				// Get the type.
				long type = JSON.getLong(request, Message.TYPE);
				
				if (type == Message.SYNC) {
					reply = process.processSync(manager);
				}
				else if (type == Message.START) {
					reply = process.processStartRequest(request, manager);
				}
				else if (type == Message.SHOW_ALL) {
					reply = process.processShowAllRequest(request, manager);
				}
				else if (type == Message.STOP) {
					reply = process.processStopRequest(request, manager);
				}
				else if (type == Message.KILL) {
					reply = process.processKillRequest(request, manager);
				}
				else if (type == Message.CONNECT) {
					reply = process.processConnectRequest(request, manager);
				}
				else if (type == Message.SHOW) {
					reply = process.processShowStreamRequest(request, manager);
				}
				else if (type == Message.IS_ALIVE) {
					reply = process.processIsAliveRequest(request, manager);
				}
				else if (type == Message.SEND_PARAMETERS) {
					reply = process.processSendParametersRequest(request, manager);
				}
				else if (type == Message.STATUS) {
					reply = process.processStatusRequest();
				}
				else if (type == Message.ALL_AVAILABLE) {
					reply = process.processAllAvailableRequest(request, manager);
				}
				else if (type == Message.OUTPUT) {
					reply = process.processOutputRequest(request, manager);
				}
				else if (type == Message.SET_STATUS) {
					reply = process.processSetStatusRequest(request, manager);
				}
				else if (type == Message.GET_STATUS) {
					reply = process.processGetStatusRequest(request, manager);
				}
				else if (type == Message.SET_RESULT) {
					// The result data is in the second frame.
					byte[] resultData = message.getLastData();
					reply = process.processSetResultRequest(request, resultData, manager);
				}
				else if (type == Message.REQUEST_PORT) {
					reply = process.processRequestPortRequest(request, manager);
				}
				else if (type == Message.CONNECT_PORT) {
					reply = process.processConnectPortRequest(request, manager);
				}
				else if (type == Message.REMOVE_PORT) {
					reply = process.processRemovePortRequest(request, manager);
				}
				else if (type == Message.CREATE_PUBLISHER) {
					reply = process.processCreatePublisherRequest(request, manager);
				}
				else if (type == Message.TERMINATE_PUBLISHER) {
					reply = process.processTerminatePublisherRequest(request, manager);
				}
				else if (type == Message.CONNECT_PUBLISHER) {
					reply = process.processConnectPublisherRequest(request, manager);
				}
				else if (type == Message.STARTED_UNMANAGED) {
					reply = process.processStartedUnmanagedRequest(request, manager);
				}
				else if (type == Message.TERMINATED_UNMANAGED) {
					reply = process.processTerminatedUnmanagedRequest(request, manager);
			
				}
				else {
					System.err.println("Unknown request type " + type);
					message.send(server);
				}

				// Send reply to the client.
				if (reply != null) {
					reply.send(server);
				}
			}
			catch (ParseException e) {
				System.err.println("Cannot parse request");
			}
			finally {
				// reply bad request if no reply was sent
				if (reply == null) {
					reply = new Zmq.Msg();
					reply.add("Bad request");
					reply.send(server);
				}

				// Do not use the garbage collector since Java 9 because it is causing a memory leak.
				// A sleep is used to avoid to have too many requests that "block" the zeromq queue.
				try {
					Thread.sleep(ConfigManager.getInstance().getSleepTime());
				} catch (InterruptedException e) {
				}
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

				if (attributes.getValue("Specification-Version") != null && attributes.getValue("Build-Timestamp") != null) {
					System.out.println("Cameo server version " + attributes.getValue("Specification-Version") + "--" + attributes.getValue("Build-Timestamp"));

					// The manifest is found, we can return.
					return;
				}
			}

		} catch (IOException E) {
			// handle
		}
	}
	
	public static void main(String[] args) {

		// verify args
		if (args.length < 1) {
			showVersion();
			System.out.printf("Usage: <XML config file>\n");
			System.exit(1);
		}

		Server server = new Server(args[0]);
		server.run();
	}
}