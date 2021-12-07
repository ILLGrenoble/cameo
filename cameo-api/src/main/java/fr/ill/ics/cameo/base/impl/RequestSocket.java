package fr.ill.ics.cameo.base.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.SocketException;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.messages.JSON.ConcurrentParser;
import fr.ill.ics.cameo.messages.Messages;

public class RequestSocket {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private int timeout = 0;
	private ConcurrentParser parser;

	public RequestSocket(Zmq.Context context, int timeout, ConcurrentParser parser) {
		this.context = context;
		this.socket = context.createSocket(Zmq.REQ);
		this.timeout = timeout;
		this.parser = parser;
	}
	
	public RequestSocket(Zmq.Context context, int timeout) {
		this.context = context;
		this.socket = context.createSocket(Zmq.REQ);
		this.timeout = timeout;
		this.parser = null;
	}
		
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void connect(String endpoint) {
		
		try {
			boolean result = socket.connect(endpoint);
			if (!result) {
				throw new SocketException("Cannot connect socket to " + endpoint);
			}
		}
		catch (Exception e) {
			throw new SocketException(e.getMessage());
		}
	}
	
	public Zmq.Msg request(Zmq.Msg request, int overrideTimeout) throws ConnectionTimeout {

		// send request, wait safely for reply
		Zmq.Msg msg = request.duplicate();
		msg.send(socket);

		int usedTimeout = timeout;
		if (overrideTimeout > -1) {
			usedTimeout = overrideTimeout;
		}

		if (usedTimeout > 0) {

			Zmq.Poller poller = context.createPoller(socket);
			Zmq.Msg reply = null;
			if (poller.poll(usedTimeout)) {
				reply = Zmq.Msg.recvMsg(socket);
			}
			else {
				throw new ConnectionTimeout();
			}

			return reply;

		} else {
			// direct receive
			Zmq.Msg reply = Zmq.Msg.recvMsg(socket);

			return reply;
		}
	}
	
	public Zmq.Msg request(Zmq.Msg request) throws ConnectionTimeout {
		return request(request, -1);
	}
	
	//TODO Remove when possible: verify responses from server must always be JSON!!!
	public void request(JSONObject request) throws ConnectionTimeout {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		request(message, -1);
	}
	
	//TODO Remove when possible: verify responses from server must always be JSON!!!
	public void request(JSONObject request, int timeout) throws ConnectionTimeout {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		request(message, timeout);
	}
	
	
	public JSONObject requestJSON(JSONObject request, int timeout) throws ConnectionTimeout {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		Zmq.Msg reply = request(message, timeout);
		
		try {
			return parser.parse(Messages.parseString(reply.getFirstData()));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public JSONObject requestJSON(JSONObject request) throws ConnectionTimeout {
		return requestJSON(request, -1);
	}
	
	public void terminate() {
		// it is better to call destroySocket rather than socket.close()
		// it is really important to destroy the socket because Java will do
		// it later
		// with the garbage collector
		context.destroySocket(socket);
	}
}
