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

#ifndef CAMEO_REQUESTS_H_
#define CAMEO_REQUESTS_H_

#include <string>
#include <vector>

namespace cameo {

/**
 * Namespace for the messages exchanged.
 */
namespace message {

	/**
	 * Type constant.
	 */
	constexpr const char* TYPE = "type";

	/**
	 * Sync type value.
	 */
	const int SYNC = 1;

	/**
	 * Sync stream type value.
	 */
	const int SYNC_STREAM = 2;

	/**
	 * Start type value.
	 */
	const int START = 3;

	/**
	 * Stop type value.
	 */
	const int STOP = 4;

	/**
	 * Connect type value.
	 */
	const int CONNECT = 5;

	/**
	 * Connect with id type value.
	 */
	const int CONNECT_WITH_ID = 6;

	/**
	 * Apps type value.
	 */
	const int APPS = 7;

	/**
	 * Output port type value.
	 */
	const int OUTPUT_PORT = 8;

	/**
	 * Output port with id type value.
	 */
	const int OUTPUT_PORT_WITH_ID = 9;

	/**
	 * Is alive type value.
	 */
	const int IS_ALIVE = 10;

	/**
	 * Write input type value.
	 */
	const int WRITE_INPUT = 11;

	/**
	 * Kill type value.
	 */
	const int KILL = 12;

	/**
	 * Status type value.
	 */
	const int STATUS = 13;

	/**
	 * List type value.
	 */
	const int LIST = 14;

	/**
	 * Set status port type value.
	 */
	const int SET_STATUS = 15;

	/**
	 * Get status type value.
	 */
	const int GET_STATUS = 16;

	/**
	 * Responder proxy port type value.
	 */
	const int RESPONDER_PROXY_PORT = 36;

	/**
	 * Publisher proxy port type value.
	 */
	const int PUBLISHER_PROXY_PORT = 17;

	/**
	 * Subscriber proxy port type value.
	 */
	const int SUBSCRIBER_PROXY_PORT = 18;

	/**
	 * Cancel type value.
	 */
	const int CANCEL = 19;

	/**
	 * Set result type value.
	 */
	const int SET_RESULT = 20;

	/**
	 * Request type value.
	 */
	const int REQUEST = 21;

	/**
	 * Response type value.
	 */
	const int RESPONSE = 22;

	/**
	 * Attach unregistered type value.
	 */
	const int ATTACH_UNREGISTERED = 23;

	/**
	 * Detach unregistered type value.
	 */
	const int DETACH_UNREGISTERED = 24;

	/**
	 * Implementation version type value.
	 */
	const int IMPL_VERSION = 25;

	/**
	 * Store key value type value.
	 */
	const int STORE_KEY_VALUE = 26;

	/**
	 * Get key value type value.
	 */
	const int GET_KEY_VALUE = 27;

	/**
	 * Remove key type value.
	 */
	const int REMOVE_KEY = 28;

	/**
	 * Request port type value.
	 */
	const int REQUEST_PORT = 29;

	/**
	 * Port unavailable type value.
	 */
	const int PORT_UNAVAILABLE = 30;

	/**
	 * Release port type value.
	 */
	const int RELEASE_PORT = 31;

	/**
	 * Ports type value.
	 */
	const int PORTS = 32;

	/**
	 * Set stop handler type value.
	 */
	const int SET_STOP_HANDLER = 33;

	/**
	 * Stream type value.
	 */
	const int STREAM = 34;

	/**
	 * Stream end type value.
	 */
	const int STREAM_END = 35;

	/**
	 * Event message constants.
	 */
	struct Event {
		constexpr static const char* CANCEL = "cancel";
		constexpr static const char* STREAM = "stream";
		constexpr static const char* STATUS = "status";
		constexpr static const char* RESULT = "result";
		constexpr static const char* KEYVALUE = "keyvalue";
	};

	/**
	 * Application identity message constants.
	 */
	struct ApplicationIdentity {
		constexpr static const char* NAME = "name"; // string
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* SERVER = "server"; // string
		constexpr static const char* STARTER = "starter"; // object
		constexpr static const char* STARTER_PROXY_PORT = "starterProxyPort"; // int32
		constexpr static const char* STARTER_LINKED = "starterLinked"; // boolean
	};

	/**
	 * Sync stream request message constants.
	 */
	struct SyncStreamRequest {
		constexpr static const char* NAME = "name"; // string
	};

	/**
	 * Start request message constants.
	 */
	struct StartRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* ARGS = "args"; // repeated string args = 2;
		constexpr static const char* STARTER = "starter"; // object
		constexpr static const char* STARTER_PROXY_PORT = "starterProxyPort"; // int32
		constexpr static const char* STARTER_LINKED = "starterLinked"; // boolean
	};

