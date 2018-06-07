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

package fr.ill.ics.cameo.threads;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import fr.ill.ics.cameo.manager.Application;
import fr.ill.ics.cameo.manager.ApplicationState;
import fr.ill.ics.cameo.manager.ConfigManager;
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.manager.ProcessState;

public class VerifyApplicationThread extends Thread {

	private Application application;
	private Manager manager;
	private Logger logger;

	public VerifyApplicationThread(Application application, Manager manager, Logger logger) {
		super();
		this.application = application;
		this.manager = manager;
		this.logger = logger;
	}
	
	private void terminateStreamThread() {

		// try to terminate the stream thread
		if (application.getStreamThread() != null) {

			try {
				application.getStreamThread().join();

			} catch (InterruptedException e) {
				// do nothing
			}
		}

		manager.resetApplicationStreamThread(application);
	}

	private boolean onTermination() {
		
		// if the application is unmanaged
		if (!application.isManaged()) {
			
			// wait for the termination.
			if (application.getProcessHandle() != null) {
				CompletableFuture<ProcessHandle> onProcessExit = application.getProcessHandle().onExit();
				
				try {
					onProcessExit.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				}
				
				// no error because we do not have the exit code.
				return false;
			}
			
			// no error.
			return false;
		}
		
		// the process can be null if it was impossible to launch the executable
		if (application.getProcess() == null) {
			return true;
		}
		
		// first get result of last execution
		try {
			int result = application.getProcess().waitFor();

			// pass result to error callback
			if (result != 0 && !application.hasToBeKilled()) {
				logger.info("Application " + application.getNameId() + " returned error code " + result);
				
				if (application.getErrorExecutable() != null) {
					int currentState = application.getApplicationState();
					manager.setApplicationState(application, ApplicationState.PROCESSING_ERROR);
					application.executeError(result, currentState);
				}
				
				return true;
			}
			
			return false;

		} catch (InterruptedException e) {
			return true;
		}
	}
	
	public void run() {
		
		logger.info("Thread for application " + application.getNameId() + " started");
		manager.setApplicationProcessState(application, ProcessState.RUNNING);
		manager.setApplicationState(application, ApplicationState.STARTING);
		application.start();

		// wait starting time
		logger.info("Waiting end of starting time for " + application.getNameId());

		int i = 0;
		double start = System.currentTimeMillis();
		
		// using do-while to ensure that the loop is executed once
		boolean forceRunning = false;
		do {
			// if application has stopped
			if (!application.isAlive()) {
				logger.warning("Application " + application.getNameId() + " stopped running while starting time");

				// application stopped unexpectedly
				onTermination();

				// test number of retries
				if (i == application.getRetries()) {
					manager.setApplicationProcessState(application, ProcessState.DEAD);
					manager.setApplicationState(application, ApplicationState.ERROR);
					manager.sendEndOfStream(application);
					
					// Do it here to ensure that the end of stream is sent.
					terminateStreamThread();
					
					logger.severe("Application " + application.getNameId() + " is in state ERROR");
					return;
					
				} else {
					terminateStreamThread();
				}	
				
				LogInfo.getInstance().getLogger().fine("Restarting application");
				
				// application is restarting
				manager.setApplicationProcessState(application, ProcessState.RUNNING);
				manager.setApplicationState(application, ApplicationState.STARTING);
				application.reset();
				application.start();

				i++;
				start = System.currentTimeMillis();

			} else {
				manager.startApplicationStreamThread(application);
			}

			try {
				Thread.sleep(ConfigManager.getInstance().getPollingTime());
			} catch (InterruptedException e) {
				// do nothing
			}

			// the application can have change its current state
			int currentState = application.getApplicationState();
			forceRunning = (currentState == ApplicationState.RUNNING);
			
		} while ((System.currentTimeMillis() - start) < (application.getStartingTime() * 1000) 
				&& i <= application.getRetries() 
				&& !application.hasToStop()
				// in case the client application changed its state, considering it is running before the starting time expired
				&& !forceRunning);
		

		// application is running only if starting time >= 0
		// negative values indicate that the application is responsible to send the RUNNING state
		if (application.getStartingTime() >= 0) {
			manager.setApplicationState(application, ApplicationState.RUNNING);

			if (forceRunning && application.getStartingTime() > 0) {
				logger.info("Application " + application.getNameId() + " is RUNNING before starting time expired");
			} else {
				logger.info("Application " + application.getNameId() + " is now RUNNING");
			}
		}

		// loop while application is alive and has not to stop
		while (application.isAlive() && !application.hasToStop()) {

			try {
				Thread.sleep(ConfigManager.getInstance().getPollingTime());
				
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		// if appli has to stop
		if (application.hasToStop()) {
			logger.info("Application " + application.getNameId() + " has to stop");
			manager.setApplicationProcessState(application, ProcessState.ZOMBIE);
			
			// test if application is stopped nicely
			if (application.hasToBeKilled()) {
				manager.setApplicationState(application, ApplicationState.KILLING);
				
			} else {
				manager.setApplicationState(application, ApplicationState.STOPPING);
				
				// execute stop if it exists
				application.executeStop();
					
				// wait until process is dead or timeout is over
				double time = System.currentTimeMillis();
				
				if (application.getStoppingTime() > 0) {
					logger.info("Waiting for the application " + application.getNameId() + " to stop before " + application.getStoppingTime() + "s");
				} else {
					logger.info("Waiting for the application " + application.getNameId() + " to stop");
				}

				// in case stopping time is -1, we wait undefinitely
				while (application.isAlive()
					&& (!application.isTimeout((System.currentTimeMillis() - time) / 1000.0))
					&& (!application.hasToBeKilled())) {

					try {
						Thread.sleep(ConfigManager.getInstance().getPollingTime());
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}

			// if process is still alive (timeout is over) or stop is immediate
			if (application.isAlive()) {
				if (!application.hasToBeKilled()) {
					logger.warning("Application " + application.getNameId() + " must be killed due to stop timeout");
				}	
				application.kill();
				manager.setApplicationState(application, ApplicationState.KILLED);
				
			} else if (onTermination()) {
				manager.setApplicationState(application, ApplicationState.ERROR);
				
			} else {
				manager.setApplicationState(application, ApplicationState.STOPPED);
			}
			
			terminateStreamThread();
		}
		// if application died with state RUNNING
		// in case the application can restart
		else if (application.getApplicationState() == ApplicationState.RUNNING && application.isRestart()) {

			logger.warning("Application " + application.getNameId() + " died with state RUNNING, trying to start it again");
			
			onTermination();
			
			// only terminate the stream thread so that the application is not removed from the list
			terminateStreamThread();
			
			// launch a new verification thread here
			VerifyApplicationThread applicationThread = new VerifyApplicationThread(application, manager, logger);
			applicationThread.start();
			
		} else {
			logger.info("Application " + application.getNameId() + " has terminated");
			
			if (onTermination()) {
				manager.setApplicationState(application, ApplicationState.ERROR);
				
			} else {

				if (application.hasToStop()) {
					manager.setApplicationState(application, ApplicationState.STOPPED);
				} else {
					manager.setApplicationState(application, ApplicationState.SUCCESS);
				}
			}
			
			terminateStreamThread();
		}
		
	}

}