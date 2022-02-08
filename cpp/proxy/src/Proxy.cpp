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

#include <string>
#include <iostream>
#include <zmq.hpp>

int main(int argc, char *argv[]) {

	if (argc < 2) {
		std::cout << "Usage: <port>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string port{argv[1]};
	std::string address("tcp://*:");
	address += port;

	// Prepare our context and socket.
	zmq::context_t context(1);
	zmq::socket_t router(context, ZMQ_ROUTER);
	router.setsockopt(ZMQ_IDENTITY, "R");
	router.bind(address);

	// Initialize poll set.
	zmq::pollitem_t items[] = { { router, 0, ZMQ_POLLIN, 0 } };
	zmq::const_buffer emptyBuffer;

	while (true) {
		zmq::poll(&items[0], 1, -1);

		if (items[0].revents & ZMQ_POLLIN) {

			std::cout << "Got a message" << std::endl;

			zmq::message_t fromIdentity, empty, toIdentity, message;
			if (!router.recv(fromIdentity, zmq::recv_flags::none)) {
				continue;
			}
			if (!router.recv(empty, zmq::recv_flags::none)) {
				continue;
			}
			if (!router.recv(toIdentity, zmq::recv_flags::none)) {
				continue;
			}
			if (!router.recv(empty, zmq::recv_flags::none)) {
				continue;
			}
			if (!router.recv(message, zmq::recv_flags::none)) {
				continue;
			}

//			std::string fromIdentityStr = std::string(fromIdentity.data<char>(), fromIdentity.size());
//			std::string toIdentityStr = std::string(toIdentity.data<char>(), toIdentity.size());

//			char* p_end;
//			const long f = std::strtol(fromIdentityStr.c_str(), &p_end, 10);
//			const long t = std::strtol(toIdentityStr.c_str(), &p_end, 10);
//
//			std::cout << "Message received from " << fromIdentityStr << " must be sent to " << toIdentityStr << std::endl;

			// This part will be removed and replaced by the router identity once the message is sent.
			router.send(toIdentity, zmq::send_flags::sndmore);
			router.send(emptyBuffer, zmq::send_flags::sndmore);
			router.send(fromIdentity, zmq::send_flags::sndmore);
			router.send(emptyBuffer, zmq::send_flags::sndmore);

			// Loop on the parts.
			bool more = message.more();

			if (more) {
				router.send(message, zmq::send_flags::sndmore);

				while (true) {

					zmq::message_t messagePart;

					if (!router.recv(messagePart, zmq::recv_flags::none)) {
						continue;
					}

					if (messagePart.more()) {
						router.send(messagePart, zmq::send_flags::sndmore);
					}
					else {
						router.send(messagePart, zmq::send_flags::none);
						break;
					}
				}
			}
			else {
				router.send(message, zmq::send_flags::none);
			}

			std::cout << "Message sent" << std::endl;
		}
	}

	return EXIT_SUCCESS;
}