	/**
	 * Request response message constants.
	 */
	struct RequestResponse {
		constexpr static const char* VALUE = "value"; // required int32 value = 1;
		constexpr static const char* MESSAGE = "message"; // optional string message = 2;
	};

	/**
	 * Set stop handler request message constants.
	 */
	struct SetStopHandlerRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* STOPPING_TIME = "stoppingTime"; // int32
	};

	/**
	 * Stop request message constants.
	 */
	struct StopRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* LINK = "link";
	};

	/**
	 * Connect request message constants.
	 */
	struct ConnectRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
	};

	/**
	 * Connect with id request message constants.
	 */
	struct ConnectWithIdRequest {
		constexpr static const char* ID = "id"; // int32
	};

	/**
	 * Application config message constants.
	 */
	struct ApplicationConfig {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* DESCRIPTION = "description"; // optional string description = 2;
		constexpr static const char* RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		constexpr static const char* MULTIPLE = "multiple";
		constexpr static const char* RESTART = "restart"; // required bool restart = 4;
		constexpr static const char* STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		constexpr static const char* STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	};

	/**
	 * Application config list response message constants.
	 */
	struct ApplicationConfigListResponse {
		constexpr static const char* APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	};

	/**
	 * Status event message constants.
	 */
	struct StatusEvent {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr static const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr static const char* EXIT_CODE = "exitCode"; // optional
	};

	/**
	 * Result event message constants.
	 */
	struct ResultEvent {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* DATA = "data"; // required bytes data = 3;
	};

	/**
	 * Application info message constants.
	 */
	struct ApplicationInfo {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr static const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr static const char* ARGS = "args"; // required string args = 5;
		constexpr static const char* PID = "pid"; // optional int64 pid = 6;
	};

	/**
	 * Application info list response message constants.
	 */
	struct ApplicationInfoListResponse {
		constexpr static const char* APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo = 1;
	};

	/**
	 * Output port with id request message constants.
	 */
	struct OutputPortWithIdRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	/**
	 * Output port request message constants.
	 */
	struct OutputPortRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
	};

	/**
	 * Is alive request message constants.
	 */
	struct IsAliveRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	/**
	 * Is alive response message constants.
	 */
	struct IsAliveResponse {
		constexpr static const char* IS_ALIVE = "isAlive"; // required bool isAlive = 1;
	};

	/**
	 * Application stream message constants.
	 */
	struct ApplicationStream {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* MESSAGE = "message"; // required string message = 2;
		constexpr static const char* EOL = "eol"; // boolean
	};

	/**
	 * Write input request message constants.
	 */
	struct WriteInputRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* INPUTS = "inputs"; // repeated string parameters = 2;
	};

	/**
	 * Kill request message constants.
	 */
	struct KillRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	/**
	 * Set status request message constants.
	 */
	struct SetStatusRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 2;
	};

	/**
	 * Get status message constants.
	 */
	struct GetStatusRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	/**
	 * Set result request message constants.
	 */
	struct SetResultRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* DATA = "data"; // required bytes data = 2;
	};

	/**
	 * Request message constants.
	 */
	struct Request {
		constexpr static const char* APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		constexpr static const char* APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		constexpr static const char* SERVER_ENDPOINT = "serverEndpoint";
		constexpr static const char* SERVER_PROXY_PORT = "serverProxyPort";
		constexpr static const char* REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	};

	/**
	 * Attach unregistered request message constants.
	 */
	struct AttachUnregisteredRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* PID = "pid"; // optional int64 pid = 2;
	};

	/**
	 * Detach unregistered request message constants.
	 */
	struct DetachUnregisteredRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	/**
	 * Version response message constants.
	 */
	struct VersionResponse {
		constexpr static const char* MAJOR = "major";
		constexpr static const char* MINOR = "minor";
		constexpr static const char* REVISION = "revision";
	};

	/**
	 * Store key value request message constants.
	 */
	struct StoreKeyValueRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
		constexpr static const char* VALUE = "value"; // string
	};

	/**
	 * Get key value request message constants.
	 */
	struct GetKeyValueRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
	};

	/**
	 * Remove key request message constants.
	 */
	struct RemoveKeyRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
	};

	/**
	 * Key event message constants.
	 */
	struct KeyEvent {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* NAME = "name"; // string
		constexpr static const char* STATUS = "status"; // long STORE_KEY_VALUE or REMOVE_KEY
		constexpr static const char* KEY = "key"; // string
		constexpr static const char* VALUE = "value"; // string
	};

	/**
	 * Request port request message constants.
	 */
	struct RequestPortRequest {
		constexpr static const char* ID = "id"; // int32
	};

	/**
	 * Port unavailable message constants.
	 */
	struct PortUnavailableRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* PORT = "port"; // int32
	};

	/**
	 * Release port request message constants.
	 */
	struct ReleasePortRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* PORT = "port"; // int32
	};

	/**
	 * Port info message constants.
	 */
	struct PortInfo {
		constexpr static const char* PORT = "port"; // int32
		constexpr static const char* STATUS = "status"; // string
		constexpr static const char* OWNER = "owner"; // string
	};

	/**
	 * Port info list response message constants.
	 */
	struct PortInfoListResponse {
		constexpr static const char* PORT_INFO = "portInfo"; // multiple PortInfo
	};

}

