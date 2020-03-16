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

	const std::string TYPE = "type";

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
		const std::string NAME = "name"; // required string name = 1;
		const std::string ARGS = "args"; // repeated string args = 2;
		const std::string INSTANCE_REFERENCE = "instanceReference"; // required string instanceReference = 3;
	}

	namespace RequestResponse {
		const std::string VALUE = "value"; // required int32 value = 1;
		const std::string MESSAGE = "message"; // optional string message = 2;
	}

	namespace StopRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}

	namespace ConnectRequest {
		const std::string NAME = "name"; // required string name = 1;
	}

	namespace ApplicationConfig {
		const std::string NAME = "name"; // required string name = 1;
		const std::string DESCRIPTION = "description"; // optional string description = 2;
		const std::string RUNS_SINGLE = "runsSingle"; // required bool runsSingle = 3;
		const std::string RESTART = "restart"; // required bool restart = 4;
		const std::string STARTING_TIME = "startingTime"; // required int32 startingTime = 5;
		const std::string RETRIES = "retries"; // required int32 retries = 6;
		const std::string STOPPING_TIME = "stoppingTime"; // required int32 stoppingTime = 7;
	}

	namespace AllAvailableResponse {
		const std::string APPLICATION_CONFIG = "applicationConfig"; // repeated ApplicationConfig applicationConfig = 1;
	}

	namespace StatusEvent {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		const std::string PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
	}

	namespace PublisherEvent {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string PUBLISHER_NAME = "publisherName"; // required string publisherName = 3;
	}

	namespace ResultEvent {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string DATA = "data"; // required bytes data = 3;
	}

	namespace PortEvent {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string PORT_NAME = "portName"; // required string portName = 3;
	}

	namespace ApplicationInfo {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string APPLICATION_STATE = "applicationState"; // required int32 applicationState = 3;
		const std::string PAST_APPLICATION_STATES = "pastApplicationStates"; // required int32 pastApplicationStates = 4;
		const std::string ARGS = "args"; // required string args = 5;
		const std::string PID = "pid"; // optional int64 pid = 6;
	}

	namespace ApplicationInfoListResponse {
		const std::string APPLICATION_INFO = "applicationInfo"; // repeated ApplicationInfo applicationInfo = 1;
	}

	namespace ShowStreamRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}

	namespace IsAliveRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}

	namespace IsAliveResponse {
		const std::string IS_ALIVE = "isAlive"; // required bool isAlive = 1;
	}

	namespace ApplicationStream {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string MESSAGE = "message"; // required string message = 2;
	}

	namespace SendParametersRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string PARAMETERS = "parameters"; // repeated string parameters = 2;
	}

	namespace KillRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}

	namespace OutputRequest {
		const std::string NAME = "name"; // required string name = 1;
	}

	namespace SetStatusRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string APPLICATION_STATE = "applicationState"; // required int32 applicationState = 2;
	}

	namespace GetStatusRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}

	namespace SetResultRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string DATA = "data"; // required bytes data = 2;
	}

	namespace RequestPortRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
	}

	namespace ConnectPortRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
	}

	namespace RemovePortRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
	}

	namespace CreatePublisherRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
		const std::string NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // required int32 numberOfSubscribers = 3;
	}

	namespace TerminatePublisherRequest {
		const std::string ID = "id"; // required int32 id = 1;
		const std::string NAME = "name"; // required string name = 2;
	}

	namespace ConnectPublisherRequest {
		const std::string APPLICATION_ID = "applicationId"; // required int32 applicationId = 1;
		const std::string PUBLISHER_NAME = "publisherName"; // required string publisherName = 2;
	}

	namespace PublisherResponse {
		const std::string MESSAGE = "message"; // optional string message = 1;
		const std::string PUBLISHER_PORT = "publisherPort"; // required int32 publisherPort = 2;
		const std::string SYNCHRONIZER_PORT = "synchronizerPort"; // required int32 synchronizerPort = 3;
		const std::string NUMBER_OF_SUBSCRIBERS = "numberOfSubscribers"; // optional int32 numberOfSubscribers = 4;
	}

	namespace Request {
		const std::string APPLICATION_NAME = "applicationName"; // required string applicationName = 1;
		const std::string APPLICATION_ID = "applicationId"; // required int32 applicationId = 2;
		const std::string SERVER_URL = "serverUrl"; // required string serverUrl = 5;
		const std::string SERVER_PORT = "serverPort"; // required int32 serverPort = 6;
		const std::string REQUESTER_PORT = "requesterPort"; // required int32 requesterPort = 7;
	}

	namespace StartedUnmanagedRequest {
		const std::string NAME = "name"; // required string name = 1;
		const std::string PID = "pid"; // optional int64 pid = 2;
	}

	namespace TerminatedUnmanagedRequest {
		const std::string ID = "id"; // required int32 id = 1;
	}
}

}

#endif

