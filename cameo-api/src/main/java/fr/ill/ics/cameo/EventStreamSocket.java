package fr.ill.ics.cameo;
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

import fr.ill.ics.cameo.impl.ServicesImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;

public class EventStreamSocket {
		
	private ServicesImpl services;
	private Zmq.Socket socket;
	private Zmq.Socket cancelSocket;
	private boolean canceled = false;
	
	public EventStreamSocket(ServicesImpl services, Zmq.Socket subscriber, Zmq.Socket cancelPublisher) {
		super();
		this.services = services;
		this.socket = subscriber;
		this.cancelSocket = cancelPublisher;
	}
	
	public Event receive() {
		
		String message = this.socket.recvStr();
		Event event = null;
		
		// We can receive messages from the status publisher located in the server
		// as well as messages from the cancel publisher located in the same process.
		if (message.equals(Message.Event.STATUS)) {
			
			byte[] statusMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject status = services.parse(statusMessage);
				
				int id = JSON.getInt(status, Message.StatusEvent.ID);
				String name = JSON.getString(status, Message.StatusEvent.NAME);
				int state = JSON.getInt(status, Message.StatusEvent.APPLICATION_STATE);
				int pastStates = JSON.getInt(status, Message.StatusEvent.PAST_APPLICATION_STATES);
				
				event = new StatusEvent(id, name, state, pastStates);
			
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Message.Event.RESULT)) {
				
			byte[] resultMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject result = services.parse(resultMessage);
				
				int id = JSON.getInt(result, Message.ResultEvent.ID);
				String name = JSON.getString(result, Message.ResultEvent.NAME);
				
				// Get the next message to get the data.
				byte[] data = this.socket.recv();
				
				event = new ResultEvent(id, name, data);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Message.Event.PUBLISHER)) {
			
			byte[] publisherMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject publisher = services.parse(publisherMessage);
				
				int id = JSON.getInt(publisher, Message.PublisherEvent.ID);
				String name = JSON.getString(publisher, Message.PublisherEvent.NAME);
				String publisherName = JSON.getString(publisher, Message.PublisherEvent.PUBLISHER_NAME);
				
				event = new PublisherEvent(id, name, publisherName);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Message.Event.PORT)) {
			
			byte[] portMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject publisher = services.parse(portMessage);
				
				int id = JSON.getInt(publisher, Message.PortEvent.ID);
				String name = JSON.getString(publisher, Message.PortEvent.NAME);
				String portName = JSON.getString(publisher, Message.PortEvent.PORT_NAME);
				
				event = new PortEvent(id, name, portName);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Message.Event.CANCEL)) {
			canceled = true;
			return null;
		}
	
		return event;
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