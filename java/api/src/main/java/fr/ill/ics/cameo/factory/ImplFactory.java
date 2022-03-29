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

package fr.ill.ics.cameo.factory;

import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.impl.EventStreamSocketImpl;
import fr.ill.ics.cameo.base.impl.OutputStreamSocketImpl;
import fr.ill.ics.cameo.base.impl.RequestSocketImpl;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.base.impl.zmq.EventStreamSocketZmq;
import fr.ill.ics.cameo.base.impl.zmq.OutputStreamSocketZmq;
import fr.ill.ics.cameo.base.impl.zmq.RequestSocketZmq;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.coms.impl.zmq.PublisherZmq;
import fr.ill.ics.cameo.coms.impl.zmq.SubscriberZmq;

public class ImplFactory {

	public static Context createContext() {
		return new ContextZmq();
	}
	
	public static RequestSocketImpl createRequestSocket(Context context, String endpoint, String responderIdentity, int timeout) {
		return new RequestSocketZmq(context, endpoint, responderIdentity, timeout);
	}
	
	public static EventStreamSocketImpl createEventStreamSocket() {
		return new EventStreamSocketZmq();
	}
	
	public static OutputStreamSocketImpl createOutputStreamSocket(String name) {
		return new OutputStreamSocketZmq(name);	
	}
	
	public static PublisherImpl createPublisher() {
		return new PublisherZmq();
	}
	
	public static SubscriberImpl createSubscriber() {
		return new SubscriberZmq();	
	}
	
	public static fr.ill.ics.cameo.coms.basic.impl.ResponderImpl createBasicResponder() {
		return new fr.ill.ics.cameo.coms.basic.impl.zmq.ResponderZmq();
	}
	
	public static fr.ill.ics.cameo.coms.impl.RequesterImpl createBasicRequester() {
		return new fr.ill.ics.cameo.coms.impl.zmq.RequesterZmq();
	}
	
	public static fr.ill.ics.cameo.coms.multi.impl.ResponderImpl createMultiResponder() {
		return new fr.ill.ics.cameo.coms.multi.impl.zmq.ResponderZmq();
	}
	
	public static fr.ill.ics.cameo.coms.multi.impl.ResponderRouterImpl createMultiResponderRouter() {
		return new fr.ill.ics.cameo.coms.multi.impl.zmq.ResponderRouterZmq();
	}
	
}
