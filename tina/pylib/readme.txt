Python packages go in this directory.

With a directory structure like this:
  foo/
    __init__.py
    bar.py

you can perform imports in Python code like:
  import foo
  import foo.bar
  from foo import bar
  etc...
