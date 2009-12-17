
function newSession() {
   document.location = "chrome://tina/content/session.xul";

}

function pytest(event) {
    var tdr = Cc["Python.TinasoftDataRelational"].
        createInstance(Ci.nsITinasoftDataRelational);
    tdr.testXPCOM();
}
