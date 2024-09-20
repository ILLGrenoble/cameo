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

package eu.ill.cameo.server;

import org.json.simple.JSONObject;

import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.com.Zmq.Msg;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.server.manager.ApplicationState;

public class Converter {

	public static String toString(int state) {
		
		if (state == ApplicationState.NIL) {
			return "NIL";
		}
		else if (state == ApplicationState.STARTING) {
			return "STARTING";
		}
		else if (state == ApplicationState.RUNNING) {
			return "RUNNING";
		}
		else if (state == ApplicationState.STOPPING) {
			return "STOPPING";
		}
		else if (state == ApplicationState.KILLING) {
			return "KILLING";
		}
		else if (state == ApplicationState.PROCESSING_FAILURE) {
			return "PROCESSING_FAILURE";
		}
		else if (state == ApplicationState.FAILURE) {
			return "FAILURE";
		}
		else if (state == ApplicationState.SUCCESS) {
			return "SUCCESS";
		}
		else if (state == ApplicationState.STOPPED) {
			return "STOPPED";
		}
		else if (state == ApplicationState.KILLED) {
			return "KILLED";
		}
		
		return "NIL";		
	}
	
	public static Msg reply(JSONObject response) {
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(Messages.serialize(response));
		
		return reply;
	}
}