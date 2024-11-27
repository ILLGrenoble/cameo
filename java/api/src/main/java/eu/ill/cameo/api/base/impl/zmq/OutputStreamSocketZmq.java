/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base.impl.zmq;




import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.api.base.IdGenerator;
import eu.ill.cameo.api.base.OutputStreamSocket;
import eu.ill.cameo.api.base.RequestSocket;
import eu.ill.cameo.api.base.UnexpectedException;
import eu.ill.cameo.api.base.impl.OutputStreamSocketImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.Endpoint;
import eu.ill.cameo.common.strings.StringId;

public class OutputStreamSocketZmq implements OutputStreamSocketImpl {
	
	private String name;
	private Zmq.Context context;
	private Parser parser;
	private Zmq.Socket subscriberSocket;
	private Zmq.Socket cancelSocket;
	private int applicationId = -1;
	private AtomicBoolean ended = new AtomicBoolean(false);
	private AtomicBoolean canceled = new AtomicBoolean(false);

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
		Zmq.Poller poller = this.context.createPoller(1);
		poller.register(subscriber);
		
		while (true) {
			// The server returns a SYNC_STREAM message that is used to synchronize the subscriber.
			try {
				requestSocket.requestJSON(Messages.createSyncStreamRequest(name));
			}
			catch (ConnectionTimeout e) {
				// Do nothing.
			}

			// Return at the first message received by the subscriber.
			poller.poll(100);
			if (poller.pollin(0)) {
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
	
	public OutputStreamSocket.Output receive()	{
		
		// Loop on recvStr() because in case of configuration multiple=yes, messages can come from different instances.
		while (true) {
			String messageType = this.subscriberSocket.recvStr();
			
			// Cancel can only come from this instance.
			if (messageType.equals(Messages.Event.CANCEL)) {
				canceled.set(true);
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
						ended.set(true);
						return null;
					}
					
					// Here the type of message is STREAM.
					String line = JSON.getString(jsonMessage, Messages.ApplicationStream.MESSAGE);
					boolean endOfLine = JSON.getBoolean(jsonMessage, Messages.ApplicationStream.EOL);
					
					return new OutputStreamSocket.Output(id, line, endOfLine);
				}
				
				// Here, the application id is different from id, then re-iterate.
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse response: " + messageValue);
			}
		}
	}
	
	public boolean hasEnded() {
		return ended.get();
	}
	
	public void cancel() {
		cancelSocket.sendMore(Messages.Event.CANCEL);
		cancelSocket.send(Messages.Event.CANCEL);
	}
	
	public boolean isCanceled() {
		return canceled.get();
	}
	
	public void terminate() {
		context.destroySocket(subscriberSocket);
	}
}