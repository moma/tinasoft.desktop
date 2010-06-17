        
function TinaServiceClass(url) {
    var STATUS_ERROR = 0;
    var STATUS_RUNNING = 1;
    var SERVER_URL = url; // don't forget the "/" at the end
    return {

    runImportFile: function (path, configFile, corporaId, index, filetypeFormat, overwrite) {

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
    
    
    getGraphList: function(dataset, cb) {
        console.log("calling listGraphs("+_dataset+", "+cb+")");
        this._GET("graph", { dataset: _dataset }, {error:"couldn't list graphs"}, cb);
    },  
    
    getDataset: function(_dataset, cb) {
        console.log("calling dataset("+_dataset+","+cb+")");
        this._GET("dataset",
            { dataset: _dataset }, 
            {error:"couldn't get dataset"},
            cb      
        );
    },

    getDatasetList: function(cb) {
        console.log("calling listDataset("+cb+")");
        
        this._GET("dataset",
            { dataset: '' }, 
            { error: "couldn't get dataset"},
            cb
        );
    },
    
    getFile: function(_dataset, _file, cb) {
        console.log("calling file("+_dataset+","+_file+")");
        
        this._GET("file",
           { dataset: _dataset, file: _file }, 
           {error:"couldn't get file"},
           cb
        );
    },
    
    getCooccurrences: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling cooccurrences("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        
        this._GET("cooccurrences",
            // input params
            { dataset: _dataset, 
              periods: _periods, 
              whitelist: _whitelist 
            }, 
            {error:"couldn't get cooccurrences"},
            cb
        );
    },
    
    getWhitelist: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling listGraphs("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        this._GET("whitelist",
            { dataset: _dataset, periods: _periods, whitelist: _whitelist },
            {error:"couldn't get whitelist"}, 
            cb
            );
    },
    getCorpus: function(_dataset, _id, cb) {
        console.log("calling corpus("+_dataset+", "+_id+","+cb+")");
        this._GET("corpus", { dataset: _dataset, id: _id }, {error:"couldn't get corpus"}, cb);
    },
    getNGram: function(_dataset, _id, cb) {
        console.log("calling ngram("+_dataset+","+_id+","+cb+")");
        this._GET("ngram", { dataset: _dataset, id: _id }, {error:"couldn't get ngram"}, cb);
    },
    getDocument: function(_dataset, _id, cb) {
        console.log("calling document("+_dataset+", "+_id+", "+cb+")");
        this._GET("document", { dataset: _dataset, id: _id }, {error:"couldn't get document"}, cb);
    },
    
    putFile: function(_dataset, _file, cb) {
        console.log("calling putFile("+_dataset+","+_file+")");
        this._POST("file",
           { dataset: _dataset, file: _file }, 
           {error:"couldn't get file"},
           cb
        );
    },

    putWhitelist: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling putWhitelist("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        this._POST("whitelist",
            { dataset: _dataset, periods: _periods, whitelist: _whitelist },
            {error:"couldn't get whitelist"}, 
            cb
            );
    },
    
    processCooccurrences: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling processCooccurrences("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        
        this._POST("cooccurrences",
            // input params
            { dataset: _dataset, 
              periods: _periods, 
              whitelist: _whitelist 
            }, 
            {error:"couldn't get cooccurrences"},
            cb
        );
    },

    exportGraph: function(dataset, cb) {
        console.log("calling exportGraph("+_dataset+", "+cb+")");
        this._POST("graph", { dataset: _dataset }, {error:"couldn't list graphs"}, cb);
    },  
    
    putDataset: function(_dataset, cb) {
        console.log("calling dataset("+_dataset+","+cb+")");
        this._POST("dataset",
            { dataset: _dataset }, 
            {error:"couldn't get dataset"},
            cb      
        );
    },

    putCorpus: function(_dataset, _id, cb) {
        console.log("calling putCorpus("+_dataset+", "+_id+","+cb+")");
        this._POST("corpus", { dataset: _dataset, id: _id }, {error:"couldn't get corpus"}, cb);
    },
    putNGram: function(_dataset, _id, cb) {
        console.log("calling putNGram("+_dataset+","+_id+","+cb+")");
        this._POST("ngram", { dataset: _dataset, id: _id }, {error:"couldn't get ngram"}, cb);
    },
    putDocument: function(_dataset, _id, cb) {
        console.log("calling putDocument("+_dataset+", "+_id+", "+cb+")");
        this._POST("document", { dataset: _dataset, id: _id }, {error:"couldn't get document"}, cb);
    },

    /**
     * do an HTTP GET request to SERVER_URL + path + params
      SERVER_URL is a constant,
      path is a parameter,
      params is serialized to URL encoded arguments
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
