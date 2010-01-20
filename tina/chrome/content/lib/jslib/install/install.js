//Prefs
var htmlresults = false;
var installName = "Beonex Communicator 0.6";
var installPage = "http://www.beonex.com/communicator/version/0.6/install";

//Other global vars

// for exclusive
const anymozilla = 0; // any Mozilla
const sameversion = 1; // this Mozilla release only,
                          e.g. Mozilla 0.6/Netscape 6.0/Beonex Comm. 0.6
const samevendorrelease = 2; // this vendor release only, e.g. Beonex Comm. 0.6

var newline;
if (htmlresults)
  newline = "<br>";
else
  newline = "\n";

var numstatus = 0;
var numxpi = 0;
var gxpi;


function statusCallback(url, status) {
    for (i in gxpi) {
        if ( url.indexOf(gxpi[i]) != -1 ) {
            gxpi[i] = status;
            numstatus++;
            break;
        }
    }

    // if we've gotten all results then display them
    if (numstatus == numxpi)
    {
        var restart = false;

        var textResults = "";
        var textInstructions = "";

        for (i in gxpi)
        {
            textResults += i + ": ";
            switch (status) {
              case 999:
                 restart = true;     // fall-through
              case 0:
                 textResults += "Successful";
                 restart = true; //XXX
                 break;
              case -210:
                 textResults += "Cancelled";
                 break;
              default:
                 textResults += "Error encountered -- "+status;
                 break;
            }
            textResults += newline;
        }
        if (restart)
        {
            textInstructions += "Please restart Communicator.";
            if (isWindows())
              textInstructions += " If you are using Win9x (incl. Windows ME), restart your computer.";
        }

        if (htmlresults)
        {
          dlg = window.open("","resultWindow");
          //open(,, "width=400,height=300,scrollbars=yes,resizable=yes"); XXX
          dlg.document.write("<he" + "ad>" +
                             "<tit" + "le>" + "XPInstall Results" +
                             "</tit" + "le>" + "</he" + "ad>");
          dlg.document.write("<bo" + "dy>" + "<h1>Installation Results</h1>");
          dlg.document.write(textResults);
          if (textInstructions != "")
          {
              dlg.document.write("<h1>Instructions</h1>");
              dlg.document.write(textInstructions);
          }
          dlg.document.write("<p><center><f" + "orm name=\"okclose\">" +
                             "<i" + "nput type=button " +
                             "on" + "Click=\"window.close();\" " +
                             "on" + "Command=\"window.close();\" " +
                             "value=\"OK\">" +
                             "</center></f" + "orm>");
          dlg.document.write("</bo" + "dy>");
          dlg.document.close();
        }
        else
        {
          alert(textResults + newline + textInstructions);
        }
    }
}


function isWindows()
{
  return (navigator.platform.indexOf("Win") == 0);
}
function isLinux()
{
  return (navigator.platform.indexOf("Linux") == 0);
}
function isMac()
{
  return (navigator.platform.indexOf("Mac") != -1);
}
function badPlatform()
{
  alert("Platform not recognized");
}


function gotoInstallPage()
{
  document.location.href = installPage;
}

// exclusive: anymozilla, sameversion or samevendorrelease
function browserOK(exclusive)
{
  if (exclusive == anymozilla)
  {
    if (typeof InstallTrigger == "object")
      return true;
    else
    {
      if(window.confirm("This package is intended only for Beonex Communicator, Netscape 6 or Mozilla.\nWould you like to download " + installName + "?"))
        gotoInstallPage();
      return false;
    }
  }
  else if (exclusive == sameversion)
  {
    var start = navigator.userAgent.indexOf("Gecko/") + 6;
    var build = parseInt(navigator.userAgent.substring(start, start + 8));
    if (typeof InstallTrigger == "object"
        && build >= 20001100)
      return true;
    else
    {
      if(window.confirm("This package is intended only for Beonex Communicator 0.6, Netscape 6.0 or Mozilla 0.6.\nWould you like to download " + installName + "?"))
        gotoInstallPage();
      return false;
    }
  }
  else if (exclusive == samevendorrelease)
  {
    if (typeof InstallTrigger == "object" && navigator.vendor && navigator.vendor == "Beonex" && navigator.vendorSub && navigator.vendorSub.substr(0,3) == "0.6")
      return true;
    else
    {
      if(window.confirm("This package is intended for Beonex Communicator 0.6 only.\nWould you like to download " + installName + "?"))
        gotoInstallPage();
      return false;
    }
  }
  else
  {
    alert("Fatal error: Bug on Website");
    return false;
  }
}


// xpi = Array for InstallTrigger
// exclusive = see above
function startInstall(xpi, exclusive)
{
  if (!browserOK(exclusive))
    return;

  gxpi = xpi;
  for (i in xpi) {
    numxpi++;
  }

  InstallTrigger.install(xpi,statusCallback);
}


// genLaunch - Generic Launcher: picks up package URLs and names from document.

// idClass = Class of package to be installed.
//   Use empty string to install all packages.
// exclusive = see above
//
// Example:
// <a class="dlLinux,java" name="Java RE" href="http://foo/bar-ix.xpi">bla</a>
// <a class="dlLinux,psm" name="PSM" href="http://foo/baz-ix.xpi">bla bla</a>
// <a class="dlWin32,java" name="Java RE" href="http://foo/bar-win.xpi">bla</a>
// <a class="dlWin32,psm" name="PSM" href="http://foo/baz-win.xpi">bla bla</a>
// <div onclick="genLaunch('java',1)>button 1</div>
// <div onclick="genLaunch('',1)>button 2</div>
// If you click on button 1, the Java package for your platform
// will be installed.
// If you click on button 2, both the Java and PSM package for your platform
// will be installed.

function genLaunch(idClass, exclusive)
{
  if (!browserOK(exclusive))  // make sure, older browsers don't fail below
    return;

  var platformClass;
  if (isWindows())
    platformClass = "dlWin32";
  else if (isLinux())
    platformClass = "dlLinux";
  else if (isMac())
    platformClass = "dlMac";
  else
  {
    badPlatform();
    return;
  }
  // "dlAll" is OK, too.

  var xpi = new Array();
  var found = false;

  var elems = document.getElementsByTagName("A");
  for (var i = 0; i < elems.length; i++)
  {
    var elem = elems.item(i);
    var attrs = elem.attributes;
    if (attrs)
    {
      var classes = attrs.getNamedItem("class");
      if (classes && (
          classes.value.indexOf(platformClass) != -1 ||
          classes.value.indexOf("dlAll") != -1
          ))
      {
        if (classes && classes.value.indexOf(idClass) != -1)
        {
          uri = attrs.getNamedItem("href").value;
          name = attrs.getNamedItem("name").value;
          //alert("name " + name + "\nuri " + uri);
          xpi[name] = uri;
          found = true;
        }
      }
    }
  }

  if (!found)
    alert("No appropriate packages available");
  else
    startInstall(xpi,exclusive);
}

