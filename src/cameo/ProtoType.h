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

#ifndef CAMEO_PROTOTYPE_H_
#define CAMEO_PROTOTYPE_H_

namespace cameo {

enum ProtoType {
	PROTO_INIT,
	PROTO_START,
	PROTO_STOP,
	PROTO_KILL,
	PROTO_CONNECT,
	PROTO_SHOWALL,
	PROTO_SHOW,
	PROTO_STATUS,
	PROTO_WRITESTREAM,
	PROTO_ISALIVE,
	PROTO_SENDPARAMETERS,
	PROTO_APPLICATION,
	PROTO_ALLAVAILABLE,
	PROTO_SETSTATUS,
	PROTO_GETSTATUS,
	PROTO_CREATEPUBLISHER,
	PROTO_TERMINATEPUBLISHER,
	PROTO_CONNECTPUBLISHER,
	PROTO_SUBSCRIBEPUBLISHER,
	PROTO_REQUESTPORT,
	PROTO_CONNECTPORT,
	PROTO_REMOVEPORT,
	PROTO_REQUEST,
	PROTO_RESPONSE,
	PROTO_CANCEL,
	PROTO_SETRESULT
};

}

#endif
