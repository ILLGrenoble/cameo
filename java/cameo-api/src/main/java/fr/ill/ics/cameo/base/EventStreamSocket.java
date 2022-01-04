package fr.ill.ics.cameo.base;
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



import fr.ill.ics.cameo.base.impl.EventStreamSocketImpl;
import fr.ill.ics.cameo.base.impl.zmq.EventStreamSocketZmq;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.strings.Endpoint;

public class EventStreamSocket {
	
	private EventStreamSocketImpl impl;
	
	public EventStreamSocket() {
		//TODO Replace with factory.
		this.impl = new EventStreamSocketZmq();
	}

	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		impl.init(context, endpoint, requestSocket, parser);
	}
	
	public Event receive() {
		return impl.receive();
	}
	
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	public void cancel() {
		impl.cancel();
	}

	public void destroy() {
		impl.destroy();
	}
}