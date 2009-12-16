/* application prefs */
pref("general.startup.pyxpcom_gui_app",true);
pref("toolkit.defaultChromeURI", "chrome://pyxpcom_gui_app/content/pyxpcom_gui_app.xul");

/* debugging prefs, see:
 *   https://developer.mozilla.org/en/Debugging_a_XULRunner_Application
 */
pref("browser.dom.window.dump.enabled", true);
pref("javascript.options.showInConsole", true);
pref("javascript.options.strict", true);
pref("nglayout.debug.disable_xul_cache", true);
pref("nglayout.debug.disable_xul_fastload", true);

