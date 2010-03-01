# -*- coding: utf-8 -*-
from tinasoft import TinaApp, ThreadPool
from tinasoft.pytextminer import stopwords

import jsonpickle

import sys
import os
from distutils import sysconfig

from xpcom import components, verbose, COMException, ServerException, nsError
from xpcom._xpcom import NS_PROXY_SYNC, NS_PROXY_ALWAYS, NS_PROXY_ASYNC, getProxyForObject

_observerSvc = components.classes["@mozilla.org/observer-service;1"].\
            getService(components.interfaces.nsIObserverService)
_observerProxy = getProxyForObject(1, components.interfaces.nsIObserverService, _observerSvc, NS_PROXY_SYNC | NS_PROXY_ALWAYS)

class TinasoftCallback():
    _com_interfaces_ = components.interfaces.koIAsyncCallback
    _reg_clsid_ = "{19ea86a2-fb13-4eb3-8ce1-f62c27664ad9}"
    _reg_contractid_ = "Python.TinasoftCallback"
    __name__ = 'TinasoftCallback'

    def callback(self, msg, returnValue):
        _observerProxy.notifyObservers(None, msg, None)
        return jsonpickle.encode( returnValue )

    def importFile( self, returnValue):
        return self.callback( "tinasoft_runImportFile_finish_status", returnValue)

    def processCooc( self, returnValue ):
        return self.callback( "tinasoft_runprocessCooc_finish_status", returnValue)

    def exportCorpora( self, returnValue ):
        return self.callback( "tinasoft_exportCorpora_finish_status", returnValue)

    def exportGraph( self, returnValue ):
        return self.callback( "tinasoft_exportGraph_finish_status", returnValue)


class Tinasoft(TinaApp, ThreadPool):
    _com_interfaces_ = components.interfaces.ITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.Tinasoft"
    __name__ = 'Tinasoft'

    def __init__(self, *args, **kwargs):
        TinaApp.__init__(self, *args, **kwargs)
        ThreadPool.__init__(self, 1)
        cb = TinasoftCallback()
        self.callback = cb

    def runImportFile(self, *args, **kwargs):
        _observerProxy.notifyObservers(None, 'tinasoft_runImportFile_running_status', str(args[0]))
        self.logger.debug(args)
        self.queueTask( self.importFile, args, kwargs, self.callback.importFile )

    def runExportCorpora(self, *args, **kwargs):
        _observerProxy.notifyObservers(None, 'tinasoft_runExportcorpora_running_status', str(args[0]))
        self.logger.debug(args)
        def task( *args, **kwargs ):
            # args[0] is a json serialized periods id
            # args[1] is a corpora id
            args[3] = self.getWhitelist( args[3] )
            args[4] = [stopwords.StopWordFilter( "file://%s" % args[4] )]
            self.exportCorpora( *args, **kwargs )
        self.queueTask(task, args, kwargs, self.callback.exportCorpora)

    def runProcessCooc(self, *args, **kwargs):
        _observerProxy.notifyObservers(None, 'tinasoft_runProcessCooc_running_status', str(args[0]))
        self.logger.debug(args)
        def task( *args, **kwargs ):
            args[0] = self.getWhitelist( args[0] )
            args[3] = [stopwords.StopWordFilter( "file://%s" % args[3] )]
            self.processCooc( *args, **kwargs )
        self.queueTask(task, args, kwargs, self.callback.processCooc)

    def runExportCoocMatrix(self): pass

    def runExportGraph(self): pass

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
