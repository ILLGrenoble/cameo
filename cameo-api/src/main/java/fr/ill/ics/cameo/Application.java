package fr.ill.ics.cameo;
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

import fr.ill.ics.cameo.impl.ThisImpl;
import fr.ill.ics.cameo.impl.InstanceImpl;
import fr.ill.ics.cameo.impl.PublisherImpl;
import fr.ill.ics.cameo.impl.RequestImpl;
import fr.ill.ics.cameo.impl.RequesterImpl;
import fr.ill.ics.cameo.impl.ResponderImpl;
import fr.ill.ics.cameo.impl.SubscriberImpl;

public class Application {
	
	public static interface Handler {
		void handle();
	}
	
	public static class This {
		
		static ThisImpl impl;
		private static Server server;
		private static Server starterServer;
		
		static public void init(String[] args) {
			impl = new ThisImpl(args);
			server = new Server(impl.getEndpoint());
			
			server.registerEventListener(impl.getEventListener());
			
			if (impl.getStarterEndpoint() != null) {
				starterServer = new Server(impl.getStarterEndpoint());
			}	
		}
		
		static public String getName() {
			return impl.getName();
		}
	
		static public int getId() {
			return impl.getId();
		}
		
		public int getTimeout() {
			return impl.getTimeout();
		}

		public void setTimeout(int timeout) {
			impl.setTimeout(timeout);
		}
		
		static public String getEndpoint() {
			if (impl == null) {
				return "";				
			}
			return impl.getEndpoint();
		}
		
		static public Server getServer() {
			return server;
		}
		
