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

package eu.ill.cameo.api.base;


import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.impl.OutputStreamSocketImpl;
import eu.ill.cameo.api.factory.ImplFactory;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.strings.Endpoint;

/**
 * Class defining an output stream thread.
 */
public class OutputStreamSocket implements ICancelable {
	
	/**
	 * Class defining an output.
	 */
	public static class Output {
		
		private int id;
		private String message;
		private boolean endOfLine;
		
		/**
		 * Constructor.
		 * @param id The application id.
		 * @param message The message.
		 * @param endOfLine True if is end of line.
		 */
		public Output(int id, String message, boolean endOfLine) {
			super();
			this.id = id;
			this.message = message;
			this.endOfLine = endOfLine;
		}
	
		/**
		 * Gets the application id.
		 * @return The application id.
		 */
		public int getId() {
			return id;
		}
	
		/**
		 * Gets the message.
		 * @return The message.
		 */
		public String getMessage() {
			return message;
		}
		
		/**
		 * Returns true if is end of line.
		 * @return True if is end of line.
		 */
		public boolean isEndOfLine() {
			return endOfLine;
		}
	
		@Override
		public String toString() {
			JSONObject result = new JSONObject();
			
			result.put("id", id);
			result.put("message", message);
			result.put("eol", endOfLine);
			
			return result.toJSONString();
		}
	
	}

	private OutputStreamSocketImpl impl;
	private int applicationId;

	/**
	 * Constructor.
	 * @param name The application name.
	 */
	public OutputStreamSocket(String name) {
		impl = ImplFactory.createOutputStreamSocket(name);
	}

	/**
	 * Initializes the socket.
	 * @param context The context.
	 * @param endpoint The endpoint.
	 * @param requestSocket The request socket.
	 * @param parser The JSON parser.
	 */
	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		impl.init(context, endpoint, requestSocket, parser);
	}
	
	/**
	 * Sets the application id.
	 * @param id The application id.
	 */
	public void setApplicationId(int id) {
		this.applicationId = id;
		impl.setApplicationId(id);
	}

	/**
	 * Cancels the socket.
	 */
	@Override
	public void cancel() {
		impl.cancel();
	}
	
	/**
	 * Returns true if has canceled.
	 * @return True if has canceled.
	 */
	@Override
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	/**
	 * Receives an output.
	 * @return The output.
	 */
	public OutputStreamSocket.Output receive()	{
		return impl.receive();
	}
	
	/**
	 * Returns true if has ended.
	 * @return True if has ended.
	 */
	public boolean hasEnded() {
		return impl.hasEnded();
	}

	/**
	 * Terminates the socket.
	 */
	public void terminate() {
		impl.terminate();
	}

}