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

const HELP_URL = "http://tina.csregistry.org/";
const INTRO_URL = "chrome://tina/content/about.xul";

/*******************************************************************************
 * Functions submitting requests to Tinaserver
 * see tinaservice.js and tinaservicecallbacks.js
*******************************************************************************/

/*
 * Requests to import/extract a data set source file
 */

var submitImportfile = function(event) {
    var corpora = $("#importdatasetid");
    var path = $("#importfilepath");
    var filetype = $("#importfiletype");
    if ( corpora.val() == '' ) {
        corpora.addClass('ui-state-error');
        console.log( "missing the corpora field" );
        return false;
    }
    if ( path.val() == "" ) {
        path.addClass('ui-state-error');
        console.log( "missing the path field" );
        return false;
    }
    var overwrite = $("#importoverwrite:checked");
    //alert(overwrite.val());
    if (overwrite.val() !== undefined) {
        overwrite = 'True';
    }
    else {
        overwrite = 'False';
    }
    var extract = $("#importextract:checked");
    var callback = false;
    //alert(extract.val());
    if (extract.val() !== undefined) {
        TinaService.getFile(
            path.val(),
            corpora.val(),
            filetype.val(),
            overwrite,
            TinaServiceCallback.extractFile
        );
        //return true;
    }
    else {
        TinaService.postFile(
            corpora.val(),
            path.val(),
            filetype.val(),
            overwrite,
            TinaServiceCallback.importFile
        );
        //return true;
    }

};

/*
 * Requests to process cooccurrences
 * then to generate a graph
 */

var submitprocessCoocGraph = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    var userfilterspath  = $("#userstopwordsfile")
    if ( whitelistpath.val() == '' ) {
        whitelistpath.addClass('ui-state-error');
        console.log( "missing the white list path field" );
        return false;
    }
    TinaServiceCallback.postCooc.success = function(){
        TinaService.postGraph(
            corpora,
            corporaAndPeriods[corpora],
            whitelistpath.val(),
            TinaServiceCallback.postGraph
        );
    };
    for (corpora in corporaAndPeriods) {
        TinaService.postCooccurrences(
            corpora,
            corporaAndPeriods[corpora],
            whitelistpath.val(),
            userfilterspath.val(),
            TinaServiceCallback.postCooc
        );
        break;
    }
    //return true;
};


/* Writing a data set's graph action controler */

/*
var submitExportGraph = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    // DEBUG
    if ( whitelistpath.val() == '' ) {
        whitelistpath.addClass('ui-state-error');
        whitelistpath.addClass('ui-state-error');
        console.log( "missing the white list path field" );
        return false;
    }
    threshold = [0,1];
    for (corpora in corporaAndPeriods) {
        TinaService.runExportGraph(
            corpora,
            corporaAndPeriods[corpora],
            threshold,
            whitelistpath.val()
        );
        return true;
    }
};
* /

/* Requests to export a data set's whitelist csv */

var submitExportWhitelist = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var complementwhitelistfile = $("#complementwhitelistfile");
    var userstopwordsfile = $("#userstopwordsfile");
    var whitelistlabel = $("#whitelistlabel");
    var minoccs = $("#minoccs");
    if ( whitelistlabel.val() == '' ) {
        whitelistlabel.addClass('ui-state-error');
        alert( "please choose a white list label" );
        return false;
    }

    for (corpora in corporaAndPeriods) {
        //console.log( corporaAndPeriods[corpora]);

        TinaService.getWhitelist(
            corpora,
            corporaAndPeriods[corpora],
            whitelistlabel.val(),
            complementwhitelistfile.val(),
            userstopwordsfile.val(),
            parseInt(minoccs.val()),
            TinaServiceCallback.getWhitelist
        );
        return true;
    }
};


/******************************************************************************
 * Functions displaying dynamic content
 *****************************************************************************/

/* Duplicate documents found after data set import */
var displayDuplicateDocs = function(data) {
    if (data.length == 0)
        $( "#duplicate_docs" ).empty().hide();
    else {
        var div = $( "#duplicate_docs" ).empty().show();
        div.append( "<h3>duplicate documents found ("+ (data.length) +")</h3>" );
        for ( var i=0; i < data.length; i++ ) {
            div.append( "<p class='ui-state-active'>"+data[i]['id']+"<br/>"+data[i]['label']+"</p>" );
        }
    }
};

/*
 * displays the list of existing graphs
 * for a given <TR> and a dataset id
 */
