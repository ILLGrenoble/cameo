package fr.ill.ics.cameo.base;

/**
 * Interface defining an abstract Cameo object that can be initialized and terminated.
 */
public interface IObject {
	/**
	 * Initializes the object.
	 */
	void init();

	/**
	 * Terminates the object.
	 */
	void terminate();
}
