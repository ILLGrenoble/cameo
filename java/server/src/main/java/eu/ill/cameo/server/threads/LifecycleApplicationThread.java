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

package eu.ill.cameo.server.threads;

import java.util.logging.Logger;

import eu.ill.cameo.server.manager.Application;
import eu.ill.cameo.server.manager.ApplicationState;
import eu.ill.cameo.server.manager.Log;
import eu.ill.cameo.server.manager.Manager;
import eu.ill.cameo.server.manager.ProcessState;

/**
 * Class following the execution of the application. 
 * It is implemented as a thread that is starting before the execution of the application and stopping after it.
 *
 */
public class LifecycleApplicationThread extends ApplicationThread {

	private Manager manager;
	private Logger logger;
	private int exitValue = 0;

	/**
	 * Constructor.
	 * @param application
	 * @param manager
	 * @param logger
	 */
	public LifecycleApplicationThread(Application application, Manager manager, Logger logger) {
		super(application);
		this.manager = manager;
		this.logger = logger;
	}
	
	
	private void terminateStreamThread() {

		// Terminate the stream thread.
		if (application.getStreamThread() != null) {

			try {
				application.getStreamThread().join();
			}
			catch (InterruptedException e) {
			}
		}

		manager.resetApplicationStreamThread(application);
	}

	/**
	 * Method called when the application is terminating.
	 * @return
	 */
	private boolean onTermination() {
		
		// If the application is unregistered.
		if (!application.isRegistered()) {
			
			// Wait for the termination.
			application.waitFor();
			
			// We consider that there is no error (it is not possible to know).
			return true;
		}
		
		// The process can be null if it was impossible to launch the executable.
		if (application.getProcess() == null) {
			return false;
		}
		
		// Get result of the last execution.
		try {
			exitValue = application.getProcess().waitFor();
			
			// An exit value different from 0 indicates the process terminated with an error.
			// If the cameo app was killed i.e. destroyForcibly() was called (RegisteredApplication.kill() or UnregisteredApplication.kill()) the exit value is 0.
			// Thus we do not have to filter these cases.
			// However for a manual kill using a system call, the exit value is not 0 but we can consider as an error.
			if (exitValue != 0) {
				Log.logger().info("Application " + application.getNameId() + " returned error code " + exitValue);
				
				// Execute the error callback.
				if (application.getErrorExecutable() != null) {
					int currentState = application.getApplicationState();
					manager.setApplicationState(application, ApplicationState.PROCESSING_FAILURE, exitValue);
					application.executeError(exitValue, currentState);
				}
				return false;
			}
			return true;
		}
		catch (InterruptedException e) {
			return false;
		}
	}
	
	public void run() {
		
		manager.setApplicationProcessState(application, ProcessState.RUNNING);
		manager.setApplicationState(application, ApplicationState.STARTING);
		application.start();
		manager.startApplicationStreamThread(application);
		
		// Wait starting time if starting time > 0.
		boolean forceRunning = false;
		
		Log.logger().fine("Application " + application.getNameId() + " is waiting end of starting time");

		double start = System.currentTimeMillis();
		
		// Using do-while to ensure that the loop is executed once.
		do {
			// If application has stopped.
			if (!application.isAlive()) {
				
				if (application.getStartingTime() > 0) {
					Log.logger().fine("Application " + application.getNameId() + " stopped executing while starting time");
				}
				break;
			}

			sleep();

			// The application can have change its current state.
			int currentState = application.getApplicationState();
			forceRunning = (currentState == ApplicationState.RUNNING);
			
		} while ((System.currentTimeMillis() - start) < (application.getStartingTime() * 1000) 
				&& !application.hasToStop()
				// In case the client application changed its state, considering it is running before the starting time expired.
				&& !forceRunning);
	
		// Application is running only if starting time >= 0.
		// Negative values indicate that the application is responsible to send the RUNNING state.
		if (application.getStartingTime() >= 0) {
			manager.setApplicationState(application, ApplicationState.RUNNING);
		}

		// Loop while application is alive and has not to stop.
		while (application.isAlive() && !application.hasToStop()) {
			sleep();
		}

		// If the application has to stop.
		if (application.hasToStop()) {
			
			if (application.hasToStopDueToLink()) {
				Log.logger().info("Application " + application.getNameId() + " has to stop because it is linked to its starter");
			}
			else {
				Log.logger().info("Application " + application.getNameId() + " has to stop");
			}
			
			manager.setApplicationProcessState(application, ProcessState.ZOMBIE);
			
			// Test if application is stopped nicely.
			if (application.hasToBeKilled()) {
				manager.setApplicationState(application, ApplicationState.KILLING);
				
				Log.logger().info("Application " + application.getNameId() + " is being killed");
			}
			else {
				manager.setApplicationState(application, ApplicationState.STOPPING);
				
				// Execute stop if it exists.
				application.executeStop();
					
				// Wait until process is dead or timeout is over.
				double time = System.currentTimeMillis();
				
				if (application.getStoppingTime() > 0) {
					Log.logger().info("Application " + application.getNameId() + " is expected to stop before " + application.getStoppingTime() + "s");
				}
				else {
					Log.logger().info("Application " + application.getNameId() + " is expected to stop");
				}

				// In case stopping time is -1, we wait indefinitely.
				while (application.isAlive()
					&& (!application.isTimeout((System.currentTimeMillis() - time) / 1000.0))
					&& (!application.hasToBeKilled())) {

					sleep();
				}
			}

			// If process is still alive (timeout is over) or stop is immediate.
			if (application.isAlive()) {
				if (!application.hasToBeKilled()) {
					Log.logger().fine("Application " + application.getNameId() + " must be killed due to stop timeout");
				}	
				application.kill();
				manager.setApplicationState(application, ApplicationState.KILLED);
			}
			else if (!onTermination()) {
				manager.setApplicationState(application, ApplicationState.FAILURE);
			}
			else {
				manager.setApplicationState(application, ApplicationState.STOPPED);
			}
			
			terminateStreamThread();
		}
		// If application died with state RUNNING.
		// In case the application can restart.
		else if (application.getApplicationState() == ApplicationState.RUNNING && application.isRestart()) {
			Log.logger().fine("Application " + application.getNameId() + " died, restarting it");
			
			onTermination();
			
			// Only terminate the stream thread so that the application is not removed from the list.
			terminateStreamThread();
			
			// Launch a new verification thread here.
			LifecycleApplicationThread lifecycleThread = new LifecycleApplicationThread(application, manager, logger);
			lifecycleThread.start();
		}
		else {
			Log.logger().info("Application " + application.getNameId() + " has terminated");
			
			if (!onTermination()) {
				manager.setApplicationState(application, ApplicationState.FAILURE, exitValue);
			}
			else {
				if (application.hasToStop()) {
					manager.setApplicationState(application, ApplicationState.STOPPED, exitValue);
				}
				else {
					manager.setApplicationState(application, ApplicationState.SUCCESS, exitValue);
				}
			}
			
			terminateStreamThread();
		}
		
	}

}