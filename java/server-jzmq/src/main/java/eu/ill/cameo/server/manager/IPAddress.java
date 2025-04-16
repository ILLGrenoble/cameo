package eu.ill.cameo.server.manager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Class providing an iteration over the network interfaces to get the IP address which is not a loopback address.
 * Indeed the call to InetAddress.getLocalHost() returned a loopback address in some context whereas it should not.
 */
public class IPAddress {

	private static List<Inet4Address> getInet4Addresses() throws SocketException {
		List<Inet4Address> ret = new ArrayList<Inet4Address>();

		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
					ret.add((Inet4Address) inetAddress);
				}
			}
		}

		return ret;
	}

	/**
	 * Returns this host's first non-loopback IPv4 address string in textual
	 * representation.
	 * 
	 * @return The IP address
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static String getHost4Address() throws SocketException, UnknownHostException {
		List<Inet4Address> inet4 = getInet4Addresses();
		return !inet4.isEmpty() ? inet4.get(0).getHostAddress() : InetAddress.getLocalHost().getHostAddress();
	}
}
