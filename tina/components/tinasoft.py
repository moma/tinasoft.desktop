# -*- coding: utf-8 -*-
from tinasoft import TinaApp, ThreadPool
from tinasoft.pytextminer import stopwords

import jsonpickle

import sys
import os
from os import makedirs
from os.path import exists, join

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
        _observerProxy.notifyObservers(None, msg, jsonpickle.encode( returnValue ))
        return jsonpickle.encode( returnValue )

    def importFile( self, returnValue):
        return self.callback( "tinasoft_runImportFile_finish_status", returnValue)

    def processCoocGraph( self, returnValue ):
        return self.callback( "tinasoft_runProcessCoocGraph_finish_status", returnValue)

    def exportCorpora( self, returnValue ):
        return self.callback( "tinasoft_runExportCorpora_finish_status", returnValue)

    def exportGraph( self, returnValue ):
        return self.callback( "tinasoft_runExportGraph_finish_status", returnValue)


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
        TinaApp.notify = _observerProxy.notifyObservers

    def runImportFile(self, *args, **kwargs):
        _observerProxy.notifyObservers(None,
            'tinasoft_runImportFile_running_status',
            jsonpickle.encode( self.STATUS_RUNNING )
        )
            #in wstring filePath,
            #in wstring configFile,
            #in wstring corpora_id,
            #in boolean index,
            #in wstring fileFormat,
            #in wstring overwrite
        self.queueTask( self.importFile, args, kwargs, self.callback.importFile )

    def runExportCorpora(self, *args, **kwargs):
        _observerProxy.notifyObservers(None,
            'tinasoft_runExportCorpora_running_status',
            jsonpickle.encode( self.STATUS_RUNNING )
        )
        def task( *args, **kwargs ):
                #in wstring periods,
                #in wstring corpora_id,
                #in wstring exportPath,
                #in wstring whitelistPath,
                #in wstring userfiltersPath
            args = list(args)
            # args[0] is a STRING of periods id
            args[0] = args[0].split(',')
            # args[1] is a corpora id
            #  args[3] is a white list
            if args[3] != '':
                args[3] = self.getWhitelist( args[3],
                occsCol='occurrences',
                accept='x'
            )
            else:
                args[3] = None
            #  args[4] is an user defined stopwords file
            if args[4] == '':
                args[4] = []
            else:
                args[4] = [stopwords.StopWordFilter( "file://%s" % args[4] )]
            return self.exportCorpora( *args, **kwargs )
        # queue this task
        self.queueTask(task, args, kwargs, self.callback.exportCorpora)



    def runProcessCoocGraph( self, *args, **kwargs ):
        """defines and queues the double task of writing a new cooc matrix into storage and generating the corresponding graph"""
        _observerProxy.notifyObservers(None,
            'tinasoft_runProcessCoocGraph_running_status',
            jsonpickle.encode( self.STATUS_RUNNING )
        )
        def taskCoocGraph( *args, **kwargs ):
                #in wstring whitelistPath,
                #in wstring corpora_id,
                #in wstring periods,
                #in wstring userfiltersPath,
                #in wstring threshold,
            args = list(args)
            whitelistpath = args[0]
            args[0] = self.getWhitelist( whitelistpath,
                occsCol='occurrences',
                accept='x'
            )
            whitelist = args[0]
            corporaid = args[1]
            periods = args[2].split(',')
            args[2] = periods
            if args[3] == '':
                args[3] = []
            else:
                args[3] = [stopwords.StopWordFilter( "file://%s" % args[3] )]
            # first step : cooc matrix
            if self.processCooc( *args, **kwargs ) == self.STATUS_ERROR:
                return self.STATUS_ERROR

            # threshold param parsing
            if args[4] == '':
                threshold = None
            else:
                threshold = map( float,  args[4].split(',') )
            gexfpath = self.getGraphPath( corporaid, periods, threshold )
            # second step : graph generation
            return self.exportGraph( gexfpath, periods, threshold, whitelist, **kwargs )
        # queue this task
        self.queueTask(taskCoocGraph, args, kwargs, self.callback.processCoocGraph)


    def runExportCoocMatrix(self): pass


    def runExportGraph( self, *args, **kwargs ):
        _observerProxy.notifyObservers(None,
            'tinasoft_runExportGraph_running_status',
            jsonpickle.encode( self.STATUS_RUNNING )
        )
        def task( *args, **kwargs ):
                #in wstring corpora_id,
                #in wstring periods,
                #in wstring threshold,
                #in wstring whitelistPath
            args = list(args)
            # whitelist instance
            args[3] = self.getWhitelist( args[3],
                occsCol='occurrences',
                accept='x'
            )
            # periods parsing
            args[1] = args[1].split(',')
            # threshold parsing
            if args[2] == '':
                args[2] = None
            else:
                args[2] = args[2].split(',')
                args[2] = map( float, args[2] )
            # gexf file path
            args[0] = self.getGraphPath( args[0], args[1], args[2] )
            # path, periods, threshold, self.whitelist
            return self.exportGraph( *args, **kwargs )
        self.queueTask(task, args, kwargs, self.callback.exportGraph)

    def walkGraphPath( self, corporaid ):
        """returns the list of files in the gexf directory tree"""
        path = join( self.config['user'], corporaid )
        if not exists( path ):
            return self.serialize( [] )
        return self.serialize( [join( path, file ) for file in os.listdir( path )] )

    def getGraphPath(self, corporaid, periods, threshold):
        """returns the relative path for a given graph in the graph dir tree"""
        path = join( self.config['user'], corporaid )
        if not exists( path ):
            makedirs( path )
        filename = "-".join( periods ) + "_" \
            + "-".join( map(str,threshold) ) \
            + ".gexf"
        #self.logger.debug( join( path, filename ) )
        return join( path, filename )

    def __del__(self):
        """resumes all the transactions when destroying this object"""
        self.joinAll()

    def pythonEnv(self):
        """logs the complete python debug env"""
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
