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

#ifndef CAMEO_SERVICESIMPL_H_
#define CAMEO_SERVICESIMPL_H_

#include "../../proto/Messages.pb.h"
#include "../ProtoType.h"
#include <vector>
#include <memory>
#include "zmq.hpp"

namespace cameo {

class ServicesImpl {

public:
	ServicesImpl();
	virtual ~ServicesImpl();

	void setTimeout(int timeout);
	int getTimeout() const;

	bool isAvailable(const std::string& strRequestType, const std::string& strRequestData, const std::string& endpoint, int timeout);
	void waitForSubscriber(zmq::socket_t * subscriber, const std::string& strRequestType, const std::string& strRequestData, const std::string& endpoint);
	void subscribeToPublisher(const std::string& endpoint);

	std::string createRequest(ProtoType type) const;
	std::string createInitRequest() const;
	std::string createStartRequest(const std::string& name, const std::vector<std::string> & args, const std::string& instanceReference) const;
	std::string createStopRequest(int id) const;
	std::string createKillRequest(int id) const;
	std::string createConnectRequest(const std::string& name) const;
	std::string createIsAliveRequest(int id) const;
	std::string createAllAvailableRequest() const;
	std::string createShowAllRequest() const;
	std::string createShowStatusRequest() const;
	std::string createSetStatusRequest(int id, int32_t state) const;
	std::string createGetStatusRequest(int id) const;
	std::string createSetResultRequest(int id, const std::string& data) const;
	std::string createSubscribePublisherRequest() const;
	std::string createCreatePublisherRequest(int id, const std::string& name, int numberOfSubscribers) const;
	std::string createConnectPublisherRequest(int id, const std::string& publisherName) const;
	std::string createTerminatePublisherRequest(int id, const std::string& name) const;
	std::string createRequestPortRequest(int id, const std::string& name) const;
	std::string createConnectPortRequest(int id, const std::string& name) const;
	std::string createRemovePortRequest(int id, const std::string& name) const;
	std::string createStartedUnmanagedRequest(const std::string& name) const;
	std::string createTerminatedUnmanagedRequest(int id) const;

	zmq::socket_t * createEventSubscriber(const std::string& endpoint, const std::string& cancelEndpoint);
	zmq::socket_t * createCancelPublisher(const std::string& endpoint);

	std::auto_ptr<zmq::message_t> tryRequestWithOnePartReply(const std::string& strRequestType, const std::string& strRequestData, const std::string& endpoint, int overrideTimeout = -1);
	std::string createShowStreamRequest(int id) const;

	proto::MessageType_Type convertToProtoType(ProtoType type) const;

	int m_timeout;
	zmq::context_t m_context;

	static const std::string STATUS;
	static const std::string RESULT;
	static const std::string PUBLISHER;
	static const std::string PORT;
	static const std::string CANCEL;
};

}

#endif