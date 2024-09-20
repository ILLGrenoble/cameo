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

import eu.ill.cameo.api.base.TimeoutCounter;
import eu.ill.cameo.common.strings.Endpoint;

public interface RequesterImpl {

	void setPollingTime(int value);
	void setTimeout(int value);
	
	void init(Endpoint endpoint, String responderIdentity, TimeoutCounter timeoutCounter);
	
	void send(byte[] requestData);
	void send(String request);
	void sendTwoParts(byte[] requestData1, byte[] requestData2);
	
	byte[] receive();
	String receiveString();
	
	void cancel();
	boolean isCanceled();
	
	boolean hasTimedout();
	
	void terminate();
	
}