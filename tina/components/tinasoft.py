# -*- coding: utf-8 -*-
from tinasoft import TinaApp

import sys
import os

print ""
print "pythonlib paths:"
for p in sys.path:
   print p

print ""

print "exec_prefix:",sys.exec_prefix

print "executable:",sys.executable



from distutils import sysconfig

pyver = sysconfig.get_config_var('VERSION')
getvar = sysconfig.get_config_var

if True:

    print "prefix:",sysconfig.PREFIX

    print  "exec_prefix:",sysconfig.EXEC_PREFIX

    flags = ['-I' + sysconfig.get_python_inc(),
             '-I' + sysconfig.get_python_inc(plat_specific=True)]
    flags.extend(getvar('CFLAGS').split())
    print ' '.join(flags)

    libs = getvar('LIBS').split() + getvar('SYSLIBS').split()
    libs.append('-lpython'+pyver)
    # add the prefix/lib/pythonX.Y/config dir, but only if there is no
    # shared library in prefix/lib/.
    libs.insert(0, '-L' + getvar('LIBPL'))
    print ' '.join(libs)

from xpcom import components, verbose, COMException, ServerException, nsError

class Tinasoft(TinaApp):
    _com_interfaces_ = components.interfaces.ITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.Tinasoft"

    def __init__(self):
        TinaApp.__init__(self)
