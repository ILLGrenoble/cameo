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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.manager.Application;
import fr.ill.ics.cameo.manager.ConfigManager;
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.proto.Messages.ApplicationStream;

public class StreamApplicationThread extends Thread {

	private Application application;
	private BufferedReader reader;
	private Zmq.Socket publisher;
	private FileOutputStream fileOutputStream;
	
	/**
	 * used to listen stream
	 * 
	 * @param application
	 * @param logger
	 * @param streamName
	 */
	public StreamApplicationThread(Application application, Manager manager) {
		super();
		this.application = application;
		
		// we get the application by the name as the publisher is shared among the different instances
		publisher = manager.getStreamPublisher(application.getName()); 
	}
	
	private void sendLine(String line) {
		
		// prepare our context and publisher
		if (application.isWriteStream()) {
			if (fileOutputStream == null) { // if we have to write log but file is not yet created
				createFile(application.getLogPath());
			}
			try {
				if (fileOutputStream != null) {
					fileOutputStream.write(line.getBytes());
					fileOutputStream.write("\n".getBytes());
				}
				
			} catch (IOException e) {
				System.err.println("error writing stream");
			}
		}
		if (application.hasStream()) {

			ApplicationStream stream = ApplicationStream.newBuilder()
					.setId(application.getId())
					.setMessage(line)
					.build();

			publisher.sendMore("STREAM");
			publisher.send(stream.toByteArray(), 0);
		}
	}
	
	public void run() {
		
		// The process is now accessible and cannot be null.
		InputStreamReader is = new InputStreamReader(application.getProcess().getInputStream());
		reader = new BufferedReader(is);
		if (application.isWriteStream()) {
			createFile(application.getLogPath());
			if (fileOutputStream == null) {
				return;
			}
		}
		
		LogInfo.getInstance().getLogger().info("Started listening stream for application " + application.getNameId());
				
		try {
			try {
				while (application.isAlive() && (application.isWriteStream() || application.hasStream())) {
					
					// polling because the standard Java API does not allow to do it differently
					// problematic:
					// when the process is killed, it is impossible to unblock the reader.readLine call (and any other underlying calls) 
					if (reader.ready()) {
						String line = reader.readLine();
						sendLine(line);
												
					} else {
						try {
							Thread.sleep(ConfigManager.getInstance().getPollingTime());
						} catch (InterruptedException e) {
							// do nothing
						}
					}
				}
				
				// test here
				while (reader.ready()) {
					String line = reader.readLine();
					sendLine(line);
				}
				
			} catch (IOException e) { // if a file is not created
				LogInfo.getInstance().getLogger().severe("Reader error for application " + application.getNameId());
			}
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			LogInfo.getInstance().getLogger().severe("Problem while streaming " + application.getNameId());
			
		} finally {

			sendEndOfStream();
			
			// close the file properly
			try {
				if (fileOutputStream != null) {
					fileOutputStream.flush();
					fileOutputStream.close();
					fileOutputStream = null;
				}
				
			} catch (IOException e) {
				LogInfo.getInstance().getLogger().severe("Problem while closing log file of " + application.getNameId());
			}
			
		}
		
		LogInfo.getInstance().getLogger().info("Finished listening stream for application " + application.getNameId());
	
	}

	public void sendEndOfStream() {
		// send the end of stream
		// the message was originally done in manager when the application was terminated
		// but not the stream thread because they are not synchronized
		if (application.hasStream()) {

			ApplicationStream stream = ApplicationStream.newBuilder()
				.setId(application.getId())
				.setMessage("endstream")
				.build();
		
			publisher.sendMore("ENDSTREAM");
			publisher.send(stream.toByteArray(), 0);
		}
	}
	
	/**
	 * create file to log
	 * 
	 * @param path
	 * @return
	 */
	private void createFile(String path) {
		
		// no creation if path is empty
		if (path.isEmpty()) {
			return;
		}
		
		File file = null;
		// create file
		try {
			file = new java.io.File(path + "/" + application.getNameId() + ".log");
			file.createNewFile();
			fileOutputStream = new FileOutputStream(file);
		} catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Unable to create file " + file.getAbsolutePath() +  " for application " + application.getNameId());
		}
	}
}