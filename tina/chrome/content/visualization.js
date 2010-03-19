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

        this.setLevel("macro");
        
        //this.setProperty("macro", "layout/repulsion", 0.5);
        
        // initial edge weight threshold
	    this.dispatchProperty("weight/min", 0.0);
	    this.dispatchProperty("weight/max", 1.0);
	    
	    // we want to keep documents
	    this.dispatchProperty("category/value", "Document");
	    this.dispatchProperty("category/mode", "keep");
	        
		this.dispatchProperty("radius/value", 1.0);
		
	    // create a new "Category()" filter instance, which use the "category" namespace, and attach it to the "macro" new
	    // and YES, you can define filters or properties at any time, it's totally asynchronous ;)
		this.bindFilter("Category","category", "macro");
		this.bindFilter("ThresholdWeight","weight", "macro");
		this.bindFilter("NodeRadius","radius", "macro")

		this.bindFilter("Category","category", "meso");
		this.bindFilter("ThresholdWeight","weight", "meso");
		this.bindFilter("NodeRadius","radius", "meso")

        //this.loadRelativeGraph("macro","user/fet open/8_0.0-1.0.gexf");

        // disable the applet when on another tab (to save CPU)
        // WARNING, DISABLING THE APPLET IN OPENGL CAUSE AN INFINITE LOOP
        this.disable();

        // finally, once the gexf is loaded, we light the tab!
        this.logDebug("enabling applet tab..");

        $("#tabs").data('disabled.tabs', []);
    },

    size: function(w,h) {
        if (applet == null) return;
        wrapper.width = w;
        wrapper.height = h;
        // update the overlay layout (eg. recenter the toolbars)
        //$('.htoolbar').css('left', (  (wrapper.width - parseInt($('#htoolbariframe').css('width'))) / 2   ));
    },

    // Public methods
    loadFromURI: function(uri) {
        if (applet == null) return false;
        return applet.getSession().updateFromURI(view,uri);
    },
    loadFromString: function(view,gexf) {
        if (applet == null) return false;
        return applet.getSession().updateFromString(view,gexf);
    },

    setGenericityRange: function(from,to) {
        if (applet == null) return;
        //applet.updateViewFromString(gexf);
    },

    toggleLabels: function(view) {
            if (applet != null && applet.getView(view).toggleLabels()) {
                 $('.toggle-labels .'+view).addClass("ui-state-active");
            } else {
                 $('.toggle-labels .'+view).removeClass("ui-state-active");
            }
    },

    toggleNodes: function(view) {
            if (applet != null && applet.getView(view).toggleNodes()) {
                 $('.toggle-nodes .'+view).addClass("ui-state-active");
            } else {
                 $('.toggle-nodes .'+view).removeClass("ui-state-active");
            }
    },
    toggleEdges: function(view) {
            if (applet != null && applet.getView(view).toggleEdges()) {
                 $('.toggle-edges .'+view).addClass("ui-state-active");
            } else {
                 $('.toggle-edges .'+view).removeClass("ui-state-active");
            }
    },

    togglePause: function(view) {
            if (applet != null && applet.getView(view).togglePause()) {
                 $('.toggle-pause .'+view).addClass("ui-state-active");
            } else {
                 $('.toggle-pause .'+view).removeClass("ui-state-active");
            }
    },

    unselect: function() {
        if (applet == null) return;
        applet.unselect();
        applet.clear("meso");
        applet.resetCamera("meso");
    },
    
    clear: function() {
        if (applet == null) return;
        try {
            applet.getSession().clear();
        } catch (e) {
            this.logError("exception: "+e);

        }
    },
    
    nodeRightClicked: function(level,x,y,id,label,attr) {
        if (applet == null) return;
        this.logDebug("nodeRightClicked called! attributes: '"+attr+"'");

        /*if (id == null) { $('#sidebariframe').hide(); }
        else            { $('#sidebariframe').show(); }*/

        if (level == "macro") {
            if (attr=="Document") {
                this.selectMacroDocument2(x,y,id,label);
            } else if (attr=="NGram") {
                this.selectMacroTerm2(x,y,id,label);
            }
        } else if (level == "meso") {
            if (attr=="Document") {
                this.selectMesoDocument2(x,y,id,label);
            } else if (attr=="NGram") {
                this.selectMesoTerm2(x,y,id,label);
            }
        } else if (level == "micro") {
            if (attr=="Document") {
                this.selectMicroDocument2(x,y,id,label);
            } else if (attr=="NGram") {
                this.selectMicroTerm2(x,y,id,label);
            }
        }
    },
    nodeLeftClicked: function(level,x,y,id,label,attr) {
        if (applet == null) return;
        if (id==null) {
            $('#infodiv').html("<h2>No element selected. <br/>To begin exploration, try clicking or double-clicking on nodes on the left!</h2>");
            applet.resetCamera("meso");
            applet.clear("meso");
            return;
        }

        this.logDebug("nodeLeftClicked called! attributes: '"+attr+"'");

        /*if (id == null) { $('#sidebariframe').hide(); }
        else            { $('#sidebariframe').show(); }*/

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
        return this.loadFromString(view,str.value);
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

    enabled: function() {
        if (applet == null) return false;
        return applet.isEnabled();
    },
    disabled: function() {
        if (applet == null) return true;
        return !applet.isEnabled();
    },
    enable:  function() {
        if (applet == null) return;
        applet.setEnabled(true);
    },
    disable:  function() {
        if (applet == null) return;
        applet.setEnabled(false);
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
        switchedTo(level);
    },

    getWidth: function() {
        return getAppletWidth();
    },
    
    getHeight: function() {
       return getAppletHeight();
    },
    
    
    bindFilter: function(level, name, path, level) {
        if (applet == null) return;
        if (level == null) return applet.getSession().addFilter(name, path);
        return applet.getView(level).addFilter(name, path);
    },
    
      
    dispatchProperty: function(key,value) {
        if (applet == null) return;
        return applet.getSession().setProperty(key,value);
    },
    
    setProperty: function(level,key,value) {
        if (applet == null) return;
        return applet.getView(level).setProperty(key,value);
    },
    
    touch: function(level) {
        applet.getView(level).getGraph().touch();
    },
    
    search: function(txt) {
        this.logNormal("Searching is not implemented yet..");
    },


    getNGram: function(id) {
       var ng = getNGram( id );
       this.logDebug("ng= "+ng);
       if (ng == null) return null;
       delete ng['py/object'];
       ng["edges_data"] = { "Document" : {}, "Corpus" : {} };
       for (var doc in ng["edges"]["Document"]) {
           var obj = getDocument(doc);
           if (obj == null) { delete ng["edges"]["Document"][doc];} 
           else {  ng["edges_data"]["Document"][doc] = obj;}
       }
       /*
       for (var corp in ng["edges"]["Corpus"]) {
           var obj = getCorpus(corp);
           if (obj == null) { delete ng["edges"]["Corpus"][corp];}
           else { ng["edges_data"]["Corpus"][corp] = obj;}
       }*/
       return ng;
  },
   getNGram2: function(id) {
       var ng = getNGram( id );
       this.logDebug("ng= "+ng);
       if (ng == null) return null;
       delete ng['py/object'];
       ng["edges_data"] = { "NGram" : {}, "Document" : {}, "Corpus" : {} };
       for (var doc in ng["edges"]["Document"]) {
           var obj = getDocument(doc);
           if (obj == null) { delete ng["edges"]["Document"][doc];} 
           else { 
             ng["edges_data"]["Document"][doc] = obj; 
             for (var ng2 in obj["edges"]["NGram"]) { 
                 var obj2 = getNGram(ng2);
                 if (obj2 == null) { delete obj["edges"]["NGram"]; }
                 ng["edges_data"]['NGram'][ng2] = obj2;
             }
           }
       }
       /*
       for (var corp in ng["edges"]["Corpus"]) {
           var obj = getCorpus(corp);
           if (obj == null) { delete ng["edges"]["Corpus"][corp];}
           else { ng["edges_data"]["Corpus"][corp] = obj;}
       }
       */
       return ng;
  },
  
  getDocument: function(id) {
       var doc = getDocument( id );
       this.logDebug("doc= "+doc);
       if (doc == null) return null;
       delete doc['py/object'];
       doc["edges_data"] = { "NGram" : {}, "Corpus" : {} };
       for (var ng in doc["edges"]["NGram"]) {
           var obj = getNGram(ng);
           if (obj == null) { delete doc["edges"]["NGram"][ng];} 
           else {  doc["edges_data"]["NGram"][ng] = obj;}
       }
       /*
       for (var corp in doc["edges"]["Corpus"]) {
           var obj = getCorpus(corp);
           if (obj == null) { delete doc["edges"]["Corpus"][corp];}
           else { doc["edges_data"]["Corpus"][corp] = obj;}
       }*/
       return doc;
  },
  getDocument2: function(id) {
       var doc = getDocument( id );
       this.logDebug("doc= "+doc);
       if (doc == null) return null;
       delete doc['py/object'];
       doc["edges_data"] = {  "Document" : {}, "NGram" : {}, "Corpus" : {} };
       for (var ng in doc["edges"]["NGram"]) {
           var obj = getNGram(ng);
           if (obj == null) { delete doc["edges"]["NGram"][ng];} 
           else { 
             doc["edges_data"]["NGram"][ng] = obj; 
             for (var doc2 in obj["edges"]["Document"]) { 
                 var obj2 = getDocument(doc2);
                 if (obj2 == null) { delete obj["edges"]["Document"]; }
                 doc["edges_data"]['Document'][doc2] = obj2;
             }
           }
       }/*
       for (var corp in doc["edges"]["Corpus"]) {
           var obj = getCorpus(corp);
           if (obj == null) { delete doc["edges"]["Corpus"][corp];}
           else { doc["edges_data"]["Corpus"][corp] = obj;}
       }*/
       return doc;
  },
  
  selectMacroTerm: function(x,y,id,label,attr) {
     this.logDebug("selectMacroTerm("+id+","+label+")");
     var ng = this.getNGram(id);
     if (ng==null) return;
     
     $('#infodiv').html(Shotenjin.render("\n\
     <h1 class=\"nodedetailsh1\">Field \"${label}\"</h1>\n\
     <h3 class=\"nodedetailsh3\">Linked to these projects:</h3>\n\
     <p class=\"nodedetailsp\">\n\
     <?js var first; for (var doc in edges['Document']) { first=doc; break; } ?>\
         <a href=\"\" class=\"ui-widget-content ui-state-default\">#{edges_data['Document'][first]['label']}</a> \
     <?js for (var doc in edges['Document']) { if (doc == first) continue; ?>\
       <br/><a href=\"javascript:tinaviz.selectMacroDocument('#{doc}')\" class=\"ui-widget-content ui-state-default\">#{edges_data['Document'][doc]['label']}</a>\
     <?js } ?>.\
     </p>\n", ng));

    this.showMesoTerm(ng,x,y,id,label,attr);
  },
  selectMacroTerm2: function(x,y,id,label,attr) {
     this.logDebug("selectMacroTerm2("+id+","+label+")");
     this.showMesoTerm2(this.getNGram2(id),x,y,id,label,attr);
   },
  selectMacroDocument: function(x,y,id,label,attr) {
     if (applet == null) return;
     this.logDebug("selectMacroDocument("+id+","+label+")");
     var doc = this.getDocument(id);
     if (doc==null) return;
     
     $('#infodiv').html(Shotenjin.render("\n\
     <h1 class=\"nodedetailsh1\">Project ${label}</h1>\n\
     <h3 class=\"nodedetailsh3\">Contains these terms:</h3>\n\
     <p class=\"nodedetailsp\">\n\
     <?js var first; for (var ng in edges['NGram']) { first=ng; break; } ?>\
         <a href=\"\" class=\"ui-widget-content ui-state-default\">${edges_data['NGram'][ng]['label']}</a> \
     <?js for (var ng in edges['NGram']) { if (ng == first) continue; ?>\
      <br/><a href=\"javascript:tinaviz.selectMacroTerm('#{ng}')\" class=\"ui-widget-content ui-state-default\">${edges_data['NGram'][ng]['label']}</a>\
     <?js } ?>.\
     </p>\n", doc));

          this.showMesoDocument(doc, x,y,id,label,attr);
  },
  selectMacroDocument2: function(x,y,id,label,attr) {
     if (applet == null) return;
     this.logDebug("selectMacroDocument2("+id+","+label+")");
     this.showMesoDocument2(this.getDocument2(id), x,y,id,label,attr);
  },
  selectMesoTerm: function(x,y,id,label,attr) {
     this.logDebug("selectMesoTerm("+id+","+label+")");
     var ng = this.getNGram(id);
     if (ng==null) return;
     
     $('#infodiv').html(Shotenjin.render("\n\
     <h1 class=\"nodedetailsh1\">Field \"${label}\"</h1>\n\
     <h3 class=\"nodedetailsh3\">Linked to these projects:</h3>\n\
     <p class=\"nodedetailsp\">\n\
     <?js var first; for (var doc in edges['Document']) { first=doc; break; } ?>\
         <a href=\"\" class=\"ui-widget-content ui-state-default\">${edges_data['Document'][first]['label']}</a> \
     <?js for (var doc in edges['Document']) { if (doc == first) continue; ?>\
     <br/><a href=\"javascript:tinaviz.selectMesoDocument('#{doc}')\" class=\"ui-widget-content ui-state-default\">${edges_data['Document'][doc]['label']}</a>\
     <?js } ?>.\
     </p>\n", ng));
     
    this.showMesoTerm(ng,x,y,id,label,attr);
    },
     selectMesoTerm2: function(x,y,id,label,attr) {
     this.logDebug("selectMesoTerm2("+id+","+label+")");
     this.showMesoTerm2(this.getNGram2(id),x,y,id,label,attr);
    },
    
     selectMesoDocument: function(x,y,id,label,attr) {
     this.logDebug("selectMesoDocument("+id+","+label+")");
     var doc = this.getDocument(id);
     if (doc==null) return;
     
     $('#infodiv').html(Shotenjin.render("\n\
     <h1 class=\"nodedetailsh1\">Project ${label}</h1>\n\
     <h3 class=\"nodedetailsh3\">Contains these terms:</h3>\n\
     <p class=\"nodedetailsp\">\n\
     <?js var first; for (var ng in edges['NGram']) { first=ng; break; } ?>\
         <a href=\"\" class=\"ui-widget-content ui-state-default\">${edges_data['NGram'][ng]['label']}</a> \
     <?js for (var ng in edges['NGram']) { if (ng == first) continue; ?>\
     <br/><a href=\"javascript:tinaviz.selectMesoTerm('#{ng}')\" class=\"ui-widget-content ui-state-default\">${edges_data['NGram'][ng]['label']}</a>\
     <?js } ?>.\
     </p>\n", doc));

     this.showMesoDocument(doc, x,y,id,label,attr);
  },
    selectMesoDocument2: function(x,y,id,label,attr) {
     this.logDebug("selectMesoDocument2("+id+","+label+")");
     this.showMesoDocument2(this.getDocument2(id), x,y,id,label,attr);
  },

    
    
    showMesoDocument: function(doc,x,y,id,label,attr) {
           if (doc == null) return;
            var gexf = Shotenjin.render("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n\
        <meta lastmodifieddate=\"19-Feb-2010\"><description></description></meta>\n\
    <graph>\n\
        <attributes class=\"node\">\n\
        </attributes>\n\
        <tina>\n\
            <selected node=\""+id+"\" />\n\
        </tina>\n\
        <nodes>\n\
            <node id=\"#{id}\" label=\"${label}\">\n\
                <viz:size value=\"7\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"Document\" />\n\
                </attvalues>\n\
            </node>\n\
<?js     for (var ng in edges['NGram']) {  ?>\
            <node id=\"#{ng}\" label=\"${edges_data['NGram'][ng]['label']}\">\n\
                <viz:size value=\"4.0\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"NGram\" />\n\
                </attvalues>\n\
            </node>\n\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js var i=0;                                                                               ?>\
<?js for (var ng1 in edges['NGram']) {                                                  ?>\
            <edge id=\"#{i++}\" source=\"#{id}\" target=\"#{ng1}\" weight=\"20\" />\n\
<?js    for (var ng2 in edges['NGram']) {                                               ?>\
<?js        var weight = 0;                                                                 ?>\
<?js        for (var shared in edges_data['NGram'][ng1]['edges']['Document']) {            ?>\
<?js            if (shared==id) continue;                                                   ?>\
<?js            if (shared in edges_data['NGram'][ng2]['edges']['Document']) weight++;     ?>\
<?js        }                                                                               ?>\
<?js        if (weight < 2) continue;                                                       ?>\
<?js        weight = weight / 10.0;                                                         ?>\
            <edge id=\"#{i++}\" source=\"#{ng1}\" target=\"#{ng2}\" weight=\"#{weight}\" />\n\
<?js    }                                                                                   ?>\
<?js }                                                                                      ?>\
        </edges>\n\
    </graph>\n\
</gexf>", doc);
     // console.log(gexf);
     applet.clear("meso");
     applet.getSession().updateFromString("meso",gexf);
    },
        
    showMesoDocument2: function(doc,x,y,id,label,attr) {
        if (doc == null) return;
        var doctype = "Document";
      
        var gexf = Shotenjin.render("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n\
        <meta lastmodifieddate=\"19-Feb-2010\"><description></description></meta>\n\
    <graph>\n\
        <attributes class=\"node\">\n\
        </attributes>\n\
        <tina>\n\
            <selected node=\""+id+"\" />\n\
        </tina>\n\
        <nodes>\n\
            <node id=\"#{id}\" label=\"${label}\">\n\
                <viz:size value=\"7\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\""+doctype+"\" />\n\
                </attvalues>\n\
            </node>\n\
<?js for (var ng in edges['NGram']) { ?>\
<?js    for (var doc in edges_data['NGram'][doc]['edges']['"+doctype+"']) { ?>\
            <node id=\"#{doc}\" label=\"${edges_data['"+doctype+"'][doc]['label']}\">\n\
                <viz:size value=\"4.0\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\""+doctype+"\" />\n\
                </attvalues>\n\
            </node>\n\
<?js    } ?>\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js var i=0;                                                                                 ?>\
<?js for (var ng in edges['NGram']) {                                                     ?>\
<?js    for (var doc1 in edges_data['NGram'][ng]['edges']['"+doctype+"']) {                ?>\
            <edge id=\"#{i++}\" source=\"#{id}\" target=\"#{doc}\" weight=\"#{20}\" />\n\
<?js                                                                                          ?>\
<?js        for (var doc2 in edges_data['NGram'][ng]['edges']['"+doctype+"']) {            ?>\
<?js            if (doc1 == doc2) continue;                                                     ?>\
<?js            var weight=0;                                                                 ?>\
<?js            for (var shared in edges_data['Document'][doc1]['edges']['NGram']) {           ?>\
<?js                if (shared==id) continue;                                                 ?>\
<?js                if (shared in edges_data['Document'][doc2]['edges']['NGram']) weight++;   ?>\
<?js            }                                                                             ?>\
<?js        if (weight < 2) continue;                                                         ?>\
<?js        weight = weight / 10.0;                                                         ?>\
            <edge id=\"#{i++}\" source=\"#{doc1}\" target=\"#{doc2}\" weight=\"#{weight}\" />\n\
<?js        }                                                                                 ?>\
<?js    }                                                                                     ?>\
<?js }                                                                                        ?>\
        </edges>\n\
    </graph>\n\
</gexf>", doc);
     // console.log(gexf);
     applet.clear("meso");
     applet.getSession().updateFromString("meso",gexf);
    },
     showMesoTerm: function(ng,x,y,id,label,attr) {
            if (ng == null) return;
            var gexf = Shotenjin.render("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n\
        <meta lastmodifieddate=\"19-Feb-2010\"><description></description></meta>\n\
    <graph>\n\
        <attributes class=\"node\">\n\
        </attributes>\n\
        <tina>\n\
            <selected node=\""+id+"\" />\n\
        </tina>\n\
        <nodes>\n\
            <node id=\"#{id}\" label=\"${label}\">\n\
                <viz:size value=\"7\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"NGram\" />\n\
                </attvalues>\n\
            </node>\n\
<?js     for (var doc in edges['Document']) {  ?>\
            <node id=\"#{doc}\" label=\"${edges_data['Document'][doc]['label']}\">\n\
                <viz:size value=\"4.0\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\"Document\" />\n\
                </attvalues>\n\
            </node>\n\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js var i=0;                                                                               ?>\
<?js for (var doc1 in edges['Document']) {                                                  ?>\
            <edge id=\"#{i++}\" source=\"#{id}\" target=\"#{doc1}\" weight=\"20\" />\n\
<?js    for (var doc2 in edges['Document']) {                                               ?>\
<?js        var weight = 0;                                                                 ?>\
<?js        for (var shared in edges_data['Document'][doc1]['edges']['NGram']) {            ?>\
<?js            if (shared==id) continue;                                                   ?>\
<?js            if (shared in edges_data['Document'][doc2]['edges']['NGram']) weight++;     ?>\
<?js        }                                                                               ?>\
<?js        if (weight < 2) continue;                                                       ?>\
<?js        weight = weight / 10.0;                                                         ?>\
            <edge id=\"#{i++}\" source=\"#{doc1}\" target=\"#{doc2}\" weight=\"#{weight}\" />\n\
<?js    }                                                                                   ?>\
<?js }                                                                                      ?>\
        </edges>\n\
    </graph>\n\
</gexf>", ng);
     // console.log(gexf);
     applet.clear("meso");
     applet.getSession().updateFromString("meso",gexf);
    },
    
     showMesoTerm2: function(ng,x,y,id,label,attr) {
      if (ng == null) return;
        var doctype = "NGram";
        
        var gexf = Shotenjin.render("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n\
        <meta lastmodifieddate=\"19-Feb-2010\"><description></description></meta>\n\
    <graph>\n\
        <attributes class=\"node\">\n\
        </attributes>\n\
        <tina>\n\
            <selected node=\""+id+"\" />\n\
        </tina>\n\
        <nodes>\n\
            <node id=\"#{id}\" label=\"${label}\">\n\
                <viz:size value=\"7\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\""+doctype+"\" />\n\
                </attvalues>\n\
            </node>\n\
<?js for (var doc in edges['Document']) { ?>\
<?js    for (var ng in edges_data['Document'][doc]['edges']['"+doctype+"']) { ?>\
            <node id=\"#{ng}\" label=\"${edges_data['"+doctype+"'][ng]['label']}\">\n\
                <viz:size value=\"4.0\"/>\n\
                <attvalues>\n\
                    <attvalue for=\"0\" value=\""+doctype+"\" />\n\
                </attvalues>\n\
            </node>\n\
<?js    } ?>\
<?js } ?>\
        </nodes>\n\
        <edges>\n\
<?js var i=0;                                                                                 ?>\
<?js for (var doc in edges['Document']) {                                                     ?>\
<?js    for (var ng1 in edges_data['Document'][doc]['edges']['"+doctype+"']) {                ?>\
            <edge id=\"#{i++}\" source=\"#{id}\" target=\"#{ng}\" weight=\"#{20}\" />\n\
<?js                                                                                          ?>\
<?js        for (var ng2 in edges_data['Document'][doc]['edges']['"+doctype+"']) {            ?>\
<?js            if (ng1 == ng2) continue;                                                     ?>\
<?js            var weight=0;                                                                 ?>\
<?js            for (var shared in edges_data['NGram'][ng1]['edges']['Document']) {           ?>\
<?js                if (shared==id) continue;                                                 ?>\
<?js                if (shared in edges_data['NGram'][ng2]['edges']['Document']) weight++;    ?>\
<?js            }                                                                             ?>\
<?js        if (weight < 15) continue;                                                        ?>\
<?js        weight = weight / 10.0;                                                           ?>\
            <edge id=\"#{i++}\" source=\"#{ng1}\" target=\"#{ng2}\" weight=\"#{weight}\" />\n\
<?js        }                                                                                 ?>\
<?js    }                                                                                     ?>\
<?js }                                                                                        ?>\
        </edges>\n\
    </graph>\n\
</gexf>", ng);
     // console.log(gexf);
     applet.clear("meso");
     applet.getSession().updateFromString("meso",gexf);
    }
  };
}

tinaviz = new Tinaviz();

//$(document).ready(function() {

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
    /*
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
    */


//});



