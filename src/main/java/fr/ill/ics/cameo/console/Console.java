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

package fr.ill.ics.cameo.console;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Option;
import fr.ill.ics.cameo.OutputPrintThread;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.Server;

public class Console {

	private String endpoint;
	private Server server;
	String[] applicationArgs;
	private String applicationName;
	private int applicationId;
	private static String CAMEO_SERVER = "CAMEO_SERVER";
	
	public Console(String[] args) {
		super();
		
		int startIndex = 0;
		// Searching for endpoint
		if ("-e".equals(args[0])) {
			if (args.length > 1) {
				endpoint = args[1];
				startIndex = 2;
				
			} else {
				System.out.println("Endpoint is missing.");
				System.exit(1);
			}
			
		} else {
			// Check environment
			Map<String, String> environment = System.getenv();
			if (environment.containsKey(CAMEO_SERVER)) {
				endpoint = environment.get(CAMEO_SERVER);
			} else {
				// Default endpoint
				endpoint = "tcp://localhost:7000";
			}
		}

		server = new Server(endpoint);
		
		applicationArgs = new String[args.length - startIndex];
		for (int i = 0; i < args.length - startIndex; i++) {
			applicationArgs[i] = args[i + startIndex];
		}
	}
	
	public void terminate() {
		server.terminate();
	}

	public void execute() {

		String command;
		
		// if last argument is an integer, then this is applicationID
		String lastArg = applicationArgs[applicationArgs.length - 1];
		
		if (applicationArgs.length == 0) {
			command = "list";
			
		} else if (applicationArgs.length == 1) {
			command = applicationArgs[0];
			
		} else {
			try {
				applicationId = Integer.parseInt(lastArg);
				applicationName = applicationArgs[0];
				command = applicationArgs[1];
			} catch (NumberFormatException e) {
				applicationId = -1;
				applicationName = applicationArgs[0];
				command = applicationArgs[1];	
			}
		}	
				
		try {
			// test connection
			if (!server.isAvailable()) {
				System.out.println("The server is not available.");
				return;
			}
			
			if (command.equals("start")) {
				processStart(applicationArgs);
			} else if (command.equals("stop")) {
				processStop(applicationArgs);
			} else if (command.equals("kill")) {
				processKill(applicationArgs);
			} else if (command.equals("show")) {
				if (applicationName == null) {
					processShowAll();
				} else {
					processShow(applicationArgs);
				}
				
			} else if (command.equals("list")) {
				processAllAvailable();
			} else if (command.equals("test")) {
				processTest(applicationArgs);
			} else if (command.equals("connect")) {
				processConnect(applicationArgs, true);
			} else if (command.equals("listen")) {
				processConnect(applicationArgs, false);
			} else if (command.equals("id")) {
				processID(applicationArgs);
			} else {
				System.out.println("Unknown command " + command + ".");
			}
			
		} catch (ConnectionTimeout e) {
			System.out.println("Connection timeout occurred with server " + endpoint + ".");
		}
	}

	private LinkedList<Integer> getIDs(String applicationName) {
		List<Application.Info> applicationInstances = server.getApplicationInfos();
		LinkedList<Integer> ids = new LinkedList<Integer>();
		
		for (Application.Info info : applicationInstances) {
			if (info.getName().equals(applicationName)) {
				ids.add(info.getId());
			}
		}
		return ids;
	}
	
	private void processAllAvailable() {
		List<Application.Configuration> applicationConfigs = server.getApplicationConfigurations();

		if (applicationConfigs == null) {
			System.out.println("The server " + endpoint + " is not available.");
			return;
		}
		
		if (applicationConfigs.isEmpty()) {
			System.out.println("No available application in server " + endpoint + ".");
		}
		
		System.out.println("Available applications in " + endpoint);
		for (Application.Configuration config : applicationConfigs) {
			
			String description = "<no description>";
			if (!config.getDescription().isEmpty()) {
				description = config.getDescription();
			}
			
			System.out.println(config.getName() + ": " + description);
		}		
	}
	
