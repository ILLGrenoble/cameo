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

#include <iostream>
#include <sstream>
#include "../SocketException.h"
#include "../ConnectionTimeout.h"
#include "RequestSocketImpl.h"

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

const std::string ServicesImpl::STATUS = "STATUS";
const std::string ServicesImpl::RESULT = "RESULT";
const std::string ServicesImpl::PUBLISHER = "PUBLISHER";
const std::string ServicesImpl::PORT = "PORT";
const std::string ServicesImpl::CANCEL = "CANCEL";
const std::string ServicesImpl::STREAM = "STREAM";
const std::string ServicesImpl::ENDSTREAM = "ENDSTREAM";

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

/**
 * convert enum type into Proto type
 */
proto::MessageType_Type ServicesImpl::convertToProtoType(ProtoType type) const {
	if (type == PROTO_INIT) {
		return proto::MessageType_Type_INIT;
	} else if (type == PROTO_ISALIVE) {
		return proto::MessageType_Type_ISALIVE;
	} else if (type == PROTO_SENDPARAMETERS) {
		return proto::MessageType_Type_SENDPARAMETERS;
	} else if (type == PROTO_SHOW) {
		return proto::MessageType_Type_SHOW;
	} else if (type == PROTO_STATUS) {
		return proto::MessageType_Type_STATUS;
	} else if (type == PROTO_SHOWALL) {
		return proto::MessageType_Type_SHOWALL;
	} else if (type == PROTO_START) {
		return proto::MessageType_Type_START;
	} else if (type == PROTO_STOP) {
		return proto::MessageType_Type_STOP;
	} else if (type == PROTO_KILL) {
		return proto::MessageType_Type_KILL;
	} else if (type == PROTO_CONNECT) {
		return proto::MessageType_Type_CONNECT;
	} else if (type == PROTO_ALLAVAILABLE) {
		return proto::MessageType_Type_ALLAVAILABLE;
	} else if (type == PROTO_SETSTATUS) {
		return proto::MessageType_Type_SETSTATUS;
	} else if (type == PROTO_GETSTATUS) {
		return proto::MessageType_Type_GETSTATUS;
	} else if (type == PROTO_SETRESULT) {
		return proto::MessageType_Type_SETRESULT;
	} else if (type == PROTO_CREATEPUBLISHER) {
		return proto::MessageType_Type_CREATEPUBLISHER;
	} else if (type == PROTO_CONNECTPUBLISHER) {
		return proto::MessageType_Type_CONNECTPUBLISHER;
	} else if (type == PROTO_SUBSCRIBEPUBLISHER) {
		return proto::MessageType_Type_SUBSCRIBEPUBLISHER;
	} else if (type == PROTO_TERMINATEPUBLISHER) {
		return proto::MessageType_Type_TERMINATEPUBLISHER;
	} else if (type == PROTO_REQUESTPORT) {
		return proto::MessageType_Type_REQUESTPORT;
	} else if (type == PROTO_CONNECTPORT) {
		return proto::MessageType_Type_CONNECTPORT;
	} else if (type == PROTO_REMOVEPORT) {
		return proto::MessageType_Type_REMOVEPORT;
	} else if (type == PROTO_REQUEST) {
		return proto::MessageType_Type_REQUEST;
	} else if (type == PROTO_RESPONSE) {
		return proto::MessageType_Type_RESPONSE;
	} else if (type == PROTO_CANCEL) {
		return proto::MessageType_Type_CANCEL;
	} else if (type == PROTO_STARTEDUNMANAGED) {
		return proto::MessageType_Type_STARTEDUNMANAGED;
	} else if (type == PROTO_TERMINATEDUNMANAGED) {
		return proto::MessageType_Type_TERMINATEDUNMANAGED;
	} else if (type == PROTO_OUTPUT) {
		return proto::MessageType_Type_OUTPUT;
	} else {
		cerr << "unsupported proto type" << endl;
		return proto::MessageType_Type(0);
	}

}

std::string ServicesImpl::createIsAliveRequest(int id) const {
	proto::IsAliveCommand isAliveCommand;
	isAliveCommand.set_id(id);
	std::string strRequestoIsAlive;
	isAliveCommand.SerializeToString(&strRequestoIsAlive);
	return strRequestoIsAlive;
}

std::string ServicesImpl::createInitRequest() const {
	std::string strRequestData;
	proto::Init initType;
	initType.SerializeToString(&strRequestData);
	return strRequestData;
}

std::string ServicesImpl::createRequestType(ProtoType type) const {
	proto::MessageType messageType;
	messageType.set_type(convertToProtoType(type));
	std::string strRequestType;
	messageType.SerializeToString(&strRequestType);
	return strRequestType;

}

std::string ServicesImpl::createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& instanceReference) const {
	proto::StartCommand startCommand;
	startCommand.set_name(name);
	for (unsigned int j = 0; j < args.size(); j++) {
		startCommand.add_args(args[j]);
	}
	startCommand.set_instancereference(instanceReference);
	std::string strRequestStart;
	startCommand.SerializeToString(&strRequestStart);
	return strRequestStart;
}

std::string ServicesImpl::createStopRequest(int id) const {
	proto::StopCommand stopCommand;
	stopCommand.set_id(id);
	std::string strRequestStop;
	stopCommand.SerializeToString(&strRequestStop);
	return strRequestStop;
}

std::string ServicesImpl::createKillRequest(int id) const {
	proto::KillCommand killCommand;
	killCommand.set_id(id);
	std::string strRequestKill;
	killCommand.SerializeToString(&strRequestKill);
	return strRequestKill;
}

