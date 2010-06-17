
function readLines(filename) {

        var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
        var path = (new DIR_SERVICE()).get("CurProcD", Components.interfaces.nsIFile).path;
        var filePath;// = path + filename;
        if (path.search(/\\/) != -1) { filePath = path + "\\"+filename }
        else { filePath = path + "/"+filename  }

        this.logDebug("going to load "+filename);
        var file =
            Components.classes["@mozilla.org/file/local;1"]
                .createInstance(Components.interfaces.nsILocalFile);
        this.logDebug("initWithPath: "+filePath);
        file.initWithPath(filePath);

        var fstream =
            Components.classes["@mozilla.org/network/file-inpost-stream;1"]
                .createInstance(Components.interfaces.nsIFileInpostStream);
        var cstream =
            Components.classes["@mozilla.org/intl/converter-inpost-stream;1"]
                .createInstance(Components.interfaces.nsIConverterInpostStream);

        fstream.init(file, -1, 0, 0);

        cstream.init(fstream, "UTF-8", 16000000, 0); // 16Mb - you can use another encoding here if you wish

        var str = {};
        cstream.readString(-1, str); // read the whole file and post it in str.value
        cstream.close(); // this closes fstream
        return str.value;
}
    

function TinaServiceClass(url) {
    var STATUS_ERROR = 0;
    var STATUS_RUNNING = 1;
    var SERVER_URL = url; // don't forget the "/" at the end
    return {

    runImportFile: function (path, configFile, corporaId, index, filetypeFormat, overwrite) {
        console.log("calling dataset("+_dataset+","+cb+")");
        this._POST("file",
            { file: _path }, 
            {error:"couldn't get dataset"},
            cb      
        );
    },
    
    // Export an extraction session
    runExportCorpora: function(periods, corporaId, exportPath, whitelistPath, userfiltersPath) {
    
    },
    
    // import a whitelist and writes cooc matrix
    runProcessCoocGraph: function(whitelistPath, corporaId, periods, userfiltersPath, threshold) {
        // a convertir en sous appels
    },

    // export a cooc matrix to text file
    runExportCoocMatrix: function() {
        // TODO
    
    },

    // export a gexf graph
    runExportGraph: function(corporaId, periods, threshold, whitelistPath) {
        // TODO
    
    },

    /*
    url="http://localhost:8888/graph?$dataset&filetype=gexf"
    */
    getGraph: function(dataset, cb) {
        console.log("calling getGraph("+_dataset+", "+cb+")");
          
        this._GET("file",
           { 
            path: _path, 
            dataset: _dataset, 
            index: _index,
            format:  _format,
            overwrite:  _overwrite
           }, 
           {error:"couldn't get file"},
           cb
        );
        
    },  
     
    getGraphList: function(cb) {
        console.log("calling getGraphList("+cb+")");
        this.getDatasetList({
            success: function(datasets) {
                var graphs = {};
                for (dataset in datasets) {
                    console.log("searching graph for dataset "+dataset);
                    this.getGraph(dataset, {
                        success: function(graph) {
                            console.log("got graph for dataset "+dataset);
                            graphs[dataset] = graph;
                        }
                    });
                }
                cb.success( graphs );
            }
        });
    },  
     
    
    /*
    url="http://localhost:8888/file?$path$dataset$index$format$overwrite"
    */
    getFile: function(_path, _dataset, _index, _format, _overwrite, cb) {
        //console.log("calling file("+_dataset+","+_file+")");
        //var data = this.loadFromString(view, this.readLines(_file));
        
        this._GET("file",
           { 
            path: _path, 
            dataset: _dataset, 
            index: _index,
            format:  _format,
            overwrite:  _overwrite
           }, 
           {error:"couldn't get file"},
           cb
        );
    },
    
    /*
    url="http://localhost:8888/cooccurrences?$periods$whitelist"
    */
    getCooccurrences: function(_periods, _whitelist, cb) {
        console.log("calling getCooccurrences("+_periods+","+_whitelist+","+cb+")");
        
        this._GET("cooccurrences",
            // inpost params
            { periods: _periods, 
              whitelist: _whitelist 
            }, 
            {error:"couldn't get cooccurrences"},
            cb
        );
    },
    /*
    url="http://localhost:8888/whitelist?$periods$dataset$whitelistlabel"
    */

    getWhitelist: function(_dataset, _periods, _whitelistlabel, cb) {
        console.log("calling getWhitelist("+_dataset+","+_periods+","+_whitelistlabel+","+cb+")");
        this._GET("whitelist",
            { dataset: _dataset,
              periods: _periods, 
              whitelistlabel: _whitelistlabel 
            },
            {error:"couldn't get whitelist"}, 
            cb
        );
    },
    
    /*
      Access methods to objects stored in the database
    */
    getCorpus: function(_dataset, _id, cb) {
        console.log("calling getCorpus("+_dataset+", "+_id+","+cb+")");
        this._GET("corpus", { dataset: _dataset, id: _id }, {error:"couldn't get corpus"}, cb);
    },
    getNGram: function(_dataset, _id, cb) {
        console.log("calling getNGram("+_dataset+","+_id+","+cb+")");
        this._GET("ngram", { dataset: _dataset, id: _id }, {error:"couldn't get ngram"}, cb);
    },
    getDocument: function(_dataset, _id, cb) {
        console.log("calling getDocument("+_dataset+", "+_id+", "+cb+")");
        this._GET("document", 
         { dataset: _dataset, id: _id },
         {error:"couldn't get document"}, 
         cb
        );
    },
    getDataset: function(_dataset, cb) {
        console.log("calling getDataset("+_dataset+","+cb+")");
        this._GET("dataset",
            { dataset: _dataset }, 
            {error:"couldn't get dataset"},
            cb      
        );
    },
    
    /*
    Special method to list existing datasets
    */
    getDatasetList: function(cb) {
        console.log("calling getDatasetList("+cb+")");
        this.getDataset('', cb);
    },
    
    /* POST */
    
    /*
    postFile
    curl http://localhost:8888/file -d dataset="test_data_set" -d path="tests/data/pubmed_tina_test.csv"
    */
    postFile: function(_dataset, _path, cb) {
        console.log("calling postFile("+_dataset+","+_path+")");
        this._POST("file",
           { dataset: _dataset,
             path: _path 
           }, 
           {error:"couldn't post file"},
           cb
        );
    },

    /* postWhitelist
    curl http://localhost:8888/whitelist -d path="tests/data/pubmed_whitelist.csv" -d whitelistlabel="testwhitelist"

    */
    postWhitelist: function(_path, _whitelistlabel, cb) {
        console.log("calling postWhitelist("+_path+","+_whitelistlabel+","+cb+")");
        this._POST("whitelist",
            { 
              path: _path,
              whitelistlabel: _whitelistlabel 
            },
            {error:"couldn't post whitelist"}, 
            cb
       );
    },
    
    /*
    curl http://localhost:8888/cooccurrences -d dataset="test_data_set" -d whitelist="tests/data/pubmed_whitelist.csv" -d periods="1"
    */
    postCooccurrences: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling postCooccurrences("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        
        this._POST("cooccurrences",
            // inpost params
            { dataset: _dataset, 
              periods: _periods, 
              whitelist: _whitelist 
            }, 
            {error:"couldn't post cooccurrences"},
            cb
        );
    },

   /*
   curl http://localhost:8888/graph -d dataset="test_data_set" -d periods="1"
   */
    postGraph: function(_dataset, _periods, cb) {
        console.log("calling postGraph("+_dataset+", "+cb+")");
        this._POST("graph", 
            { dataset: _dataset,
              periods: _periods }, 
            {error:"couldn't post graph"},
            cb
        );
    },  
    
    postDataset: function(_obj, cb) {
        console.log("calling dataset("+_obj+","+cb+")");
        this._POST("dataset",
            { dataset: _obj }, 
            {error:"couldn't get dataset"},
            cb      
        );
    },

    postCorpus: function(_dataset, _obj, cb) {
        console.log("calling postCorpus("+_dataset+", "+_obj+","+cb+")");
        this._POST("corpus", { dataset: _dataset, id: _obj }, {error:"couldn't get corpus"}, cb);
    },
    postNGram: function(_dataset, _obj, cb) {
        console.log("calling postNGram("+_dataset+","+_obj+","+cb+")");
        this._POST("ngram", { dataset: _dataset, id: _obj }, {error:"couldn't get ngram"}, cb);
    },
    postDocument: function(_dataset, _obj, cb) {
        console.log("calling postDocument("+_dataset+", "+_obj+", "+cb+")");
        this._POST("document", { dataset: _dataset, id: _obj }, {error:"couldn't get document"}, cb);
    },

    /**
     * do an HTTP GET request to SERVER_URL + path + params
     * SERVER_URL is a constant,
     * path is a parameter,
     * params is serialized to URL encoded arguments
     */ 
    _GET: function(path, params, def, _cb) {
    
        // setup default values, if defined
        var cb = {};
        for (key in def) { cb[key] = def[key]; }
        if ("error" in def) { cb.error = function(e) { console.log(val+": "+e); }; }
        for (key in _cb) { cb[key] = _cb[key]; }
        
        // call the jquery ajax, passing the params and the callbacks
        $.ajax({
                // jquery to url
                url: SERVER_URL+"/"+path,
                data: params,
                type: "GET",
                dataType: "json",
                beforeSend: function() {
                     console.log("calling "+this.url);
                },
                error: cb.error,
                success: cb.success,
         });
    
    },
    
    /**
     * do an HTTP POST request to SERVER_URL + path
     * SERVER_URL is a constant,
     * path is a parameter,
     */ 
    _POST: function(path, params, def, _cb) {
    
        // setup default values, if defined
        var cb = {};
        for (key in def) { cb[key] = def[key]; }
        if ("error" in def) { cb.error = function(e) { console.log(val+": "+e); }; }
        for (key in _cb) { cb[key] = _cb[key]; }
        
        // call the jquery ajax, passing the params and the callbacks
        $.ajax({
                // jquery to url
                url: SERVER_URL+"/"+path,
                type: "POST",
                dataType: "json",
                data: params,
                beforeSend: function() {
                     console.log("calling "+this.url);
                },
                error: cb.error,
                success: cb.success,
         });
    
    },
    
    };
    
}
