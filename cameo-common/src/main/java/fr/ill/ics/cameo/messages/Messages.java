package fr.ill.ics.cameo.messages;

import java.nio.charset.Charset;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Definitions for the JSON objects. 
 */
public class Messages {
	
	public static Charset CHARSET = Charset.forName("UTF-8");
	
	public static final String TYPE = "type";
	
	public static final long SYNC = 1;
	public static final long SYNC_STREAM = 2;
	public static final long START = 3;
	public static final long STOP = 4;
	public static final long CONNECT = 5;
	public static final long CONNECT_WITH_ID = 6;
	public static final long APPS = 7;
	public static final long OUTPUT_PORT = 8;
	public static final long OUTPUT_PORT_WITH_ID = 9;
	public static final long IS_ALIVE = 10;
	public static final long WRITE_INPUT = 11;
	public static final long KILL = 12;
	public static final long STATUS = 13;
	public static final long LIST = 14;
	public static final long SET_STATUS = 15;
	public static final long GET_STATUS = 16;
	public static final long CREATE_PUBLISHER_v0 = 17;
	public static final long TERMINATE_PUBLISHER_v0 = 18;
	public static final long CONNECT_PUBLISHER_v0 = 19;
	public static final long SUBSCRIBE_PUBLISHER_v0 = 20;
	public static final long CANCEL = 21;
	public static final long SET_RESULT = 22;
	public static final long REQUEST_PORT_v0 = 23;
	public static final long CONNECT_PORT_v0 = 24;
	public static final long REMOVE_PORT_v0 = 25;
	public static final long REQUEST = 26;
	public static final long RESPONSE = 27;
	public static final long ATTACH_UNMANAGED = 28;
	public static final long DETACH_UNMANAGED = 29;
	public static final long IMPL_VERSION = 30;
	public static final long STORE_KEY_VALUE = 31;
	public static final long GET_KEY_VALUE = 32;
	public static final long REMOVE_KEY = 33;
	public static final long REQUEST_PORT = 34;
	public static final long PORT_UNAVAILABLE = 35;
	public static final long RELEASE_PORT = 36;
	public static final long PORTS = 37;
	public static final long SET_STOP_HANDLER = 38;
				
	public static class Event {
		public static final String SYNC = "SYNC";
		public static final String CANCEL = "CANCEL";
		public static final String STREAM = "STREAM";
		public static final String ENDSTREAM = "ENDSTREAM";
		public static final String SYNCSTREAM = "SYNCSTREAM";
		public static final String STATUS = "STATUS";
		public static final String RESULT = "RESULT";
		public static final String PORT = "PORT";
		public static final String PUBLISHER = "PUBLISHER";
		public static final String KEYVALUE = "KEYVALUE";
	}
	
	public static class ApplicationIdentity {
		public static final String NAME = "name"; // string
		public static final String ID = "id"; // int32
		public static final String SERVER = "server"; // string
		public static final String STARTER = "starter"; // object
	}
	
	public static class SyncStreamRequest {
		public static final String NAME = "name"; // string
	}
	
