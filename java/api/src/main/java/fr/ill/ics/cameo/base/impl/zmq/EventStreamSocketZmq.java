package fr.ill.ics.cameo.base.impl.zmq;
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
import fr.ill.ics.cameo.base.CancelIdGenerator;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.Event;
import fr.ill.ics.cameo.base.KeyEvent;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.ResultEvent;
import fr.ill.ics.cameo.base.StatusEvent;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.base.impl.EventStreamSocketImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class EventStreamSocketZmq implements EventStreamSocketImpl {
	
	private Zmq.Context context;
	private Parser parser;
	private Zmq.Socket subscriberSocket;
	private Zmq.Socket cancelSocket;
	private boolean canceled = false;
	
	public EventStreamSocketZmq() {
		super();
	}
	
	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		
		this.context = ((ContextZmq)context).getContext();
		this.parser = parser;
				
		// Prepare our subscriber.
		Zmq.Socket subscriber = this.context.createSocket(Zmq.SUB);
		
		subscriber.connect(endpoint.toString());
		
		subscriber.subscribe(Messages.Event.STATUS);
		subscriber.subscribe(Messages.Event.RESULT);
		subscriber.subscribe(Messages.Event.KEYVALUE);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		// polling to wait for connection
		Zmq.Poller poller = this.context.createPoller(subscriber);
		
		while (true) {
			// The server returns a STATUS message that is used to synchronize the subscriber
			try {
				requestSocket.requestJSON(Messages.createSyncRequest());
			}
			catch (ConnectionTimeout e) {
				// Do nothing.
			}

			// Return after the first message received by the subscriber.
			if (poller.poll(100)) {
				break;
			}
		}
		
		Zmq.Socket cancelPublisher = this.context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		this.subscriberSocket = subscriber;
		this.cancelSocket = cancelPublisher;
	}
	
	public Event receive() {
		
		String message = this.subscriberSocket.recvStr();
		Event event = null;
		
		// We can receive messages from the status publisher located in the server
		// as well as messages from the cancel publisher located in the same process.
		if (message.equals(Messages.Event.STATUS)) {
			
			byte[] statusMessage = this.subscriberSocket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = parser.parse(Messages.parseString(statusMessage));
				
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
				
			byte[] resultMessage = this.subscriberSocket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = parser.parse(Messages.parseString(resultMessage));
				
				int id = JSON.getInt(jsonObject, Messages.ResultEvent.ID);
				String name = JSON.getString(jsonObject, Messages.ResultEvent.NAME);
				
				// Get the next message to get the data.
				byte[] data = this.subscriberSocket.recv();
				
				event = new ResultEvent(id, name, data);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		}
		else if (message.equals(Messages.Event.KEYVALUE)) {
			
			byte[] keyValueMessage = this.subscriberSocket.recv();
			
			try {
				// Get the JSON object.
				JSONObject jsonObject = parser.parse(Messages.parseString(keyValueMessage));
				
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
		context.destroySocket(subscriberSocket);
	}
}