#!/bin/bash

java=false

cameo_jeromq_jar=examples/java/jeromq/target/cameo-examples-jeromq-full.jar
if [ -f $cameo_jeromq_jar ]
then
  export CLASSPATH=$cameo_jeromq_jar
  java=true
fi

cameo_jzmq_jar=examples/java/jzmq/target/cameo-examples-jzmq-full.jar
if [ -f $cameo_jzmq_jar ]
then
  export CLASSPATH=$cameo_jzmq_jar
  java=true
fi

cameo_proxy=build/cpp/proxy/cameo-rep-proxy
if [ -f $cameo_proxy ]
then
  export PATH=build/cpp/proxy:$PATH
fi

cameo_examples_cpp=build/examples/cpp/requesterapp
if [ -f $cameo_examples_cpp ]
then
  export PATH=build/examples/cpp:$PATH
fi

cameo_python_api=build/python/api/cameopyConfigVersion.cmake
if [ -f $cameo_python_api ]
then
  export PYTHONPATH=build/python/api:$PYTHONPATH
fi

echo "PATH $PATH"


if [ "$java" = true ]
then
  java eu.ill.cameo.server.Server examples/config.xml --log-console
else
  echo "Cannot run the server without Java build"
  exit
fi
