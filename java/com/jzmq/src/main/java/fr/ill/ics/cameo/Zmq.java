package fr.ill.ics.cameo;

import java.util.Iterator;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;


public class Zmq {

	public static final int REP = 1;
	public static final int REQ = 2;
	public static final int PUB = 3;
	public static final int SUB = 4;
	public static final int ROUTER = 5;
	public static final int DEALER = 6;
	
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
		
		public byte[][] getAllData() {
			
			byte[][] result = new byte[message.size()][];
			Iterator<ZFrame> iterator = message.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				ZFrame frame = iterator.next();
				result[i] = frame.getData();
				++i;
			}
			
			return result;
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

		public Msg duplicate() {
			return new Msg(message.duplicate());
		}

		public void destroy() {
			message.destroy();
		}
	}
	
	public static class Socket {
		
		private ZMQ.Socket socket;
		
		Socket(ZMQ.Socket socket) {
			this.socket = socket;
		}

		public void setIdentity(String identity) {
			socket.setIdentity(identity.getBytes());
		}
		
		public void bind(String endpoint) {
			socket.bind(endpoint);
		}

		public void sendMore(String data) {
			socket.send(data, 2);
		}

		public void sendMore(byte[] data) {
			socket.send(data, 2);
		}
		
		public void send(byte[] data, int flags) {
			socket.send(data, flags);
		}

		public boolean connect(String address) {
			socket.connect(address);
			return true;
		}

		public void subscribe(String topic) {
			socket.subscribe(topic.getBytes());
		}

		public void send(String data) {
			socket.send(data);
		}

		public String recvStr() {
			return socket.recvStr();
		}

		public byte[] recv() {
			return socket.recv();
		}
	}
	
	public static class Poller {
		
		private PollItem[] items;
		private int currentIndex = 0;
		
		Poller(ZContext context, int size) {
			items = new PollItem[size];
		}
		
		public void register(Zmq.Socket socket) {
			items[currentIndex] = new PollItem(socket.socket, ZMQ.Poller.POLLIN);
			currentIndex++;
		}
		
		public void poll(long timeout) {
			ZMQ.poll(items, timeout);
		}
		
		public boolean pollin(int i) {
			return items[i].isReadable();
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
			case ROUTER:
				return new Socket(context.createSocket(ZMQ.ROUTER));
			case DEALER:
				return new Socket(context.createSocket(ZMQ.DEALER));
			}
						
			return null;
		}

		public void close() {
			context.close();
		}

		public void destroy() {
			context.destroy();
		}

		public void destroySocket(Socket socket) {
			context.destroySocket(socket.socket);
		}
		
		public Poller createPoller(int size) {
			return new Poller(context, size);
		}
		
	}
	
}
