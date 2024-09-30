import sys
import cameopy
import time
from threading import Event

this = cameopy.This
this.init(sys.argv)

this.setRunning()

stopping = Event()

def stop():
    print("Stop executed")
    stopping.set()
    
this.handleStop(stop, -1)

i = 0
while not stopping.is_set():
    print("Waiting", i, "...")
    time.sleep(0.1)
    i += 1

this.setResult("3097")

this.terminate()

print("Finished the application")
