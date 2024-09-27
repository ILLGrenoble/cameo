#!/bin/bash

cameo_jeromq_jar=examples/java/jeromq/target/cameo-examples-jeromq-full.jar
if [ -f $cameo_jeromq_jar ]
then
  java -jar java/console/jeromq/target/cameo-console-jeromq-3.0.0-full.jar "$@"
  exit
fi

cameo_jzmq_jar=examples/java/jzmq/target/cameo-examples-jzmq-full.jar
if [ -f $cameo_jzmq_jar ]
then
  java -jar java/console/jzmq/target/cameo-console-jzmq-3.0.0-full.jar "$@"
  exit
fi

echo "Examples not built"
