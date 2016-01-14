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

package fr.ill.ics.cameo.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;



public class ProcessErrorApplication {

	public static void write(String content, File file) {

		try {
			file.createNewFile();
			
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.println(content);
			writer.close();
			
		} catch (IOException e) {
			System.err.println("Cannot create file " + file);
		}
	}
	
	/**
	 * args[0] = application id
	 * args[1] = error code
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("error " + args[1] + " for application " + args[0]);
	}

}