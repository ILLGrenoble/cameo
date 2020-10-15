package fr.ill.ics.cameo.messages;

import java.nio.charset.Charset;

import org.json.simple.JSONObject;

/**
 * Definitions for the JSON objects. 
 */
public class Message {
	
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
	public static final long CREATE_PUBLISHER = 17;
	public static final long TERMINATE_PUBLISHER = 18;
	public static final long CONNECT_PUBLISHER = 19;
	public static final long SUBSCRIBE_PUBLISHER = 20;
	public static final long CANCEL = 21;
	public static final long SET_RESULT = 22;
	public static final long REQUEST_PORT = 23;
	public static final long CONNECT_PORT = 24;
	public static final long REMOVE_PORT = 25;
	public static final long REQUEST = 26;
	public static final long RESPONSE = 27;
	public static final long STARTED_UNMANAGED = 28;
	public static final long TERMINATED_UNMANAGED = 29;
	public static final long IMPL_VERSION = 30;
	public static final long STORE_KEY_VALUE = 31;
	public static final long GET_KEY_VALUE = 32;
	public static final long REMOVE_KEY = 33;
		
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
	
	public static class SyncStreamRequest {
		public static final String NAME = "name"; // string
	}
	
	public static class StartRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String ARGS = "args"; // repeated string args = 2;
		public static final String INSTANCE_REFERENCE = "instanceReference"; // required string instanceReference = 3;
	}
	
	public static class RequestResponse {
		public static final String VALUE = "value"; // required int32 value = 1;
		public static final String MESSAGE = "message"; // optional string message = 2;
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

	public static class ListResponse {
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

	public static class RequestPortRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class ConnectPortRequest {
		public static final String ID = "id"; // required int32 id = 1;
		public static final String NAME = "name"; // required string name = 2;
	}

	public static class RemovePortRequest {
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

	public static class StartedUnmanagedRequest {
		public static final String NAME = "name"; // required string name = 1;
		public static final String PID = "pid"; // optional int64 pid = 2;
	}

	public static class TerminatedUnmanagedRequest {
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
	
	public static String parseString(byte[] data) {
		return new String(data, CHARSET);
	}
	
	public static byte[] serialize(String string) {
		return string.getBytes(CHARSET);
	}
	
	public static byte[] serialize(JSONObject object) {
		return serialize(object.toJSONString());
	}
}
