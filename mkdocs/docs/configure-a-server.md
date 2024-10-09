# Configure a server

## The main configuration file
The main configuration file of a CAMEO server is an XML file usually called *config.xml* but the name is not imposed. We already saw a minimal example where no application were registered. Let's discover the different options. 

Let's take an example with two applications registered:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config host="mycomputer" port="7000" proxy_ports="10000, 10001, 10002"
        polling_time="100" sleep_time="10"
        max_applications="20" 
        log_level="FINE" log_directory="/home/cameo/log">
    <applications>
        <application name="App1" description="My application"
                     working_directory=""
                     starting_time="inf" stopping_time="20" 
                     multiple="no" restart="no" info_arg="yes"
                     stream="yes"
                     log_directory="default"
                     environment="app.properties">
            <start executable="/home/cameo/app1" args="-option test"/>
        </application>
        <application name="App2">
            <start executable="/home/cameo/app2"/>
            <stop executable="kill" args="-2"/>
            <error executable="bash" args="/home/cameo/error.sh -debug"/>
        </application>
    <applications>	
</config>
```

The root tag is *config* which has the *applications* tag as child which has many *application* tags as children. Each *application* tag has a mandatory *start* child tag and optional *stop* and *error* children.

Attributes of the *config* tag:

**Attribute** | **Default value**     | **Description**
--------------|-----------------------|----------------
host          | The default interface | Hostname or "IP" or an IPv4 address or localhost to override the default interface. If "IP" then the IPv4 address related to localhost is provided.
port          | 7000                  | The base port of the server.
proxy_ports   |                       | The three ports used by the proxy. The first two ports are accessible from outside, the third one is for internal use. By default, proxies are not started.
polling_time  | 100                   | Sleep duration in milliseconds between some phases of the lifecycle of an app and sleep duration in the output stream retrieval of an app.
sleep_time    | 5                     | Sleep duration in milliseconds between two requests process. It avoids the server to stall if there are two many pending requests.
max_applications | 65536              | Max number of running apps at a time.
log_level     | INFO                  | Log level. Possible values are OFF, INFO, FINE, FINER, FINEST.
log_directory | .                     | The directory where the file *cameo.log* is written.

Attributes of the *application* tag:

**Attribute**       | **Default value**     | **Description**
--------------------|-----------------------|----------------
name                |                       | The name of the app, given when a *start* is requested. This is a mandatory attribute.
description         |                       | The description of the app.
working_directory   | .                     | The directory where the app is executed.
starting_time       | 0                     | The duration in seconds after which the app becomes *RUNNING*. Possible value is a positive integer or *inf* for infinity.
stopping_time       | 10                    | The duration in seconds after which the app is killed when a *stop* is requested. Possible value is a positive integer or *inf* for infinity.
multiple            | inf                   | If *no*, then only one instance is accepted. If *yes* or *inf*, there is no limit on the number of parallel instances. A number indicates the maximum of parallel instances. Possible values are *yes*, *no*, *inf* or a strictly positive number.
restart             | no                    | If *yes*, the app is automatically restarted when it died unexpectedly. Possible values are *yes* and *no*.
info_arg            | yes                   | If *yes*, an additional argument containing information about the app is passed. For unmanaged application, it can be necessary to set it to *no*. Possible values are *yes* and *no*.
stream              | yes                  | If *yes*, the standard error and output stream are published to the console application and other instance reference objects. Possible values are *yes* and *no*.
log_directory       |                       | The directory where the *&lt;name&gt;.&lt;id&gt;.log* file of the app instance is written. An empty value means no log, a *default* value means it inherits the value of the *config* tag.
environment         |                       | The path to the properties file where environment variables dedicated to the app can be defined. It can be absolute or relative to the folder of the main configuration file.


Attributes of the *start*, *stop*, *error* tags:

**Attribute**       | **Default value**     | **Description**
--------------------|-----------------------|----------------
executable          |                       | The path to the program executed.
args                |                       | The arguments passed to the executable.

The *start* tag is mandatory, the associated executable program is executed when a *start* is requested. The arguments *args* are first set then come the arguments passed to the *start* request and then the last argument is added if *info_arg* is *yes*. 

When the *stop* tag is defined, a *stop* command is executed corresponding to the executable with the arguments *args*. It can be useful for programs that can be stopped nicely with a signal. However for managed CAMEO apps, it is better to define a stop handler that is automatically triggered when a *stop* is requested.

When the *error* tag is defined, an *error* command is executed when the application finishes with an error. That can be a segmentation fault for a C++ program or an exception for a Java program. In that case, the error can be processed e.g. analyze the generated core to send an email.

## Environment files

An application often depends on environment variables. When an application is launched on a local account, some specific environment variables may have been defined. However when the application is launched by a CAMEO server, it may not work, because some environment variables are missing. Indeed the environment is not the one of the local account even if the CAMEO server is launched as a user service. The *environment files* are there to solve this problem.

An example with the *app.properties* file:
```
MYAPP_HOME=/home/cameo/myapp
LD_LIBRARY_PATH=/usr/local/lib
PATH=/home/cameo/bin:$PATH
```

The variables are written using a standard *shell* syntax, and composition is possible. These variables are applied to the context of the launched process. So be careful with the special *PATH* variable: it cannot be the path of the executable. However if you need to execute a program inside your app, then this *PATH* will be used. Be also careful with *LD_LIBRARY_PATH* which is for security reason not inherited. It means that it is empty by default.

If your app has a graphical user interface, you may need to check the variable *DISPLAY* and add it to the environment file if necessary.

One practical way to check the required environment variables is to launch the app in a working environment e.g. on a local account and print the list of environment variables.

## Log

The CAMEO server application is logging in the *cameo.log* file and it is also possible to log the standard error and output of the applications started by the CAMEO server.

Indeed when you need to debug an application, the standard error and output may be useful. 
CAMEO applications are started in background and it is possible to log the standard error and output. For that, you need to define the *log_directory* attribute of the application tag with a path that exists or the *default* value which is the location of the *cameo.log* file. Each running CAMEO application has a unique id provided by the server and the produced log file is *&lt;name&gt;.&lt;id&gt;.log* for an app with the name *name* and id *id*. For instance it could be *App2.12.log*.

## Start the server

Once the configuration file and its associated environment files have been defined, the CAMEO server can be started. The server can be started directly in a shell however it is recommended to start it as a service. See the [Installation](installation.md) page for more details.

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

It is not mandatory to initialize *This* with the program arguments. It is possible to pass an explicit endpoint that must be a **local** endpoint because it has no sense to attach the app to a remote computer.  
In C++:
```cpp
cameo::This::init("App1", "tcp://localhost:10000");
```
In Java:
```java
This.init("App1", "tcp://localhost:10000");
```
In Python:
```python
cameopy.This.init("App1", "tcp://localhost:10000")
```
In that case, the application shall **not** be **registered**.

### *This* not initialized

If you have a black box application i.e. that you cannot compile or modify, then you cannot initialize *This* inside but you can still **register** the application in the configuration file. However it is recommended in that special case to set *info_arg* to *no*. Otherwise the additional argument may not be supported by the app when it parses the arguments.

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

## Ports

The implementation of a CAMEO server is based on ZeroMQ and a sockets of different types are open. Here are the different ports:

* **Base**: This is the port of the server endpoint. By default it is 7000 but it can be defined by the *port* attribute in the configuration file.
* **Status**: This is the port from which are published the events of status of the different running applications.
* **Stream**: Each application for which the attribute *stream* is set to *yes* publishes the standard error and output on a stream port.
* **Coms**: We will see later that the provided coms also use some ports for their implementation.
* **Proxy**: The ports used by the proxies. See section [Use the proxies with a firewall](use-the-proxies-with-a-firewall.md) for more details.

Except the base port and the proxy ports that are fixed meaning that they must be free before starting the CAMEO server, all the other ports are **dynamically assigned** i.e. they will surely assigned.

## Stop and error executables

If you need to define a stop executable then you can send a signal to the process of the application. For instance:
```xml
<stop executable="kill" args="-2"/>
```
The *PID* of the running application is added as a last argument.

If you need to define an error executable, you can for instance define:
```xml
<error executable="bash" args="/home/cameo/error.sh -debug"/>
```
If the running application terminates with an error the following arguments will be added to the command executed: id, error code, state before the error. For instance:
```
bash /home/cameo/error.sh -debug 13 139 RUNNING
```
These information can be used to send a report by email.