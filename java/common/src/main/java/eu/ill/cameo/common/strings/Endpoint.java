/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.common.strings;

import eu.ill.cameo.common.BadFormatException;

public class Endpoint {

	private String protocol = "tcp";
	private String address;
	private int port;
	
	public Endpoint(String protocol, String address, int port) {
		super();
		this.protocol = protocol;
		this.address = address;
		this.port = port;
	}
	
	public Endpoint(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof Endpoint)) {
			return false;
		}
		Endpoint other = (Endpoint)object;
		
		if (protocol == null && other.getProtocol() != null) {
			return false;
		}
		if (!protocol.equals(other.getProtocol())) {
			return false;
		}
		
		if (address == null && other.getAddress() != null) {
			return false;
		}
		if (!address.equals(other.getAddress())) {
			return false;
		}
		
		if (port != other.getPort()) {
			return false;
		}
		
		return true;
	}
		
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public static Endpoint parse(String string) {
		
		String[] tokens = string.split(":");
		
		if (tokens.length != 3) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		String protocol = tokens[0];
		String substring = tokens[1];
		String address;
		
		try {
			address = substring.substring(2);
		}
		catch (IndexOutOfBoundsException e) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		if (!substring.startsWith("//")) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		if (address.length() == 0) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		int port = 0;
		
		try {
			port = Integer.parseInt(tokens[2]);
		}
		catch (NumberFormatException e) {
			throw new BadFormatException("Bad format for endpoint " + string);
		}
		
		return new Endpoint(protocol, address, port);
	}
	
	/**
	 * Returns a new Endpoint instance with a different port.
	 * @param port
	 * @return
	 */
	public Endpoint withPort(int port) {
		return new Endpoint(protocol, address, port);
	}

	@Override
	public String toString() {
		return protocol + "://" + address + ":" + port;
	}
	
}