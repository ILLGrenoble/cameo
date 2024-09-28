#!/bin/bash

java=false
proxy=false
cpp=false
python=false

echo "Configure the paths"

cameo_jeromq_jar=tests/java/jeromq/target/cameo-tests-jeromq-full.jar
if [ -f $cameo_jeromq_jar ]
then
  export CLASSPATH=$cameo_jeromq_jar
  echo "export CLASSPATH=$cameo_jeromq_jar"
  java=true
else
  cameo_jzmq_jar=tests/java/jzmq/target/cameo-tests-jzmq-full.jar
  if [ -f $cameo_jzmq_jar ]
  then
    export CLASSPATH=$cameo_jzmq_jar
    echo "export CLASSPATH=$cameo_jzmq_jar"
    java=true
  else
    echo "Cannot run the tests without Java build"
    exit
  fi
fi

cameo_proxy=build/cpp/proxy/cameo-rep-proxy
if [ -f $cameo_proxy ]
then
  export PATH=build/cpp/proxy:$PATH
  echo "export PATH=build/cpp/proxy:\$PATH"
  proxy=true
fi

cameo_tests_cpp=build/tests/cpp/testsimple
if [ -f $cameo_tests_cpp ]
then
  export PATH=build/tests/cpp:$PATH
  echo "export PATH=build/tests/cpp:\$PATH"
  cpp=true
fi

cameo_python_api=build/python/api/cameopyConfigVersion.cmake
if [ -f $cameo_python_api ]
then
  export PYTHONPATH=build/python/api:$PYTHONPATH
  echo "export PYTHONPATH=build/python/api:\$PYTHONPATH"
  python=true
fi

echo ""

if [ "$java" = true ]
then
  java eu.ill.cameo.test.TestSelector java 1
  if [ "$proxy" = true ]
  then
    java eu.ill.cameo.test.TestSelector java 1 true
  fi
fi

if [ "$cpp" = true ]
then
  java eu.ill.cameo.test.TestSelector cpp 1
  if [ "$proxy" = true ]
  then
    java eu.ill.cameo.test.TestSelector cpp 1 true
  fi
fi

if [ "$python" = true ]
then
  java eu.ill.cameo.test.TestSelector python 1
  if [ "$proxy" = true ]
  then
    java eu.ill.cameo.test.TestSelector python 1 true
  fi
fi
