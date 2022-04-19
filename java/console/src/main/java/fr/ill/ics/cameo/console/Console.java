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

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.AppException;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Option;
import fr.ill.ics.cameo.base.OutputPrintThread;
import fr.ill.ics.cameo.base.OutputStreamSocket;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.UnexpectedException;

public class Console {

	private String endpoint;
	private Server server;
	
	private int currentIndex = 0;
	
	String[] applicationArgs;
	private String applicationName = null;
	private String commandName = "help";
	private int applicationId = -1;
	private boolean useStopHandler = true;
	
	// Command options.
	private boolean start = false;
	private boolean mute = false;
	private boolean quiet = false;
	private boolean consoleVersion = false;
	private boolean useProxy = false;
	
	private static String CAMEO_SERVER = "CAMEO_SERVER";
	
	private static String APPLICATION_OPTION = "--app";
	private static String SHORT_APPLICATION_OPTION = "-a";
	
	private static String ENDPOINT_OPTION = "--endpoint";
	private static String SHORT_ENDPOINT_OPTION = "-e";

	private static String PROXY_OPTION = "--proxy-endpoint";
	private static String SHORT_PROXY_OPTION = "-pe";
	
	private static String PORT_OPTION = "--port";
	private static String SHORT_PORT_OPTION = "-p";
	
	private static String MUTE_OPTION = "--mute";
	private static String SHORT_MUTE_OPTION = "-m";
	
	private static String START_OPTION = "--start";
	private static String SHORT_START_OPTION = "-s";
	
	private static String QUIET_OPTION = "--quiet";
	private static String SHORT_QUIET_OPTION = "-q";
	
	private static String NO_STOP_HANDLER_OPTION = "--no-stop-handler";
	private static String SHORT_NO_STOP_HANDLER_OPTION = "-S";
	
	private static String CONSOLE_OPTION = "--console";
	private static String SHORT_CONSOLE_OPTION = "-c";
		
	private static String NAME = "Name";
	private static String DESCRIPTION = "Description";
	private static String ID = "ID";
	private static String PID = "PID";
	private static String STATUS = "Status";
	private static String ARGS = "Args";
	private static String PORT = "Port";
	private static String APPLICATION = "Application";
	
	
	public static String cellString(String name, int length) {
		String result = name;
				
		for (int i = 0; i < length - name.length(); ++i) {
			result += " ";
		}
		return result;
	}

