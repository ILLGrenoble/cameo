# Windows test

If you installed the service using the *msi* executable then the CAMEO service is registered but not running.

You can start it manually by launching *services.msc* in command line.

Once done, open a terminal, the *cmo* program is accessible:

```
cmo
```

Display the list of available applications:

```
cmo list
```

If it is blocking then the CAMEO server is not running. You should see the apps *file-transfer-server* and *file-transfer-client*.
Start the server:

```
cmo exec file-transfer-server
```

It is blocking the terminal so open a new terminal and run the client with an existing text file:

```
cmo exec file-transfer-client read text "C:\/Users/john\/Documents\/test.txt" "C:\/Users/john\/"
```

If the file exists, it will be copied in the target directory.
