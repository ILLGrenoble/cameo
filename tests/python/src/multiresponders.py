import sys
import threading
import cameopy

this = cameopy.This
this.init(sys.argv)

def stop():
    print("Stopping...")
    #cameopy.This.cancelWaitings()
    router.cancel()
    print("Stopped")
    
this.handleStop(stop)


numberOfTimes = 1 

if len(sys.argv) >= 2:
    numberOfTimes = int(sys.argv[1])


responders = []

def responderProcess(id):
    print("Creating responder")
    
    responder = cameopy.MultiResponder.create(router)
    responders.append(responder)
    
    print("Created responder")
    
    for i in range(numberOfTimes):
        
        # Receive request.
        request = responder.receive();
        print("Received request", request.get())
    
        request.reply(str(id) + " to " + request.get())
    
    #responder.terminate()

print("Creating router")

router = cameopy.MultiResponderRouter.create("responder")

print("Created router")

this.setRunning()

N = 5
tds = []

for id in range(N):
    t = threading.Thread(target=responderProcess, args=(id,))
    t.start()
    tds.append(t)

print("Router running")

router.run()

print("Router finished")

for id in range(N):
    tds[id].join()
    responders[id].terminate()

router.terminate()
this.terminate()

print("Finished the application")