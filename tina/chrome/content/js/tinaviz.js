

function Tinaviz() {

    var wrapper = null;
    var applet = null;
    var cbsAwait = {};
    var cbsRun = {};
    this.isReady = 0;
    this.infodiv = null;

    //return {
        this.init= function() {
            if (wrapper != null || applet != null) return;
            wrapper = $('#vizframe').contents().find('#tinaviz')[0];
            if (wrapper == null) return;
            applet = wrapper.getSubApplet();
            if (applet == null) return;
            this.auto_resize();
            this.main();
            this.isReady = 1;
        }
        /************************
         * Main method
         *
         ************************/
        this.main = function() {

            this.logDebug("starting tinasoft desktop..");

            this.setView("macro");

            this.dispatchProperty("edgeWeight/min", 0.0);
            this.dispatchProperty("edgeWeight/max", 1.0);
            this.dispatchProperty("nodeWeight/min", 0.0);
            this.dispatchProperty("nodeWeight/max", 1.0);
            this.dispatchProperty("category/category", "NGram");
            this.dispatchProperty("output/nodeSizeMin", 5.0);
            this.dispatchProperty("output/nodeSizeMax", 20.0);
            this.dispatchProperty("output/nodeSizeRatio", 50.0/100.0);

            this.dispatchProperty("selection/radius", 1.0);

            this.bindFilter("Category", "category", "macro");

            this.bindFilter("NodeWeightRange",  "nodeWeight", "macro");
            this.bindFilter("EdgeWeightRange", "edgeWeight",  "macro");
            this.bindFilter("NodeFunction", "radiusByWeight", "macro");
            this.bindFilter("Output", "output", "macro");

            this.bindFilter("SubGraphCopyStandalone", "category", "meso");
            this.setProperty("meso", "category/source", "macro");
            this.setProperty("meso", "category/category", "Document");
            this.setProperty("meso", "category/mode", "keep");
            this.bindFilter("NodeWeightRange",  "nodeWeight", "meso");
            this.bindFilter("EdgeWeightRange", "edgeWeight",  "meso");
            this.bindFilter("NodeFunction", "radiusByWeight", "meso");
            this.bindFilter("Output", "output", "meso");

            //this.togglePause();

            // init the node list with ngrams
            this.updateNodes( "macro", "NGram" );
            // cache the document list
            this.getNodes( "macro", "Document" );

            this.logDebug("enabling applet tab..");
            //$( "#tabs" ).enable();
            $( "#tabs" ).tabs( "option", "disabled", false );
        
            this.infodiv.display_current_category();
            this.infodiv.display_current_view();
            
            this.logDebug("tinasoft desktop started!");
        }

        /************************
         * Core applet methods
         *
         ************************/

        /*
         * Core method communicating with the applet
         */
        this.bindFilter= function(name, path, view) {
            if (applet == null) return;
            if (view == null) return applet.getSession().addFilter(name, path);
            return applet.getView(view).addFilter(name, path);
        }

        /*
         * Core method communicating with the applet
         */
        this.dispatchProperty= function(key,value) {
            if (applet == null) return;
            return applet.setProperty("all",key,value);
        }

        /*
         * Core method communicating with the applet
         */
        this.setProperty= function(view,key,value) {
            if (applet == null) return;
            return applet.setProperty(view,key,value);
        }

        /*
         * Core method communicating with the applet
         */
        this.getProperty= function(view,key) {
            if (applet == null) return;
            return applet.getProperty(view,key);
        }

        /*
         * Commands switching between view levels
         */
        this.setView = function(view) {
            if (applet == null) return;
            applet.setView(view);
        }

        /*
         * Gets the the view level name
         */
        this.getView = function(view) {
            if (applet == null) return;
            return applet.getView().getName();
        }

        /*
         * Commits the applet's parameters
         * Accept an optional callback to give some reaction to events
         */
        this.touch= function(view) {
            if (applet == null) return;
            //this.logNormal("touch("+view+")");
            if (view===undefined) {
                applet.touch();
            } else {
                applet.touch(view);
            }
        }

        /*
         *  Adds a node to the current selection
         */
        this.selectFromId = function( id ) {
            if (applet == null) return;
            return applet.selectFromId(id);
        }

        this.resetLayoutCounter= function(view) {
            if (applet == null) return;
            // TODO switch to the other view
            applet.resetLayoutCounter();
        }

        /*
         *  Get the current state of the applet
         */
        this.isEnabled = function() {
            if (applet == null) {
                return false;
            } else {
                return applet.isEnabled();
            }
        }
        /*
         *  Set the current state of the applet to enable
         */
        this.setEnabled =  function(value) {
            if (applet == null) return;
            applet.setEnabled(value);
        }

        
        /*
        * Search nodes
        */
        this.getNodesByLabel = function(label, type) {
            if (applet == null) return {};
            return $.parseJSON( applet.getNodesByLabel(label, type));
        }

        /*
        * Search and select nodes
        */
        this.searchNodes= function(label, type) {
            if (applet == null) return {};
            var matchlist = this.getNodesByLabel(label, type);
            for (var i = 0; i < matchlist.length; i++ ) {
                applet.selectFromId( decodeJSON( matchlist[i]['id'] ) );
                // todo: auto center!!
                //applet.
            }
        }

        /*
        * Highlight nodes
        */
        this.highlightNodes= function(label, type) {
            if (applet == null) return {};
            var matchlist = this.getNodesByLabel(label, type);
            for (var i = 0; i < matchlist.length; i++ ) {
                applet.highlightFromId( decodeJSON( matchlist[i]['id'] ) );
                // todo: auto center!!
                //applet.
            }
        }

        this.touchCallback= function(view, cb) {
            if (applet == null) return;
            if (view==null) {
                applet.touch();
            }
            if (cb==null) {
               applet.touch(view);
            } else {
                this.enqueueCb(applet.touch(view),cb);
            }
        }

        /*
        * recenter the graph
        */
        this.autoCentering= function() {
            if (applet == null) return false;
            return applet.autoCentering();
        }

        /*
         *  Gets lattributes o a given node
         */
        this.getNodeAttributes = function(id) {
            if (applet == null) return;
            return $.parseJSON( applet.getNodesAttributes(id) );
        }

        /*
         * Gets the list of neighbours for a given node
         */
        this.getNeighbourhood = function(view,id) {
            if (applet == null) return;
            return $.parseJSON( applet.getNeighbourhood(view,id) );
        }


        /************************
         * Core Callback system
         *
         ************************/

        /*
         * Push a callback in the queue
         */
        this.enqueueCb=function(id,cb) {
            cbsAwait[id] = cb;
        }

        /*
         * Runs a callback
         */
        this.runCb=function(id) {
            cbsRun[i]();
            delete cbsRun[i];
        }
        /**
         * Put a callback for the "await" list to the "run" list
         *
         */
        this.activateCb=function(id) {
            cbsRun[id] = cbsAwait[id];
            delete cbsAwait[id];
            return id;
        }

        /**
         * How the callback system works:
         *
         * When the client call the "touch()" method, an update of the current view is
         * scheduled by the applet, then the id of the new revision will be stored together
         * with a callback address, by the javascript.
         *
         * As soon as the current view will reach this revision (or a greater one) the
         * corresponding callback(s) will be called, then removed from the stack.
         *
         */
        this.cbSync=function(id) {
            for (i in cbsAwait) {
                if (i<=id) {
                    setTimeout("tinaviz.runCb("+this.activateCb(i)+")",0);
                }
            }
        }

        /********************************
         *
         * Mouse Callback system
         *
         ********************************/

        /*
         *  Callback of right clics
         */
        this.nodeRightClicked = function(view, data) {
            if (applet == null) return;
            //if (view == "meso") {
                //this.toggleCategory(view);
            //}
        }

        /*
         *  Callback of left clics
         */
        this.nodeLeftClicked = function(view, data) {
            if ( data == null ) return;
            // copies the category from view to meso
            //if (view == "meso") {
                //this.toggleCategory(view);
                //this.setProperty("meso", "category/category", this.getProperty(view, "category/category"));
            //}
        }

        /*
         *  Callback of double left clics
         */
        this.leftDoubleClicked = function(view, data) {
            var category = this.getProperty("current", "category/category");
            this.logNormal( "after double-clic, category = "+category );
            for (var id in data) {
                this.viewMeso(decodeJSON(id), category);
                break;
            }
            /*if (view =="macro") {
            }
            if (view == "meso") {
                //this.toggleCategory(view);
            }*/
        }

        /*
         *  Callback of a node selection/clics
         */
        this.selected = function(view, attr, mouse) {
            if (attr == null) return;
            // always updates infodiv
            data = $.parseJSON(attr);
            this.infodiv.reset();
            this.infodiv.update(view, data);

            // left == selected a node
            if ( mouse == "left" ) {
                //this.nodeLeftClicked(view,data);
            }
            //right == unselected a node
            else if ( mouse == "right" ) {
                //this.nodeRightClicked(view,data);
            }
            else if (mouse == "doubleLeft") {
                this.leftDoubleClicked(view, data);
            }
        }

        /**
        * Callback after CHANGING THE VIEW LEVEL
        */
        this.switchedTo = function(view, selected) {
            if (applet == null) return;

            this.autoCentering();
            /*if (view=="macro") {
                $("#toggle-project").button('enable');
            } else if (view=="meso") {
                $("#toggle-project").button('disable');
            }*/

            // update the buttons
            $("#"+view+"-sliderEdgeWeight").slider( "option", "values", [
                this.getProperty(view, "edgeWeight/min"),
                this.getProperty(view, "edgeWeight/max")*100
            ]);
            $("#"+view+"-sliderNodeWeight").slider( "option", "values", [
                this.getProperty(view, "nodeWeight/min"),
                this.getProperty(view, "nodeWeight/max")*100
            ]);
            this.infodiv.display_current_category();
            this.infodiv.display_current_view();
            switchTab(view);
        }
        /************************
         *
         * I/O system
         *
         ************************/

        // Public methods
        this.loadFromURI= function(uri) {
            if (applet == null) return false;
            return applet.getSession().updateFromURI(view,uri);
        }
        
        // pass a GEXF string!
        this.loadFromString= function(view,gexf) {
            if (applet == null) return false;
            return applet.getSession().updateFromString(view,gexf);
        }
    
        // TODO: use a cross-browser compatible way of getting the current URL
        this.readGraphJava= function(view,graphURL) {
            // window.location.href
            // window.location.pathname
            var sPath = document.location.href;
            var gexfURL = sPath.substring(0, sPath.lastIndexOf('/') + 1) + graphURL;
            applet.getSession().updateFromURI(view,gexfURL);
        }

        this.readGraphAJAX= function(view,graphURL) {
            if (applet == null) return;
            $.ajax({
                url: graphURL,
                type: "GET",
                dataType: "text",
                beforeSend: function() { },
                error: function() { },
                success: function(gexf) {
                   applet.getSession().updateFromString(view,gexf);
               }
            });
        }

    
        this.loadRelativeGraphFromFile = function(view,filename) {
            this.loadFromString(view, this.readLines(filename));
        }
        
        // using string technique
        this.loadAbsoluteGraph= function(view,filename) {

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
        }

        this.loadAbsoluteGraphFromURI= function(filename) {
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
        }
        /***********************************
         *
         * Manual actions controler system
         *
         ***********************************/
        /*
         * hide/show labels
         */
        this.toggleLabels = function() {
            if (applet == null) return;
            return applet.getView().toggleLabels();
        }

        /*
         * hide/show nodes
         */
        this.toggleNodes = function() {
            if (applet == null) return;
            return applet.getView().toggleNodes();
        }

        /*
         * hide/show edges
         */
        this.toggleEdges = function() {
            if (applet == null) return;
            return applet.getView().toggleLinks();
        }

        /*
         * play/pause layout engine
         */
        this.togglePause = function() {
            if (applet == null) return;
            return applet.getView().togglePause();
        }

        /*
         * toggles HD rendering
         */
        this.toggleHD = function() {
            if (applet == null) return;
            return applet.getView().toggleHD();
        }
        /*
        * Get the opposite category name (the NOT DISPLAYED one)
        */
        this.getOppositeCategory = function(cat) {
            if (cat == "Document")
                return "NGram";
            else if (cat == "NGram")
                return "Document";
            else alert("error, cannot get opposite category of "+cat);

        }

        /**
         * Manually toggles the category
         */
        this.toggleCategory = function(view) {
            if (applet == null) return;
            // get and set the new category to display
            var next_cat = this.getOppositeCategory( this.getProperty(view, "category/category"));
            this.setProperty(view, "category/category", next_cat);
            //this.unselect();
            // resets the layout
            this.resetLayoutCounter();
            this.touch();
            this.autoCentering();
            // updates the node list table
            this.updateNodes(view, next_cat);
            // adds neighbour nodes (from next_cat) to the selection of the macro view
            /*for(var id in this.infodiv.selection) {
                var neighbours = this.getNeighbourhood("macro", id);
                for (var neighbourId in neighbours) {
                    if (neighbours[neighbourId].category == next_cat) {
                        this.logNormal( "selecting a neighbour "+neighbourId );
                        //this.selectFromId(neighbourId);
                    }
                }
            }*/
        }

        /**
         * Manually toggles the view to meso given an id
         */
        this.viewMeso = function(id, category) {
            // selects unique node
            this.unselect();
            this.selectFromId(id);
            // sets the category of the graph
            this.setProperty("meso", "category/category", category);
            //this.setProperty("macro", "category/category", category);
            this.setView("meso");
            this.touch("meso");
            this.updateNodes("meso", category);
        }


        this.clear= function() {
            if (applet == null) return;
            applet.clear();
        }
        
        /*
        * Manually unselects all nodes
        */
        this.unselect= function() {
            if (applet == null) return;
            applet.unselect();
            this.infodiv.reset();
            //if (this.getView() == "meso") {
               // this.setView("macro");
                //tinaviz.resetLayoutCounter();
                
                //this.autoCentering();
            //}
            this.touch("current");
           
        }


        /*
         *  Retrieves list of nodes
         */
        this.getNodes = function(view, category) {
            if (applet == null) return;
            this.infodiv.data[category] = $.parseJSON( applet.getNodes(view, category) );
            return this.infodiv.data[category];
        }
        /*
         *  Fires the update of node list cache and display
         */
        this.updateNodes = function(view, category)  {
            if ( category == this.infodiv.last_category ) return;
            this.infodiv.display_current_category();
            if (this.infodiv.data[category] === undefined)
                this.infodiv.updateNodeList( this.getNodes( view, category ), category );
            else
                this.infodiv.updateNodeList( this.infodiv.data[category], category );
        }


        /*
         *  Try to log an error with firebug otherwise alert it
         */
        this.logError= function(msg) {
            try {
                console.error(msg);
            }
            catch (e){
                alert(msg);
                return;
            }
        }
        /*
         *  Try to log an normal msg with firebug otherwise returns
         */
        this.logNormal = function(msg) {
            try {
                console.log(msg);
            }
            catch (e) {
                return;
            }
        }
        /*
         *  Try to log an normal msg with firebug otherwise returns
         */
        this.logDebug= function(msg) {
            return this.logNormal(msg);
        }


        /****************************************
         *
         * HTML VIZ DIV ADJUSTING/ACTION
         *
         ****************************************/

        /*
         * Dynamic div width
         */
        this.getWidth= function() {
            return $("#vizdiv").width();
        }

        /*
         * Dynamic div height
         */
        this.getHeight= function() {
            return getScreenHeight() - $("#hd").height() - $("#ft").height() - 50;
        }
        /*
         * Callback changing utton states
         */
        this.buttonStateCallback = function(button, enabled) {
            $(document).ready(
                function() {
                    // state = "disable"; if (enabled) { state = "enable"; }
                    $("#toggle-"+button).toggleClass("ui-state-active", enabled);
                    //$("#toggle-"+button).button(state);
                }
            );
        }

        /*
         * PUBLIC METHOD, AUTOMATIC RESIZE THE APPLET
         */
        this.auto_resize = function() {
           this.size(this.getWidth(), this.getHeight());
        }

        /*
         * PRIVATE METHOD, RESIZE THE APPLET
         */
        this.size= function(width, height) {
            if (wrapper == null || applet == null) return;
            wrapper.width = width;
            wrapper.height = height;
            $('#tinaviz').css('width',width);
            $('#tinaviz').css('height',height);
        }
    //};
}

