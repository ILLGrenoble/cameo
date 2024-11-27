/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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