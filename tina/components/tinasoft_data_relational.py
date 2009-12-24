# -*- coding: utf-8 -*-
from tinasoft.data import Engine
from xpcom import components, verbose, COMException, ServerException, nsError

class TinasoftDataRelational():
    _com_interfaces_ = components.interfaces.nsITinasoftDataRelational
    _reg_clsid_ = "{4ff50853-96cb-4eca-b633-43be1833ae90}"
    _reg_contractid_ = "Python.TinasoftDataRelational"

    def __init__(self):
        return Engine("mozstorage://test-tinasoft.sqlite")
