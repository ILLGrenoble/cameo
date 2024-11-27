/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_PUBLISHERIMPL_H_
#define CAMEO_PUBLISHERIMPL_H_

#include <string>

namespace cameo {
namespace coms {

class PublisherImpl {

public:
	virtual ~PublisherImpl() {}

	virtual void init(const std::string& publisherIdentity) = 0;

	virtual int getPublisherPort() const = 0;

	virtual void sendSync() = 0;
	virtual void send(const std::string& data) = 0;
	virtual void sendTwoParts(const std::string& data1, const std::string& data2) = 0;
	virtual void setEnd() = 0;
	virtual bool hasEnded() = 0;
	virtual void terminate() = 0;
};

}
}

#endif