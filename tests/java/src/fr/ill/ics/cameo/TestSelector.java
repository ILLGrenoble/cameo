package fr.ill.ics.cameo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Option;
import fr.ill.ics.cameo.base.OutputPrintThread;
import fr.ill.ics.cameo.base.OutputStreamSocket;
import fr.ill.ics.cameo.base.Server;

public class TestSelector {

	public static Process startServer(String config) {

		System.out.println("*** Starting server ***");
		
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

		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
		
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		
		try {
			System.out.println("*** Server started ***");
			return builder.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void startApplication(Server server, String appName, String[] appArgs) {
		
		Instance instance = server.start(appName, appArgs, Option.OUTPUTSTREAM);

		if (instance.exists()) {

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
		else {
			System.out.println("*** No application ***");
		}
	}
	
	public static ArrayList<String> getJavaTests() {
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("testsimplejava");
		apps.add("testveryfastjava");
		apps.add("teststopjava");
		apps.add("testresultjava");
		apps.add("testerrorjava");
		apps.add("teststoragejava");
		apps.add("testwaitstoragejava");
		apps.add("testpublisherjava");
		apps.add("testsubscriberjava");
		apps.add("testresponderjava");
		apps.add("testrequesterjava");
		apps.add("testcanceljava");
		apps.add("teststreamjava");
		
		return apps;
	}
	
	public static ArrayList<String> getCppTests() {
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("testsimplecpp");
		apps.add("testveryfastcpp");
		apps.add("teststopcpp");
		apps.add("testresultcpp");
		apps.add("testerrorcpp");
		apps.add("teststoragecpp");
		apps.add("testwaitstoragecpp");
		apps.add("testpublishercpp");
		apps.add("testsubscribercpp");
		apps.add("testrespondercpp");
		apps.add("testrequestercpp");
		apps.add("testcancelcpp");
		apps.add("teststreamcpp");
		
		return apps;
	}

	public static ArrayList<String> getPythonTests() {
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("testsimplepy");
		apps.add("testveryfastpy");
		apps.add("teststoppy");
		apps.add("testresultpy");
		apps.add("testerrorpy");
		apps.add("testpublisherpy");
		apps.add("testsubscriberpy");
		apps.add("testresponderpy");
		apps.add("testrequesterpy");
		apps.add("testcancelpy");
		apps.add("teststreampy");
		
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
				
		Server server = new Server("tcp://localhost:10000");
	
		if (server.isAvailable()) {
			System.out.println("*** Server is available ***");
		}
		
		int argsIndex = 0;
		
		try {
			String appName;
			String[] appArgs;
			
			ArrayList<String> apps = new ArrayList<String>();
						
			if (args.length < argsIndex + 1) {
				
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
				appName = args[argsIndex];
				
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
				
				appArgs = new String[args.length - 1 - argsIndex];
				for (int i = 0; i < args.length - 1 - argsIndex; ++i) {
					appArgs[i] = args[argsIndex + i + 1];
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
				serverProcess.destroyForcibly();
				
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
