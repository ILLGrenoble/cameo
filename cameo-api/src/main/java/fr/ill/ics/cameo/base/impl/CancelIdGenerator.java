package fr.ill.ics.cameo.base.impl;

public class CancelIdGenerator {

	private static int id = 0;
	
	public static synchronized int newId() {
		id++;
		return id;
	}
}