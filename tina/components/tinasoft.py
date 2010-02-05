# -*- coding: utf-8 -*-
from tinasoft import TinaApp


import nltk
#import numpy
#import yaml
#import jsonpickle

from xpcom import components, verbose, COMException, ServerException, nsError

class TinasoftDataRelational(TinaApp):
    _com_interfaces_ = components.interfaces.nsITinasoft
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        TinaApp.__init__()
