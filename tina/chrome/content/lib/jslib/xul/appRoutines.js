if(typeof(JS_LIB_LOADED)=='boolean') 
{

const JS_APPROUTINES_FILE     = "appRoutines.js";
const JS_APPROUTINES_LOADED   = true;


/****************** Common Application Routines *********************/

/**************************** QUIT **********************************/
function jslibQuit() {

	try {
	  var windowManager = C.classes['@mozilla.org/appshell/window-mediator;1']
		                    .getService(C.interfaces.nsIWindowMediator);
    var enumerator = windowManager.getEnumerator(null);
		
		// we are only closing dom windows for now
		// var appShell = C.classes['@mozilla.org/appshell/appShellService;1'].getService();
		// appShell = appShell.QueryInterface(C.interfaces.nsIAppShellService);
		
		while (enumerator.hasMoreElements()) {
		  var domWindow = enumerator.getNext();
			if (("tryToClose" in domWindow) && !domWindow.tryToClose())
			  return false;
			domWindow.close();
		}
		// we are only closing dom windows for now
	  // appShell.quit(C.interfaces.nsIAppShellService.eAttemptQuit);
	} catch (e) {
      jslibPrint(e);
	}

  return true;
}

/**************************** QUIT **********************************/

jslibDebug('*** load: '+JS_APPROUTINES_FILE+' OK');

} // END BLOCK JS_LIB_LOADED CHECK

// If jslib base library is not loaded, dump this error.
else {
   dump("JS_BASE library not loaded:\n"
        + " \tTo load use: chrome://jslib/content/jslib.js\n" 

        + " \tThen: include('chrome://jslib/content/xul/appRoutines.js');\n\n");

}; // END FileSystem Class
