package fr.ill.ics.cameo.base;
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



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.base.impl.ComImpl;
import fr.ill.ics.cameo.base.impl.InstanceImpl;
import fr.ill.ics.cameo.base.impl.ServerImpl;
import fr.ill.ics.cameo.base.impl.ThisImpl;
import fr.ill.ics.cameo.strings.Endpoint;

public class Application {
	
	public static interface Handler {
		void handle();
	}
	
	public static class This {
		
		static ThisImpl impl;
		private static ServerImpl serverImpl;
		private static Server server;
		private static Server starterServer;
		
		public static class Com {
			
			private ComImpl impl;
			private ThisImpl thisImpl;
			
			Com(ComImpl impl, ThisImpl thisImpl) {
				this.impl = impl;
				this.thisImpl = thisImpl;
			}
			
			public void storeKeyValue(String key, String value) {
				impl.storeKeyValue(key, value);
			}
			
			public String getKeyValue(String key) throws UndefinedKeyException {
				try {
					return impl.getKeyValue(key);
				}
				catch (UndefinedApplicationException e) {
					// Should not happen in This.
					e.printStackTrace();
				}
				return null;
			}
			
			public void removeKey(String key) throws UndefinedKeyException {
				try {
					impl.removeKey(key);
				}
				catch (UndefinedApplicationException e) {
					// Should not happen in This.
					e.printStackTrace();
				}
			}
			
			public int requestPort() {
				try {
					return impl.requestPort();
				}
				catch (UndefinedApplicationException e) {
					// Should not happen in This.
					e.printStackTrace();
				}
				return -1;
			}
			
			public void setPortUnavailable(int port) {
				try {
					impl.setPortUnavailable(port);
				}
				catch (UndefinedApplicationException e) {
					// Should not happen in This.
					e.printStackTrace();
				}
			}
			
			public void releasePort(int port) {
				try {
					impl.releasePort(port);
				}
				catch (UndefinedApplicationException e) {
					// Should not happen in This.
					e.printStackTrace();
				}
			}

			public JSONObject request(JSONObject request) {
				return thisImpl.request(request);
			}
			
			public JSONObject parse(Msg message) {
				try {
					return thisImpl.parse(message);
				}
				catch (ParseException e) {
					throw new UnexpectedException("Cannot parse message");
				}
			}
			
			/**
			 * TODO Temporary access.
			 * @return
			 */
			public ThisImpl getImpl() {
				return thisImpl;
			}
			
			/**
			 * TODO Temporary access.
			 * @return
			 */
			public ServerImpl getServerImpl() {
				return impl.getServer();
			}
		}
		
		private static Com com;
		
		static private void initServer() {
			serverImpl = new ServerImpl(impl.getEndpoint(), 0);
			server = new Server(serverImpl);
			server.registerEventListener(impl.getEventListener());
			
			if (impl.getStarterEndpoint() != null) {
				starterServer = new Server(impl.getStarterEndpoint());
			}
			com = new Com(new ComImpl(serverImpl, impl.getId()), impl);
		}
		
		static public void init(String[] args) {
			impl = new ThisImpl(args);
			
			initServer();
		}
		
		static public void init(String name, String endpoint) {
			impl = new ThisImpl(name, endpoint);
			
			initServer();
		}
				
		static public String getName() {
			if (impl == null) {
				return null;		
			}
			return impl.getName();
		}
	
		static public int getId() {
			if (impl == null) {
				return 0;		
			}
			return impl.getId();
		}
		
		public int getTimeout() {
			return impl.getTimeout();
		}

		public void setTimeout(int timeout) {
			impl.setTimeout(timeout);
		}
		
		static public Endpoint getEndpoint() {
			if (impl == null) {
				return null;		
			}
			return impl.getEndpoint();
		}
		
		static public Server getServer() {
			return server;
		}
		
		static public Server getStarterServer() {
			return starterServer;
		}
		
		static public Com getCom() {
			return com;
		}
		
		static public boolean isAvailable(int timeout) {
			return impl.isAvailable(timeout);
		}
		
		static public boolean isAvailable() {
			return impl.isAvailable(10000);
		}
		
		static public void cancelWaitings() {
			impl.cancelWaitings();
		}
		
		static public void terminate() {
			impl.terminate();
			server.terminate();
			if (starterServer != null) {
				starterServer.terminate();
			}
		}
	
		static public void setResult(byte[] data) {
			impl.setResult(data);		
		}
		
		static public void setResult(String data) {
			impl.setResult(data);
		}
				
		/**
		 * Sets the owner application RUNNING.
		 * @return
		 * @throws StateException, ConnectionTimeout
		 */
		static public boolean setRunning() {
			return impl.setRunning();
		}
	
		/**
		 * Returns true if the application is in STOPPING state. Can be used when the application is already polling.
		 * @return
		 */
		static public boolean isStopping() {
			return impl.isStopping();
		}
		
		/**
		 * Sets the stop handler with stopping time that overrides the one that may be defined in the configuration of the server.
		 * @param handler
		 */
		static public void handleStop(final Handler handler, int stoppingTime) {
			impl.createStopHandler(handler, stoppingTime);
		}
		
