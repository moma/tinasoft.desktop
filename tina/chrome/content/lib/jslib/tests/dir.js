load('chrome/jslib/jslib.js');

// turn debugging on ...
JS_LIB_DEBUG = JS_LIB_ON;

include (jslib_dir);

function printMsg(aMsg) { print("TESTING: "+aMsg); }
function printIn(aMsg) { print("  IN: "+aMsg); }
function printOut(aMsg) { print("  Out: "+aMsg); }
function printSpace() { print(" "); }

printSpace();
printSpace();

var d = new Dir("/tmp/pete");

// create
printMsg("create");
printIn("no args ...");
printOut(d.create());
printSpace();

// createUnique
printMsg("createUnique");
printIn("no args ...");
printOut(d.createUnique());
printSpace();

// normalize
printMsg("normalize");
printIn("no args ...");
printOut(d.normalize());
printSpace();

// path
printMsg("path");
printIn("no args ...");
printOut(d.path);
printSpace();

// readDir
printMsg("readDir");
printIn("no args ...");
printOut(d.readDir());
printSpace();

// exists
printMsg("exists");
printIn("no args ...");
printOut(d.exists());
printSpace();

// clone
printMsg("clone");
printIn("no args ...");
printOut(d.clone());
printSpace();

// equals
printMsg("equals");
printIn("d.clone()");
printOut(d.equals(d.clone()));
printSpace();

// isDir
printMsg("isDir");
printIn("no args ...");
printOut(d.isDir());
printSpace();

// isFile
printMsg("isFile");
printIn("no args ...");
printOut(d.isFile());
printSpace();

// isSymlink
printMsg("isSymlink");
printIn("no args ...");
printOut(d.isSymlink());
printSpace();

// isExec
printMsg("isExec");
printIn("no args ...");
printOut(d.isExec());
printSpace();

// isReadable
printMsg("isReadable");
printIn("no args ...");
printOut(d.isReadable());
printSpace();

// isWritable
printMsg("isWritable");
printIn("no args ...");
printOut(d.isWritable());
printSpace();

// isSpecial
printMsg("isSpecial");
printIn("no args ...");
printOut(d.isSpecial());
printSpace();

// isHidden
printMsg("isHidden");
printIn("no args ...");
printOut(d.isHidden());
printSpace();

// permissions
printMsg("permissions");
printIn("no args ...");
printOut(d.permissions);
printSpace();

// leaf
printMsg("leaf");
printIn("no args ...");
printOut(d.leaf);
printSpace();

// parent
printMsg("parent");
printIn("no args ...");
printOut(d.parent);
printSpace();

// nsIFile
printMsg("nsIFile");
printIn("no args ...");
printOut(d.nsIFile);
printSpace();

// remove
printMsg("remove");
printIn("no args ...");
printOut(d.remove());
printSpace();

// initPath
printMsg("initPath");
printIn("/tmp/pete");
printOut(d.initPath("/tmp/pete"));
printSpace();

// move
printMsg("move");
printIn("/tmp/pete_moved");
printOut(d.move("/tmp/pete_moved"));
printSpace();

// remove
printMsg("remove");
printIn("no args ...");
printOut(d.remove());
printSpace();

// initWithPath
printMsg("initWithPath");
printIn("file:///tmp/pete_moved/");
printOut(d.initWithPath("file:///tmp/pete_moved/"));
printSpace();

// append
printMsg("append");
printIn("foo");
printOut(d.append("foo"));
printSpace();

quit();

