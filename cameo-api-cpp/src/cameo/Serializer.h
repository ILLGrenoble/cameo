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
#include <stdint.h>

namespace cameo {

void serialize(const std::string& data, std::string& result);
void serialize(const int32_t* data, std::size_t size, std::string& result);
void serialize(const int64_t* data, std::size_t size, std::string& result);
void serialize(const float* data, std::size_t size, std::string& result);
void serialize(const double* data, std::size_t size, std::string& result);

void parse(const std::string& data, std::string& result);
void parse(const std::string& data, std::vector<int32_t>& result);
void parse(const std::string& data, std::vector<int64_t>& result);
void parse(const std::string& data, std::vector<float>& result);
void parse(const std::string& data, std::vector<double>& result);

}

#endif