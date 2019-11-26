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



import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.proto.Messages;

public class EventStreamSocket {
		
	private Zmq.Context context;
	private Zmq.Socket socket;
	private Zmq.Socket cancelSocket;
	private boolean canceled = false;
	
	private static final String STATUS = "STATUS";
	private static final String RESULT = "RESULT";
	private static final String PUBLISHER = "PUBLISHER";
	private static final String PORT = "PORT";
	private static final String CANCEL = "CANCEL";
	
	public EventStreamSocket(Zmq.Context context, Zmq.Socket subscriber, Zmq.Socket cancelPublisher) {
		super();
		this.context = context;
		this.socket = subscriber;
		this.cancelSocket = cancelPublisher;
	}
	
	public Event receive() {
		
		String response = this.socket.recvStr();
		Event event = null;
		
		// We can receive messages from the status publisher located in the server
		// as well as messages from the cancel publisher located in the same process.
		if (response.equals(STATUS)) {
			
			byte[] statusResponse = this.socket.recv();
			
			try {
				Messages.StatusEvent protoStatus = Messages.StatusEvent.parseFrom(statusResponse);
				event = new StatusEvent(protoStatus.getId(), protoStatus.getName(), protoStatus.getApplicationState(), protoStatus.getPastApplicationStates());
				
			} catch (InvalidProtocolBufferException e) {
				throw new UnexpectedException("Cannot parse response");
			}
			
		} else if (response.equals(RESULT)) {
				
				byte[] resultResponse = this.socket.recv();
				
				try {
					Messages.ResultEvent protoResult = Messages.ResultEvent.parseFrom(resultResponse);
					event = new ResultEvent(protoResult.getId(), protoResult.getName(), protoResult.getData().toByteArray());
					
				} catch (InvalidProtocolBufferException e) {
					throw new UnexpectedException("Cannot parse response");
				}
			
		} else if (response.equals(PUBLISHER)) {
			
			byte[] publisherResponse = this.socket.recv();
			
			try {
				Messages.PublisherEvent protoPublisher = Messages.PublisherEvent.parseFrom(publisherResponse);
				event = new PublisherEvent(protoPublisher.getId(), protoPublisher.getName(), protoPublisher.getPublisherName());
				
			} catch (InvalidProtocolBufferException e) {
				throw new UnexpectedException("Cannot parse response");
			}
		
		} else if (response.equals(PORT)) {
			
			byte[] portResponse = this.socket.recv();
			
			try {
				Messages.PortEvent protoPort = Messages.PortEvent.parseFrom(portResponse);
				event = new PortEvent(protoPort.getId(), protoPort.getName(), protoPort.getPortName());
				
			} catch (InvalidProtocolBufferException e) {
				throw new UnexpectedException("Cannot parse response");
			}
			
		} else if (response.equals(CANCEL)) {
			canceled = true;
			return null;
		}
	
		return event;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void cancel() {
		cancelSocket.sendMore(CANCEL);
		cancelSocket.send("cancel");
	}

	public void destroy() {
		context.destroySocket(socket);
	}
}