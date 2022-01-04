#define DOCTEST_CONFIG_IMPLEMENT
#include <doctest/doctest.h>

#include "cameo/cameo.h" // this assumes CMake to add the include_dir at compilation time
#include <iostream>

#define SERVERNAME "responder"
#define CAMEO_RESPONDER "responder"
#define RESPONDERPYTHON "responderpython"

#define CAMEO_PUBLISHER "publisher"
#define CAMEO_PUBLISHER_PYTHON "publisherpython"
#define TEXT "this is a text message"
#define TEXT2 "another message"

/*************************************************************/
TEST_CASE("self" * doctest::expected_failures(0) // the getEndpoint
) {

	cameo::application::This::setRunning();
	CHECK(cameo::application::This::getName() == "testA");
	// this name can effectively be set to the local hostname
	// CHECK(cameo::application::This::getEndpoint() == "tcp://localhost.localdomain:2000");
	CHECK(cameo::application::This::setRunning() == true);

	CHECK(cameo::application::This::isAvailable() == true);
	CHECK(cameo::application::This::isStopping() == false);

	cameo::Server& server = cameo::application::This::getServer();
	std::cout << "This: " << cameo::application::This() << std::endl; // testing stream output

	CHECK(server.isAvailable() == true);
	std::cout << "Server: " << server << std::endl; // testing stream output

	std::unique_ptr<cameo::application::Instance> self =
	    server.connect(cameo::application::This::getName());
	CHECK(self->exists() == true);
	// cameo::application::This::terminate(); // never call this!
}

/*************************************************************/
TEST_CASE("instance") {
	cameo::Server& server = cameo::application::This::getServer();

	std::unique_ptr<cameo::application::Instance> self =
	    server.connect(cameo::application::This::getName());
	CHECK(self->exists() == true);

	CHECK(self->getName() == cameo::application::This::getName());
	CHECK(self->getId() > 0);

	//  std::string endpoint = cameo::application::This::getEndpoint();
	// endpoint.erase(endpoint.size()-5); // remove the port
	// CHECK( self->getUrl() == endpoint);
	CHECK(self->getEndpoint() == cameo::application::This::getEndpoint());
	CHECK(self->getNameId() ==
	      std::string(cameo::application::This::getName()) + "." + std::to_string(self->getId()));

	CHECK(self->hasResult() == false);

	CHECK(self->getErrorMessage() == "");
	// self->waitFor();
	self->cancelWaitFor();
	CHECK(self->getLastState() == cameo::application::RUNNING);
	CHECK(self->getActualState() == cameo::application::RUNNING);

	// cannot do that if hasResult is false!
	if (self->hasResult() == true) {
		auto result = self->getResult();
		CHECK(result.has_value() == true);
		CHECK(result.value() == "");
	}

	// auto result = self->getBinaryResult();
	// CHECK(result.has_value() == false);
	CHECK(self->stop() == true);
	CHECK(self->exists() == true);
	CHECK(self->kill() == true);
	CHECK(self->exists() == true);
}

/*************************************************************/
TEST_CASE("requester") {
	std::string responder_name;

	cameo::Server& server = cameo::application::This::getServer();
	CHECK(server.isAvailable() == true);

	SUBCASE("cpp") { responder_name = CAMEO_RESPONDER; }
	SUBCASE("python") { responder_name = RESPONDERPYTHON; }

	// tell the server to start the reponder application
	std::unique_ptr<cameo::application::Instance> responder_instance;
	{
		responder_instance = server.start(responder_name);
		std::cout << "Responder instance: " << (*responder_instance) << std::endl;
		REQUIRE(responder_instance->exists() == true);
	}
	// connect to the responder application
	std::unique_ptr<cameo::application::Instance> responderServer = server.connect(responder_name);
	std::cout << *responderServer << std::endl;
	CHECK(responderServer->exists() == true);
	CHECK(responder_instance->getEndpoint() == responderServer->getEndpoint());
	CHECK(responder_instance->getName() == responderServer->getName());
	CHECK(responder_instance->getId() == responderServer->getId());
	// responder_instance and responderServer are the SAME!
	// why returning a smart pointer instead of an object?
	// what is exactly an instance? what does it represent?

	std::unique_ptr<cameo::coms::Requester> requester =
	    //   cameo::coms::Requester::create( *responderServer, CAMEO_RESPONDER);
	    cameo::coms::Requester::create(*responder_instance, CAMEO_RESPONDER);


	// requester->send(TEXT);
	// auto message = requester->receive();
	// CHECK(message.has_value() == true);
	// CHECK(message.value() == TEXT);
	
	requester->sendBinary(TEXT);
	auto message = requester->receiveBinary();
	CHECK(message.has_value() == true);
	CHECK(message.value() == TEXT);

	//  requester->cancel();
}

