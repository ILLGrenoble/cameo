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

namespace message {

	constexpr const char* TYPE = "type";

	const int SYNC = 1;
	const int SYNC_STREAM = 2;
	const int START = 3;
	const int STOP = 4;
	const int CONNECT = 5;
	const int CONNECT_WITH_ID = 6;
	const int APPS = 7;
	const int OUTPUT_PORT = 8;
	const int OUTPUT_PORT_WITH_ID = 9;
	const int IS_ALIVE = 10;
	const int WRITE_INPUT = 11;
	const int KILL = 12;
	const int STATUS = 13;
	const int LIST = 14;
	const int SET_STATUS = 15;
	const int GET_STATUS = 16;
	const int RESPONDER_PROXY_PORT = 36;
	const int PUBLISHER_PROXY_PORT = 17;
	const int SUBSCRIBER_PROXY_PORT = 18;
	const int CANCEL = 19;
	const int SET_RESULT = 20;
	const int REQUEST = 21;
	const int RESPONSE = 22;
	const int ATTACH_UNREGISTERED = 23;
	const int DETACH_UNREGISTERED = 24;
	const int IMPL_VERSION = 25;
	const int STORE_KEY_VALUE = 26;
	const int GET_KEY_VALUE = 27;
	const int REMOVE_KEY = 28;
	const int REQUEST_PORT = 29;
	const int PORT_UNAVAILABLE = 30;
	const int RELEASE_PORT = 31;
	const int PORTS = 32;
	const int SET_STOP_HANDLER = 33;
	const int STREAM = 34;
	const int STREAM_END = 35;

	struct Event {
		constexpr static const char* CANCEL = "cancel";
		constexpr static const char* STREAM = "stream";
		constexpr static const char* STATUS = "status";
		constexpr static const char* RESULT = "result";
		constexpr static const char* KEYVALUE = "keyvalue";
	};

	struct ApplicationIdentity {
		constexpr static const char* NAME = "name"; // string
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* SERVER = "server"; // string
		constexpr static const char* STARTER = "starter"; // object
		constexpr static const char* STARTER_PROXY_PORT = "starterProxyPort"; // int32
		constexpr static const char* STARTER_LINKED = "starterLinked"; // boolean
	};

	struct SyncStreamRequest {
		constexpr static const char* NAME = "name"; // string
	};

