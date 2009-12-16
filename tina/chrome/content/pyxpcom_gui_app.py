import mozutils

def pyxpcom_gui_app_doCommand(event):
    item_name = event.target.id
    if item_name == "menu_FileQuitItem":
        mozutils.doQuit(forceQuit=False)
    elif item_name == "menu_About":
        arguments = None
        window.openDialog("chrome://pyxpcom_gui_app/content/about.xul", "about", "centerscreen,modal", arguments)
