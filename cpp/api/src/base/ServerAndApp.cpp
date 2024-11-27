/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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