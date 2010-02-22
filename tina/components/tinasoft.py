# -*- coding: utf-8 -*-
from tinasoft import TinaApp, ThreadPool

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

    def callback(self, filename):
        _observerProxy.notifyObservers(None, 'tinasoft_finish_status', None)
        return filename
        # get Desktop directory
        #file = components.classes["@mozilla.org/file/directory_service;1"].\
        #    getService(components.interfaces.nsIProperties).\
        #    get("Desk", components.interfaces.nsIFile)
        #file.append(filename)
        #ios = components.classes["@mozilla.org/network/io-service;1"].\
        #    getService(components.interfaces.nsIIOService)
        #URL = ios.newFileURI(filename)
        #_logger.debug(URL)
        #return URL

        #nsIFilePicker = components.interfaces.nsIFilePicker
        #fp = components.classes["@mozilla.org/filepicker;1"]\
        #    .createInstance(nsIFilePicker)
        #fp.init(window, "Save file", nsIFilePicker.modeOpen)
        #fp.appendFilters(nsIFilePicker.filterAll)
        #rv = fp.show()
        #if (rv == nsIFilePicker.returnOK or rv == nsIFilePicker.returnReplace):
        #    file = fp.file
            #Get the path as string. Note that you usually won't
            #need to work with the string paths.
        #    path = fp.file.path
        #    self.logger.debug(path)
        #    file.append(URL)
            #work with returned nsILocalFile...


class Tinasoft(TinaApp, ThreadPool):
    _com_interfaces_ = components.interfaces.ITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.Tinasoft"
    __name__ = 'Tinasoft'

    def __init__(self, *args, **kwargs):
        TinaApp.__init__(self, *args, **kwargs)
        ThreadPool.__init__(self, 1)
        cb = TinasoftCallback()
        self.callback = cb.callback

    def runImportFile(self, *args, **kwargs):
        _observerProxy.notifyObservers(None, 'tinasoft_running_status', str(args[3]))
        self.queueTask(self.importFile, args, kwargs, self.callback)

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
