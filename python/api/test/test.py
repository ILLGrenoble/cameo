#!/usr/bin/python3
import sys

#sys.path.append('/usr/local/src/cameo-api-cpp/cameo-python/build/cameopython-prefix/src/cameopython-build/')
sys.path.append('/tmp/devel/lib64')

import cameopy
t= cameopy.This

t.init( sys.argv)
print("Name: ", t.getName())
print("Id: ", t.getId())
print("Timeout: ", t.getTimeout())
t.setRunning()


resp = cameopy.Responder.create("responder")
print("Responder name: ",resp.getName())
print("Responder: ", str(resp))

starter = t.connectToStarter()

request = resp.receive();
print(request.getBinary())
request.replyBinary("this is a text message")

  # std::unique_ptr<cameo::application::Request> message = responder->receive();
  # CHECK(message->getBinary() == TEXT );
  
  # message->replyBinary(TEXT);

#kam.This.setRunning();
