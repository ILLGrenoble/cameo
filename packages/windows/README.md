# Install with Wix

Install the wix software and install the extension:

```
wix extension add WixToolset.UI.wixext
```

Copy the cameo-server.jar, cameo-console.jar and cameo-file-transfer.jar files in the current directory.
Compile msi:

```
wix build -ext WixToolset.UI.wixext cameo.wxs
```

Debug the installation with log:

```
msiexec /i cameo.msi /l* cameo.log
```
