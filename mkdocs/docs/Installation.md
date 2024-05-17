# Install a release

## Install the packages

Debian packages are provided in the [releases](https://code.ill.fr/cameo/cameo/-/releases) page.
If you can install them, it is the recommended installation. If you cannot then you have to manually compile the components.

## Compile the components

Download the latest release:

```
$ git clone -b r2.1.1 --depth 1 https://code.ill.fr/cameo/cameo.git
```

Then follow the instructions in [INSTALL.md](https://code.ill.fr/cameo/cameo/-/blob/master/INSTALL.md).
Our experience on the windows compilation is described in [windows compilation](Windows-compilation).

## List of the components

Depending on your install directory you should have:

Server:
```
<install directory>/share/java/cameo/cameo-server-jzmq-<version>-full.jar
<install directory>/share/java/cameo/cameo-server-jeromq-<version>-full.jar
<install directory>/bin/cameo-rep-proxy
<install directory>/bin/cameo-pub-proxy
<install directory>/bin/cameo-server
```
Console:
```
<install directory>/share/java/cameo/cameo-console-jzmq-<version>-full.jar
<install directory>/share/java/cameo/cameo-console-jeromq-<version>-full.jar
<install directory>/bin/cmo
```
C++ API:
```
<install directory>/lib/libcameo-api-cpp.so.<version>
<install directory>/lib/libcameo-api-cpp.so
<install directory>/include/cameo/api/
<install directory>/share/cmake/cameo-api-cpp/
```
Python API:
```
<install directory>/lib/cmake/cameopy/
<install directory>/lib/python3/dist-packages/cameopy/__init__.py
<install directory>/lib/python3/dist-packages/cameopy/cameopy.cpython-310-x86_64-linux-gnu.so
```

Debian packages have */usr* as install directory.
From that point, you should have the scripts *cameo-server* and *cmo* accessible from a shell.


# Running a CAMEO server

## Launch the server

Once it is compiled, a CAMEO server can be run using the *cameo-server* script which is running a Java virtual machine. It means that if you need at least a Java runtime (>=11) from OpenJDK or Oracle.

Now create the simple configuration *config.xml*:

```xml
<config port="7000">
	<applications>
		<application name="ls" info_arg="no">
			<start executable="/usr/bin/ls"/>
		</application>
	</applications>
</config>
```
How to write a complete configuration file is shown in a next page.
Run the CAMEO server:

```
$ cameo-server config.xml --log-console
```

If you encounter a problem with the 7000 port then change it e.g. 11000.

## Execute commands using the console

You can contact the CAMEO server using the *cmo* console application. If it is installed:

```
$ cmo list
```

If you changed the port then you must specify it:

```
$ cmo -p 11000 list
```

Or you can set the *CAMEO_SERVER* environment variable to *tcp://localhost:11000*.
Open a new shell and run to execute the *ls* app:

```
$ cmo exec ls
```


## Tests

Once you compiled successfully the different components, you can test them at the [test all the components](test-all-the-components) page.

# Run the CAMEO server as a service

It is recommended to run your CAMEO server as a service. 

## Linux

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

## Windows

On windows we successfully used [nssm](https://nssm.cc/).
You can install the [JDK](resources/cameo-file-transfer/jdk-14.0.2_windows-x64_bin.exe) and test [cameo-file-transfer.zip](resources/cameo-file-transfer/cameo-file-transfer.zip). Modify the file *config.xml* to reference the correct absolute path to the bin directory of the freshly installed JDK and modify the cameo server bat file to point java on the bin directory of the freshly installed JDK. Test the cameo server by running the bat file and request it from a remote computer. The firewall may ask to accept the connection. If this is not the case, check the allowed apps in the firewall settings and in particular the Java program. Follow the Microsoft [recommendations](https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-defender-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c).

Then register the cameo service using nssm. Run in command line:
```
nssm install cameo
```
Select the cameo server bat file and let the working directory as is. Start the newly installed cameo service by starting *services.msc* in command line and right-click on the cameo service and start.



# JZMQ or JeroMQ ?

By default the JZMQ is used because it has better performance. The scripts *cameo-server* and *cmo* use JZMQ. However if you prefer to use JeroMQ, the server can be called:
```
$ java -jar <install directory>/share/java/cameo/cameo-server-jeromq-<version>-full.jar "$@"
```
The console can also be called:
```
$ java -jar <install directory>/share/java/cameo/cameo-console-jeromq-<version>-full.jar "$@"
```
Using one or another ZeroMQ implementation is discussed in the page [dependencies](dependencies).  

# Use the APIs

## Java

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
    <groupId>fr.ill.ics</groupId>
    <artifactId>cameo-api</artifactId>
    <version>2.2.0</version>
</dependency>
```
By default the JZMQ implementation is used which requires to have the Java ZeroMQ binding installed but if your prefer JeroMQ then add the dependencies:
```xml
<dependency>
    <groupId>fr.ill.ics</groupId>
    <artifactId>cameo-api</artifactId>
    <version>2.2.0</version>
    <exclusions>
        <exclusion>
            <groupId>fr.ill.ics</groupId>
            <artifactId>cameo-com-jzmq</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>fr.ill.ics</groupId>
    <artifactId>cameo-com-jeromq</artifactId>
    <version>0.1.0</version>
</dependency>
```

## C++

If you installed the Debian package or compiled manually and have a correct include path you should be able to include:

```c++
#include <cameo/api/cameo.h>
```

## Python

If you installed the Debian package, it should be available directly or if you compiled manually, you should update the *PYTHONPATH* variable:
```
$ export PYTHONPATH=<cameo install path>/lib/python3/dist-packages:$PYTHONPATH
```
Then you can import in your code:
```python
import cameopy
```
