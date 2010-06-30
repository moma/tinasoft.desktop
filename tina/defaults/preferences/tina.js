/* application prefs */
pref("general.startup.tina",true);
pref("toolkit.defaultChromeURI", "chrome://tina/content/tina.xul");

pref("plugin.expose_full_path",true);

/* hack to stop system-wide java plugin scans */
//pref("plugin.scan.SunJRE", "1.9");
//pref("plugin.scan.4xPluginFolder", false);
//pref("plugin.scan.plid.all", false);
pref("plugin.scan.WindowsMediaPlayer", "19.0");

//pref("java.default_java_location_others","/home/jbilcke/Checkouts/git/TINA/tinasoft.desktop/java");
//pref("java.java_plugin_library_name","libnpjp2");

/* magic hack */
pref("capability.principal.codebase.p0.granted", "UniversalXPConnect");
pref("capability.principal.codebase.p0.id", "file:///");

/* debugging prefs, see:
 *   https://developer.mozilla.org/en/Debugging_a_XULRunner_Application
 */
pref("browser.dom.window.dump.enabled", true);
pref("javascript.options.showInConsole", true);
pref("javascript.options.strict", false);
pref("nglayout.debug.disable_xul_cache", true);
pref("nglayout.debug.disable_xul_fastload", true);
pref("tina.desktop.visualization.appletparams.engine", "software");

pref("browser.download.folderList", 0);
pref("browser.download.useDownloadDir", true);
