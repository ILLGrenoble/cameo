package fr.ill.ics.cameo.base;

public class ServerAndInstance {

	private Server server;
	private Instance instance;
	
	public ServerAndInstance(Server server, Instance instance) {
		this.server = server;
		this.instance = instance;
	}
	
	public void terminate() {
		instance.terminate();
		server.terminate();
	}
	
	public Server getServer() {
		return server;
	}
	public Instance getInstance() {
		return instance;
	}
	
	
}
