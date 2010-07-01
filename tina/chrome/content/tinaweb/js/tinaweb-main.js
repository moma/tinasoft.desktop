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

 /* useful for fullscreen mode */
function getScreenWidth() {
    var x = 0;
    if (self.innerHeight) {
            x = self.innerWidth;
    }
    else if (document.documentElement && document.documentElement.clientHeight) {
            x = document.documentElement.clientWidth;
    }
    else if (document.body) {
            x = document.body.clientWidth;
    }
    return x;
}

/* useful for fullscreen mode */
function getScreenHeight() {
    var y = 0;
    if (self.innerHeight) {
        y = self.innerHeight;
    }
    else if (document.documentElement && document.documentElement.clientHeight) {
        y = document.documentElement.clientHeight;
    }
    else if (document.body) {
        y = document.body.clientHeight;
    }

    return y;
}

var tinaviz = {};

$(document).ready(function(){
    
    $("#title").html("FET Open projects explorer");

    tinaviz = new Tinaviz({
        tag: $("#vizdiv"),
        path: "js/tinaviz/"
    });

   $('#appletInfo').effect('pulsate', {}, 'fast');

    $(window).bind('resize', function() {
        if (!tinaviz.isEnabled()) return;

        /***************** SET SIZES *****************/
        var infoDivWidth = 390;

        var w = getScreenWidth() - infoDivWidth - 30;
        var h = getScreenHeight() - $("#hd").height() - $("#ft").height() - 60;

        $("#infodiv").css('height', ""+(h - 12)+"px");
        $("#infodiv").css('width', ""+(infoDivWidth)+"px");

        $(".accord_entry").css('height', ""+(h - 70)+"px");
        /*********************************************/

        tinaviz.size(w, h);
    });
    
    tinaviz.ready(function(){

        var infodiv =  InfoDiv('infodiv');
        tinaviz.infodiv = infodiv;

        /***************** SET SIZES *****************/
        var infoDivWidth = 390;

        var w = getScreenWidth() - infoDivWidth - 30;
        var h = getScreenHeight() - $("#hd").height() - $("#ft").height() - 60;

        $("#infodiv").css('height', ""+(h - 12)+"px");
        $("#infodiv").css('width', ""+(infoDivWidth)+"px");

        $(".accord_entry").css('height', ""+(h - 70)+"px");
        /*********************************************/


        $("#infodiv").accordion({
            fillSpace: true,
           // autoHeight: false,
            //clearStyle: true, // keep it to true for tinaweb
            animated: 'easyslide',
        });

        tinaviz.infodiv.reset();
        
        var defaultView = "macro";
        
        tinaviz.setView(defaultView);

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

        //tinaviz.readGraphJava("macro", "bipartite_graph_bipartite_map_bionet_2004_2007_g.gexf_.gexf");
        $("#appletInfo").html("Loading graph..");

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
                
        tinaviz.open({
            view: defaultView,
            url: "FET60bipartite_graph_cooccurrences_.gexf"
        });
        
        tinaviz.event({
        
            viewChanged: function(view) {

                tinaviz.autoCentering();

                $("#sliderEdgeWeight").slider( "option", "values", [
                    parseInt( view.get("edgeWeight/min") ),
                    parseInt(view.get("edgeWeight/max")) *100 
                ]);
                $("#sliderNodeWeight").slider( "option", "values", [
                    parseInt(view.get("nodeWeight/min") ),
                    parseInt(view.get("nodeWeight/max")) *100 
                ]);
                tinaviz.infodiv.display_current_category();
                tinaviz.infodiv.display_current_view();
                
                var disable = false;
                if (view.name == "meso") {
                    // TODO check selection
                    // if selection has edges with edge of all the same weight, we disable the filter
                    var weight = null;
                    for (node in view.nodes) {
                        for (out in node.outputs) {
                            if (weight == null) {
                                weight = out.weight;
                            }
                            else {
                                if (weight != out.weight) {
                                    disable = false;
                                    return;
                                }
                            }
                        }
                    }
                    disable = true;
                } 
                $("#sliderEdgeWeight").slider( "option", "disabled", disable );
            }
        });
        

    });

});
