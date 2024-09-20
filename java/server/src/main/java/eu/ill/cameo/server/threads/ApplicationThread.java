package eu.ill.cameo.server.threads;

import eu.ill.cameo.server.manager.Application;
import eu.ill.cameo.server.manager.ConfigManager;

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