function displayGraphColumn(corpora) {
    //console.log("displayListGraph : row = " + trid + " , dataset = "+ corpora);
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'graph_list_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();

    TinaService.getGraph(
        corpora.id,
        {
            success: function(graphList) {
                for ( var i=0; i < graphList.length; i++ ) {
                    var button = $("<button class='ui-state-default ui-corner-all' value='"
                        + graphList[i]
                        + "'>"
                        + graphList[i]
                        + "</button><br/>"
                    ).click(function(event) {
                        tinaviz.clear();
                        $("#tabs").data('disabled.tabs', []);
                        switchTab( "macro" );
                        tinaviz.readGraphJava("macro",$(this).attr('value'));
                    });
                    ol.append(button);
                }
            }
        }
    );
}

function displayWhitelistColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'whitelist_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    TinaService.getWalkUserPath(
        corpora.id,
        "whitelist",
        {
            success: function(list) {
                for ( var i=0; i < list.length; i++ ) {
                    var button = $("<button class='ui-state-default ui-corner-all' value='"
                        + list[i]
                        + "'>"
                        + list[i]
                        + "</button><br/>"
                    ).click(function(event) {
                        // TODO sets the working session, and get whitelist information from DB
                    });
                    ol.append(button);
                }
            }
        }
    );
}

/*
 * Displays the list of corpus (selectable buttons)
 * for a given corpora and a <TR>
 */
function displayPeriodColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'selectable_corpus_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' class='selectable'>"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty()
    for ( var id in corpora['edges']['Corpus'] ) {
        ol.append("<li id='"
            + id
            + "' class='ui-widget-content ui-state-default'>"
            + id
        +"</li>");
    }
    selectableCorpusInit( ol, corpora );
}

/*
 * Resets any li from any 'selectable' class div
 */
function selectableReset() {
    /* Resets any li from any 'selectable' class div */
    $("li",".selectable").each(function(){
        var li = $(this);
        li.removeClass('ui-state-active');
    });
}

/*
 * Create selectable buttons on a list of corpus
 */
function selectableCorpusInit( ol, corpora ) {
    ol.selectable({
        stop: function(){
            selectableReset();
            corporaAndPeriods = {};
            corporaAndPeriods[corpora.id] = [];
            $(".ui-selected",this).each(function(){
                var selected_li = $(this);
                selected_li.addClass('ui-state-active');
                corporaAndPeriods[corpora.id].push(( selected_li.html() ));
            });
            Cache.setValue( "last_selected_periods", corporaAndPeriods );
            //console.log ( Cache.getValue( "last_selected_periods", "", 1 ) );
        },
    });
}

function displayDatasetRow(list) {
    var tbody = $("#data_table > table > tbody");
    for ( var i=0; i<list.length; i++ ) {
        // populates each row
        var dataset_id = list[i];
        var trid = dataset_id + "_tr";
        var tr = $("<tr id='"+dataset_id+"_tr'></tr>")
            //.addClass("ui-widget-content")
            .append( $("<td></td>").html(dataset_id) )
        ;
        tbody.append(tr);
        TinaService.getDataset(dataset_id, {
            success: function(dataset) {
                //tr.append( $("<td></td>").html(dataset.label) );
                displayPeriodColumn( dataset );
                displayGraphColumn( dataset );
                displayWhitelistColumn( dataset );
            }
        });
    }
}

/*
 * Gets the list of datasets
 * and populates a table
 * with corpus and graphs
 */
