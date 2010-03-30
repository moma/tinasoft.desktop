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
        
        // load default values (eg. from settings)
	    this.dispatchProperty("weight/min", 0.0);
	    this.dispatchProperty("weight/max", 1.0);
	    
	    this.dispatchProperty("genericity/min", 0.0);
	    this.dispatchProperty("genericity/max", 1.0);
	    
	    this.dispatchProperty("radiusByGenericity/max", 1.0);
	    
	    // we want to keep documents
	    this.dispatchProperty("category/value", "NGram");
	    this.dispatchProperty("category/mode", "keep");
	        
		this.dispatchProperty("radius/value", 1.0);
		
		// we want to create a "batchviz's local exploration"-like behaviour?
		//  it's trivial with the new architecture! use the "explorer" filter

	    // create a new "Category()" filter instance, which use the "category" namespace, and attach it to the "macro" new
	    // and YES, you can define filters or properties at any time, it's totally asynchronous ;)
	    
		this.bindFilter("Category",             "category",           "macro");
		//this.bindFilter("ThresholdGenericity",  "genericity",         "macro");
		this.bindFilter("ThresholdWeight",      "weight",             "macro");
		//this.bindFilter("NodeFunction",         "radiusByGenericity", "macro");
		this.bindFilter("NodeRadius",           "radius",             "macro");  
		this.bindFilter("WeightSize",           "weightSize",         "macro");
        //this.bindFilter("Layout",           "layout",   "macro");
  
  
        // MESO LOCAL EXPLORATION FILTERS
        // create a local graph in the meso view, from the macro view
        this.bindFilter("SubGraphCopy",         "subgraph",           "meso");
        this.setProperty("meso", "subgraph/source", "macro");
		this.setProperty("meso", "subgraph/item", "");
				
		this.bindFilter("Category",             "category",           "meso");
				
		// filter by genericity
		//this.bindFilter("ThresholdGenericity",  "genericity",         "meso");
		
		// filter by edge threshold
		this.bindFilter("ThresholdWeight",      "weight",             "meso");

		// multiply the radius by the genericity
		//this.bindFilter("NodeFunction",         "radiusByGenericity", "meso");
		
		// multiply the radius by the GUI slider value
		this.bindFilter("NodeRadius",           "radius",             "meso");
		
		// multiply the radius by the GUI slider value
		this.bindFilter("WeightSize",           "weightSize",         "meso");
        // this.bindFilter("Layout",           "layout",   "meso");

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
        this.setProperty("meso", "subgraph/item", "");
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
        this.logDebug("nodeRightClicked called on "+level+" "+label+" "+attr+"");

         // here 'attr' is the category of the current selected node!
         // and cat the current filter
        /*if (id == null) { $('#sidebariframe').hide(); }
        else            { $('#sidebariframe').show(); }*/
        var cat = this.getProperty(level, "category/value");
         if (cat=="Document") {
                this.setProperty(level, "category/value", "NGram");
                this.touch(level);
            } else if (cat=="NGram") {
                this.setProperty(level, "category/value", "Document");
                this.touch(level);
            }
            if (id != this.getProperty(level, "subgraph/item")) {
                applet.clear("meso");
                this.setProperty("meso", "subgraph/item", id);
           }
           
        if (level == "macro") {

            if (attr=="Document") {
                 //this.setProperty("macro", "category/value", "Document");
                 //this.touch("macro");
                 this.printDocument(x,y,id,label,attr);
            } else if (attr=="NGram") {
                 //this.setProperty("macro", "category/value", "NGram");
                 //this.touch("macro");
                 this.printNGram(x,y,id,label,attr);
            }
        } else if (level == "meso") { // si dans meso
           
            this.touch(level);
            if (attr=="Document") { // et clic droit sur un doc
                //this.setProperty("meso", "category/value", "Document");
                //this.touch("meso");
                this.printDocument(x,y,id,label,attr);
            } else if (attr=="NGram") {
                //this.setProperty("meso", "category/value", "NGram");
                //this.touch("meso");
                this.printNGram(x,y,id,label,attr);
            }
        } else if (level == "micro") {
            if (attr=="Document") {
            } else if (attr=="NGram") {
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

               this.logDebug("nodeLeftClicked called on "+level+" "+label+" "+attr+"");

        if (level == "macro") {
            if (attr=="Document") {
                 applet.clear("meso");
                this.setProperty("meso", "subgraph/item", id);
                this.setProperty("meso", "category/value", "NGram");
                this.touch("meso");
                this.printDocument(x,y,id,label);
            } else if (attr=="NGram") {  
                applet.clear("meso");
                this.setProperty("meso", "subgraph/item", id);
                this.setProperty("meso", "category/value", "Document");
                this.touch("meso");               
                this.printNGram(x,y,id,label);
            }
        } else if (level == "meso") {
            if (attr=="Document") {
                applet.clear("meso");
                this.setProperty("meso", "subgraph/item", id);
                this.setProperty("meso", "category/value", "NGram");
                this.touch("meso");
                this.printDocument(x,y,id,label);
            } else if (attr=="NGram") {
                applet.clear("meso");
                this.setProperty("meso", "subgraph/item", id);
                this.setProperty("meso", "category/value", "Document");
                this.touch("meso");
                this.printNGram(x,y,id,label);
            }
        } else if (level == "micro") {
            if (attr=="Document") {
            } else if (attr=="NGram") {
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

        cstream.init(fstream, "UTF-8", 16000000, 0); // 16Mb - you can use another encoding here if you wish

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
        applet.storeResolution();
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
    
    
    bindFilter: function(name, path, level) {
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
    
    getProperty: function(level,key,value) {
        if (applet == null) return;
        return applet.getView(level).getProperty(key);
    },
    
    touch: function(level) {
        applet.getView(level).getGraph().touch();
    },
    
    search: function(txt) {
        this.logNormal("Searching is not implemented yet..");
    },

  
  printNGram: function(x,y,id,label,attr) {

     var ng = getNGram( id );
     this.logDebug("ng= "+ng);
     if (ng == null) return null;
     // don't query the DB if we are on a "lambda" graph
     
     delete ng['py/object'];
     ng["edges_data"] = { "Document" : {}, "Corpus" : {} };
      for (var doc in ng["edges"]["Document"]) {
           var obj = getDocument(doc);
           if (obj == null) { delete ng["edges"]["Document"][doc];} 
           else {  ng["edges_data"]["Document"][doc] = obj;}
      }
     
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
     

    },

     printDocument: function(x,y,id,label,attr) {

     var doc = getDocument( id );
     this.logDebug("doc= "+doc);
     if (doc == null) return null;
     // don't query the DB if we are on a "lambda" graph
          
     delete doc['py/object'];
     doc["edges_data"] = { "NGram" : {}, "Corpus" : {} };
      for (var ng in ng["edges"]["NGram"]) {
           var obj = getNGram(doc);
           if (obj == null) { delete doc["edges"]["NGram"][ng];} 
           else {  doc["edges_data"]["NGram"][ng] = obj;}
      }
     
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

  }

  };
}

tinaviz = new Tinaviz();

