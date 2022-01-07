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
server = this.getServer()

app = server.start("streampy", cameopy.OUTPUTSTREAM)

socket = app.getOutputStreamSocket()    

# Start thread.
t = threading.Thread(target=printOutput, args=(socket,))
t.start()

time.sleep(1)

print("Canceling output")

socket.cancel()
t.join()

state = app.waitFor()

print("Finished the application")
