package fr.ill.ics.cameo.manager;

public class UnmanagedApplication extends Application {

	private boolean alive = true;
	
	public UnmanagedApplication(String endpoint, int id, String name, long pid) {
		super(endpoint, id);
		
		// Set config.
		this.setName(name);
		
		// Get the process handle from the pid.
		if (pid != 0) {
			try {
				processHandle = ProcessHandle.of(pid).get();
				
				LogInfo.getInstance().getLogger().info("Unmanaged application " + this.getNameId() + " has a process handle");
				
			} catch (Exception e) {
				LogInfo.getInstance().getLogger().info("Unmanaged application " + this.getNameId() + " has no process handle");
			}
		} else {
			LogInfo.getInstance().getLogger().info("Unmanaged application " + this.getNameId() + " has no pid");
		}
	}
	
	synchronized void terminate() {
		alive = false;
	}
	
	@Override
	public boolean isManaged() {
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
		// Should never be called.
	}
	
	@Override
	public synchronized void kill() {
		// Should never be called.	
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
