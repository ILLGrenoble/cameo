package fr.ill.ics.cameo.coms;

import java.util.List;

import fr.ill.ics.cameo.base.Application.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.coms.impl.RequestImpl;

/**
 * Class Request.
 * 
 */
public class Request {
	
	private RequestImpl impl;
	private Server requesterServer = null;
	
	Request(RequestImpl impl) {
		this.impl = impl;
	}
	
	public byte[] getBinary() {
		return impl.get();
	}
	
	public String get() {
		return impl.getString();
	}
	
	public byte[][] getTwoBinaryParts() {
		
		byte[][] result = new byte[2][];
		result[0] = impl.get();
		result[1] = impl.get2();
		
		return result;
	}
	
	public void reply(byte[] response) {
		impl.reply(response);
	}
	
	public void reply(String response) {
		impl.reply(response);
	}
	
	public Instance connectToRequester() {
		
		// Instantiate the requester server if it is null.
		if (requesterServer == null) {
			requesterServer = new Server(impl.getRequesterServerEndpoint());
		}	
		
		// Connect and find the instance.
		List<Instance> instances = requesterServer.connectAll(impl.getRequesterApplicationName());
		
		for (Instance instance : instances) {
			if (instance.getId() == impl.getRequesterApplicationId()) {
				return instance;
			}
		}
		
		// Not found.
		return null;
	}
	
	/**
	 * Gets the requester server and transfers the ownership. The client code is responsible to terminate the server.
	 * @return
	 */
	public Server getServer() {
		
		// Transfers the ownership of the server.
		Server result = requesterServer;
		requesterServer = null;
		
		return result;
	}
	
	public void terminate() {
		
		if (requesterServer != null) {
			requesterServer.terminate();
		}
	}
	
	@Override
	public String toString() {
		return impl.toString();
	}
}