function Tinaviz() {
  var wrapper = null;
  var applet = null;
  var width = null;
  var height = null;
  var categoryFilter = "keepCategoryFilter";
  var categoryFilterSwitch = "including";
 

  return {
    init: function() {
        if (wrapper != null || applet != null) return;
        wrapper = $('#vizframe').contents().find('#tinaviz')[0];
        applet = wrapper.getSubApplet();
        //this.size(this.getWidth(),this.getHeight());
        
        this.logDebug("loading tinaapptests-exportGraph.gexf");
        
        this.setLevel("macro");
        this.loadRelativeGraph("macro","user/fet open/8_0.0-1.0.gexf");

        // disable the applet when on another tab (to save CPU)
        // WARNING WARNING WANRING WARNING
        // DISABLING THE APPLET IN OPENGL MODE IS STUPID BECAUSE THIS CAUSE A BIG INFINITE LOOP !
        this.setEnabled(false);
        
        // we can already prepare the control layout
        $('#gui').show();
        $('#sidebariframe').hide();

        // finally, once the gexf is loaded, we light the tab!
        this.logDebug("enabling applet tab..");
        parent.appletReady();

    },
    
    setup: function() {
        var corpus = parent.getCorpus("2"); 
        if (corpus == null) {
          this.logError("get corpus failed"); 
          return;
        }
       jQuery.each(corpus, function(i, val) {
          this.logNormal( "EACH CORPUS : "+ i );
       });

         // update the HTML form
        $('#nodedetailstitle').html("Document: "+"(none)");
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
             label: 'TINA Document',
             category: 'Document'
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
        this.logDebug(output);
    },
    
    size: function(w,h) {
        if (applet == null) return;
        wrapper.width = w;
        wrapper.height = h;
        // update the overlay layout (eg. recenter the toolbars)
        $('.htoolbar').css('left', (  (wrapper.width - parseInt($('#htoolbariframe').css('width'))) / 2   ));
    },
    
    // Public methods
    loadFromURI: function(uri) {
        if (applet == null) return;
        applet.getSession().updateFromURI(view,uri);
    },
    loadFromString: function(view,gexf) {
        if (applet == null) return;
        applet.getSession().updateFromString(view,gexf);
    },
    
    setGenericityRange: function(from,to) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf);
    },
    
    toggleLabels: function(view) {
            if (applet != null && applet.getView(view).toggleLabels()) {
                 parent.$('.toggle-labels .'+view).addClass("ui-state-active"); 
            } else {
		         parent.$('.toggle-labels .'+view).removeClass("ui-state-active"); 
	        }
    },

    toggleNodes: function(view) {
            if (applet != null && applet.getView(view).toggleNodes()) {
                 parent.$('.toggle-nodes .'+view).addClass("ui-state-active"); 
            } else {
		         parent.$('.toggle-nodes .'+view).removeClass("ui-state-active"); 
	        }
    },
    toggleEdges: function(view) {
            if (applet != null && applet.getView(view).toggleEdges()) {
                 parent.$('.toggle-edges .'+view).addClass("ui-state-active"); 
            } else {
		         parent.$('.toggle-edges .'+view).removeClass("ui-state-active"); 
	        }
    },

    togglePause: function(view) {
            if (applet != null && applet.getView(view).togglePause()) {
                 parent.$('.toggle-pause .'+view).addClass("ui-state-active"); 
            } else {
		         parent.$('.toggle-pause .'+view).removeClass("ui-state-active"); 
	        }
    },
    
    toggleDocuments: function() {
        if (applet == null) return;
        // toggle the filter
        
        // switch the
        applet.filterConfig(categoryFilter, categoryFilterSwitch, 
           ! applet.filterConfig(categoryFilter, categoryFilterSwitch));
  
    },

    toggleTerms: function() {
        if (applet == null) return;
        applet.filterConfig(categoryFilter, "mask", "NGram");
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
            this.logError("exception: "+e);
        
        }
    },

    nodeSelected: function(level,x,y,id,label,attr) {
        if (applet == null) return;

        this.logDebug("nodeSelected called! attributes: '"+attr+"'");

        if (id == null) { $('#sidebariframe').hide(); } 
        else            { $('#sidebariframe').show(); }

        if (level == "macro") {
		    if (attr=="Document") {
			    this.selectMacroDocument(x,y,id,label);
		    } else if (attr=="NGram") {
			    this.selectMacroTerm(x,y,id,label);
		    }
        } else if (level == "meso") {
		    if (attr=="Document") {
			    this.selectMesoDocument(x,y,id,label);
		    } else if (attr=="NGram") {
			    this.selectMesoTerm(x,y,id,label);
		    }
	    } else if (level == "micro") {
		    if (attr=="Document") {
			    this.selectMicroDocument(x,y,id,label);
		    } else if (attr=="NGram") {
			    this.selectMicroTerm(x,y,id,label);
		    }
	    }
    },

    takePDFPicture: function () {
        var outputFilePath = "graph.pdf";
        var result;
        try {
            result = viz.takePDFPicture(outputFilePath);
        } catch (e) {
            this.logError(e);
            return;
        }
        this.logDebug('Saving to '+outputFilePath+'</p>');
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
        this.logDebug('Saving to '+outputFilePath+'</p>');
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

    loadDataGraph: function(view,filename) {
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
        this.logDebug("loading data/graph/ graph: "+gexfPath);
        result = this.loadFromURI(uri);
    },
     // using string technique
    loadRelativeGraph: function(view,filename) {
    
        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("CurProcD", Components.interfaces.nsIFile).path;
        var gexfPath;// = path + filename;
        if (path.search(/\\/) != -1) { gexfPath = path + "\\"+filename }
        else { gexfPath = path + "/"+filename  }

        this.logDebug("going to load "+filename);
        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        this.logDebug("initWithPath: "+gexfPath);      
        file.initWithPath(gexfPath);

        var fstream = 
            Components.classes["@mozilla.org/network/file-input-stream;1"]
                .createInstance(Components.interfaces.nsIFileInputStream);
        var cstream = 
            Components.classes["@mozilla.org/intl/converter-input-stream;1"]
                .createInstance(Components.interfaces.nsIConverterInputStream);

        fstream.init(file, -1, 0, 0);
        // MAX filesize: 8 MB
        cstream.init(fstream, "UTF-8", 10000000, 0); // you can use another encoding here if you wish

        var str = {};
        cstream.readString(-1, str); // read the whole file and put it in str.value
        cstream.close(); // this closes fstream
        this.logDebug("calling this.loadFromString(..) with a big file!");      
        result = this.loadFromString(view,str.value);
    },
     // using string technique
    loadAbsoluteGraph: function(view,filename) {
    
        var gexfPath;

        this.logDebug("going to load "+filename);
        var file = 
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        this.logDebug("initWithPath: "+filename);      
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
        this.logDebug("calling this.loadFromString(..):"+str.value);      
        result = this.loadFromString(view,str.value);
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
        this.logDebug("loading absolute graph: "+uri);
        result = this.loadFromURI(uri);
    },
    
    readGraph: function(view,graphURL) {
        if (applet == null) return;
        $.ajax({
		    url: graphURL,
		    type: "GET",
	        dataType: "text",
	        success: function(gexf) { 
	           applet.getSession().updateFromString(view,gexf);
	       } 
        });
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
    },
        
    setLevel: function(level) {
        if (applet == null) return;
        applet.getSession().setLevel(level);
    },

    
    logError: function(msg) {
        console.error(msg);
    },
    logNormal: function(msg) {
       console.log(msg);
    },
    logDebug: function(msg) {
       console.log("DEBUG "+msg);
    },
    switchedTo: function(level) {
        // here it is a bit tricky, we need to change the tabs, which are a bit far in the div/iframe tree...
        parent.switchedTo(level);
    },

    getWidth: function() {
        return parent.getAppletWidth();
    },
    getHeight: function() {
       return parent.getAppletHeight();
    },
    
    search: function(txt) {
        this.logNormal("Searching is not implemented yet..");
    },
    
     
  selectMacroDocument: function(x,y,id,label) {
     if (applet == null) return;
     this.logDebug("selectMacroDocument called!");

 
    applet.getSession().getMeso().selectNodeById(id);
        // TODO pass the ID to the elishowk API
        
        // TODO update the DIV with data from the database
       this.logDebug("selectMacroDocument called for ID "+id); 
       var doc = parent.getDocument( id );     
       this.logDebug("doc= "+doc); 

         // update the HTML form
      if (doc != null) {
        $('#nodedetailstitle').html("Project: "+ label);
        $('#abstract').html("Document abstract");
      }

  },

  selectMacroTerm: function(x,y,id,label) {
     if (applet == null) return;
     this.logDebug("selectMacroTerm called!");
     applet.getSession().getMeso().selectNodeById(id);
        // TODO pass the ID to the elishowk API
        
        // TODO update the DIV with data from the database
       this.logDebug("selectMacroTerm called for ID "+id);  
       var ng = parent.getNGram( id );     
        this.logDebug("ng= "+ng);
         /*
        for (var key in ng) {
            this.logDebug( "key: "+ key +" value: " +ng[key]);
        }*/
       

         // update the HTML form
       if (ng == null) return;
       
       delete ng['py/object'];
       ng["edges_data"] = {
          "Document" : {},
          "Corpus" : {}
       };

       $('#nodedetailstitle').html("Term: "+ng['label']);
       $('#abstract').html("");
     
       for (var doc in ng["edges"]["Document"]) { 
           var obj = parent.getDocument(doc);
           if (obj == null) {
             delete ng["edges"]["Document"][doc];
           } else {
             ng["edges_data"]["Document"][doc] = obj;
           }
       }
       
       for (var corp in ng["edges"]["Corpus"]) { 
           var obj = parent.getCorpus(corp);
           if (obj == null) {
             delete ng["edges"]["Corpus"][corp];
           } else {
             ng["edges_data"]["Corpus"][corp] = obj;
           }
       }
     
        var gexf = Shotenjin.render("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n\
        <meta lastmodifieddate=\"19-Feb-2010\"><description>Generic Map/2002-2007</description></meta>\n\
    <graph>\n\
        <attributes class=\"node\">\n\
        </attributes>\n\
        <tina>\n\
        </tina>\n\
        <nodes>\n\
            <node id=\"#{id}\" label=\"#{label}\">\n\
                <viz:size value=\"20\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"NGram\" />\n\
                </attvalues>\n\
            </node>\n\
<?js for (var target_type in edges) { ?>\
<?js     for (var target_node in edges[target_type]) { ?>\
            <node id=\"#{target_node}\" label=\"#{edges_data[target_type][target_node]['label']}\">\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"#{target_type}\" />\n\
                </attvalues>\n\
            </node>\n\
<?js    } ?>\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js var i=0; ?>\
<?js for (var target_type in edges) { ?>\
<?js     for (var target_node in edges[target_type]) { ?>\
            <edge id=\"#{i++}\" source=\"#{id}\" target=\"#{target_node}\" weight=\"#{edges[target_type][target_node]}\" />\n\
<?js    } ?>\
<?js } ?>\
        </edges>\n\
    </graph>\n\
</gexf>", ng);
        // console.log(gexf);
        applet.getSession().updateFromString("meso",gexf);
  },


  selectMesoTerm: function(x,y,id,label) {
     if (applet == null) return;
     this.logDebug("selectMesoTerm called!");
     applet.getSession().getMacro().selectNodeById(id);
  },

  selectMesoDocument: function(x,y,id,label) {
     if (applet == null) return;
       this.logDebug("selectMesoDocument called!"); 
       this.logDebug("ID= "+id+" "); 
       this.logDebug(" applet.getSession().getMacro().selectNodeById(id) "); 
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

       this.logDebug("parent.getDocument( "+id+" )"); 
       var doc = parent.getDocument( id );     


         // update the HTML form
        $('#nodedetailstitle').html("Document: "+"(none)");
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
             label: 'TINA Document',
             category: 'Document'
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
        //result = applet.getSession().getMeso().getGraph().updateFromString(output);
        if (!result) console.error(e);
        
        // also ask to 
        // TODO update the DIV with data from the database
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



