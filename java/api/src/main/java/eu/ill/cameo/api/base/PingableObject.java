package eu.ill.cameo.api.base;

/**
 * Interface defining an abstract Cameo object that can be initialized and terminated and which is pingable.
 */
public abstract class PingableObject extends StateObject implements IPingable {
	
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
	
}