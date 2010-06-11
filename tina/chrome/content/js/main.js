/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL = "chrome://tina/content/about.xul";


var TinaService = new TinaServiceClass("http://localhost:8888");

/* Tinasoft observers handlers */

var tinasoftTaskObserver = {
    observe : function ( subject , topic , data ) {
        //console.log(topic + "\nDATA = " + data);
        //data = JSON.parse(data);
        // traitements en fonction du topic...
        if(topic == "tinasoft_runImportFile_finish_status"){
            if (data == TinaService.STATUS_ERROR) {
                $('#importFile button').toggleClass("ui-state-error", 1);
                $('#importFile button').html( "sorry an error happened, please see 'Tools'>'Error Console'>Errors or log directory" );
                return;
            }
            // data contains json encoded list of duplicate documents found
            displayDuplicateDocs( JSON.parse(data) );
            $('#importFile button').toggleClass("ui-state-disabled", 1);
            $('#importFile button').html( "Launch" );
            displayListCorpora( "graph_table" );
            displayListCorpora( "corpora_table" );
            $( "#corpora_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runImportFile_running_status") {
            if (data == TinaService.STATUS_RUNNING) {
                $('#importFile button').toggleClass("ui-state-disabled", 1);
                $('#importFile button').html( "please wait during init" );
            }
            else {
                $('#importFile button').html( "imported "+data+" lines" );
            }
        }

        if(topic == "tinasoft_runProcessCoocGraph_finish_status"){
            var button = $('#processCooc button');
            if (data == TinaService.STATUS_ERROR) {
                button.toggleClass("ui-state-error", 1);
                button.html( "sorry an error happened, please see 'Tools'>'Error Console'>Errors or log directory" );
                return;
            }
            else {
                button.html( "Loading macro view" );
                tinaviz.clear();
                console.log( "opening " + data );
                switchTab( "macro" );
                tinaviz.loadRelativeGraph("macro", JSON.parse(data));
            }
            button.html("New graph");
            button.toggleClass("ui-state-disabled", 1);
            displayListCorpora( "graph_table" );
            displayListCorpora( "corpora_table" );
            //$( "#graph_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runProcessCoocGraph_running_status") {
            if (data == TinaService.STATUS_RUNNING) {
                $('#processCooc button').toggleClass("ui-state-disabled", 1);
                $('#processCooc button').html( "starting" );
            }
            $('#processCooc button').html( data );
        }
        /*
        if(topic == "tinasoft_runExportGraph_finish_status"){
            displayListCorpora( "graph_table" );
            $( "#graph_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runExportGraph_running_status") {
        }*/

        if(topic == "tinasoft_runExportCorpora_finish_status"){
            if (data == TinaService.STATUS_ERROR) {
                $('#exportCorpora button').toggleClass("ui-state-error", 1);
                $('#exportCorpora button').html( "sorry an error happened, please see 'Tools'>'Error Console'>Errors" );
                return;
            }
            $('#exportCorpora button').toggleClass("ui-state-disabled", 1);
            $('#exportCorpora button').html( "Launch" );
        }
        if (topic == "tinasoft_runExportCorpora_running_status") {
            if (data == TinaService.STATUS_RUNNING) {
                $('#exportCorpora button').toggleClass("ui-state-disabled", 1);
                $('#exportCorpora button').html( "starting" );
            }
            $('#exportCorpora button').html( data+" % done" );
        }
    }
};

/* Setting Tinasoft observers */
/*
var ObserverServ = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
// Observers registering
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportCorpora_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportCorpora_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCoocGraph_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCoocGraph_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportGraph_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportGraph_running_status" , false );
*/

/* Duplicate document in data set controler */
var displayDuplicateDocs = function(data) {
    var div = $( "#duplicate_docs" ).empty().show();
    div.append( "<h3>duplicate documents found ("+ (data.length) +")</h3>" );
    for ( var i=0; i < data.length; i++ ) {
        div.append( "<p class='ui-state-active'>"+data[i]['id']+"<br/>"+data[i]['label']+"</p>" );
    }
};

/**************************
 * Tinasoft FORM ACTIONS
***************************/

/* Importing data set action controler */

var submitImportfile = function(event) {
    var corpora = $("#corpora");
    var path = $("#csvfile");
    var config  = $("#configfile");
    var filetype = $("#filetype");
    var overwrite = $("#overwrite:checked");
    if (overwrite.val() !== undefined) {
        overwrite = true;
    }
    else {
        overwrite = false;
    }
    // DEBUG VALUE
    //path.val("pubmed_tina_test.csv");
    //config.val("import.yaml");
    //exportpath.val("test-export.csv");
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
    TinaService.runImportFile(
        path.val(),
        config.val(),
        corpora.val(),
        false,
        filetype.val(),
        overwrite
    );
    return true;

};

/* Writing cooccurrences and generate a graph action controler */

var submitprocessCoocGraph = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    var userfilterspath  = $("#userstopwordsfile")
    if ( whitelistpath.val() == '' ) {
        whitelistpath.addClass('ui-state-error');
        console.log( "missing the white list path field" );
        return false;
    }
    // DEBUG VALUE
    var opts = {
        'DocumentGraph': {
            'edgethreshold': [0.0, 2.0],
            'nodethreshold': [1, 'inf']
        },
        'NGramGraph': {
            'edgethreshold': [0.0, 1.0],
            'nodethreshold': [2, 'inf']
        }
    };
    for (corpora in corporaAndPeriods) {
        TinaService.runProcessCoocGraph(
            whitelistpath.val(),
            corpora,
            corporaAndPeriods[corpora],
            userfilterspath.val(),
            JSON.stringify( opts )
        );
    }
    return true;
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

/* Writing a data set's export csv action controler */

var submitExportCorpora = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#prewhitelistfile");
    var userstopwordspath = $("#userstopwordsfile");
    var exportpath = $("#exportfile");
    if ( exportpath.val() == '' ) {
        exportpath.addClass('ui-state-error');
        console.log( "missing the export path field" );
        return false;
    }
    for (corpora in corporaAndPeriods) {
            /*in wstring periods,
            in wstring corpora_id,
            in wstring exportPath,
            in wstring whitelistPath,
            in wstring userfiltersPath*/
        TinaService.runExportCorpora(
            corporaAndPeriods[corpora],
            corpora,
            exportpath.val(),
            whitelistpath.val(),
            userstopwordspath.val()
        );
        return true;
    }
};

