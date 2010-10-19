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
// wait for the DOM to be loaded
$(document).ready(function() {

    $("#title").html("<h3>Tinasoft Desktop</h3>");

    tinaviz = new Tinaviz({
        tag: $("#vizdiv"),
        path: "tinaweb/",
        branding: false,
        width: 1,
        height: 1
    });

   //$('#appletInfo').effect('pulsate', {}, 'fast');

    $(window).bind('resize', function() {
        var size = resize();
        tinaviz.size(size.w, size.h);
    });

    tinaviz.ready(function(){


    var prefs = {
            gexf: "default.gexf",
            view: "macro",
            category: "Document",
            node_id: "",
            search: "",
            magnify: "0.5",
            cursor_size: "1.0",
            edge_filter_min: "0.0",
            edge_filter_max: "1.0",
            node_filter_min: "0.0",
            node_filter_max: "1.0",
            layout: "tinaforce",
            edge_rendering: "curve"

        };
        var urlVars = getUrlVars();
        for (x in urlVars) {
            prefs[x] = urlVars[x];
        }

        tinaviz.setView(prefs.view);
        var macro = tinaviz.views.macro;
        var meso = tinaviz.views.meso;

        //session.add("nodes/0/keywords", "newKeyword");

        tinaviz.set("edgeWeight/min", parseFloat(prefs.edge_filter_min));
        tinaviz.set("edgeWeight/max", parseFloat(prefs.edge_filter_max));
        tinaviz.set("nodeWeight/min", parseFloat(prefs.node_filter_min));
        tinaviz.set("nodeWeight/max", parseFloat(prefs.node_filter_max));
        tinaviz.set("category/category", prefs.category);
        tinaviz.set("output/nodeSizeMin", 5.0);
        tinaviz.set("output/nodeSizeMax", 20.0);
        tinaviz.set("output/nodeSizeRatio", parseFloat(prefs.magnify));
        tinaviz.set("selection/radius", parseFloat(prefs.cursor_size));
        tinaviz.set("layout/algorithm", prefs.layout)
        tinaviz.set("rendering/edge/shape", prefs.edge_rendering);
        tinaviz.set("data/source", "gexf");

        macro.filter("Category", "category");
        macro.filter("NodeWeightRange", "nodeWeight");
        macro.filter("EdgeWeightRange", "edgeWeight");
        macro.filter("Output", "output");

        meso.filter("SubGraphCopyStandalone", "category");
        meso.set("category/source", "macro");
        meso.set("category/category", "Document");
        meso.set("category/mode", "keep");

        meso.filter("NodeWeightRangeHack", "nodeWeight");
        meso.filter("EdgeWeightRangeHack", "edgeWeight");
        meso.filter("Output", "output");

        tinaviz.infodiv = InfoDiv('infodiv');
        
        tinaviz.infodiv.reset();

        $("#infodiv").accordion({
            //fillSpace: true
        });

        toolbar.init();

/*
        // init the node list with ngrams
        tinaviz.updateNodes( "macro", "NGram" );

        // cache the document list
        tinaviz.getNodes( "macro", "Document" );*/

        tinaviz.open({
            before: function() {
                $('#appletInfo').show();
                $('#appletInfo').html("please wait while loading the graph..");
                //$('#appletInfo').effect('pulsate', { 'times':5 }, 1000);
                tinaviz.infodiv.reset();
            },
            success: function() {

                // init the node list with ngrams
                tinaviz.updateNodes( "macro", "NGram" );
                // cache the document list
                tinaviz.getNodes("macro", "Document" );
                tinaviz.infodiv.display_current_category();
                tinaviz.infodiv.display_current_view();
                $('#appletInfo').html("Graph loaded");
                $('#appletInfo').effect('pulsate', { 'times':1 }, 1000);
                $.doTimeout(1000, function() {
                    $("#appletInfo").hide();
                });

            },
            error: function(msg) {
                $("#appletInfo").html("error loading graph: "+msg);
            },
            cache: false
        });

        tinaviz.event({

            selectionChanged: function(selection) {
                tinaviz.infodiv.reset();

                if ( selection.mouseMode == "left" ) {
                // nothing to do
                } else if ( selection.mouseMode == "right" ) {
                // nothing to do
                } else if (selection.mouseMode == "doubleLeft") {
                    var macroCategory = tinaviz.views.macro.category();
                    //console.log("selected doubleLeft ("+selection.viewName+","+selection.data+")");
                    tinaviz.views.meso.category(macroCategory);
                    if (selection.viewName == "macro") {
                        tinaviz.setView("meso");
                    }
                    tinaviz.updateNodes("meso", macroCategory);
                    tinaviz.autoCentering();
                    tinaviz.views.meso.set("layout/iter", 0);
                }
                tinaviz.infodiv.update(selection.viewName, selection.data);
            },
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

                var showFilter = false;
                if (view.getName() == "meso") {

                    // TODO check selection
                    // if selection has edges with edge of all the same weight, we disable the filter
                    var weight = null;
                    for (node in view.nodes) {
                        //alert("node:"+node);
                        for (out in node.outputs) {
                            //alert("node weight:"+out.weight);
                            if (weight == null) {
                                weight = out.weight;
                            }
                            else {
                                if (weight != out.weight) {
                                    showFilter = true;
                                    break;
                                }
                            }
                        }
                    }

                }
                $("#sliderEdgeWeight").slider( "option", "disabled", false );
            }
        });

        var size = resize();
        tinaviz.size(size.w, size.h);

        /*tinaviz.open({
            view: "macro",
            url: "user/ty/gexf/20100728-1-graph.gexf"
        });*/
    });


});
