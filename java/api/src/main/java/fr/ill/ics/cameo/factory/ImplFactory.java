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

/**
 * Class defining an implementation factory.
 * This implementation of the factory generates ZeroMQ implementations.
 */
public class ImplFactory {

	/**
	 * Creates a context.
	 * @return A Context object.
	 */
	public static Context createContext() {
		return new ContextZmq();
	}
	
	/**
	 * Creates a request socket.
	 * @param context The context.
	 * @param endpoint The endpoint.
	 * @param responderIdentity The responder identity.
	 * @param timeout The timeout.
	 * @return A RequestSocketImpl object.
	 */
	public static RequestSocketImpl createRequestSocket(Context context, String endpoint, String responderIdentity, int timeout) {
		return new RequestSocketZmq(context, endpoint, responderIdentity, timeout);
	}
	
	/**
	 * Creates an event stream socket.
	 * @return An EventStreamSocketImpl object.
	 */
	public static EventStreamSocketImpl createEventStreamSocket() {
		return new EventStreamSocketZmq();
	}
	
	/**
	 * Creates an outout stream socket.
	 * @param name The application name.
	 * @return An OutputStreamSocketImpl object.
	 */
	public static OutputStreamSocketImpl createOutputStreamSocket(String name) {
		return new OutputStreamSocketZmq(name);	
	}
	
	/**
	 * Creates a publisher.
	 * @return An PublisherImpl object.
	 */
	public static PublisherImpl createPublisher() {
		return new PublisherZmq();
	}
	
	/**
	 * Creates a subscriber.
	 * @return A SubscriberImpl object.
	 */
	public static SubscriberImpl createSubscriber() {
		return new SubscriberZmq();	
	}
	
	/**
	 * Creates a basic responder.
	 * @return A fr.ill.ics.cameo.coms.basic.impl.ResponderImpl object.
	 */
	public static fr.ill.ics.cameo.coms.basic.impl.ResponderImpl createBasicResponder() {
		return new fr.ill.ics.cameo.coms.basic.impl.zmq.ResponderZmq();
	}
	
	/**
	 * Creates a requester.
	 * @return A fr.ill.ics.cameo.coms.impl.RequesterImpl object.
	 */
	public static fr.ill.ics.cameo.coms.impl.RequesterImpl createRequester() {
		return new fr.ill.ics.cameo.coms.impl.zmq.RequesterZmq();
	}
	
	/**
	 * Creates a multi responder.
	 * @return A fr.ill.ics.cameo.coms.multi.impl.ResponderImpl object.
	 */
	public static fr.ill.ics.cameo.coms.multi.impl.ResponderImpl createMultiResponder() {
		return new fr.ill.ics.cameo.coms.multi.impl.zmq.ResponderZmq();
	}
	
	/**
	 * Creates a multi responder router.
	 * @return A fr.ill.ics.cameo.coms.multi.impl.ResponderRouterImpl object.
	 */
	public static fr.ill.ics.cameo.coms.multi.impl.ResponderRouterImpl createMultiResponderRouter() {
		return new fr.ill.ics.cameo.coms.multi.impl.zmq.ResponderRouterZmq();
	}
	
}
