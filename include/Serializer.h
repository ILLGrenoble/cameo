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

#ifndef CAMEO_SERIALIZER_H_
#define CAMEO_SERIALIZER_H_

#include <string>
#include <vector>

namespace cameo {

/**
 * Serializes the string data into a string. The current implementation only copies the data.
 */
void serialize(const std::string& data, std::string& result);

/**
 * Parses the string data into a string. The current implementation only copies the data.
 */
void parse(const std::string& data, std::string& result);

}

#endif
