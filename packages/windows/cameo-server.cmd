@echo off
@setlocal

java -jar "%~dp0\cameo-server.jar" --log-console "%~dp0\config.xml"