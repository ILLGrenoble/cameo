package fr.ill.ics.cameo.base;

public class TimeCondition {

	private boolean notified = false;
	
	public synchronized boolean waitFor(int timeMs) {
		
		try {
			super.wait(timeMs);
		} catch (InterruptedException e) {
			return true;
		}
		
		return notified;
	}
	
	public synchronized void notifyCondition() {
		notified = true;
		notify();
	}
}
