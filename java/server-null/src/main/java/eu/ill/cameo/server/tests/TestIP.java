package eu.ill.cameo.server.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Test to verify the value returned by InetAddress.getLocalHost() which is sometimes 127.0.0.1 in some context.
 */
public class TestIP {

	public static void main(String[] args) {
		try {
			String host = InetAddress.getLocalHost().getHostAddress();
			
			System.out.println("IP = " + host);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
