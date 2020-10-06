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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Option;
import fr.ill.ics.cameo.OutputPrintThread;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.Server;
import fr.ill.ics.cameo.UnexpectedException;

public class Console {

	private String endpoint;
	private Server server;
	
	private int currentIndex = 0;
	
	String[] applicationArgs;
	private String applicationName = null;
	private String commandName = "help";
	private int applicationId = -1;
	
	private static String CAMEO_SERVER = "CAMEO_SERVER";
	
	private static String APPLICATION_OPTION = "--app";
	private static String SHORT_APPLICATION_OPTION = "-a";
	
	private static String ENDPOINT_OPTION = "--endpoint";
	private static String SHORT_ENDPOINT_OPTION = "-e";
	
	private static String PORT_OPTION = "--port";
	private static String SHORT_PORT_OPTION = "-p";
	
	
	public static String column(String name, int length) {
		String result = name;
		
		if (name.length() > 16) {
			return result.substring(0, 16) + "... ";
		}
		
		for (int i = 0; i < length - name.length(); ++i) {
			result += " ";
		}
		return result;
	}
		
	private void defineEndpoint(String[] args) {
		
		// Searching for endpoint.
		if (ENDPOINT_OPTION.equals(args[currentIndex]) || SHORT_ENDPOINT_OPTION.equals(args[currentIndex])) {
			if (args.length > currentIndex + 1) {
				currentIndex += 1;
				endpoint = args[currentIndex];
				currentIndex += 1;
			}
			else {
				System.out.println("Endpoint is missing.");
				System.exit(1);
			}
		}
		else if (PORT_OPTION.equals(args[currentIndex]) || SHORT_PORT_OPTION.equals(args[currentIndex])) {
			if (args.length > currentIndex + 1) {
				currentIndex += 1;
				endpoint = "tcp://localhost:" + args[currentIndex];
				currentIndex += 1;
			}
			else {
				System.out.println("Port is missing.");
				System.exit(1);
			}
		}
		else {
			// Check environment.
			Map<String, String> environment = System.getenv();
			if (environment.containsKey(CAMEO_SERVER)) {
				endpoint = environment.get(CAMEO_SERVER);
			} else {
				// Default endpoint.
				endpoint = "tcp://localhost:7000";
			}
		}
	}
	
	private void defineCommandName(String[] args) {
		
		if (args.length == currentIndex) {
			commandName = "help";
		}
		else if (APPLICATION_OPTION.equals(args[currentIndex]) || SHORT_APPLICATION_OPTION.equals(args[currentIndex])) {
			
			// Searching for the application name.
			// The application is specified with -a option.
			if (args.length > currentIndex + 1) {
				currentIndex += 1;
				applicationName = args[currentIndex];
				currentIndex += 1;
			}
			else {
				System.out.println("Application name is missing.");
				System.exit(1);
			}
			
			if (args.length > currentIndex) {
				commandName = args[currentIndex];
				currentIndex += 1;
			}
			else {
				commandName = "apps";
			}
		}
		else {
			// The application name is not specified with -a option.
			// The command name is then first
			commandName = args[currentIndex];
			currentIndex += 1;
			
			if (args.length > currentIndex) {
				applicationName = args[currentIndex];
				currentIndex += 1;
			}
		}
	}
	
	private void defineApplicationArgs(String[] args) {
		applicationArgs = new String[args.length - currentIndex];
		for (int i = 0; i < args.length - currentIndex; i++) {
			applicationArgs[i] = args[i + currentIndex];
		}
	}
	
	public Console(String[] args) {
		super();
		
		if (args.length == 0) {
			return;
		}
		
		defineEndpoint(args);
		defineCommandName(args);
		defineApplicationArgs(args);
		
		// Initialise the server if it is not a help command.
		if (!commandName.equals("help")) {
			server = new Server(endpoint);
		}
	}
	
	public void terminate() {
		
		if (server != null) {
			server.terminate();
		}
	}

