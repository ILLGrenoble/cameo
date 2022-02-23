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
	request.pushValue(message::SYNC);

	return request.toString();
}

std::string createSyncStreamRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SYNC_STREAM);

	request.pushKey(message::SyncStreamRequest::NAME);
	request.pushValue(name);

	return request.toString();
}

std::string createVersionRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::IMPL_VERSION);

	return request.toString();
}

std::string createIsAliveRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::IS_ALIVE);

	request.pushKey(message::IsAliveRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::START);

	request.pushKey(message::StartRequest::NAME);
	request.pushValue(name);

	request.pushKey(message::StartRequest::ARGS);
	request.startArray();
	for (auto& a : args) {
		request.pushValue(a);
	}
	request.endArray();

	if (thisId != -1) {
		request.pushKey(message::StartRequest::STARTER);

		request.startObject();

		request.pushKey(message::ApplicationIdentity::NAME);
		request.pushValue(thisName);

		request.pushKey(message::ApplicationIdentity::ID);
		request.pushValue(thisId);

		request.pushKey(message::ApplicationIdentity::SERVER);
		request.pushValue(thisEndpoint);

		request.endObject();
	}

	return request.toString();
}

std::string createSetStopHandlerRequest(int id, int stoppingTime) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_STOP_HANDLER);

	request.pushKey(message::SetStopHandlerRequest::ID);
	request.pushValue(id);

	request.pushKey(message::SetStopHandlerRequest::STOPPING_TIME);
	request.pushValue(stoppingTime);

	return request.toString();
}

std::string createStopRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::STOP);

	request.pushKey(message::StopRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createKillRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::KILL);

	request.pushKey(message::KillRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createConnectRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CONNECT);

	request.pushKey(message::ConnectRequest::NAME);
	request.pushValue(name);

	return request.toString();
}

std::string createConnectWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CONNECT_WITH_ID);

	request.pushKey(message::ConnectWithIdRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createListRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::LIST);

	return request.toString();
}

std::string createAppsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::APPS);

	return request.toString();
}

std::string createStreamStatusRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::STATUS);

	return request.toString();
}

std::string createPublisherProxyPortRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PUBLISHER_PROXY_PORT);

	return request.toString();
}

std::string createSubscriberProxyPortRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SUBSCRIBER_PROXY_PORT);

	return request.toString();
}

std::string createOutputPortWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::OUTPUT_PORT_WITH_ID);

	request.pushKey(message::OutputPortWithIdRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createOutputPortRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::OUTPUT_PORT);

	request.pushKey(message::OutputPortRequest::NAME);
	request.pushValue(name);

	return request.toString();
}

std::string createSetStatusRequest(int id, int32_t state) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_STATUS);

	request.pushKey(message::SetStatusRequest::ID);
	request.pushValue(id);

	request.pushKey(message::SetStatusRequest::APPLICATION_STATE);
	request.pushValue(state);

	return request.toString();
}

std::string createGetStatusRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::GET_STATUS);

	request.pushKey(message::GetStatusRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createSetResultRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_RESULT);

	request.pushKey(message::SetResultRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createAttachUnregisteredRequest(const std::string& name, long pid) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::ATTACH_UNREGISTERED);

	request.pushKey(message::AttachUnregisteredRequest::NAME);
	request.pushValue(name);

	request.pushKey(message::AttachUnregisteredRequest::PID);
	request.pushValue(pid);

	return request.toString();
}

std::string createDetachUnregisteredRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::DETACH_UNREGISTERED);

	request.pushKey(message::DetachUnregisteredRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createRequestResponse(int32_t value, const std::string& message) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushValue(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushValue(message);

	return request.toString();
}

std::string createStoreKeyValueRequest(int id, const std::string& key, const std::string& value) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::STORE_KEY_VALUE);

	request.pushKey(message::StoreKeyValueRequest::ID);
	request.pushValue(id);

	request.pushKey(message::StoreKeyValueRequest::KEY);
	request.pushValue(key);

	request.pushKey(message::StoreKeyValueRequest::VALUE);
	request.pushValue(value);

	return request.toString();
}

std::string createGetKeyValueRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::GET_KEY_VALUE);

	request.pushKey(message::GetKeyValueRequest::ID);
	request.pushValue(id);

	request.pushKey(message::GetKeyValueRequest::KEY);
	request.pushValue(key);

	return request.toString();
}

std::string createRemoveKeyRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REMOVE_KEY);

	request.pushKey(message::RemoveKeyRequest::ID);
	request.pushValue(id);

	request.pushKey(message::RemoveKeyRequest::KEY);
	request.pushValue(key);

	return request.toString();
}

std::string createRequestPortRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST_PORT);

	request.pushKey(message::RequestPortRequest::ID);
	request.pushValue(id);

	return request.toString();
}

std::string createPortUnavailableRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PORT_UNAVAILABLE);

	request.pushKey(message::PortUnavailableRequest::ID);
	request.pushValue(id);

	request.pushKey(message::PortUnavailableRequest::PORT);
	request.pushValue(port);

	return request.toString();
}

std::string createReleasePortRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RELEASE_PORT);

	request.pushKey(message::ReleasePortRequest::ID);
	request.pushValue(id);

	request.pushKey(message::ReleasePortRequest::PORT);
	request.pushValue(port);

	return request.toString();
}

std::string createPortsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PORTS);

	return request.toString();
}

}
