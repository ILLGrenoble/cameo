package eu.ill.cameo.server.tests;

import eu.ill.cameo.server.manager.IPAddress;

public class TestIP4 {
	
	public static void main(String[] args) {
		try {
			String host = IPAddress.getHost4Address();
			
			System.out.println("IP = " + host);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
