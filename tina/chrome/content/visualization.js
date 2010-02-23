function Tinaviz() {
  // Private variable
  var wrapper = null;
  var applet = null;
  var width = null;
  var height = null;
  
  // Private method
  var privateMethod = function(){
    // Access to private fields
    //name += " Changed";
  };

  return {
    init: function() {
        wrapper = $('#vizframe').contents().find('#tinaviz')[0];
        applet = wrapper.getSubApplet();
        
        applet.setEnabled(false);
        
        this.resized();
        
        $('#gui').show();
    },
    
    resized: function() {
        if (applet == null) return;
        wrapper.width = computeAppletWidth();
        wrapper.height = computeAppletHeight();
        // update the overlay layout (eg. recenter the toolbars)
        $('.htoolbar').css('left', (  (wrapper.width - parseInt($('#htoolbariframe').css('width'))) / 2   ));
    },
    
    // Public methods
    loadGraphURI: function(uri) {
        if (applet == null) return;
        applet.updateViewFromURI(uri);
    },
    loadGraphString: function(gexf) {
        if (applet == null) return;
        applet.updateViewFromString(gexf);
    },
    selectNodesByNamePattern: function(name) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf); 
    }, 
    selectNodeByUUID: function(gexf) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf);
    },  
    setGenericityRange: function(from,to) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf);
    },
    toggleLabels: function() {
        if (applet == null) return;
        applet.toggleLabels();
    },
    toggleNodes: function() {
        if (applet == null) return;
        applet.toggleNodes();
    },
    toggleEdges: function() {
        if (applet == null) return;
        applet.toggleLinks();
    },
    togglePause: function() {
        if (applet == null) return;
        applet.togglePause();
    },
    reproject: function() {
        if (applet == null) return;
        //applet.reproject();
    },
    switchTermProjects: function() {
        if (applet == null) return;
        // change the filter!
        //applet.showProjectGraph();
    },

    
    switchToLocalExploration: function() {
        applet.switchToLocalExploration();
    },
    
    switchToGlobalExploration: function() {
        applet.switchToGlobalExploration();
    },
    
    nodeClicked: function(x,y,id,label) {
        if (applet == null) return;
        
        
        // CALL ELISHOWK API HERE
        
        var context = {
         nodes: ['AAA','BBB','CCC'],
         edges: ['AAA','BBB']
        };

        // a basic GEXF template (the applet isn't very strict regarding to the GEXf version)
        var template = '<?xml version="1.0" encoding="UTF-8"?><gexf><graph>\n\
        <attributes class="node">\n\
        </attributes>\n\
        <nodes>\n\
        <?js for (var i = 0, n = items.length; i < n; i++) { ?>\n\
          <node id="#{i}" label="voisin">\n\
            <attvalues>\n\
              <attvalue for="0" value="0" />\n\
            </attvalues>\n\
          </node>\n\
        <?js } ?>\n\
        </nodes>\n\
        <edges>\n\
        <?js for (var i = 0, n = items.length; i < n; i++) { ?>\n\
          <edge id="#{i}" source="" target="" />\n\
        <?js } ?>\n\
        </edges>\n\
        </graph><gexf>';

        var output = Shotenjin.render(template, context);
 
        // send to the applet
        applet.updateViewFromString(output);
    
    },
    
    takePDFPicture: function () {
        var outputFilePath = "graph.pdf";
        var result;
        try {
            result = viz.takePDFPicture(outputFilePath);
        } catch (e) {
            //alert("Oops! Something bad just happened. Calling 911...");
            $('#debugdiv').append('<br><b>exception='+e+'</b>');
            console.log(e);
            return;
        }
        $('#debugdiv').append('<br>Saving to '+outputFilePath+'</p>');
        setTimeout("downloadFile('"+outputFilePath+"', 60)",2000);
    },

    takePicture: function() {
        var outputFilePath = "graph.png";
        var result;
        try {
            result = viz.takePicture(outputFilePath);
        } catch (e) {
            //alert("Oops! Something bad just happened. Calling 911...");
            $('#debugdiv').append('<br><b>exception='+e+'</b>');
             console.log(e);
             return;
        }
        $('#debugdiv').append('<br>Saving to '+outputFilePath+'</p>');
        setTimeout("downloadFile('"+outputFilePath+"', 60)",2000);
    },

    downloadFile: function(outputFilePath, timeout) {
        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var downloadPath;
        if (path.search(/\\/) != -1) { downloadPath = path + "\\..\\"+outputFilePath }
        else { downloadPath = path + "/../"+outputFilePath  }
        
        var downloadFile = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
                
        downloadFile.initWithPath(downloadPath);
        if (downloadFile.exists() && timeout < 0) {
            window.location.href = 
                Components.classes["@mozilla.org/network/protocol;1?name=file"]
                   .createInstance(Components.interfaces.nsIFileProtocolHandler)
                      .getURLSpecFromFile(downloadFile);
            return true;
       }
        if (downloadFile.exists() && timeout > 0) {
            setTimeout("downloadFile('"+outputFilePath+"', "+(-1)+")", 3000);
            return true;
        } else {
            // $('#debugdiv').append('<br>waiting..</p>');
            setTimeout("downloadFile('"+outputFilePath+"', "+(timeout -1)+")", 1500);
            return true;
        }
        return false;
    },

    loadGexf: function(filename) {

        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var gexfPath;

        // todo: find a better way to convert the path
        if (path.search(/\\/) != -1) { gexfPath = path + "\\content\\data\\"+filename }
        else { gexfPath = path + "/content/applet/data/"+filename  }

        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
                
        file.initWithPath(gexfPath);

        var fstream = 
            Components.classes["@mozilla.org/network/file-input-stream;1"]
                .createInstance(Components.interfaces.nsIFileInputStream);
        var cstream = 
            Components.classes["@mozilla.org/intl/converter-input-stream;1"]
                .createInstance(Components.interfaces.nsIConverterInputStream);

        fstream.init(file, -1, 0, 0);
        cstream.init(fstream, "UTF-8", 0, 0); // you can use another encoding here if you wish

        var str = {};
        cstream.readString(-1, str); // read the whole file and put it in str.value
        cstream.close(); // this closes fstream

        try {
            result = applet.updateViewFromString(str.value);
        } catch (e) {
            if(e.rhinoException != null) { console.log(applet.getStackTraceAsString(e.rhinoException)); } 
            else if(e.javaException != null) { console.log(applet.getStackTraceAsString(e.javaException)); } 
            console.log(e);
        }
    },

    updateView2: function(filename) {

        if (!filename) filename = "test2.gexf";

        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");

        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var gexfPath;
        if (path.search(/\\/) != -1) { gexfPath = path + "\\content\\data\\"+filename }
        else { gexfPath = path + "/content/applet/data/"+filename  }

        var gexfFile = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
                
        gexfFile.initWithPath(gexfPath);

        var uri = 
            Components.classes["@mozilla.org/network/protocol;1?name=file"]
                .createInstance(Components.interfaces.nsIFileProtocolHandler)
                    .getURLSpecFromFile(gexfFile);
                    
        try {
            result = applet.updateViewFromURI(uri);
        } catch (e) {
            if(e.rhinoException != null) { console.log(applet.getStackTraceAsString(e.rhinoException)); } 
            else if(e.javaException != null) { console.log(applet.getStackTraceAsString(e.javaException)); } 
            console.log(e);
        }
    },

    
    isEnabled: function() {
        if (applet == null) {
            return false;
        } else {
            return applet.isEnabled();
        }
    },
    setEnabled:  function(enabled) {
        if (applet == null) return;
        applet.setEnabled(enabled);
    }
  };
}

