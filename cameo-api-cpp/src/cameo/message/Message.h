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

#ifndef CAMEO_MESSAGE_H_
#define CAMEO_MESSAGE_H_

#include <string>

namespace cameo {

namespace message {

	constexpr const char* TYPE = "type";

	const int SYNC = 1;
	const int START = 2;
	const int STOP = 3;
	const int CONNECT = 4;
	const int SHOW_ALL = 5;
	const int SHOW = 6;
	const int ENABLE_STREAM = 7;
	const int IS_ALIVE = 8;
	const int SEND_PARAMETERS = 9;
	const int KILL = 10;
	const int STATUS = 11;
	const int ALL_AVAILABLE = 12;
	const int OUTPUT = 13;
	const int SET_STATUS = 14;
	const int GET_STATUS = 15;
	const int CREATE_PUBLISHER = 16;
	const int TERMINATE_PUBLISHER = 17;
	const int CONNECT_PUBLISHER = 18;
	const int SUBSCRIBE_PUBLISHER = 19;
	const int CANCEL = 20;
	const int SET_RESULT = 21;
	const int REQUEST_PORT = 22;
	const int CONNECT_PORT = 23;
	const int REMOVE_PORT = 24;
	const int REQUEST = 25;
	const int RESPONSE = 26;
	const int STARTED_UNMANAGED = 27;
	const int TERMINATED_UNMANAGED = 28;

	namespace StartRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* ARGS = "args"; // repeated string args = 2;
		constexpr const char* INSTANCE_REFERENCE = "instanceReference"; // required string instanceReference = 3;
	}

	namespace RequestResponse {
		constexpr const char* VALUE = "value"; // required int32 value = 1;
		constexpr const char* MESSAGE = "message"; // optional string message = 2;
	}

	namespace StopRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace ConnectRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
	}

	namespace ApplicationConfig {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* DESCRIPTION = "description"; // optional string description = 2;
		constexpr const char* RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		constexpr const char* RESTART = "restart"; // required bool restart = 4;
		constexpr const char* STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		constexpr const char* RETRIES = "retries"; // required int32 retries = 6;
		constexpr const char* STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	}

	namespace AllAvailableResponse {
		constexpr const char* APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	}

	namespace StatusEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		constexpr const char* PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
	}

	namespace PublisherEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* PUBLISHER_NAME = "publisherName"; // required string publisherName = 3;
	}

	namespace ResultEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* DATA = "data"; // required bytes data = 3;
	}

	namespace PortEvent {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* PORT_NAME = "portName"; // required string portName = 3;
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

	namespace ShowStreamRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
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
	}

	namespace SendParametersRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* PARAMETERS = "parameters"; // repeated string parameters = 2;
	}

	namespace KillRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}

	namespace OutputRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
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

	namespace RequestPortRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
	}

	namespace ConnectPortRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
	}

	namespace RemovePortRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
	}

	namespace CreatePublisherRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
		constexpr const char* NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // required int32 numberOfSubscribers = 3;
	}

	namespace TerminatePublisherRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
		constexpr const char* NAME = "name"; // required string name = 2;
	}

	namespace ConnectPublisherRequest {
		constexpr const char* APPLICATION_ID = "applicationId"; // required int32 applicationId = 1;
		constexpr const char* PUBLISHER_NAME = "publisherName"; // required string publisherName = 2;
	}

	namespace PublisherResponse {
		constexpr const char* MESSAGE = "message"; // optional string message = 1;
		constexpr const char* PUBLISHER_PORT = "publisherPort"; // required int32 publisherPort = 2;
		constexpr const char* SYNCHRONIZER_PORT = "synchronizerPort"; // required int32 synchronizerPort = 3;
		constexpr const char* NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // optional int32 numberOfSubscribers = 4;
	}

	namespace Request {
		constexpr const char* APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		constexpr const char* APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		constexpr const char* SERVER_URL = "serverUrl"; // required string serverUrl = 5;
		constexpr const char* SERVER_PORT = "serverPort"; // required int32 serverPort = 6;
		constexpr const char* REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	}

	namespace StartedUnmanagedRequest {
		constexpr const char* NAME = "name"; // required string name = 1;
		constexpr const char* PID = "pid"; // optional int64 pid = 2;
	}

	namespace TerminatedUnmanagedRequest {
		constexpr const char* ID = "id"; // required int32 id = 1;
	}
}

}

#endif
