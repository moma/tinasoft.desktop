
/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/about.xul";

/* Tinasoft SINGLETON */
function TinasoftSingle( ) {
    this.cls = Cc["Python.Tinasoft"];
    this.i = cls.createInstance(Ci.ITinasoft);
}

TinasoftSingle.prototype = {
}

var Tinasoft = new TinasoftSingle();
