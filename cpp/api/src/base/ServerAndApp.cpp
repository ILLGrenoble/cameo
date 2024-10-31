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

#include "ServerAndApp.h"

namespace cameo {

ServerAndApp::ServerAndApp() {
}

ServerAndApp::ServerAndApp(std::unique_ptr<Server>& server, std::unique_ptr<App>& app) :
	m_server(std::move(server)), m_app(std::move(app)) {
}

Server& ServerAndApp::getServer() {
	return *m_server.get();
}

bool ServerAndApp::hasApp() const {
	return m_app.get() != nullptr;
}

App& ServerAndApp::getApp() {
	return *m_app.get();
}

void ServerAndApp::terminate() {

	m_server->terminate();
	m_app->terminate();
}

}
