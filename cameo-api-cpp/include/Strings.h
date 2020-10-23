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

#ifndef CAMEO_STRINGS_H_
#define CAMEO_STRINGS_H_

#include <string>
#include <vector>
#include <optional>

namespace cameo {

std::vector<std::string> split(const std::string& str, char c);

class Endpoint {

public:
	Endpoint(const std::string& address, int port);

	const std::string& getAddress() const;
	int getPort() const;

	static Endpoint parse(const std::string& str);

	std::string toString() const;

private:
	std::string m_address;
	int m_port;
};

}

#endif