/**
 * Creates a sync request.
 * \return The JSON string.
 */
std::string createSyncRequest();

/**
 * Creates a sync stream request.
 * \param name The name.
 * \return The JSON string.
 */
std::string createSyncStreamRequest(const std::string& name);

/**
 * Creates a version request.
 * \return The JSON string.
 */
std::string createVersionRequest();

/**
 * Creates a start request.
 * \param name The name.
 * \param args The arguments.
 * \param thisName This application name.
 * \param thisId This application id.
 * \param thisEndpoint This application endpoint.
 * \param thisProxyPort This application proxy port.
 * \param linked True if the application is linked.
 * \return The JSON string.
 */
std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint, int thisProxyPort, bool linked);

/**
 * Creates a set stop handler request.
 * \param id The id.
 * \param stoppingTime The stopping time.
 * \return The JSON string.
 */
std::string createSetStopHandlerRequest(int id, int stoppingTime);

/**
 * Creates a stop request.
 * \param id The id.
 * \param link True if the stop is because the app is linked to its parent which is stopping.
 * \return The JSON string.
 */
std::string createStopRequest(int id, bool link);

/**
 * Creates a kill request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createKillRequest(int id);

/**
 * Creates a connect request.
 * \param name The name.
 * \return The JSON string.
 */
std::string createConnectRequest(const std::string& name);

/**
 * Creates a connect with id request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createConnectWithIdRequest(int id);

/**
 * Creates a is alive request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createIsAliveRequest(int id);

/**
 * Creates a list request.
 * \return The JSON string.
 */
std::string createListRequest();

/**
 * Creates an apps request.
 * \return The JSON string.
 */
std::string createAppsRequest();

/**
 * Creates a stream status request.
 * \return The JSON string.
 */
std::string createStreamStatusRequest();

/**
 * Creates a set status request.
 * \param id The id.
 * \param state The state.
 * \return The JSON string.
 */
std::string createSetStatusRequest(int id, int32_t state);

/**
 * Creates a get status request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createGetStatusRequest(int id);

/**
 * Creates a responder proxy port request.
 * \return The JSON string.
 */
std::string createResponderProxyPortRequest();

/**
 * Creates a publisher proxy port request.
 * \return The JSON string.
 */
std::string createPublisherProxyPortRequest();

/**
 * Creates a subscriber proxy port request.
 * \return The JSON string.
 */
std::string createSubscriberProxyPortRequest();

/**
 * Creates a set result request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createSetResultRequest(int id);

/**
 * Creates an attach unregistered request.
 * \param name The name.
 * \param pid The PID.
 * \return The JSON string.
 */
std::string createAttachUnregisteredRequest(const std::string& name, long pid);

/**
 * Creates a detached unregistered request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createDetachUnregisteredRequest(int id);

/**
 * Creates an output port with id request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createOutputPortWithIdRequest(int id);

/**
 * Creates an output port request.
 * \param name The name.
 * \return The JSON string.
 */
std::string createOutputPortRequest(const std::string& name);

/**
 * Creates a request response.
 * \param value The value.
 * \param message The message.
 * \return The JSON string.
 */
std::string createRequestResponse(int32_t value, const std::string& message);

/**
 * Creates a store key value request.
 * \param id The id.
 * \param key The key.
 * \param value The value.
 * \return The JSON string.
 */
std::string createStoreKeyValueRequest(int id, const std::string& key, const std::string& value);

/**
 * Creates a get key value request.
 * \param id The id.
 * \param key The key.
 * \return The JSON string.
 */
std::string createGetKeyValueRequest(int id, const std::string& key);

/**
 * Creates a remove key request.
 * \param id The id.
 * \param key The key.
 * \return The JSON string.
 */
std::string createRemoveKeyRequest(int id, const std::string& key);

/**
 * Creates a request port request.
 * \param id The id.
 * \return The JSON string.
 */
std::string createRequestPortRequest(int id);

/**
 * Creates a port unavailable request.
 * \param id The id.
 * \param port The port.
 * \return The JSON string.
 */
std::string createPortUnavailableRequest(int id, int port);

/**
 * Creates a release port request.
 * \param id The id.
 * \param port The port.
 * \return The JSON string.
 */
std::string createReleasePortRequest(int id, int port);

/**
 * Creates a ports request.
 * \return The JSON string.
 */
std::string createPortsRequest();

}

#endif

