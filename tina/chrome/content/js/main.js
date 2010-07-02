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


function resizeApplet() {
    var w = getScreenWidth() - 57;
    var h = getScreenHeight() - 142;

    $('.tabfiller').css("height",""+(h+15)+"px");

    $('#whitebox').css("height",""+(h)+"px");
    $('#whitebox').css("width",""+(w)+"px");

    // the iframe
    $('#vizframe').css("height",""+(h)+"px");
    $('#vizframe').css("width",""+(w-300)+"px");

    $('#hide').show();
    $('#infodiv').css("height",""+(h-50)+"px");
    $('#infodiv').css("width",""+(300)+"px");
    tinaviz.size(w - 350,h);

   //$("#infodiv").css( 'height', getScreenHeight() - $("#hd").height() - $("#ft").height() - 60);
}

var tinaviz = {};
// wait for the DOM to be loaded
$(document).ready(function() {

    $('#appletInfo').effect('pulsate', {}, 'fast');

    var dirService = Components.classes["@mozilla.org/file/directory_service;1"].getService(Components.interfaces.nsIProperties);
    var tinavizDir = dirService.get("AChrom", Components.interfaces.nsIFile);
    tinavizDir.append("content");
    tinavizDir.append("tinaweb");
    tinavizDir.append("js");
    tinavizDir.append("tinaviz"); // returns an nsIFile object

    var ios = Components.classes["@mozilla.org/network/io-service;1"].
                    getService(Components.interfaces.nsIIOService);
    var URL = ios.newFileURI(tinavizDir);

    //alert("url:"+URL.spec);

    var w = getScreenWidth() - 390;
    var h = getScreenHeight() - $("#hd").height() - $("#ft").height() - 60;

    tinaviz = new Tinaviz({
        tag: $("#vizdiv"),
        path: URL.spec,
        context: "",
        engine: "software",
        branding: false,
        width: w,
        height: h
    });

    tinaviz.ready(function(){
        console.log("tinaviz.ready !");
        var infodiv =  InfoDiv('infodiv');
        tinaviz.infodiv = infodiv;

        // auto-adjusting infodiv height
        $(infodiv.id).css('height', tinaviz.height - 40);

        $(infodiv.id).accordion({
            fillSpace: true,
        });

        infodiv.reset();


        tinaviz.setView("macro");

        var session = tinaviz.session();
        var macro = tinaviz.view("macro");
        var meso = tinaviz.view("meso");

        session.set("edgeWeight/min", 0.0);
        session.set("edgeWeight/max", 1.0);
        session.set("nodeWeight/min", 0.0);
        session.set("nodeWeight/max", 1.0);
        session.set("category/category", "NGram");
        session.set("output/nodeSizeMin", 5.0);
        session.set("output/nodeSizeMax", 20.0);
        session.set("output/nodeSizeRatio", 50.0/100.0);
        session.set("selection/radius", 1.0);

        macro.filter("Category", "category");
        macro.filter("NodeWeightRange", "nodeWeight");
        macro.filter("EdgeWeightRange", "edgeWeight");
        macro.filter("NodeFunction", "radiusByWeight");
        macro.filter("Output", "output");

        meso.filter("SubGraphCopyStandalone", "category");
        meso.set("category/source", "macro");
        meso.set("category/category", "Document");
        meso.set("category/mode", "keep");

        meso.filter("NodeWeightRangeHack", "nodeWeight");
        meso.filter("EdgeWeightRangeHack", "edgeWeight");
        meso.filter("NodeFunction", "radiusByWeight");
        meso.filter("Output", "output");

        // init the node list with ngrams
        tinaviz.updateNodes( "macro", "NGram" );

        // cache the document list
        tinaviz.getNodes( "macro", "Document" );

        tinaviz.open({

            success: function() {
             // init the node list with ngrams
             tinaviz.updateNodes( defaultView, "NGram" );

             // cache the document list
             tinaviz.getNodes(defaultView, "Document" );

             tinaviz.infodiv.display_current_category();
             tinaviz.infodiv.display_current_view();

             $("#appletInfo").hide();
             tinaviz.size(w, h);
           },
           error: function(msg) {
             $("#appletInfo").html("Error, couldn't load graph: "+msg);
           }
        });

        //infodiv.display_current_category();
        //infodiv.display_current_view();
    });


    if (!tinaviz.isEnabled()) {
        //resizeApplet();
        tinaviz.setEnabled(true);
        tinaviz.setView("macro");
    }

    $(window).bind('resize', function() {
        if (tinaviz.isEnabled()) {
            resizeApplet();
        }
    });
    //initSubmitEvents();
    //initPytextminerUi();
});
