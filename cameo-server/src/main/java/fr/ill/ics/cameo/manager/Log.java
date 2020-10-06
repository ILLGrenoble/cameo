/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.manager;

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

	public static void init() {

		instance.logger.setLevel(Level.FINE);
		
		try {
			File logDirectory = new File(ConfigManager.getInstance().getLogPath());

			// If the directory does not exist, create it
			if (!logDirectory.exists()) {
				logDirectory.mkdir();
			}
						
			FileHandler fileHandler = new FileHandler(ConfigManager.getInstance().getLogPath() + "/cameo.log", false);
			fileHandler.setFormatter(new LogFormatter());
			fileHandler.setLevel(Level.INFO);
			instance.logger.addHandler(fileHandler);
			
			if (ConfigManager.getInstance().isDebugMode()) {
				ConsoleHandler consoleHandler = new ConsoleHandler();
				consoleHandler.setFormatter(new DebugFormatter());
				consoleHandler.setLevel(Level.FINE);
				instance.logger.addHandler(consoleHandler);
			}

			// disable terminal output
			instance.logger.setUseParentHandlers(false);

			// first log
			instance.logger.fine("Logs written to " + ConfigManager.getInstance().getLogPath() + "/cameo.log");
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Logger logger() {
		return instance.logger;
	}

}