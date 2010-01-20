/* application prefs */
pref("general.startup.tina",true);
pref("toolkit.defaultChromeURI", "chrome://tina/content/tina.xul");

/* magic hack */
pref("capability.principal.codebase.p0.granted", "UniversalXPConnect");
pref("capability.principal.codebase.p0.id", "file:///"); 

/* debugging prefs, see:
 *   https://developer.mozilla.org/en/Debugging_a_XULRunner_Application
 */
pref("browser.dom.window.dump.enabled", true);
pref("javascript.options.showInConsole", true);
pref("javascript.options.strict", true);
pref("nglayout.debug.disable_xul_cache", true);
pref("nglayout.debug.disable_xul_fastload", true);
pref("tina.desktop.visualization.appletparams.engine", "software");
