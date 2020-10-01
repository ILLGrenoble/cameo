package fr.ill.ics.cameo.threads;

import fr.ill.ics.cameo.manager.Application;
import fr.ill.ics.cameo.manager.ConfigManager;

/**
 * Base class for the application thread: LifecycleApplicationThread and StreamApplicationThread.  
 *
 */
public class ApplicationThread extends Thread {

	protected Application application;
	
	public ApplicationThread(Application application) {
		this.application = application;
	}
	
	void sleep() {
		try {
			Thread.sleep(ConfigManager.getInstance().getPollingTime());
		}
		catch (InterruptedException e) {
		}

	}
}
