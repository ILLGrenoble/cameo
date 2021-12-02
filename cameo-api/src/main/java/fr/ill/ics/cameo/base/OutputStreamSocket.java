package fr.ill.ics.cameo.base;
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



import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Socket;
import fr.ill.ics.cameo.base.impl.ServicesImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;

public class OutputStreamSocket {
	
	private ServicesImpl services;
	private Zmq.Socket socket;
	private Zmq.Socket cancelSocket;
	private int applicationId = -1;
	private boolean ended = false;
	private boolean canceled = false;

	public OutputStreamSocket(ServicesImpl services, Zmq.Socket subscriber, Zmq.Socket cancelPublisher) {
		super();
		this.services = services;
		this.socket = subscriber;
		this.cancelSocket = cancelPublisher;		
	}
	
	/**
	 * Sets the application id.
	 * @param id
	 */
	public void setApplicationId(int id) {
		this.applicationId = id;
	}
	
	public Application.Output receive()	{
		
		// Loop on recvStr() because in case of configuration multiple=yes, messages can come from different instances.
		while (true) {
			String messageType = this.socket.recvStr();
			
			// Cancel can only come from this instance.
			if (messageType.equals(Message.Event.CANCEL)) {
				canceled = true;
				return null;
			}
			
			// Get the second part of the message.
			byte[] messageValue = this.socket.recv();

			// Continue if type of message is SYNCSTREAM. Theses messages are only used for the poller.
			if (messageType.equals(Message.Event.SYNCSTREAM)) {
				continue;
			}
			
			try {
				// Get the JSON object.
				JSONObject stream = services.parse(messageValue);
				
				int id = JSON.getInt(stream, Message.ApplicationStream.ID);
				
				// Filter on the application id so that only the messages concerning the instance applicationId are processed.
				// Others are ignored.
				if (applicationId == -1 || applicationId == id) {
					
					// Terminate the stream if type of message is ENDSTREAM.
					if (messageType.equals(Message.Event.ENDSTREAM)) {
						ended = true;
						return null;
					}
					
					// Here the type of message is STREAM.
					String line = JSON.getString(stream, Message.ApplicationStream.MESSAGE);
					boolean endOfLine = JSON.getBoolean(stream, Message.ApplicationStream.EOL);
					
					return new Application.Output(id, line, endOfLine);
				}
				
				// Here, the application id is different from id, then re-iterate.
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response : " + messageValue);
			}
		}
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void cancel() {
		cancelSocket.sendMore(Message.Event.CANCEL);
		cancelSocket.send(Message.Event.CANCEL);
	}
	
	public void destroy() {
		services.destroySocket(socket);
	}
}