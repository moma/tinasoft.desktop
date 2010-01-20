jslib.init(this);
include (jslib_file);

jslibTurnDumpOn();
jslibTurnStrictOn();

const tf = "/tmp";
// const tf = "file:///tmp";
// const tf = "http:///tmp";

var gFile;

function outBad (aMsg)  { dump("error test -> ("+aMsg+") result -> "); }
function outGood (aMsg) { dump("normal test -> ("+aMsg+") result -> "); }

function test ()
{
  testBadInit();
  testInit();
}

function testBadInit ()
{
  jslibPrintSep("BAD INITIALIZATION TEST");
  outBad("initialization");
  gFile = new File;
  outBad("append");
  testAppend();
  outBad("appendRelativePath");
  testAppendRelative();
  outBad("remove");
  testRemove();
  outBad("create");
  testCreate();
  outBad("exists");
  testExists();
  outBad("path");
  testPath();
  outBad("leaf");
  testLeaf();
  outBad("set leaf");
  testSetLeaf();
  outBad("permissions");
  testPermissions();
  outBad("set permissions");
  testSetPermissions();
  outBad("dateModified");
  testDateModified();
  outBad("nsIFile");
  testNSIFile();
  outBad("parent");
  testParent();
  outBad("URL");
  testURL();
  outBad("isDir");
  testIsDir();
  outBad("isFile");
  testIsFile();
  outBad("isExec");
  testIsExec();
  outBad("isSymlink");
  testIsSymlink();
  outBad("open");
  testOpen();
  outBad("write");
  testWrite();
  outBad("read");
  testRead();
  outBad("readAllLines");
  testReadAllLines();
  outBad("size");
  testSize();
  outBad("ext");
  testExt();
  outBad("move");
  testMove("/tmp/io-test-moved.txt");
  outBad("move bad obj");
  testMove({});

  outBad("initPath");
  rv = testInitBlankObj();
	jslibPrint(rv);
  outBad("initPath");
  rv = testInitBogusObj();
	jslibPrint(rv);
  
}

function testInit ()
{
  jslibPrintSep("GOOD INITIALIZATION TEST");
  var rv;
  outGood("initialization");
  gFile = new File(tf);
  rv = gFile.path;
  jslibPrint(rv);
  outGood("append");
  rv = testAppend();
  jslibPrint(rv);
  outGood("appendRelativePath");
  rv = testAppendRelative();
  jslibPrint(rv);
  outGood("exists");
  rv = testExists();
  jslibPrint(rv);
  outGood("create");
  rv = testCreate();
  jslibPrint(rv);
  outGood("remove");
  jslibPrint(rv);
  outGood("path");
  rv = testPath();
  jslibPrint(rv);
  outGood("leaf");
  rv = testLeaf();
  jslibPrint(rv);
  outGood("set leaf");
  rv = testSetLeaf();
  jslibPrint(rv);
  testResetLeaf();
  outGood("permissions");
  rv = testPermissions();
  jslibPrint(rv);
  outGood("set permissions");
  rv = testSetPermissions();
  jslibPrint(rv);
  outGood("new permissions");
  rv = testPermissions();
  jslibPrint(rv);
  outGood("dateModified");
  rv = testDateModified();
  jslibPrint(rv);
  outGood("nsIFile");
  rv = testNSIFile();
  jslibPrint(rv);
  outGood("parent");
  rv = testParent();
  jslibPrint(rv);
  outGood("URL");
  rv = testURL();
  jslibPrint(rv);
  outGood("isDir");
  rv = testIsDir();
  jslibPrint(rv);
  outGood("isFile");
  rv = testIsFile();
  jslibPrint(rv);
  outGood("isExec");
  rv = testIsExec();
  jslibPrint(rv);
  outGood("isSymlink");
  rv = testIsSymlink();
  jslibPrint(rv);
  outGood("remove");
  rv = testRemove();
  jslibPrint(rv);
  outGood("open");
  rv = testOpen("w");
  jslibPrint(rv);
  outGood("write");
  rv = testWrite();
  jslibPrint(rv);
  outGood("open");
  rv = testOpen("a");
  jslibPrint(rv);
  outGood("write");
  rv = testWriteAppend();
  jslibPrint(rv);
  outGood("open");
  rv = testOpen();
  jslibPrint(rv);
  outGood("read");
  rv = testRead();
  jslibPrint(rv);
  outGood("readAllLines");
  rv = testReadAllLines();
  jslibPrint(rv);
  outGood("size");
  rv = testSize();
  jslibPrint(rv);
  outGood("ext");
  rv = testExt();
  jslibPrint(rv);
  outGood("move bad object");
  rv = testMove({});
  jslibPrint(rv);
  outGood("move");
  rv = testMove("/tmp/io-test-moved.txt");
  jslibPrint(rv);
  outGood("move good obj");
  rv = testMove(new File("/tmp/io-test-moved-obj.txt"));
  jslibPrint(rv);
  outGood("move");
  rv = testMove("/tmp/");
  jslibPrint(rv);
  outGood("move");
  rv = testMove("/tmp/appended/");
  jslibPrint(rv);



  outGood("initPath");
  rv = testInitPath();
  jslibPrint(rv);
  outGood("initWithPath");
  rv = testInitWithPath();
  jslibPrint(rv);
  outGood("initPath(using File Object)");
  rv = testInitPathFileObj();
  jslibPrint(rv);
  outGood("initPath(using nsIFile Object)");
  rv = testInitPathNSIFileObj();
  jslibPrint(rv);


}

