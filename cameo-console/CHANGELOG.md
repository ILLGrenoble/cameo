1.0.0
-----

* Replaced protobuf by JSON.
* Added server and version commands.
* Removed test command as it is an alias to exec.
* Added long names --app and --endpoint for -a and -e options.
* Endpoint can be defined more simply: hostname:port or tcp://hostname or hostname.
* New option --port, -p.
* New options --mute, -m and --start, -s options to connect.
* New option --console, -c to version.
* Removed listen command as it is done with connect --mute.
* Added stop handler by typing S enter.
* New option --no-stop-handler, -S to disable the stop handler.
* Quit the exec command without killing the application by typing Q.
* Exit with the exit code when the application executed or connected exits with an error.
* Added command connect with id.

0.1.4
-----

* Part of a maven multimodule project.
* Updated all cameo dependencies.

0.1.3
-----

* Depends on cameo-com.

0.1.2
-----

* Replaced the profiles by submodules for jeromq and jzmq.
* Updated to Java 9 and using new features from the Process API to better follow the unmanaged applications.

0.1.0
-----

* JZMQ support.
