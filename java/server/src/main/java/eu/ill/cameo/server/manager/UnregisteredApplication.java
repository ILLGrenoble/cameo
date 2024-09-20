package eu.ill.cameo.server.manager;

import eu.ill.cameo.common.strings.Endpoint;
import eu.ill.cameo.processhandle.ProcessHandlerImpl;

public class UnregisteredApplication extends Application {

	private boolean alive = true;
	
	public UnregisteredApplication(Endpoint endpoint, int id, String name, long pid) {
		super(endpoint, id);
		
		// Set config.
		this.setName(name);
		
		// Get the process handle from the pid.
		if (pid != 0) {
			try {
				processHandle = ProcessHandlerImpl.ofPid(pid);
				
				Log.logger().info("Unregistered application " + this.getNameId() + " has pid " + pid);
			}
			catch (Exception e) {
				Log.logger().info("Unregistered application " + this.getNameId() + " has no process handle");
			}
		}
		else {
			Log.logger().info("Unregistered application " + this.getNameId() + " has no pid");
		}
	}
	
	synchronized void terminate() {
		alive = false;
	}
	
	@Override
	public boolean isRegistered() {
		return false;
	}
	
	@Override
	synchronized public Process getProcess() {
		return null;
	}
	
	@Override
	synchronized public boolean isAlive() {
		
		if (processHandle != null) {
			return processHandle.isAlive();
		}
		
		return alive;
	}
	
	@Override
	public synchronized void start() {
		// Should never be called.		
	}
	
	@Override
	public void executeStop() {
		// Nothing to do because there cannot exist a stop executable.
	}
	
	@Override
	public synchronized void kill() {
		
		// Kill the application using the process handle.
		if (processHandle != null) {
			processHandle.destroyForcibly();
		}
	}
	
	@Override
	public synchronized void reset() {
		// Should never be called.
	}

	@Override
	public String[] getArgs() {
		return null;
	}	
}
