# Installation

## Install a release

### From the packages

#### Linux

Debian packages can be generated and if you can get them, it is the recommended installation. If you cannot then you have to manually compile the components.

#### Windows

You can execute the *msi* installer provided in the [releases](https://github.com/ILLGrenoble/cameo/releases) page.
A short test of the installation is presented [here](windows-test.md).

### Compile the components

Download the latest release:

```
git clone -b r3.0.3 --depth 1 https://github.com/ILLGrenoble/cameo.git
```

Then follow the instructions [here](https://github.com/ILLGrenoble/cameo/blob/master/INSTALL.md).


### List of the installed components

Depending on your &lt;install&gt; directory and &lt;zeromq&gt; implementation you should have:

Server:
``` { .bash .no-copy }
<install>/share/java/cameo/cameo-server-<zeromq>-<version>-full.jar
<install>/bin/cameo-rep-proxy
<install>/bin/cameo-pub-proxy
<install>/bin/cameo-server
```
Console:
``` { .bash .no-copy }
<install>/share/java/cameo/cameo-console-<zeromq>-<version>-full.jar
<install>/bin/cmo
```
C++ API:
``` { .bash .no-copy }
<install>/lib/libcameo-api-cpp.so.<version>
<install>/lib/libcameo-api-cpp.so
<install>/include/cameo/api/
<install>/share/cmake/cameo-api-cpp/
```
Python API:
``` { .bash .no-copy }
<install>/lib/cmake/cameopy/
<install>/lib/python3/dist-packages/cameopy/__init__.py
<install>/lib/python3/dist-packages/cameopy/cameopy.cpython-310-x86_64-linux-gnu.so
```

Debian packages have */usr* as install directory.
From that point, you should have the scripts *cameo-server* and *cmo* accessible from a shell.


## Running a CAMEO server

### Launch the server

Once it is compiled, a CAMEO server can be run using the *cameo-server* script which is running a Java virtual machine. It means that you need at least a Java runtime (>=11) from OpenJDK or Oracle.

Now create the simple configuration *config.xml*:

```xml
<config port="7000">
	<applications>
		<application name="ping" info_arg="no">
			<start executable="/usr/bin/ping"/>
		</application>
	</applications>
</config>
```
How to write a complete configuration file is shown in a next page.
Run the CAMEO server:

```
cameo-server config.xml --log-console
```

If you encounter a problem with the 7000 port then change it e.g. 11000.

### Execute commands using the console

You can contact the CAMEO server using the *cmo* console application. If it is installed:

```
cmo list
```

If you changed the port then you must specify it:

```
cmo -p 11000 list
```

Or you can set the *CAMEO_SERVER* environment variable to *tcp://localhost:11000*.
Open a new shell and run to execute the *ping* app with the *localhost* argument:

```
cmo exec ping localhost
```

The ping lines are streamed and you can stop the app by *ctl-C*.

### Tests

Once you compiled successfully the different components, you can test them at the [Tests](tests.md) page.

## Run the CAMEO server as a service

It is recommended to run your CAMEO server as a service. 

### Linux

On Linux, *systemd* can be used.
We give an example of a user *systemd* configuration:

```
[Unit]
Description=CAMEO Server Service

[Service]
ExecStart=/usr/bin/cameo-server ${HOME}/.cameo/config.xml
WorkingDirectory=/home/user/cameo
Environment=DISPLAY=:0

[Install]
WantedBy=default.target
```

### Windows

The *msi* installer registers the CAMEO service but does not start it. You need to do manually.

The firewall may ask you to accept running applications. If this is not the case, check the allowed apps in the firewall settings and in particular the Java program. Follow the Microsoft [recommendations](https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-defender-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c).


## JZMQ or JeroMQ ?

By default JZMQ is used because it has better performance. The scripts *cameo-server* and *cmo* use JZMQ. However if you prefer to use JeroMQ, the server can be called:
```
java -jar <install directory>/share/java/cameo/cameo-server-jeromq-<version>-full.jar "$@"
```
The console can also be called:
```
java -jar <install directory>/share/java/cameo/cameo-console-jeromq-<version>-full.jar "$@"
```
Using one or another ZeroMQ implementation is discussed in the page [Dependencies](dependencies.md).  

## Use the APIs

### Java

To use the Java API, first add to your Maven POM file the repository:
```xml
<repository>
    <id>ill-repo-releases</id>
    <url>https://maven.ill.fr/content/repositories/releases</url>
</repository>
```

Then add the dependency:
```xml
<dependency>
    <groupId>eu.ill.cameo</groupId>
    <artifactId>cameo-api</artifactId>
    <version>3.0.0</version>
</dependency>
```
By default the JZMQ implementation is used which requires to have the Java ZeroMQ binding installed but if your prefer JeroMQ then add the dependencies:
```xml
<dependency>
    <groupId>eu.ill.cameo</groupId>
    <artifactId>cameo-api</artifactId>
    <version>3.0.0</version>
    <exclusions>
        <exclusion>
            <groupId>eu.ill.cameo</groupId>
            <artifactId>cameo-com-jzmq</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>eu.ill.cameo</groupId>
    <artifactId>cameo-com-jeromq</artifactId>
    <version>1.0.0</version>
</dependency>
```

### C++

If you installed the Debian package or compiled manually and have a correct include path you should be able to include:

```c++
#include <cameo/api/cameo.h>
```

### Python

If you installed the Debian package, it should be available directly or if you compiled manually, you should update the *PYTHONPATH* variable:
```
$ export PYTHONPATH=<cameo install path>/lib/python3/dist-packages:$PYTHONPATH
```
Then you can import in your code:
```python
import cameopy
```
