// tina.xul functions
//
function newSession() {
   document.location = "chrome://tina/content/session.xul";

}

function pytest() {
	alert("start of pytest");
    var tdr = Cc["Python.TinasoftDataRelational"].createInstance(Ci.nsITinasoftDataRelational);
    tdr.connection( "testTinasoftDataRelational.db" );
	alert("end of pytest");
}
