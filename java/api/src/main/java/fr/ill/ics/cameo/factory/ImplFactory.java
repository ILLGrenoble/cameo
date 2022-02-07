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
	
	public static RequestSocketImpl createRequestSocket(Context context, int timeout) {
		return new RequestSocketZmq(context, timeout);
	}
	
	public static EventStreamSocketImpl createEventStreamSocket() {
		return new EventStreamSocketZmq();
	}
	
	public static OutputStreamSocketImpl createOutputStreamSocket(String name) {
		return new OutputStreamSocketZmq(name);	
	}
	
	public static PublisherImpl createPublisher(String name, int numberOfSubscribers) {
		return new PublisherZmq(name, numberOfSubscribers);
	}
	
	public static SubscriberImpl createSubscriber() {
		return new SubscriberZmq();	
	}
	
	public static fr.ill.ics.cameo.coms.legacy.impl.zmq.ResponderZmq createLegacyResponder() {
		return new fr.ill.ics.cameo.coms.legacy.impl.zmq.ResponderZmq();
	}
	
	public static fr.ill.ics.cameo.coms.basic.impl.zmq.ResponderZmq createBasicResponder() {
		return new fr.ill.ics.cameo.coms.basic.impl.zmq.ResponderZmq();
	}

	public static fr.ill.ics.cameo.coms.legacy.impl.zmq.RequesterZmq createLegacyRequester() {
		return new fr.ill.ics.cameo.coms.legacy.impl.zmq.RequesterZmq();
	}
	
	public static fr.ill.ics.cameo.coms.basic.impl.zmq.RequesterZmq createBasicRequester() {
		return new fr.ill.ics.cameo.coms.basic.impl.zmq.RequesterZmq();
	}
	
}