/* Indexation workflow control */



function displayListGraph(trid, corpora) {
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'graph_list_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    var graphList = JSON.parse( TinaService.walkGraphPath(corpora['id']) );
    for ( var i=0; i < graphList.length; i++ ) {
        var button = $("<button class='ui-state-default ui-corner-all' value='"
            + graphList[i]
            + "'>"
            + graphList[i]
            + "</button><br/>"
        ).click(function(event) {
            tinaviz.clear();
            console.log( "opening " + $(this).attr('value') );
            $("#tabs").data('disabled.tabs', []);
            switchTab( "macro" );
            tinaviz.loadRelativeGraph("macro",$(this).attr('value'));
        });
        ol.append(button);
    }
}

function displayListCorpus(trid, corpora) {
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

function selectableReset() {
    /* Resets any li from any 'selectable' class div */
    $("li",".selectable").each(function(){
        var li = $(this);
        li.removeClass('ui-state-active');
    });
}

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

function displayListCorpora(table) {
    TinaService.listDatasets({
        success: function(list) {
        
            var body = $( "#" +table+ " tbody" );
            body.empty();
            for ( var i=0, len=list.length; i<len; i++ ){
                var dtst_trid = table+ "_tr_corpora_" + i;
                var datasetName = list[i];
                
                TinaService.dataset(datasetName, {                
                    success: function(dataset) {
                    body.append("<tr id='"+ dtst_trid
                        + "' class='ui-widget-content'>"
                        + "<td>"
                        + dataset.label
                        + "</td>"
                        +"</tr>");
                //console.log(body.html());
                    displayListCorpus( dtst_trid, dataset );
                    displayListGraph( dtst_trid, dataset );
                    }, 
                    error: function(e) {
                        console.log("couldn't error"); 
                    }
                });


            }
        },
        
        
        error: function(error) {
            console.log(error);
        }
    });
}

/* Storage getters */
var getCorpus = function(corpusid) {
    return( JSON.parse( TinaService.getCorpus(corpusid) ) );
};
var getDocument = function(documentid) {
    // console.log("doc id="+documentid);
    return( JSON.parse( TinaService.getDocument(documentid) ) );
};
var getCorpora = function(corporaid) {
    return( JSON.parse( TinaService.getCorpora(corporaid) ) );
};
var getNGram = function(ngramid) {
    return( JSON.parse( TinaService.getNGram(ngramid) ) );
};


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

$(function(){
    $.extend($.fn.disableTextSelect = function() {
        return this.each(function() {
            if($.browser.mozilla){//Firefox $("#sliderEdgeWeight")
                $(this).css('MozUserSelect','none');
            } else if($.browser.msie) {//IE
                $(this).bind('selectstart',function(){return false;});
            } else {//Opera, etc.
                $(this).mousedown(function(){return false;});
            }
        });
    });
});

/*
 * utility modifying the Object prototype
 * to get its lenght
 */
Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};
/*
 * utility returning a list
 * from the values of a given object
 */
