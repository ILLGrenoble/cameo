package fr.ill.ics.cameo;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;


public class Zmq {

	public static final int REP = 1;
	public static final int REQ = 2;
	public static final int PUB = 3;
	public static final int SUB = 4;
	
	public static class Msg {
		
		private ZMsg message;
		
		public Msg() {
			message = new ZMsg();
		}
		
		Msg(ZMsg message) {
			this.message = message;
		}

		public static Msg recvMsg(Socket socket) {
			return new Msg(ZMsg.recvMsg(socket.socket));
		}

		public int size() {
			return message.size();
		}
		
		public byte[] getFirstData() {
			return message.getFirst().getData();
		}
		
		public byte[] getLastData() {
			return message.getLast().getData();
		}

		public void send(Socket socket) {
			message.send(socket.socket);			
		}

		public void add(String data) {
			message.add(data);
		}

		public void add(byte[] data) {
			message.add(data);
		}
	}
	
	public static class Socket {
		
		private ZMQ.Socket socket;
		
		Socket(ZMQ.Socket socket) {
			this.socket = socket;
		}

		public void bind(String endpoint) {
			socket.bind(endpoint);
		}

		public void sendMore(String data) {
			socket.sendMore(data);
		}

		public void send(byte[] data, int flags) {
			socket.send(data, flags);
		}
	}
	
	public static class Context {
	
		private ZContext context;
		
		public Context() {
			context = new ZContext();
		}

		public Socket createSocket(int type) {
			
			switch (type) {
			case REP:
				return new Socket(context.createSocket(ZMQ.REP));
			case REQ:
				return new Socket(context.createSocket(ZMQ.REQ));
			case PUB:
				return new Socket(context.createSocket(ZMQ.PUB));
			case SUB:
				return new Socket(context.createSocket(ZMQ.SUB));
			}
						
			return null;
		}

		public void close() {
			context.close();
		}
		
	}
}