tinaviz = new Tinaviz();

$(document).ready(function() {

    // hide all the GUI until we loaded the applet
    $("#gui").hide();
    
    /*
    $("#searchfield").autocomplete({
        data: labels,
        extraSearch: function(term) {
            applet = $('#vizframe').contents().find('#tinaviz')[0];
            viz = applet.getLabels();
            viz.searchLabelDynamicFocus(term);
        }
    });
    */
    $("#altslider").slider({
            orientation: "vertical",
            range: true,
            values: [1, 100],
            slide: function(event, ui) {

                //$("#amount").val('$' + ui.values[0] + ' - $' + ui.values[1]); // ui.values[1]
                //viz.setGenericityRange(ui.values[0],ui.values[1], 100);
                tinaviz.setGenericityRange(ui.values[0],ui.values[1]);

                $("#upperThresholdInput").val(ui.values[1]/100);
                $("#lowerThresholdInput").val(ui.values[0]/100);
            }
    });


    //all hover and click logic for buttons
    $(".fg-button:not(.ui-state-disabled)")
        .hover(
            function(){
                    $(this).addClass("ui-state-hover");
            },
            function(){
                    $(this).removeClass("ui-state-hover");
            }
         )
         .mousedown(
            function(){
                    $(this)
                        .parents('.fg-buttonset-single:first')
                            .find(".fg-button.ui-state-active")
                                .removeClass("ui-state-active");
                    if( $(this).is('.ui-state-active.fg-button-toggleable, .fg-buttonset-multi .ui-state-active') ) {
                        $(this).removeClass("ui-state-active");
                    }
                    else {
                        $(this).addClass("ui-state-active"); 
                    }
            }
         )
         .mouseup(
            function(){
                if(! $(this).is('.fg-button-toggleable, .fg-buttonset-single .fg-button, .fg-buttonset-multi .fg-button') ){
                    $(this).removeClass("ui-state-active");
                }
            }
          );

        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var appletPath;
        if (path.search(/\\/) != -1) { appletPath = path + "\\content\\applet\\index.html" }
        else { appletPath = path + "/content/applet/index.html" }
        var appletFile = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
        appletFile.initWithPath(appletPath);
        var appletURL = Components.classes["@mozilla.org/network/protocol;1?name=file"].createInstance(Components.interfaces.nsIFileProtocolHandler).getURLSpecFromFile(appletFile);
        
        
        $('#container')
            .html('<iframe id="vizframe" class="vizframe" allowtransparency="true" scrolling="no"  frameborder="0" src="'+appletURL+'"></iframe>');
});