		/**
		 * Sets the stop handler with default stopping time.
		 * @param handler
		 */
		static public void handleStop(final Handler handler) {
			handleStop(handler, -1);
		}
		
		/**
		 * 
		 * @return
		 */
		static public Instance connectToStarter(int options) {
			if (starterServer == null) {
				return null;
			}
			
			// Iterate the instances to find the id
			List<Instance> instances = starterServer.connectAll(impl.getStarterName());
			for (Instance i : instances) {
				if (i.getId() == impl.getStarterId()) {
					return i;
				}
			}
			
			return null;
		}
		
		/**
		 * 
		 * @return
		 */
		static public Instance connectToStarter() {
			return connectToStarter(0);
		}
	}
	
	/**
	 * describe states of application
	 *
	 */
	public static class State {
		
		public final static int UNKNOWN = 0;
		public final static int STARTING = 1;
		public final static int RUNNING = 2;
		public final static int STOPPING = 4;
		public final static int KILLING = 8;
		public final static int PROCESSING_ERROR = 16;
		public final static int ERROR = 32;
		public final static int SUCCESS = 64;
		public final static int STOPPED = 128;
		public final static int KILLED = 256;
		
		public static int parse(String value) {
			
			if (value.equals("UNKNOWN")) {
				return State.UNKNOWN;
			} else if (value.equals("STARTING")) {
				return State.STARTING;
			} else if (value.equals("RUNNING")) {
				return State.RUNNING;
			} else if (value.equals("STOPPING")) {
				return State.STOPPING;
			} else if (value.equals("KILLING")) {
				return State.KILLING;
			} else if (value.equals("PROCESSING_ERROR")) {
				return State.PROCESSING_ERROR;
			} else if (value.equals("ERROR")) {
				return State.ERROR;
			} else if (value.equals("SUCCESS")) {
				return State.SUCCESS;
			} else if (value.equals("STOPPED")) {
				return State.STOPPED;
			} else if (value.equals("KILLED")) {
				return State.KILLED;
			}
			
			return State.UNKNOWN;
		}
				
		public static String toString(int applicationStates) {
			
			ArrayList<String> states = new ArrayList<String>();
			
			if ((applicationStates & State.STARTING) != 0) {
				states.add("STARTING");
			}
			
			if ((applicationStates & State.RUNNING) != 0) {
				states.add("RUNNING");
			}
			
			if ((applicationStates & State.STOPPING) != 0) {
				states.add("STOPPING");
			}
			
			if ((applicationStates & State.KILLING) != 0) {
				states.add("KILLING");
			}
			
			if ((applicationStates & State.PROCESSING_ERROR) != 0) {
				states.add("PROCESSING_ERROR");
			}
			
			if ((applicationStates & State.ERROR) != 0) {
				states.add("ERROR");
			}
			
			if ((applicationStates & State.SUCCESS) != 0) {
				states.add("SUCCESS");
			}
			
			if ((applicationStates & State.STOPPED) != 0) {
				states.add("STOPPED");
			}
			
			if ((applicationStates & State.KILLED) != 0) {
				states.add("KILLED");
			}
			
			if (states.size() == 0) {
				return "UNKNOWN";
			}
			
			if (states.size() == 1) {
				return states.get(0);
			}
			
			String result = "";			
			
			for (int i = 0; i < states.size() - 1; i++) {
				result += states.get(i) + ", ";
			}
			result += states.get(states.size() - 1);
			
			return result;
		}
	}
	
	
	/**
	 * Class that implements simple asynchronous programming model.
	 * There is no connection timeout as they are hidden as bad results.
	 * The class is not thread safe and should be used in a single thread.
	 * Question? stop/kill can be called concurrently 
	 *
	 */
	public static class Instance {

		private InstanceImpl impl;
		
		/**
		 * Class defining the Communication Operations Manager (COM).
		 */
		public static class Com {
			
			private ComImpl impl;
			
			Com(ComImpl impl) {
				this.impl = impl;
			}
			
			public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
				return impl.getKeyValue(key);
			}
		}
		
		private Com com;
		
		Instance(InstanceImpl impl) {
			this.impl = impl;
			this.com = new Com(impl.createCom());
		}
		
		public String getName() {
			return impl.getName();
		}
		
		public int getId() {
			return impl.getId();
		}
		
		public Endpoint getEndpoint() {
			return impl.getEndpoint();
		}
		
		public String getNameId() {
			return impl.getNameId();
		}
		
		public Com getCom() {
			return com;
		}
		
		public boolean hasResult() {
			return impl.hasResult();
		}
		
		/**
		 * 
		 * @return true if the instance exists, i.e. the task is executed, otherwise false. 
		 */
		public boolean exists() {
			return impl.exists();
		}
		
		/**
		 * Returns the error message.
		 * @return
		 */
		public String getErrorMessage() {
			return impl.getErrorMessage();
		}
		
		/**
		 * Requests the stop of the application.
		 * The stop is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
		 * Or it can be called in parallel with waitFor.
		 * @return false if it does not succeed, the error message is then set.
		 */
		public boolean stop() {
			return impl.stop();	
		}
		
