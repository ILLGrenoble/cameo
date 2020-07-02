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

#include "Serializer.h"
#include "../proto/Messages.pb.h"

namespace cameo {

void serialize(const std::string& data, std::string& result) {

	// encode the data
	proto::StringValue value;
	value.set_value(data);

	value.SerializeToString(&result);
}

void serialize(const int32_t* data, std::size_t size, std::string& result) {

	// encode the data
	proto::Int32Array array;

	for (int i = 0; i < size; i++) {
		array.add_value(data[i]);
	}

	array.SerializeToString(&result);
}

void serialize(const int64_t* data, std::size_t size, std::string& result) {

	// encode the data
	proto::Int64Array array;

	for (int i = 0; i < size; i++) {
		array.add_value(data[i]);
	}

	array.SerializeToString(&result);
}

void serialize(const float* data, std::size_t size, std::string& result) {

	// encode the data
	proto::Float32Array array;

	for (int i = 0; i < size; i++) {
		array.add_value(data[i]);
	}

	array.SerializeToString(&result);
}

void serialize(const double* data, std::size_t size, std::string& result) {

	// encode the data
	proto::Float64Array array;

	for (int i = 0; i < size; i++) {
		array.add_value(data[i]);
	}

	array.SerializeToString(&result);
}

void parse(const std::string& data, std::string& result) {

	proto::StringValue value;
	value.ParseFromArray(data.c_str(), data.size());

	result = value.value();
}

void parse(const std::string& data, std::vector<int32_t>& result) {

	proto::Int32Array array;
	array.ParseFromArray(data.c_str(), data.size());

	size_t size = array.value_size();
	result.resize(size);

	for (int i = 0; i < size; i++) {
		result[i] = array.value(i);
	}
}

void parse(const std::string& data, std::vector<int64_t>& result) {

	proto::Int64Array array;
	array.ParseFromArray(data.c_str(), data.size());

	size_t size = array.value_size();
	result.resize(size);

	for (int i = 0; i < size; i++) {
		result[i] = array.value(i);
	}
}

void parse(const std::string& data, std::vector<float>& result) {

	proto::Float32Array array;
	array.ParseFromArray(data.c_str(), data.size());

	size_t size = array.value_size();
	result.resize(size);

	for (int i = 0; i < size; i++) {
		result[i] = array.value(i);
	}
}

void parse(const std::string& data, std::vector<double>& result) {

	proto::Float64Array array;
	array.ParseFromArray(data.c_str(), data.size());

	size_t size = array.value_size();
	result.resize(size);

	for (int i = 0; i < size; i++) {
		result[i] = array.value(i);
	}
}

}
