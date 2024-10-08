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

package eu.ill.cameo.api.coms.impl;

public interface PublisherImpl {
	
	void init(String publisherIdentity);
	
	int getPublisherPort();
	
	void sendSync();
	void send(byte[] data);
	void send(String data);
	void sendTwoParts(byte[] data1, byte[] data2);
	void sendEnd();
	boolean hasEnded();
	void terminate();
}