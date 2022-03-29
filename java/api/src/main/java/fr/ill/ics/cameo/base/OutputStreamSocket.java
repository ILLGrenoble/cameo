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

package fr.ill.ics.cameo.base;


import fr.ill.ics.cameo.base.impl.OutputStreamSocketImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.strings.Endpoint;

public class OutputStreamSocket {
	
	public static class Output {
		
		private int id;
		private String message;
		private boolean endOfLine;
			
		public Output(int id, String message, boolean endOfLine) {
			super();
			this.id = id;
			this.message = message;
			this.endOfLine = endOfLine;
		}
	
		public int getId() {
			return id;
		}
	
		public String getMessage() {
			return message;
		}
		
		public boolean isEndOfLine() {
			return endOfLine;
		}
	
		@Override
		public String toString() {
			return "[id=" + id + ", message=" + message + " eol=" + endOfLine + "]";
		}
	
	}

	private OutputStreamSocketImpl impl;
	private int applicationId;

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
		this.applicationId = id;
		impl.setApplicationId(id);
	}
	
	public OutputStreamSocket.Output receive()	{
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
	
	@Override
	public String toString() {
		return "[applicationId=" + applicationId + "]";
	}

}