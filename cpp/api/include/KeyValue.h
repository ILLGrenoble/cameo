/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_KEYVALUE_H_
#define CAMEO_KEYVALUE_H_

#include "Defines.h"
#include <string>

namespace cameo {

/**
 * Class defining a key value.
 */
class CAMEO_EXPORT KeyValue {

public:
	/**
	 * Type of the status.
	 */
	enum Status {UNDEFINED, STORED, REMOVED};

	/**
	 * Constructor.
	 * \param key The key.
	 */
	KeyValue(const std::string& key);

	/**
	 * Sets the status.
	 * \param status The status.
	 */
	void setStatus(Status status);

	/**
	 * Sets the value.
	 * \param value The value.
	 */
	void setValue(const std::string& value);

	/**
	 * Gets the status.
	 * \return The status.
	 */
	Status getStatus() const;

	/**
	 * Gets the key.
	 * \return The key.
	 */
	const std::string& getKey() const;

	/**
	 * Gets the value.
	 * \return The value.
	 */
	const std::string& getValue() const;

private:
	Status m_status;
	std::string m_key;
	std::string m_value;
};

}

#endif