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

#ifndef CAMEO_RESPONSE_H_
#define CAMEO_RESPONSE_H_

#include <string>
#include <iostream>

namespace cameo {

class Response {

	friend std::ostream& operator<<(std::ostream&, const Response&);

public:
	Response();
	Response(int value, const std::string& message);

	int getValue() const;
	const std::string& getMessage() const;
	bool isSuccess() const;

private:
	int m_value;
	std::string m_message;
};

std::ostream& operator<<(std::ostream&, const Response&);

}

#endif