	private void processShowAll() {

		List<Application.Info> applicationInstances = server.getApplicationInfos();

		if (applicationInstances.isEmpty()) {
			System.out.println("No application is running.");
		}
		
		for (Application.Info info : applicationInstances) {
			System.out.println(info.getName() + "." + info.getId() + " is " + Application.State.toString(info.getApplicationState()));
		}
	}

	private void processShow(String[] args) {

		// check arguments
		if (args.length < 2) {
			return;
		}

		String applicationName = args[0];

		LinkedList<Integer> applicationIDs = getIDs(applicationName);
		
		if (applicationIDs.isEmpty()) {
			System.out.println("No application is running.");
		}
		
		List<Application.Info> applicationInstances = server.getApplicationInfos();
		
		for (Application.Info info : applicationInstances) {
			if (applicationIDs.contains(info.getId())) {
				System.out.println(info.getName() + "." + info.getId() + " is " + Application.State.toString(info.getApplicationState()));
			}	
		}
	}
	
	private void processID(String[] args) {

		// check arguments
		if (args.length < 2) {
			System.out.println("\tid ...");
			return;
		}

		String applicationName = args[0];

		LinkedList<Integer> applicationIDs = getIDs(applicationName);
		
		if (applicationIDs.isEmpty()) {
			System.out.println("No application is running.");
		}
		
		for (int i : applicationIDs) {
			System.out.println(i);
		}
		
	}
	
	private void processStop(String[] args) {

		// check arguments
		if (args.length < 2) {
			return;
		}

		List<Application.Instance> applications = server.connectAll(applicationName);
		
		for (Application.Instance application : applications) {
			
			if ((applicationId == -1) || (applicationId == application.getId())) {
			
				System.out.println("Stopping " + application.getNameId() + "...");
				
				application.stop();
				application.waitFor();
				System.out.println("Done");
			}
		}
	}
	
	private void processKill(String[] args) {

		// check arguments
		if (args.length < 2) {
			return;
		}
		
		List<Application.Instance> applications = server.connectAll(applicationName);
				
		for (Application.Instance application : applications) {
			
			if ((applicationId == -1) || (applicationId == application.getId())) {
			
				System.out.println("Killing " + application.getNameId() + "...");
				
				application.kill();
				application.waitFor();
				System.out.println("Done");
			}
		}
	}

	private void processStart(String[] args) {

		// check arguments
		if (args.length < 2) {
			return;
		}

		String applicationName = args[0];

		String[] applicationArgs = new String[args.length - 2];
		for (int i = 0; i < args.length - 2; i++) {
			applicationArgs[i] = args[i + 2];
		}

		Application.Instance result = server.start(applicationName, applicationArgs);

		if (result.exists()) {
			System.out.println("Started " + result.getNameId() + ".");			
		} else {
			System.out.println("Cannot start " + applicationName + ": " + result.getErrorMessage() + ".");
		}
	}

