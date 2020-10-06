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

#ifndef CAMEO_RESULTEVENT_H_
#define CAMEO_RESULTEVENT_H_

#include <iostream>
#include "Application.h"
#include "Event.h"

namespace cameo {

class ResultEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const ResultEvent&);

public:
	ResultEvent(int id, const std::string& name, const std::string& data);
	ResultEvent(const ResultEvent& event);

	virtual ResultEvent* clone();

	const std::string& getData() const;

private:
	std::string m_data;
};

std::ostream& operator<<(std::ostream&, const ResultEvent&);

}

#endif