	public void execute() {

		if (commandName.equals("help")) {
			processHelp();
			return;
		}
		
		try {
			// test connection
			if (!server.isAvailable()) {
				System.out.println("The server is not available.");
				return;
			}
			if (commandName.equals("endpoint")) {
				processServerEndpoint();
			}
			else if (commandName.equals("version")) {
				processServerVersion();
			}
			else if (commandName.equals("server")) {
				processServer();
			}
			else if (commandName.equals("start")) {
				processStart();
			}
			else if (commandName.equals("stop")) {
				processStop();
			}
			else if (commandName.equals("kill")) {
				processKill();
			}
			else if (commandName.equals("apps")) {
				if (applicationName == null) {
					processShowAll();
				}
				else {
					processShow();
				}
				
			}
			else if (commandName.equals("list")) {
				processAllAvailable();
			}
			else if (commandName.equals("exec") || commandName.equals("test")) {
				processExec();
			}
			else if (commandName.equals("connect")) {
				processConnect(true);
			}
			else if (commandName.equals("listen")) {
				processConnect(false);
			}
			else {
				System.out.println("Unknown command " + commandName + ".");
			}
		}
		catch (ConnectionTimeout e) {
			System.out.println("Connection timeout occurred with server " + endpoint + ".");
		}
	}

	private void processServerEndpoint() {

		System.out.println(endpoint);
	}
	
	private void processServerVersion() {

		int[] version = server.getVersion();
		System.out.println(version[0] + "." + version[1] + "." + version[2]);
	}
	
	private void processServer() {

		int[] version = server.getVersion();
		System.out.println("Cameo server " + endpoint + " version " + version[0] + "." + version[1] + "." + version[2]);
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

		System.out.println(column("Name", 20) + column("Description", 20));
		
		List<Application.Configuration> applicationConfigs = server.getApplicationConfigurations();

		if (applicationConfigs == null) {
			System.out.println("The server " + endpoint + " is not available.");
			return;
		}
		
		if (applicationConfigs.isEmpty()) {
			System.out.println("No available application in server " + endpoint + ".");
		}
		
		for (Application.Configuration config : applicationConfigs) {
			System.out.println(column(config.getName(), 20) + config.getDescription());
		}		
	}
	
	private void processShowAll() {

		System.out.println(column("Name", 20) + column("ID", 10) + column("PID", 10) + column("Status", 30));
		
		List<Application.Info> applicationInstances = server.getApplicationInfos();
		
		for (Application.Info info : applicationInstances) {
			
			long pid = info.getPid();
			if (pid == 0) {
				System.out.println(column(info.getName(), 20) + column(info.getId() + "", 10) + column("-", 10) + column(Application.State.toString(info.getApplicationState()), 30));
			}
			else {
				System.out.println(column(info.getName(), 20) + column(info.getId() + "", 10) + column(info.getPid() + "", 10) + column(Application.State.toString(info.getApplicationState()), 30));
			}
		}
	}

	private void processShow() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		LinkedList<Integer> applicationIDs = getIDs(applicationName);
		
		List<Application.Info> applicationInstances = server.getApplicationInfos();
		
