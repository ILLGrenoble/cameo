/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "RemoteException.h"

namespace cameo {

RemoteException::RemoteException(const std::string& message) :
	m_message{message} {
}

RemoteException::~RemoteException() throw() {
}

const char* RemoteException::what() const throw() {
	return m_message.c_str();
}

}