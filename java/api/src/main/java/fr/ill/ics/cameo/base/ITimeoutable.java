package fr.ill.ics.cameo.base;

/**
 * Interface defining an interface for objects that have timeout.
 */
public interface ITimeoutable {

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
