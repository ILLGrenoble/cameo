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

import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.common.strings.Endpoint;

public interface SubscriberImpl {
	
	void setPollingTime(int value);
	void setTimeout(int value);
	
	void init(int appId, Endpoint endpoint, Endpoint appStatusEndpoint, String publisherIdentity, boolean checkApp) throws ConnectionTimeout;
	boolean sync(int timeout);
	boolean hasEnded();
	boolean isCanceled();
	boolean hasTimedout();
	byte[] receive();
	byte[][] receiveTwoParts();
	String receiveString();
	void cancel();
	void terminate();
}