@echo off
@setlocal

rem Copy and rename the server and console jars
echo "Copy the server and console jars"

cd "%~dp0"\..\..\build\java\server-jeromq\installed\share\java\cameo
for /r %%f in (*.jar) do copy %%f "%~dp0\cameo-server.jar"

cd "%~dp0"\..\..\build\java\console-jeromq\installed\share\java\cameo
for /r %%f in (*.jar) do copy %%f "%~dp0\cameo-console.jar"

rem Go to base directory
cd "%~dp0"

rem Get cameo file transfer
git clone https://github.com/ILLGrenoble/cameo-file-transfer.git

cd cameo-file-transfer

rem call mvn to not exit after the command
call mvn -B package
cd target
dir

for /r %%f in (*full.jar) do copy %%f "%~dp0\cameo-file-transfer.jar"

rem Go to base directory
cd "%~dp0"

rd /s /q cameo-file-transfer

echo List of installed extensions:
wix extension list

rem Build the msi
wix build -ext WixToolset.UI.wixext/5.0.2 cameo.wxs