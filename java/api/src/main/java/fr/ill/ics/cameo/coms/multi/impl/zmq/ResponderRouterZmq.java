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

package fr.ill.ics.cameo.coms.multi.impl.zmq;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.multi.impl.ResponderRouterImpl;
import fr.ill.ics.cameo.strings.Endpoint;

public class ResponderRouterZmq implements ResponderRouterImpl {

	private int pollingTime = 100;
	private int responderPort;
	
	private Zmq.Context context;
	private Zmq.Socket router;
	private Zmq.Socket dealer;
	
	private boolean canceled = false;
	
	public void init(String responderIdentity, String dealerEndpoint) {
		
		// Create a socket ROUTER.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		router = context.createSocket(Zmq.ROUTER);
		
		// Set the identity.
		router.setIdentity(responderIdentity);
		
		// Connect to the proxy.
		Endpoint proxyEndpoint = This.getEndpoint().withPort(This.getCom().getResponderProxyPort());
		router.connect(proxyEndpoint.toString());
		
		String endpointPrefix = "tcp://*:";	
		
		// Loop to find an available port for the responder.
		while (true) {
		
			int port = This.getCom().requestPort();
			String pubEndpoint = endpointPrefix + port;

			try {
				router.bind(pubEndpoint);
				responderPort = port;
				break;
			}
			catch (Exception e) {
				This.getCom().setPortUnavailable(port);
			}
		}
		
		// Create a socket DEALER.
		dealer = context.createSocket(Zmq.DEALER);
		dealer.bind(dealerEndpoint);
	}
	
	public void setPollingTime(int value) {
		pollingTime = value;
	}
	
	public int getResponderPort() {
		return responderPort;
	}
	
	public void cancel() {
		canceled = true;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void run() {
		
	}
	
	public void terminate() {
		context.destroySocket(router);
		context.destroySocket(dealer);
		
		// Release the responder port.
		This.getCom().releasePort(responderPort);
	}
	
	
}