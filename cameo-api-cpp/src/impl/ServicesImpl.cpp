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

#include "ServicesImpl.h"
#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "message/Message.h"
#include "RequestSocketImpl.h"
#include <iostream>
#include <sstream>
#include "JSON.h"

// Using Visual Studio preprocessor.
// It must be improved in case of other compilers.
#ifdef _WIN32
	#define GET_PROCESS_PID() GetCurrentProcessId()
#else
	#include <unistd.h>

	#define GET_PROCESS_PID() ::getpid()
#endif


using namespace std;

namespace cameo {

ServicesImpl::ServicesImpl() :
	m_context(1), m_timeout(0) {
}

ServicesImpl::~ServicesImpl() {
}

void ServicesImpl::setTimeout(int timeout) {
	m_timeout = timeout;
}

int ServicesImpl::getTimeout() const {
	return m_timeout;
}

std::string ServicesImpl::createSyncRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SYNC);

	return request.toString();
}

std::string ServicesImpl::createSyncStreamRequest(const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SYNC_STREAM);

	request.pushKey(message::SyncStreamRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createVersionRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::IMPL_VERSION);

	return request.toString();
}

std::string ServicesImpl::createIsAliveRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::IS_ALIVE);

	request.pushKey(message::IsAliveRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& instanceReference) const {

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

	request.pushKey(message::StartRequest::INSTANCE_REFERENCE);
	request.pushString(instanceReference);

	return request.toString();
}

std::string ServicesImpl::createStopRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::STOP);

	request.pushKey(message::StopRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createKillRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::KILL);

	request.pushKey(message::KillRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createConnectRequest(const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT);

	request.pushKey(message::ConnectRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createConnectWithIdRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_WITH_ID);

	request.pushKey(message::ConnectWithIdRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createListRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::LIST);

	return request.toString();
}

std::string ServicesImpl::createAppsRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::APPS);

	return request.toString();
}

std::string ServicesImpl::createStreamStatusRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::STATUS);

	return request.toString();
}

std::string ServicesImpl::createOutputPortWithIdRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::OUTPUT_PORT_WITH_ID);

	request.pushKey(message::OutputPortWithIdRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createOutputPortRequest(const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::OUTPUT_PORT);

	request.pushKey(message::OutputPortRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createSetStatusRequest(int id, int32_t state) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SET_STATUS);

	request.pushKey(message::SetStatusRequest::ID);
	request.pushInt(id);

	request.pushKey(message::SetStatusRequest::APPLICATION_STATE);
	request.pushInt(state);

	return request.toString();
}

std::string ServicesImpl::createGetStatusRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::GET_STATUS);

	request.pushKey(message::GetStatusRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createSetResultRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SET_RESULT);

	request.pushKey(message::SetResultRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createSubscribePublisherRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SUBSCRIBE_PUBLISHER_v0);

	return request.toString();
}

std::string ServicesImpl::createCreatePublisherRequest(int id, const std::string& name, int numberOfSubscribers) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CREATE_PUBLISHER_v0);

	request.pushKey(message::CreatePublisherRequest::ID);
	request.pushInt(id);

	request.pushKey(message::CreatePublisherRequest::NAME);
	request.pushString(name);

	request.pushKey(message::CreatePublisherRequest::NUMBER_OF_SUBSCRIBERS);
	request.pushInt(numberOfSubscribers);

	return request.toString();
}

std::string ServicesImpl::createConnectPublisherRequest(int id, const std::string& publisherName) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_PUBLISHER_v0);

	request.pushKey(message::ConnectPublisherRequest::APPLICATION_ID);
	request.pushInt(id);

	request.pushKey(message::ConnectPublisherRequest::PUBLISHER_NAME);
	request.pushString(publisherName);

	return request.toString();
}

std::string ServicesImpl::createTerminatePublisherRequest(int id, const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::TERMINATE_PUBLISHER_v0);

	request.pushKey(message::TerminatePublisherRequest::ID);
	request.pushInt(id);

	request.pushKey(message::TerminatePublisherRequest::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createRequestPortV0Request(int id, const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST_PORT_v0);

	request.pushKey(message::RequestPortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::RequestPortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createConnectPortV0Request(int id, const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_PORT_v0);

	request.pushKey(message::ConnectPortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::ConnectPortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createRemovePortV0Request(int id, const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REMOVE_PORT_v0);

	request.pushKey(message::RemovePortV0Request::ID);
	request.pushInt(id);

	request.pushKey(message::RemovePortV0Request::NAME);
	request.pushString(name);

	return request.toString();
}

std::string ServicesImpl::createAttachUnmanagedRequest(const std::string& name) const {

	// Get the pid.
	long pid = GET_PROCESS_PID();

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::ATTACH_UNMANAGED);

	request.pushKey(message::AttachUnmanagedRequest::NAME);
	request.pushString(name);

	request.pushKey(message::AttachUnmanagedRequest::PID);
	request.pushInt64(pid);

	return request.toString();
}

std::string ServicesImpl::createDetachUnmanagedRequest(int id) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::DETACH_UNMANAGED);

	request.pushKey(message::DetachUnmanagedRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createRequestResponse(int64_t value) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushInt64(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushString("");

	return request.toString();
}

std::string ServicesImpl::createRequestResponse(int64_t value, const std::string& message) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RESPONSE);

	request.pushKey(message::RequestResponse::VALUE);
	request.pushInt64(value);

	request.pushKey(message::RequestResponse::MESSAGE);
	request.pushString(message);

	return request.toString();
}


