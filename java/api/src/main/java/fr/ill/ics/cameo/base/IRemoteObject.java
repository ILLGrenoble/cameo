package fr.ill.ics.cameo.base;

/**
 * Interface defining an abstract Cameo remote object that has timeout.
 */
public interface IRemoteObject extends IObject {

	/**
	 * Sets the timeout.
	 * @param value The timeout.
	 */
	void setTimeout(int value);

	/**
	 * Gets the timeout.
	 * @return The timeout.
	 */
	int getTimeout();
}
