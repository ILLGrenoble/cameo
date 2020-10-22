package fr.ill.ics.cameo.strings;

public class Name {

	public static boolean check(String string) {
		return string.matches("[a-zA-Z0-9\\-_]+");
	}
}
