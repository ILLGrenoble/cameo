package fr.ill.ics.cameo.base.impl.zmq;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.SocketException;
import fr.ill.ics.cameo.base.impl.ContextImpl;
import fr.ill.ics.cameo.base.impl.RequestSocketImpl;

public class RequestSocketZmq implements RequestSocketImpl {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private int timeout = 0;

	public RequestSocketZmq(Context context, int timeout) {
		// Get the Zmq context.
		this.context = ((ContextImpl)context).getContext();
		this.socket = this.context.createSocket(Zmq.REQ);
		this.timeout = timeout;
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
	
	public byte[][] request(byte[] part1, int overrideTimeout) {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(part1);
		
		 return request(message, overrideTimeout).getAllData();
	}
	
	public byte[][] request(byte[] part1, byte[] part2, int overrideTimeout) {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(part1);
		message.add(part2);
		
		 return request(message, overrideTimeout).getAllData();
	}
	
	public byte[][] request(byte[] part1, byte[] part2, byte[] part3, int overrideTimeout) {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(part1);
		message.add(part2);
		message.add(part3);
		
		return request(message, overrideTimeout).getAllData();
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
