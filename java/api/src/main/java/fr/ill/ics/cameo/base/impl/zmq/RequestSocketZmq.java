package fr.ill.ics.cameo.base.impl.zmq;

import java.util.Arrays;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.SocketException;
import fr.ill.ics.cameo.base.impl.RequestSocketImpl;

public class RequestSocketZmq implements RequestSocketImpl {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private int timeout = 0;
	private String responderIdentity;

	public RequestSocketZmq(Context context, int timeout) {
		// Get the Zmq context.
		this.context = ((ContextZmq)context).getContext();
		this.socket = this.context.createSocket(Zmq.REQ);
		this.timeout = timeout;
	}
			
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void connect(String endpoint, String responderIdentity) {
		
		this.responderIdentity = responderIdentity;
		
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
	
	private Zmq.Msg createMessage() {
	
		Zmq.Msg message = new Zmq.Msg();
		
		// Add the responder identity as first part.
		message.add(responderIdentity);
		message.add(new byte[0]);
	
		return message;
	}
	
	public byte[][] request(byte[] part1, int overrideTimeout) {
	
		// Create the message which adds the responder identity.
		Zmq.Msg message = createMessage();
		message.add(part1);

		// Remove the first two parts that contain the responder identity.
		byte[][] data = request(message, overrideTimeout).getAllData();
		
		return Arrays.copyOfRange(data, 2, data.length);
	}
	
	public byte[][] request(byte[] part1, byte[] part2, int overrideTimeout) {

		// Create the message which adds the responder identity.
		Zmq.Msg message = createMessage();
		message.add(part1);
		message.add(part2);
	
		// Remove the first two parts that contain the responder identity.
		byte[][] data = request(message, overrideTimeout).getAllData();
		
		return Arrays.copyOfRange(data, 2, data.length);
	}
	
	public byte[][] request(byte[] part1, byte[] part2, byte[] part3, int overrideTimeout) {
		
		// Create the message which adds the responder identity.
		Zmq.Msg message = createMessage();
		message.add(part1);
		message.add(part2);
		message.add(part3);
	
		// Remove the first two parts that contain the responder identity.
		byte[][] data = request(message, overrideTimeout).getAllData();
		
		return Arrays.copyOfRange(data, 2, data.length);
	}
	
	public Zmq.Msg request(Zmq.Msg request, int overrideTimeout) throws ConnectionTimeout {

		// Send request.
		request.send(socket);

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
		}
		else {
			// Direct receive.
			Zmq.Msg reply = Zmq.Msg.recvMsg(socket);

			return reply;
		}
	}
	
	public void terminate() {
		// It is better to call destroySocket rather than socket.close().
		// It is really important to destroy the socket because Java will do it later with the garbage collector
		context.destroySocket(socket);
	}
}
