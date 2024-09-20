package eu.ill.cameo.processhandle;

public class ProcessHandlerImpl {

	public ProcessHandlerImpl(Process process) {
	}
	
	static public ProcessHandlerImpl ofPid(long pid) {
		return null;
	}
	
	static public long pid() {
		return 0;
	}
	
	public long getPid() {
		return 0;
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
