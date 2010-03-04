function Tinaviz() {
  var wrapper = null;
  var applet = null;
  var width = null;
  var height = null;
  var categoryFilter = "keepCategoryFilter";
  var categoryFilterSwitch = "including";
  
  var selectMacroProject = function(x,y,id,label) {
     if (applet == null) return;
     console.log("selectMacroProject called!");

 
    applet.getSession().getMeso().selectNodeById(id);
        // TODO pass the ID to the elishowk API
        
        // TODO update the DIV with data from the database
       console.log("selectMacroProject called for ID "+id); 
       var doc = parent.getDocument( id );     
       console.log("doc= "+doc); 

         // update the HTML form
      if (doc != null) {
        $('#nodedetailstitle').html("Project: "+ label);
        $('#abstract').html("Document abstract");
      }

  };

  var selectMacroTerm = function(x,y,id,label) {
     if (applet == null) return;
     console.log("selectMacrolTerm called!");
     applet.getSession().getMeso().selectNodeById(id);
        // TODO pass the ID to the elishowk API
        
        // TODO update the DIV with data from the database
       console.log("selectMacroTerm called for ID "+id);  
       var doc = parent.getDocument( id );     
       console.log("doc= "+doc); 

         // update the HTML form
       if (doc != null) {
        $('#nodedetailstitle').html("Term: "+label);
        $('#abstract').html("");
       }
 
  };


  var selectMesoTerm = function(x,y,id,label) {
     if (applet == null) return;
     console.log("selectMesoTerm called!");
     applet.getSession().getMacro().selectNodeById(id);
  };

  var selectMesoProject = function(x,y,id,label) {
     if (applet == null) return;
       console.log("selectMesoProject called!"); 
       console.log("ID= "+id+" "); 
       console.log(" applet.getSession().getMacro().selectNodeById(id) "); 
     // applet.getSession().getMacro().selectNodeById(id);
	/*
	var getCorpus = function(corpusid) {
	    return( JSON.parse( TinaService.getCorpus(corpusid) ) );
	};
	var getDocument = function(documentid) {
	    return( JSON.parse( TinaService.getDocument(documentid) ) );
	};
	var getCorpora = function(corporaid) {
	    return( JSON.parse( TinaService.getCorpora(corporaid) ) );
	};
	var getNGram = function(ngramid) {
	    return( JSON.parse( TinaService.getNGram(ngramid) ) );
	};
	   */

       console.log("parent.getDocument( "+id+" )"); 
       var doc = parent.getDocument( id );     


         // update the HTML form
        $('#nodedetailstitle').html("Project: "+"(none)");
        $('#abstract').html("Document abstract");


        // TODO pass the ID to the elishowk API
        var context = {
         root:  {
            uuid: id,
         },
         neighborhood: [
            {
             uuid: '432561326751248',
             label: 'this is an ngram',
             category: 'term'
             },
            {
             uuid: '715643267560489',
             label: 'TINA PROJECT',
             category: 'project'
             },
         ]
        };

        // a basic GEXF template (the applet isn't very strict regarding to the GEXf version)
        var template = '<?xml version="1.0" encoding="UTF-8"?>\n\
<gexf xmlns="http://www.gephi.org/gexf" xmlns:viz="http://www.gephi.org/gexf/viz">\n\
        <meta lastmodifieddate="19-Feb-2010"><description>Generic Map/2002-2007</description></meta>\n\
    <graph>\n\
        <attributes class="node">\n\
        </attributes>\n\
        <tina>\n\
        </tina>\n\
        <nodes>\n\
<?js for (var i = 0, n = neighborhood.length; i < n; i++) { ?>\
            <node id="#{neighborhood[i].uuid}" label="#{neighborhood[i].label}">\n\
                <attvalues>\n\
                    <attvalue for="0" value="#{neighborhood[i].category}" />\n\
                </attvalues>\n\
            </node>\n\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js for (var i = 0, n = neighborhood.length; i < n; i++) { ?>\
            <edge id="#{i}" source="#{root.uuid}" target="#{neighborhood[i].uuid}" weight="1.0" />\n\
<?js } ?>\
        </edges>\n\
    </graph>\n\
</gexf>';
        
  
        /* call the template engine (tenjin is really fast!)*/
        var output = Shotenjin.render(template, context);
        
        console.log(output);
       
        console.log("calling applet.getSession().getMeso().getgraph().updateFromString(output)");
        result = applet.getSession().getMeso().getGraph().updateFromString(output);
        if (!result) console.error(e);
        
        // also ask to 
        // TODO update the DIV with data from the database
  };

  return {
    init: function() {
        if (wrapper != null || applet != null) return;
        wrapper = $('#vizframe').contents().find('#tinaviz')[0];
        applet = wrapper.getSubApplet();
        
        this.toMacro();
        //this.loadGraph("examples/map_dopamine_2002_2007_g.gexf");
        
        //this.setup();  
   
        // disable the applet when on another tab (to save CPU)
        this.setEnabled(false);
        
        // we can already prepare the control layout
        $('#gui').show();
        $('#sidebariframe').hide();

        // finally, once the gexf is loaded, we light the tab!
        console.log("enabling applet tab..");
        parent.appletReady();

    },
    
    setup: function() {
        var corpus = parent.getCorpus("2"); 
        if (corpus == null) {
          console.log("get corpus failed"); 
          return;
        }
       jQuery.each(corpus, function(i, val) {
          console.log( i );
       });

         // update the HTML form
        $('#nodedetailstitle').html("Project: "+"(none)");
        $('#abstract').html("Document abstract");

        // TODO pass the ID to the elishowk API
        var context = {
         root:  {
            uuid: id,
         },
         neighborhood: [
            {
             uuid: '432561326751248',
             label: 'this is an ngram',
             category: 'term'
             },
            {
             uuid: '715643267560489',
             label: 'TINA PROJECT',
             category: 'project'
             },
         ]
        };


        var template = '<?xml version="1.0" encoding="UTF-8"?>\n\
<gexf xmlns="http://www.gephi.org/gexf" xmlns:viz="http://www.gephi.org/gexf/viz">\n\
        <meta lastmodifieddate="19-Feb-2010"><description>Generic Map/2002-2007</description></meta>\n\
    <graph>\n\
        <attributes class="node">\n\
        </attributes>\n\
        <tina>\n\
        </tina>\n\
        <nodes>\n\
<?js for (var i = 0, n = neighborhood.length; i < n; i++) { ?>\
            <node id="#{neighborhood[i].uuid}" label="#{neighborhood[i].label}">\n\
                <attvalues>\n\
                    <attvalue for="0" value="#{neighborhood[i].category}" />\n\
                </attvalues>\n\
            </node>\n\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js for (var i = 0, n = neighborhood.length; i < n; i++) { ?>\
            <edge id="#{i}" source="#{root.uuid}" target="#{neighborhood[i].uuid}" weight="1.0" />\n\
<?js } ?>\
        </edges>\n\
    </graph>\n\
</gexf>';


        /* call the template engine (tenjin is really fast!)*/
        var output = Shotenjin.render(template, context);
        
        console.log(output);

    },
    
    resized: function() {
        if (applet == null) return;
        wrapper.width = computeAppletWidth();
        wrapper.height = computeAppletHeight();
        // update the overlay layout (eg. recenter the toolbars)
        $('.htoolbar').css('left', (  (wrapper.width - parseInt($('#htoolbariframe').css('width'))) / 2   ));
    },
    
    // Public methods
    loadFromURI: function(uri) {
        if (applet == null) return;
        applet.getSession().updateFromURI(uri);
    },
    loadFromString: function(gexf) {
        if (applet == null) return;
        applet.getSession().updateFromString(gexf);
    },
    
    setGenericityRange: function(from,to) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf);
    },
    toggleLabels: function() {
        if (applet == null) return;
        applet.getView().toggleLabels(); 
    },
    toggleNodes: function() {
        if (applet == null) return;
        applet.getView().toggleNodes();
    },
    toggleEdges: function() {
        if (applet == null) return;
        applet.getView().toggleLinks();
    },
    togglePause: function() {
        if (applet == null) return;
        applet.getView().togglePause();
    },
    
    toggleProjects: function() {
        if (applet == null) return;
        // toggle the filter
        
        // switch the
        applet.filterConfig(categoryFilter, categoryFilterSwitch, 
           ! applet.filterConfig(categoryFilter, categoryFilterSwitch));
  
    },
    reproject: function() {
        if (applet == null) return;
        //applet.reproject();
    },
    toggleTerms: function() {
        if (applet == null) return;
        applet.filterConfig(categoryFilter, "mask", "NGram");
    },
    

    toMacro: function() {
        if (applet == null) return;
        applet.getSession().toMacroLevel();
    },
    
    toMeso: function() {
        if (applet == null) return;
        applet.getSession().toMesoLevel();
    },
    
    toMicro: function() {
        if (applet == null) return;
        applet.getSession().toMicroLevel();
    },
    
    
    unselect: function() {
        if (applet == null) return;
        applet.unselect();
    },
    clear: function() {
        if (applet == null) return;
        try {
            applet.getSession().clear();
        } catch (e) {
            console.log("exception: "+e);
        
        }
    },


    nodeSelected: function(level,x,y,id,label,attr) {
        if (applet == null) return;

        console.log("nodeSelected called! attributes: '"+attr+"'");

        if (id == null) { $('#sidebariframe').hide(); } 
        else            { $('#sidebariframe').show(); }

        if (level == "macro") {
		if (tags=="project") {
			selectMacroProject(x,y,id,label);
		} else if (tags=="NGram") {
			selectMacroTerm(x,y,id,label);
		}
        } else if (level == "meso") {
		if (tags=="project") {
			selectMesoProject(x,y,id,label);
		} else if (tags=="NGram") {
			selectMesoTerm(x,y,id,label);
		}
	}
    },

    takePDFPicture: function () {
        var outputFilePath = "graph.pdf";
        var result;
        try {
            result = viz.takePDFPicture(outputFilePath);
        } catch (e) {
            console.log(e);
            return;
        }
        console.log('Saving to '+outputFilePath+'</p>');
        setTimeout("downloadFile('"+outputFilePath+"', 60)",2000);
    },

    takePicture: function() {
        var outputFilePath = "graph.png";
        var result;
        try {
            result = viz.takePicture(outputFilePath);
        } catch (e) {
             console.log(e);
             return;
        }
        console.log('Saving to '+outputFilePath+'</p>');
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

    // BROKEN, REPLACE BY HTML5 FILE API //
    /*
    loadGexfWithStringMethod: function(filename) {

        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var gexfPath;

        // todo: find a better way to convert the path
        if (path.search(/\\/) != -1) { gexfPath = path + "\\content\\applet\\data\\"+filename }
        else { gexfPath = path + "/content/applet/data/"+filename  }
        console.log("going to load "+gexfPath);
        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        console.log("initWithPath ");      
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
        console.log("calling this.loadFromString(..)");      
        result = this.loadFromString(str.value);

    },
    */

    loadGraph: function(filename) {
        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var gexfPath;
        if (path.search(/\\/) != -1) { gexfPath = path + "\\data\\graph\\"+filename }
        else { gexfPath = path + "/data/graph/"+filename  }
        
        var gexfFile = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);    
        gexfFile.initWithPath(gexfPath);
        var uri = 
            Components.classes["@mozilla.org/network/protocol;1?name=file"]
                .createInstance(Components.interfaces.nsIFileProtocolHandler)
                    .getURLSpecFromFile(gexfFile);   
        console.log("loading data/graph/ graph: "+gexfPath);
        result = this.loadFromURI(uri);
    },
     // using string technique
    loadRelativeGraph: function(filename) {
    
        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
        var gexfPath;
        if (path.search(/\\/) != -1) { gexfPath = path + "\\data\\graph\\"+filename }
        else { gexfPath = path + "/data/graph/"+filename  }

        console.log("going to load "+filename);
        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        console.log("initWithPath: "+gexfPath);      
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
        console.log("calling this.loadFromString(..):"+str.value);      
        result = this.loadFromString(str.value);
    },
     // using string technique
    loadAbsoluteGraph: function(filename) {
    
        var gexfPath;

        console.log("going to load "+filename);
        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        console.log("initWithPath: "+filename);      
        file.initWithPath(filename);

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
        console.log("calling this.loadFromString(..):"+str.value);      
        result = this.loadFromString(str.value);
    },

    loadAbsoluteGraphFromURI: function(filename) {
        var gexfFile = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);    
        gexfFile.initWithPath(filename);
        var uri = 
            Components.classes["@mozilla.org/network/protocol;1?name=file"]
                .createInstance(Components.interfaces.nsIFileProtocolHandler)
                    .getURLSpecFromFile(gexfFile);   
        console.log("loading absolute graph: "+uri);
        result = this.loadFromURI(uri);
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
            animate: true,
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
