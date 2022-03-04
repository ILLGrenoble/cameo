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
import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.IdGenerator;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.base.impl.OutputStreamSocketImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.StringId;

public class OutputStreamSocketZmq implements OutputStreamSocketImpl {
	
	private String name;
	private Zmq.Context context;
	private Parser parser;
	private Zmq.Socket subscriberSocket;
	private Zmq.Socket cancelSocket;
	private int applicationId = -1;
	private boolean ended = false;
	private boolean canceled = false;

	public OutputStreamSocketZmq(String name) {
		super();
		this.name = name;
	}
	
	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		
		this.context = ((ContextZmq)context).getContext();
		this.parser = parser;
		
		// Prepare our context and subscriber
		Zmq.Socket subscriber = this.context.createSocket(Zmq.SUB);
		subscriber.connect(endpoint.toString());
		
		// Subscribe to the topic.
		String topicId = StringId.from(Messages.Event.STREAM, name);
		subscriber.subscribe(topicId);
		
		String cancelEndpoint = "inproc://" + IdGenerator.newStringId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		Zmq.Socket cancelPublisher = this.context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		// Polling to wait for connection.
		Zmq.Poller poller = this.context.createPoller(subscriber);
		
		while (true) {
			// The server returns a SYNC_STREAM message that is used to synchronize the subscriber.
			try {
				requestSocket.requestJSON(Messages.createSyncStreamRequest(name));
			}
			catch (ConnectionTimeout e) {
				// Do nothing.
			}

			// Return at the first message received by the subscriber.
			if (poller.poll(100)) {
				break;
			}
		}
		
		this.subscriberSocket = subscriber;
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
			String messageType = this.subscriberSocket.recvStr();
			
			// Cancel can only come from this instance.
			if (messageType.equals(Messages.Event.CANCEL)) {
				canceled = true;
				return null;
			}
			
			// Get the second part of the message.
			byte[] messageValue = this.subscriberSocket.recv();

			try {
				// Get the JSON object.
				JSONObject jsonMessage = parser.parse(Messages.parseString(messageValue));
				
				int type = JSON.getInt(jsonMessage, Messages.TYPE);
				
				// Continue if type of message is SYNC_STREAM. Theses messages are only used for the poller.
				if (type == Messages.SYNC_STREAM) {
					continue;
				}
				
				int id = JSON.getInt(jsonMessage, Messages.ApplicationStream.ID);
				
				// Filter on the application id so that only the messages concerning the instance applicationId are processed.
				// Others are ignored.
				if (applicationId == -1 || applicationId == id) {
					
					// Terminate the stream if type of message is STREAM_END.
					if (type == Messages.STREAM_END) {
						ended = true;
						return null;
					}
					
					// Here the type of message is STREAM.
					String line = JSON.getString(jsonMessage, Messages.ApplicationStream.MESSAGE);
					boolean endOfLine = JSON.getBoolean(jsonMessage, Messages.ApplicationStream.EOL);
					
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
		cancelSocket.sendMore(Messages.Event.CANCEL);
		cancelSocket.send(Messages.Event.CANCEL);
	}
	
	public void terminate() {
		context.destroySocket(subscriberSocket);
	}
}