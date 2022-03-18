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

public class Application {
	
	public static interface Handler {
		void handle();
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
			return "[id=" + id + ", message=" + message + " eol=" + endOfLine + "]";
		}

	}
		

	

}