function displayDataTable(parent_div) {
    // populates each row
    //var datasetName = "";
    //var trid = "";
    var tbody = $( "<tbody></tbody>" );
    //tbody.addClass("ui-widget-content");
    $("#"+parent_div+" > table").append(tbody);
    TinaService.getDatasetList({
        success: function(list) {
            displayDatasetRow(list);
        }
        //Cache.setValue("last_data_table", table);
    });
}


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


    $('#waitMessage').effect('pulsate', {}, 'fast');

    /*
    MAGIC TRICK FOR CHROME AND JAVA (MAYBE NOT NEEDED ANYMORE)
    MAGIC TRICK FOR MAKE THE APPLET WORK WITH firefox chrome / file:// protocols
    */

    var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
    var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
    var appletPath;
    if (path.search(/\\/) != -1) { appletPath = path + "\\content\\applet_iframe.html" }
    else { appletPath = path + "/content/applet_iframe.html" }
    var appletFile = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
    appletFile.initWithPath(appletPath);
    var appletURL = Components.classes["@mozilla.org/network/protocol;1?name=file"].createInstance(Components.interfaces.nsIFileProtocolHandler).getURLSpecFromFile(appletFile);
    var iframehtml = '<iframe id="vizframe" name="vizframe" class="vizframe" allowtransparency="false" scrolling="no" frameborder="0" src="'+appletURL+'"></iframe>';
    window.setTimeout("$('#container').html('"+iframehtml+"');", 3000);


    tinaviz = new Tinaviz({
        tag: $("#vizdiv"),
        path: "tinaweb/js/tinaviz/",
        context: "parent.",
        engine: "software",
        width: 0,
        height: 0
    });

    tinaviz.ready(function(){

        var infodiv =  InfoDiv('infodiv');
        tinaviz.infodiv = infodiv;

        // auto-adjusting infodiv height
        $(infodiv.id).css('height', tinaviz.height - 40);

        $(infodiv.id).accordion({
            fillSpace: true,
        });

        infodiv.reset();

        var w = getScreenWidth() - 390;
        var h = getScreenHeight() - $("#hd").height() - $("#ft").height() - 60;
        tinaviz.size(w, h);

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

        //tinaviz.readGraphJava("macro", "FET60bipartite_graph_cooccurrences_.gexf");
        tinaviz.readGraphJava("macro", "bipartite_graph_bipartite_map_bionet_2004_2007_g.gexf_.gexf");

        // init the node list with ngrams
        tinaviz.updateNodes( "macro", "NGram" );

        // cache the document list
        tinaviz.getNodes( "macro", "Document" );

        $("#waitMessage").hide();

        infodiv.display_current_category();
        infodiv.display_current_view();
    });

    $('#waitMessage').effect('pulsate', {}, 'fast');
    //$("#tabs").tabs();
    $('#hide').hide();
    /* resets cache vars */
    var corporaAndPeriods = Cache.setValue( "last_selected_periods", {} );

    if (!tinaviz.isEnabled()) {
        //resizeApplet();
        tinaviz.setEnabled(true);
        tinaviz.setView("macro");
    }

    /*$("#tabs").bind('tabsselect', function(event, ui) {

        // MAGIC TRICK FOR THE JAVA IFRAME
        if (ui.index == 2) {
            if (!tinaviz.isEnabled()) {
                resizeApplet();
                tinaviz.setEnabled(true);
            }
            tinaviz.setView("macro");
        } else if (ui.index == 3) {
            if (!tinaviz.isEnabled()) {
                resizeApplet();
                tinaviz.setEnabled(true);
            }
            tinaviz.setView("meso");
        } else {
            // hide the frame; magic!
            tinaviz.setEnabled(false);

            $('#vizframe').css("height","0px");
            $('#vizframe').css("width","0px");

            $('#whitebox').css("height","0px");
            $('#whitebox').css("width","0px");

            $('#infodiv').css("height","0px");
            $('#infodiv').css("width","0px");
            $('#hide').hide();
        }
    });*/


    /*var max = 0;
    $("label").each(function(){
        if ($(this).width() > max)
            max = $(this).width();
    });
    $("label").width(max);*/
    $('#importFile').click(function(event) {
        submitImportfile(event);
    });
    $('#exportWhitelist').click(function(event) {
        submitExportWhitelist(event)
    });
    $('#processCooc').click(function(event) {
        submitprocessCoocGraph(event)
    });
    /*$('#exportGraph button').click(function(event) {
        submitExportGraph(event)
    });*/

    var dupldoc = $( "#duplicate_docs" ).empty().hide();

    $(window).bind('resize', function() {
        if (tinaviz.isEnabled()) {
            resizeApplet();
        }
    });

    /*
    // TODO : handler to open a graph file
    $('#htoolbar input[type=file]').change(function(e){
        tinaviz.clear();
        tinaviz.readGraphJava( $(this).val() );
    });

    // all hover and c$( ".selector" ).slider( "option", "values", [1,5,9] );lick logic for buttons

    $(".fg-button:not(.ui-state-disabled)")
    .hover(
        function(){
            $(this).addClass("ui-state-hover");
        },
        function(){
            $(this).removeClass("ui-state-hover");
        }
    )
    .mousedown(function(){
        $(this).parents('.fg-buttonset-single:first').find(".fg-button.ui-state-active").removeClass("ui-state-active");
        if( $(this).is('.ui-state-active.fg-button-toggleable, .fg-buttonset-multi .ui-state-active') ) {
            $(this).removeClass("ui-state-active");
        }
        else {
            $(this).addClass("ui-state-active");
        }
    })
    .mouseup(function(){
        if(! $(this).is('.fg-button-toggleable, .fg-buttonset-single .fg-button,  .fg-buttonset-multi .fg-button') ) {
            $(this).removeClass("ui-state-active");
        }
    });

    // binds the click event to tinaviz.searchNodes()

    $("#macro-search").submit(function() {
      var txt = $("#macro-search-input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
     });
      $("#meso-search").submit(function() {
      var txt = $("#meso-search-input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
    });
    /*
    $("#search").keypress(function() {
      var txt = $("#search-input").val();
      if (txt=="") {
        tinaviz.unselect();
      } else {
           tinaviz.highlightNodes(txt, "containsIgnoreCase");
      }
    });
    */
    /*$("#macro-search-button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#macro-search-input").val();
          if (txt=="") {
                tinaviz.unselect();
          } else {
                tinaviz.searchNodes(txt, "containsIgnoreCase");
          }
    });

    $("#meso-search-button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#meso-search-input").val();
          if (txt=="") {
                tinaviz.unselect();
          } else {
                tinaviz.searchNodes(txt, "containsIgnoreCase");
          }
    });

    // SLIDERS INIT
    $.extend($.ui.slider.defaults, {
        //range: "min",
        min: 0,
        max: 100,
        value: 100.0,
        animate: true,
        orientation: "horizontal",
    });

    // MACRO SLIDERS
    $("#macro-sliderEdgeWeight #meso-sliderEdgeWeight").slider({
        range: true,
        values: [0, 100],
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "edgeWeight/min", ui.values[0] / 100.0);
            tinaviz.setProperty("current", "edgeWeight/max", ui.values[1] / 100.0);
            tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }
    });

    $("#macro-sliderNodeWeight #meso-sliderNodeWeight").slider({
        range: true,
        values: [0, 100],
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "nodeWeight/min", ui.values[0] / 100.0);
            tinaviz.setProperty("current", "nodeWeight/max", ui.values[1] / 100.0);
            tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }
    });

    $("#macro-sliderNodeSize  #meso-sliderNodeSize").slider({
        value: 50.0,
        max: 100.0,// precision/size
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "output/nodeSizeRatio", ui.value / 100.0);
            //tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }}
    );

    $("#macro-sliderSelectionZone #meso-sliderSelectionZone").slider({
        value: 1.0,
        max: 300.0, // max disk radius, in pixel
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "selection/radius", ui.value);
            tinaviz.touch();
        }
    });*/

    /*
    $("#macro-toggle-showLabels #meso-toggle-showLabels").click(function(event) {
        tinaviz.toggleLabels();
    });

    $("#macro-toggle-showNodes #meso-toggle-showNodes").click(function(event) {
        tinaviz.toggleNodes();
    });

    $("#macro-toggle-showEdges #meso-toggle-showEdges").click(function(event) {
        tinaviz.toggleEdges();
    });
    */
    /*
    $("#macro-toggle-paused #meso-toggle-paused").button({
        icons: {primary:'ui-icon-pause'},
        text: true,
        label: "pause",
    })
    .click(function(event) {
        tinaviz.togglePause();
        if( $("#macro-toggle-paused").button('option','icons')['primary'] == 'ui-icon-pause'  ) {
            $("#macro-toggle-paused").button('option','icons',{'primary':'ui-icon-play'});
            $("#macro-toggle-paused").button('option','label',"play");
        }
        else {
            $("#macro-toggle-paused").button('option','icons',{'primary':'ui-icon-pause'});
            $("#macro-toggle-paused").button('option','label',"pause");
        }
    });
    $("#meso-toggle-paused").button({
        icons: {primary:'ui-icon-pause'},
        text: true,
        label: "pause",
    })
    .click(function(event) {
        tinaviz.togglePause();
        if( $("#meso-toggle-paused").button('option','icons')['primary'] == 'ui-icon-pause'  ) {
            $("#meso-toggle-paused").button('option','icons',{'primary':'ui-icon-play'});
            $("#meso-toggle-paused").button('option','label',"play");
        }
        else {
            $("#meso-toggle-paused").button('option','icons',{'primary':'ui-icon-pause'});
            $("#meso-toggle-paused").button('option','label',"pause");
        }
    });
    $("#macro-toggle-unselect #meso-toggle-unselect").button({
        icons: {primary:'ui-icon-close'},
    }).click(function(event) {
        tinaviz.unselect();
    });

    $("#macro-toggle-autoCentering #meso-toggle-autoCentering").button({
        text: true,
        icons: {
            primary: 'ui-icon-home'
        }
    })
    .click(function(event) {
        tinaviz.autoCentering();
    });

    $("#macro-toggle-switch #meso-toggle-switch").button({
        text: true,
        icons: {
            primary: 'ui-icon-arrows'
        },
    }).click(function(event) {
        tinaviz.toggleCategory("current");
    });*/

    // magic trick for applet loading rights


    // hide by default all submit forms
    //$("#import_form").hide();
    //$("#whitelist_form").hide();
    //$("#graph_form").hide();
    //$("#cooc_form").hide();


    /* Fetch data into table */
    var data_table = displayDataTable("data_table");

    $("#toggle_working_session").button({
        icons: {primary:'ui-icon-carat-2-e-w'},
        text: true,
        label: "work session manager"
    })
    .click(function(event) {
        $("#data_table").toggle("fold");
        // TODO : display current state of the session in the button label
    });

    $("#about_tinasoft").hide();
    $("#toggle_about").button({
        icons: {primary:'ui-icon-info'},
        text: true,
        label: "about tinasoft"
    })
    .click(function(event) {
        $("#about_tinasoft").dialog({modal: true});
    });
});
