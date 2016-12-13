package fr.ill.ics.cameo.manager;

public class UnmanagedApplication extends Application {

	private boolean alive = true;
	
	public UnmanagedApplication(String endpoint, int id, String name) {
		super(endpoint, id);
		
		// Set config.
		this.setName(name);
	}
	
	synchronized void terminate() {
		alive = false;
	}
	
	@Override
	synchronized public Process getProcess() {
		return null;
	}

	@Override
	synchronized public boolean isAlive() {
		return alive;
	}
	
	@Override
	public synchronized void start() {
		// Should never be called.		
	}
	
	@Override
	public void executeStop(String PID) {
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
