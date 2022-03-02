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



import fr.ill.ics.cameo.base.impl.OutputStreamSocketImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.strings.Endpoint;

public class OutputStreamSocket {
	
	private OutputStreamSocketImpl impl;

	public OutputStreamSocket(String name) {
		impl = ImplFactory.createOutputStreamSocket(name);
	}

	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		impl.init(context, endpoint, requestSocket, parser);
	}
	
	/**
	 * Sets the application id.
	 * @param id
	 */
	public void setApplicationId(int id) {
		impl.setApplicationId(id);
	}
	
	public Application.Output receive()	{
		return impl.receive();
	}
	
	public boolean isEnded() {
		return impl.isEnded();
	}
	
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	public void cancel() {
		impl.cancel();
	}
	
	public void terminate() {
		impl.terminate();
	}

}