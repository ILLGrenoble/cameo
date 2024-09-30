import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

this.setResult("test result")

this.terminate()

print("Finished the application")
