/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.OutputPrintThread;
import eu.ill.cameo.api.base.OutputStreamSocket;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.StartException;

public class TestSelector {
	
	private static String N = "1";
	private static String proxy = "false";
	private static boolean useProxy = false;
	private static boolean verbose = false;
	
	private static LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
	
	private static void printLine(String line) {
		if (verbose) {
			System.out.println(line);
		}
	}
	
	private static void parseArgs(String[] args) {
		
		if (args.length > 1) {
			N = args[1];
		}
		if (args.length > 2) {
			proxy = args[2];
			useProxy = (args[2].equals("true"));
		}
		if (args.length > 3) {
			verbose = (args[3].equals("true"));
		}
		
		//System.out.println("Args " + N + " " + proxy + " " + useProxy + " " + verbose);
	}
	
	public static Process startServer(String config) {

		System.out.println("*** Starting Cameo server ***");
		
		String jarFileName = "";
		
		CodeSource codeSource = TestSelector.class.getProtectionDomain().getCodeSource();
		try {
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			jarFileName = jarFile.toString();
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add("java");
		
		String javaLibraryPath = System.getProperty("java.library.path");
		if (javaLibraryPath != null) {
			commandList.add("-Djava.library.path=" + javaLibraryPath);
		}
		commandList.add("-classpath");
		commandList.add(jarFileName);
		commandList.add("eu.ill.cameo.server.Server");
		commandList.add(config);

		// Proxy path is let by default.
		
		// Prepare the command.
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
		
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		
		try {
			System.out.println("*** Cameo Server started ***");
			return builder.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void startApplication(Server server, String appName, String[] appArgs) {

		try {
			App instance = server.start(appName, appArgs, Option.OUTPUTSTREAM);

			OutputStreamSocket streamSocket = null;
			OutputPrintThread outputThread = null;
			
			if (verbose) {
				// Start output thread.
				streamSocket = instance.getOutputStreamSocket();
	
				// The socket can be null if the application is already terminated.
				if (streamSocket == null) {
					System.out.println("*** The application " + instance.getNameId() + " has no output stream ***");
				}
				else {
					outputThread = new OutputPrintThread(streamSocket);
					outputThread.start();
				}
			}
			
			// Start the application.
			System.out.println("*** Starting " + appName + " ***");
			instance.waitFor();
			
			// Terminate the thread and the server.
			if (streamSocket != null) {
				outputThread.waitFor();
			}
			
			if (verbose) {
				System.out.println("*** Finished " + appName + " ***");
				System.out.println("");
			}
			
			// Set the result.
			results.put(appName, instance.getExitCode());
		}
		catch (StartException e) {
			System.out.println("*** No application ***");
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getJavaTests() {
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("testappexceptionsjava");
		apps.add("testsimplejava");
		apps.add("testveryfastjava");
		apps.add("teststopjava");
		apps.add("testresultjava");
		apps.add("testerrorjava");
		apps.add("teststoragejava");
		apps.add("testwaitstoragejava");
		apps.add("testbasicresponderjava");
		apps.add("testbasicrequesterjava");
		//apps.add("testmultiresponderjava");
		//apps.add("testmultirespondersjava");
		apps.add("testpublisherjava");
		apps.add("testpublishersyncjava");
		apps.add("testsubscriberjava");
		apps.add("testcanceljava");
		apps.add("teststreamjava");
		apps.add("testcheckappjava");
		apps.add("testlinkedjava");
		apps.add("testmaxappsjava");
		apps.add("testcomstimeoutjava");
		apps.add("testrespondererrorjava");
		apps.add("testpublishererrorjava");
		apps.add("testsubscribertimeoutjava");
		apps.add("testheartbeatjava");
		
		return apps;
	}
	
	public static ArrayList<String> getCppTests() {
		ArrayList<String> apps = new ArrayList<String>();
	
		apps.add("testappexceptionscpp");
		apps.add("testsimplecpp");
		apps.add("testveryfastcpp");
		apps.add("teststopcpp");
		apps.add("testresultcpp");
		apps.add("testerrorcpp");
		apps.add("teststoragecpp");
		apps.add("testwaitstoragecpp");
		apps.add("testbasicrespondercpp");
		apps.add("testbasicrequestercpp");
		//apps.add("testmultirespondercpp");
		//apps.add("testmultiresponderscpp");		
		apps.add("testpublishercpp");
		apps.add("testpublishersynccpp");
		apps.add("testsubscribercpp");
		apps.add("testcancelcpp");
		apps.add("teststreamcpp");
		apps.add("testcheckappcpp");
		apps.add("testlinkedcpp");
		apps.add("testcomstimeoutcpp");
		apps.add("testrespondererrorcpp");
		apps.add("testpublishererrorcpp");
		apps.add("testsubscribertimeoutcpp");
		apps.add("testheartbeatcpp");
		
		return apps;
	}

	public static ArrayList<String> getPythonTests() {
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("testappexceptionspy");		
		apps.add("testsimplepy");
		apps.add("testveryfastpy");
		apps.add("teststoppy");
		apps.add("testresultpy");
		apps.add("testerrorpy");
		apps.add("testpublisherpy");
		apps.add("testsubscriberpy");
		apps.add("testbasicresponderpy");
		apps.add("testbasicrequesterpy");
		//apps.add("testmultiresponderpy");
		//apps.add("testmultiresponderspy");
		apps.add("testcancelpy");
		apps.add("teststreampy");
		apps.add("testterminatepy");
		apps.add("testrespondererrorpy");
		apps.add("testpublishererrorpy");
		apps.add("testsubscribertimeoutpy");
		apps.add("testheartbeatpy");
		
		return apps;
	}
	
	private static String padRight(String s, int n) {
	     return String.format("%-" + n + "s", s);  
	}
	
	private static int getMaxAppNameLength() {
		
		int length = 1;
		
		for (String n : results.keySet()) {
			if (n.length() > length) {
				length = n.length();
			}
		}
		
		return length;
	}
	
	private static void printResults() {
		
		int maxLength = getMaxAppNameLength();
		int columnNameLength = maxLength + 10;
		String line = "-".repeat(columnNameLength + 9);
		System.out.println(line);
		System.out.println("Results");
		System.out.println(line);
				
		for (Entry<String, Integer> result : results.entrySet()) {
			
			String stringResult = "OK";
			if (result.getValue() != 0) {
				stringResult = "ERROR " + result.getValue();
			}
			
			System.out.println(padRight(result.getKey(), columnNameLength) + stringResult);
		}
	}
	
	public static void main(String[] args) {
		
		// Parse the args.
		parseArgs(args);
		
		// Define the server process.
		Process serverProcess = null;
		
		String testsPath = "tests/java/tests.xml";
		File testsFile = new File(testsPath);
		
		if (!testsFile.exists()) {
			System.out.println("*** Config tests file is not accessible, go to the Cameo root directory ***");
			return;
		}
		
		// Start the server.
		serverProcess = startServer(testsPath);

		Server server = null;

		// It is necessary to loop because the Cameo server may not be connected to the proxy (connect is asynchronous).
		while (true) {
			try {
				System.out.println("*** Trying to create server ***");
				
				if (useProxy) {
					server = Server.create("tcp://localhost:12000", Option.USE_PROXY);
					
				}
				else {
					server = Server.create("tcp://localhost:11000", 0);	
				}
				
				server.setTimeout(100);
				server.init();
				
				System.out.println("*** Server created ***");
				break;
			}
			catch (Exception e) {
			}
		}
		
		if (server.isAvailable()) {
			System.out.println("*** Server is available ***");
			System.out.println("");
		}
		
		try {
			String appName;
			String[] appArgs = new String[2];
			appArgs[0] = N;
			appArgs[1] = proxy;
			
			ArrayList<String> apps = new ArrayList<String>();
						
			if (args.length < 1) {
				
				System.out.println("*** Available applications ***");
			
				apps.addAll(getJavaTests());
				apps.addAll(getCppTests());
				apps.addAll(getPythonTests());
				
				for (String app : apps) {
					System.out.println("\t" + app);
				}	
								
				return;
			}
			else {
				appName = args[0];
				
				if (appName.equals("all")) {
					apps.addAll(getJavaTests());
					apps.addAll(getCppTests());
					apps.addAll(getPythonTests());
				}
				else if (appName.equals("java")) {
					apps.addAll(getJavaTests());
				}
				else if (appName.equals("cpp")) {
					apps.addAll(getCppTests());
				}
				else if (appName.equals("python")) {
					apps.addAll(getPythonTests());
				}
				else {
					apps.add(appName);
				}
			}

			// Test all applications or only one.
			for (String app : apps) {
				startApplication(server, app, appArgs);	
			}
		}
		finally {
			
			// Terminate the server.
			server.terminate();

			// If the server process exists.
			if (serverProcess != null) {
				// Destroy the server process.
				serverProcess.destroy();
				
				// Waiting for the server that should not return without
				// interruption.
				try {
					serverProcess.waitFor();
				}
				catch (InterruptedException e) {
					// Do nothing.
				}
			}
		}
		
		System.out.println("");
		printResults();
	}
}