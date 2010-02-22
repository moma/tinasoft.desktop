# -*- coding: utf-8 -*-
from tinasoft import TinaApp, ThreadPool

import sys
import os
from distutils import sysconfig
from time import sleep

from xpcom import components, verbose, COMException, ServerException, nsError
import threading

class Tinasoft(TinaApp, ThreadPool):
    _com_interfaces_ = components.interfaces.ITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.Tinasoft"
    __name__ = 'Tinasoft'

    def __init__(self,*args, **kwargs):
        TinaApp.__init__(self, *args, **kwargs)
        ThreadPool.__init__(self, 1)

    def runImportFile(self, *args, **kwargs):
        def callback(returnValue):
            self.logger.debug("end of thread runImportFile " + str(returnValue))
        self.logger.debug("starting a thread")
        self.queueTask(self.importFile, args, kwargs, callback)

    #def runImportFile(self, *args, **kwargs):
    #    def importCallback():
    #        self.logger.debug("End of runImportFile()")
    #        return 1
    #    self.logger.debug("Running asynchronous command")
    #    t = threading.Thread(target=self.importFile,
    #                         args=args, kwargs=kwargs)
    #    t.setDaemon(True)
    #    t.start()
    #    self.logger.debug("running "+ str(t.getName()))

    def __del__(self):
        self.joinAll()

    def pythonEnv(self):
        self.logger.debug( "python environment debug:" )
        for p in sys.path:
           self.logger.debug( p )
        self.logger.debug( "exec_prefix:"+sys.exec_prefix )
        self.logger.debug( "executable:"+sys.executable )

        pyver = sysconfig.get_config_var('VERSION')
        getvar = sysconfig.get_config_var

        #self.logger.debug( "prefix:"+sysconfig.PREFIX )
        #self.logger.debug(  "exec_prefix:"+sysconfig.EXEC_PREFIX )

        flags = ['-I' + sysconfig.get_python_inc(),
                 '-I' + sysconfig.get_python_inc(plat_specific=True)]
        flags.extend(getvar('CFLAGS').split())
        self.logger.debug( ' '.join(flags) )

        libs = getvar('LIBS').split() + getvar('SYSLIBS').split()
        libs.append('-lpython'+pyver)
        # add the prefix/lib/pythonX.Y/config dir, but only if there is no
        # shared library in prefix/lib/.
        libs.insert(0, '-L' + getvar('LIBPL'))
        self.logger.debug( ' '.join(libs) )