	struct StartRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* ARGS = "args"; // repeated string args = 2;
		constexpr static const char* STARTER = "starter"; // object
		constexpr static const char* STARTER_PROXY_PORT = "starterProxyPort"; // int32
		constexpr static const char* STARTER_LINKED = "starterLinked"; // boolean
	};

	struct RequestResponse {
		constexpr static const char* VALUE = "value"; // required int32 value = 1;
		constexpr static const char* MESSAGE = "message"; // optional string message = 2;
	};

	struct SetStopHandlerRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* STOPPING_TIME = "stoppingTime"; // int32
	};

	struct StopRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct ConnectRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
	};

	struct ConnectWithIdRequest {
		constexpr static const char* ID = "id"; // int32
	};

	struct ApplicationConfig {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* DESCRIPTION = "description"; // optional string description = 2;
		constexpr static const char* RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		constexpr static const char* RESTART = "restart"; // required bool restart = 4;
		constexpr static const char* STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		constexpr static const char* STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	};

	struct ApplicationConfigListResponse {
		constexpr static const char* APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	};

	struct StatusEvent {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr static const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr static const char* EXIT_CODE = "exitCode"; // optional
	};

	struct ResultEvent {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* DATA = "data"; // required bytes data = 3;
	};

	struct ApplicationInfo {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* NAME = "name"; // required string name = 2;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr static const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr static const char* ARGS = "args"; // required string args = 5;
		constexpr static const char* PID = "pid"; // optional int64 pid = 6;
	};

	struct ApplicationInfoListResponse {
		constexpr static const char* APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo = 1;
	};

	struct OutputPortWithIdRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct OutputPortRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
	};

	struct IsAliveRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct IsAliveResponse {
		constexpr static const char* IS_ALIVE = "isAlive"; // required bool isAlive = 1;
	};

	struct ApplicationStream {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* MESSAGE = "message"; // required string message = 2;
		constexpr static const char* EOL = "eol"; // boolean
	};

	struct WriteInputRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* INPUTS = "inputs"; // repeated string parameters = 2;
	};

	struct KillRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct SetStatusRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 2;
	};

	struct GetStatusRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct SetResultRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
		constexpr static const char* DATA = "data"; // required bytes data = 2;
	};

	struct Request {
		constexpr static const char* APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		constexpr static const char* APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		constexpr static const char* SERVER_ENDPOINT = "serverEndpoint";
		constexpr static const char* SERVER_PROXY_PORT = "serverProxyPort";
		constexpr static const char* REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	};

	struct AttachUnregisteredRequest {
		constexpr static const char* NAME = "name"; // required string name = 1;
		constexpr static const char* PID = "pid"; // optional int64 pid = 2;
	};

	struct DetachUnregisteredRequest {
		constexpr static const char* ID = "id"; // required int32 id = 1;
	};

	struct VersionResponse {
		constexpr static const char* MAJOR = "major";
		constexpr static const char* MINOR = "minor";
		constexpr static const char* REVISION = "revision";
	};

	struct StoreKeyValueRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
		constexpr static const char* VALUE = "value"; // string
	};

	struct GetKeyValueRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
	};

	struct RemoveKeyRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* KEY = "key"; // string
	};

	struct KeyEvent {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* NAME = "name"; // string
		constexpr static const char* STATUS = "status"; // long STORE_KEY_VALUE or REMOVE_KEY
		constexpr static const char* KEY = "key"; // string
		constexpr static const char* VALUE = "value"; // string
	};

	struct RequestPortRequest {
		constexpr static const char* ID = "id"; // int32
	};

	struct PortUnavailableRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* PORT = "port"; // int32
	};

	struct ReleasePortRequest {
		constexpr static const char* ID = "id"; // int32
		constexpr static const char* PORT = "port"; // int32
	};

	struct PortInfo {
		constexpr static const char* PORT = "port"; // int32
		constexpr static const char* STATUS = "status"; // string
		constexpr static const char* OWNER = "owner"; // string
	};

	struct PortInfoListResponse {
		constexpr static const char* PORT_INFO = "portInfo"; // multiple PortInfo
	};

}

std::string createSyncRequest();
std::string createSyncStreamRequest(const std::string& name);
std::string createVersionRequest();
std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint, int thisProxyPort, bool linked);
std::string createSetStopHandlerRequest(int id, int stoppingTime);
std::string createStopRequest(int id);
std::string createKillRequest(int id);
std::string createConnectRequest(const std::string& name);
std::string createConnectWithIdRequest(int id);
std::string createIsAliveRequest(int id);
std::string createListRequest();
std::string createAppsRequest();
std::string createStreamStatusRequest();
std::string createSetStatusRequest(int id, int32_t state);
std::string createGetStatusRequest(int id);
std::string createResponderProxyPortRequest();
std::string createPublisherProxyPortRequest();
std::string createSubscriberProxyPortRequest();
std::string createSetResultRequest(int id);
std::string createAttachUnregisteredRequest(const std::string& name, long pid);
std::string createDetachUnregisteredRequest(int id);
std::string createOutputPortWithIdRequest(int id);
std::string createOutputPortRequest(const std::string& name);
std::string createRequestResponse(int32_t value, const std::string& message);
std::string createStoreKeyValueRequest(int id, const std::string& key, const std::string& value);
std::string createGetKeyValueRequest(int id, const std::string& key);
std::string createRemoveKeyRequest(int id, const std::string& key);
std::string createRequestPortRequest(int id);
std::string createPortUnavailableRequest(int id, int port);
std::string createReleasePortRequest(int id, int port);
std::string createPortsRequest();

}

#endif

