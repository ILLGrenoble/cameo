import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

this.setRunning()

for i in range(20):
    print("Printing " + str(i))  
    time.sleep(0.1)

this.terminate()

print("Finished the application")
