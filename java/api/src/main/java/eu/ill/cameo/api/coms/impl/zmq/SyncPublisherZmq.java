/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.impl.zmq;

public class SyncPublisherZmq extends PublisherZmq {

	public synchronized void sendSync() {
		super.sendSync();
	}
	
	public synchronized void send(byte[] data) {
		super.send(data);
	}
	
	public synchronized void send(String data) {
		super.send(data);
	}
	
	public synchronized void sendTwoParts(byte[] data1, byte[] data2) {
		super.sendTwoParts(data1, data2);
	}
	
	public synchronized void sendEnd() {
		super.sendEnd();
	}
		
}