	public static void printLine(int length) {
		String line = "";
		for (int i = 0; i < length; ++i) {
			line += '-';
		}
		System.out.println(line);
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
		else if (PROXY_OPTION.equals(args[currentIndex]) || SHORT_PROXY_OPTION.equals(args[currentIndex])) {
			
			useProxy = true;
			
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
		
		// Add the tcp:// prefix if necessary.
		if (!endpoint.contains("://")) {
			endpoint = "tcp://" + endpoint;
		}
		
		// Find the port and add default 7000 port if not present.
		int tcpSeparator = endpoint.indexOf(':');
		int portSeparator = endpoint.lastIndexOf(':');
		
		if (tcpSeparator == portSeparator) {
			// No port is defined.
			endpoint += ":7000";
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
		}
		
		// Options to command.
		while (args.length > currentIndex) {
			String arg = args[currentIndex];
			if (arg.startsWith("-")) {
				if (MUTE_OPTION.equals(arg) || SHORT_MUTE_OPTION.equals(arg)) {
					mute = true;
				}
				else if (START_OPTION.equals(arg) || SHORT_START_OPTION.equals(arg)) {
					start = true;
				}
				else if (QUIET_OPTION.equals(arg) || SHORT_QUIET_OPTION.equals(arg)) {
					quiet = true;
				}
				else if (CONSOLE_OPTION.equals(arg) || SHORT_CONSOLE_OPTION.equals(arg)) {
					consoleVersion = true;
				}
				else if (NO_STOP_HANDLER_OPTION.equals(arg) || SHORT_NO_STOP_HANDLER_OPTION.equals(arg)) {
					useStopHandler = false;
				}
				currentIndex += 1;
			}
			else {
				break;
			}
		}
		
		if (args.length > currentIndex) {
			applicationName = args[currentIndex];
			currentIndex += 1;
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
			server = Server.create(endpoint, 0, useProxy);
			server.init();
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
				processVersion();
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
				processApps();
			}
			else if (commandName.equals("list")) {
				processList();
			}
			else if (commandName.equals("ports")) {
				processPorts();
			}
			else if (commandName.equals("exec")) {
				processExec();
			}
			else if (commandName.equals("connect")) {
				processConnect();
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
	
	private void processVersion() {
		
		if (consoleVersion) {
			System.out.println(getVersion());
		}
		else {
			int[] version = server.getVersion();
			System.out.println(version[0] + "." + version[1] + "." + version[2]);
		}
	}
	
	private void processServer() {

		int[] version = server.getVersion();
		System.out.println("Cameo server " + endpoint + " version " + version[0] + "." + version[1] + "." + version[2]);
	}
	
	private LinkedList<Integer> getIDs(String applicationName) {
		List<App.Info> applicationInstances = server.getApplicationInfos();
		LinkedList<Integer> ids = new LinkedList<Integer>();
		
		for (App.Info info : applicationInstances) {
			if (info.getName().equals(applicationName)) {
				ids.add(info.getId());
			}
		}
		return ids;
	}
	
	private void processList() {
		
		List<App.Config> applicationConfigs = server.getApplicationConfigs();

		if (applicationConfigs == null) {
			System.out.println("The server " + endpoint + " is not available.");
			return;
		}
		
		if (applicationConfigs.isEmpty()) {
			System.out.println("No available application in server " + endpoint + ".");
		}
		
		// Calculate max lengths of name and description.
		int maxNameLength = NAME.length() + 1;
		int maxDescriptionLength = DESCRIPTION.length() + 1;
				
		for (App.Config config : applicationConfigs) {
			if (config.getName().length() > maxNameLength) {
				maxNameLength = config.getName().length();
			}
			if (config.getDescription().length() > maxNameLength) {
				maxDescriptionLength = config.getDescription().length();
			}
		}
		
		// Print headers.
		System.out.println(cellString(NAME, maxNameLength) + " " + cellString(DESCRIPTION, maxDescriptionLength));
		printLine(maxNameLength + maxDescriptionLength + 1);
		
		for (App.Config config : applicationConfigs) {
			System.out.println(cellString(config.getName(), maxNameLength) + " " + config.getDescription());
		}		
	}
	
	private void processApps() {
		
		List<App.Info> applicationInstances = server.getApplicationInfos();
		
		int maxNameLength = NAME.length();
		int maxArgsLength = ARGS.length();
		
		for (App.Info info : applicationInstances) {
			if (info.getName().length() > maxNameLength) {
				maxNameLength = info.getName().length();
			}
			if (info.getArgs().length() > maxArgsLength) {
				maxArgsLength = info.getArgs().length();
			}
		}
		
		// Print headers.
		System.out.println(cellString(NAME, maxNameLength) + " " + cellString(ID, 10) + cellString(PID, 10) + cellString(STATUS, 20) + cellString(ARGS, maxArgsLength));
		
		printLine(maxNameLength + 1 + 10 + 10 + 20 + maxArgsLength);
		
		for (App.Info info : applicationInstances) {
			
			long pid = info.getPid();
			if (pid == 0) {
				System.out.println(cellString(info.getName(), maxNameLength) + " " + cellString(info.getId() + "", 10) + cellString("-", 10) 
					+ cellString(State.toString(info.getState()), 20)
					+ info.getArgs());
			}
			else {
				System.out.println(cellString(info.getName(), maxNameLength) + " " + cellString(info.getId() + "", 10) + cellString(info.getPid() + "", 10) 
					+ cellString(State.toString(info.getState()), 20)
					+ info.getArgs());
			}
		}
	}

	private void processPorts() {
		
		List<App.Port> ports = server.getPorts();
		
		int maxOwnerLength = APPLICATION.length();
		
		for (App.Port port : ports) {
			if (port.getOwner().length() > maxOwnerLength) {
				maxOwnerLength = port.getOwner().length();
			}
		}
		
		// Print headers.
		System.out.println(cellString(PORT, 10) + cellString(STATUS, 15) + cellString(APPLICATION, maxOwnerLength));
		
		printLine(10 + 10 + maxOwnerLength);
		
		for (App.Port port : ports) {
			System.out.println(cellString(port.getPort() + "", 10) + cellString(port.getStatus(), 15) + cellString(port.getOwner(), maxOwnerLength));
		}
	}
		
	private void processStop() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		try {
			applicationId = Integer.parseInt(applicationName);
			
			List<App.Info> applicationInstances = server.getApplicationInfos();
			for (App.Info info : applicationInstances) {
				
				if (info.getId() == applicationId) {
					applicationName = info.getName();
				}
			}
		}
		catch (NumberFormatException e) {
			// Do nothing.
		}
				
		List<App> applications = server.connectAll(applicationName);
		for (App application : applications) {
			
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
			
			List<App.Info> applicationInstances = server.getApplicationInfos();
			for (App.Info info : applicationInstances) {
				
				if (info.getId() == applicationId) {
					applicationName = info.getName();
				}
			}
				
		} catch (NumberFormatException e) {
			// Do nothing.
		}
		
		List<App> applications = server.connectAll(applicationName);
		for (App application : applications) {
			
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

		try {
			App result = server.start(applicationName, applicationArgs);
			System.out.println("Started " + result.getNameId() + ".");
		}
		catch (AppException e) {
			System.out.println("Cannot start " + applicationName + ": " + e.getMessage() + ".");
		}
	}

	private int waitFor(App app) {
		
		int state = app.waitFor(State.SUCCESS 
				| State.STOPPED 
				| State.KILLED 
				| State.ERROR 
				| State.PROCESSING_ERROR);

		if (state == State.PROCESSING_ERROR) {
			
			System.out.print("The application terminated with error that is now processed...");
			// waiting for the end of process
			state = app.waitFor();
			
			System.out.println("done");
		}
		
		return state;
	}
	
	private int startThreadsAndWaitFor(App app, Thread shutdownHook) {
		
		final String appName = app.getName();
		int appId = app.getId();
		final String appNameId = app.getNameId();
		
		OutputStreamSocket streamSocket = app.getOutputStreamSocket();
		OutputPrintThread outputThread = null;
		
		// Start the output thread.
		if (streamSocket != null) {
			if (!quiet) {
				outputThread = new OutputPrintThread(streamSocket);
				outputThread.start();
			}
		}
		else {
			System.out.println("The application " + appNameId + " has no output stream.");
		}
		
		// Start the input thread.
		InputThread inputThread = null;
		
		if (!mute) {
			inputThread = new InputThread(server, appId, shutdownHook);
			
			if (useStopHandler) {
				inputThread.setStopHandler(new Runnable() {
					public void run() {
						// create a new Services object in case the shutdown hook happens during the termination
						// of the main object
						Server server = Server.create(endpoint);
						server.init();
						server.isAvailable();
						
						List<App> applications = server.connectAll(appName);
						for (App application : applications) {
							if (application.getId() == appId) {
								application.stop();
								System.out.println("Stopping " + appNameId + ".");
							}
						}
	
						// never forget to terminate the server to properly release JeroMQ objects
						// otherwise the termination of the application will block
						server.terminate();
					}
				});
			}
			
			inputThread.start();
		}	

		// Wait for the app.
		int state = waitFor(app);
				
		// Wait for the output thread.
		if (outputThread != null) {
			outputThread.waitFor();
		}
		
		// Wait for the input thread.
		if (!mute) {
			inputThread.stopAndWaitFor();	
		}

		return state;
	}
	
	private void finishApplication(int state, String appNameId, Integer exitCode) {
		
		// Process state.
		if (state == State.SUCCESS) {
			System.out.println("The application " + appNameId + " terminated successfully.");
		}
		else if (state == State.STOPPED) {
			System.out.println("The application " + appNameId + " has been stopped.");
		}
		else if (state == State.KILLED) {
			System.out.println("The application " + appNameId + " has been killed.");
		}
		else if (state == State.ERROR) {
			if (exitCode != null) {
				System.out.println("The application " + appNameId + " terminated with error " + exitCode + ".");
				
				// Exit with the exit code of the app.
				System.exit(exitCode);
			}
			else {
				System.out.println("The application " + appNameId + " terminated with error.");
				
				// Exit with -1 as we do not know.				
				System.exit(-1);
			}
		}
	}
	
	private void processExec() {
		
		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		try {
			// then start the application
			final App app = server.start(applicationName, applicationArgs, Option.OUTPUTSTREAM);
			final String appName = app.getName();
			int appId = app.getId();
			final String appNameId = app.getNameId();
			
			System.out.println("Started " + appNameId + ".");
			
			Thread shutdownHook = new Thread(new Runnable() {
				public void run() {
					
					// create a new Services object in case the shutdown hook happens during the termination
					// of the main object
					Server server = Server.create(endpoint);
					server.init();
					server.isAvailable();
					
					List<App> applications = server.connectAll(appName);
					for (App application : applications) {
						if (application.getId() == appId) {
							application.kill();
							application.waitFor();
							System.out.println("Killed " + appNameId + ".");
						}
					}
	
					// never forget to terminate the server to properly release JeroMQ objects
					// otherwise the termination of the application will block
					server.terminate();
				}
			});
			
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			
			// Start the threads and wait for them and the app.
			int state = startThreadsAndWaitFor(app, shutdownHook);
			
			// Finish the application.
			finishApplication(state, appNameId, app.getExitCode());
		}
		catch (AppException e) {
			System.out.println("Cannot start " + applicationName + ": " + e.getMessage() + ".");
		}
	}
	
	private void processConnect() {

		if (applicationName == null) {
			System.out.println("Application name is missing.");
			System.exit(1);
		}
		
		App app = null;
		
		// Test if an id is provided.
		try {
			applicationId = Integer.parseInt(applicationName);
			
			try {
				// We can connect to the application with the id.
				app = server.connect(applicationId, Option.OUTPUTSTREAM);
			}
			catch (AppException e) {
				System.out.println("Cannot connect : there is no application executing with id " + applicationId + ".");
				return;
			}
		}
		catch (NumberFormatException e) {
			// Do nothing.
		}
		
		// Test if no id is provided. Thus use the name.
		if (applicationId == -1) {
				
			// Connect all the apps.
			List<App> apps = server.connectAll(applicationName, Option.OUTPUTSTREAM);
			
			if (apps.size() > 1) {
				System.out.println("More than one application " + applicationName + " is executing, please select one.");
				return;
			}
			else if (apps.isEmpty()) {
				
				// There is no application.
				if (!start) {
					// Return as the option 'start' is false.
					System.out.println("There is no application " + applicationName + " that is executing.");
					return;
				}
				else {
					try {
						// Start the application as the option 'start' is true.
						app = server.start(applicationName, applicationArgs, Option.OUTPUTSTREAM);
						System.out.println("Started " + app.getNameId() + ".");
					}
					catch (AppException e) {
						System.out.println("Cannot start " + applicationName + " : " + e.getMessage() + ".");
						return;
					}
				}
			}
			else {
				app = apps.get(0);
			}
		}
		
		final String appNameId = app.getNameId();
		
		System.out.println("Connected " + appNameId);
		
		// Start the threads and wait for them and the app.
		int state = startThreadsAndWaitFor(app, null);
				
		// Finish the application.
		finishApplication(state, appNameId, app.getExitCode());
	}

	private void processHelp() {
		
		System.out.println("Usage: cmo <server options> [command] <command options>");
		
		System.out.println("[server options]");
		System.out.println("  -e, --endpoint [endpoint]    Define the server endpoint. Full endpoint is tcp://hostname:port. ");
		System.out.println("                               Short endpoints hostname:port or tcp://hostname or hostname are valid.");
		System.out.println("                               If endpoint is hostname then the port is 7000.");
		System.out.println("                               If not specified, the CAMEO_SERVER environment variable is used.");
		System.out.println("                               If the CAMEO_SERVER environment variable is not defined, the default value is tcp://localhost:7000.");
		System.out.println("  -pe, --proxy-endpoint        Define the server proxy endpoint. Same remarks than for the server endpoint.");
		System.out.println("  -p, --port [port]            Define the server endpoint port.");
		System.out.println("                               If specified, the endpoint is tcp://localhost:port.");
		System.out.println("  -a, --app [name]             Define the application name.");
		System.out.println("");
		
		System.out.println("[commands]");
		System.out.println("  start [name] <args>          Start the application with name.");
		
		System.out.println("  exec <options> [name] <args> Start the application with name and blocks until its termination. Output streams are displayed.");
		System.out.println("    [options]");
		System.out.println("    -m, --mute                 Disable the input stream.");
		System.out.println("    -q, --quiet                Disable the output stream.");
		System.out.println("    -S, --no-stop-handler      Disable the stop handler.");

		System.out.println("  connect <options> [name]     Connect the application with name.");
		System.out.println("    [options]");
		System.out.println("    -s, --start                Start the application if it is not already executing.");
		System.out.println("    -m, --mute                 Disable the input stream.");
		System.out.println("    -q, --quiet                Disable the output stream.");
		System.out.println("    -S, --no-stop-handler      Disable the stop handler.");
		
		System.out.println("  connect <options> [id]       Connect the application with id.");
		System.out.println("    [options]");
		System.out.println("    -m, --mute                 Disable the input stream.");
		System.out.println("    -q, --quiet                Disable the output stream.");
		System.out.println("    -S, --no-stop-handler      Disable the stop handler.");
		
		System.out.println("  stop [name]                  Stop the application with name. Kill the application if the stop timeout is reached.");
		System.out.println("  stop [id]                    Stop the application with id. Kill the application if the stop timeout is reached.");
		
		System.out.println("  kill [name]                  Kill the application with name.");
		System.out.println("  kill [id]                    Kill the application with id.");
		System.out.println("");
		
		System.out.println("[display commands]");
		System.out.println("  help                         Display the help.");
		System.out.println("  endpoint                     Display the server endpoint.");
		System.out.println("  server                       Display the server endpoint and version.");
		System.out.println("  version <options>            Display the server version.");
		System.out.println("    [options]");
		System.out.println("    -c, --console              Display the console version.");
		System.out.println("  list                         Display the available applications.");
		System.out.println("  ports                        Display the application ports.");
		System.out.println("  apps <name>                  Display all the started applications.");
		System.out.println("");
		
		System.out.println("[exit in exec, connect]");
		System.out.println("  ctrl+c                       Kill the application only in exec and quit.");
		System.out.println("  S, shift+s enter             Stop the application if the stop handler is enabled (default) and quit.");
		System.out.println("  Q, shift+q enter             Quit without killing the application.");
		System.out.println("");
		
		System.out.println("[examples]");
		System.out.println("$ cmo exec subpubjava pubjava");
		System.out.println("$ cmo kill subpubjava");
		System.out.println("$ cmo -a subpubjava exec pubjava");
		System.out.println("$ cmo -e tcp://localhost:7000 -a subpubjava exec pubjava");
		System.out.println("$ cmo -e tcp://localhost:7000 connect -s subpubjava");
	}
	
	private static String getVersion() {
		
		try {
			Enumeration<URL> resources = Console.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			
			while (resources.hasMoreElements()) {
			    
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes attributes = manifest.getMainAttributes();
				
				if (attributes.getValue("Specification-Version") != null 
					&& attributes.getValue("Build-Timestamp") != null) {
					
					// The manifest is found, we can return.
					return attributes.getValue("Specification-Version");
				}
			}
		}
		catch (IOException E) {
	      // handle
	    }
		
		return "?";
	}

	public static void main(String[] args) {

		Console console = null;
				
		try {
			console = new Console(args);
			console.execute();
		}
		catch (UnexpectedException e) {
			System.out.println("Incompatible cameo server.");
			System.exit(1);
		}
		catch (Exception e) {
			System.out.println("Cannot connect to server: " + e.getMessage());
			System.exit(1);
		}
		finally {
			if (console != null) {
				console.terminate();
			}	
		}
	}
}