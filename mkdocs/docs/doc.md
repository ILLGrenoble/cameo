# Documentation

To generate the documentation, you need *mkdocs material* and *doxygen*. You can get *mkdocs* and *mkdocs-material* using *pip* or with a distribution package.
Do not forget to activate the option *CAMEO_DOC* while configuring with *CMake*.

You can test the site:

```
cd mkdocs
mkdocs serve
```

To publish the site, replace the *docs* directory with the *build/docs* directory then commit and push to the repository.