

var tinaviz = {};
tinaviz.wrapper = {};
tinaviz.applet = {};

tinaviz.init = function () {
    applet = $('#vizframe').contents().find('#tinaviz')[0];
    viz = applet.getSubApplet();
    
    console.log("calling getParentWidth()");
   
    width = getWidth();
    console.log("width="+width);
   
    console.log("calling getParentHeight()");
    height = getWidth();
    console.log("height="+height);
   
    applet.width = width;
    applet.height = height;

    // update the overlay layout (eg. recenter the toolbars)
    $('.htoolbar').css('left', (  (width - parseInt($('#htoolbariframe').css('width'))) / 2   ));

    $('#gui').show();
}

tinaviz.updateGraph = function (url) {
    //var cls = Cc["Python.TinasoftDataRelational"];
    //var ob = cls.createInstance(Ci.nsITinasoftDataRelational);
    //ob.connect("Test-tdr.sqlite");
}
tinaviz.updateGraph = function (url) {
    //var cls = Cc["Python.TinasoftDataRelational"];
    //var ob = cls.createInstance(Ci.nsITinasoftDataRelational);
    //ob.connect("Test-tdr.sqlite");
}