Object.values = function(obj) {
    var values = new Array();
    for (key in obj) {
        values.push(obj[key]);
    }
    return values;
};

/*
 * utility to safely decode encoded
 * values from JSON sent by the java applet
 */
function decodeJSON(encvalue) {
    if (encvalue !== undefined)
        return decodeURIComponent(encvalue).replace(/\+/g, " ").replace(/%21/g, "!").replace(/%27/g, "'").replace(/%28/g, "(").replace(/%29/g, ")").replace(/%2A/g, "*");
    else
        return "";
};

/*
 * Asynchronously displays of node list
 */
function displayNodeRow(label, id, category) {
    //console.console.log("inserting "+label);
    $("#node_table > tbody").append(
        $("<tr></tr>").append(
            $("<td id='"+id+"'></td>").text(label).click( function(eventObject) {
                //switch to meso view
                tinaviz.viewMeso(id, category);
            })
        )
    );
};

/*
 * Infodiv object need tinaviz object to retrieve data
 */
function InfoDiv(divid) {

    return {
    id: divid,
    selection : {},
    neighbours : {},
    label : $( "#node_label" ),
    contents : $( "#node_contents" ),
    cloud : $( "#node_neighbourhood" ),
    unselect_button: $( "#toggle-unselect" ),
    table: $("#node_table > tbody"),
    data: {},
    categories: {
        'NGram' : 'keywords',
        'Document': 'projects',
    },
    last_category: "",
    /*
    * dispatch current category displayed
    */
    display_current_category: function() {
        var current_view = tinaviz.getView();
        var current_cat = tinaviz.getProperty("current","category/category");
        if (current_cat !== undefined)
            var opposite = this.categories[tinaviz.getOppositeCategory(current_cat)];
            //$("#title_acc_1").text("current selection of "+ this.categories[current_cat]);
        if (opposite !== undefined)
            if (current_view == "macro")
                $("#toggle-switch").button("option", "label", "switch to "+ opposite);
            else
                $("#toggle-switch").button("option", "label", "view " + opposite + " neighbours");
        else
            $("#toggle-switch").button("option", "label", "switch category");
    },
    /*
    * dispatch current view displayed
    */
    display_current_view: function() {
        var current_view = tinaviz.getView();
        tinaviz.logNormal( current_view );
        if (current_view !== undefined) {
            var level = $("#level");
        level.empty().html(current_view + " level <span class='ui-icon ui-icon-help icon-right' title='></span>");
            var title = $("#infodiv > h3:first");
            if (current_view == "meso") {
                level.addClass("ui-state-highlight");
                $("#level > span").attr("title","zoom out to switch to the macro view");
                title.addClass("ui-state-highlight");
            }
            else {
                level.removeClass("ui-state-highlight");
                $("#level > span").attr("title","zoom in or double click on a node to switch to is meso view");
                title.removeClass("ui-state-highlight");
            }
        }
    },

    alphabeticListSort: function( listitems, textkey ) {
        listitems.sort(function(a, b) {
            var compA = a[textkey].toUpperCase();
            var compB = b[textkey].toUpperCase();
            return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
        })
        return listitems;

    },

    /*
     * Generic sorting DOM lists
     */
    alphabeticJquerySort: function(parentdiv, childrendiv, separator) {
        var listitems = parentdiv.children(childrendiv).get();
        listitems.sort(function(a, b) {
           var compA = $(a).html().toUpperCase();
           var compB = $(b).html().toUpperCase();
           return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
        })
        $.each(listitems, function(idx, itm) {
            if ( idx != 0 && idx != listitems.length )
                parentdiv.append(separator);
            parentdiv.append(itm);
        });
        return parentdiv;
    },

    /*
     * updates the tag cloud
     * of the opposite nodes of a given selection
     */
    updateTagCloud: function( viewLevel ) {
        /* builds aggregated tag object */
        if (Object.size( this.selection ) == 0) return;
        var tempcloud = {};
        for (var nodeid in this.selection) {
            // gets the full neighbourhood for the tag cloud
            var nb = tinaviz.getNeighbourhood(viewLevel,nodeid);
            var taglist = new Array();
            for (var nbid in nb) {
                if ( tempcloud[nbid] !== undefined )
                    tempcloud[nbid]['degree']++;
                // pushes a node if belongs to the opposite category
                else if (this.selection[nodeid]['category'] != nb[nbid]['category']) {
                    //tinaviz.logNormal("adding to tag cloud : "+decodeJSON(nb[nbid]['label']));
                    tempcloud[nbid] = {
                        'id': nbid,
                        'label' : decodeJSON(nb[nbid]['label']),
                        'degree' : 1,
                        'occurrences': parseInt(nb[nbid]['occurrences']),
                        'category': decodeJSON(nb[nbid]['category']),
                    };
                }
            }
        }
        var sorted_tags = this.alphabeticListSort( Object.values( tempcloud ), 'label' );
        //tinaviz.logNormal(sorted_tags);
        /* some display sizes const */
        var sizecoef = 15;
        var const_doc_tag = 12;
        var tooltip = "";
        /* displays tag cloud */
        var tagcloud = $("<p></p>");
        for (var i = 0; i < sorted_tags.length; i++) {
            var tag = sorted_tags[i];
            var tagid = tag['id'];
            var tagspan = $("<span id='"+tagid+"'></span>");
            tagspan.addClass('ui-widget-content');
            tagspan.addClass('tinaviz_node');
            tagspan.html(tag['label']);
            (function() {
                var attached_id = tagid;
                var attached_cat =  tag['category'];
                tagspan.click( function() {
                    //tinaviz.logNormal("clicked on " + tagid + " - " +tag['label']);
                    //switch to meso view
                    tinaviz.viewMeso(attached_id, attached_cat);
                });
            })();
            // sets the tag's text size
            if (sorted_tags.length == 1) {
                if ( tag['category'] == 'Document' )
                    tagspan.css('font-size', const_doc_tag);
                else
                    tagspan.css('font-size',
                        Math.floor( sizecoef*Math.log( 1.5 + tag['occurrences'] ) )
                    );
                tooltip = "click on a label to switch to its meso view - size is proportional to edge weight";
            }
            else {
                tagspan.css('font-size',
                    Math.floor( sizecoef*Math.log( 1.5 + tag['degree'] ) )
                );
                tooltip = "click on a label to switch to its meso view - size is proportional to the degree";
            }
            // appends the final tag to the cloud paragraph
            tagcloud.append(tagspan);
            if (i != sorted_tags.length-1 && sorted_tags.length > 1)
                tagcloud.append(", &nbsp;");
        }
        // updates the main cloud  div
        this.cloud.empty();
        this.cloud.append( '<h3>selection related to <span class="ui-icon ui-icon-help icon-right" title="'+tooltip+'"></span></h3>' );
        this.cloud.append( tagcloud );
    },

    /*
     * updates the label and content DOM divs
     */
    updateInfo: function(lastselection) {
        var current_cat = tinaviz.getProperty("current", "category/category");
        tinaviz.logNormal("current category = "+current_cat);
        var labelinnerdiv = $("<div></div>");
        var contentinnerdiv = $("<div></div>");
        for(var id in lastselection) {
            var node = lastselection[id];
            if (node.category == current_cat)  {
                this.selection[id] = lastselection[id];
                labelinnerdiv.append( $("<b></b>").html(decodeJSON(node.label)) );
                // displays contents only if it's a document
                if ( node.category == 'Document' && node.content != null ) {
                    contentinnerdiv.append( $("<b></b>").html(decodeJSON(node.label)) );
                    contentinnerdiv.append( $("<p></p>").html(decodeJSON(node.content)) );
                }
            }
        }
        if (Object.size( this.selection ) != 0) {
            this.label.empty();
            this.unselect_button.show();
            this.contents.empty();
            this.label.append( this.alphabeticJquerySort( labelinnerdiv, "b", ", &nbsp;" ));
            this.contents.append( contentinnerdiv );
        }
        else
            this.reset();
    },

    /*
     * updates the infodiv contents
     */
    update: function(view, lastselection) {
        if ( Object.size ( lastselection ) == 0 ) {
            this.reset();
            return;
        }
        this.updateInfo(lastselection);
        this.updateTagCloud("macro");
        return;
    },

    /*
     * Resets the entire infodiv
     */
    reset: function() {
        this.unselect_button.hide();
        this.label.empty().append($("<h2></h2>").html("empty selection"));
        this.contents.empty().append($("<h4></h4>").html("click on a node to begin exploration"));
        this.cloud.empty();
        this.selection = {};
        this.neighbours = {};
        return;
    },

    /*
     * Init the node list
     */
    updateNodeList: function(node_list, category) {
        if (category != this.last_category) {
            this.table.empty();
            this.last_category = category;
            for (var i = 0; i < node_list.length; i++ ) {
                (function () {
                    var rowLabel = decodeJSON(node_list[i]['label']);
                    var rowId = decodeJSON(node_list[i]['id']);
                    var rowCat = category;
                    // asynchronously displays the node list
                    setTimeout("displayNodeRow(\""+rowLabel+"\",\""+rowId+"\",\""+rowCat+"\")", 0);
                })();
            }
        }
    },

    } // end of return
};

