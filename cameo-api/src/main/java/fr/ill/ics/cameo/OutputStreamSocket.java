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

public class OutputStreamSocket {
	
	private ServicesImpl services;
	private Zmq.Socket socket;
	private Zmq.Socket cancelSocket;
	private boolean ended = false;
	private boolean canceled = false;

	protected static final String STREAM = "STREAM";
	protected static final String ENDSTREAM = "ENDSTREAM";
	protected static final String CANCEL = "CANCEL";

	
	public OutputStreamSocket(ServicesImpl services, Zmq.Socket subscriber, Zmq.Socket cancelPublisher) {
		super();
		this.services = services;
		this.socket = subscriber;
		this.cancelSocket = cancelPublisher;		
	}
	
	public Application.Output receive()	{
		
		String message = this.socket.recvStr();
				
		if (message.equals(STREAM)) {
		}
		else if (message.equals(ENDSTREAM)) {
			ended = true;
			return null;
		}
		else if (message.equals(CANCEL)) {
			canceled = true;
			return null;
		}
				
		byte[] streamMessage = this.socket.recv();
		
		try {
			// Get the JSON object.
			JSONObject stream = services.parse(streamMessage);
			
			int id = JSON.getInt(stream, Message.ApplicationStream.ID);
			String line = JSON.getString(stream, Message.ApplicationStream.MESSAGE);
			
			return new Application.Output(id, line);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	
	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void cancel() {
		cancelSocket.sendMore(CANCEL);
		cancelSocket.send("cancel");
	}
	
	public void destroy() {
		services.destroySocket(socket);
	}
}