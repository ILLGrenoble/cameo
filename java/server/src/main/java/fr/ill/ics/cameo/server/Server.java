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
import fr.ill.ics.cameo.manager.Log;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.ResponderIdentity;

public class Server {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private String configFileName;
	private InputStream configStream;
	private static String implementationVersion = "?";
	private static String buildTimestamp = "?";
	
	/**
	 * Class storing the version in the major, minor, revision format.
	 *
	 */
	static class Version {
		public int major = 0;
		public int minor = 0;
		public int revision = 0;
		
		@Override
		public String toString() {
			return "Version [major=" + major + ", minor=" + minor + ", revision=" + revision + "]";
		}
	}
	
	private static Version version = new Version();

	public Server(String configFileName) {
		this.configFileName = configFileName;
	}

	public Server(InputStream configStream) {
		this.configStream = configStream;
	}
	
	private void initSocket() {
		
		// Create the socket.
		socket = context.createSocket(Zmq.ROUTER);
		
		// Set the identity.
		socket.setIdentity(ResponderIdentity.CAMEO_SERVER);
		
		// Connect the socket to the proxy local endpoint as the proxy and this server run on the same host.
		Endpoint proxyEndpoint = ConfigManager.getInstance().getProxyLocalEndpoint();

		try {
			socket.connect(proxyEndpoint.toString());
			
			Log.logger().info("Connected to proxy " + proxyEndpoint);
		}
		catch (Exception e) {
			Log.logger().severe("Cannot connect to proxy " + proxyEndpoint + ": " + e.getMessage());
			System.exit(1);
		}
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
		
		// Create and connect the socket.
		initSocket();

		Log.logger().fine("Service is ready at " + ConfigManager.getInstance().getEndpoint());

		// Init the stream sockets.
		manager.initStreamSockets(context);

		// Create a shutdown hook.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

				manager.killAllApplications();
				Log.logger().fine("Exited");
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
				// Receive the multi-part message.
				message = Zmq.Msg.recvMsg(socket);

				if (message == null) {
					break;
				}

				// Get all the parts. 
				byte[][] data = message.getAllData();
		
				// Get the identity of the router.
				byte[] proxyIdentity = data[0];
				Log.logger().info("Received from " + ResponderIdentity.toInt(proxyIdentity));
		
				// Get the identity of the requester.
				byte[] requesterIdentity = data[2];
				Log.logger().info("Received requester " + ResponderIdentity.toInt(requesterIdentity));

				// Prepare the reply.
				reply = new Zmq.Msg();
				
				// Add the necessary parts.
				reply.add(proxyIdentity);
				reply.add(new byte[0]);
				reply.add(requesterIdentity);
				reply.add(new byte[0]);
				
				// Get the JSON request object.
				JSONObject request = (JSONObject)parser.parse(Messages.parseString(data[4]));
				
				// Process the request, first get the type.
				long type = JSON.getLong(request, Messages.TYPE);
				
				// Process on the type.
				if (type == Messages.SYNC) {
					process.processSync(reply, manager);
				}
				else if (type == Messages.SYNC_STREAM) {
					process.processSyncStream(request, reply, manager);
				}
				else if (type == Messages.START) {
					process.processStartRequest(request, reply, manager);
				}
				else if (type == Messages.STOP) {
					process.processStopRequest(request, reply, manager);
				}
				else if (type == Messages.KILL) {
					process.processKillRequest(request, reply, manager);
				}
				else if (type == Messages.CONNECT) {
					process.processConnectRequest(request, reply, manager);
				}
				else if (type == Messages.CONNECT_WITH_ID) {
					process.processConnectWithIdRequest(request, reply, manager);
				}
				else if (type == Messages.OUTPUT_PORT) {
					process.processOutputPortRequest(request, reply, manager);
				}
				else if (type == Messages.OUTPUT_PORT_WITH_ID) {
					process.processOutputPortWithIdRequest(request, reply, manager);
				}
				else if (type == Messages.IS_ALIVE) {
					process.processIsAliveRequest(request, reply, manager);
				}
				else if (type == Messages.WRITE_INPUT) {
					process.processWriteInputRequest(request, reply, manager);
				}
				else if (type == Messages.STATUS) {
					process.processStatusRequest(reply);
				}
				else if (type == Messages.APPS) {
					process.processAppsRequest(request, reply, manager);
				}
				else if (type == Messages.LIST) {
					process.processListRequest(request, reply, manager);
				}
				else if (type == Messages.SET_STATUS) {
					process.processSetStatusRequest(request, reply, manager);
				}
				else if (type == Messages.GET_STATUS) {
					process.processGetStatusRequest(request, reply, manager);
				}
				else if (type == Messages.SET_RESULT) {
					// The result data is in the next frame.
					process.processSetResultRequest(request, data[5], reply, manager);
				}
				else if (type == Messages.ATTACH_UNREGISTERED) {
					process.processAttachUnregisteredRequest(request, reply, manager);
				}
				else if (type == Messages.DETACH_UNREGISTERED) {
					process.processDetachUnregisteredRequest(request, reply, manager);
				}
				else if (type == Messages.IMPL_VERSION) {
					process.processVersion(version, reply);
				}
				else if (type == Messages.STORE_KEY_VALUE) {
					process.processStoreKeyValue(request, reply, manager);
				}
				else if (type == Messages.GET_KEY_VALUE) {
					process.processGetKeyValue(request, reply, manager);
				}
				else if (type == Messages.REMOVE_KEY) {
					process.processRemoveKeyValue(request, reply, manager);
				}
				else if (type == Messages.REQUEST_PORT) {
					process.processRequestPortRequest(request, reply, manager);
				}
				else if (type == Messages.PORT_UNAVAILABLE) {
					process.processPortUnavailableRequest(request, reply, manager);
				}
				else if (type == Messages.RELEASE_PORT) {
					process.processReleasePortRequest(request, reply, manager);
				}
				else if (type == Messages.PORTS) {
					process.processPortsRequest(request, reply, manager);
				}
				else if (type == Messages.SET_STOP_HANDLER) {
					process.processSetStopHandlerRequest(request, reply, manager);
				}
				else {
					System.err.println("Unknown request type " + type);
					message.send(socket);
				}

				// Send reply to the client.
				if (reply != null) {
					reply.send(socket);
				}
			}
			catch (ParseException e) {
				System.err.println("Cannot parse request");
				
				// Reply bad request if no reply was sent.
				if (reply != null) {
					reply.add("Bad request");
					reply.send(socket);
				}
			}
			finally {
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
	
	private static void retrieveVersion() {
		
		try {
			Enumeration<URL> resources = Server.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

			while (resources.hasMoreElements()) {

				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attributes = manifest.getMainAttributes();

				if (attributes.getValue("Implementation-Version") != null && attributes.getValue("Build-Timestamp") != null) {
					implementationVersion = attributes.getValue("Implementation-Version");
					buildTimestamp = attributes.getValue("Build-Timestamp");

					// Analyse the implementation version.
					String[] elements = implementationVersion.split("\\.");
					
					try {
						if (elements.length > 0) {
							version.major = Integer.valueOf(elements[0]);
						}
						if (elements.length > 1) {
							version.minor = Integer.valueOf(elements[1]);
						}
						if (elements.length > 2) {
							String[] revisionElements = elements[2].split("-");
							if (revisionElements.length > 0) {
								version.revision = Integer.valueOf(revisionElements[0]);
							}
							else {
								version.revision = Integer.valueOf(elements[2]);
							}
						}
					}
					catch (NumberFormatException e) {
					}
					
					// The manifest is found, we can return.
					return;
				}
			}

		} catch (IOException E) {
			// handle
		}
	}

	public static void showVersion() {
		System.out.println("Cameo server version " + implementationVersion + "--" + buildTimestamp);
	}
	
	public static void main(String[] args) {

		// Retrieve the version.
		retrieveVersion();
		
		// Verify arguments.
		if (args.length < 1) {
			showVersion();
			System.out.printf("Usage: [--log-console] <config file>\n");
			System.exit(1);
		}
		
		String configFile = "";
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("--log-console")) {
				Log.enableLogConsole();
			}
			if (args[i].endsWith(".xml")) {
				configFile = args[i];
			}
		}

		Server server = new Server(configFile);
		server.run();
	}
}