		/**
		 * Requests the kill of the application.
		 * The stop is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
		 * Or it can be called in parallel with waitFor. 
		 * @return false if it does not succeed, the error message is then set.
		 */
		public boolean kill() {
			return impl.kill();		
		}
			
		public int waitFor(int states) {
			return impl.waitFor(states);
		}
		
		/**
		 * The call is blocking until a terminal state is received i.e. SUCCESS, STOPPED, KILLED, ERROR.
		 */
		public int waitFor() {
			return impl.waitFor(0);
		}
		
		/**
		 * TODO Temporary access.
		 * @param eventName
		 * @return
		 */
		public int waitFor(String eventName) {
			return impl.waitFor(eventName);
		}
		
		public int waitFor(KeyValue keyValue) {
			return impl.waitFor(keyValue);
		}
		
		public void cancelWaitFor() {
			impl.cancelWaitFor();
		}
		
		public int getLastState() {
			// The call is not blocking but pops the entire content of the queue and returns the last received state, i.e. the current state. 
			return impl.waitFor(0, false);
		}
		
		public int getActualState() {
			return impl.getActualState();
		}
		
		public Set<Integer> getPastStates() {
			return impl.getPastStates();
		}
		
		/**
		 * Returns the exit code.
		 * @return null if is not assigned, the exit code otherwise
		 */
		public Integer getExitCode() {
			return impl.getExitCode();
		}
		
		/**
		 * Terminates the local instances by removing the status listener.
		 * Does not kill nor stop the execution application instance.
		 * It terminates the local object.
		 */
		public void terminate() {
			impl.terminate();
		}
					
		/**
		 * Returns the result of the Instance.
		 * @return
		 */
		public byte[] getBinaryResult() {
			return impl.getResult();
		}
		
		/**
		 * Returns the result of the Instance. Provided by convenience for string results.
		 * Returns always null when the Instance was created by a connect call.
		 * @return
		 */
		public String getStringResult() {
			return impl.getStringResult();
		}
				
		public OutputStreamSocket getOutputStreamSocket() {
			return impl.getOutputStreamSocket();
		}
		
		@Override
		public String toString() {
			return impl.toString();
		}
	}
	
	public static class Configuration {

		private String name;
		private String description;
		private boolean singleInstance;
		private boolean restart;
		private int startingTime;
		private int stoppingTime;

		public Configuration(String name, String description, boolean singleInstance, boolean restart, int startingTime, int stoppingTime) {
			super();
			this.description = description;
			this.singleInstance = singleInstance;
			this.restart = restart;
			this.name = name;
			this.startingTime = startingTime;
			this.stoppingTime = stoppingTime;
		}

		public String getDescription() {
			return description;
		}
		
		public boolean hasSingleInstance() {
			return singleInstance;
		}

		public boolean canRestart() {
			return restart;
		}

		public String getName() {
			return name;
		}

		public int getStartingTime() {
			return startingTime;
		}

		public int getStoppingTime() {
			return stoppingTime;
		}
		
		@Override
		public String toString() {
			return "[name=" + name + ", description=" + description + ", single instance=" + singleInstance + ", restart=" + restart + ", starting time=" + startingTime + ", stopping time=" + stoppingTime + "]";
		}

	}
	
	
	public static class Info {

		private int id;
		private int applicationState;
		private int pastApplicationStates;
		private String args;
		private String name;
		private long pid;

		public Info(String name, int id, long pid, int applicationState, int pastApplicationStates, String args) {
			super();
			this.id = id;
			this.pid = pid;
			this.applicationState = applicationState;
			this.pastApplicationStates = pastApplicationStates;
			this.args = args;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public int getApplicationState() {
			return applicationState;
		}
		
		public int getPastApplicationStates() {
			return pastApplicationStates;
		}

		public String getArgs() {
			return args;
		}
		
		public String getName() {
			return name;
		}
		
		public long getPid() {
			return pid;
		}

		@Override
		public String toString() {
			return "[name=" + name + ", id=" + id + ", state=" + applicationState + ", pastStates=" + pastApplicationStates + ", args=" + args + "]";
		}

	}
	
	public static class Port {
		
		private int port;
		private String status;
		private String owner;
		
		public Port(int port, String status, String owner) {
			super();
			this.port = port;
			this.status = status;
			this.owner = owner;
		}

		public int getPort() {
			return port;
		}

		public String getStatus() {
			return status;
		}

		public String getOwner() {
			return owner;
		}
		
	}
	
	public static class Output {
		
		private int id;
		private String message;
		private boolean endOfLine;
			
		public Output(int id, String message, boolean endOfLine) {
			super();
			this.id = id;
			this.message = message;
			this.endOfLine = endOfLine;
		}

		public int getId() {
			return id;
		}

		public String getMessage() {
			return message;
		}
		
		public boolean isEndOfLine() {
			return endOfLine;
		}

		@Override
		public String toString() {
			return "ApplicationStream [id=" + id + ", message=" + message + " eol=" + endOfLine + "]";
		}

	}
		

	

}