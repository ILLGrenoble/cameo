import sys
import cameopy

# Initialize cameo.
this = cameopy.This
this.init(sys.argv)

# Parameters: responder endpoint, language, message, number of times.
if len(sys.argv) < 6:
    print("Parameters: <responder endpoint> <language> <message> <number of times>")
    exit(-1)  

responderEndpoint = sys.argv[1]
language = sys.argv[2]
message = sys.argv[3]
numberOfTimes = int(sys.argv[4])

# Initialize the cameo server.
server = cameopy.Server.create(responderEndpoint)
server.init()

print("Connected server", server)

# Connect to the responder app.
appName = "responder-" + language
responderApp = server.connect(appName)

if responderApp is None:
    responderApp = server.start(appName)

print("App", responderApp, "has state", cameopy.toString(responderApp.getState()))

# Create a requester.
requester = cameopy.coms.Requester.create(responderApp, "the-responder")
requester.init()

print("Created requester", requester)

for i in range(numberOfTimes):
    # Send a simple message as string.
    request = message + "-" + str(i)
    requester.send(request)
    response = requester.receiveString()
    print("Response is", response)

# Stop the responder app and wait for its termination.
responderApp.stop()
state = responderApp.waitFor()

print("App responder finished with state", cameopy.toString(state))

# Terminate the requester and server.
requester.terminate()
server.terminate()
this.terminate()