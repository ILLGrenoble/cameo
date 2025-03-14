/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.multi.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.coms.multi.impl.ResponderRouterImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.strings.Endpoint;

public class ResponderRouterZmq implements ResponderRouterImpl {

	private int pollingTime = 100;
	private int responderPort;

	private Zmq.Context context;
	private Zmq.Socket router;
	private Zmq.Socket dealer;

	private AtomicBoolean canceled = new AtomicBoolean(false);

	public void init(String responderIdentity, String dealerEndpoint) {

		// Create a socket ROUTER.
		this.context = ((ContextZmq) This.getCom().getContext()).getContext();
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
			String endpoint = endpointPrefix + port;

			try {
				router.bind(endpoint);
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
		canceled.set(true);
	}

	public boolean isCanceled() {
		return canceled.get();
	}

	public void run() {
		
		Zmq.Poller poller = context.createPoller(2);
		poller.register(router);
		poller.register(dealer);

		while (true) {

			poller.poll(pollingTime);

			if (poller.pollin(0)) {
				while (true) {

					byte[] message = router.recv();

					if (router.hasMore()) {
						dealer.sendMore(message);
					}
					else {
						dealer.send(message, 0);
						break;
					}
				}
			}

			if (poller.pollin(1)) {
				while (true) {

					byte[] message = dealer.recv();

					if (dealer.hasMore()) {
						router.sendMore(message);
					}
					else {
						router.send(message, 0);
						break;
					}
				}
			}
			
			if (canceled.get()) {
				break;
			}
		}
	}

	public void terminate() {
		context.destroySocket(router);
		context.destroySocket(dealer);

		// Release the responder port.
		This.getCom().releasePort(responderPort);
	}

}