@echo off
@setlocal

rem Install wix with dotnet
dotnet tool install --global wix --version 5.0.2

rem Add the necessary extension
wix extension add WixToolset.UI.wixext/5.0.2

rem Verify the extension
echo List of installed extensions:
wix extension list