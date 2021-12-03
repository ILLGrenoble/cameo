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

#include "JSON.h"
#include "Messages.h"

namespace cameo {

std::string createSyncRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SYNC);

	return request.toString();
}

std::string createSyncStreamRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SYNC_STREAM);

	request.pushKey(message::SyncStreamRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createVersionRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::IMPL_VERSION);

	return request.toString();
}

std::string createIsAliveRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::IS_ALIVE);

	request.pushKey(message::IsAliveRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::START);

	request.pushKey(message::StartRequest::NAME);
	request.pushString(name);

	request.pushKey(message::StartRequest::ARGS);
	request.startArray();
	for (auto& a : args) {
		request.pushString(a);
	}
	request.endArray();

	if (thisId != -1) {
		request.pushKey(message::StartRequest::STARTER);

		request.startObject();

		request.pushKey(message::ApplicationIdentity::NAME);
		request.pushString(thisName);

		request.pushKey(message::ApplicationIdentity::ID);
		request.pushInt(thisId);

		request.pushKey(message::ApplicationIdentity::SERVER);
		request.pushString(thisEndpoint);

		request.endObject();
	}

	return request.toString();
}

std::string createSetStopHandlerRequest(int id, int stoppingTime) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SET_STOP_HANDLER);

	request.pushKey(message::SetStopHandlerRequest::ID);
	request.pushInt(id);

	request.pushKey(message::SetStopHandlerRequest::STOPPING_TIME);
	request.pushInt(stoppingTime);

	return request.toString();
}

std::string createStopRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::STOP);

	request.pushKey(message::StopRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createKillRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::KILL);

	request.pushKey(message::KillRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createConnectRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT);

	request.pushKey(message::ConnectRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createConnectWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_WITH_ID);

	request.pushKey(message::ConnectWithIdRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createListRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::LIST);

	return request.toString();
}

std::string createAppsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::APPS);

	return request.toString();
}

std::string createStreamStatusRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::STATUS);

	return request.toString();
}

std::string createOutputPortWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::OUTPUT_PORT_WITH_ID);

	request.pushKey(message::OutputPortWithIdRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createOutputPortRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::OUTPUT_PORT);

	request.pushKey(message::OutputPortRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createSetStatusRequest(int id, int32_t state) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SET_STATUS);

	request.pushKey(message::SetStatusRequest::ID);
	request.pushInt(id);

	request.pushKey(message::SetStatusRequest::APPLICATION_STATE);
	request.pushInt(state);

	return request.toString();
}

std::string createGetStatusRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::GET_STATUS);

	request.pushKey(message::GetStatusRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createSetResultRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SET_RESULT);

	request.pushKey(message::SetResultRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createRequestPortV0Request(int id, const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST_PORT_v0);

	request.pushKey(message::RequestPortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::RequestPortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createConnectPortV0Request(int id, const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_PORT_v0);

	request.pushKey(message::ConnectPortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::ConnectPortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createRemovePortV0Request(int id, const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REMOVE_PORT_v0);

	request.pushKey(message::RemovePortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::RemovePortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string createAttachUnmanagedRequest(const std::string& name, long pid) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::ATTACH_UNMANAGED);

	request.pushKey(message::AttachUnmanagedRequest::NAME);
	request.pushString(name);

	request.pushKey(message::AttachUnmanagedRequest::PID);
	request.pushInt64(pid);

	return request.toString();
}

std::string createDetachUnmanagedRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::DETACH_UNMANAGED);

	request.pushKey(message::DetachUnmanagedRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createRequestResponse(int64_t value) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushInt64(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushString("");

	return request.toString();
}

std::string createRequestResponse(int64_t value, const std::string& message) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushInt64(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushString(message);

	return request.toString();
}


std::string createStoreKeyValueRequest(int id, const std::string& key, const std::string& value) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::STORE_KEY_VALUE);

	request.pushKey(message::StoreKeyValueRequest::ID);
	request.pushInt(id);

	request.pushKey(message::StoreKeyValueRequest::KEY);
	request.pushString(key);

	request.pushKey(message::StoreKeyValueRequest::VALUE);
	request.pushString(value);

	return request.toString();
}

std::string createGetKeyValueRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::GET_KEY_VALUE);

	request.pushKey(message::GetKeyValueRequest::ID);
	request.pushInt(id);

	request.pushKey(message::GetKeyValueRequest::KEY);
	request.pushString(key);

	return request.toString();
}

std::string createRemoveKeyRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REMOVE_KEY);

	request.pushKey(message::RemoveKeyRequest::ID);
	request.pushInt(id);

	request.pushKey(message::RemoveKeyRequest::KEY);
	request.pushString(key);

	return request.toString();
}

std::string createRequestPortRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST_PORT);

	request.pushKey(message::RequestPortRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string createPortUnavailableRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::PORT_UNAVAILABLE);

	request.pushKey(message::PortUnavailableRequest::ID);
	request.pushInt(id);

	request.pushKey(message::PortUnavailableRequest::PORT);
	request.pushInt(port);

	return request.toString();
}

std::string createReleasePortRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RELEASE_PORT);

	request.pushKey(message::ReleasePortRequest::ID);
	request.pushInt(id);

	request.pushKey(message::ReleasePortRequest::PORT);
	request.pushInt(port);

	return request.toString();
}

std::string createPortsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::PORTS);

	return request.toString();
}

}
