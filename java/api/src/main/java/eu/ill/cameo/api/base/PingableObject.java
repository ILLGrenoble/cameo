package eu.ill.cameo.api.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interface defining an abstract Cameo object that can be initialized and terminated and which is pingable.
 */
public abstract class PingableObject extends StateObject implements IPingable {
	
	private AtomicBoolean enabled = new AtomicBoolean(true);
	
	/**
	 * Initializes the object.
	 */
	@Override
	public void init() {
		if (This.instance != null) {
			This.instance.getPingableSet().add(this);
		}
	}
	
	/**
	 * Terminates the object.
	 */
	@Override
	public void terminate() {
		if (This.instance != null) {
			This.instance.getPingableSet().remove(this);
		}
	}
	
	/**
	 * Enables the pingable object.
	 * @param value True if pinged.
	 */
	@Override
	public void setPinged(boolean value) {
		enabled.set(value);
	}
	
	/**
	 * Returns true if is enabled.
	 * @return true if is enabled.
	 */
	@Override
	public boolean isPinged() {
		return enabled.get();
	}

}