std::string ServicesImpl::createConnectRequest(const std::string& name) const {
	proto::ConnectCommand connectCommand;
	connectCommand.set_name(name);
	std::string strConnect;
	connectCommand.SerializeToString(&strConnect);
	return strConnect;
}

std::string ServicesImpl::createAllAvailableRequest() const {
	proto::AllAvailableCommand allAvailableCommand;
	std::string strRequestAllAvailable;
	allAvailableCommand.SerializeToString(&strRequestAllAvailable);
	return strRequestAllAvailable;
}

std::string ServicesImpl::createShowAllRequest() const {
	proto::ShowAllCommand showAllCommand;
	std::string strRequestShowAll;
	showAllCommand.SerializeToString(&strRequestShowAll);
	return strRequestShowAll;
}

std::string ServicesImpl::createShowStatusRequest() const {
	return "status";
}

zmq::socket_t * ServicesImpl::createEventSubscriber(const std::string& endpoint, const std::string& cancelEndpoint) {

	zmq::socket_t * subscriber = new zmq::socket_t(m_context, ZMQ_SUB);

	vector<string> streamList;
	streamList.push_back(STATUS);
	streamList.push_back(RESULT);
	streamList.push_back(PUBLISHER);
	streamList.push_back(PORT);
	streamList.push_back(CANCEL);

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
	streamList.push_back(STREAM);
	streamList.push_back(ENDSTREAM);
	streamList.push_back(CANCEL);

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

std::string ServicesImpl::createShowStreamRequest(int id) const {
	proto::ShowStreamCommand showStreamCommand;
	showStreamCommand.set_id(id);
	std::string strRequestShowStream;
	showStreamCommand.SerializeToString(&strRequestShowStream);
	return strRequestShowStream;
}

std::string ServicesImpl::createSetStatusRequest(int id, int32_t state) const {
	proto::SetStatusCommand setStatusCommand;
	setStatusCommand.set_id(id);
	setStatusCommand.set_applicationstate(state);
	std::string strSetStatus;
	setStatusCommand.SerializeToString(&strSetStatus);

	return strSetStatus;
}

std::string ServicesImpl::createGetStatusRequest(int id) const {
	proto::GetStatusCommand getStatusCommand;
	getStatusCommand.set_id(id);
	std::string strGetStatus;
	getStatusCommand.SerializeToString(&strGetStatus);

	return strGetStatus;
}

std::string ServicesImpl::createSetResultRequest(int id, const std::string& data) const {
	proto::SetResultCommand setResultCommand;
	setResultCommand.set_id(id);
	setResultCommand.set_data(data);
	std::string strSetResult;
	setResultCommand.SerializeToString(&strSetResult);

	return strSetResult;
}

std::string ServicesImpl::createSubscribePublisherRequest() const {
	proto::SubscribePublisherCommand subscribePublisherCommand;
	std::string result;
	subscribePublisherCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createCreatePublisherRequest(int id, const std::string& name, int numberOfSubscribers) const {
	proto::CreatePublisherCommand createPublisherCommand;
	createPublisherCommand.set_id(id);
	createPublisherCommand.set_name(name);
	createPublisherCommand.set_numberofsubscribers(numberOfSubscribers);
	std::string result;
	createPublisherCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createConnectPublisherRequest(int id, const std::string& publisherName) const {
	proto::ConnectPublisherCommand connectPublisherCommand;
	connectPublisherCommand.set_applicationid(id);
	connectPublisherCommand.set_publishername(publisherName);
	std::string result;
	connectPublisherCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createTerminatePublisherRequest(int id, const std::string& name) const {
	proto::TerminatePublisherCommand terminatePublisherCommand;
	terminatePublisherCommand.set_id(id);
	terminatePublisherCommand.set_name(name);
	std::string result;
	terminatePublisherCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createRequestPortRequest(int id, const std::string& name) const {
	proto::RequestPortCommand requestPortCommand;
	requestPortCommand.set_id(id);
	requestPortCommand.set_name(name);
	std::string result;
	requestPortCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createConnectPortRequest(int id, const std::string& name) const {
	proto::ConnectPortCommand connectPortCommand;
	connectPortCommand.set_id(id);
	connectPortCommand.set_name(name);
	std::string result;
	connectPortCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createRemovePortRequest(int id, const std::string& name) const {
	proto::RemovePortCommand removePortCommand;
	removePortCommand.set_id(id);
	removePortCommand.set_name(name);
	std::string result;
	removePortCommand.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createStartedUnmanagedRequest(const std::string& name) const {

	// Get the pid.
	long pid = GET_PROCESS_PID();

	proto::StartedUnmanagedCommand command;
	command.set_name(name);
	command.set_pid(pid);
	std::string result;
	command.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createTerminatedUnmanagedRequest(int id) const {
	proto::TerminatedUnmanagedCommand command;
	command.set_id(id);
	std::string result;
	command.SerializeToString(&result);

	return result;
}

std::string ServicesImpl::createOutputRequest(const std::string& name) const {
	proto::OutputCommand command;
	command.set_name(name);
	std::string result;
	command.SerializeToString(&result);

	return result;
}

bool ServicesImpl::isAvailable(RequestSocketImpl * socket, int timeout) {

	string requestTypePart = createRequestType(PROTO_INIT);
	string requestDataPart = createInitRequest();

	try {
		unique_ptr<zmq::message_t> reply = socket->request(requestTypePart, requestDataPart, timeout);

		if (reply.get() != nullptr) {
			return true;
		}

	} catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}

	return false;
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
