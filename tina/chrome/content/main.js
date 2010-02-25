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
        if(topic == "tinasoft_finish_status"){
            alert("tinasoft_finish_status");
            console.log(data);
        }
        if (topic == "tinasoft_running_status") {
            alert("tinasoft_running_status, please wait");
            console.log(data);
        }
    }
};

/* Setting Tinasoft observers */

var ObserverServ = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
// enregistrement
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_running_status" , false );

/* Tinasoft corpora importing tool */

var submitImportfile = function(event) {
    corpora = $("#corpora")
    path = $("#csvfile")
    config  = $("#configfile")
    exportpath = $("#exportfile")
    // DEBUG
    //path.val("pubmed_tina_test.csv");
    config.val("import.yaml");
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
    if ( config.val() == "" ) {
        config.addClass('ui-state-error');
        console.log( "missing the config file field" );
        return false;
    }
    if ( exportpath.val() == "" ) {
        exportpath.addClass('ui-state-error');
        console.log( "missing the export path file field" );
        return false;
    }
    TinaService.runImportFile(
        path.val(),
        config.val(),
        corpora.val(),
        exportpath.val(),
        false,
        false,
        'tina'
    );

};

/* Tinasoft storage read acces */

var listCorpora = function() {
    console.log("inside listCorpora()");
    json=TinaService.listCorpora();
    console.log(json);
    return( JSON.parse(json) );
};

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

function displayListCorpora(oldivid) {
    var ol = $("#"+oldivid).empty();
    var json = listCorpora();
    for ( var i=0, len=json.length; i<len; ++i ){
        ol.append("<li id='"+json[i]['id']+"' class='ui-widget-content'>"+json[i]['label']+"</li>\n");
    }
}

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
   $('#push button').click(function(event) {
        //var target = $(event.target);
        submitImportfile(event);
    });
    $('#cooc button').click(function(event) {
        console.error("not implemented yet");
        //runProcessCooc(event);
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
    $("#selectable_corpora").selectable({
        stop: function(){
            //var result = ""
            $(this).each(function(){
                li = $("#selectable li");
                if (li.hasClass("ui-selected")) {
                    var index = li.index(this) + 1;
                    li.removeClass("ui-state-default");
                    li.addClass("ui-state-active");
                }
                else {
                    li.removeClass("ui-state-active");
                    li.addClass("ui-state-default");
                }

            });
        },
        selected: function(event, ui) {
            console.log(ui);
        },
        unselected: function(event, ui){
        },
    });
    displayListCorpora( "selectable_corpora" );


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
