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