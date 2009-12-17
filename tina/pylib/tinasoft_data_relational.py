from xpcom import components, verbose

# -*- coding: utf-8 -*-
from tinasoft.data.Relational import *
from xpcom import components, verbose

class TinasoftDataRelational(Api):
    _com_interfaces_ = components.interfaces.nsITinasoftDataRelational
    _reg_clsid_ = "{c456ceb5-f142-40a8-becc-764911bc8ca5}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        Api.__init__(self)
        if verbose:
            print "TinasoftDataRelational: __init__ method called"

    def __del__(self):
        if verbose:
            print "TinasoftDataRelational: __del__ method called - object is destructing"

    def testXPCOM(self):
        cls = components.classes["@mozilla.org/sample;1"]
        ob = cls.createInstance(components.interfaces.nsISample)
        # nsISample defines a "value" property - let's use it!
        ob.value = "nsISample test value"
        if ob.value != "nsISample test value":
            print "Eeek - what happened?"
        else:
            print ob.value
