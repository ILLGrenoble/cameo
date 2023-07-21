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

import java.text.ParseException;

import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.messages.JSON;
import jakarta.json.Json;
import jakarta.json.JsonObject;


public class TestStorage {

	public static void main(String[] args) {
		
		This.init(args);

		String key = "fr.ill.ics.cameo.test.testkey";
		
		JsonObject valueObject = Json.createObjectBuilder()
										.add("x", 12)
										.build();
		
		try {
			This.getCom().getKeyValue(key);
		}
		catch (UndefinedKeyException e) {
			System.out.println("Key is undefined: " + e.getMessage());
		}
		
		try {
			This.getCom().storeKeyValue(key, valueObject.toString());
		}
		catch (KeyAlreadyExistsException e) {
		}
		
		try {
			This.getCom().storeKeyValue(key, valueObject.toString());
		}
		catch (KeyAlreadyExistsException e) {
			System.out.println("Key already exists: " + e.getMessage());
		}
		
		try {
			String valueString = This.getCom().getKeyValue(key);
			valueObject = JSON.parse(valueString);
			
			System.out.println("x = " + JSON.getInt(valueObject, "x"));
			
			This.getCom().removeKey(key);
		}
		catch (UndefinedKeyException e) {
		}
		catch (ParseException e) {
		}
		
		try {
			This.getCom().getKeyValue(key);
		}
		catch (UndefinedKeyException e) {
			System.out.println("Cannot remove key: " + e.getMessage());
		}

		System.out.println("Finished the application");
		
		This.terminate();
	}

}