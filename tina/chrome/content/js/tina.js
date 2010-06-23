/* ***** BEGIN LICENSE BLOCK *****
 * GNU GPL 3
 * ***** END LICENSE BLOCK *****
 * tina.js controller
 * here are the main functions */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/about.xul";

// If a window with the type exists just focus it otherwise open a new window
function openWindowForType(type, uri, features) {
  var topWindow = Cc['@mozilla.org/appshell/window-mediator;1'].
                  getService(Ci.nsIWindowMediator).
                  getMostRecentWindow(type);

  if (topWindow)
    topWindow.focus();
  else if (features)
    window.open(uri, "_blank", features);
  else
    window.open(uri, "_blank", "chrome,extrachrome,menubar,resizable,scrollbars,status,toolbar");
    //window.open(uri, "_blank", "chrome,extrachrome,resizable,scrollbars,status,toolbar");
}

function buildHelpMenu()
{
  /*var updates = Cc["@mozilla.org/updates/update-service;1"].
                getService(Ci.nsIApplicationUpdateService);
  var um = Cc["@mozilla.org/updates/update-manager;1"].
           getService(Ci.nsIUpdateManager);
    */
  // Disable the UI if the update enabled pref has been locked by the
  // administrator or if we cannot update for some other reason
  /*var checkForUpdates = document.getElementById("menu-update");
  var canUpdate = updates.canUpdate;
  checkForUpdates.setAttribute("disabled", !canUpdate);
  if (!canUpdate)
    return;

  var strings = document.getElementById("strings");
  var activeUpdate = um.activeUpdate;
    */
  // If there's an active update, substitute its name into the label
  // we show for this item, otherwise display a generic label.
  /*function getStringWithUpdateName(key) {
    if (activeUpdate && activeUpdate.name)
      return strings.getFormattedString(key, [activeUpdate.name]);
    return strings.getString(key + "Fallback");
  }

  // By default, show "Check for Updates..."
  var key = "default";
  if (activeUpdate) {
    switch (activeUpdate.state) {
    case "downloading":
      // If we're downloading an update at present, show the text:
      // "Downloading TinaSoft x.x..." otherwise we're paused, and show
      // "Resume Downloading TinaSoft x.x..."
      key = updates.isDownloading ? "downloading" : "resume";
      break;
    case "pending":
      // If we're waiting for the user to restart, show: "Apply Downloaded
      // Updates Now..."
      key = "pending";
      break;
    }
  }
  checkForUpdates.label = getStringWithUpdateName("updatesItem_" + key);
  if (um.activeUpdate && updates.isDownloading)
    checkForUpdates.setAttribute("loading", "true");
  else
    checkForUpdates.removeAttribute("loading");
    */
}


function openAddons() {
  openWindowForType("Extension:Manager",
                    "chrome://mozapps/content/extensions/extensions.xul");
}
function openAboutPlugins() {
   openWindowForType("about:plugins","about:plugins");
}
// DEBUGGING
//openAboutPlugins();

function openErrorConsole() {
  openWindowForType("global:console", "chrome://global/content/console.xul");
}

// DEBUGGING
//openErrorConsole();

function openConfig() {
  openWindowForType("Preferences:ConfigManager", "chrome://global/content/config.xul");

}
/*function openDOMInspector() {
    openDialog("chrome://inspector/content/", "_blank",
                    "chrome,all,dialog=no");
}*/

/**
 * Opens the update manager and checks for updates to the application.
 */
function openUpdates()
{
  var um = Cc["@mozilla.org/updates/update-manager;1"].
           getService(Ci.nsIUpdateManager);
  var prompter = Cc["@mozilla.org/updates/update-prompt;1"].
                 createInstance(Ci.nsIUpdatePrompt);

  // If there's an update ready to be applied, show the "Update Downloaded"
  // UI instead and let the user know they have to restart the browser for
  // the changes to be applied.
  if (um.activeUpdate && um.activeUpdate.state == "pending")
    prompter.showUpdateDownloaded(um.activeUpdate);
  else
    prompter.checkForUpdates();
}

function openAbout() {
    openWindowForType("TinaSoft:About", "chrome://tina/content/about.xul","chrome,dialog,centerscreen");
}

function openHelp() {
    openURL(HELP_URL);
}
var fullScreen = false;

function switchFullScreen() {
   if (!fullScreen) {
    fullScreen = true;
    setTimeout('window.fullScreen = true;',1);
   } else {
    fullScreen = false;
    setTimeout('window.fullScreen = false;',1);
   }
}

