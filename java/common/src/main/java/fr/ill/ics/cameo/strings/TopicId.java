package fr.ill.ics.cameo.strings;

public class TopicId {
	
	public static byte[] from(byte[] id, String name) {
		byte[] nameBytes = name.getBytes();
		byte[] result = new byte[id.length + nameBytes.length];
				
		System.arraycopy(id, 0, result, 0, id.length);
		System.arraycopy(nameBytes, 0, result, id.length, nameBytes.length);
		
		return result;
	}
	
}
