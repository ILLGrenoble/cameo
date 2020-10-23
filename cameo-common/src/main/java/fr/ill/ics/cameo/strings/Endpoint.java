package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.BadFormatException;

public class Endpoint {

	private String address;
	private int port;
	
	public Endpoint(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public static Endpoint parse(String string) {
		
		String[] tokens = string.split(":");
		
		if (tokens.length != 2) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		String address = tokens[0];
		int port = 0;
		
		try {
			port = Integer.parseInt(tokens[1]);
		}
		catch (NumberFormatException e) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		return new Endpoint(address, port);
	}

	@Override
	public String toString() {
		return address + ":" + port;
	}
	
}
