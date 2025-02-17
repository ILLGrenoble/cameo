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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class Log {

	private final Logger logger = Logger.getLogger("cameo.log");
	private static boolean logConsole = false;
	private final static Log instance = new Log();

	private static class DebugFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			return "LOG   : " + record.getMessage() + "\n";
		}
	}
	
	private static class LogFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			Date date = new Date();
			return date.toString() + " " + record.getLevel() + ": " + record.getMessage() + "\n";
		}
	}
	
	private Log() {
	}

	public static void enableLogConsole() {
		logConsole  = true;
	}
	
	public static void init() {
		
		try {
			File logDirectory = new File(ConfigManager.getInstance().getLogPath());

			// If the directory does not exist, create it
			if (!logDirectory.exists()) {
				logDirectory.mkdir();
			}
			
			Level logLevel = Level.parse(ConfigManager.getInstance().getLogLevel().toUpperCase());
			instance.logger.setLevel(logLevel);
			
			FileHandler fileHandler = new FileHandler(ConfigManager.getInstance().getLogPath() + "/cameo.log", false);
			fileHandler.setFormatter(new LogFormatter());
			fileHandler.setLevel(logLevel);
			
			instance.logger.addHandler(fileHandler);
			
			if (logConsole) {
				ConsoleHandler consoleHandler = new ConsoleHandler();
				consoleHandler.setFormatter(new DebugFormatter());
				consoleHandler.setLevel(logLevel);
				instance.logger.addHandler(consoleHandler);
			}

			// disable terminal output
			instance.logger.setUseParentHandlers(false);

			// first log
			instance.logger.fine("Logs written to " + ConfigManager.getInstance().getLogPath() + "/cameo.log");
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Logger logger() {
		return instance.logger;
	}

}