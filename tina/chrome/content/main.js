
/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/intro.xul";

// data.html javascript functions
//
function goToSessions() {
   document.location = "chrome://tina/content/session.xul";

}
function goToDatas() {
   document.location = "chrome://tina/content/data.html";

}
