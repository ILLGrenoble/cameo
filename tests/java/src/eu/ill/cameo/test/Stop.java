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

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.ill.cameo.api.base.This;


public class Stop {

	public static AtomicBoolean stopping = new AtomicBoolean(false);
	
	public static void main(String[] args) {

		This.init(args);

		This.handleStop(() -> {
			System.out.println("Stop handler executed");
			stopping.set(true);
		});
		
		This.setRunning();
		
		try {
			Date begin = new Date();

			int i = 0;
			while (!stopping.get()) {
				System.out.println("Waiting " + i + "...");
				Thread.sleep(100);
				i++;
			}
			
			Date end = new Date();
			
			String result = "";
			result += end.getTime() - begin.getTime();
			
			This.setStringResult(result);
			
			System.out.println("Finished the application");
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			This.terminate();			
		}
	}

}