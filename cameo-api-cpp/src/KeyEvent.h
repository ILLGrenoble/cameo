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

#ifndef CAMEO_KEYEVENT_H_
#define CAMEO_KEYEVENT_H_

#include <iostream>
#include "Event.h"

namespace cameo {

class KeyEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const KeyEvent&);

public:
	enum Status {STORED, REMOVED};

	KeyEvent(int id, const std::string& name, Status status, const std::string& key, const std::string& value);
	KeyEvent(const KeyEvent& event);

	virtual KeyEvent* clone();

	Status getStatus() const;
	const std::string& getKey() const;
	const std::string& getValue() const;

private:
	Status m_status;
	std::string m_key;
	std::string m_value;
};

std::ostream& operator<<(std::ostream&, const KeyEvent&);

}

#endif
