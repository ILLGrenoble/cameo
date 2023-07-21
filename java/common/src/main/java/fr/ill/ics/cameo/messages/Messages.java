package fr.ill.ics.cameo.messages;

import java.nio.charset.Charset;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

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
	public static final long RESPONDER_PROXY_PORT = 36;
	public static final long PUBLISHER_PROXY_PORT = 17;
	public static final long SUBSCRIBER_PROXY_PORT = 18;
	public static final long CANCEL = 19;
	public static final long SET_RESULT = 20;
	public static final long REQUEST = 21;
	public static final long RESPONSE = 22;
	public static final long ATTACH_UNREGISTERED = 23;
	public static final long DETACH_UNREGISTERED = 24;
	public static final long IMPL_VERSION = 25;
	public static final long STORE_KEY_VALUE = 26;
	public static final long GET_KEY_VALUE = 27;
	public static final long REMOVE_KEY = 28;
	public static final long REQUEST_PORT = 29;
	public static final long PORT_UNAVAILABLE = 30;
	public static final long RELEASE_PORT = 31;
	public static final long PORTS = 32;
	public static final long SET_STOP_HANDLER = 33;
	public static final long STREAM = 34;
	public static final long STREAM_END = 35;

	public static class Event {
		public static final String CANCEL = "cancel";
		public static final String STREAM = "stream";
		public static final String STATUS = "status";
		public static final String RESULT = "result";
		public static final String KEYVALUE = "keyvalue";
	}

	public static class ApplicationIdentity {
		public static final String NAME = "name"; // string
		public static final String ID = "id"; // int32
		public static final String SERVER = "server"; // string
		public static final String STARTER = "starter"; // object
		public static final String STARTER_PROXY_PORT = "starterProxyPort"; // int32
		public static final String STARTER_LINKED = "starterLinked"; // boolean		
	}

	public static class SyncStreamRequest {
		public static final String NAME = "name"; // string
	}

	public static class StartRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String ARGS = "args"; // repeated string args = 2;
		public static final String STARTER = "starter"; // object
		public static final String STARTER_PROXY_PORT = "starterProxyPort"; // int32
		public static final String STARTER_LINKED = "starterLinked"; // boolean
	}

	public static class RequestResponse {
		public static final String VALUE = "value"; // required int32 value = 1;
		public static final String MESSAGE = "message"; // optional string message = 2;
	}

	public static class SetStopHandlerRequest {
		public static final String ID = "id"; // int32
		public static final String STOPPING_TIME = "stoppingTime"; // int32
	}

	public static class StopRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String LINK = "link";
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
		public static final String MULTIPLE = "multiple"; // required bool runsSingle = 3;
		public static final String RESTART = "restart"; // required bool restart = 4;
		public static final String STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		public static final String STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	}

	public static class ApplicationConfigListResponse {
		public static final String APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig
																				// applicationConfig = 1;
	}

	public static class StatusEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		public static final String PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32
																						// pastApplicationStates = 4;
		public static final String EXIT_CODE = "exitCode"; // optional
	}

	public static class ResultEvent {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String DATA = "data"; // required bytes data = 3;
	}

	public static class ApplicationInfo {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
		public static final String APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		public static final String PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32
																						// pastApplicationStates = 4;
		public static final String ARGS = "args"; // required string args = 5;
		public static final String PID = "pid"; // optional int64 pid = 6;
	}

	public static class ApplicationInfoListResponse {
		public static final String APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo =
																			// 1;
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

	public static class Request {
		public static final String APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		public static final String APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		public static final String SERVER_ENDPOINT = "serverEndpoint";
		public static final String SERVER_PROXY_PORT = "serverProxyPort";
		public static final String REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	}

	public static class AttachUnregisteredRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String PID = "pid"; // optional int64 pid = 2;
	}

	public static class DetachUnregisteredRequest {
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

	public static byte[] serialize(JsonObject object) {
		return serialize(object.toString());
	}

	public static JsonObject createSyncRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.SYNC)
				.build();

		return request;
	}

	public static JsonObject createSyncStreamRequest(String name) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.SYNC_STREAM)
				.add(Messages.SyncStreamRequest.NAME, name)
				.build();

		return request;
	}

	public static JsonObject createVersionRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.IMPL_VERSION)
				.build();

		return request;
	}

	public static JsonObject createStreamStatusRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.STATUS)
				.build();

		return request;
	}

	public static JsonObject createResponderProxyPortRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.RESPONDER_PROXY_PORT)
				.build();

		return request;
	}
	
	public static JsonObject createPublisherProxyPortRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.PUBLISHER_PROXY_PORT)
				.build();

		return request;
	}

	public static JsonObject createSubscriberProxyPortRequest() {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.SUBSCRIBER_PROXY_PORT)
				.build();

		return request;
	}

	public static JsonObject createGetStatusRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.GET_STATUS)
				.add(Messages.GetStatusRequest.ID, id)
				.build();

		return request;
	}

	public static JsonObject createAttachUnregisteredRequest(String name, long pid) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.ATTACH_UNREGISTERED)
				.add(Messages.AttachUnregisteredRequest.NAME, name)
				.add(Messages.AttachUnregisteredRequest.PID, pid)
				.build();

		return request;
	}

	public static JsonObject createDetachUnregisteredRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.DETACH_UNREGISTERED)
				.add(Messages.DetachUnregisteredRequest.ID, id)
				.build();

		return request;
	}

	public static JsonObject createSetStopHandlerRequest(int id, int stoppingTime) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.SET_STOP_HANDLER)
				.add(Messages.SetStopHandlerRequest.ID, id)
				.add(Messages.SetStopHandlerRequest.STOPPING_TIME, stoppingTime)
				.build();

		return request;
	}

	/**
	 * create isAlive request
	 * 
	 * @param text
	 * @return
	 */
	public static JsonObject createIsAliveRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.IS_ALIVE)
				.add(Messages.IsAliveRequest.ID, id)
				.build();

		return request;
	}

	/**
	 * create start request with parameters
	 * 
	 * @param name
	 * @param args
	 * @param linked 
	 * @param returnResult
	 * @return request
	 */
	public static JsonObject createStartRequest(String name, String[] args, String thisName, int thisId, String thisEndpoint, int thisProxyPort, boolean linked) {

		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.START)
				.add(Messages.StartRequest.NAME, name);

		// Add the starter object if This exists.
		if (thisName != null) {

			JsonObject starter = Json.createObjectBuilder()
				.add(Messages.ApplicationIdentity.NAME, thisName)
				.add(Messages.ApplicationIdentity.ID, thisId)
				.add(Messages.ApplicationIdentity.SERVER, thisEndpoint)
				.build();

			builder.add(Messages.StartRequest.STARTER, starter);
			builder.add(Messages.StartRequest.STARTER_PROXY_PORT, thisProxyPort);
			builder.add(Messages.StartRequest.STARTER_LINKED, linked);
		}

		if (args != null) {
			JsonArrayBuilder listBuilder = Json.createArrayBuilder();
			for (int i = 0; i < args.length; i++) {
				listBuilder.add(args[i]);
			}
			builder.add(Messages.StartRequest.ARGS, listBuilder.build());
		}

		return builder.build();
	}

	/**
	 * create stop request
	 * 
	 * @param id
	 * @param link
	 * @return request
	 */
	public static JsonObject createStopRequest(int id, boolean link) {

		JsonObject request = Json.createObjectBuilder()
				.add(Messages.TYPE, Messages.STOP)
				.add(Messages.StopRequest.ID, id)
				.add(Messages.StopRequest.LINK, link).build();

		return request;
	}

	/**
	 * create kill request
	 * 
	 * @param id
	 * @return request
	 */
	public static JsonObject createKillRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.KILL)
			.add(Messages.KillRequest.ID, id).build();

		return request;
	}

	/**
	 * create connect request
	 * 
	 * @return request
	 */
	public static JsonObject createConnectRequest(String name) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.CONNECT)
			.add(Messages.ConnectRequest.NAME, name).build();

		return request;
	}

	/**
	 * create connect with id request
	 * 
	 * @return request
	 */
	public static JsonObject createConnectWithIdRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.CONNECT_WITH_ID)
			.add(Messages.ConnectWithIdRequest.ID, id).build();

		return request;
	}

	/**
	 * create all available request
	 * 
	 * @return request
	 */
	public static JsonObject createListRequest() {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.LIST).build();

		return request;
	}

	/**
	 * create showall request
	 * 
	 * @return request
	 */
	public static JsonObject createAppsRequest() {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.APPS).build();

		return request;
	}

	/**
	 * create showall request
	 * 
	 * @return request
	 */
	public static JsonObject createOutputPortWithIdRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.OUTPUT_PORT_WITH_ID)
			.add(Messages.OutputPortWithIdRequest.ID, id).build();

		return request;
	}

	/**
	 * create WriteInput request
	 * 
	 * @param id
	 * @param inputs
	 * @return
	 */
	public static JsonObject createWriteInputRequest(int id, String[] inputs) {

		JsonObjectBuilder builder = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.WRITE_INPUT)
			.add(Messages.WriteInputRequest.ID, id);

		JsonArrayBuilder listBuilder = Json.createArrayBuilder();
		for (int i = 0; i < inputs.length; i++) {
			listBuilder.add(inputs[i]);
		}
		
		builder.add(Messages.WriteInputRequest.PARAMETERS, listBuilder);

		return builder.build();
	}

	/**
	 * create output request
	 * 
	 * @param name
	 */
	public static JsonObject createOutputPortRequest(String name) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.OUTPUT_PORT)
			.add(Messages.OutputRequest.NAME, name)
			.build();

		return request;
	}

	public static JsonObject createRequestResponse(int value, String message) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.RESPONSE)
			.add(Messages.RequestResponse.VALUE, value)
			.add(Messages.RequestResponse.MESSAGE, message)
			.build();

		return request;
	}

	public static JsonObject createStoreKeyValueRequest(int applicationId, String key, String value) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.STORE_KEY_VALUE)
			.add(Messages.StoreKeyValueRequest.ID, applicationId)
			.add(Messages.StoreKeyValueRequest.KEY, key)
			.add(Messages.StoreKeyValueRequest.VALUE, value)
			.build();

		return request;
	}

	public static JsonObject createGetKeyValueRequest(int applicationId, String key) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.GET_KEY_VALUE)
			.add(Messages.GetKeyValueRequest.ID, applicationId)
			.add(Messages.GetKeyValueRequest.KEY, key)
			.build();

		return request;
	}

	public static JsonObject createRemoveKeyRequest(int applicationId, String key) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.REMOVE_KEY)
			.add(Messages.RemoveKeyRequest.ID, applicationId)
			.add(Messages.RemoveKeyRequest.KEY, key)
			.build();

		return request;
	}

	public static JsonObject createRequestPortRequest(int applicationId) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.REQUEST_PORT)
			.add(Messages.RequestPortRequest.ID, applicationId).build();

		return request;
	}

	public static JsonObject createPortUnavailableRequest(int applicationId, int port) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.PORT_UNAVAILABLE)
			.add(Messages.PortUnavailableRequest.ID, applicationId)
			.add(Messages.PortUnavailableRequest.PORT, port)
			.build();

		return request;
	}

	public static JsonObject createReleasePortRequest(int applicationId, int port) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.RELEASE_PORT)
			.add(Messages.ReleasePortRequest.ID, applicationId)
			.add(Messages.ReleasePortRequest.PORT, port)
			.build();

		return request;
	}

	public static JsonObject createPortsRequest() {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.PORTS)
			.build();

		return request;
	}

	public static JsonObject createSetStatusRequest(int id, int state) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.SET_STATUS)
			.add(Messages.SetStatusRequest.ID, id)
			.add(Messages.SetStatusRequest.APPLICATION_STATE, state)
			.build();

		return request;
	}

	public static JsonObject createSetResultRequest(int id) {

		JsonObject request = Json.createObjectBuilder()
			.add(Messages.TYPE, Messages.SET_RESULT)
			.add(Messages.SetResultRequest.ID, id)
			.build();

		return request;
	}

}
