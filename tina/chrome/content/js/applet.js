//      This program is free software; you can redistribute it and/or modify
//      it under the terms of the GNU General Public License as published by
//      the Free Software Foundation; either version 2 of the License, or
//      (at your option) any later version.
//
//      This program is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      GNU General Public License for more details.
//
//      You should have received a copy of the GNU General Public License
//      along with this program; if not, write to the Free Software
//      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//      MA 02110-1301, USA.


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

