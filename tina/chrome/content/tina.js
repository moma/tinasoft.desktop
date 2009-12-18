// tina.js controller
// main functions
const Cc = Components.classes;
const Ci = Components.interfaces;

function newSession() {
   document.location = "chrome://tina/content/session.xul";
}

function pytest() {
    var tdr = Cc["Python.TinasoftDataRelational"].createInstance(Ci.nsITinasoftDataRelational);
    dump(tdr);
    tdr.connectdb( "testTinasoftDataRelational.db" );
	dump("end of pytest");
}



