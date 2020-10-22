package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.BadFormatException;

public class NameId {

	private String name;
	private Integer id;
	
	public NameId(String name, Integer id) {
		super();
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public Integer getId() {
		return id;
	}
	
	public static NameId parse(String string) {
		
		String[] tokens = string.split("\\.");
		
		if (tokens.length > 2) {
			throw new BadFormatException("Bad format for nameid " + string);
		}
		
		String name = tokens[0];
		Integer id = null;
		
		if (tokens.length == 2) {
			try {
				id = Integer.parseInt(tokens[1]);
			}
			catch (NumberFormatException e) {
				throw new BadFormatException("Bad format for nameid " + string);
			}
		}
		
		return new NameId(name, id);
	}

	@Override
	public String toString() {
		
		if (id != null) {
			return name + "." + id;
		}
		return name;
	}
}
