
/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/about.xul";

/* Tinasoft SINGLETON */

if ( typeof(TinaService) == "undefined" ) {
    cls = Cc["Python.Tinasoft"];
    var TinaService = cls.getService(Ci.ITinasoft);
}

var submitImportfile = function(event) {
    corpora = $("#corpora")
    path = $("#csvfile")
    config  = $("#configfile")
    // TODO DEBUG
    path.val("/home/elishowk/code/Tinasoft/tests/pubmed_tina_test.csv");
    config.val("import.yaml");
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
    TinaService.runImportFile(
        path.val(),
        config.val(),
        corpora.val(),
        false,
        false,
        true,
        'tina'
    );
    console.log( "end of submitting file " + path);
    return true;
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

function computeAppletWidth() {
    return getWidth() - 15;
}
         
function computeAppletHeight() {
    return getHeight() - 130;
}
  
// wait for the DOM to be loaded
$(document).ready(function() {
  $("#tabs").tabs();
  $("#tabs").bind('tabsselect', function(event, ui) {
  
      // MAGIC TRICK FOR THE JAVA IFRAME
      if (ui.index == 2) {
        // we want to size the iframe very precisely (at the pixel)
        $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
        $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
        
        if (!tabvizframe.tinaviz.isEnabled()) {
            tabvizframe.tinaviz.resized();
            tabvizframe.tinaviz.setEnabled(true);
        }
        //tabvizframe.tinaviz.setModeGlobal()
        //tabvizframe.tinaviz.loadGexf()
      } else if (ui.index == 3) {
        // we want to size the iframe very precisely (at the pixel)
        $('#tabvizframe').css("height",""+(computeAppletHeight())+"px");
        $('#tabvizframe').css("width",""+(computeAppletWidth())+"px");
        tabvizframe.tinaviz.resized();
        tabvizframe.tinaviz.setEnabled(true);
        //tabvizframe.tinaviz.setModeGlobal()
        //tabvizframe.tinaviz.loadGexf()
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
  var buttons = $('#push button').button();
  buttons.click(function(event) {
    //var target = $(event.target);
    submitImportfile(event);
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
