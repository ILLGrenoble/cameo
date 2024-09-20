package eu.ill.cameo.api.base;

/**
 * Interface defining an a cancelable object.
 */
public interface ICancelable {
	/**
	 * Cancels the object.
	 */
	void cancel();

	/**
	 * Returns true if is canceled.
	 * \return True if is canceled.
	 */
	boolean isCanceled();
}
