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
import fr.ill.ics.cameo.messages.Messages;

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
		if (message.equals(Messages.Event.STATUS)) {
			
			byte[] statusMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = services.parse(statusMessage);
				
				int id = JSON.getInt(jsonObject, Messages.StatusEvent.ID);
				String name = JSON.getString(jsonObject, Messages.StatusEvent.NAME);
				int state = JSON.getInt(jsonObject, Messages.StatusEvent.APPLICATION_STATE);
				int pastStates = JSON.getInt(jsonObject, Messages.StatusEvent.PAST_APPLICATION_STATES);
								
				if (jsonObject.containsKey(Messages.StatusEvent.EXIT_CODE)) {
					int exitCode = JSON.getInt(jsonObject, Messages.StatusEvent.EXIT_CODE);
					event = new StatusEvent(id, name, state, pastStates, exitCode);
				}
				else {
					event = new StatusEvent(id, name, state, pastStates);	
				}
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.RESULT)) {
				
			byte[] resultMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = services.parse(resultMessage);
				
				int id = JSON.getInt(jsonObject, Messages.ResultEvent.ID);
				String name = JSON.getString(jsonObject, Messages.ResultEvent.NAME);
				
				// Get the next message to get the data.
				byte[] data = this.socket.recv();
				
				event = new ResultEvent(id, name, data);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.PUBLISHER)) {
			
			byte[] publisherMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = services.parse(publisherMessage);
				
				int id = JSON.getInt(jsonObject, Messages.PublisherEvent.ID);
				String name = JSON.getString(jsonObject, Messages.PublisherEvent.NAME);
				String publisherName = JSON.getString(jsonObject, Messages.PublisherEvent.PUBLISHER_NAME);
				
				event = new PublisherEvent(id, name, publisherName);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.PORT)) {
			
			byte[] portMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = services.parse(portMessage);
				
				int id = JSON.getInt(jsonObject, Messages.PortEvent.ID);
				String name = JSON.getString(jsonObject, Messages.PortEvent.NAME);
				String portName = JSON.getString(jsonObject, Messages.PortEvent.PORT_NAME);
				
				event = new PortEvent(id, name, portName);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.KEYVALUE)) {
			
			byte[] keyValueMessage = this.socket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = services.parse(keyValueMessage);
				
				int id = JSON.getInt(jsonObject, Messages.KeyEvent.ID);
				String name = JSON.getString(jsonObject, Messages.KeyEvent.NAME);
				long status = JSON.getLong(jsonObject, Messages.KeyEvent.STATUS);
				String key = JSON.getString(jsonObject, Messages.KeyEvent.KEY);
				String value = JSON.getString(jsonObject, Messages.KeyEvent.VALUE);
				
				if (status == Messages.STORE_KEY_VALUE) {
					event = new KeyEvent(id, name, KeyEvent.Status.STORED, key, value);
				}
				else {
					event = new KeyEvent(id, name, KeyEvent.Status.REMOVED, key, value);
				}
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.CANCEL)) {
			canceled = true;
			return null;
		}
	
		return event;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void cancel() {
		cancelSocket.sendMore(Messages.Event.CANCEL);
		cancelSocket.send(Messages.Event.CANCEL);
	}

	public void destroy() {
		services.destroySocket(socket);
	}
}