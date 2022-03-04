package fr.ill.ics.cameo.base;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

	private static AtomicInteger id = new AtomicInteger(0);
	
	public static int newId() {
		int newId = id.get() + 1;
		id.set(newId);
		
		return newId;
	}
	
	public static String newStringId() {
		return "cameo." + newId();
	}
}
