import sys
import threading
import cameopy

this = cameopy.This
this.init(sys.argv)

def responderProcess():
    print("Creating responder")
    
    responder = cameopy.MultiResponder.create(router)
    
    print("Created responder")
    
    # Receive first request.
    request = responder.receive();
    print("Received request", request.get())
    
    request.reply("1st response")
    
    # Receive second request.
    request = responder.receive();
    
    print("Received request with parts", request.get(), request.getSecondBinaryPart())
    
    request.reply("2nd response")
    
    router.cancel() 
    

print("Creating router")

router = cameopy.MultiResponderRouter.create("responder")

print("Created router")

this.setRunning()

t = threading.Thread(target=responderProcess, args=())
t.start()

router.run()

t.join()

print("Finished the application")