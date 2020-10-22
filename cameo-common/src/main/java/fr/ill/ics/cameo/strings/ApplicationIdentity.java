package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.BadFormatException;

public class ApplicationIdentity {

	private NameId nameId;
	private Endpoint endpoint;
	
	public ApplicationIdentity(NameId nameId, Endpoint endpoint) {
		super();
		this.nameId = nameId;
		this.endpoint = endpoint;
	}
	
	public NameId getNameId() {
		return nameId;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public static ApplicationIdentity parse(String string) {
		
		String[] tokens = string.split("@");
		
		if (tokens.length != 2) {
			throw new BadFormatException("Bad format for application identity " + string);
		}
		
		return new ApplicationIdentity(NameId.parse(tokens[0]), Endpoint.parse(tokens[1]));
	}

	@Override
	public String toString() {
		return nameId + "@" + endpoint;
	}
	
}
