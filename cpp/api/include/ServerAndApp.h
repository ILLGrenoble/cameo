/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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