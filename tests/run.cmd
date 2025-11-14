@echo off
@setlocal

set "java=false"
set "proxy=false"
set "cpp=false"
set "python=false"

echo Configure the paths

set "cameo_jeromq_jar=tests\java\jeromq\target\cameo-tests-jeromq-full.jar"
if exist "%cameo_jeromq_jar%" (
  set "CLASSPATH=%cameo_jeromq_jar%"
  set "java=true"
)

set "cameo_jzmq_jar=tests\java\jzmq\target\cameo-tests-jzmq-full.jar"
if exist "%cameo_jzmq_jar%" (
  set "CLASSPATH=%cameo_jzmq_jar%"
  set "java=true"
)

if "%java%" == "false" (
  echo Cannot run the server without Java build
  exit /b 1
)

echo set CLASSPATH=%CLASSPATH%

set "cameo_proxy=build\cpp\proxy\Release\cameo-rep-proxy.exe"
if exist "%cameo_proxy%" (
  set "PATH=build\cpp\proxy\Release;%PATH%"
  echo set PATH=build\cpp\proxy\Release;%%PATH%%
  set "proxy=true"
)

set "cameo_tests_cpp=build\tests\cpp\Release\testsimple.exe"
if exist "%cameo_tests_cpp%" (
  set "PATH=build\tests\cpp\Release;%PATH%"
  echo set PATH=build\tests\cpp\Release;%%PATH%%
  set "cpp=true"
)

set "cameo_python_api=build\python\api\cameopyConfigVersion.cmake"
if exist "%cameo_python_api%" (
  set "PYTHONPATH=build\python\api\Release;%PYTHONPATH%"
  echo set PYTHONPATH=build\python\api\Release;%%PYTHONPATH%%
  set "python=true"
)

rem Copy the dlls to ensure they are loaded at runtime
copy build\cpp\api\Release\*.dll build\python\api\Release

if "%~1" == "setup" (
  exit /b 0
)

if "%java%" == "true" (
  java eu.ill.cameo.test.TestSelector java 1
  if "%proxy%" == "true" (
    java eu.ill.cameo.test.TestSelector java 1 true
  )
)

if "%cpp%" == "true" (
  java eu.ill.cameo.test.TestSelector cpp 1
  if "%proxy%" == "true" (
    java eu.ill.cameo.test.TestSelector cpp 1 true
  )
)

if "%python%" == "true" (
  java eu.ill.cameo.test.TestSelector python 1
  if "%proxy%" == "true" (
    java eu.ill.cameo.test.TestSelector python 1 true
  )
)
