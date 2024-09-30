import sys
import threading
import cameopy

this = cameopy.This
this.init(sys.argv)

def stop():
    print("Stopping...")
    #cameopy.This.cancelAll()
    router.cancel()
    print("Stopped")
    
this.handleStop(stop)


numberOfTimes = 1 

if len(sys.argv) >= 2:
    numberOfTimes = int(sys.argv[1])


responders = []

def responderProcess(id):
    print("Creating responder")
    
    responder = cameopy.coms.multi.Responder.create(router)
    responder.init()
    responders.append(responder)
    
    print("Created responder")
    
    for i in range(numberOfTimes):
        
        # Receive request.
        request = responder.receive();
        print("Received request", request.getString())
    
        request.reply(str(id) + " to " + request.getString())
    
    responder.terminate()

print("Creating router")

router = cameopy.coms.multi.ResponderRouter.create("responder")
router.init()

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
