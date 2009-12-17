from xpcom import components, verbose
from tinasoft.data import *

class TinasoftDataRelational(Relational):
    _com_interfaces_ = components.interfaces.nsITinasoftDataRelational
    _reg_clsid_ = "{c456ceb5-f142-40a8-becc-764911bc8ca5}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        Relational.__init__(self)
        if verbose:
            print "TinasoftDataRelational: __init__ method called"

    def __del__(self):
        if verbose:
            print "TinasoftDataRelational: __del__ method called - object is destructing"
