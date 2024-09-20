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

/**
 * Class defining an output print thread.
 */
public class OutputPrintThread extends Thread {

	private OutputStreamSocket socket;

	/**
	 * Constructor.
	 * @param socket The socket.
	 */
	public OutputPrintThread(OutputStreamSocket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		try {
			while (true) {
				OutputStreamSocket.Output stream = socket.receive();
				
				if (stream == null) {
					// The stream is finished.
					return;
				}
				
				if (stream.isEndOfLine()) {
					System.out.println(stream.getMessage());
				}
				else {
					System.out.print(stream.getMessage());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			// destroy the socket, because it is no longer needed
			socket.terminate();
		}
	}
	
	/**
	 * Waits for the end of the thread.
	 */
	public void waitFor() {
		try {
			this.join();
		}
		catch (InterruptedException e) {
			// do nothing
		}
	}
	
}