import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 4 else (sys.argv[2] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, useProxy)
server.init()

try:   
    server.start("fuzz")
except cameopy.StartException:
    print("Application fuzz cannot be started")    

app = server.connect("fuzz")    

if app is None:
    print("Application fuzz cannot be connected")
    

basicResponder = cameopy.BasicResponder.create("basic-responder")
basicResponder.init()

basicResponder2 = cameopy.BasicResponder.create("basic-responder")

try:
    basicResponder2.init()
except cameopy.InitException:
    print("Responder cannot be initialized")
  
    
multiResponder = cameopy.MultiResponderRouter.create("multi-responder")
multiResponder.init()

multiResponder2 = cameopy.MultiResponderRouter.create("multi-responder")

try:
    multiResponder2.init()
except cameopy.InitException:
    print("Responder cannot be initialized")


publisher = cameopy.Publisher.create("publisher")
publisher.init()

publisher2 = cameopy.Publisher.create("publisher")

try:
    publisher2.init()
except cameopy.InitException:
    print("Publisher cannot be initialized")
