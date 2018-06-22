package fr.ill.ics.cameo;

public class ProcessHandlerImpl {

	public ProcessHandlerImpl(Process process) {
	}
	
	static public ProcessHandlerImpl ofPid(long pid) {
		return null;
	}
	
	static public long pid() {
		return -1;
	}
	
	public boolean hasProcessHandle() {
		return true;
	}
	
	public boolean isAlive() {
		return false;
	}
	
	public void destroyForcibly() {
	}
	
	public void waitFor() {
	}
}
