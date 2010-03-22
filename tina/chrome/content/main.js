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
        console.log(topic + "\nDATA = " + data);
        //data = JSON.parse(data);
        // traitements en fonction du topic...
        if(topic == "tinasoft_runImportFile_finish_status"){
            if (data == TinaService.STATUS_ERROR) {
                $('#importFile button').toggleClass("ui-state-error", 1);
                $('#importFile button').html( "sorry an error happened, please see 'Tools'>'Error Console'>Errors" );
                return;
            }
            $('#importFile button').toggleClass("ui-state-disabled", 1);
            $('#importFile button').html( "Launch" );
            displayListCorpora( "corpora_table" );
            displayListCorpora( "graph_table" );
            $( "#corpora_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runImportFile_running_status") {
            if (data == TinaService.STATUS_RUNNING) {
                $('#importFile button').toggleClass("ui-state-disabled", 1);
                $('#importFile button').html( "starting" );
            }
            $('#importFile button').html( "imported "+data+" lines" );
        }
        if(topic == "tinasoft_runProcessCoocGraph_finish_status"){
            if (data == TinaService.STATUS_ERROR) {
                $('#processCooc button').toggleClass("ui-state-error", 1);
                $('#processCooc button').html( "sorry an error happened, please see 'Tools'>'Error Console'>Errors" );
                return;
            }
            $('#processCooc button').html( "Launch" );
            $('#processCooc button').toggleClass("ui-state-disabled", 1);
            displayListCorpora( "corpora_table" );
            displayListCorpora( "graph_table" );
            $( "#graph_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runProcessCoocGraph_running_status") {
            if (data == TinaService.STATUS_RUNNING) {
                $('#processCooc button').toggleClass("ui-state-disabled", 1);
                $('#processCooc button').html( "starting" );
            }
            $('#processCooc button').html( data );
        }

        if(topic == "tinasoft_runExportGraph_finish_status"){
            displayListCorpora( "corpora_table" );
            displayListCorpora( "graph_table" );
            $( "#graph_table" ).toggleClass("ui-state-highlight", 1);
        }

        if (topic == "tinasoft_runExportGraph_running_status") {
        }

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

/**************************
 * Tinasoft FORM ACTIONS
***************************/

/* Importing data set action controler */

var submitImportfile = function(event) {
    var corpora = $("#corpora");
    var path = $("#csvfile");
    var config  = $("#configfile");
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
        'tina',
        overwrite
    );
    return true;

};

/* Writing cooccurrences and generate a graph action controler */

var submitprocessCoocGraph = function(event) {
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
    var whitelistpath = $("#whitelistfile")
    var userfilterspath  = $("#userfiltersfile")
    if ( whitelistpath.val() == '' ) {
        whitelistpath.addClass('ui-state-error');
        console.log( "missing the white list path field" );
        return false;
    }
    // DEBUG VALUE
    threshold = [0,1];
    for (corpora in corporaAndPeriods) {
        TinaService.runProcessCoocGraph(
            whitelistpath.val(),
            corpora,
            corporaAndPeriods[corpora],
            userfilterspath.val(),
            threshold
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
    var userstopwordspath = $("#preuserstopwordsfile");
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

var listCorpora = function() {
    corporaList=TinaService.listCorpora();
    return( JSON.parse(corporaList) );
};

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
            $("#tabs").data('disabled.tabs', [4]);
            if (tinaviz.loadRelativeGraph("macro",$(this).attr('value')) == true) {
                switchedTo( "macro" );
            }
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
    var body = $( "#" +table+ " tbody" )
    body.empty();
    var list = listCorpora();
    for ( var i=0, len=list.length; i<len; i++ ){
        var corpo_trid = table+ "_tr_corpora_" + i;
        var corpora = list[i]
        body.append("<tr id='"+ corpo_trid
            + "' class='ui-widget-content'>"
            + "<td>"
            + corpora.label
            + "</td>"
        +"</tr>");
        //console.log(body.html());
        displayListCorpus( corpo_trid, corpora );
        displayListGraph( corpo_trid, corpora );

    }
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


function switchedTo(level) {
    var tabs = { "macro" : 2,
                 "meso"  : 3,
                 "micro" : 4 };
    $("#tabs").tabs( 'select' , tabs[level] );
}


function resizeApplet() {
    var w = getAppletWidth();
    var h = getAppletHeight();
    
    $('.tabfiller').css("height",""+(h+10)+"px");

    $('#whitebox').css("height",""+(h)+"px");
    $('#whitebox').css("width",""+(w)+"px");
    
    $('#vizframe').css("height",""+(h)+"px");
    $('#vizframe').css("width",""+(w-350)+"px");
    $('#infodiv').show();
    $('#infodiv').css("height",""+(h-50)+"px");
    $('#infodiv').css("width",""+(300)+"px");
    tinaviz.size(w,h);
}
// wait for the DOM to be loaded
$(document).ready(function() {
    $("#tabs").tabs();

    $("#tabs").data('disabled.tabs', [0, 1, 2, 3, 4]);

    $('#infodiv').hide();

    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );


    $("#tabs").bind('tabsselect', function(event, ui) {

        // MAGIC TRICK FOR THE JAVA IFRAME
        if (ui.index == 2) {
            if (tinaviz.disabled()) {
                resizeApplet();
                tinaviz.enable();
            }
            tinaviz.setLevel("macro");
        } else if (ui.index == 3) {
            if (tinaviz.disabled()) {
                resizeApplet();
                tinaviz.enable();
            }
            tinaviz.setLevel("meso");
        } else if (ui.index == 4) {
            if (tinaviz.disabled()) {
                resizeApplet();
                tinaviz.enable();
            }
            tinaviz.setLevel("micro");
        } else {
            // hide the frame; magic!
            tinaviz.disable();
            $('#vizframe').css("height","0px");
            $('#vizframe').css("width","0px");

            $('#whitebox').css("height","0px");
            $('#whitebox').css("width","0px");

            $('#infodiv').css("height","0px");
            $('#infodiv').css("width","0px");
            $('#infodiv').hide();
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

    $.extend($.ui.slider.defaults, {
            range: "min",
            min: 0,
		    max: 100,
            value: 100.0,
            animate: true,
            orientation: "horizontal",
    });
    
    
    // MESO SLIDERS

    $("#meso_repulsion").slider({slide: function(event, ui) {
		tinaviz.setProperty("meso", "layout/repulsion", ui.value / 100.0);
	    tinaviz.touch("meso");
	}});
		
    $("#meso_weight_threshold").slider({
    	range: true,
		values: [0, 200],
        slide: function(event, ui) {
		    tinaviz.setProperty("meso", "weight/min", ui.values[0] / 200.0);
		    tinaviz.setProperty("meso", "weight/max", ui.values[1] / 200.0);
		    tinaviz.touch("meso");
	    }
	});
	
    $("#meso_node_size").slider({
        max: 800,
        slide: function(event, ui) {
		tinaviz.setProperty("meso", "radius/value", ui.value / 100.0);
		tinaviz.touch("meso");
	}});
    
    
    // MACRO SLIDERS
    $("#macro_repulsion").slider({slide: function(event, ui) {
		tinaviz.setProperty("macro", "layout/repulsion", ui.value / 100.0);
		tinaviz.touch("macro");
	}});
	
    $("#macro_weight_threshold").slider({
    	range: true,
        values: [0, 200],
        slide: function(event, ui) {
		tinaviz.setProperty("macro", "weight/min", ui.values[0] / 200.0);
		tinaviz.setProperty("macro", "weight/max", ui.values[1] / 200.0);
		tinaviz.touch("macro");
	}});

    $("#macro_node_size").slider({
        max: 800,
        slide: function(event, ui) {
		tinaviz.setProperty("macro", "radius/value", ui.value / 100.0);
		tinaviz.touch("macro");
	}});
	
    
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
    displayListCorpora( "corpora_table" );
    displayListCorpora( "graph_table" );

    $('.hover-star').rating({
        callback: function(value, link){
            // TODO update the user db (sessions, votes, comments..) here
        }
    });


    // TINASOFT WINDOW IS RESIZED
    $(window).bind('resize', function() {
        // check if the applet is ready
        if (tinaviz.enabled()) {
            resizeApplet();
        }
    });


       var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var appletPath;
        if (path.search(/\\/) != -1) { appletPath = path + "\\content\\applet\\index.html" }
        else { appletPath = path + "/content/applet/index.html" }
        var appletFile = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
        appletFile.initWithPath(appletPath);
        var appletURL = Components.classes["@mozilla.org/network/protocol;1?name=file"].createInstance(Components.interfaces.nsIFileProtocolHandler).getURLSpecFromFile(appletFile);
        var iframehtml = '<iframe id="vizframe" name="vizframe" class="vizframe" allowtransparency="false" scrolling="no"  frameborder="1" src="'+appletURL+'"></iframe>';

        window.setTimeout("$('#container').html('"+iframehtml+"');", 2000);

});
