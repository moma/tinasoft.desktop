jslib.init(this);

function onloadAbout ()
{
  include(jslib_fileutils);
  var fu = new FileUtils();

  var p1 = fu.chromeToPath("chrome://jslib/content/");
  var p2 = fu.chromeToPath("chrome://jsliblive/content/");

  getEl("jslibPath").value = p1;
  getEl("jsliblivePath").value = p2;
}

// Firefox only
function toOpenWindowByType(inType, uri, features, urlToLoad)
{
  var wm = jslibGetService("@mozilla.org/appshell/window-mediator;1",
                           "nsIWindowMediator");
  var topWindow = wm.getMostRecentWindow(inType);
  
  if (topWindow)  {
    topWindow.focus();
    var theBrowser = topWindow.document.getElementById("content");
    var tabAdded = theBrowser.addTab(urlToLoad);
    theBrowser.selectedTab = tabAdded;
  }
  else  {
    window.openDialog(uri, "", features, urlToLoad);
  }
}

function goLink(evt)
{
  var href = evt.target.getAttribute("href");
  if (href){
    toOpenWindowByType("navigator:browser", "chrome://browser/content/browser.xul", "all,dialog=no", href)
    self.close();
  }
}

function getEl (aEl) { return document.getElementById(aEl); }

