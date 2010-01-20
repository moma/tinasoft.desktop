load('chrome/jslib/jslib.js');

// turn debugging on ...
JS_LIB_DEBUG = JS_LIB_ON;

include (jslib_zip);
include (jslib_dirutils);

var du = new DirUtils;
du.useObj = true;

var f = du.getChromeDir();
f.append("toolkit.jar");

var zip = new Zip(f);

function printMsg(aMsg) { print("TESTING: "+aMsg); }
function printIn(aMsg) { print("  IN: "+aMsg); }
function printOut(aMsg) { print("  Out: "+aMsg); }
function printSpace() { print(" "); }

printSpace();
printSpace();

// File
printMsg("File");
printIn("no args ...");
printOut(zip.File);
printSpace();

// nsIFile
printMsg("nsIFile");
printIn("no args ...");
printOut(zip.nsIFile);
printSpace();

// file
printMsg("file");
printIn("no args ...");
printOut(zip.file);
printSpace();


// getAllEntries
printMsg("getAllEntries");
printIn("no args ...");
printOut(zip.getAllEntries());
printSpace();

var entry = zip.getAllEntries()[1];
jslibPrintMsg("entry", entry);

// getEntryName
printMsg("getEntryName");
printIn(entry);
printOut(zip.getEntryName(entry));
printSpace();

// getEntrySize
printMsg("getEntrySize");
printIn(entry);
printOut(zip.getEntrySize(entry));
printSpace();

// getEntryRealSize
printMsg("getEntryRealSize");
printIn(entry);
printOut(zip.getEntryRealSize(entry));
printSpace();

/******* need to check as there seems to be failure from the component
// getEntryCompression
printMsg("getEntryCompression");
printIn(entry);
printOut(zip.getEntryCompression(entry));
printSpace();
************************/

// getEntryCRC32
printMsg("getEntryCRC32");
printIn(entry);
printOut(zip.getEntryCRC32(entry));
printSpace();

// readEntry
printMsg("readEntry");
printIn(entry);
printOut(zip.readEntry(entry));
printSpace();

// open
printMsg("open");
printIn("no args ...");
printOut(zip.open());
printSpace();

// extract
printMsg("extract");
printIn("/tmp/zip_extracted");
printOut(zip.extract("/tmp/zip_extracted"));
printSpace();

// close
printMsg("close");
printIn("no args ...");
printOut(zip.close());
printSpace();


// zip write test

var fs = "/tmp/zip_test.log";
f = new File(fs);

f.remove();
f.open("w");
f.write("THIS IS A TEST OF ZIP WRITE LIB ...");
f.close();

zip = new Zip("/tmp/jslib_test_zipWrite.zip");

// compress file
printMsg("compressFile");
printIn(fs);
printOut(zip.compressFile(fs, 9));
printSpace();

quit();

