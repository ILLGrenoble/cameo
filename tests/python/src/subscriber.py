import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

applicationName = ""

if len(sys.argv) >= 3:
    applicationName = sys.argv[1]

server = this.getServer()

app = server.connect(applicationName, 0)

subscriber = cameopy.coms.Subscriber.create(app, "publisher")
subscriber.init()

print("Synchronized with 1 publisher")

this.setRunning()

while True:
    data = subscriber.receiveTwoParts()
    if data:
        print("Received", data[0].decode("utf-8"), ",", data[1].decode("utf-8"))
    else:
        break

print("Finished the application")
