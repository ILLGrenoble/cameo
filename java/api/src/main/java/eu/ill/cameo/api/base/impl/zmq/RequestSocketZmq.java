package eu.ill.cameo.api.base.impl.zmq;

import java.util.Arrays;

import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.api.base.SocketException;
import eu.ill.cameo.api.base.impl.RequestSocketImpl;
import eu.ill.cameo.com.Zmq;

public class RequestSocketZmq implements RequestSocketImpl {

	private Zmq.Context context;
	private Zmq.Socket socket;
	private String endpoint;
	private int timeout = 0;
	private String responderIdentity;

	public RequestSocketZmq(Context context, String endpoint, String responderIdentity, int timeout) {
		// Get the Zmq context.
		this.context = ((ContextZmq)context).getContext();
		this.endpoint = endpoint;
		this.responderIdentity = responderIdentity;
		this.timeout = timeout;
		
		init();
	}
			
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void init() {

		if (socket == null) {
			socket = this.context.createSocket(Zmq.REQ);
			
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
		
		//TODO set socket linger ?
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

		// Init if not already done or if a timeout occurred.
		init();
		
		// Send request.
		request.send(socket);

		int usedTimeout = timeout;
		if (overrideTimeout > -1) {
			usedTimeout = overrideTimeout;
		}

		if (usedTimeout > 0) {
			
			Zmq.Poller poller = this.context.createPoller(1);
			poller.register(socket);
			poller.poll(usedTimeout);
			Zmq.Msg reply = null;

			if (poller.pollin(0)) {
				reply = Zmq.Msg.recvMsg(socket);	
			}
			else {
				// Timeout occurred.
				// Reset the socket.
				terminate();
				
				throw new ConnectionTimeout(endpoint);
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