std::string ServicesImpl::createStoreKeyValueRequest(int id, const std::string& key, const std::string& value) {

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

std::string ServicesImpl::createGetKeyValueRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::GET_KEY_VALUE);

	request.pushKey(message::GetKeyValueRequest::ID);
	request.pushInt(id);

	request.pushKey(message::GetKeyValueRequest::KEY);
	request.pushString(key);

	return request.toString();
}

std::string ServicesImpl::createRemoveKeyRequest(int id, const std::string& key) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REMOVE_KEY);

	request.pushKey(message::RemoveKeyRequest::ID);
	request.pushInt(id);

	request.pushKey(message::RemoveKeyRequest::KEY);
	request.pushString(key);

	return request.toString();
}

std::string ServicesImpl::createRequestPortRequest(int id) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST_PORT);

	request.pushKey(message::RequestPortRequest::ID);
	request.pushInt(id);

	return request.toString();
}

std::string ServicesImpl::createPortUnavailableRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::PORT_UNAVAILABLE);

	request.pushKey(message::PortUnavailableRequest::ID);
	request.pushInt(id);

	request.pushKey(message::PortUnavailableRequest::PORT);
	request.pushInt(port);

	return request.toString();
}

std::string ServicesImpl::createReleasePortRequest(int id, int port) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RELEASE_PORT);

	request.pushKey(message::ReleasePortRequest::ID);
	request.pushInt(id);

	request.pushKey(message::ReleasePortRequest::PORT);
	request.pushInt(port);

	return request.toString();
}

zmq::socket_t * ServicesImpl::createEventSubscriber(const std::string& endpoint, const std::string& cancelEndpoint) {

	zmq::socket_t * subscriber = new zmq::socket_t(m_context, ZMQ_SUB);

	vector<string> streamList;
	streamList.push_back(message::Event::STATUS);
	streamList.push_back(message::Event::RESULT);
	streamList.push_back(message::Event::PUBLISHER);
	streamList.push_back(message::Event::PORT);
	streamList.push_back(message::Event::KEYVALUE);
	streamList.push_back(message::Event::CANCEL);

	for (vector<string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		subscriber->setsockopt(ZMQ_SUBSCRIBE, s->c_str(), s->length());
	}

	subscriber->connect(endpoint.c_str());
	subscriber->connect(cancelEndpoint.c_str());

	return subscriber;
}

zmq::socket_t * ServicesImpl::createOutputStreamSubscriber(const std::string& endpoint, const std::string& cancelEndpoint) {

	zmq::socket_t * subscriber = new zmq::socket_t(m_context, ZMQ_SUB);

	vector<string> streamList;
	streamList.push_back(message::Event::SYNCSTREAM);
	streamList.push_back(message::Event::STREAM);
	streamList.push_back(message::Event::ENDSTREAM);
	streamList.push_back(message::Event::CANCEL);

	for (vector<string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		subscriber->setsockopt(ZMQ_SUBSCRIBE, s->c_str(), s->length());
	}

	subscriber->connect(endpoint.c_str());
	subscriber->connect(cancelEndpoint.c_str());

	return subscriber;
}

zmq::socket_t * ServicesImpl::createCancelPublisher(const std::string& endpoint) {

	zmq::socket_t * publisher = new zmq::socket_t(m_context, ZMQ_PUB);
	publisher->bind(endpoint.c_str());

	return publisher;
}

zmq::socket_t * ServicesImpl::createRequestSocket(const std::string& endpoint) {

	zmq::socket_t* socket = new zmq::socket_t(m_context, ZMQ_REQ);

	try {
		// Set the linger value to 0 to ensure that pending requests are destroyed in case of timeout.
		int value = 0;
		socket->setsockopt(ZMQ_LINGER, &value, sizeof(int));

		// Connect to the endpoint.
		socket->connect(endpoint.c_str());
	}
	catch (exception const & e) {
		throw SocketException(e.what());
	}

	return socket;
}

bool ServicesImpl::isAvailable(RequestSocketImpl * socket, int timeout) {

	string request = createSyncRequest();

	try {
		unique_ptr<zmq::message_t> reply = socket->request(request, timeout);

		if (reply.get() != nullptr) {
			return true;
		}
	}
	catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}
	catch (...) {
		// Should not happen.
	}

	return false;
}

void ServicesImpl::sendSyncStream(RequestSocketImpl * socket, const std::string& name) {

	string request = createSyncStreamRequest(name);

	try {
		unique_ptr<zmq::message_t> reply = socket->request(request);
	}
	catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}
	catch (...) {
		// Should not happen.
	}
}

void ServicesImpl::waitForStreamSubscriber(zmq::socket_t * subscriber, RequestSocketImpl * socket, const std::string& name) {

	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*subscriber);
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		sendSyncStream(socket, name);

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

void ServicesImpl::waitForSubscriber(zmq::socket_t * subscriber, RequestSocketImpl * socket) {

	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*subscriber);
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		isAvailable(socket, 100);

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

}
