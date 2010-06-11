        
function TinaServiceClass(url) {
    var STATUS_ERROR = 0;
    var STATUS_RUNNING = 1;
    var SERVER_URL = url; // don't forget the "/" at the end
    return {
    
    
    listGraphs: function(dataset, cb) {
        console.log("calling listGraphs("+_dataset+", "+cb+")");
        this.doGET("graph", { dataset: _dataset }, {error:"couldn't list graphs"}, cb);
    },  
    
    dataset: function(_dataset, cb) {
        console.log("calling dataset("+_dataset+","+cb+")");
        this.doGET("dataset",
            { dataset: _dataset }, 
            {error:"couldn't get dataset"},
            cb      
        
        );
    },

    listDatasets: function(cb) {
        console.log("calling listDataset("+cb+")");
        
        this.doGET("dataset",
            { dataset: '' }, 
            { error: "couldn't get dataset"},
            cb
        );
    },
    
    file: function(_dataset, _file, cb) {
        console.log("calling file("+_dataset+","+_file+")");
        
        this.doGET("file",
           { dataset: _dataset, file: _file }, 
           {error:"couldn't get file"},
           cb
        );
    },
    
    cooccurrences: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling cooccurrences("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        
        this.doGET("cooccurrences",
            // input params
            { dataset: _dataset, 
              periods: _periods, 
              whitelist: _whitelist 
            }, 
            {error:"couldn't get cooccurrences"},
            cb
        );
    },

    
    whitelist: function(_dataset, _periods, _whitelist, cb) {
        console.log("calling listGraphs("+_dataset+","+_periods+","+_whitelist+","+cb+")");
        this.doGET("whitelist",
            { dataset: _dataset, periods: _periods, whitelist: _whitelist },
            {error:"couldn't get whitelist"}, 
            cb
            );
    },
    corpus: function(_dataset, _id, cb) {
        console.log("calling corpus("+_dataset+", "+_id+","+cb+")");
        this.doGET("corpus", { dataset: _dataset, id: _id }, {error:"couldn't get corpus"}, cb);
    },
    ngram: function(_dataset, _id, cb) {
        console.log("calling ngram("+_dataset+","+_id+","+cb+")");
        this.doGET("ngram", { dataset: _dataset, id: _id }, {error:"couldn't get ngram"}, cb);
    },
    document: function(_dataset, _id, cb) {
        console.log("calling document("+_dataset+", "+_id+", "+cb+")");
        this.doGET("document", { dataset: _dataset, id: _id }, {error:"couldn't get document"}, cb);
    },
    parametrize: function(params) {
        var out="";
        var i=0;
        for (key in params) {
            var val = params[key];
            var s = "";
            // todo: type checking
            if (val === Array) {
                console.log("parsing array..");
                for (var x = 0; x < val.length; x++ ) {
                    if (x==0) {
                        s=s+key+"="+x;
                    } else {
                        s=s+"&"+key+"="+x;
                    }
                }
                // for each sub val, we concatenate
            } else {
                s = s+key+"="+val;
            }
            if (i++ ==0) {
                out = out + "?";
            } else {
                out = out + "&";
            }
            out = out + s;
        }
        return out;
    },
    doGET: function(path, params, def, _cb) {
    
        // setup default values, if defined
        var cb = {};
        cb = cb + def;
        for (key in def) {
            var val = def[key];
            if (key=="error") {
                cb.error = function(e) { console.log(val+": "+e); };
                break;
            }
        }
        
        // todo check this, looks bad..
        cb = cb + _cb; 
        
        // call the jquery ajax, passing the params and the callbacks
        $.ajax({
                // jquery to url
                url: SERVER_URL+"/"+path+""+this.parametrize(params),
                type: "GET",
                dataType: "json",
                beforeSend: function() {
                     console.log("calling "+this.url);
                },
                error: cb.error,
                success: cb.success,
         });
    
    },
    
    };
    
}
