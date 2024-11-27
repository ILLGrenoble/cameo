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

import eu.ill.cameo.api.base.KeyAlreadyExistsException;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.UndefinedKeyException;


public class WaitStorage {

	public static void main(String[] args) {
		
		This.init(args);
		String key = "eu.ill.cameo.test.testkey";
		
		JSONObject valueObject = new JSONObject();
		valueObject.put("x", 12);

		// Sleep 100ms.
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
		}
	
		try {
			This.getCom().storeKeyValue(key, valueObject.toJSONString());
		}
		catch (KeyAlreadyExistsException e1) {
		}
		
		// Sleep 100ms.
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
		}

		try {
			This.getCom().removeKey(key);
		}
		catch (UndefinedKeyException e) {
		}

		System.out.println("Finished the application");
		
		This.terminate();
	}

}