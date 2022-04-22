import sys
import time
import threading
import cameopy

def printOutput(socket):
    while True:
        output = socket.receive()
        if output:
            print(output.getMessage())
        else:
            return

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 3 else (sys.argv[1] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, useProxy)
server.init()

app = server.start("streampy", cameopy.OUTPUTSTREAM)

socket = app.getOutputStreamSocket()    

# Start thread.
t = threading.Thread(target=printOutput, args=(socket,))
t.start()

time.sleep(1)

print("Canceling output")

socket.cancel()
socket.terminate()

t.join()

state = app.waitFor()

print("Finished the application")
