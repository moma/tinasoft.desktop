# -*- coding: utf-8 -*-
from tinasoft.data.Relational import *
from xpcom import components, verbose

class TinasoftDataRelational(Api):
    _com_interfaces_ = components.interfaces.nsITinasoftDataRelational
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        Api.__init__(self)

    def __del__(self):
        if verbose:
            print "TinasoftDataRelational: object is destructing"

    def testXPCOM(self):
        cls = components.classes["Python.TestComponent"]
        ob = cls.createInstance(components.interfaces.nsIPythonTestInterfaceDOMStrings)
        print ob.GetStrings()
