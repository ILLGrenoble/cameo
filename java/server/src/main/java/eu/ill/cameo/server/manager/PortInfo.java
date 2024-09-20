package fr.ill.ics.cameo.server.manager;

public class PortInfo {

	private int port;
	private String status;
	private String applicationNameId;
	
	public PortInfo(int port, String status, String applicationNameId) {
		super();
		this.port = port;
		this.status = status;
		this.applicationNameId = applicationNameId;
	}

	public int getPort() {
		return port;
	}

	public String getStatus() {
		return status;
	}

	public String getApplicationNameId() {
		return applicationNameId;
	}
	
	
}
