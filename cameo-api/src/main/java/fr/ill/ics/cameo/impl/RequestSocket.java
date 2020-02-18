package fr.ill.ics.cameo.impl;

import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Zmq;

public class RequestSocket {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private int timeout = 0;

	public RequestSocket(Zmq.Context context, Zmq.Socket socket, int timeout) {
		this.context = context;
		this.socket = socket;
		this.timeout = timeout;
	}
	
	public RequestSocket(Zmq.Context context, Zmq.Socket socket) {
		this.context = context;
		this.socket = socket;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
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
	
	public void terminate() {
		// it is better to call destroySocket rather than socket.close()
		// it is really important to destroy the socket because Java will do
		// it later
		// with the garbage collector
		context.destroySocket(socket);
	}
}
