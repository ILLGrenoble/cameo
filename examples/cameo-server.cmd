@echo off
@setlocal

set "java=false"

set "cameo_jeromq_jar=examples\java\jeromq\target\cameo-examples-jeromq-full.jar"
if exist "%cameo_jeromq_jar%" (
  set "CLASSPATH=%cameo_jeromq_jar%"
  set "java=true"
)

set "cameo_jzmq_jar=examples\java\jzmq\target\cameo-examples-jzmq-full.jar"
if exist "%cameo_jzmq_jar%" (
  set "CLASSPATH=%cameo_jzmq_jar%"
  set "java=true"
)

set "PATH=build\cpp\api\Release;%PATH%"

set "cameo_proxy=build\cpp\proxy\Release\cameo-rep-proxy.exe"
if exist "%cameo_proxy%" (
  set "PATH=build\cpp\proxy\Release;%PATH%"
)

set "cameo_examples_cpp=build\examples\cpp\Release\requesterapp.exe"
if exist "%cameo_examples_cpp%" (
  set "PATH=build\examples\cpp\Release;%PATH%"
)

set "cameo_python_api=build\python\api\cameopyConfigVersion.cmake"
if exist "%cameo_python_api%" (
  set "PYTHONPATH=build\python\api\Release;%PYTHONPATH%"
)


if "%java%" == "false" (
  echo Cannot run the server without Java build
  exit /b 1
)

java eu.ill.cameo.server.Server examples\config.xml --log-console



