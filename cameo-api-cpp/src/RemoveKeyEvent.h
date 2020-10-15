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

#ifndef CAMEO_REMOVEKEYEVENT_H_
#define CAMEO_REMOVEKEYEVENT_H_

#include <iostream>
#include "KeyEvent.h"

namespace cameo {

class RemoveKeyEvent : public KeyEvent {

	friend std::ostream& operator<<(std::ostream&, const RemoveKeyEvent&);

public:
	RemoveKeyEvent(int id, const std::string& name, const std::string& key);
	RemoveKeyEvent(const RemoveKeyEvent& event);

	virtual RemoveKeyEvent* clone();
};

std::ostream& operator<<(std::ostream&, const RemoveKeyEvent&);

}

#endif
