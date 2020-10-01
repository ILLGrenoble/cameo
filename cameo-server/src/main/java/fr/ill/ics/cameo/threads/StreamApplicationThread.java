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

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.manager.Application;
import fr.ill.ics.cameo.manager.LogInfo;
import fr.ill.ics.cameo.manager.Manager;
import fr.ill.ics.cameo.messages.Message;

/**
 * Class getting the stream from the process input stream.
 * It is implemented as a thread.
 *
 */
public class StreamApplicationThread extends ApplicationThread {

	private BufferedReader reader;
	private char lastChar = ' ';
	private StringBuffer characters = new StringBuffer(1024);
	private boolean send = false;
	private boolean eol;
	private Zmq.Socket publisher;
	private FileOutputStream fileOutputStream;
	
	/**
	 * Constructor. 
	 * @param application
	 * @param logger
	 * @param streamName
	 */
	public StreamApplicationThread(Application application, Manager manager) {
		super(application);
		
		// we get the application by the name as the publisher is shared among the different instances
		publisher = manager.getStreamPublisher(application.getName()); 
	}
	
	private void sendMessage(String line, boolean endOfLine) {
		
		// Prepare the file if the log is written.
		if (application.isWriteStream()) {
			if (fileOutputStream == null) { 
				// Create the log file if it has not been created.
				createFile(application.getLogPath());
			}
			try {
				if (fileOutputStream != null) {
					fileOutputStream.write(line.getBytes());
					
					// Finish the line.
					if (endOfLine) {
						fileOutputStream.write("\n".getBytes());
					}
				}
			}
			catch (IOException e) {
				LogInfo.getInstance().getLogger().severe("Error while writing stream to file for " + application.getNameId() + " : " + e.getMessage());
			}
		}

		if (application.hasStream()) {
			// Send the stream.
			JSONObject event = new JSONObject();
			event.put(Message.ApplicationStream.ID, application.getId());
			event.put(Message.ApplicationStream.MESSAGE, line);
			event.put(Message.ApplicationStream.EOL, endOfLine);
			
			publisher.sendMore("STREAM");
			publisher.send(Message.serialize(event), 0);
		}
	}
	
	/**
	 * Reads characters from the buffered reader.
	 * We implement our read method since the reader.readLine() is not able to manage the sequences when an input is requested.
	 * The implementation takes more CPU than with the reader.readLine() call.
	 * @return
	 */
	private void readCharacters() {

		// A line is considered to be terminated by any one of a line feed ('\n') [1], a carriage return ('\r') [2], a carriage return followed immediately by a line feed [3].
		
		characters.setLength(0);
		send = false;
		eol = false;
		// Do not reset lastChar because it is used for case [3].
		
		try {
			while (reader.ready()) {
				
				int c = reader.read();
				if (c == -1) {
					return;
				}
				else {
					// Converting back to char (in fact the implementation of reader.read() in the current JRE converts a char to int).
					// TODO Is it always true? Use Character.toChars​(int) ?
					char cc = (char)c;

					if (lastChar == '\r' && cc == '\n') {
						// Case [3], the line has already been finished by '\r'
						lastChar = ' ';
						return;
					}
					
					lastChar = cc;
					
					if (cc == '\n' || cc == '\r') {
						// Cases [1] and [2].
						send = true;
						eol = true;
						return;
					}
														
					characters.append(cc);
					send = true;
				}
			}
		}
		catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Error while reading stream to file for " + application.getNameId() + " : " + e.getMessage());
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
					
					// Polling because the standard Java API does not allow to do it differently. 
					// Indeed when the process is killed, it is impossible to unblock the reader.readLine() call (and any other underlying calls). 
					if (reader.ready()) {
						readCharacters();
						if (send) {
							sendMessage(characters.toString(), eol);
						}
					}							
					else {
						sleep();
					}
				}
				
				while (reader.ready()) {
					readCharacters();
					if (send) {
						sendMessage(characters.toString(), eol);
					}
				}
			}
			catch (IOException e) {
				LogInfo.getInstance().getLogger().severe("Reader error for application " + application.getNameId());
			}
		}
		finally {

			sendEndOfStream();
			
			// Close the file properly.
			try {
				if (fileOutputStream != null) {
					fileOutputStream.flush();
					fileOutputStream.close();
					fileOutputStream = null;
				}
				
			} catch (IOException e) {
				LogInfo.getInstance().getLogger().severe("Problem while closing log file of " + application.getNameId() + " : " + e.getMessage());
			}
			
		}
		
		LogInfo.getInstance().getLogger().info("Finished listening stream for application " + application.getNameId());
	}

	public void sendEndOfStream() {
		// Send the end of stream.
		// The message was originally done in manager when the application was terminated but not the stream thread because they are not synchronized.
		if (application.hasStream()) {
			// Send the stream.
			JSONObject event = new JSONObject();
			event.put(Message.ApplicationStream.ID, application.getId());
			event.put(Message.ApplicationStream.MESSAGE, "endstream");
			event.put(Message.ApplicationStream.EOL, true);
			
			publisher.sendMore("ENDSTREAM");
			publisher.send(Message.serialize(event), 0);
		}
	}
	
	/**
	 * Create the log file.
	 * @param path
	 */
	private void createFile(String path) {
		
		// Do not create if the path is empty.
		if (path.isEmpty()) {
			return;
		}
		
		File file = null;
		
		// Create the file.
		try {
			file = new java.io.File(path + "/" + application.getNameId() + ".log");
			file.createNewFile();
			fileOutputStream = new FileOutputStream(file);
		}
		catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Unable to create file " + file.getAbsolutePath() +  " for application " + application.getNameId() + " : " + e.getMessage());
		}
	}
}