var tinaviz = new Tinaviz();


/*

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
    loadRelativeGraphOld: function(view,filename) {

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

        cstream.init(fstream, "UTF-8", 32000000, 0); // 16Mb - you can use another encoding here if you wish

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
        
        

        
        var destDirPath;
        var destDir = Components.classes["@mozilla.org/file/local;1"]
        .createInstance(Components.interfaces.nsILocalFile);
        if (path.search(/\\/) != -1) {   destDirPath = path + "\\chrome\\content\\applet\\"; }
        else {   destDirPath = path + "/chrome/content/applet/"; }
        
       
        var finaFile =
             Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        finaFile.initWithPath(destDirPath+"current.gexf");
        if (finaFile.exists()) finaFile.remove(false);
        
        destDir.initWithPath(destDirPath);
        //console.log("file.copyTo("+destDir+",\"current.gexf\");");
        file.copyTo(destDir,"current.gexf");
        
        this.logDebug("destDirPath + current.gexf: "+destDirPath+"current.gexf");

        finaFile =
             Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        finaFile.initWithPath(destDirPath+"current.gexf");
        
        var uri =
              Components.classes["@mozilla.org/network/protocol;1?name=file"]
                .createInstance(Components.interfaces.nsIFileProtocolHandler)
                    .getURLSpecFromFile(finaFile);
                    
        this.logDebug("loading absolute graph: "+uri);
        result = this.loadFromURI(view,uri);
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
    
*/


/*


  };
}

tinaviz = new Tinaviz();
*/

