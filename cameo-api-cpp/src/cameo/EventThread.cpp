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

#include "EventThread.h"
#include "Server.h"
#include "EventStreamSocket.h"
#include "StatusEvent.h"
#include "ResultEvent.h"
#include "PublisherEvent.h"
#include "PortEvent.h"

namespace cameo {

EventThread::EventThread(Server * server, std::unique_ptr<EventStreamSocket>& socket) :
	m_server(server) {
	m_socket = std::move(socket);
}

EventThread::~EventThread() {
}

void EventThread::start() {
}

void EventThread::cancel() {
	m_socket->cancel();
}

void EventThread::processStatusEvent(StatusEvent * status) {

}

void EventThread::processResultEvent(ResultEvent * result) {

}

void EventThread::processPublisherEvent(PublisherEvent * publisher) {

}

void EventThread::processPortEvent(PortEvent * port) {

}

}
