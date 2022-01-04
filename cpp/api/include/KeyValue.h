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

#ifndef CAMEO_KEYVALUE_H_
#define CAMEO_KEYVALUE_H_

#include <string>

namespace cameo {

class KeyValue {

public:
	enum Status {UNDEFINED, STORED, REMOVED};

	KeyValue(const std::string& key);

	void setStatus(Status status);
	void setValue(const std::string& value);

	Status getStatus() const;
	const std::string& getKey() const;
	const std::string& getValue() const;

private:
	Status m_status;
	std::string m_key;
	std::string m_value;
};

}

#endif