		static public Server getStarterServer() {
			return starterServer;
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
		 * Sets the stop handler.
		 * @param handler
		 */
		static public void handleStop(final Handler handler) {
			impl.createStopHandler(handler);
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
		
		static String getReference() {
			if (impl != null) {
				return getName() + "." + getId() + "@" + getEndpoint();
			}
			return "";
		}
		
		static public void storeKeyValue(String key, String value) {
			server.storeKeyValue(impl.getId(), key, value);
		}
		
		static public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
			return server.getKeyValue(impl.getId(), key);
		}
		
		static public void removeKey(String key) throws UndefinedApplicationException, UndefinedKeyException {
			server.removeKey(impl.getId(), key);
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
	 * @author legoc
	 *
	 */
	public static class Instance {

		private InstanceImpl impl;
		
		Instance(InstanceImpl impl) {
			this.impl = impl;
		}
		
		public String getName() {
			return impl.getName();
		}
		
		public int getId() {
			return impl.getId();
		}
		
		public String getUrl() {
			return impl.getUrl();
		}
		
		public String getEndpoint() {
			return impl.getEndpoint();
		}
		
		public String getNameId() {
			return impl.getNameId();
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
		
		public void cancelWaitFor() {
			impl.cancelWaitFor();
		}
		
		public int getLastState() {
			// The call is not blocking but pops the entire content of the queue and returns the last received state, i.e. the current state. 
			return impl.waitFor(0, null, false);
		}
		
		public int getActualState() {
			return impl.getActualState();
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
		
		public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
			return impl.getKeyValue(key);
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
		
	/**
	 * Class Publisher.
	 *
	 */
	public static class Publisher {

		private PublisherImpl impl;
		
		Publisher(PublisherImpl impl) {
			this.impl = impl;
		}
		
		/**
		 * 
		 * @param name
		 * @return
		 * @throws PublisherCreationException, ConnectionTimeout
		 */
		static public Publisher create(String name, int numberOfSubscribers) throws PublisherCreationException {
			return new Publisher(This.impl.publish(name, numberOfSubscribers));
		}
		
		/**
		 * 
		 * @param name
		 * @return
		 * @throws PublisherCreationException, ConnectionTimeout
		 */
		static public Publisher create(String name) throws PublisherCreationException {
			return new Publisher(This.impl.publish(name, 0));
		}
		
		public String getName() {
			return impl.getName();
		}
		
		/**
		 * Returns true if the wait succeeds or false if it was canceled.
		 * @return
		 */
		public boolean waitForSubscribers() {
			return impl.waitForSubscribers();
		}
		
		/**
		 * Cancels the wait for subscribers.
		 */
		public void cancelWaitForSubscribers() {
			impl.cancelWaitForSubscribers();
		}

		public void send(byte[] data) {
			impl.send(data);
		}
		
		public void send(String data) {
			impl.send(data);
		}
				
		public void sendTwoParts(byte[] data1, byte[] data2) {
			impl.sendTwoParts(data1, data2);
		}
		
		public void sendEnd() {
			impl.sendEnd();
		}
		
		public boolean isEnded() {
			return impl.isEnded();
		}
		
		public void terminate() {
			impl.terminate();
		}
			
		@Override
		public String toString() {
			return impl.toString();
		}
	}
	
	
	/**
	 * Class Subscriber. 
	 *
	 */
	public static class Subscriber {
		
		private SubscriberImpl impl;
		
		Subscriber(SubscriberImpl impl) {
			this.impl = impl;
		}
		
		/**
		 * Subscribes to the application publisher.
		 * @param publisherName
		 * @return
		 */
		public static Subscriber create(Instance application, String publisherName) {
			return new Subscriber(application.impl.subscribe(publisherName));
		}
				
		public String getPublisherName() { 
			return impl.getPublisherName();
		}
		
		public String getInstanceName() {
			return impl.getInstanceName();
		}
		
		public int getInstanceId() {
			return impl.getInstanceId();
		}
		
		public String getInstanceEndpoint() {
			return impl.getInstanceEndpoint();
		}
		
		public boolean isEnded() {
			return impl.isEnded();
		}
		
		public boolean isCanceled() {
			return impl.isCanceled();
		}
				
		/**
		 * 
		 * @return the byte[] data. If the return value is null, then the stream is finished. 
		 */
		public byte[] receive() {
			return impl.receive();
		}
		
		/**
		 * 
		 * @return the string data. If the return value is null, then the stream is finished. 
		 */
		public String receiveString() {
			return impl.receiveString();
		}
		
		/**
		 * 
		 * @return the two parts byte[][] data. If the return value is null, then the stream is finished. 
		 */
		public byte[][] receiveTwoParts() {
			return impl.receiveTwoParts();
		}
		
		public void cancel() {
			impl.cancel();
		}
		
		public void terminate() {
			impl.terminate();
		}

		@Override
		public String toString() {
			return impl.toString();
		}
	}
	
	
	/**
	 * Class Request.
	 * 
	 */
	public static class Request {
		
		private RequestImpl impl;
		private Server requesterServer = null;
		
		Request(RequestImpl impl) {
			this.impl = impl;
		}
		
		public byte[] getBinary() {
			return impl.get();
		}
		
		public String get() {
			return impl.getString();
		}
		
		public byte[][] getTwoBinaryParts() {
			
			byte[][] result = new byte[2][];
			result[0] = impl.get();
			result[1] = impl.get2();
			
			return result;
		}
		
		public void reply(byte[] response) {
			impl.reply(response);
		}
		
		public void reply(String response) {
			impl.reply(response);
		}
		
		public Instance connectToRequester() {
			
			// Instantiate the requester server if it is null.
			if (requesterServer == null) {
				requesterServer = new Server(impl.getRequesterServerEndpoint());
			}	
			
			// Connect and find the instance.
			List<Instance> instances = requesterServer.connectAll(impl.getRequesterApplicationName());
			
			for (Instance instance : instances) {
				if (instance.getId() == impl.getRequesterApplicationId()) {
					return instance;
				}
			}
			
			// Not found.
			return null;
		}
		
		/**
		 * Gets the requester server and transfers the ownership. The client code is responsible to terminate the server.
		 * @return
		 */
		public Server getServer() {
			
			// Transfers the ownership of the server.
			Server result = requesterServer;
			requesterServer = null;
			
			return result;
		}
		
		public void terminate() {
			
			if (requesterServer != null) {
				requesterServer.terminate();
			}
		}
		
		@Override
		public String toString() {
			return impl.toString();
		}
	}

	/**
	 * Class Responder.
	 *
	 */
	public static class Responder {

		private ResponderImpl impl;
		
		Responder(ResponderImpl impl) {
			this.impl = impl;
		}
		
		/**
		 * 
		 * @param name
		 * @return
		 * @throws ResponderCreationException, ConnectionTimeout
		 */
		static public Responder create(String name) throws ResponderCreationException {
			return new Responder(This.impl.respond(name));
		}
		
		public String getName() {
			return impl.getName();
		}
		
		public Request receive() {
			RequestImpl requestImpl = impl.receive();
			if (requestImpl == null) {
				return null;
			}
			return new Request(requestImpl);
		}

		public void cancel() {
			impl.cancel();			
		}
		
		public boolean isEnded() {
			return impl.isEnded();
		}
		
		public boolean isCanceled() {
			return impl.isCanceled();
		}
				
		public void terminate() {
			impl.terminate();
		}
			
		@Override
		public String toString() {
			return impl.toString();
		}
	}

	
	/**
	 * Class Responder.
	 *
	 */
	public static class Requester {

		private RequesterImpl impl;
		
		Requester(RequesterImpl impl) {
			this.impl = impl;
		}
		
		/**
		 * 
		 * @param name
		 * @return
		 * @throws RequesterCreationException, ConnectionTimeout
		 */
		static public Requester create(Instance application, String name) throws RequesterCreationException {
			return new Requester(This.impl.request(name, application.impl));
		}
		
		public String getName() {
			return impl.getName();
		}
		
		public void send(byte[] request) {
			impl.send(request);
		}
		
		public void send(String request) {
			impl.send(request);
		}
		
		public void sendTwoParts(byte[] request1, byte[] request2) {
			impl.sendTwoParts(request1, request2);
		}
		
		public byte[] receive() {
			return impl.receive();
		}
		
		public String receiveString() {
			return impl.receiveString();
		}
		
		public void cancel() {
			impl.cancel();			
		}
		
		public boolean isCanceled() {
			return impl.isCanceled();
		}
		
		public void terminate() {
			impl.terminate();
		}
			
		@Override
		public String toString() {
			return impl.toString();
		}
	}

}