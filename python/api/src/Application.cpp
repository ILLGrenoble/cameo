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

using namespace cameo;
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

	py::class_<ServerAndApp>(m, "ServerAndApp")
		    .def("getServer", &ServerAndApp::getServer)
			.def("getApp", &ServerAndApp::getApp);

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
	    .def_static("isAvailable", &This::isAvailable,
	    		"timeout"_a = 10000,
	    		py::call_guard<py::gil_scoped_release>()) //, py::arg("timeout") = 1000) // this does not work!
	    .def_static("isStopping", &This::isStopping, py::call_guard<py::gil_scoped_release>())
	    .def_static("handleStop", &This::handleStop,
	    		"function"_a,
				"stoppingTime"_a = -1,
				py::call_guard<py::gil_scoped_release>())
	    .def_static("cancelAll", &This::cancelAll, py::call_guard<py::gil_scoped_release>())
	    .def_static("setRunning", &This::setRunning, py::call_guard<py::gil_scoped_release>())
	    .def_static("setResult", &This::setResult,
	    		"data"_a,
				py::call_guard<py::gil_scoped_release>())
	    .def_static("connectToStarter", &This::connectToStarter,
	    		"options"_a = 0,
				"useProxy"_a = false,
	    		py::call_guard<py::gil_scoped_release>())
		.def_static("__str__", &This::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<Output>(m, "Output")
		.def("getId", &Output::getId)
		.def("getMessage", &Output::getMessage)
		.def("isEndOfLine", &Output::isEndOfLine)
		.def("__str__", &Output::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<OutputStreamSocket>(m, "OutputStreamSocket")
		.def("terminate", &OutputStreamSocket::terminate, py::call_guard<py::gil_scoped_release>())
		.def("receive", &OutputStreamSocket::receive, py::call_guard<py::gil_scoped_release>())
		.def("cancel", &OutputStreamSocket::cancel, py::call_guard<py::gil_scoped_release>())
		.def("hasEnded", &OutputStreamSocket::hasEnded)
		.def("isCanceled", &OutputStreamSocket::isCanceled)
		.def("__str__", &OutputStreamSocket::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App>(m, "App")
		.def("terminate", &App::terminate, py::call_guard<py::gil_scoped_release>())
	    .def("getName", &App::getName)
	    .def("getId", &App::getId)
	    .def("getEndpoint", &App::getEndpoint)
	    .def("getNameId", &App::getNameId)
	    //.def("getCom", &App::getCom)
	    .def("hasResult", &App::hasResult)
	    .def("exists", &App::exists)
	    .def("getErrorMessage", &App::getErrorMessage)
	    .def("stop", &App::stop, py::call_guard<py::gil_scoped_release>())
	    .def("kill", &App::kill, py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<>(&App::waitFor), py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<int>(&App::waitFor),
	    		"states"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("cancelWaitFor", &App::cancelWaitFor, py::call_guard<py::gil_scoped_release>())
	    .def("getLastState", &App::getLastState, py::call_guard<py::gil_scoped_release>())
	    .def("getActualState", &App::getActualState, py::call_guard<py::gil_scoped_release>())
	    .def("getPastStates", &App::getPastStates, py::call_guard<py::gil_scoped_release>())
	    .def("getExitCode", &App::getExitCode)
	    .def("getResult",
			 [](App* instance) {
				 auto result = instance->getResult();
				 if (result.has_value() == false)
					 return py::bytes("");
				 return py::bytes(result.value());
			 }, py::call_guard<py::gil_scoped_release>())
	    .def("getStringResult", &App::getResult, py::call_guard<py::gil_scoped_release>())
	    .def("getOutputStreamSocket", &App::getOutputStreamSocket)
		.def("__str__", &App::toString,
		   		py::call_guard<py::gil_scoped_release>());

	py::class_<AppArray>(m, "AppArray");

	py::class_<Server>(m, "Server")
		.def_static("create", py::overload_cast<const std::string&, int, bool>(&Server::create),
				"endpoint"_a,
				"timeout"_a = 0,
				"useProxy"_a = false,
		   		py::call_guard<py::gil_scoped_release>())

		.def("terminate", &Server::terminate, py::call_guard<py::gil_scoped_release>())
		.def("setTimeout", &Server::setTimeout,
				"value"_a)
		.def("getTimeout", &Server::getTimeout)
		.def("getEndpoint", &Server::getEndpoint)
		.def("getVersion", &Server::getVersion)
		.def("isAvailable", py::overload_cast<>(&Server::isAvailable, py::const_), py::call_guard<py::gil_scoped_release>())
		.def("isAvailable", py::overload_cast<int>(&Server::isAvailable, py::const_),
				"timeout"_a,
				py::call_guard<py::gil_scoped_release>())

		.def("start", py::overload_cast<const std::string&, int>(&Server::start),
				"name"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())

		.def("start", py::overload_cast<const std::string&, const std::vector<std::string>&, int>(&Server::start),
				"name"_a, "args"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("connectAll", &Server::connectAll,
				"name"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("connect", py::overload_cast<const std::string&, int>(&Server::connect),
				"name"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("connect", py::overload_cast<int, int>(&Server::connect),
				"id"_a, "options"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("killAllAndWaitFor", &Server::killAllAndWaitFor,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("getApplicationConfigs", &Server::getApplicationConfigs, py::call_guard<py::gil_scoped_release>())
		.def("getApplicationInfos", py::overload_cast<>(&Server::getApplicationInfos, py::const_), py::call_guard<py::gil_scoped_release>())
		.def("getApplicationInfos", py::overload_cast<const std::string&>(&Server::getApplicationInfos, py::const_),
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("getPorts", &Server::getPorts, py::call_guard<py::gil_scoped_release>())
		.def("getActualState", &Server::getActualState,
				"id"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("getPastStates", &Server::getPastStates,
				"id"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("__str__", &Server::toString,
				py::call_guard<py::gil_scoped_release>());


	py::class_<Publisher>(m, "Publisher")
	    .def_static("create", &Publisher::create,
	    		"name"_a,
	    		"numberOfSubscribers"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("terminate", &Publisher::terminate, py::call_guard<py::gil_scoped_release>())
	    .def("getName", &Publisher::getName)
	    .def("waitForSubscribers", &Publisher::waitForSubscribers, py::call_guard<py::gil_scoped_release>())
	    .def("cancelWaitForSubscribers", &Publisher::cancelWaitForSubscribers, py::call_guard<py::gil_scoped_release>())
	    .def("send", &Publisher::send,
	    		"data"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendTwoParts", &Publisher::sendTwoParts,
	    		"data1"_a, "data2"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendEnd", &Publisher::sendEnd, py::call_guard<py::gil_scoped_release>())
	    .def("hasEnded", &Publisher::hasEnded)
		.def("__str__", &Publisher::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<Subscriber>(m, "Subscriber")
	    .def_static("create", &Subscriber::create,
	    		"instance"_a,
	    		"publisherName"_a,
	    		py::call_guard<py::gil_scoped_release>())
		.def("terminate", &Subscriber::terminate, py::call_guard<py::gil_scoped_release>())
	    .def("getPublisherName", &Subscriber::getPublisherName)
	    .def("getAppName", &Subscriber::getAppName)
	    .def("getAppId", &Subscriber::getAppId)
	    .def("getAppEndpoint", &Subscriber::getAppEndpoint)
	    .def("hasEnded", &Subscriber::hasEnded)
	    .def("isCanceled", &Subscriber::isCanceled)

		.def("receive",
			[](Subscriber* instance) {
				auto result = instance->receive();
				 if (result.has_value() == false)
					 return py::bytes("");
				 return py::bytes(result.value());
			}, py::call_guard<py::gil_scoped_release>())

	    .def("receiveString", &Subscriber::receive, py::call_guard<py::gil_scoped_release>())
	    .def("receiveTwoParts",
			[](Subscriber* instance) {
				auto result = instance->receiveTwoParts();
				 if (result.has_value() == false)
					 return py::tuple();

				 py::tuple tupleResult(2);
				 tupleResult[0] = py::bytes(std::get<0>(result.value()));
				 tupleResult[1] = py::bytes(std::get<1>(result.value()));

				 return tupleResult;
			}, py::call_guard<py::gil_scoped_release>())
	    .def("cancel", &Subscriber::cancel, py::call_guard<py::gil_scoped_release>())
		.def("__str__", &Subscriber::toString,
					py::call_guard<py::gil_scoped_release>());

	py::class_<basic::Request>(m, "BasicRequest")
		.def("getObjectId", &basic::Request::getObjectId)
	    .def("getRequesterEndpoint", &basic::Request::getRequesterEndpoint)
		.def("get", [](basic::Request* instance) {
					auto result = instance->get();
					return py::bytes(result);
				 }, py::call_guard<py::gil_scoped_release>())
		.def("getString", &basic::Request::get)
	    .def("getSecondPart",
			[](basic::Request* instance) {
				 auto result = instance->getSecondPart();
				 return py::bytes(result);
			 }, py::call_guard<py::gil_scoped_release>())
	    .def("reply", &basic::Request::reply,
	    		"response"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connectToRequester", &basic::Request::connectToRequester,
	    		"options"_a = 0,
				"useProxy"_a = false,
	    		py::call_guard<py::gil_scoped_release>())
		.def("__str__", &basic::Request::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<basic::Responder>(m, "BasicResponder")
	    .def_static("create", &basic::Responder::create,
	    		"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
		.def("terminate", &basic::Responder::terminate, py::call_guard<py::gil_scoped_release>())
	    .def("getName", &basic::Responder::getName)
	    .def("cancel", &basic::Responder::cancel, py::call_guard<py::gil_scoped_release>())
	    .def("receive", &basic::Responder::receive, py::call_guard<py::gil_scoped_release>())
	    .def("isCanceled", &basic::Responder::isCanceled)
		.def("__str__", &basic::Responder::toString,
				py::call_guard<py::gil_scoped_release>());


	py::class_<multi::Request>(m, "MultiRequest")
		.def("getObjectId", &multi::Request::getObjectId)
		.def("getRequesterEndpoint", &multi::Request::getRequesterEndpoint)
		.def("get",
			[](multi::Request* instance) {
				 auto result = instance->get();
				 return py::bytes(result);
			 }, py::call_guard<py::gil_scoped_release>())
		.def("getString", &multi::Request::get)
		.def("getSecondPart",
			[](multi::Request* instance) {
				 auto result = instance->getSecondPart();
				 return py::bytes(result);
			 }, py::call_guard<py::gil_scoped_release>())
		.def("reply", &multi::Request::reply,
				"response"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("connectToRequester", &multi::Request::connectToRequester,
				"options"_a = 0,
				"useProxy"_a = false,
				py::call_guard<py::gil_scoped_release>())
		.def("__str__", &multi::Request::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<multi::ResponderRouter>(m, "MultiResponderRouter")
		.def_static("create", &multi::ResponderRouter::create,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("terminate", &multi::ResponderRouter::terminate, py::call_guard<py::gil_scoped_release>())
		.def("setPollingTime", &multi::ResponderRouter::setPollingTime,
				"value"_a)
		.def("getName", &multi::ResponderRouter::getName)
		.def("cancel", &multi::ResponderRouter::cancel, py::call_guard<py::gil_scoped_release>())
		.def("run", &multi::ResponderRouter::run, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &multi::ResponderRouter::isCanceled)
		.def("__str__", &multi::ResponderRouter::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<multi::Responder>(m, "MultiResponder")
		.def_static("create", &multi::Responder::create,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("terminate", &multi::Responder::terminate, py::call_guard<py::gil_scoped_release>())
		.def("cancel", &multi::Responder::cancel, py::call_guard<py::gil_scoped_release>())
		.def("receive", &multi::Responder::receive, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &multi::Responder::isCanceled)
		.def("__str__", &multi::Responder::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<Requester>(m, "Requester")
	    .def_static("create", &Requester::create,
	    		"instance"_a,
				"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
		.def("terminate", &Requester::terminate, py::call_guard<py::gil_scoped_release>())
		.def("setTimeout", &Requester::setTimeout)
		.def("setPollingTime", &Requester::setPollingTime)
	    .def("getName", &Requester::getResponderName)
		.def("getAppName", &Requester::getAppName)
		.def("getAppId", &Requester::getAppId)
		.def("getAppEndpoint", &Requester::getAppEndpoint)
	    .def("send", &Requester::send,
	    		"request"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("sendTwoParts", &Requester::sendTwoParts,
	    		"request1"_a, "request2"_a,
	    		py::call_guard<py::gil_scoped_release>())

		.def("receive",
			[](Requester* instance) {
				 auto result = instance->receive();
				 if (result.has_value() == false)
					 return py::bytes("");
				 return py::bytes(result.value());
			 }, py::call_guard<py::gil_scoped_release>())

	    .def("receiveString", &Requester::receive, py::call_guard<py::gil_scoped_release>())
	    .def("cancel", &Requester::cancel, py::call_guard<py::gil_scoped_release>())
	    .def("isCanceled", &Requester::isCanceled)
        .def("hasTimedout", &Requester::hasTimedout)
		.def("__str__", &Requester::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App::Config>(m, "AppConfig")
	    .def(py::init<const std::string&, const std::string&, bool, bool, int, int>())
	    .def("getName", &App::Config::getName)
	    .def("getDescription", &App::Config::getDescription)
	    .def("hasSingleInstance", &App::Config::hasSingleInstance)
	    .def("canRestart", &App::Config::canRestart)
	    .def("getStartingTime", &App::Config::getStartingTime)
	    .def("getStoppingTime", &App::Config::getStoppingTime)
		.def("__str__", &App::Config::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App::Info>(m, "AppInfo")
	    .def(py::init<const std::string&, int, int, State, State, const std::string&>())
	    .def("getId", &App::Info::getId)
	    .def("getState", &App::Info::getState)
	    .def("getPastStates", &App::Info::getPastStates)
	    .def("getArgs", &App::Info::getArgs)
	    .def("getName", &App::Info::getName)
	    .def("getPid", &App::Info::getPid)
		.def("__str__", &App::Info::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App::Port>(m, "AppPort")
	    .def(py::init<int, const std::string&, const std::string&>())
	    .def("getPort", &App::Port::getPort)
	    .def("getStatus", &App::Port::getStatus)
	    .def("getOwner", &App::Port::getOwner)
		.def("__str__", &App::Port::toString,
				py::call_guard<py::gil_scoped_release>());

}
