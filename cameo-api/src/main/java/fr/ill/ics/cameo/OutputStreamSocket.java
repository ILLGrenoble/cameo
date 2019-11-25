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

import fr.ill.ics.cameo.proto.Messages;

public class OutputStreamSocket {
	
	private Zmq.Context context;
	private Zmq.Socket socket;
	private Zmq.Socket cancelSocket;

	protected static final String STREAM = "STREAM";
	protected static final String ENDSTREAM = "ENDSTREAM";
	protected static final String CANCEL = "CANCEL";

	
	public OutputStreamSocket(Zmq.Context context, Zmq.Socket subscriber, Zmq.Socket cancelPublisher) {
		super();
		this.context = context;
		this.socket = subscriber;
		this.cancelSocket = cancelPublisher;		
	}
	
	public Application.Output receive()	{
		
		String response = this.socket.recvStr();
				
		boolean end = false;
		
		if (response.equals(STREAM)) {
			end = false;
		} else if (response.equals(ENDSTREAM)) {
			end = true;
		} else {
			System.err.println("bad stream message header " + response);
			return null;
		}
				
		byte[] messageResponse = this.socket.recv();
		
		try {
			Messages.ApplicationStream protoStream = Messages.ApplicationStream.parseFrom(messageResponse);
			return new Application.Output(protoStream.getId(), protoStream.getMessage(), end);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public void cancel() {
		cancelSocket.sendMore(CANCEL);
		cancelSocket.send("cancel");
	}
	
	public void destroy() {
		context.destroySocket(socket);
	}
}