HOW-TO INSTALL PYTHON MODULE INTO THE EMBEDDED PYTHON LIBRARY ???

1- WITH PYTHON SOURCE CODE DISTRIBUTIONS

  - unpack the source archive
  - execute the following commands

cd $WORKPATH/tinasoft.desktop/install/python
export PYTHONHOME=$WORKPATH/tinasoft.desktop/tina/xulrunner/python
# under mac: use DYLD_LIBRARY_PATH
export DYLD_LIBRARY_PATH=$PYTHONHOME/lib
cd python-src
chmod 700 $PYTHONHOME/bin/python2.6
$PYTHONHOME/bin/python2.6 setup.py build
$PYTHONHOME/bin/python2.6 setup.py install

2- WITH PYTHON EGGS

TODO
