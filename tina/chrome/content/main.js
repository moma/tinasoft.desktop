/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL = "chrome://tina/content/about.xul";

/* Tinasoft XPCOM Service instance */

if ( typeof(TinaService) == "undefined" ) {
    cls = Cc["Python.Tinasoft"];
    var TinaService = cls.getService(Ci.ITinasoft);
}

/* Tinasoft observers handler */

var tinasoftTaskObserver = {
    observe : function ( subject , topic , data ){
        // traitements en fonction du topic...
        if(topic == "tinasoft_runImportFile_finish_status"){
            //alert("tinasoft_runImportFile_finish_status");
            $('#importFile button').toggleClass("ui-state-disabled", 1);
            $('#exportCorpora button').toggleClass("ui-state-disabled", 1);
        }
        if (topic == "tinasoft_runImportFile_running_status") {
            //alert("tinasoft_runImportFile_running_status, please wait");
            $('#importFile button').toggleClass("ui-state-disabled", 1);
            $('#exportCorpora button').toggleClass("ui-state-disabled", 1);
            //console.log(data);
        }
        if(topic == "tinasoft_runProcessCooc_finish_status"){
            $('#processCooc button').toggleClass("ui-state-disabled", 1);
            $('#generateGraph button').toggleClass("ui-state-disabled", 1);
            console.log("tinasoft_runProcessCooc_finish_status = " +data);
        }
        if (topic == "tinasoft_runProcessCooc_running_status") {
            $('#processCooc button').toggleClass("ui-state-disabled", 1);
            console.log("tinasoft_runProcessCooc_running_status = " +data);
        }
        if(topic == "tinasoft_runExportGraph_finish_status"){
            $('#generateGraph button').toggleClass("ui-state-disabled", 1);
            console.log("tinasoft_runExportGraph_finish_status = " +data);
        }
        if (topic == "tinasoft_runExportGraph_running_status") {
            $('#generateGraph button').toggleClass("ui-state-disabled", 1);
            console.log("tinasoft_runExportGraph_running_status = " +data);
        }
    }
};

/* Setting Tinasoft observers */

var ObserverServ = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
// Observers registering
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCooc_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCooc_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCooc_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCooc_running_status" , false );

/* Importing data set action controler */

var submitImportfile = function(event) {
    corpora = $("#corpora")
    path = $("#csvfile")
    config  = $("#configfile")
    //exportpath = $("#exportfile")
    // DEBUG
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
        false,
        'tina'
    );
    return true;

};

/* Writing a data set's cooccurrences action controler */

var submitprocessCooc = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    var userfilterspath  = $("#userfiltersfile")
    // DEBUG
    if ( whitelistpath.val() == '' ) {
        whitelistpath.addClass('ui-state-error');
        console.log( "missing the white list path field" );
        return false;
    }

    for (corpora in corporaAndPeriods) {
        TinaService.runProcessCooc(
            whitelistpath.val(),
            corpora,
            corporaAndPeriods[corpora],
            userfilterspath.val()
        );
    }
    return true;
};


/* Writing a data set's graph action controler */

var submitExportGraph = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    // DEBUG
    if ( whitelistpath.val() == '' ) {
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
    }
    return true;
};

/* Indexation workflow control */

var listCorpora = function() {
    corporaList=TinaService.listCorpora();
    return( JSON.parse(corporaList) );
};

function displayListCorpus(trid, corpora) {
    var tr = $("#"+trid);
    tr.append("<td>"
        + "<ol id='selectable_corpus_"
        + trid + "' class='selectable'>"
        + "</ol></td>"
    );
    var ol = $( "#selectable_corpus_" + trid  ).empty()
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
        console.log("removed selected");
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
            console.log ( Cache.getValue( "last_selected_periods", "", 1 ) );
        },
    });
}

var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );

function displayListCorpora(table) {
    var body = $(table + " tbody")
    body.empty();
    var list = listCorpora();
    for ( var i=0, len=list.length; i<len; i++ ){
        var corpora = list[i]
        body.append("<tr id='tr_corpora_" + i
            +"' class='ui-widget-content'>"
            + "<td>"
            + corpora.label
            + "</td>"
        +"</tr>");
        displayListCorpus( "tr_corpora_" + i, corpora );
    }
}

/* Storage getters */
var getCorpus = function(corpusid) {
    return( JSON.parse( TinaService.getCorpus(corpusid) ) );
};
var getDocument = function(documentid) {
    console.log("doc id="+documentid);
    return( JSON.parse( TinaService.getDocument(documentid) ) );
};
var getCorpora = function(corporaid) {
    return( JSON.parse( TinaService.getCorpora(corporaid) ) );
};
var getNGram = function(ngramid) {
    return( JSON.parse( TinaService.getNGram(ngramid) ) );
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
function computeAppletWidth() {
    return getWidth() - 15;
}

/* TODO replace by CSS query */
function computeAppletHeight() {
    return getHeight() - 140;
}

/* called when java applet is ready */
function appletReady() {
  $("#tabs").data('disabled.tabs', [4]);
}

function selectMacro() {
    $("#tabs").tabs( 'select' , 2 );
}
function selectMeso() {
    $("#tabs").tabs( 'select' , 3 );
}
function selectMicro() {
    $("#tabs").tabs( 'select' , [4] );
}

// wait for the DOM to be loaded
$(document).ready(function() {
    $("#tabs").tabs();
    $("#tabs").data('disabled.tabs', [2, 3, 4]);

    $("#tabs").bind('tabsselect', function(event, ui) {

        // MAGIC TRICK FOR THE JAVA IFRAME
        if (ui.index == 2) {
            if (!tabvizframe.tinaviz.isEnabled()) {
                $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
                $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
                tabvizframe.tinaviz.resized();
                tabvizframe.tinaviz.setEnabled(true);
            }
            tabvizframe.tinaviz.selectToMacro();
        } else if (ui.index == 3) {
            if (!tabvizframe.tinaviz.isEnabled()) {
                $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
                $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
                tabvizframe.tinaviz.resized();
                tabvizframe.tinaviz.setEnabled(true);
            }
            tabvizframe.tinaviz.selectToMeso();
        } else if (ui.index == 4) {
            if (!tabvizframe.tinaviz.isEnabled()) {
                $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
                $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
                tabvizframe.tinaviz.resized();
                tabvizframe.tinaviz.setEnabled(true);
            }
            tabvizframe.tinaviz.selectToMicro();
        } else {
            // hide the frame; magic!
            tabvizframe.tinaviz.setEnabled(false);
            $('#tabvizframe').css("height","0px");
            $('#tabvizframe').css("width","0px");
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
        submitprocessCooc(event)
    });
    $('#generateGraph button').click(function(event) {
        submitExportGraph(event)
    });

    $.extend($.ui.slider.defaults, {
            range: "min",
            animate: true,
            orientation: "vertical"
    });
        // setup master volume
    $("#localrepulsion").slider({
            value: 100,
            orientation: "horizontal"
    });
    $("#globalrepulsion").slider({
            value: 100,
            orientation: "horizontal"
    });
    /*$("#disable-widgets").toggle(function() {
    buttons.button("disable");
    }, function() {
    buttons.button("enable");
    });
    $("#toggle-widgets").toggle(function() {
    buttons.button();
    }, function() {
    buttons.button("destroy");
    }).click();*/
    displayListCorpora( "#corpora_table" );

    // TINASOFT WINDOW IS RESIZED
    $(window).bind('resize', function() {
        // check if the applet is ready
        if (tabvizframe.tinaviz.isEnabled()) {
            // resize the iframe with pixel precision (we decided to control the size manually in our CSS)
            $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
            $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
            // call the tinaviz's resized() callback
            tabvizframe.tinaviz.resized();
        }
    });
});
