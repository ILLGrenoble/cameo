package fr.ill.ics.cameo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.Option;
import fr.ill.ics.cameo.base.OutputPrintThread;
import fr.ill.ics.cameo.base.OutputStreamSocket;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.StartException;

public class TestSelector {

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
		commandList.add("fr.ill.ics.cameo.server.Server");
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

			// Start output thread.
			OutputStreamSocket streamSocket = instance.getOutputStreamSocket();

			OutputPrintThread outputThread = null;

			// The socket can be null if the application is already terminated.
			if (streamSocket == null) {
				System.out.println("*** The application " + instance.getNameId() + " has no output stream ***");
			}
			else {
				outputThread = new OutputPrintThread(streamSocket);
				outputThread.start();
			}

			System.out.println("\n*** Starting " + appName + " ***");
			instance.waitFor();
			
			// Terminate the thread and the server.
			if (streamSocket != null) {
				outputThread.waitFor();
			}
			
			System.out.println("*** Finished " + appName + " ***");
		}
		catch (StartException e) {
			System.out.println("*** No application ***");
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
		apps.add("testmultiresponderjava");
		apps.add("testmultirespondersjava");
		apps.add("testpublisherjava");
		apps.add("testsubscriberjava");
		apps.add("testcanceljava");
		apps.add("teststreamjava");
		apps.add("testcheckappjava");
		apps.add("testlinkedjava");
		apps.add("testmaxappsjava");
		
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
		apps.add("testmultirespondercpp");
		apps.add("testmultiresponderscpp");		
		apps.add("testpublishercpp");
		apps.add("testsubscribercpp");
		apps.add("testcancelcpp");
		apps.add("teststreamcpp");
		apps.add("testcheckappcpp");
		apps.add("testlinkedcpp");
		
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
		apps.add("testmultiresponderpy");
		apps.add("testmultiresponderspy");
		apps.add("testcancelpy");
		apps.add("teststreampy");
		apps.add("testterminatepy");
		
		return apps;
	}
	
	public static void main(String[] args) {
		
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
				String lastArg = args[args.length - 1];
				
				boolean useProxy = Boolean.parseBoolean(lastArg);
				
				System.out.println("*** Trying to create server ***");
				
				if (useProxy) {
					server = Server.create("tcp://localhost:10000", true);
					
				}
				else {
					server = Server.create("tcp://localhost:11000", false);	
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
		}
		
		try {
			String appName;
			String[] appArgs;
			appArgs = new String[args.length - 1];
			for (int i = 0; i < args.length - 1; ++i) {
				appArgs[i] = args[i + 1];
			}
			
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
	}
}
