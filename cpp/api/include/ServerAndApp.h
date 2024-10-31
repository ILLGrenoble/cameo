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

#ifndef CAMEO_SERVERANDAPP_H_
#define CAMEO_SERVERANDAPP_H_

#include "Server.h"

namespace cameo {

/**
 * Helper class to provide an App instance and its associated Server instance.
 */
class CAMEO_EXPORT ServerAndApp {

public:
	/**
	 * Empty constructor.
	 */
	ServerAndApp();

	/**
	 * Constructor.
	 * \param server The server.
	 * \param app The app started by the server.
	 */
	ServerAndApp(std::unique_ptr<Server> &server, std::unique_ptr<App> &app);

	/**
	 * Gets the server.
	 * \return The server.
	 */
	Server& getServer();

	/**
	 * Returns the App instance existence. The instance may not exist if it was not possible to connect it.
	 * \return true if the App instance exists.
	 */
	bool hasApp() const;

	/**
	 * Gets the app.
	 * \return The app.
	 */
	App& getApp();

	/**
	 * Terminates the server and app.
	 */
	void terminate();

private:
	std::unique_ptr<Server> m_server;
	std::unique_ptr<App> m_app;
};

}

#endif

