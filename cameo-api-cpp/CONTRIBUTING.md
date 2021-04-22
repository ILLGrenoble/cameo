## Development 
How to find the list of public headers and check that they are all in the include/ directory
 1. move all the headers in src/
 2. move cameo.h to include/
 3. run the following command multiple times until there is no output
```
#!/bin/fish
set -l ORIG_DIR src
for f in (for file in include/*; grep include $file | grep '"'; end | sort | uniq | sed 's|.* "||;s|"||'); if [ -e $ORIG_DIR/$f ] ; echo $f; git mv $ORIG_DIR/$f include/; end; end
```
