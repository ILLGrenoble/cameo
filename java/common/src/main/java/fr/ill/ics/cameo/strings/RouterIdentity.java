package fr.ill.ics.cameo.strings;

public class RouterIdentity {

	public String from(int applicationId, String name) {
		return applicationId + ":" + name;
	}
}
