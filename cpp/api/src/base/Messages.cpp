/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "JSON.h"
#include "Messages.h"

namespace cameo {

std::string createSyncRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SYNC);

	return request.dump();
}

std::string createSyncStreamRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SYNC_STREAM);

	request.pushKey(message::SyncStreamRequest::NAME);
	request.pushValue(name);

	return request.dump();
}

std::string createVersionRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::IMPL_VERSION);

	return request.dump();
}

std::string createIsAliveRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::IS_ALIVE);

	request.pushKey(message::IsAliveRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& thisName, int thisId, const std::string& thisEndpoint, int thisProxyPort, bool linked) {

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

		request.pushKey(message::StartRequest::STARTER_PROXY_PORT);
		request.pushValue(thisProxyPort);

		request.pushKey(message::StartRequest::STARTER_LINKED);
		request.pushValue(linked);
	}

	return request.dump();
}

std::string createSetStopHandlerRequest(int id, int stoppingTime) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_STOP_HANDLER);

	request.pushKey(message::SetStopHandlerRequest::ID);
	request.pushValue(id);

	request.pushKey(message::SetStopHandlerRequest::STOPPING_TIME);
	request.pushValue(stoppingTime);

	return request.dump();
}

std::string createStopRequest(int id, bool link) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::STOP);

	request.pushKey(message::StopRequest::ID);
	request.pushValue(id);

	request.pushKey(message::StopRequest::LINK);
	request.pushValue(link);

	return request.dump();
}

std::string createKillRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::KILL);

	request.pushKey(message::KillRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createConnectRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CONNECT);

	request.pushKey(message::ConnectRequest::NAME);
	request.pushValue(name);

	return request.dump();
}

std::string createConnectWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CONNECT_WITH_ID);

	request.pushKey(message::ConnectWithIdRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createListRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::LIST);

	return request.dump();
}

std::string createAppsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::APPS);

	return request.dump();
}

std::string createStreamStatusRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::STATUS);

	return request.dump();
}

std::string createResponderProxyPortRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RESPONDER_PROXY_PORT);

	return request.dump();
}

std::string createPublisherProxyPortRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PUBLISHER_PROXY_PORT);

	return request.dump();
}

std::string createSubscriberProxyPortRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SUBSCRIBER_PROXY_PORT);

	return request.dump();
}

std::string createOutputPortWithIdRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::OUTPUT_PORT_WITH_ID);

	request.pushKey(message::OutputPortWithIdRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createOutputPortRequest(const std::string& name) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::OUTPUT_PORT);

	request.pushKey(message::OutputPortRequest::NAME);
	request.pushValue(name);

	return request.dump();
}

std::string createSetStatusRequest(int id, int32_t state) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_STATUS);

	request.pushKey(message::SetStatusRequest::ID);
	request.pushValue(id);

	request.pushKey(message::SetStatusRequest::APPLICATION_STATE);
	request.pushValue(state);

	return request.dump();
}

std::string createGetStatusRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::GET_STATUS);

	request.pushKey(message::GetStatusRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createSetResultRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::SET_RESULT);

	request.pushKey(message::SetResultRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createAttachUnregisteredRequest(const std::string& name, long pid) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::ATTACH_UNREGISTERED);

	request.pushKey(message::AttachUnregisteredRequest::NAME);
	request.pushValue(name);

	request.pushKey(message::AttachUnregisteredRequest::PID);
	request.pushValue(pid);

	return request.dump();
}

std::string createDetachUnregisteredRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::DETACH_UNREGISTERED);

	request.pushKey(message::DetachUnregisteredRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createRequestResponse(int32_t value, const std::string& message) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushValue(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushValue(message);

	return request.dump();
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

	return request.dump();
}

std::string createGetKeyValueRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::GET_KEY_VALUE);

	request.pushKey(message::GetKeyValueRequest::ID);
	request.pushValue(id);

	request.pushKey(message::GetKeyValueRequest::KEY);
	request.pushValue(key);

	return request.dump();
}

std::string createRemoveKeyRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REMOVE_KEY);

	request.pushKey(message::RemoveKeyRequest::ID);
	request.pushValue(id);

	request.pushKey(message::RemoveKeyRequest::KEY);
	request.pushValue(key);

	return request.dump();
}

std::string createRequestPortRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST_PORT);

	request.pushKey(message::RequestPortRequest::ID);
	request.pushValue(id);

	return request.dump();
}

std::string createPortUnavailableRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PORT_UNAVAILABLE);

	request.pushKey(message::PortUnavailableRequest::ID);
	request.pushValue(id);

	request.pushKey(message::PortUnavailableRequest::PORT);
	request.pushValue(port);

	return request.dump();
}

std::string createReleasePortRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RELEASE_PORT);

	request.pushKey(message::ReleasePortRequest::ID);
	request.pushValue(id);

	request.pushKey(message::ReleasePortRequest::PORT);
	request.pushValue(port);

	return request.dump();
}

std::string createPortsRequest() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::PORTS);

	return request.dump();
}

}