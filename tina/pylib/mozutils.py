from xpcom import components as Components

def doAlert(message, title=""):
    promptService =  Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService(Components.interfaces.nsIPromptService)
    promptService.alert(None, title, message)

def doQuit(forceQuit=False):
    appStartup = Components.classes['@mozilla.org/toolkit/app-startup;1'].getService(Components.interfaces.nsIAppStartup)
      
    # eAttemptQuit will try to close each XUL window, but the XUL window can cancel the quit
    # process if there is unsaved data. eForceQuit will quit no matter what.
    if forceQuit:
        quitSeverity = Components.interfaces.nsIAppStartup.eForceQuit
    else:
        quitSeverity = Components.interfaces.nsIAppStartup.eAttemptQuit
    appStartup.quit(quitSeverity)
