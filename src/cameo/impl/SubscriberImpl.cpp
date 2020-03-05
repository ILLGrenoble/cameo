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

#include "SubscriberImpl.h"

#include <sstream>

#include "../Serializer.h"
#include "CancelIdGenerator.h"
#include "ServicesImpl.h"
#include "RequestSocketImpl.h"
#include "../Server.h"

using namespace std;

namespace cameo {

const std::string SubscriberImpl::SYNC = "SYNC";
const std::string SubscriberImpl::STREAM = "STREAM";
const std::string SubscriberImpl::ENDSTREAM = "ENDSTREAM";
const std::string SubscriberImpl::CANCEL = "CANCEL";
const std::string SubscriberImpl::STATUS = "STATUS";

SubscriberImpl::SubscriberImpl(Server * server, const std::string & url, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint) :
	m_server(server),
	m_url(url),
	m_publisherName(publisherName),
	m_publisherPort(publisherPort),
	m_synchronizerPort(synchronizerPort),
	m_numberOfSubscribers(numberOfSubscribers),
	m_instanceName(instanceName),
	m_instanceId(instanceId),
	m_instanceEndpoint(instanceEndpoint),
	m_statusEndpoint(statusEndpoint),
	m_ended(false),
	m_canceled(false) {
}

SubscriberImpl::~SubscriberImpl() {
}

void SubscriberImpl::init() {

	// Create a socket for publishing.
	m_subscriber.reset(new zmq::socket_t(m_server->m_impl->m_context, ZMQ_SUB));
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, SYNC.c_str(), SYNC.length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, STREAM.c_str(), STREAM.length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, ENDSTREAM.c_str(), ENDSTREAM.length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, CANCEL.c_str(), CANCEL.length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, STATUS.c_str(), STATUS.length());

	stringstream pubEndpoint;
	pubEndpoint << m_url << ":" << m_publisherPort;

	m_subscriber->connect(pubEndpoint.str().c_str());

	// We must first bind the cancel publisher before connecting the subscriber.
	stringstream cancelEndpoint;

	// We define a unique name.
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();
	m_cancelEndpoint = cancelEndpoint.str();

	m_cancelPublisher = unique_ptr<zmq::socket_t>(new zmq::socket_t(m_server->m_impl->m_context, ZMQ_PUB));
	m_cancelPublisher->bind(m_cancelEndpoint.c_str());

	m_subscriber->connect(m_cancelEndpoint.c_str());
	m_subscriber->connect(m_statusEndpoint.c_str());

	// Synchronize the subscriber only if the number of subscribers > 0.
	if (m_numberOfSubscribers > 0) {

		stringstream syncEndpoint;
		syncEndpoint << m_url << ":" << m_synchronizerPort;

		string endpoint = syncEndpoint.str();

		// Create a request socket.
		unique_ptr<RequestSocketImpl> requestSocket = m_server->createRequestSocket(endpoint);

		// Poll subscriber.
		zmq_pollitem_t items[1];
		items[0].socket = static_cast<void *>(*m_subscriber);
		items[0].fd = 0;
		items[0].events = ZMQ_POLLIN;
		items[0].revents = 0;

		while (true) {
			m_server->m_impl->isAvailable(requestSocket.get(), 100);

			// Wait for 100ms.
			int rc = zmq::poll(items, 1, 100);
			if (rc != 0) {
				break;
			}
		}

		unique_ptr<zmq::message_t> reply = requestSocket->request(m_server->m_impl->createRequestType(PROTO_SUBSCRIBEPUBLISHER), m_server->m_impl->createSubscribePublisherRequest());
		proto::RequestResponse requestResponse;
		requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	}
}

bool SubscriberImpl::isEnded() const {
	return m_ended;
}

bool SubscriberImpl::isCanceled() const {
	return m_canceled;
}

bool SubscriberImpl::receiveBinary(std::string& data) {

	while (true) {
		unique_ptr<zmq::message_t> message(new zmq::message_t());
		m_subscriber->recv(message.get());

		string response(static_cast<char*>(message->data()), message->size());

		if (response == STREAM) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			data = string(static_cast<char*>(message->data()), message->size());

			return true;

		} else if (response == ENDSTREAM) {
			m_ended = true;
			return false;

		} else if (response == CANCEL) {
			m_canceled = true;
			return false;

		} else if (response == STATUS) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());

			proto::StatusEvent protoStatus;
			protoStatus.ParseFromArray(message->data(), message->size());

			if (protoStatus.id() == m_instanceId) {
				application::State state = protoStatus.applicationstate();

				// test the terminal state
				if (state == application::SUCCESS
					|| state == application::STOPPED
					|| state == application::KILLED
					|| state == application::FAILURE) {
					// Exit because the remote application has terminated.
					return false;
				}
			}
		}
	}

	return false;
}

bool SubscriberImpl::receive(std::string& data) {

	string bytes;
	bool stream = receiveBinary(bytes);

	if (!stream) {
		return false;
	}

	parse(bytes, data);

	return true;
}

bool SubscriberImpl::receive(std::vector<int32_t>& data) {

	string bytes;
	bool stream = receiveBinary(bytes);

	if (!stream) {
		return false;
	}

	parse(bytes, data);

	return true;
}

bool SubscriberImpl::receive(std::vector<int64_t>& data) {

	string bytes;
	bool stream = receiveBinary(bytes);

	if (!stream) {
		return false;
	}

	parse(bytes, data);

	return true;
}

bool SubscriberImpl::receive(std::vector<float>& data) {

	string bytes;
	bool stream = receiveBinary(bytes);

	if (!stream) {
		return false;
	}

	parse(bytes, data);

	return true;
}

bool SubscriberImpl::receive(std::vector<double>& data) {

	string bytes;
	bool stream = receiveBinary(bytes);

	if (!stream) {
		return false;
	}

	parse(bytes, data);

	return true;
}

bool SubscriberImpl::receiveTwoBinaryParts(std::string& data1, std::string& data2) {

	while (true) {
		unique_ptr<zmq::message_t> message(new zmq::message_t());
		m_subscriber->recv(message.get());

		string response(static_cast<char*>(message->data()), message->size());

		if (response == STREAM) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			data1 = string(static_cast<char*>(message->data()), message->size());

			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			data2 = string(static_cast<char*>(message->data()), message->size());

			return true;

		} else if (response == ENDSTREAM) {
			m_ended = true;
			return false;

		} else if (response == CANCEL) {
			return false;

		} else if (response == STATUS) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());

			proto::StatusEvent protoStatus;
			protoStatus.ParseFromArray(message->data(), message->size());

			if (protoStatus.id() == m_instanceId) {
				application::State state = protoStatus.applicationstate();

				// test the terminal state
				if (state == application::SUCCESS
					|| state == application::STOPPED
					|| state == application::KILLED
					|| state == application::FAILURE) {
					// Exit because the remote application has terminated.
					return false;
				}
			}
		}
	}

	return false;
}

WaitingImpl * SubscriberImpl::waiting() {

	// Waiting gets the cancel publisher.
	return new SocketWaitingImpl(m_cancelPublisher.get(), CANCEL);
}

}
