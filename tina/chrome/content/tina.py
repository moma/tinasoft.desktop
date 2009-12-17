# -*- coding: utf-8 -*-
import mozutils

def tina_doCommand(event):
    item_name = event.target.id
    if item_name == "menu_FileQuitItem":
        mozutils.doQuit(forceQuit=False)
    elif item_name == "menu_About":
        arguments = None
        window.openDialog("chrome://tina/content/about.xul", "about", "centerscreen,modal", arguments)
