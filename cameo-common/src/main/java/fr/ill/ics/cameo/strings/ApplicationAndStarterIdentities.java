package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.BadFormatException;

public class ApplicationAndStarterIdentities {

	private ApplicationIdentity application;
	private ApplicationIdentity starter;
	
	public ApplicationAndStarterIdentities(ApplicationIdentity application, ApplicationIdentity starter) {
		super();
		this.application = application;
		this.starter = starter;
	}

	public ApplicationIdentity getApplication() {
		return application;
	}

	public ApplicationIdentity getStarter() {
		return starter;
	}

	public static ApplicationAndStarterIdentities parse(String string) {
		
		// The string is either <name>@<endpoint>:<name>@<endpoint> or <name>@<endpoint>:
		// To separate the two identities, we search for the last : before the last @.
		
		int firstIndex = string.indexOf('@');
		
		if (firstIndex == -1) {
			throw new BadFormatException("Bad format for application and starter identities " + string);
		}
		
		int index = string.lastIndexOf('@');
		
		if (index == firstIndex) {

			// Format <name>@<endpoint>:
			if (string.charAt(string.length() - 1) != ':') {
				throw new BadFormatException("Bad format for application and starter identities " + string);
			}
			
			String applicationString = string.substring(0, string.length() - 1);
			
			ApplicationIdentity application = ApplicationIdentity.parse(applicationString);
			
			return new ApplicationAndStarterIdentities(application, null);
		}
		else {
			// Format <name>@<endpoint>:<name>@<endpoint>
			String substring = string.substring(0, index);
			index = substring.lastIndexOf(':');
			
			if (index == -1) {
				throw new BadFormatException("Bad format for application and starter identities " + string);
			}
			
			String applicationString = string.substring(0, index);
			String starterString = string.substring(index + 1, string.length());
	
			ApplicationIdentity application = ApplicationIdentity.parse(applicationString);
			ApplicationIdentity starter = ApplicationIdentity.parse(starterString);
			
			return new ApplicationAndStarterIdentities(application, starter);
		}
	}

	@Override
	public String toString() {
		if (starter != null) {
			return application + ":" + starter;
		}
		return application + ":";
	}
	
}
