load('chrome/jslib/jslib.js');

// turn debugging on ...
JS_LIB_DEBUG = JS_LIB_ON;

include (jslib_file);

function printMsg(aMsg) { print("TESTING: "+aMsg); }
function printIn(aMsg) { print("  IN: "+aMsg); }
function printOut(aMsg) { print("  Out: "+aMsg); }
function printSpace() { print(" "); }

printSpace();
printSpace();

var f = new File("/tmp/./foo.dat");

// create
printMsg("create");
printIn("no args ...");
printOut(f.create());
printSpace();

// createUnique
printMsg("createUnique");
printIn("no args ...");
printOut(f.createUnique());
printSpace();

// normalize
printMsg("normalize");
printIn("no args ...");
printOut(f.normalize());
printSpace();

// path
printMsg("path");
printIn("no args ...");
printOut(f.path);
printSpace();

// open
printMsg("open");
printIn("w, 0644");
printOut(f.open("w", 0644));
printSpace();

// write
printMsg("write");
printIn("this is line one ...\\n");
printOut(f.write("this is line one ...\n"));
printSpace();

// close
printMsg("close");
printIn("no args ...");
printOut(f.close());
printSpace();

// open
printMsg("open");
printIn("a");
printOut(f.open("a"));
printSpace();

// write
printMsg("write");
printIn("this is line two ...");
printOut(f.write("       this is line two ..."));
printSpace();

// close
printMsg("close");
printIn("no args ...");
printOut(f.close());
printSpace();

// open
printMsg("open");
printIn("no args ...");
printOut(f.open());
printSpace();

// read
printMsg("read");
printIn("no args ...");
printOut(f.read());
printSpace();

// close
printMsg("close");
printIn("no args ...");
printOut(f.close());
printSpace();

// open
printMsg("open");
printIn("no args ...");
printOut(f.open());
printSpace();

// readAllLines
printMsg("readAllLines");
printIn("no args ...");
printOut(f.read());
printSpace();

// close
printMsg("close");
printIn("no args ...");
printOut(f.close());
printSpace();

// exists
printMsg("exists");
printIn("no args ...");
printOut(f.exists());
printSpace();

// clone
printMsg("clone");
printIn("no args ...");
printOut(f.clone());
printSpace();

// equals
printMsg("equals");
printIn("f.clone()");
printOut(f.equals(f.clone()));
printSpace();

// isDir
printMsg("isDir");
printIn("no args ...");
printOut(f.isDir());
printSpace();

// isFile
printMsg("isFile");
printIn("no args ...");
printOut(f.isFile());
printSpace();

// isSymlink
printMsg("isSymlink");
printIn("no args ...");
printOut(f.isSymlink());
printSpace();

// isExec
printMsg("isExec");
printIn("no args ...");
printOut(f.isExec());
printSpace();

// isReadable
printMsg("isReadable");
printIn("no args ...");
printOut(f.isReadable());
printSpace();

// isWritable
printMsg("isWritable");
printIn("no args ...");
printOut(f.isWritable());
printSpace();

// isSpecial
printMsg("isSpecial");
printIn("no args ...");
printOut(f.isSpecial());
printSpace();

// isHidden
printMsg("isHidden");
printIn("no args ...");
printOut(f.isHidden());
printSpace();

// permissions
printMsg("permissions");
printIn("no args ...");
printOut(f.permissions);
printSpace();

// size
printMsg("size");
printIn("no args ...");
printOut(f.size);
printSpace();

// leaf
printMsg("leaf");
printIn("no args ...");
printOut(f.leaf);
printSpace();

// ext
printMsg("ext");
printIn("no args ...");
printOut(f.ext);
printSpace();

// parent
printMsg("parent");
printIn("no args ...");
printOut(f.parent);
printSpace();

// nsIFile
printMsg("nsIFile");
printIn("no args ...");
printOut(f.nsIFile);
printSpace();

// copy
printMsg("copy");
printIn("/tmp/foo.dat.bak");
printOut(f.copy("/tmp/foo.dat.bak"));
printSpace();

// remove
printMsg("remove");
printIn("no args ...");
printOut(f.remove());
printSpace();

// initPath
printMsg("initPath");
printIn("/tmp/foo.dat.bak");
printOut(f.initPath("/tmp/foo.dat.bak"));
printSpace();

// move
printMsg("move");
printIn("/tmp/foo.dat");
printOut(f.move("/tmp/foo.dat"));
printSpace();

// remove
printMsg("remove");
printIn("no args ...");
printOut(f.remove());
printSpace();

// initWithPath
printMsg("initWithPath");
printIn("file:///tmp");
printOut(f.initWithPath("file:///tmp"));
printSpace();

// append
printMsg("append");
printIn("foo");
printOut(f.append("foo"));
printSpace();

// appendRelativePath
printMsg("appendRelativePath");
printIn("bar/baz/bam");
printOut(f.appendRelativePath("bar/baz/bam"));
printSpace();

quit();

