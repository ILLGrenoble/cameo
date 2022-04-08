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

package fr.ill.ics.cameo.coms.impl.zmq;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class PublisherZmq implements PublisherImpl {

	private int publisherPort;
	private String publisherIdentity;
	private Zmq.Context context;
	private Zmq.Socket publisher = null;
	private boolean ended = false;
	
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
		
		if (!ended) {
			publisher.sendMore(publisherIdentity);
	
			JSONObject messageType = new JSONObject();
			messageType.put(Messages.TYPE, Messages.STREAM_END);
			publisher.send(Messages.serialize(messageType), 0);
			
			ended = true;
		}
	}

	public boolean hasEnded() {
		return ended;
	}
	
	public void terminate() {
		
		sendEnd();
		
		context.destroySocket(publisher);
		
		// Release the publisher port.
		This.getCom().releasePort(publisherPort);
	}
		
}