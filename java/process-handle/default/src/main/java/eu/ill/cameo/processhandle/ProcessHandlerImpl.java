/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.processhandle;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ProcessHandlerImpl {

	private ProcessHandle processHandle;
	
	public ProcessHandlerImpl(ProcessHandle handle) {
		processHandle = handle;
	}
	
	public ProcessHandlerImpl(Process process) {
		processHandle = process.toHandle();
	}
	
	static public ProcessHandlerImpl ofPid(long pid) {
		return new ProcessHandlerImpl(ProcessHandle.of(pid).get());
	}
	
	static public long pid() {
		return ProcessHandle.current().pid();
	}
	
	public long getPid() {
		return processHandle.pid();
	}
	
	public boolean hasProcessHandle() {
		return true;
	}
	
	public boolean isAlive() {
		return processHandle.isAlive();
	}
	
	public void destroyForcibly() {
		processHandle.destroyForcibly();
	}
	
	public void waitFor() {
		
		CompletableFuture<ProcessHandle> onProcessExit = processHandle.onExit();
		
		try {
			onProcessExit.get();
		}
		catch (InterruptedException e) {
		}
		catch (ExecutionException e) {
		}
	}
}