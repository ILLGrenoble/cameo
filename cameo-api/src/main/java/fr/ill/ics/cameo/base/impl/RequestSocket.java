package fr.ill.ics.cameo.base.impl;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.SocketException;

public class RequestSocket {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private int timeout = 0;

	public RequestSocket(Zmq.Context context, int timeout) {
		this.context = context;
		this.socket = context.createSocket(Zmq.REQ);
		this.timeout = timeout;
	}
	
	public RequestSocket(Zmq.Context context) {
		this.context = context;
		this.socket = context.createSocket(Zmq.REQ);
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
	
	public Zmq.Msg request(JSONObject request, int overrideTimeout) throws ConnectionTimeout {
		return request(ServicesImpl.message(request), overrideTimeout);
	}
	
	public Zmq.Msg request(JSONObject request) throws ConnectionTimeout {
		return request(ServicesImpl.message(request), -1);
	}
	
	public void terminate() {
		// it is better to call destroySocket rather than socket.close()
		// it is really important to destroy the socket because Java will do
		// it later
		// with the garbage collector
		context.destroySocket(socket);
	}
}
