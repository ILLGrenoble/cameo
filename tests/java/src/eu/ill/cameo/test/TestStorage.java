/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.test;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import eu.ill.cameo.api.base.KeyAlreadyExistsException;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.UndefinedKeyException;
import eu.ill.cameo.common.messages.JSON;


public class TestStorage {

	public static void main(String[] args) {
		
		This.init(args);

		String key = "eu.ill.cameo.test.testkey";
		
		JSONObject valueObject = new JSONObject();
		valueObject.put("x", 12);
		
		try {
			This.getCom().getKeyValue(key);
		}
		catch (UndefinedKeyException e) {
			System.out.println("Key is undefined: " + e.getMessage());
		}
		
		try {
			This.getCom().storeKeyValue(key, valueObject.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
		}
		
		try {
			This.getCom().storeKeyValue(key, valueObject.toJSONString());
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