	private void processTest(String[] args) {

		// check arguments
		if (args.length < 2) {
			return;
		}

		final String applicationName = args[0];

		String[] applicationArgs = new String[args.length - 2];
		for (int i = 0; i < args.length - 2; i++) {
			applicationArgs[i] = args[i + 2];
		}
		
		// then start the application
		final Application.Instance result = server.start(applicationName, applicationArgs, Option.OUTPUTSTREAM);
				
		if (result.exists()) {
			System.out.println("Started " + result.getNameId() + ".");
		} else {
			System.out.println("Cannot test " + applicationName + ": " + result.getErrorMessage() + ".");
			return;			
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				
				// create a new Services object in case the shutdown hook happens during the termination
				// of the main object
				Server server = new Server(endpoint);
				server.isAvailable();
				
				List<Application.Instance> applications = server.connectAll(result.getName());
				for (Application.Instance application : applications) {
					if (application.getId() == result.getId()) {
						application.kill();
						application.waitFor();
						System.out.println("Killed " + result.getNameId() + ".");
					}
				}

				// never forget to terminate the server to properly release JeroMQ objects
				// otherwise the termination of the application will block
				server.terminate();
			}
		}));
		
		// start output thread
		OutputStreamSocket streamSocket = result.getOutputStreamSocket();

		OutputPrintThread outputThread = null;
		
		// the socket can be null if the application is already terminated
		
		if (streamSocket == null) {
			System.out.println("The application " + result.getNameId() + " has no output stream.");
		
		} else {
			outputThread = new OutputPrintThread(streamSocket);
			outputThread.start();
		}

		// start input thread
		InputThread inputThread = new InputThread(server, result.getId());
		inputThread.start();
		
		int state = result.waitFor(Application.State.SUCCESS 
									| Application.State.STOPPED 
									| Application.State.KILLED 
									| Application.State.ERROR 
									| Application.State.PROCESSING_ERROR);
		
		boolean processingError = false;
		if (state == Application.State.PROCESSING_ERROR) {
			processingError = true;
			System.out.print("The application terminated with error that is now processed...");
			// waiting for the end of process
			state = result.waitFor();	
		}
		
		if (streamSocket != null) {
			outputThread.waitFor();
		}
		
		inputThread.stopAndWaitFor();
		
		if (state == Application.State.SUCCESS) {
			System.out.println("The application " + result.getNameId() + " terminated successfully.");
			
		} else if (state == Application.State.STOPPED) {
			System.out.println("The application " + result.getNameId() + " has been stopped.");
			
		} else if (state == Application.State.KILLED) {
			System.out.println("The application " + result.getNameId() + " has been killed.");
			
		} else if (state == Application.State.ERROR) {
			if (processingError) {
				// the state PROCESSING_ERROR has been received
				System.out.println("done");
			} else {
				System.out.println("The application " + result.getNameId() + " terminated with error.");
			}
		}
	}
	
	private void processConnect(String[] args, boolean input) {

		// check arguments
		if (args.length < 2) {
			return;
		}
		
		final String applicationName = args[0];
		
		List<Application.Instance> results = server.connectAll(applicationName, Option.OUTPUTSTREAM);
		
		if (results.size() > 1) {
			System.out.println("More than one application " + applicationName + " is running, please select one.");
			return;
		} else if (results.isEmpty()) {
			System.out.println("No application " + applicationName + " is running.");
		}
		
		Application.Instance result = results.get(0);
		
		// start input thread
		InputThread inputThread = null;
		
		if (input) {
			inputThread = new InputThread(server, result.getId());
			inputThread.start();
		}	
		
		OutputStreamSocket streamSocket = result.getOutputStreamSocket();
		
		if (streamSocket == null) {
			System.out.println("The application " + result.getNameId() + " has no output stream.");
			return;
		}

		System.out.println("Connected " + result.getNameId());
		
		OutputPrintThread outputThread = new OutputPrintThread(streamSocket);
		outputThread.start();
				
		outputThread.waitFor();
		
		if (input) {
			inputThread.stopAndWaitFor();	
		}
	}

	private static void help() {
		
		System.out.println("Usage:");
		System.out.println("[-e <endpoint>]         Defines the server endpoint.");
		System.out.println("                        If not specified, the CAMEO_SERVER environment variable is used.");
		System.out.println("                        Default value is tcp://localhost:7000.");
		System.out.println("[commands]");
		System.out.println("  list                  Lists the available applications.");
		System.out.println("  [name] show           Shows all the started applications.");
		System.out.println("  <name> start   <args> Starts the application.");
		System.out.println("  <name> test    <args> Tests the application.");
		System.out.println("  <name> stop    [id]   Stops the application.");
		System.out.println("  <name> kill    [id]   Kills the application.");
		System.out.println("  <name> connect        Connects the application.");
		System.out.println("  <name> listen         Listens to the application.");
		System.out.println("  <name> id             Prints the ids of the application.");
	}
	
	public static void main(String[] args) {

		// test arguments
		if (args.length == 0) {
			help();
			System.exit(1);
		}
		
		Console console = null;
				
		try {
			console = new Console(args);
	
			console.execute();
			
		} finally {
			if (console != null) {
				console.terminate();
			}	
		}
	}
}