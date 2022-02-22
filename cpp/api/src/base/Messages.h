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

#include "Strings.h"
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
	const int PUBLISHER_PROXY_PORT = 17;
	const int CANCEL = 21;
	const int SET_RESULT = 22;
	const int REQUEST = 26;
	const int RESPONSE = 27;
	const int ATTACH_UNREGISTERED = 28;
	const int DETACH_UNREGISTERED = 29;
	const int IMPL_VERSION = 30;
	const int STORE_KEY_VALUE = 31;
	const int GET_KEY_VALUE = 32;
	const int REMOVE_KEY = 33;
	const int REQUEST_PORT = 34;
	const int PORT_UNAVAILABLE = 35;
	const int RELEASE_PORT = 36;
	const int PORTS = 37;
	const int SET_STOP_HANDLER = 38;
	const int STREAM_MESSAGE = 39;
	const int STREAM_END = 40;

	namespace Event {
//		constexpr const char* SYNC = "sync";
//		constexpr const char* CANCEL = "cancel";
//		constexpr const char* STREAM = "stream";
//		constexpr const char* ENDSTREAM_temp = "endstream";
//		constexpr const char* STATUS = "status";
//		constexpr const char* RESULT = "result";
//		constexpr const char* KEYVALUE = "keyvalue";

		extern const uint32_t SYNC_value;
		extern const uint32_t CANCEL_value;
		extern const uint32_t STREAM_value;
		extern const uint32_t ENDSTREAM_temp_value;
		extern const uint32_t STATUS_value;
		extern const uint32_t RESULT_value;
		extern const uint32_t KEYVALUE_value;

		extern const std::string SYNC;
		extern const std::string CANCEL;
		extern const std::string STREAM;
		extern const std::string ENDSTREAM_temp;
		extern const std::string STATUS;
		extern const std::string RESULT;
		extern const std::string KEYVALUE;

	}

	namespace ApplicationIdentity {
		constexpr const char* NAME = "name"; // string
		constexpr const char* ID = "id"; // int32
		constexpr const char* SERVER = "server"; // string
		constexpr const char* STARTER = "starter"; // object
	}

	namespace SyncStreamRequest {
		constexpr const char* NAME = "name"; // string
	}

	namespace StartRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* ARGS = "args"; // repeated string args = 2;
		constexpr const char* STARTER = "starter"; // object
	}

	namespace RequestResponse {
		constexpr const char* VALUE = "value"; // required int32 value = 1;
		constexpr const char* MESSAGE = "message"; // optional string message = 2;
	}

	namespace SetStopHandlerRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* STOPPING_TIME = "stopping_time"; // int32
	}

	namespace StopRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace ConnectRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
	}

	namespace ConnectWithIdRequest {
		constexpr const char* ID = "id"; // int32
	}

	namespace ApplicationConfig {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* DESCRIPTION = "description"; // optional string description = 2;
		constexpr const char* RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		constexpr const char* RESTART = "restart"; // required bool restart = 4;
		constexpr const char* STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		constexpr const char* STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	}

	namespace ApplicationConfigListResponse {
		constexpr const char* APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	}

	namespace StatusEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr const char* EXIT_CODE = "exitCode"; // optional
	}

	namespace ResultEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* DATA = "data"; // required bytes data = 3;
	}

	namespace ApplicationInfo {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		constexpr const char* ARGS = "args"; // required string args = 5;
		constexpr const char* PID = "pid"; // optional int64 pid = 6;
	}

	namespace ApplicationInfoListResponse {
		constexpr const char* APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo = 1;
	}

	namespace OutputPortWithIdRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace OutputPortRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
	}

	namespace IsAliveRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace IsAliveResponse {
		constexpr const char* IS_ALIVE = "isAlive"; // required bool isAlive = 1;
	}

	namespace ApplicationStream {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* MESSAGE = "message"; // required string message = 2;
		constexpr const char* EOL = "eol"; // boolean
	}

	namespace WriteInputRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* INPUTS = "inputs"; // repeated string parameters = 2;
	}

	namespace KillRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace SetStatusRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 2;
	}

	namespace GetStatusRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace SetResultRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* DATA = "data"; // required bytes data = 2;
	}

	namespace Request {
		constexpr const char* APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		constexpr const char* APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		constexpr const char* SERVER_URL = "serverUrl"; // required string serverUrl = 5;
		constexpr const char* SERVER_PORT = "serverPort"; // required int32 serverPort = 6;
		constexpr const char* REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	}

	namespace AttachUnregisteredRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* PID = "pid"; // optional int64 pid = 2;
	}

	namespace DetachUnregisteredRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace VersionResponse {
		constexpr const char* MAJOR = "major";
		constexpr const char* MINOR = "minor";
		constexpr const char* REVISION = "revision";
	}

	namespace StoreKeyValueRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* KEY = "key"; // string
		constexpr const char* VALUE = "value"; // string
	}

	namespace GetKeyValueRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* KEY = "key"; // string
	}

	namespace RemoveKeyRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* KEY = "key"; // string
	}

	namespace KeyEvent {
		constexpr const char* ID = "id"; // int32
		constexpr const char* NAME = "name"; // string
		constexpr const char* STATUS = "status"; // long STORE_KEY_VALUE or REMOVE_KEY
		constexpr const char* KEY = "key"; // string
		constexpr const char* VALUE = "value"; // string
	}

	namespace RequestPortRequest {
		constexpr const char* ID = "id"; // int32
	}

	namespace PortUnavailableRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* PORT = "port"; // int32
	}

	namespace ReleasePortRequest {
		constexpr const char* ID = "id"; // int32
		constexpr const char* PORT = "port"; // int32
	}

	namespace PortInfo {
		constexpr const char* PORT = "port"; // int32
		constexpr const char* STATUS = "status"; // string
		constexpr const char* OWNER = "owner"; // string
	}

	namespace PortInfoListResponse {
		constexpr const char* PORT_INFO = "portInfo"; // multiple PortInfo
	}

}

std::string createSyncRequest();
std::string createSyncStreamRequest(const std::string& name);
std::string createVersionRequest();
std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint);
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
std::string createPublisherProxyPortRequest();
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

