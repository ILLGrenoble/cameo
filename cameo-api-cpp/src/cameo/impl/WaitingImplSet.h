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

#ifndef CAMEO_WAITINGIMPLSET_H_
#define CAMEO_WAITINGIMPLSET_H_

#include <set>
#include <boost/thread.hpp>

namespace cameo {

class WaitingImpl;

/**
 * Class containing a set of WaitingImpl objects.
 * It is protected with a mutex because the class must be thread-safe.
 */
class WaitingImplSet {

public:
	WaitingImplSet();

	void add(WaitingImpl * waiting);
	void remove(WaitingImpl * waiting);

	void cancelAll();

private:
	boost::mutex m_mutex;
	std::set<WaitingImpl *> m_set;
};

}

#endif