package eu.ill.cameo.server.manager;

public class StatusInfo {

	private int id;
	private String name;
	private int applicationState;
	private int pastApplicationStates;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getApplicationState() {
		return applicationState;
	}
	public void setApplicationState(int applicationState) {
		this.applicationState = applicationState;
	}
	public int getPastApplicationStates() {
		return pastApplicationStates;
	}
	public void setPastApplicationStates(int pastApplicationStates) {
		this.pastApplicationStates = pastApplicationStates;
	}
	
	
}
