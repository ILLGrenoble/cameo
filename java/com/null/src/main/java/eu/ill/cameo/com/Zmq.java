/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.com;

public class Zmq {

	public static final int REP = 1;
	public static final int REQ = 2;
	public static final int PUB = 3;
	public static final int SUB = 4;
	public static final int ROUTER = 5;
	public static final int DEALER = 6;
	
	public static class Msg {
		
		public Msg() {
		}

		public static Msg recvMsg(Socket socket) {
			return null;
		}

		public int size() {
			return 0;
		}
		
		public byte[] getFirstData() {
			return null;
		}
		
		public byte[] getLastData() {
			return null;
		}
		
		public byte[][] getAllData() {
			return null;
		}

		public void send(Socket socket) {
		}

		public void add(String data) {
		}

		public void add(byte[] data) {
		}

		public Msg duplicate() {
			return null;
		}

		public void destroy() {
		}
	}
	
	public static class Socket {

		public void setIdentity(String identity) {
		}
		
		public void bind(String endpoint) {
		}

		public void sendMore(String data) {
		}
		
		public void sendMore(byte[] data) {
		}

		public void send(byte[] data, int flags) {
		}

		public boolean connect(String address) {
			return false;
		}

		public void subscribe(String topic) {
		}

		public void send(String data) {
		}

		public String recvStr() {
			return "";
		}

		public byte[] recv() {
			return null;
		}
		
		public boolean hasMore() {
			return false;
		}
	}

	public static class Poller {
		
		public void register(Zmq.Socket socket) {
		}
		
		public void poll(long timeout) {
		}
		
		public boolean pollin(int i) {
			return false;	
		}
	}
	
	public static class Context {
	
		public Context() {
		}

		public Socket createSocket(int type) {
			return null;
		}

		public void close() {
		}

		public void destroy() {
		}

		public void destroySocket(Socket socket) {
		}
		
		public Poller createPoller(int size) {
			return null;
		}
		
	}
}