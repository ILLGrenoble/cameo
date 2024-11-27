/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_IDGENERATOR_H_
#define CAMEO_IDGENERATOR_H_

#include "Defines.h"
#include <atomic>
#include <string>

namespace cameo {

/**
 * Class managing the ids of communication objects.
 */
class CAMEO_EXPORT IdGenerator {

public:
	/**
	 * Generates a new id.
	 * \return An id.
	 */
	static int newId();

	/**
	 * Generates a new string id of the form "cameo.<id>". For example "cameo.15".
	 * \return A string id.
	 */
	static std::string newStringId();

private:
	static std::atomic_int m_currentId;
};

}

#endif