# wix-test

Install extension:

$ wix extension list
$ wix extension add WixToolset.UI.wixext


Compile msi:

$ wix build -ext WixToolset.UI.wixext cameo.wxs

Debug with log:

$ msiexec /i cameo.msi /l* cameo.log

