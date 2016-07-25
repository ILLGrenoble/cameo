package fr.ill.ics.cameo.impl;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import fr.ill.ics.cameo.ConnectionTimeout;

public class RequestSocket {

	private ZContext context;
	private Socket socket;
	private int timeout;

	public RequestSocket(ZContext context, Socket socket, int timeout) {
		this.context = context;
		this.socket = socket;
		this.timeout = timeout;
	}

	public ZMsg request(ZMsg request, int overrideTimeout) throws ConnectionTimeout {

		// send request, wait safely for reply
		ZMsg msg = request.duplicate();
		msg.send(socket);

		int usedTimeout = timeout;
		if (overrideTimeout > -1) {
			usedTimeout = overrideTimeout;
		}

		if (usedTimeout > 0) {

			PollItem[] items = { new PollItem(socket, ZMQ.Poller.POLLIN) };
			ZMQ.poll(items, usedTimeout);
			ZMsg reply = null;

			// in case a response is returned before timeout
			if (items[0].isReadable()) {
				reply = ZMsg.recvMsg(socket);

			} else {
				throw new ConnectionTimeout();
			}

			return reply;

		} else {
			// direct receive
			ZMsg reply = ZMsg.recvMsg(socket);

			return reply;
		}
	}
	
	public ZMsg request(ZMsg request) throws ConnectionTimeout {
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
