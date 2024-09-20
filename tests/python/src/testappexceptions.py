import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 4 else (sys.argv[2] == "true") 

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.option.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

try:   
    server.start("fuzz")
except cameopy.StartException:
    print("Application fuzz cannot be started")    

app = server.connect("fuzz")    

if app is None:
    print("Application fuzz cannot be connected")
    

basicResponder = cameopy.coms.basic.Responder.create("basic-responder")
basicResponder.init()

basicResponder2 = cameopy.coms.basic.Responder.create("basic-responder")

try:
    basicResponder2.init()
except cameopy.InitException:
    print("Responder cannot be initialized")
  
    
multiResponder = cameopy.coms.multi.ResponderRouter.create("multi-responder")
multiResponder.init()

multiResponder2 = cameopy.coms.multi.ResponderRouter.create("multi-responder")

try:
    multiResponder2.init()
except cameopy.InitException:
    print("Responder cannot be initialized")


publisher = cameopy.coms.Publisher.create("publisher")
publisher.init()

publisher2 = cameopy.coms.Publisher.create("publisher")

try:
    publisher2.init()
except cameopy.InitException:
    print("Publisher cannot be initialized")
