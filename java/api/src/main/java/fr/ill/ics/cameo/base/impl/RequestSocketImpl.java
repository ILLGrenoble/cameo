package fr.ill.ics.cameo.base.impl;

public interface RequestSocketImpl {

	void setTimeout(int timeout);
	void connect(String endpoint, String responderIdentity);
	
	byte[][] request(byte[] part1, int overrideTimeout);
	byte[][] request(byte[] part1, byte[] part2, int overrideTimeout);
	byte[][] request(byte[] part1, byte[] part2, byte[] part3, int overrideTimeout);
	
	void terminate();
}
