/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.manager;

import java.util.Locale;

public class OperatingSystem {
	
	public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };

    private static OS os;
    
	static {
		String osString = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
		if (osString.contains("win")) {
			os = OS.WINDOWS;
		}
		else if (osString.contains("mac")) {
			os = OS.MAC;
		}
		else if (osString.contains("nix") || osString.contains("nux") || osString.contains("aix")) {
			os = OS.LINUX;
		}
		else if (osString.contains("sunos")) {
			os = OS.SOLARIS;
		}
	}
	
	public static OS get() {
		return os;
	}
}