import sys
import cameopy

# Initialize cameo.
this = cameopy.This
this.init(sys.argv)

# Parameters: publisher endpoint, language.
if len(sys.argv) < 4:
    print("Parameters: <publisher endpoint> <language>")
    exit(-1)

publisherEndpoint = sys.argv[1]
language = sys.argv[2]

# Initialize the cameo server.
server = cameopy.Server.create(publisherEndpoint)
server.init()

print("Connected server", server)

# Connect to the publisher app.
appName = "publisher-" + language
publisherApp = server.connect(appName)

# Start the publisher app if it is not running.
if publisherApp is None:
    publisherApp = server.start(appName)

print("App", publisherApp, "has state", cameopy.toString(publisherApp.getState()))

# Create a subscriber.
subscriber = cameopy.coms.Subscriber.create(publisherApp, "the-publisher")
subscriber.init()

print("Created subscriber", subscriber)

# Receive messages.
while True:
    message = subscriber.receiveString()
    if message:
        print("Received", message)
    else:
        print("Canceled")

# Terminate the cameo objects.
subscriber.terminate()
server.terminate()
this.terminate()