@echo off
@setlocal

set "cameo_jeromq_jar=examples\java\jeromq\target\cameo-examples-jeromq-full.jar"
if exist "%cameo_jeromq_jar%" (
  java -jar java\console\jeromq\target\cameo-console-jeromq-3.0.0-full.jar %*
  exit /b 0
)

set "cameo_jzmq_jar=examples\java\jzmq\target\cameo-examples-jzmq-full.jar"
if exist "%cameo_jzmq_jar%" (
  java -jar java\console\jzmq\target\cameo-console-jzmq-3.0.0-full.jar %*
  exit /b 0
)

echo Examples not built
exit /b 1