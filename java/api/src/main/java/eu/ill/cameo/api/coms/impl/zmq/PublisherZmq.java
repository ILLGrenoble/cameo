/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.coms.impl.PublisherImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.Endpoint;

public class PublisherZmq implements PublisherImpl {

	private int publisherPort;
	private String publisherIdentity;
	private Zmq.Context context;
	private Zmq.Socket publisher = null;
	private AtomicBoolean ended = new AtomicBoolean(false);
	
	public void init(String publisherIdentity) {
		
		this.publisherIdentity = publisherIdentity;
		
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		publisher = context.createSocket(Zmq.PUB);
		
		// Connect to the proxy.
		Endpoint subscriberProxyEndpoint = This.getEndpoint().withPort(This.getCom().getSubscriberProxyPort());
		publisher.connect(subscriberProxyEndpoint.toString());
		
		String endpointPrefix = "tcp://*:";	
		
		// Loop to find an available port for the publisher.
		while (true) {
		
			int port = This.getCom().requestPort();
			String pubEndpoint = endpointPrefix + port;

			try {
				publisher.bind(pubEndpoint);
				publisherPort = port;
				break;
			}
			catch (Exception e) {
				This.getCom().setPortUnavailable(port);
			}
		}
	}
	
	public int getPublisherPort() {
		return publisherPort;
	}
	
	public void sendSync() {
		
		publisher.sendMore(publisherIdentity);
		
		JSONObject messageType = new JSONObject();
		messageType.put(Messages.TYPE, Messages.SYNC_STREAM);
		publisher.send(Messages.serialize(messageType), 0);
	}
	
	public void send(byte[] data) {
		
		publisher.sendMore(publisherIdentity);
		
		JSONObject messageType = new JSONObject();
		messageType.put(Messages.TYPE, Messages.STREAM);
		publisher.sendMore(Messages.serialize(messageType));
		
		publisher.send(data, 0);
	}
	
	public void send(String data) {
		
		publisher.sendMore(publisherIdentity);
		
		JSONObject messageType = new JSONObject();
		messageType.put(Messages.TYPE, Messages.STREAM);
		publisher.sendMore(Messages.serialize(messageType));
				
		byte[] result = Messages.serialize(data);
		publisher.send(result, 0);
	}
	
	public void sendTwoParts(byte[] data1, byte[] data2) {
	
		publisher.sendMore(publisherIdentity);
	
		JSONObject messageType = new JSONObject();
		messageType.put(Messages.TYPE, Messages.STREAM);
		publisher.sendMore(Messages.serialize(messageType));
		
		publisher.sendMore(data1);
		publisher.send(data2, 0);
	}
	
	public void sendEnd() {
		
		if (!ended.get()) {
			publisher.sendMore(publisherIdentity);
	
			JSONObject messageType = new JSONObject();
			messageType.put(Messages.TYPE, Messages.STREAM_END);
			publisher.send(Messages.serialize(messageType), 0);
			
			ended.set(true);
		}
	}

	public boolean hasEnded() {
		return ended.get();
	}
	
	public void terminate() {
		
		sendEnd();
		
		context.destroySocket(publisher);
		
		// Release the publisher port.
		This.getCom().releasePort(publisherPort);
	}
		
}