function getWidth() {
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

function getHeight() {
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

/* TODO replace by CSS query */
function getAppletWidth() {
    return getWidth() - 57;
}

/* TODO replace by CSS query */
function getAppletHeight() {
    return getHeight() - 142;
}

function switchTab(level) {
    var tabs = { "macro" : 2,
                 "meso"  : 3,
                 "micro" : 4 };
    console.log("switchTab("+level+")");
    $("#tabs").tabs( 'select' , tabs[level] );
}


function resizeApplet() {
    var w = getAppletWidth();
    var h = getAppletHeight();

    $('.tabfiller').css("height",""+(h+15)+"px");

    $('#whitebox').css("height",""+(h)+"px");
    $('#whitebox').css("width",""+(w)+"px");

    $('#vizframe').css("height",""+(h)+"px");
    $('#vizframe').css("width",""+(w-350)+"px");
    $('#hide').show();
    $('#infodiv').css("height",""+(h-50)+"px");
    $('#infodiv').css("width",""+(300)+"px");
    tinaviz.size(w - 350,h);
    
   //$("#infodiv").css( 'height', getScreenHeight() - $("#hd").height() - $("#ft").height() - 60);
}


// wait for the DOM to be loaded
$(document).ready(function() {
    //$("#tabs").tabs( { disabled: [2,3] } );;
    $("#tabs").tabs();
    $('#hide').hide();
    /* restores cache vars */
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );

    $("#tabs").bind('tabsselect', function(event, ui) {

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
    });
    var max = 0;
    $("label").each(function(){
        if ($(this).width() > max)
            max = $(this).width();
    });
    $("label").width(max);
    $('#importFile button').click(function(event) {
        submitImportfile(event);
    });
    $('#exportCorpora button').click(function(event) {
        submitExportCorpora(event)
    });
    $('#processCooc button').click(function(event) {
        submitprocessCoocGraph(event)
    });
    /*$('#exportGraph button').click(function(event) {
        submitExportGraph(event)
    });*/

    var dupldoc = $( "#duplicate_docs" ).empty().hide();
    $.extend($.ui.slider.defaults, {
            //range: "min",
            min: 0,
            max: 100,
            value: 100.0,
            animate: true,
            orientation: "horizontal",
    });

    /* Fetch data into table */
    displayListCorpora( "graph_table" );
    displayListCorpora( "corpora_table" );

    $(window).bind('resize', function() {
        if (tinaviz.isEnabled()) {
            resizeApplet();
        }
    });
    

    //No text selection on elements with a class of 'noSelect'
    /*
    $('.noSelect').disableTextSelect();
    $('.noSelect').hover(function() {
        $(this).css('cursor','default');
    }, function() {
        $(this).css('cursor','auto');
    });*/


    var infodiv = new InfoDiv("#infodiv");

    // auto-adjusting infodiv height
    var new_size = tinaviz.getHeight() - 40;
    $(infodiv.id).css( 'height', new_size);

    $(infodiv.id).accordion({
        fillSpace: true,
    });

    // cleans infodiv
    infodiv.reset();
    // passing infodiv to tinaviz is REQUIRED
    tinaviz.infodiv = infodiv;

    // TODO : handler to open a graph file
    /*$('#htoolbar input[type=file]').change(function(e){
        tinaviz.clear();
        tinaviz.loadAbsoluteGraph( $(this).val() );
    });*/

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
      var txt = $("#macro-search_input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
     });
      $("#meso-search").submit(function() {
      var txt = $("#meso-search_input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
    });
    /*
    $("#search").keypress(function() {
      var txt = $("#search_input").val();
      if (txt=="") {
        tinaviz.unselect();
      } else {
           tinaviz.highlightNodes(txt, "containsIgnoreCase");
      }
    });
    */
    $("#macro-search_button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#macro-search_input").val();
          if (txt=="") {
                tinaviz.unselect();
          } else {
                tinaviz.searchNodes(txt, "containsIgnoreCase");
          }
    });
    
    $("#meso-search_button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#meso-search_input").val();
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
    /*
    $("#sliderNodeSize").slider({
        value: 50.0,
        max: 100.0,// precision/size
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "output/nodeSizeRatio", ui.value / 100.0);
            //tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }}
    );
*/
    $("#macro-sliderSelectionZone #meso-sliderSelectionZone").slider({
        value: 1.0,
        max: 300.0, // max disk radius, in pixel
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "selection/radius", ui.value);
            tinaviz.touch();
        }
    });

    $("#macro-toggle-showLabels #meso-toggle-showLabels").click(function(event) {
        tinaviz.toggleLabels();
    });

    $("#macro-toggle-showNodes #meso-toggle-showNodes").click(function(event) {
        tinaviz.toggleNodes();
    });

    $("#macro-toggle-showEdges #meso-toggle-showEdges").click(function(event) {
        tinaviz.toggleEdges();
    });

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
    });

   $('#waitMessage').effect('pulsate', {}, 'fast');

    // magic trick for applet loading rights
    
    var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
    var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
    var appletPath;
    if (path.search(/\\/) != -1) { appletPath = path + "\\content\\applet\\index.html" }
    else { appletPath = path + "/content/applet/index.html" }
    var appletFile = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
    appletFile.initWithPath(appletPath);
    var appletURL = Components.classes["@mozilla.org/network/protocol;1?name=file"].createInstance(Components.interfaces.nsIFileProtocolHandler).getURLSpecFromFile(appletFile);
    var iframehtml = '<iframe id="vizframe" name="vizframe" class="vizframe" allowtransparency="false" scrolling="no" frameborder="0" src="'+appletURL+'"></iframe>';
    window.setTimeout("$('#container').html('"+iframehtml+"');", 1000);

    $("#tabs-1-accordion").accordion({
        autoHeight: false,
        clearStyle: true,
    });
    $("#importForm").accordion({
        autoHeight: false,
        clearStyle: true,
    });
});
