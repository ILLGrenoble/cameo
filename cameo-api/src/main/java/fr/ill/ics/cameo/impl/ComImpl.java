package fr.ill.ics.cameo.impl;

import fr.ill.ics.cameo.UndefinedApplicationException;
import fr.ill.ics.cameo.UndefinedKeyException;

public class ComImpl {

	private ServerImpl server;
	private int applicationId;
	
	public ComImpl(ServerImpl server, int applicationId) {
		this.server = server;
		this.applicationId = applicationId;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void storeKeyValue(String key, String value) {
		server.storeKeyValue(applicationId, key, value);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 * @throws UndefinedApplicationException
	 * @throws UndefinedKeyException
	 */
	public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
		return server.getKeyValue(applicationId, key);
	}
	
	/**
	 * 
	 * @param key
	 * @throws UndefinedApplicationException
	 * @throws UndefinedKeyException
	 */
	public void removeKey(String key) throws UndefinedApplicationException, UndefinedKeyException {
		server.removeKey(applicationId, key);
	}
	
	public int requestPort() throws UndefinedApplicationException {
		return server.requestPort(applicationId);
	}
	
	public void setPortUnavailable(int port) throws UndefinedApplicationException {
		server.setPortUnavailable(applicationId, port);
	}
	
	public void releasePort(int port) throws UndefinedApplicationException {
		server.releasePort(applicationId, port);
	}
	
}
