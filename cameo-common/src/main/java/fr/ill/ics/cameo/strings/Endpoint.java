package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.BadFormatException;

public class Endpoint {

	private String hostname;
	private int port;
	
	public Endpoint(String hostname, int port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}
	
	public String getHostname() {
		return hostname;
	}
	public int getPort() {
		return port;
	}
	
	public static Endpoint parse(String string) {
		
		if (!string.startsWith("tcp://")) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		String substring = string.substring(6);
		String[] tokens = substring.split(":");
		
		if (tokens.length != 2) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		String hostname = tokens[0];
		int port = 0;
		
		try {
			port = Integer.parseInt(tokens[1]);
		}
		catch (NumberFormatException e) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		return new Endpoint(hostname, port);
	}

	@Override
	public String toString() {
		return "tcp://" + hostname + ":" + port;
	}
	
}
