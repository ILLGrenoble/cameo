package fr.ill.ics.cameo.base;
/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */



import java.util.ArrayList;
import java.util.List;

import fr.ill.ics.cameo.base.impl.InstanceImpl;
import fr.ill.ics.cameo.base.impl.ServerImpl;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * The server class is thread-safe except for the connect and terminate methods that must be called respectively 
 * before and after any concurrent calls.
 * @author legoc
 *
 */
public class Server {

	private ServerImpl impl;
	
	Server(ServerImpl impl) {
		this.impl = impl;
	}
	
	public Server(Endpoint endpoint) {
		impl = new ServerImpl(endpoint, 0);
	}
	
	/**
	 * Constructor with endpoint.
	 * This constructor must be used when the services are related to another cameo server that
	 * has not started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set. 
	 * @param endpoint
	 * throws SocketException.
	 */
	public Server(String endpoint) {
		impl = new ServerImpl(endpoint, 0);
	}

	public Server(Endpoint endpoint, int timeout) {
		impl = new ServerImpl(endpoint, timeout);
	}
	
	/**
	 * Constructor with endpoint and timeout.
	 * This constructor must be used when the services are related to another cameo server that
	 * has not started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set. 
	 * @param endpoint
	 * throws SocketException.
	 */
	public Server(String endpoint, int timeout) {
		impl = new ServerImpl(endpoint, timeout);
	}
	
	public int getTimeout() {
		return impl.getTimeout();
	}

	public void setTimeout(int timeout) {
		impl.setTimeout(timeout);
	}
	
	public Endpoint getEndpoint() {
		return impl.getEndpoint();
	}
			
	public int[] getVersion() {
		return impl.getVersion();
	}
	
	private int getAvailableTimeout() {
		int timeout = getTimeout();
		if (timeout > 0) {
			return timeout;
		}
		
		return 10000;
	}
	
	/**
	 * Connects to the server. Returns false if there is no connection.
	 * It must be called to initialize the receiving status.
	 */
	public boolean isAvailable(int timeout) {
		return impl.isAvailable(timeout);
	}
	
	/**
	 * Connects to the server. Returns false if there is no connection.
	 * It must be called to initialize the receiving status.
	 */
	public boolean isAvailable() {
		return impl.isAvailable(getAvailableTimeout());
	}

	public void terminate() {
		impl.terminate();
	}
	
	public void registerEventListener(EventListener listener) {
		impl.registerEventListener(listener);
	}
	
	public void unregisterEventListener(EventListener listener) {
		impl.unregisterEventListener(listener);
	}
		
	/**
	 * Sends start request with parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public Application.Instance start(String name, String[] args, int options) {
		return new Application.Instance(impl.start(name, args, options));
	}
	
	public Application.Instance start(String name, String[] args) {
		return new Application.Instance(impl.start(name, args, 0));
	}
	
	/**
	 * Sends start request without parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public Application.Instance start(String name, int options) {
		return new Application.Instance(impl.start(name, options));
	}
	
	public Application.Instance start(String name) {
		return new Application.Instance(impl.start(name, 0));
	}
		
	public void killAllAndWaitFor(String name) {
		impl.killAllAndWaitFor(name);	
	}
	
	/**
	 * 
	 * @return List of Instance, null if a connection timeout occurs
	 * @throws ConnectionTimeout
	 */
	public List<Application.Instance> connectAll(String name, int options) {
		return createInstances(impl.connectAll(name, options));
	}
	
	/**
	 * 
	 * @return List of Instance, null if a connection timeout occurs
	 * @throws ConnectionTimeout
	 */
	public List<Application.Instance> connectAll(String name) {
		return createInstances(impl.connectAll(name, Option.NONE));
	}
	
	/**
	 * 
	 * @return Returns the first application with name.
	 * @throws ConnectionTimeout
	 */
	public Application.Instance connect(String name, int options) {
		return new Application.Instance(impl.connect(name, options));
	}
	
	/**
	 * 
	 * @return Returns the first application with name.
	 * @throws ConnectionTimeout
	 */
	public Application.Instance connect(String name) {
		return new Application.Instance(impl.connect(name, Option.NONE));
	}
	
	/**
	 * 
	 * @return Returns the application with id.
	 * @throws ConnectionTimeout
	 */
	public Application.Instance connect(int id, int options) {
		return new Application.Instance(impl.connect(id, options));
	}
	
	/**
	 * 
	 * @return Returns the application with id.
	 * @throws ConnectionTimeout
	 */
	public Application.Instance connect(int id) {
		return new Application.Instance(impl.connect(id, Option.NONE));
	}
	
	/**
	 * 
	 * @return List of ApplicationConfig if everything is ok, else null
	 * @throws ConnectionTimeout
	 */
	public List<Application.Configuration> getApplicationConfigurations() {
		return impl.getApplicationConfigurations();
	}
	
	/**
	 * 
	 * @return List of ApplicationInfoForClient if everything is ok, else null
	 * @throws ConnectionTimeout 
	 */
	public List<Application.Info> getApplicationInfos() {
		return impl.getApplicationInfos();
	}
	
	/**
	 * 
	 * @param name
	 * @return the of application info of the applications with name
	 * @throws ConnectionTimeout 
	 */
	public List<Application.Info> getApplicationInfos(String name) {
		return impl.getApplicationInfos(name);		
	}
	
	public List<Application.Port> getPorts() {
		return impl.getPorts();
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public int getActualState(int id) {
		return impl.getActualState(id);
	}
				
	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param inputs
	 * @return null, if reply is null, else Response
	 * @throws WriteException 
	 * @throws ConnectionTimeout 
	 */
	public void writeToInputStream(int id, String[] inputs) throws WriteException {
		impl.writeToInputStream(id, inputs);
	}
	
	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param parametersArray
	 * @return null, if reply is null, else Response
	 * @throws WriteException 
	 * @throws ConnectionTimeout 
	 */
	public void writeToInputStream(int id, String inputs) throws WriteException {
		impl.writeToInputStream(id, inputs);
	}
	
	/**
	 * Creates a connection checker.
	 * @param handler
	 * @return
	 */
	public ConnectionChecker createConnectionChecker(ConnectionChecker.Handler handler) {
		
		ConnectionChecker connectionChecker = new ConnectionChecker(impl, handler);
		connectionChecker.start(getAvailableTimeout(), 10000);
		
		return connectionChecker;
	}
	
	/**
	 * Creates a connection checker.
	 * @param handler
	 * @param pollingTimeMs
	 * @return
	 */
	public ConnectionChecker createConnectionChecker(ConnectionChecker.Handler handler, int pollingTimeMs) {
		
		ConnectionChecker connectionChecker = new ConnectionChecker(impl, handler);
		connectionChecker.start(getAvailableTimeout(), pollingTimeMs);
		
		return connectionChecker;
	}
		
	@Override
	public String toString() {
		return impl.toString();
	}
	
	private List<Application.Instance> createInstances(List<InstanceImpl> instances) {
		List<Application.Instance> result = new ArrayList<Application.Instance>();
		for (InstanceImpl a : instances) {
			result.add(new Application.Instance(a));
		}
		return result;
	}
		
}