		for (Application.Info info : applicationInstances) {
			if (applicationIDs.contains(info.getId())) {
				System.out.println(column(info.getName(), 20) + column(info.getId() + "", 10) + column(info.getPid() + "", 10) + column(Application.State.toString(info.getApplicationState()), 30));
			}	
		}
	}
		
	private void processStop() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		try {
			applicationId = Integer.parseInt(applicationName);
			
			List<Application.Info> applicationInstances = server.getApplicationInfos();
			for (Application.Info info : applicationInstances) {
				
				if (info.getId() == applicationId) {
					applicationName = info.getName();
				}
			}
		}
		catch (NumberFormatException e) {
			// Do nothing.
		}
				
		List<Application.Instance> applications = server.connectAll(applicationName);
		for (Application.Instance application : applications) {
			
			if ((applicationId == -1) || (applicationId == application.getId())) {
			
				System.out.println("Stopping " + application.getNameId() + "...");
				
				application.stop();
				application.waitFor();
				System.out.println("Done.");
			}
		}
	}
	
	private void processKill() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		try {
			applicationId = Integer.parseInt(applicationName);
			
			List<Application.Info> applicationInstances = server.getApplicationInfos();
			for (Application.Info info : applicationInstances) {
				
				if (info.getId() == applicationId) {
					applicationName = info.getName();
				}
			}
				
		} catch (NumberFormatException e) {
			// Do nothing.
		}
		
		List<Application.Instance> applications = server.connectAll(applicationName);
		for (Application.Instance application : applications) {
			
			if ((applicationId == -1) || (applicationId == application.getId())) {
			
				System.out.println("Killing " + application.getNameId() + "...");
				
				application.kill();
				application.waitFor();
				System.out.println("Done.");
			}
		}
	}

	private void processStart() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		Application.Instance result = server.start(applicationName, applicationArgs);

		if (result.exists()) {
			System.out.println("Started " + result.getNameId() + ".");			
		}
		else {
			System.out.println("Cannot start " + applicationName + ": " + result.getErrorMessage() + ".");
		}
	}

	private void processExec() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		// then start the application
		final Application.Instance result = server.start(applicationName, applicationArgs, Option.OUTPUTSTREAM);
				
		if (result.exists()) {
			System.out.println("Started " + result.getNameId() + ".");
		}
		else {
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
		}
		else {
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
			
			// Return 0 as it is SUCCESS.
			System.exit(0);
		}
		else if (state == Application.State.STOPPED) {
			System.out.println("The application " + result.getNameId() + " has been stopped.");
		}
		else if (state == Application.State.KILLED) {
			System.out.println("The application " + result.getNameId() + " has been killed.");
		}
		else if (state == Application.State.ERROR) {
			if (processingError) {
				// the state PROCESSING_ERROR has been received
				System.out.println("done");
			}
			else {
				System.out.println("The application " + result.getNameId() + " terminated with error.");
			}
		}
		
		// Return the state in case it is not SUCCESS.
		//System.exit(state);
	}
	
	private void processConnect(boolean input) {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		List<Application.Instance> results = server.connectAll(applicationName, Option.OUTPUTSTREAM);
		
		if (results.size() > 1) {
			System.out.println("More than one application " + applicationName + " is running, please select one.");
			return;
		}
		else if (results.isEmpty()) {
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

	private void processHelp() {
		
		showVersion();
		
		System.out.println("Usage:");
		System.out.println("[-e, --endpoint <endpoint>] Define the server endpoint.");
		System.out.println("                            If not specified, the CAMEO_SERVER environment variable is used.");
		System.out.println("                            Default value is tcp://localhost:7000.");
		System.out.println("[-p, --port <port>]         Define the server endpoint port.");
		System.out.println("                            If specified, the endpoint is tcp://localhost:port.");
		System.out.println("[-a, --app <name>]          Define the application name.");
		System.out.println("");
		System.out.println("[commands]");
		System.out.println("  start [name] <args>       Starts the application with name.");
		System.out.println("  exec [name] <args>        Starts the application with name and blocks until its termination. Output streams are displayed.");
		System.out.println("  test [name] <args>        Same than exec.");
		System.out.println("  stop [name]               Stop the application with name. Kills the application if the stop timeout is reached.");
		System.out.println("  stop [id]                 Stop the application with id. Kills the application if the stop timeout is reached.");
		System.out.println("  kill [name]               Kill the application with name.");
		System.out.println("  kill [id]                 Kill the application with id.");
		System.out.println("  connect [name]            Connect the application with name.");
		System.out.println("  listen [name]             Listen to the application with name.");
		System.out.println("");
		System.out.println("[display commands]");
		System.out.println("  help                      Display the help.");
		System.out.println("  server                    Display the server endpoint and version.");
		System.out.println("  list                      Display the available applications.");
		System.out.println("  apps <name>               Display all the started applications.");
		System.out.println("");
		System.out.println("Examples:");
		System.out.println("exec subpubjava pubjava");
		System.out.println("kill subpubjava");
		System.out.println("-a subpubjava test pubjava");
		System.out.println("-e tcp://localhost:7000 -a subpubjava test pubjava");
	}
	
	private static void showVersion() {
		
		try {
			Enumeration<URL> resources = Console.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			
			while (resources.hasMoreElements()) {
			    
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attributes = manifest.getMainAttributes();
				
				if (attributes.getValue("Specification-Version") != null 
					&& attributes.getValue("Build-Timestamp") != null) {
					System.out.println("Cameo console version " + attributes.getValue("Specification-Version") + "--" + attributes.getValue("Build-Timestamp"));
					
					// The manifest is found, we can return.
					return;
				}
			}
		}
		catch (IOException E) {
	      // handle
	    }
	}

	public static void main(String[] args) {

		Console console = null;
				
		try {
			console = new Console(args);
			console.execute();
		}
		catch (UnexpectedException e) {
			System.out.println("Incompatible cameo server.");
		}
		finally {
			if (console != null) {
				console.terminate();
			}	
		}
	}
}