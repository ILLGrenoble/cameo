package fr.ill.ics.cameo.strings;

import java.nio.ByteBuffer;

public class StringId {

	public final static String CAMEO_SERVER = "0:0";
	
	public static String from(String name, int id) {
		return name + ":" + id;
	}
	
	public static String from(String name1, String name2) {
		return name1 + ":" + name2;
	}
	
	public static long toInt(byte[] identity) {
		
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(identity);
		buffer.rewind();
		long value = buffer.getLong();
		
		return value;
	}
}
