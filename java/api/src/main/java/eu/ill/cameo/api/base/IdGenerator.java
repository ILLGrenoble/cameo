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


package eu.ill.cameo.api.base;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class managing the ids of communication objects.
 */
public class IdGenerator {

	private static AtomicInteger id = new AtomicInteger(0);

	/**
	 * Generates a new id.
	 * @return An id.
	 */
	public static int newId() {
		return id.incrementAndGet();
	}
	
	/**
	 * Generates a new string id of the form "cameo.<id>". For example "cameo.15".
	 * @return A string id.
	 */
	public static String newStringId() {
		return "cameo." + newId();
	}
}
