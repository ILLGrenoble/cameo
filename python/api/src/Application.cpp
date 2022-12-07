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

	m.doc() = "Python binding of Cameo C++ API"; // optional module docstring
	m.def("toString", &toString, "Function converting numerical state to its string representation");

	m.attr("OUTPUTSTREAM")       = cameo::OUTPUTSTREAM;
	m.attr("UNLINKED")           = cameo::UNLINKED;
	m.attr("NIL")                = NIL;
	m.attr("STARTING")           = STARTING;
	m.attr("RUNNING")            = RUNNING;
	m.attr("STOPPING")           = STOPPING;
	m.attr("KILLING")            = KILLING;
	m.attr("PROCESSING_ERROR")   = PROCESSING_ERROR;
	m.attr("FAILURE")            = FAILURE;
	m.attr("SUCCESS")            = SUCCESS;
	m.attr("STOPPED")            = STOPPED;
	m.attr("KILLED")             = KILLED;

	// Exceptions

	py::register_exception<UnregisteredApplicationException>(m, "UnregisteredApplicationException");
	py::register_exception<InvalidArgumentException>(m, "InvalidArgumentException");
	py::register_exception<SocketException>(m, "SocketException");
	py::register_exception<ConnectionTimeout>(m, "ConnectionTimeout");
	py::register_exception<StartException>(m, "StartException");
	py::register_exception<InitException>(m, "InitException");

	// Important note:
	// The call_guard policy is set to py::gil_scoped_release for all bindings except for getters and setters that use a local member of the object.
	// If the policy is not set, the bindings are blocking other Python running threads.

	///////////////////////////////////////////////////////////////////////////
	// base

	py::class_<ServerAndApp>(m, "ServerAndApp")
		    .def("getServer", &ServerAndApp::getServer, py::return_value_policy::reference)
			.def("hasApp", &ServerAndApp::hasApp)
			.def("getApp", &ServerAndApp::getApp, py::return_value_policy::reference)
			.def("terminate", &ServerAndApp::terminate, py::call_guard<py::gil_scoped_release>());

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
	    .def_static("isStopping", &This::isStopping)
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
				"timeout"_a = 0,
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
		.def("hasEnded", &OutputStreamSocket::hasEnded, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &OutputStreamSocket::isCanceled);

	py::class_<App>(m, "App")
		.def("terminate", &App::terminate, py::call_guard<py::gil_scoped_release>())
	    .def("getName", &App::getName)
	    .def("getId", &App::getId)
	    .def("getEndpoint", &App::getEndpoint)
	    .def("getNameId", &App::getNameId)
	    //.def("getCom", &App::getCom)
	    .def("hasResult", &App::hasResult)
	    .def("stop", &App::stop, py::call_guard<py::gil_scoped_release>())
	    .def("kill", &App::kill, py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<>(&App::waitFor), py::call_guard<py::gil_scoped_release>())
	    .def("waitFor", py::overload_cast<int>(&App::waitFor),
	    		"states"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("cancel", &App::cancel, py::call_guard<py::gil_scoped_release>())
	    .def("getLastState", &App::getLastState, py::call_guard<py::gil_scoped_release>())
	    .def("getActualState", &App::getActualState, py::call_guard<py::gil_scoped_release>())
	    .def("getPastStates", &App::getPastStates, py::call_guard<py::gil_scoped_release>())
	    .def("getExitCode", &App::getExitCode)
	    .def("getResult", [](App* instance) {

				// Release the GIL for the blocking call getResult().
				py::gil_scoped_release release;

				auto result = instance->getResult();

				// Acquire the GIL for security.
				py::gil_scoped_acquire acquire;

				std::optional<py::bytes> bytesResult;
				if (result.has_value()) {
					bytesResult = py::bytes(result.value());
				}
				return bytesResult;
			 })
	    .def("getStringResult", &App::getResult, py::call_guard<py::gil_scoped_release>())
	    .def("getOutputStreamSocket", &App::getOutputStreamSocket)
		.def("__str__", &App::toString,
		   		py::call_guard<py::gil_scoped_release>());

	py::class_<AppArray>(m, "AppArray");

	py::class_<Server>(m, "Server")
		.def_static("create", py::overload_cast<const std::string&, bool>(&Server::create),
				"endpoint"_a,
				"useProxy"_a = false,
		   		py::call_guard<py::gil_scoped_release>())
		.def("init", &Server::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &Server::isReady)
		.def("terminate", &Server::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &Server::isTerminated)
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

	///////////////////////////////////////////////////////////////////////////
	// app
	//
	// Submodules can be part of a different C++ library: https://hopstorawpointers.blogspot.com/2018/06/pybind11-and-python-sub-modules.html

	py::module am = m.def_submodule("app", "App module");

	py::class_<App::Config>(am, "Config")
		.def(py::init<const std::string&, const std::string&, bool, bool, int, int>())
		.def("getName", &App::Config::getName)
		.def("getDescription", &App::Config::getDescription)
		.def("getMultiple", &App::Config::getMultiple)
		.def("canRestart", &App::Config::canRestart)
		.def("getStartingTime", &App::Config::getStartingTime)
		.def("getStoppingTime", &App::Config::getStoppingTime)
		.def("__str__", &App::Config::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App::Info>(am, "Info")
		.def(py::init<const std::string&, int, int, State, State, const std::string&>())
		.def("getId", &App::Info::getId)
		.def("getState", &App::Info::getState)
		.def("getPastStates", &App::Info::getPastStates)
		.def("getArgs", &App::Info::getArgs)
		.def("getName", &App::Info::getName)
		.def("getPid", &App::Info::getPid)
		.def("__str__", &App::Info::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<App::Port>(am, "Port")
		.def(py::init<int, const std::string&, const std::string&>())
		.def("getPort", &App::Port::getPort)
		.def("getStatus", &App::Port::getStatus)
		.def("getOwner", &App::Port::getOwner)
		.def("__str__", &App::Port::toString,
				py::call_guard<py::gil_scoped_release>());


	///////////////////////////////////////////////////////////////////////////
	// coms
	py::module cm = m.def_submodule("coms", "Communication patterns module");

	py::class_<Publisher>(cm, "Publisher")
	    .def_static("create", &Publisher::create,
	    		"name"_a,
	    		"numberOfSubscribers"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("init", &Publisher::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &Publisher::isReady)
		.def("terminate", &Publisher::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &Publisher::isTerminated)
	    .def("getName", &Publisher::getName)
	    .def("cancel", &Publisher::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &Publisher::isCanceled)
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

	py::class_<Subscriber>(cm, "Subscriber")
	    .def_static("create", &Subscriber::create,
	    		"instance"_a,
	    		"publisherName"_a,
	    		py::call_guard<py::gil_scoped_release>())
		.def("init", &Subscriber::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &Subscriber::isReady)
		.def("setTimeout", &Subscriber::setTimeout)
		.def("getTimeout", &Subscriber::getTimeout)
		.def("terminate", &Subscriber::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &Subscriber::isTerminated)
	    .def("getPublisherName", &Subscriber::getPublisherName)
	    .def("getAppName", &Subscriber::getAppName)
	    .def("getAppId", &Subscriber::getAppId)
	    .def("getAppEndpoint", &Subscriber::getAppEndpoint)
	    .def("hasEnded", &Subscriber::hasEnded)
		.def("receive", [](Subscriber* instance) {

				// Release the GIL for the blocking call getResult().
				py::gil_scoped_release release;

				auto result = instance->receive();

				// Acquire the GIL for security.
				py::gil_scoped_acquire acquire;

				std::optional<py::bytes> bytesResult;
				if (result.has_value()) {
					bytesResult = py::bytes(result.value());
				}
				return bytesResult;
			})
	    .def("receiveString", &Subscriber::receive, py::call_guard<py::gil_scoped_release>())
	    .def("receiveTwoParts", [](Subscriber* instance) {

				// Release the GIL for the blocking call getResult().
				py::gil_scoped_release release;

				auto result = instance->receiveTwoParts();

				// Acquire the GIL for security: creation of py::tuple crashes on Ubuntu 22 otherwise.
				py::gil_scoped_acquire acquire;

				std::optional<py::tuple> tupleResult;
				if (result.has_value()) {
					 tupleResult = py::make_tuple(py::bytes(std::get<0>(result.value())), py::bytes(std::get<1>(result.value())));
				}
				return tupleResult;
			})
	    .def("cancel", &Subscriber::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &Subscriber::isCanceled)
		.def("__str__", &Subscriber::toString,
					py::call_guard<py::gil_scoped_release>());

	py::class_<Requester>(cm, "Requester")
		.def_static("create", &Requester::create,
				"instance"_a,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("init", &Requester::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &Requester::isReady)
		.def("terminate", &Requester::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &Requester::isTerminated)
		.def("setTimeout", &Requester::setTimeout)
		.def("getTimeout", &Requester::getTimeout)
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

		.def("receive", [](Requester* instance) {

				// Release the GIL for the blocking call getResult().
				py::gil_scoped_release release;

				auto result = instance->receive();

				// Acquire the GIL for security.
				py::gil_scoped_acquire acquire;

				std::optional<py::bytes> bytesResult;
				if (result.has_value()) {
					bytesResult = py::bytes(result.value());
				}
				return bytesResult;
			 })

		.def("receiveString", &Requester::receive, py::call_guard<py::gil_scoped_release>())
		.def("cancel", &Requester::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &Requester::isCanceled)
		.def("hasTimedout", &Requester::hasTimedout)
		.def("__str__", &Requester::toString,
				py::call_guard<py::gil_scoped_release>());


	///////////////////////////////////////////////////////////////////////////
	// coms basic
	py::module cbm = cm.def_submodule("basic", "Communication patterns basic module");

	py::class_<basic::Request>(cbm, "Request")
	    .def("getRequesterEndpoint", &basic::Request::getRequesterEndpoint)
		.def("get", [](basic::Request* instance) {
				auto result = instance->get();
				return py::bytes(result);
			}, py::call_guard<py::gil_scoped_release>())
		.def("getString", &basic::Request::get)
		.def("getFirstPart", [](basic::Request* instance) {
				auto result = instance->getFirstPart();
				return py::bytes(result);
			 }, py::call_guard<py::gil_scoped_release>())
		.def("getSecondPart", [](basic::Request* instance) {
				auto result = instance->getSecondPart();
				return py::bytes(result);
			 }, py::call_guard<py::gil_scoped_release>())
	    .def("reply", &basic::Request::reply,
	    		"response"_a,
	    		py::call_guard<py::gil_scoped_release>())
	    .def("connectToRequester", &basic::Request::connectToRequester,
	    		"options"_a = 0,
				"useProxy"_a = false,
				"timeout"_a = 0,
	    		py::call_guard<py::gil_scoped_release>())
		.def("__str__", &basic::Request::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<basic::Responder>(cbm, "Responder")
	    .def_static("create", &basic::Responder::create,
	    		"name"_a,
	    		py::call_guard<py::gil_scoped_release>())
		.def("init", &basic::Responder::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &basic::Responder::isReady)
		.def("terminate", &basic::Responder::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &basic::Responder::isTerminated)
	    .def("getName", &basic::Responder::getName)
	    .def("cancel", &basic::Responder::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &basic::Responder::isCanceled)
		.def("receive", &basic::Responder::receive, py::call_guard<py::gil_scoped_release>())
		.def("__str__", &basic::Responder::toString,
				py::call_guard<py::gil_scoped_release>());


	///////////////////////////////////////////////////////////////////////////
	// coms basic
	py::module cmm = cm.def_submodule("multi", "Communication patterns multi module");

	py::class_<multi::Request>(cmm, "Request")
		.def("getRequesterEndpoint", &multi::Request::getRequesterEndpoint)
		.def("get", [](multi::Request* instance) {
				auto result = instance->get();
				return py::bytes(result);
			}, py::call_guard<py::gil_scoped_release>())
		.def("getString", &multi::Request::get)
		.def("getFirstPart", [](multi::Request* instance) {
				auto result = instance->getFirstPart();
				return py::bytes(result);
			}, py::call_guard<py::gil_scoped_release>())
		.def("getSecondPart", [](multi::Request* instance) {
				auto result = instance->getSecondPart();
				return py::bytes(result);
			}, py::call_guard<py::gil_scoped_release>())
		.def("reply", &multi::Request::reply,
				"response"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("connectToRequester", &multi::Request::connectToRequester,
				"options"_a = 0,
				"useProxy"_a = false,
				"timeout"_a = 0,
				py::call_guard<py::gil_scoped_release>())
		.def("__str__", &multi::Request::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<multi::ResponderRouter>(cmm, "ResponderRouter")
		.def_static("create", &multi::ResponderRouter::create,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("init", &multi::ResponderRouter::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &multi::ResponderRouter::isReady)
		.def("terminate", &multi::ResponderRouter::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &multi::ResponderRouter::isTerminated)
		.def("setPollingTime", &multi::ResponderRouter::setPollingTime,
				"value"_a)
		.def("getName", &multi::ResponderRouter::getName)
		.def("cancel", &multi::ResponderRouter::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &multi::ResponderRouter::isCanceled)
		.def("run", &multi::ResponderRouter::run, py::call_guard<py::gil_scoped_release>())
		.def("__str__", &multi::ResponderRouter::toString,
				py::call_guard<py::gil_scoped_release>());

	py::class_<multi::Responder>(cmm, "Responder")
		.def_static("create", &multi::Responder::create,
				"name"_a,
				py::call_guard<py::gil_scoped_release>())
		.def("init", &multi::Responder::init, py::call_guard<py::gil_scoped_release>())
		.def("isReady", &multi::Responder::isReady)
		.def("terminate", &multi::Responder::terminate, py::call_guard<py::gil_scoped_release>())
		.def("isTerminated", &multi::Responder::isTerminated)
		.def("cancel", &multi::Responder::cancel, py::call_guard<py::gil_scoped_release>())
		.def("isCanceled", &multi::Responder::isCanceled)
		.def("receive", &multi::Responder::receive, py::call_guard<py::gil_scoped_release>())
		.def("__str__", &multi::Responder::toString,
				py::call_guard<py::gil_scoped_release>());

}
