/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.manager;


public class ApplicationInfo {

	private int id;
	private int applicationState;
	private int pastApplicationStates;
	private long pid;
	private String args;
	private boolean hasToStop = false;
	private boolean showStream = false;
	private boolean writeStream = false;
	private boolean single;
	private boolean restart;
	private boolean passInfo;
	private String name;
	private String exec;
	private int startingTime;
	private String outputPath;
	private int stopTimeout;
	private String stopCommand;

	public ApplicationInfo(int id, long pid, int applicationState, int pastApplicationStates,
			String args, boolean hasToStop,
			boolean showStream, boolean writeStream, boolean single,
			boolean restart, boolean passInfo,
			String name, String exec, int startingTime,
			String outputPath, int stopTimeout, String stopCommand) {
		super();
		this.id = id;
		this.pid = pid;
		this.applicationState = applicationState;
		this.pastApplicationStates = pastApplicationStates;
		this.args = args;
		this.hasToStop = hasToStop;
		this.showStream = showStream;
		this.writeStream = writeStream;
		this.single = single;
		this.restart = restart;
		this.passInfo = passInfo;
		this.name = name;
		this.exec = exec;
		this.startingTime = startingTime;
		this.outputPath = outputPath;
		this.stopTimeout = stopTimeout;
		this.stopCommand = stopCommand;
	}

	public int getId() {
		return id;
	}

	public int getApplicationState() {
		return applicationState;
	}
	
	public int getPastApplicationStates() {
		return pastApplicationStates;
	}

	public long getPid() {
		return pid;
	}
	
	public String getArgs() {
		return args;
	}

	public boolean hasToStop() {
		return hasToStop;
	}

	public boolean isShowStream() {
		return showStream;
	}

	public boolean isWriteStream() {
		return writeStream;
	}

	public boolean isSingle() {
		return single;
	}

	public boolean isRestart() {
		return restart;
	}

	public boolean isPassInfo() {
		return passInfo;
	}

	public String getName() {
		return name;
	}

	public String getExec() {
		return exec;
	}

	public int getStartingTime() {
		return startingTime;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public int getStopTimeout() {
		return stopTimeout;
	}
	
	public String getStopCommand() {
		return stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	@Override
	public String toString() {
		return "ApplicationInfo [id=" + id + ", applicationState=" + applicationState + ", args=" + args + ", hasToStop=" + hasToStop + ", showStream=" + showStream + ", writeStream=" + writeStream + ", single=" + single + ", restart=" + restart + ", passInfo=" + passInfo + ", name=" + name + ", exec=" + exec + ", startingTime=" + startingTime + ", outputPath=" + outputPath + ", stopTimeout=" + stopTimeout + ", stopCommand=" + stopCommand + "]";
	}

}