/*************************************************************/
TEST_CASE("responder") {
	cameo::application::This::setRunning();

	std::cout << "Name: " << cameo::application::This::getName() << std::endl;
	std::cout << "Id: " << cameo::application::This::getId() << std::endl;
	std::cout << "Timeout: " << cameo::application::This::getTimeout() << std::endl;
	std::unique_ptr<cameo::coms::Responder> responder;
	try {
		responder = cameo::coms::Responder::create(CAMEO_RESPONDER);
		std::cout << "Created responder " << *responder << std::endl;
	} catch (const cameo::ResponderCreationException& e) {
		std::cout << "Responder error" << std::endl;
		CHECK(false);
	}

	// this ensures that both are at this stage: requester and responder
	std::unique_ptr<cameo::application::Instance> starter = cameo::application::This::connectToStarter();

	std::unique_ptr<cameo::coms::Request> request = responder->receive();
	CHECK(request->getBinary() == TEXT);
	//  CHECK(request->getObjectId() == " ");
	request->replyBinary(TEXT);
}

/*************************************************************/
/*************************************************************/
/*************************************************************/
TEST_CASE("subscriber") {
	std::string publisher_name;

	cameo::Server& server = cameo::application::This::getServer();
	CHECK(server.isAvailable() == true);

	SUBCASE("cpp") { publisher_name = CAMEO_PUBLISHER; }
	SUBCASE("python") { publisher_name = CAMEO_PUBLISHER_PYTHON; }

	// tell the server to start the publisher application
	std::unique_ptr<cameo::application::Instance> publisher_instance;
	{
		publisher_instance = server.start(publisher_name);
		std::cout << "Publisher instance: " << (*publisher_instance) << std::endl;
		REQUIRE(publisher_instance->exists() == true);
	}
	// connect to the publisher application
	std::unique_ptr<cameo::application::Instance> publisherServer = server.connect(publisher_name);
	std::cout << *publisherServer << std::endl;
	CHECK(publisherServer->exists() == true);
	CHECK(publisher_instance->getEndpoint() == publisherServer->getEndpoint());
	CHECK(publisher_instance->getName() == publisherServer->getName());
	CHECK(publisher_instance->getId() == publisherServer->getId());
	// publisher_instance and publisherServer are the SAME!

	std::unique_ptr<cameo::coms::Subscriber> subscriber =
	    cameo::coms::Subscriber::create(*publisher_instance, publisher_name);
	CHECK(subscriber->getPublisherName() == publisher_name);
	
	auto message = subscriber->receiveBinary();
	CHECK(message.has_value() == true);
	// now need to check if the publisher has finished
	CHECK(message.value() == TEXT); // std::string("\n")+TEXT);
	message = subscriber->receive();
	CHECK(message.has_value() == true);
	CHECK(message.value() == TEXT2);
}

/*************************************************************/
TEST_CASE("publisher") { // it is started from the subscriber
	cameo::application::This::setRunning();

	std::cout << "Name: " << cameo::application::This::getName() << std::endl;
	std::cout << "Id: " << cameo::application::This::getId() << std::endl;
	std::cout << "Timeout: " << cameo::application::This::getTimeout() << std::endl;
	std::unique_ptr<cameo::coms::Publisher> publisher;
	try {
		publisher = cameo::coms::Publisher::create(CAMEO_PUBLISHER);
		std::cout << "Created publisher " << *publisher << std::endl;
	} catch (const cameo::PublisherCreationException& e) {
		std::cout << "Publisher error" << std::endl;
		CHECK(false);
	}
	std::cout << publisher->getName() << std::endl;
	std::cout << publisher->getApplicationName() << std::endl;
	std::cout << publisher->getApplicationId() << std::endl;
	std::cout << publisher->getApplicationEndpoint() << std::endl;
	// this ensures that both are at this stage: requester and publisher
	std::unique_ptr<cameo::application::Instance> starter = cameo::application::This::connectToStarter();

	std::cout << publisher->waitForSubscribers() << std::endl;

	publisher->sendBinary(TEXT);
	publisher->send(TEXT2);
	publisher->sendEnd();
	std::cout << publisher->isEnded() << std::endl;
}

int main(int argc, char** argv) {
	//    std::cout << " CIAO " << argc << argv[argc-1] << std::endl;
	doctest::Context context;

	// !!! THIS IS JUST AN EXAMPLE SHOWING HOW DEFAULTS/OVERRIDES ARE SET !!!

	// defaults
	//    context.addFilter("test-case-exclude", "*math*"); // exclude test cases with "math" in their
	//    name
	// context.setOption("abort-after", 5);              // stop test execution after 5 failed assertions
	// context.setOption("order-by", "name");            // sort the test cases by their name

	context.applyCommandLine(argc, argv);

	// overrides
	context.setOption("no-breaks", true); // don't break in the debugger when assertions fail

	cameo::application::This thisApplication;

	try {
		thisApplication.init(argc, argv);
	} catch (...) {
		std::cerr << "[ERROR] Failed This::init" << std::endl;
		return 1;
	};
	cameo::application::State returnState = cameo::application::UNKNOWN;

	int res = context.run(); // run

	if (context.shouldExit()) // important - query flags (and --exit) rely on the user doing this
		return res;       // propagate the result of the tests

	int client_stuff_return_code = 0;
	// your program - if the testing framework is integrated in your production code

	return res + client_stuff_return_code; // the result from doctest is propagated here as well
}
