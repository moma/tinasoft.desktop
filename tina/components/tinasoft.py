# -*- coding: utf-8 -*-
from tinasoft import TinaApp

import sys
import os
from distutils import sysconfig

from xpcom import components, verbose, COMException, ServerException, nsError
import threading

#class tinaAsync(koAsyncOperationBase):
#    def __call__(self, *args, **kwargs):
#        self.args = args
#        self.kwargs = kwargs
#        return self.run()


class Tinasoft(TinaApp):
    _com_interfaces_ = components.interfaces.ITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.Tinasoft"
    __name__ = 'Tinasoft'

    def __init__(self,*args, **kwargs):
        TinaApp.__init__(self, *args, **kwargs)
        # Get a handle on the Komodo asnychronous operations service. Used for
        # checking and displaying a in-progress image on the tree view.
        #self._asyncOpSvc = components.classes['@activestate.com/koAsyncService;1'].\
        #        getService(components.interfaces.koIAsyncService)

    #def __call__(self):
    #    return self



    def runImportFile(self, *args, **kwargs):
        def importCallback():
            self.logger.debug("End of runImportFile()")
            return 1
        self.logger.debug("Running asynchronous command")
        t = threading.Thread(target=self.importFile,
                             args=args, kwargs=kwargs)
        t.setDaemon(True)
        t.start()
        self.logger.debug("running "+ str(t.getName()))
        # TODO Callback and progress

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

# Tinasoft singleton
#Tinasoft=tinasoft()
