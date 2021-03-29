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
namespace py = pybind11;

#include <cameo/cameo.h>
using namespace cameo::application;

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
	/*
	py::enum_<cameo::Option>(m, "Option")
	    .value("NONE", cameo::NONE)
	    .value("OUTPUTSTREAM", cameo::OUTPUTSTREAM)
	    .export_values();
	*/
	py::class_<This>(m, "This")
	    .def(
		"init",
		[](std::vector<std::string> args) {
			std::vector<char*> cstrs;
			cstrs.reserve(args.size());
			for (auto& s : args)
				cstrs.push_back(const_cast<char*>(s.c_str()));
			This::init(args.size(), cstrs.data());
		},
		"initialize the application")
	    .def("terminate", &This::terminate)
	    .def("getName", &This::getName)
	    .def("getId", &This::getId)
	    .def("setTimeout", &This::setTimeout)
	    .def("getTimeout", &This::getTimeout)
	    .def("getEndpoint", &This::getEndpoint)
	    .def("getServer", &This::getServer, py::return_value_policy::reference)
	    .def("getCom", &This::getCom)
	    .def("getStarterServer", &This::getStarterServer)
	    .def("isAvailable", &This::isAvailable) //, py::arg("timeout") = 1000) // this does not work!
	    .def("isStopping", &This::isStopping)
	    //	    .def("handleStop", &This::handleStop)
	    .def("cancelWaitings", &This::cancelWaitings)
	    .def("setRunning", &This::setRunning)
	    .def("setBinaryResult", &This::setBinaryResult)
	    .def("setResult", &This::setResult)
	    .def("connectToStarter", &This::connectToStarter);

	py::class_<Instance>(m, "Instance")
	    .def("getName", &Instance::getName)
	    .def("getId", &Instance::getId)
	    .def("getEndpoint", &Instance::getEndpoint)
	    .def("getNameId", &Instance::getNameId)
	    .def("getCom", &Instance::getCom)
	    .def("hasResult", &Instance::hasResult)
	    .def("exists", &Instance::exists)
	    .def("getErrorMessage", &Instance::getErrorMessage)
	    .def("stop", &Instance::stop)
	    .def("kill", &Instance::kill)
	    .def("waitFor", py::overload_cast<>(&Instance::waitFor))
	    .def("waitFor", py::overload_cast<int>(&Instance::waitFor))
	    .def("waitFor", py::overload_cast<const std::string&>(&Instance::waitFor))
	    .def("cancelWaitFor", &Instance::cancelWaitFor)
	    .def("getLastState", &Instance::getLastState)
	    .def("getActualState", &Instance::getActualState)
	    .def("getPastStates", &Instance::getPastStates)
	    .def("getExitCode", &Instance::getExitCode)
	    .def("getBinaryResult",
		 [](Instance* instance) {
			 auto result = instance->getBinaryResult();
			 if (result.has_value() == false)
				 return py::bytes("");
			 return py::bytes(result.value());
		 })
	    .def("getResult", &Instance::getResult)
	    .def("getOutputStreamSocket", &Instance::getOutputStreamSocket);

	py::class_<InstanceArray>(m, "InstanceArray");

	py::class_<Publisher>(m, "Publisher")
	    .def("create", &Publisher::create, py::arg("numberOfSubscribers") = 0)
	    .def("getName", &Publisher::getName)
	    .def("getApplicationName", &Publisher::getApplicationName)
	    .def("getApplicationId", &Publisher::getApplicationId)
	    .def("getApplicationEndpoint", &Publisher::getApplicationEndpoint)
	    .def("waitForSubscribers", &Publisher::waitForSubscribers)
	    .def("cancelWaitForSubscribers", &Publisher::cancelWaitForSubscribers)
	    .def("sendBinary", &Publisher::sendBinary)
	    .def("send", &Publisher::send)
	    .def("sendTwoBinaryParts", &Publisher::sendTwoBinaryParts)
	    .def("sendEnd", &Publisher::sendEnd)
	    .def("isEnded", &Publisher::isEnded);

	py::class_<Subscriber>(m, "Subscriber")
	    .def("create", &Subscriber::create)
	    .def("getPublisherName", &Subscriber::getPublisherName)
	    .def("getInstanceName", &Subscriber::getInstanceName)
	    .def("getInstanceId", &Subscriber::getInstanceId)
	    .def("getInstanceEndpoint", &Subscriber::getInstanceEndpoint)
	    .def("isEnded", &Subscriber::isEnded)
	    .def("isCanceled", &Subscriber::isCanceled)
	    .def("receiveBinary", &Subscriber::receiveBinary)
	    .def("receive", &Subscriber::receive)
	    .def("receiveTwoBinaryParts", &Subscriber::receiveTwoBinaryParts)
	    .def("cancel", &Subscriber::cancel);

	py::class_<Request>(m, "Request")
	    .def("getObjectId", &Request::getObjectId)
	    .def("getRequesterEndpoint", &Request::getRequesterEndpoint)
	    .def("getBinary", &Request::getBinary)
	    .def("get", &Request::get)
	    .def("getSecondBinaryPart", &Request::getSecondBinaryPart)
	    .def("setTimeout", &Request::setTimeout)
	    .def("replyBinary", &Request::replyBinary)
	    .def("reply", &Request::reply)
	    .def("connectToRequester", &Request::connectToRequester)
	    // the following require "Server.h"
	    .def("getServer", &Request::getServer);

	py::class_<Responder>(m, "Responder")
	    .def("create", &Responder::create)
	    .def("getName", &Responder::getName)
	    .def("cancel", &Responder::cancel)
	    .def("receive", &Responder::receive)
	    .def("isCanceled", &Responder::isCanceled);


	py::class_<Requester>(m, "Requester")
	    .def("create", &Requester::create)
	    .def("getName", &Requester::getName)
	    .def("sendBinary", &Requester::sendBinary)
	    .def("send", &Requester::send)
	    .def("sendTwoBinaryParts", &Requester::sendTwoBinaryParts)
	    .def("receiveBinary", &Requester::receiveBinary)
	    .def("receive", &Requester::receive)
	    .def("cancel", &Requester::cancel)
	    .def("isCanceled", &Requester::isCanceled);


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
	    .def("setTimeout", &cameo::Server::setTimeout)
	    .def("getTimeout", &cameo::Server::getTimeout)
	    .def("getEndpoint", &cameo::Server::getEndpoint)
	    .def("getVersion", &cameo::Server::getVersion)
	    .def("isAvailable", py::overload_cast<>(&cameo::Server::isAvailable, py::const_))
	    .def("isAvailable", py::overload_cast<int>(&cameo::Server::isAvailable, py::const_))
	    .def("start", py::overload_cast<const std::string&, int>(&cameo::Server::start))
	    .def("start", py::overload_cast<const std::string&, const std::vector<std::string>&, int>(
			      &cameo::Server::start))
	    .def("connectAll", &cameo::Server::connectAll)
	    .def("connect", py::overload_cast<const std::string&, int>(&cameo::Server::connect))
	    .def("connect", py::overload_cast<int, int>(&cameo::Server::connect))
	    .def("killAllAndWaitFor", &cameo::Server::killAllAndWaitFor)
	    .def("getApplicationConfigurations", &cameo::Server::getApplicationConfigurations)
	    .def("getApplicationInfos", py::overload_cast<>(&cameo::Server::getApplicationInfos, py::const_))
	    .def("getApplicationInfos",
		 py::overload_cast<const std::string&>(&cameo::Server::getApplicationInfos, py::const_))
	    .def("getPorts", &cameo::Server::getPorts)
	    .def("getActualState", &cameo::Server::getActualState)
	    .def("getPastStates", &cameo::Server::getPastStates)
	    .def("openEventStream", &cameo::Server::openEventStream);
}
