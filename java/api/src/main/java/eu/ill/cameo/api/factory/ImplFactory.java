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

package eu.ill.cameo.api.factory;

import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.api.base.impl.EventStreamSocketImpl;
import eu.ill.cameo.api.base.impl.OutputStreamSocketImpl;
import eu.ill.cameo.api.base.impl.RequestSocketImpl;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.base.impl.zmq.EventStreamSocketZmq;
import eu.ill.cameo.api.base.impl.zmq.OutputStreamSocketZmq;
import eu.ill.cameo.api.base.impl.zmq.RequestSocketZmq;
import eu.ill.cameo.api.coms.impl.PublisherImpl;
import eu.ill.cameo.api.coms.impl.SubscriberImpl;
import eu.ill.cameo.api.coms.impl.zmq.PublisherZmq;
import eu.ill.cameo.api.coms.impl.zmq.SubscriberZmq;
import eu.ill.cameo.api.coms.impl.zmq.SyncPublisherZmq;

/**
 * Class defining an implementation factory.
 * This implementation of the factory generates ZeroMQ implementations.
 */
public class ImplFactory {

	private static Context context;
	
	/**
	 * Gets the default context.
	 * @return The default Context object.
	 */
	public static synchronized Context getDefaultContext() {
		
		if (context == null) {
			context = new ContextZmq();
		}
		
		return context;
	}

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
	 * @param sync True if is a synchronized publisher.
	 * @return An PublisherImpl object.
	 */
	public static PublisherImpl createPublisher(boolean sync) {
		if (sync) {
			return new SyncPublisherZmq();
		}
		else {
			return new PublisherZmq();
		}
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
	 * @return A eu.ill.cameo.coms.basic.impl.ResponderImpl object.
	 */
	public static eu.ill.cameo.api.coms.basic.impl.ResponderImpl createBasicResponder() {
		return new eu.ill.cameo.api.coms.basic.impl.zmq.ResponderZmq();
	}
	
	/**
	 * Creates a requester.
	 * @return A eu.ill.cameo.coms.impl.RequesterImpl object.
	 */
	public static eu.ill.cameo.api.coms.impl.RequesterImpl createRequester() {
		return new eu.ill.cameo.api.coms.impl.zmq.RequesterZmq();
	}
	
	/**
	 * Creates a multi responder.
	 * @return A eu.ill.cameo.coms.multi.impl.ResponderImpl object.
	 */
	public static eu.ill.cameo.api.coms.multi.impl.ResponderImpl createMultiResponder() {
		return new eu.ill.cameo.api.coms.multi.impl.zmq.ResponderZmq();
	}
	
	/**
	 * Creates a multi responder router.
	 * @return A eu.ill.cameo.coms.multi.impl.ResponderRouterImpl object.
	 */
	public static eu.ill.cameo.api.coms.multi.impl.ResponderRouterImpl createMultiResponderRouter() {
		return new eu.ill.cameo.api.coms.multi.impl.zmq.ResponderRouterZmq();
	}
	
}
