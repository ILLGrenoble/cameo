package eu.ill.cameo.common.strings;

import eu.ill.cameo.common.BadFormatException;

public class EndpointWithProxy {

	private Endpoint endpoint;
	private int proxyPort = 0;
		
	public EndpointWithProxy(Endpoint endpoint) {
		this.endpoint = endpoint;
		this.proxyPort = 0;
	}
	
	public EndpointWithProxy(Endpoint endpoint, int proxyPort) {
		this.endpoint = endpoint;
		this.proxyPort = proxyPort;
	}
	
	public EndpointWithProxy(String address, int port, int proxyPort) {
		this.endpoint = new Endpoint(address, port);
		this.proxyPort = proxyPort;
	}
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public int getProxyPort() {
		return proxyPort;
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof EndpointWithProxy)) {
			return false;
		}
		EndpointWithProxy other = (EndpointWithProxy)object;
		
		if (endpoint == null && other.getEndpoint() != null) {
			return false;
		}
		
		if (!endpoint.equals(other.getEndpoint())) {
			return false;
		}
		
		if (proxyPort != other.getProxyPort()) {
			return false;
		}
		
		return true;
	}
	
	private static long numberOfSeparatorsNormalized(String string) {
		
		long n = string.chars().filter(ch -> ch == ':').count();
		
		if (string.startsWith("tcp://")) {
			return n;
		}
		
		return n + 1;
	}
	
	public static boolean isNotEndpoint(String string) {
		return numberOfSeparatorsNormalized(string) != 2;
	}
	
	public static boolean isNotEndpointWithProxy(String string) {
		return numberOfSeparatorsNormalized(string) != 3;
	}
	
	public static EndpointWithProxy parse(String string) {
		
		long n = numberOfSeparatorsNormalized(string);
		
		if (n == 2) {
			Endpoint endpoint = Endpoint.parse(string);
			return new EndpointWithProxy(endpoint);
		}
		else if (n == 3) {
			int index = string.lastIndexOf(':');
			
			String endpointString = string.substring(0, index);
			Endpoint endpoint = Endpoint.parse(endpointString);
					
			String portString = string.substring(index + 1);
			int port = 0;
			
			try {
				port = Integer.parseInt(portString);
			}
			catch (NumberFormatException e) {
				throw new BadFormatException("Bad format for endpoint with proxy " + string);
			}
			
			return new EndpointWithProxy(endpoint, port);
		}
		
		throw new BadFormatException("Bad format for endpoint with proxy" + string);
	}
	
	@Override
	public String toString() {
		return endpoint.toString() + ":" + proxyPort;
	}
}
