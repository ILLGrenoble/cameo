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

#ifndef CAMEO_IDGENERATOR_H_
#define CAMEO_IDGENERATOR_H_

#include <atomic>
#include <string>

namespace cameo {

/**
 * Class managing the id of cancel publishers.
 */
class IdGenerator {

public:
	static int newId();
	static std::string newStringId();

private:
	static std::atomic_int m_currentId;
};

}

#endif