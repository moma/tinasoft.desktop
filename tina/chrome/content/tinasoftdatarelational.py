# -*- coding: utf-8 -*-
import mozutils
from xpcom import components, verbose

def tina_doCommand(event):
    item_name = event.target.id
    if item_name == "menu_FileQuitItem":
        mozutils.doQuit(forceQuit=False)
    elif item_name == "menu_About":
        arguments = None
        window.openDialog("chrome://tina/content/about.xul", "about", "centerscreen,modal", arguments)

def pytest(event):
    cls = components.classes["Python.TinasoftDataRelational"]
    ob = cls.createInstance(components.interfaces.nsITinasoftDataRelational)
    print ob.GetStrings()
