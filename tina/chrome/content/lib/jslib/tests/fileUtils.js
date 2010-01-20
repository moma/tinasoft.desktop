load('chrome/jslib/jslib.js');
include (jslib_fileutils);

// turn debugging on ...
// JS_LIB_DEBUG = JS_LIB_ON;

const CHROME_JAR_URL   = "chrome://navigator/content/";
const CHROME_URL       = "chrome://jslib/content/";
const LOCAL_WIN_PATH   = "C:\\Program Files\\";
const LEAF             = "foo.dat";
const CHROME_BOGUS_URL = "chrome://bogus/content/";

var fu = new FileUtils;

function printMsg(aMsg) { print("TESTING: "+aMsg); }
function printIn(aMsg) { print("  IN: "+aMsg); }
function printOut(aMsg) { print("  Out: "+aMsg); }
function printSpace() { print(" "); }

printSpace();
printSpace();

// chromeToPath
printMsg("chromeToPath");
printIn(CHROME_URL);
printOut(fu.chromeToPath(CHROME_URL));
printSpace();

// chromeToPath
printMsg("chromeToPath");
printIn(CHROME_JAR_URL);
printOut(fu.chromeToPath(CHROME_JAR_URL));
printSpace();

// chromeToURL
printMsg("chromeToURL");
printIn(CHROME_URL);
printOut(fu.chromeToURL(CHROME_URL));
printSpace();

// isValidPath
printMsg("isValidPath");
printIn(LOCAL_WIN_PATH);
printOut(fu.isValidPath(LOCAL_WIN_PATH));
printSpace();

var tmpDir, runBin, runArg;

// test for Linux
if (fu.exists("/tmp")) {
  const LOCAL_URL      = "file:///usr/local/";
  const LOCAL_PATH     = "/usr/src/";

  tmpDir = "/tmp/";
  runBin = "/usr/bin/X11/xcalc"
  runArg = ['-stipple'];

  // urlToPath
  printMsg("urlToPath");
  printIn(LOCAL_URL);
  printOut(fu.urlToPath(LOCAL_URL));
  printSpace();

  // pathToURL
  printMsg("pathToURL");
  printIn(LOCAL_PATH);
  printOut(fu.pathToURL(LOCAL_PATH));
  printSpace();

  // exists
  printMsg("exists");
  printIn("/tmp");
  printOut(fu.exists("/tmp"));
  printSpace();
}

// test for windows
if (fu.exists("C:\\WINDOWS")) {
  const LOCAL_WIN_URL      = "file:///C:/windows/system";

  tmpDir = "C:\\WINDOWS\\TEMP\\";
  runBin = "C:\\WINDOWS\\notepad.exe";
  runArg = null;


  // urlToPath
  printMsg("urlToPath");
  printIn(LOCAL_WIN_URL);
  printOut(fu.urlToPath(LOCAL_WIN_URL));
  printSpace();

  // pathToURL
  printMsg("pathToURL");
  printIn(LOCAL_WIN_PATH);
  printOut(fu.pathToURL(LOCAL_WIN_PATH));
  printSpace();

  // exists
  printMsg("exists");
  printIn("C:\\WINDOWS");
  printOut(fu.exists("C:\\Windows"));
  printSpace();
}

var testFile = tmpDir+LEAF;

// append
printMsg("append");
printIn(tmpDir+", "+LEAF);
printOut(fu.append(tmpDir, LEAF));
printSpace();

// leaf
printMsg("leaf");
printIn(testFile);
printOut(fu.leaf(testFile));
printSpace();

// create
printMsg("create");
printIn(testFile);
printOut(fu.create(testFile));
printSpace();

// permissions
printMsg("permissions");
printIn(testFile);
printOut(fu.permissions(testFile));
printSpace();

// dateModified
printMsg("dateModified");
printIn(testFile);
printOut(fu.dateModified(testFile));
printSpace();

// size
printMsg("size");
printIn(testFile);
printOut(fu.size(testFile));
printSpace();

// ext
printMsg("ext");
printIn(testFile);
printOut(fu.ext(testFile));
printSpace();

// parent
printMsg("parent");
printIn(testFile);
printOut(fu.parent(testFile));
printSpace();

// nsIFile
printMsg("nsIFile");
printIn(testFile);
printOut(fu.nsIFile(testFile));
printSpace();

var copyFile = tmpDir+"foo.dat.bak";
// copy
printMsg("copy");
printIn(testFile+", "+copyFile);
printOut(fu.copy(testFile, copyFile));
printSpace();

// remove
printMsg("remove");
printIn(copyFile);
printOut(fu.remove(copyFile));
printSpace();

// remove
printMsg("remove");
printIn(testFile);
printOut(fu.remove(testFile));
printSpace();

// run
printMsg("run");
printIn(runBin+", "+runArg);
printOut(fu.run(runBin, runArg));
printSpace();

// chromeToPath
printMsg("chromeToPath");
printIn(CHROME_BOGUS_URL);
printOut(fu.chromeToPath(CHROME_BOGUS_URL));
printSpace();

// chromeToURL
printMsg("chromeToURL");
printIn(CHROME_BOGUS_URL);
printOut(fu.chromeToURL(CHROME_BOGUS_URL));
printSpace();

// help
print (fu.help);

quit();

