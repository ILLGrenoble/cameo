# App registration

## Registered and unregistered apps

CAMEO is very flexible and accepts different combinations for an application to be controlled by CAMEO.

### *This* initialized with the *arguments* variable

Usually to benefit from the CAMEO services inside an application, the *This* object is initialized by passing the arguments of the program. In C++:
```cpp
int main(int argc, char *argv[]) {

    // Initialize This.
    cameo::This::init(argc, argv);
```

In Java:
```java
import eu.ill.cameo.base.This;

public static void main(String[] args) {

    // Initialize This.
    This.init(args);
```

In Python:
```python
import sys
import cameopy

## Initialize This.
cameopy.This.init(sys.argv)
```

Once *This* is initialized it can be used for example to get a reference to the CAMEO server that started it. When *This* is initialized with the *arguments* variable, there are two cases:

* **Registered application**: If the application is registered in the configuration file, then *info_arg* must be *yes* which is the default value so it is not necessary to specify it. Otherwise the application will not start.
* **Not registered application**: If the application is not registered in the configuration file, it is possible to start directly the app by adding a last argument that contains the CAMEO server reference and its name. For instance:
```
/home/cameo/app1 "{\"name\":\"App1\", \"server\":\"tcp://localhost:10000\"}"
```
Then if the passed arguments are correct, *This* will initialize and the application will become **attached** to the CAMEO server referenced by the endpoint in the *server* value.

### *This* initialized with explicit arguments

It is not mandatory to initialize *This* with the program arguments so that it is possible to pass an explicit server endpoint.  
In C++:
```cpp
cameo::This::init("App1", "tcp://localhost:10000");
```
In Java:
```java
This.init("App1", "tcp://computer1:10000");
```
In Python:
```python
cameopy.This.init("App1", "tcp://computer2:10000")
```

The application shall **not** be **registered** and the app will be started locally and not by a CAMEO server. Then in the *init()* call, the CAMEO server is contacted to attach the app. As the app is not started by a CAMEO server, the CAMEO server extra functionalities are not available (output stream, log, stopping time, etc.).

The endpoint is not necessarily local and a remote server endpoint can be used as shown in the Java and Python examples. In that case, it is possible to trigger a stop handler as explained in [Control the apps](http://control-the-apps.md) that will provoke the termination of the app. But a direct stop or kill are not possible. Moreover, there is no timeout on the attach communication so that it could block indefinitely in case of a network failure.

To conclude, it is possible to attach an app to a remote CAMEO server however it is not recommended and should be done only when it is not possible to have a local server.

### *This* not initialized

If you have a black box application i.e. that you cannot compile or modify, then you cannot initialize *This* inside but you can still **register** the application in the configuration file. However it is recommended in that special case to set *info_arg* to *no*. Otherwise the additional argument may not be supported by the app when it parses the arguments. The application is **unmanaged**.

### Registered vs unregistered

We saw the different cases based on the initialization of *This* or not. But what is the difference between a registered app and an unregistered app that is attached?  

The response is simple: a registered app can be started by the console *cmo* from another computer. Moreover in case of unexpected termination a program can be run to make a report.  

Registering an app offers more flexibility in the way to start an app.

## Register a script

If you want to register a script e.g. a Bash script or a Python script, it is highly recommended to define the executable with the interpreter program rather the script itself even if it is executable. For example:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config host="mycomputer">
    <applications>
        <application name="Script1">
            <start executable="/usr/bin/bash" args="/home/cameo/app1.sh"/>
        </application>
        <application name="Script2">
            <start executable="/usr/bin/python3" args="-u /home/cameo/app2.py"/>
        </application>
    <applications>	
</config>
```
The reason is because the underlying process execution works not well when the executable is the script.