//function synchronize() {
    //applet = frame.contents().find('#tinaviz')[0];
    //viz = applet.getSubApplet();
    //labels = viz.getLabels();
    // updateAutocomplete();
    
        // setParameter("key", min, max)
        
        /*
    applet.createFilter("layout", "ForceVector");
    applet.filterConfig("layout", "attraction", 0.01);
    applet.filterConfig("layout", "repulsion", 0.0001);
    
    
    applet.createFilter("cat_filter", "AttributeFilter");
    applet.filterConfig("cat_filter", "attribute", "category");
    applet.filterConfig("cat_filter", "type", "regex");
    applet.filterConfig("cat_filter", "model", "ngrams");
    */
//}

function computeAppletWidth() {
    return parent.computeAppletWidth();
}
function computeAppletHeight() {
    return parent.computeAppletHeight();
}


function showNodeDetails(x,y,uuid,label) {
    $('#nodedetails').css("top",""+y+"px");
    $('#nodedetails').css("left",""+x+"px");
    $('#nodedetailsiframe').css("top",""+y+"px");
    $('#nodedetailsiframe').css("left",""+x+"px");

    $('#nodedetails').html('<div>'+label+'</div>');

    $('#nodedetailsiframe').show();
    $('#nodedetails').show();
}
function hideNodeDetails() {
    $('#nodedetailsiframe').hide();
    $('#nodedetails').hide();

      //  $('#nodedetails').append('<br>== returned '+result+'</p>');
       // $('#nodedetails').append('<br>== returned '+result+'</p>');
}

                /*
function loadNewViz(url) {

    // un seul import de composant xpcom
    var tina = Cc["Python.TinasoftAPI"].createInstance(Ci.nsITinasoftAPI);

    // key/value database ?
    //var key = "users/my_user_id/sessions/my_cool_session_id";
    //var key = "jbilcke_session124";
    var key = "my_session_unique_id";
    var session = tina.session(key);
    if (!session) {
       // TODO
       // "session not found, aborting..";
       // log the error
       return -1;
    }
    // retourne un objet js/json de la forme:

    // session.id = ID de la session
    // session.name = nom de session lisible (affiché dans la barre de titre par exemple)
    // session.owner = ID du propriétaire de la session
    // session.corpus = ID du corpus
    // session.bookmarks = ID de la collection de bookmarks
    // session.network = ID du graphe

    var user = tina.user(session.owner);
    if (!user) {
       // TODO
       // "session not found, aborting..";
       // log the error
       return -1;
    }

    // user.id = user id, unique
    // user.name = nom de l'utilisateur
    //

        // session.id = session id, unique
    // session.name = nom de session lisible (affiché dans la barre de titre par exemple)
    // session.owner = propriétaire de la session, sous forme de user_id
    // session.corpus
    var bookmarks = tina.bookmarks(session.bookmarks);
    if (!bookmarks) {

       // bookmarks est une liste de bookmark
       // TODO
       // "session not found, aborting..";
       // log the error
       return -1;
    }

    var bookmarks = tina.bookmarks("bookmarks");

    // user.id = user id, unique
    // user.name = nom de l'utilisateur
    //
}
*/


/*
function setParameters(config) {
    //var cls = Cc["Python.TinasoftDataRelational"];
    //var ob = cls.createInstance(Ci.nsITinasoftDataRelational);
    //ob.connect("Test-tdr.sqlite");
    document.getElementById("gslider").value = config.ui.gs * 100;

}

function genericitySliderMoved() {
  var tinaviz = document.getElementById("tinaviz");
  var gslider = document.getElementById("gslider");

}

function showLabels() {
  var tinaviz = document.getElementById("tinaviz").showLabels(document.getElementById("labels").value);

}
*/