	public static class StartRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String ARGS = "args"; // repeated string args = 2;
		public static final String STARTER = "starter"; // object
	}
	
	public static class RequestResponse {
		public static final String VALUE = "value"; // required int32 value = 1;
		public static final String MESSAGE = "message"; // optional string message = 2;
	}

	public static class SetStopHandlerRequest {
		public static final String ID = "id"; // int32
		public static final String STOPPING_TIME = "stopping_time"; // int32
	}
	
	public static class StopRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}

	public static class ConnectRequest {
		public static final String NAME = "name"; // required string name = 1;
	}
	
	public static class ConnectWithIdRequest {
		public static final String ID = "id"; // int32
	}

	public static class ApplicationConfig {
		public static final String NAME = "name"; // required string name = 1;
		public static final String DESCRIPTION = "description"; // optional string description = 2;
		public static final String RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		public static final String RESTART = "restart"; // required bool restart = 4;
		public static final String STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		public static final String STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	}

	public static class ApplicationConfigListResponse {
		public static final String APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	}

	public static class StatusEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		public static final String PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		public static final String EXIT_CODE = "exitCode"; // optional
	}

	public static class PublisherEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String PUBLISHER_NAME = "publisherName"; // required string publisherName = 3;
	}

	public static class ResultEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String DATA = "data"; // required bytes data = 3;
	}

	public static class PortEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String PORT_NAME = "portName"; // required string portName = 3;
	}
	
	public static class ApplicationInfo {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		public static final String PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		public static final String ARGS = "args"; // required string args = 5;
		public static final String PID = "pid"; // optional int64 pid = 6;
	}

	public static class ApplicationInfoListResponse {
		public static final String APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo = 1;
	}

	public static class OutputPortWithIdRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}

	public static class OutputRequest {
		public static final String NAME = "name"; // required string name = 1;
	}

	public static class IsAliveRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}

	public static class IsAliveResponse {
		public static final String IS_ALIVE = "isAlive"; // required bool isAlive = 1;
	}

	public static class ApplicationStream {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String MESSAGE = "message"; // required string message = 2;
		public static final String EOL = "eol"; // boolean
	}

	public static class WriteInputRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String PARAMETERS = "parameters"; // repeated string parameters = 2;
	}

	public static class KillRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}

	public static class SetStatusRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String APPLICATION_STATE = "applicationState"; // required int32 applicationState = 2;
	}

	public static class GetStatusRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}

	public static class SetResultRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String DATA = "data"; // required bytes data = 2;
	}

	public static class RequestPortV0Request {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class ConnectPortV0Request {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class RemovePortV0Request {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class CreatePublisherRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // required int32 numberOfSubscribers = 3;
	}

	public static class TerminatePublisherRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class ConnectPublisherRequest {
		public static final String APPLICATION_ID = "applicationId"; // required int32 applicationId = 1;
		public static final String PUBLISHER_NAME = "publisherName"; // required string publisherName = 2;
	}

	public static class PublisherResponse {
		public static final String MESSAGE = "message"; // optional string message = 1;
		public static final String PUBLISHER_PORT = "publisherPort"; // required int32 publisherPort = 2;
		public static final String SYNCHRONIZER_PORT = "synchronizerPort"; // required int32 synchronizerPort = 3;
		public static final String NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // optional int32 numberOfSubscribers = 4;
	}

	public static class Request {
		public static final String APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		public static final String APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		public static final String SERVER_URL = "serverUrl"; // required string serverUrl = 5;
		public static final String SERVER_PORT = "serverPort"; // required int32 serverPort = 6;
		public static final String REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	}

	public static class AttachUnmanagedRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String PID = "pid"; // optional int64 pid = 2;
	}

	public static class DetachUnmanagedRequest {
		public static final String ID = "id"; // required int32 id = 1;
	}
	
	public static class VersionResponse {
		public static final String MAJOR = "major";
		public static final String MINOR = "minor";
		public static final String REVISION = "revision";
	}
	
	public static class StoreKeyValueRequest {
		public static final String ID = "id"; // int32
		public static final String KEY = "key"; // string
		public static final String VALUE = "value"; // string
	}
	
	public static class GetKeyValueRequest {
		public static final String ID = "id"; // int32
		public static final String KEY = "key"; // string
	}
	
	public static class RemoveKeyRequest {
		public static final String ID = "id"; // int32
		public static final String KEY = "key"; // string
	}

	public static class KeyEvent {
		public static final String ID = "id"; // int32
		public static final String NAME = "name"; // string
		public static final String STATUS = "status"; // long STORE_KEY_VALUE or REMOVE_KEY
		public static final String KEY = "key"; // string
		public static final String VALUE = "value"; // string
	}
	
	public static class RequestPortRequest {
		public static final String ID = "id"; // int32
	}
	
	public static class PortUnavailableRequest {
		public static final String ID = "id"; // int32
		public static final String PORT = "port"; // int32
	}
	
	public static class ReleasePortRequest {
		public static final String ID = "id"; // int32
		public static final String PORT = "port"; // int32
	}
	
	public static class PortInfo {
		public static final String PORT = "port"; // int32
		public static final String STATUS = "status"; // string
		public static final String OWNER = "owner"; // string
	}

	public static class PortInfoListResponse {
		public static final String PORT_INFO = "portInfo"; // multiple PortInfo
	}
	
	public static String parseString(byte[] data) {
		return new String(data, CHARSET);
	}
	
	public static byte[] serialize(String string) {
		return string.getBytes(CHARSET);
	}
	
	public static byte[] serialize(JSONObject object) {
		return serialize(object.toJSONString());
	}
	
	public static JSONObject createSyncRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SYNC);
		
		return request;
	}

	public static JSONObject createSyncStreamRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SYNC_STREAM);
		request.put(Messages.SyncStreamRequest.NAME, name);
		
		return request;
	}
	
	public static JSONObject createVersionRequest() {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.IMPL_VERSION);

		return request;
	}
	
	public static JSONObject createStreamStatusRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.STATUS);

		return request;
	}
	
	public static JSONObject createGetStatusRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.GET_STATUS);
		request.put(Messages.GetStatusRequest.ID, id);

		return request;	
	}
	
	public static JSONObject createAttachUnmanagedRequest(String name, long pid) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.ATTACH_UNMANAGED);
		request.put(Messages.AttachUnmanagedRequest.NAME, name);
		request.put(Messages.AttachUnmanagedRequest.PID, pid);
		
		return request;
	}
	
	public static JSONObject createDetachUnmanagedRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.DETACH_UNMANAGED);
		request.put(Messages.DetachUnmanagedRequest.ID, id);
		
		return request;
	}
	
	public static JSONObject createSetStopHandlerRequest(int id, int stoppingTime) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SET_STOP_HANDLER);
		request.put(Messages.SetStopHandlerRequest.ID, id);
		request.put(Messages.SetStopHandlerRequest.STOPPING_TIME, stoppingTime);
		
		return request;
	}
	
	/**
	 * create isAlive request
	 * 
	 * @param text
	 * @return
	 */
	public static JSONObject createIsAliveRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.IS_ALIVE);
		request.put(Messages.IsAliveRequest.ID, id);

		return request;
	}
	
	/**
	 * create start request with parameters
	 * 
	 * @param name
	 * @param args
	 * @param returnResult 
	 * @return request
	 */
	public static JSONObject createStartRequest(String name, String[] args, String thisName, int thisId, String thisEndpoint) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.START);
		request.put(Messages.StartRequest.NAME, name);
		
		// Add the starter object if This exists.
		if (thisName != null) {
			
			JSONObject starter = new JSONObject();
			starter.put(Messages.ApplicationIdentity.NAME, thisName);
			starter.put(Messages.ApplicationIdentity.ID, thisId);
			starter.put(Messages.ApplicationIdentity.SERVER, thisEndpoint);
			
			request.put(Messages.StartRequest.STARTER, starter);
		}
		
		if (args != null) {
			JSONArray list = new JSONArray();
			for (int i = 0; i < args.length; i++) {
				list.add(args[i]);
			}
			request.put(Messages.StartRequest.ARGS, list);
		}

		return request;
	}

	/**
	 * create stop request
	 * 
	 * @param id
	 * @return request
	 */
	public static JSONObject createStopRequest(int id) {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.STOP);
		request.put(Messages.StopRequest.ID, id);

		return request;
	}
	
	/**
	 * create kill request
	 * 
	 * @param id
	 * @return request
	 */
	public static JSONObject createKillRequest(int id) {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.KILL);
		request.put(Messages.KillRequest.ID, id);

		return request;
	}

	/**
	 * create connect request
	 * @return request
	 */
	public static JSONObject createConnectRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CONNECT);
		request.put(Messages.ConnectRequest.NAME, name);

		return request;
	}

	/**
	 * create connect with id request
	 * @return request
	 */
	public static JSONObject createConnectWithIdRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CONNECT_WITH_ID);
		request.put(Messages.ConnectWithIdRequest.ID, id);

		return request;
	}
	
	/**
	 * create all available request
	 * 
	 * @return request
	 */
	public static JSONObject createListRequest() {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.LIST);

		return request;
	}
	
	/**
	 * create showall request
	 * 
	 * @return request
	 */
	public static JSONObject createAppsRequest() {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.APPS);

		return request;
	}

	/**
	 * create showall request
	 * 
	 * @return request
	 */
	public static JSONObject createOutputPortWithIdRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.OUTPUT_PORT_WITH_ID);
		request.put(Messages.OutputPortWithIdRequest.ID, id);

		return request;
	}
	
	/**
	 * create WriteInput request
	 * 
	 * @param id
	 * @param inputs
	 * @return
	 */
	public static JSONObject createWriteInputRequest(int id, String[] inputs) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.WRITE_INPUT);
		request.put(Messages.WriteInputRequest.ID, id);
		
		JSONArray list = new JSONArray();
		for (int i = 0; i < inputs.length; i++) {
			list.add(inputs[i]);
		}
		request.put(Messages.WriteInputRequest.PARAMETERS, list);

		return request;
	}
	
	/**
	 * create output request
	 * 
	 * @param name
	 */
	public static JSONObject createOutputPortRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.OUTPUT_PORT);
		request.put(Messages.OutputRequest.NAME, name);

		return request;
	}
	
	public static JSONObject createSubscribePublisherRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SUBSCRIBE_PUBLISHER_v0);

		return request;
	}

	public static JSONObject createStoreKeyValueRequest(int applicationId, String key, String value) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.STORE_KEY_VALUE);
		request.put(Messages.StoreKeyValueRequest.ID, applicationId);
		request.put(Messages.StoreKeyValueRequest.KEY, key);
		request.put(Messages.StoreKeyValueRequest.VALUE, value);

		return request;
	}

	public static JSONObject createGetKeyValueRequest(int applicationId, String key) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.GET_KEY_VALUE);
		request.put(Messages.GetKeyValueRequest.ID, applicationId);
		request.put(Messages.GetKeyValueRequest.KEY, key);

		return request;
	}
	
	public static JSONObject createRemoveKeyRequest(int applicationId, String key) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REMOVE_KEY);
		request.put(Messages.RemoveKeyRequest.ID, applicationId);
		request.put(Messages.RemoveKeyRequest.KEY, key);

		return request;
	}

	public static JSONObject createRequestPortRequest(int applicationId) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST_PORT);
		request.put(Messages.RequestPortRequest.ID, applicationId);

		return request;	
	}

	public static JSONObject createPortUnavailableRequest(int applicationId, int port) {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.PORT_UNAVAILABLE);
		request.put(Messages.PortUnavailableRequest.ID, applicationId);
		request.put(Messages.PortUnavailableRequest.PORT, port);

		return request;
	}

	public static JSONObject createReleasePortRequest(int applicationId, int port) {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.RELEASE_PORT);
		request.put(Messages.ReleasePortRequest.ID, applicationId);
		request.put(Messages.ReleasePortRequest.PORT, port);

		return request;
	}
	
	public static JSONObject createPortsRequest() {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.PORTS);

		return request;
	}
	
	public static JSONObject createSetStatusRequest(int id, int state) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SET_STATUS);
		request.put(Messages.SetStatusRequest.ID, id);
		request.put(Messages.SetStatusRequest.APPLICATION_STATE, state);

		return request;
	}
	
	public static JSONObject createRequestPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST_PORT_v0);
		request.put(Messages.RequestPortV0Request.ID, id);
		request.put(Messages.RequestPortV0Request.NAME, name);

		return request;
	}

	public static JSONObject createConnectPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CONNECT_PORT_v0);
		request.put(Messages.ConnectPortV0Request.ID, id);
		request.put(Messages.ConnectPortV0Request.NAME, name);

		return request;
	}

	public static JSONObject createRemovePortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REMOVE_PORT_v0);
		request.put(Messages.RemovePortV0Request.ID, id);
		request.put(Messages.RemovePortV0Request.NAME, name);

		return request;
	}
	
	public static JSONObject createSetResultRequest(int id) {

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.SET_RESULT);
		request.put(Messages.SetResultRequest.ID, id);
		
		return request;
	}
	
	public static JSONObject createCreatePublisherRequest(int id, String name, int numberOfSubscribers) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CREATE_PUBLISHER_v0);
		request.put(Messages.CreatePublisherRequest.ID, id);
		request.put(Messages.CreatePublisherRequest.NAME, name);
		request.put(Messages.CreatePublisherRequest.NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);

		return request;
	}
	
	public static JSONObject createTerminatePublisherRequest(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.TERMINATE_PUBLISHER_v0);
		request.put(Messages.TerminatePublisherRequest.ID, id);
		request.put(Messages.TerminatePublisherRequest.NAME, name);

		return request;
	}
	
	public static JSONObject createConnectPublisherRequest(int applicationId, String publisherName) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CONNECT_PUBLISHER_v0);
		request.put(Messages.ConnectPublisherRequest.APPLICATION_ID, applicationId);
		request.put(Messages.ConnectPublisherRequest.PUBLISHER_NAME, publisherName);

		return request;
	}
	
}
