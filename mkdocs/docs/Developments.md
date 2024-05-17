### Active branches
* **v1**: Branch with new server and old API. For bug resolution.
* **v2**: Branch with new server and new API.

### Versioning

The *cameo* project contains five main projects *Server*, *Console*, *Java API*, *C++ API*, *Python API* that have their own version but are dependent.

Here are the rules for the definition of the version of a project among *Server*, *Console*, *Java API*, *C++ API*, *Python API*:
* **Major**: Incremented for all the projects. A new value means that the server is not compatible with a client with a smaller major version.

* **Minor**: If a project among the five has a new functionality, its new minor value is the maximum of the current five minor values plus one. The *Python API* minor value must be the same as *C++ API* and the binding should be updated when the *C++ API* is updated. If the same functionality is implemented in *Server*, *C++ API*, *Java API*, they can all have the same new minor value.

  E.g. the new version of *Java API* with current versions Server = 1.3.1, Console = 1.2.0, Java API = 1.3.3, C++ API = 1.4.1 is 1.5.0.  

  
* **Revision**: Each project has its own revision value that is incremented when a bug fix or an optimization is done.

When we need to tag the global *cameo* project:
* **Major**: Same as the five projects.
* **Minor**: Maximum of the current five minor values.
* **Revision**: Its own revision value that is incremented when a new tag is made with existing major and minor values.

### New release

When making a new release, follow the steps:
* Check the package versions in the *CMakeLists.txt* files of the different five projects. The version of the *CAMEO* project at the root is not used but should follow the global tag rule.
* Tag the global project. Add release notes so that gitlab considers it is a release.
* Generate the deb packages using *build_and_package.sh*.
* Add the packages in the *releases* directory of the wiki sources of CAMEO.
* Add the packages as assets in the gitlab edit release page of the version.  
  E.g. for the version 1.1.0, the base link is *https://code.ill.fr/cameo/cameo/-/wikis/releases/1.1.0/*.


### Develop on a branch

Sometimes we need to develop two branches on the same machine. Developing an alternative version can be done.
Download the alternative version:
```
$ git clone https://code.ill.fr/cameo/cameo.git --branch v1
```
Compile the Java sources:
```
$ cd cameo
$ mvn clean install
```
Access the server full jar:
```
cameo-server-jzmq/target/cameo-server-jzmq-1.0.0-full.jar
```
Access the console full jar:
```
cameo-console-jzmq/target/cameo-console-jzmq-1.0.0-full.jar
```

Compile and install the C++ API sources into a temporary directory e.g. */tmp/cameo-install* :
```
$ cd cameo-api-cpp
$ mkdir build
$ cd build
$ cmake -DCMAKE_INSTALL_PREFIX=/tmp/cameo-install ..
$ cmake --build . --target install
```
Access the include and .so files from the temporary directory.
