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

#include <pybind11/pybind11.h>
#include <pybind11/stl.h>
#include <pybind11/functional.h> // Necessary for This::handleStop()

namespace py = pybind11;
using namespace pybind11::literals;

#include <cameo/api/cameo.h>

using namespace cameo::application;
using namespace cameo::coms;

#include <iostream>

PYBIND11_MODULE(cameopy, m) {

	m.doc() = "Python binding of C++ API"; // optional module docstring
	m.def("toString", &toString, "Function converting numerical state to its string representation");

	m.attr("OUTPUTSTREAM")       = cameo::OUTPUTSTREAM;
	m.attr("UNKNOWN")            = UNKNOWN;
	m.attr("STARTING")           = STARTING;
	m.attr("RUNNING")            = RUNNING;
	m.attr("STOPPING")           = STOPPING;
	m.attr("KILLING")            = KILLING;
	m.attr("PROCESSING_ERROR")   = PROCESSING_ERROR;
	m.attr("FAILURE")            = FAILURE;
	m.attr("SUCCESS")            = SUCCESS;
	m.attr("STOPPED")            = STOPPED;
	m.attr("KILLED")             = KILLED;


	// Important note:
	// The call_guard policy is set to py::gil_scoped_release for all bindings except for getters and setters that use a local member of the object.
	// If the policy is not set, the bindings are blocking other Python running threads.

	py::class_<This>(m, "This")
	    .def_static(
		"init",
		[](std::vector<std::string> args) {
			std::vector<char*> cstrs;
			cstrs.reserve(args.size());
			for (auto& s : args)
				cstrs.push_back(const_cast<char*>(s.c_str()));
			This::init(args.size(), cstrs.data());
		}, py::call_guard<py::gil_scoped_release>(),
		"initialize the application")

		.def_static("init", py::overload_cast<const std::string&, const std::string&>(&This::init),
	    		"name"_a, "endpoint"_a,
	    		py::call_guard<py::gil_scoped_release>())

	    .def_static("terminate", &This::terminate, py::call_guard<py::gil_scoped_release>())
	    .def_static("getName", &This::getName)
	    .def_static("getId", &This::getId)
		.def_static("setTimeout", &This::setTimeout,
				"value"_a)
	    .def_static("getTimeout", &This::getTimeout)
	    .def_static("getEndpoint", &This::getEndpoint)
	    .def_static("getServer", &This::getServer, py::return_value_policy::reference)
	    .def_static("getCom", &This::getCom)
	    .def_static("getStarterServer", &This::getStarterServer)
	    .def_static("isAvailable", &This::isAvailable,
	    		"timeout"_a = 10000,
	    		py::call_guard<py::gil_scoped_release>()) //, py::arg("timeout") = 1000) // this does not work!
	    .def_static("isStopping", &This::isStopping, py::call_guard<py::gil_scoped_release>())
	    .def_static("handleStop", &This::handleStop,
	    		"function"_a,
				"stoppingTime"_a = -1,
				py::call_guard<py::gil_scoped_release>())
	    .def_static("cancelWaitings", &This::cancelWaitings, py::call_guard<py::gil_scoped_release>())
	    .def_static("setRunning", &This::setRunning, py::call_guard<py::gil_scoped_release>())
	    .def_static("setBinaryResult", &This::setBinaryResult,
	    		"data"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def_static("setResult", &This::setResult,
	    		"data"_a,
				py::call_guard<py::gil_scoped_release>())
	    .def_static("connectToStarter", &This::connectToStarter, py::call_guard<py::gil_scoped_release>());

	py::class_<cameo::Output>(m, "Output")
		.def("getId", &cameo::Output::getId)
		.def("getMessage", &cameo::Output::getMessage)
		.def("isEndOfLine", &cameo::Output::isEndOfLine);

	py::class_<cameo::OutputStreamSocket>(m, "OutputStreamSocket")
		.def("receive", &cameo::OutputStreamSocket::receive, py::call_guard<py::gil_scoped_release>())
		.def("cancel", &cameo::OutputStreamSocket::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isEnded", &cameo::OutputStreamSocket::isEnded)
		.def("isCanceled", &cameo::OutputStreamSocket::isCanceled);

	py::class_<Instance>(m, "Instance")
	    .def("getName", &Instance::getName)
	    .def("getId", &Instance::getId)
	    .def("getEndpoint", &Instance::getEndpoint)
	    .def("getNameId", &Instance::getNameId)
	    //.def("getCom", &Instance::getCom)
	    .def("hasResult", &Instance::hasResult)
	    .def("exists", &Instance::exists)
	    .def("getErrorMessage", &Instance::getErrorMessage)
	    .def("stop", &Instance::stop, py::call_guard<py::gil_scoped_release>())
	    .def("kill", &Instance::kill, py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<>(&Instance::waitFor), py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<int>(&Instance::waitFor),
	    		"states"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("cancelWaitFor", &Instance::cancelWaitFor, py::call_guard<py::gil_scoped_release>())
	    .def("getLastState", &Instance::getLastState, py::call_guard<py::gil_scoped_release>())
	    .def("getActualState", &Instance::getActualState, py::call_guard<py::gil_scoped_release>())
	    .def("getPastStates", &Instance::getPastStates, py::call_guard<py::gil_scoped_release>())
	    .def("getExitCode", &Instance::getExitCode)
	    .def("getBinaryResult",
		 [](Instance* instance) {
			 auto result = instance->getBinaryResult();
			 if (result.has_value() == false)
				 return py::bytes("");
			 return py::bytes(result.value());
		 }, py::call_guard<py::gil_scoped_release>())
	    .def("getResult", &Instance::getResult, py::call_guard<py::gil_scoped_release>())
	    .def("getOutputStreamSocket", &Instance::getOutputStreamSocket);

	py::class_<InstanceArray>(m, "InstanceArray");

	py::class_<Publisher>(m, "Publisher")
	    .def_static("create", &Publisher::create,
	    		"name"_a,
	    		"numberOfSubscribers"_a = 0,
				py::call_guard<py::gil_scoped_release>())
	    .def("getName", &Publisher::getName)
	    .def("waitForSubscribers", &Publisher::waitForSubscribers, py::call_guard<py::gil_scoped_release>())
	    .def("cancelWaitForSubscribers", &Publisher::cancelWaitForSubscribers, py::call_guard<py::gil_scoped_release>())
	    .def("sendBinary", &Publisher::sendBinary,
	    		"data"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("send", &Publisher::send,
	    		"data"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendTwoBinaryParts", &Publisher::sendTwoBinaryParts,
	    		"data1"_a, "data2"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendEnd", &Publisher::sendEnd, py::call_guard<py::gil_scoped_release>())
	    .def("isEnded", &Publisher::isEnded);

	py::class_<Subscriber>(m, "Subscriber")
	    .def_static("create", &Subscriber::create,
	    		"instance"_a,
	    		"publisherName"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getPublisherName", &Subscriber::getPublisherName)
	    .def("getAppName", &Subscriber::getAppName)
	    .def("getAppId", &Subscriber::getAppId)
	    .def("getAppEndpoint", &Subscriber::getAppEndpoint)
	    .def("isEnded", &Subscriber::isEnded)
	    .def("isCanceled", &Subscriber::isCanceled)
	    .def("receiveBinary", &Subscriber::receiveBinary, py::call_guard<py::gil_scoped_release>())
	    .def("receive", &Subscriber::receive, py::call_guard<py::gil_scoped_release>())
	    .def("receiveTwoBinaryParts", &Subscriber::receiveTwoBinaryParts, py::call_guard<py::gil_scoped_release>())
	    .def("cancel", &Subscriber::cancel, py::call_guard<py::gil_scoped_release>());

	py::class_<legacy::Request>(m, "Request")
	    .def("getObjectId", &legacy::Request::getObjectId)
	    .def("getRequesterEndpoint", &legacy::Request::getRequesterEndpoint)
	    .def("getBinary", &legacy::Request::getBinary)
	    .def("get", &legacy::Request::get)
	    .def("getSecondBinaryPart", &legacy::Request::getSecondBinaryPart)
	    .def("setTimeout", &legacy::Request::setTimeout,
	    		"value"_a)
	    .def("replyBinary", &legacy::Request::replyBinary,
	    		"response"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("reply", &legacy::Request::reply,
	    		"response"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connectToRequester", &legacy::Request::connectToRequester, py::call_guard<py::gil_scoped_release>())
	    // the following require "Server.h"
	    .def("getServer", &legacy::Request::getServer);

	py::class_<legacy::Responder>(m, "Responder")
	    .def_static("create", &legacy::Responder::create,
	    		"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getName", &legacy::Responder::getName)
	    .def("cancel", &legacy::Responder::cancel, py::call_guard<py::gil_scoped_release>())
	    .def("receive", &legacy::Responder::receive, py::call_guard<py::gil_scoped_release>())
	    .def("isCanceled", &legacy::Responder::isCanceled);


	py::class_<legacy::Requester>(m, "Requester")
	    .def_static("create", &legacy::Requester::create,
	    		"instance"_a,
				"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getName", &legacy::Requester::getResponderName)
		.def("getAppName", &legacy::Requester::getAppName)
		.def("getAppId", &legacy::Requester::getAppId)
		.def("getAppEndpoint", &legacy::Requester::getAppEndpoint)
	    .def("sendBinary", &legacy::Requester::sendBinary,
	    		"request"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("send", &legacy::Requester::send,
	    		"request"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendTwoBinaryParts", &legacy::Requester::sendTwoBinaryParts,
	    		"request1"_a, "request2"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("receiveBinary", &legacy::Requester::receiveBinary, py::call_guard<py::gil_scoped_release>())
	    .def("receive", &legacy::Requester::receive, py::call_guard<py::gil_scoped_release>())
	    .def("cancel", &legacy::Requester::cancel, py::call_guard<py::gil_scoped_release>())
	    .def("isCanceled", &legacy::Requester::isCanceled);


	py::class_<Configuration>(m, "Configuration")
	    .def(py::init<const std::string&, const std::string&, bool, bool, int, int>())
	    .def("getName", &Configuration::getName)
	    .def("getDescription", &Configuration::getDescription)
	    .def("hasSingleInstance", &Configuration::hasSingleInstance)
	    .def("canRestart", &Configuration::canRestart)
	    .def("getStartingTime", &Configuration::getStartingTime)
	    .def("getStoppingTime", &Configuration::getStoppingTime);

	py::class_<Info>(m, "Info")
	    .def(py::init<const std::string&, int, int, State, State, const std::string&>())
	    .def("getId", &Info::getId)
	    .def("getState", &Info::getState)
	    .def("getPastStates", &Info::getPastStates)
	    .def("getArgs", &Info::getArgs)
	    .def("getName", &Info::getName)
	    .def("getPid", &Info::getPid);

	py::class_<Port>(m, "Port")
	    .def(py::init<int, const std::string&, const std::string&>())
	    .def("getPort", &Port::getPort)
	    .def("getStatus", &Port::getStatus)
	    .def("getOwner", &Port::getOwner);

	py::class_<cameo::Server>(m, "Server")
	    .def(py::init<const std::string&>())
	    .def("setTimeout", &cameo::Server::setTimeout,
	    		"value"_a)
	    .def("getTimeout", &cameo::Server::getTimeout)
	    .def("getEndpoint", &cameo::Server::getEndpoint)
	    .def("getVersion", &cameo::Server::getVersion)
	    .def("isAvailable", py::overload_cast<>(&cameo::Server::isAvailable, py::const_), py::call_guard<py::gil_scoped_release>())
	    .def("isAvailable", py::overload_cast<int>(&cameo::Server::isAvailable, py::const_),
	    		"timeout"_a,
	    		py::call_guard<py::gil_scoped_release>())

		.def("start", py::overload_cast<const std::string&, int>(&cameo::Server::start),
				"name"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())

	    .def("start", py::overload_cast<const std::string&, const std::vector<std::string>&, int>(&cameo::Server::start),
	    		"name"_a, "args"_a, "options"_a = 0,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connectAll", &cameo::Server::connectAll,
	    		"name"_a, "options"_a = 0,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connect", py::overload_cast<const std::string&, int>(&cameo::Server::connect),
	    		"name"_a, "options"_a = 0,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connect", py::overload_cast<int, int>(&cameo::Server::connect),
	    		"id"_a, "options"_a = 0,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("killAllAndWaitFor", &cameo::Server::killAllAndWaitFor,
	    		"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getApplicationConfigurations", &cameo::Server::getApplicationConfigurations, py::call_guard<py::gil_scoped_release>())
	    .def("getApplicationInfos", py::overload_cast<>(&cameo::Server::getApplicationInfos, py::const_), py::call_guard<py::gil_scoped_release>())
	    .def("getApplicationInfos", py::overload_cast<const std::string&>(&cameo::Server::getApplicationInfos, py::const_),
	    		"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getPorts", &cameo::Server::getPorts, py::call_guard<py::gil_scoped_release>())
	    .def("getActualState", &cameo::Server::getActualState,
	    		"id"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("getPastStates", &cameo::Server::getPastStates,
	    		"id"_a,
	    		py::call_guard<py::gil_scoped_release>());
	    //.def("openEventStream", &cameo::Server::openEventStream, py::call_guard<py::gil_scoped_release>());
}
