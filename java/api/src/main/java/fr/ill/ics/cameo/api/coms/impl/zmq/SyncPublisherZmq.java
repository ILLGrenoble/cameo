/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.api.coms.impl.zmq;

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