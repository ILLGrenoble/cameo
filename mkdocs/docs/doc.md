# Documentation

To generate the documentation, you need *mkdocs material* and *doxygen*. You can get *mkdocs* using *pip*:
First generate a virtual environment:

```
python3 -m venv .venv
```

Activate the environment:

```
source .venv/bin/activate
```

Install the dependency:

```
pip install mkdocs-material
```

Do not forget to activate the option *CAMEO_DOC* while configuring with *CMake*.

You can test the site:

```
cd mkdocs
mkdocs serve
```

To publish the site, replace the *docs* directory with the *build/docs* directory then commit and push to the repository.