/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "IdGenerator.h"

namespace cameo {

std::atomic_int IdGenerator::m_currentId{0};

int IdGenerator::newId() {
	return ++m_currentId;
}

std::string IdGenerator::newStringId() {
	return std::string{"cameo."} + std::to_string(newId());
}

}