function testRemove ()
{
  return gFile.remove();
}

function testCreate ()
{
  return gFile.create();
}

function testExists ()
{
  return gFile.exists();
}

function testPath ()
{
  return gFile.path;
}

function testLeaf ()
{
  return gFile.leaf;
}

function testSetLeaf ()
{
  return (gFile.leaf = "jslibTestFileNewLeaf.txt");
}

function testResetLeaf ()
{
  gFile.leaf = "jslibTestFile.txt";
}

function testPermissions ()
{
  return gFile.permissions;
}

function testSetPermissions ()
{
  return (gFile.permissions = 0400);
}

function testDateModified ()
{
  return gFile.dateModified;
}

function testNSIFile ()
{
  return gFile.nsIFile
}

function testParent ()
{
  return gFile.parent;
}

function testAppend ()
{
  return gFile.append("appended");
}

function testAppendRelative ()
{
  return gFile.appendRelativePath("jslibTestFile.txt");
}

function testURL ()
{
  return gFile.URL;
}

function testIsDir ()
{
  return gFile.isDir();
}

function testIsFile ()
{
  return gFile.isFile();
}

function testIsExec ()
{
  return gFile.isExec();
}

function testIsSymlink ()
{
  return gFile.isSymlink();
}

function testInitPath ()
{
  return gFile.initPath("/tmp/initPath.dat");
}

function testInitWithPath ()
{
  return gFile.initWithPath("/tmp/peteWithPath.dat");
}

function testInitPathFileObj ()
{
	var f = new File("/tmp/fileObj.dat");
  return gFile.initPath(f);
}

function testInitPathNSIFileObj ()
{
	var f = jslibCreateInstance("@mozilla.org/file/local;1", "nsILocalFile");
	f.initWithPath("/tmp/appended/");
  return gFile.initPath(f);
}

function testInitBogusObj ()
{
	var f = new Object;
	f.path = "/tmp/bogus.dat";
  return gFile.initPath(f);
}

function testInitBlankObj ()
{
	var f = new Object;
  return gFile.initPath(f);
}

function testOpen (aMode)
{
	var rv = gFile.open(aMode);
  return rv; 
}

function testWrite()
{
	var rv = gFile.write("This is line 1\n");
  return rv; 
}

function testWriteAppend()
{
	var rv = gFile.write("This is line 2\n");
	var rv = gFile.write("This is line 3\n");
  return rv; 
}

function testRead()
{
	var rv = gFile.read();
  return rv; 
}

function testReadAllLines()
{
	var rv = gFile.readAllLines();
  return rv; 
}

function testSize()
{
	var rv = gFile.size;
  return rv; 
}

function testExt()
{
	var rv = gFile.ext;
  return rv; 
}

function testMove(aDest)
{
	var rv = gFile.move(aDest);
  return rv; 
}

