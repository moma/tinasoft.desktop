load('chrome/jslib/jslib.js');

// turn debugging on ...
JS_LIB_DEBUG = JS_LIB_ON;

include (jslib_chromefile);
include (jslib_file);

const CHROME_URL       = "chrome://navigator/content/";
const JSLIB_URL        = "chrome://jslib/content/";
const BOGUS_CHROME_URL = "chrome://bogus/content/";

// test file
var f = new File("/tmp/chromeFileCopied.xul");

function printMsg(aMsg) { print("TESTING: "+aMsg); }
function printIn(aMsg) { print("  IN: "+aMsg); }
function printOut(aMsg) { print("  Out: "+aMsg); }
function printSpace() { print(" "); }

printSpace();
printSpace();

var cf;

function runTest (aChromeURL)
{
  printMsg("Running CF Test On: " + aChromeURL);
  cf = new ChromeFile(aChromeURL);

  // chromePath
  printMsg("chromePath");
  printIn("no args ...");
  printOut(cf.chromePath);
  printSpace();
  
  // urlPath
  printMsg("urlPath");
  printIn("no args ...");
  printOut(cf.urlPath);
  printSpace();
  
  // localPath
  printMsg("localPath");
  printIn("no args ...");
  printOut(cf.localPath);
  printSpace();
  
  // size
  printMsg("size");
  printIn("no args ...");
  printOut(cf.size);
  printSpace();
  
  // isJarFile
  printMsg("isJarFile");
  printIn("no args ...");
  printOut(cf.isJarFile);
  printSpace();
  
  // exists
  printMsg("exists");
  printIn("no args ...");
  printOut(cf.exists());
  printSpace();
  
  // nsIFile
  printMsg("nsIFile");
  printIn("no args ...");
  printOut(cf.nsIFile);
  printSpace();
  
  // File
  printMsg("File");
  printIn("no args ...");
  printOut(cf.File);
  printSpace();
  
  // nsIZipReader
  printMsg("nsIZipReader");
  printIn("no args ...");
  printOut(cf.nsIZipReader);
  printSpace();
  
  // Zip
  printMsg("Zip");
  printIn("no args ...");
  printOut(cf.Zip);
  printSpace();
  
  // copy
  printMsg("copy");
  printIn(f.path);
  printOut(cf.copy(f.path));
  printSpace();
  
  // open
  printMsg("open");
  printIn("no args ...");
  printOut(cf.open());
  printSpace();
  
  // read
  printMsg("read");
  printIn("no args ...");
  printOut(cf.read());
  printSpace();
  
  // close
  printMsg("close");
  printIn("no args ...");
  printOut(cf.close());
  printSpace();
}
  
runTest(CHROME_URL);
  
// remove test file
f.remove();

printSpace();
printSpace();
  
runTest(JSLIB_URL);  
  
// remove test file
f.remove();

printSpace();
printSpace();
  
runTest(BOGUS_CHROME_URL);

quit();
  
