
/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/about.xul";

/* Tinasoft SINGLETON */

if ( typeof(Tinasoft) == "undefined" ) {
    cls = Cc["Python.Tinasoft"];
    var Tinasoft = cls.createInstance(Ci.ITinasoft);
}

