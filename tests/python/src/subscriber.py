import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

applicationName = ""

if len(sys.argv) >= 3:
    applicationName = sys.argv[1]

server = this.getServer()

app = server.connect(applicationName, 0)

subscriber = cameopy.Subscriber.create(app, "publisher")

print("Synchronized with 1 publisher")

this.setRunning()

while True:
    data = subscriber.receive()
    if data:
        print("Received", data)
    else:
        break

print("Finished the application")
