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

#include "PublisherEvent.h"

#include <iostream>

namespace cameo {

PublisherEvent::PublisherEvent(int id, const std::string& name, const std::string& publisherName) :
	Event(id, name),
	m_publisherName(publisherName) {
}

const std::string& PublisherEvent::getPublisherName() const {
	return m_publisherName;
}

std::ostream& operator<<(std::ostream& os, const cameo::PublisherEvent& publisher) {
	os << "name=" << publisher.m_name
		<< "\nid=" << publisher.m_id
		<< "\npublisherName=" << publisher.m_publisherName;

	return os;
}

}
