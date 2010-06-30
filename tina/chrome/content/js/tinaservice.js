//      This program is free software; you can redistribute it and/or modify
//      it under the terms of the GNU General Public License as published by
//      the Free Software Foundation; either version 2 of the License, or
//      (at your option) any later version.
//
//      This program is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      GNU General Public License for more details.
//
//      You should have received a copy of the GNU General Public License
//      along with this program; if not, write to the Free Software
//      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//      MA 02110-1301, USA.



jQuery.ajaxSettings.traditional = true;

function TinaServiceClass(url) {

    var STATUS_ERROR = 666;
    var STATUS_RUNNING = 0;
    var STATUS_OK = 1;
    var SERVER_URL = url; // don't forget the "/" at the end

    return {

    /*
    url="http://localhost:8888/graph?$dataset&filetype=gexf"
    */
    getGraph: function(_dataset, cb) {
        this._GET("graph",
            {
                dataset: _dataset,
            },
            {
                error: "couldn't getGraph"
            },
            cb
        );

    },

    /*
    url="http://localhost:8888/file?$path$dataset$index$format$overwrite"
    */
    getFile: function(_path, _dataset, _format, _overwrite, _minoccs, cb) {
        this._GET("file",
            {
                path: _path,
                dataset: _dataset,
                format:  _format,
                overwrite:  _overwrite,
                minoccs: _minoccs
            },
            {
                error:"couldn't getFile"
            },
            cb
        );
    },

    /*
    url="http://localhost:8888/cooccurrences?$periods$whitelist"
    */
    getCooccurrences: function(_periods, _whitelistpath, cb) {
        this._GET("cooccurrences",
            {
                periods: _periods,
                whitelistpath: _whitelistpath
            },
            {
                error:"couldn't getCooccurrences"
            },
            cb
        );
    },

    /*
    url="http://localhost:8888/whitelist?$periods$dataset$whitelistlabel"
    */
    getWhitelist: function(_dataset, _periods, _whitelistlabel, _complementarywhitelist, _userstopwords, _minoccs, cb) {
        //console.log("calling getWhitelist("+_dataset+","+_periods+","+_whitelistlabel+","+cb+")");
        //console.log(_periods);
        this._GET("whitelist",
            {
                dataset: _dataset,
                periods: _periods,
                whitelistlabel: _whitelistlabel,
                whitelistpath: _complementarywhitelist,
                userstopwords: _userstopwords,
                minoccs: _minoccs,
            },
            {
                error:"couldn't getWhitelist"
            },
            cb
        );
    },

    /*
      Access methods to objects stored in the database
    */
    getCorpus: function(_dataset, _id, cb) {
        //console.log("calling getCorpus("+_dataset+", "+_id+","+cb+")");
        this._GET("corpus", { dataset: _dataset, id: _id }, {error:"couldn't getCorpus"}, cb);
    },
    getNGram: function(_dataset, _id, cb) {
        //console.log("calling getNGram("+_dataset+","+_id+","+cb+")");
        this._GET("ngram", { dataset: _dataset, id: _id }, {error:"couldn't getNgram"}, cb);
    },
    getDocument: function(_dataset, _id, cb) {
        //console.log("calling getDocument("+_dataset+", "+_id+", "+cb+")");
        this._GET("document",
            {
                dataset: _dataset, id: _id
            },
            {
                error:"couldn't getDocument"
            },
            cb
        );
    },
    getDataset: function(_dataset, cb) {
        this._GET("dataset",
            {
                dataset: _dataset
            },
            {
                error: "couldn't getDataset"
            },
            cb
        );
    },
    /*
    Special method to list existing datasets
    */
    getDatasetList: function(cb) {
        this.getDataset('', cb);
    },

    /*
     * do an HTTP GET request to SERVER_URL + path + params
     * SERVER_URL is a constant,
     * path is a parameter,
     * params is serialized to URL encoded arguments
     */
    _GET: function(path, params, defaultcb, _cb, _async) {
        _async = true;
        if (_async === undefined) {
            _async = true;
        }
        // setup default values, if defined
        var cb = {};
        for (key in defaultcb) { cb[key] = defaultcb[key]; }
        if ("error" in defaultcb) {
            cb.error = function(XMLHttpRequest, textStatus, errorThrown) {
                alert("default error cb: "+defaultcb["error"]);
                console.log(XMLHttpRequest, textStatus, errorThrown);
            };
        }
        for (key in _cb) { cb[key] = _cb[key]; }

        // call the jquery ajax, passing the params and the callbacks
        $.ajax({
                // jquery to url
                url: SERVER_URL+"/"+path,
                data: params,
                type: "GET",
                dataType: "json",
                beforeSend: cb.beforeSend,
                error: cb.error,
                success: cb.success,
                complete: cb.complete,
                //async : _async
         });
    },

    getWalkUserPath: function(_dataset, _filetype, cb) {
        this._GET("walk_user_path",
            {
                dataset: _dataset,
                filetype: _filetype
            },
            {
                error: "couldn't getWalkUserPath"
            },
            cb
        );
    },
    /************************************************************************
     * POST
     ************************************************************************/

    /*
    * postFile
    * curl http://localhost:8888/file -d dataset="test_data_set" -d path="tests/data/pubmed_tina_test.csv"
    */

    postFile: function(_dataset, _path, _format, _overwrite, cb) {
        //console.log("calling postFile("+_dataset+","+_path+",False,"+_format+","+_overwrite+")");
        this._POST("file",
            {
                dataset: _dataset,
                path: _path,
                index: 'False', // should be indexed?
                format: _format,
                overwrite: _overwrite,
            },
            {
                error: "couldn't postFile"
            },
            cb
        );
    },

    /*
     * postWhitelist
     * curl http://localhost:8888/whitelist -d path="tests/data/pubmed_whitelist.csv" -d whitelistlabel="testwhitelist"
    */
    postWhitelist: function(_path, _whitelistlabel, cb) {
        //console.log("calling postWhitelist("+_path+","+_whitelistlabel+","+cb+")");
        this._POST("whitelist",
            {
              path: _path,
              whitelistlabel: _whitelistlabel
            },
            {
                error:"couldn't postWhitelist"
            },
            cb
       );
    },

    /*
    curl http://localhost:8888/cooccurrences -d dataset="test_data_set" -d whitelist="tests/data/pubmed_whitelist.csv" -d periods="1"
    */
    postCooccurrences: function(_dataset, _periods, _whitelistpath, _userstopwords, cb) {
        //console.log("calling postCooccurrences("+_dataset+","+_periods+","+_whitelist+","+cb+")");

        this._POST("cooccurrences",
            // inpost params
            {
                dataset: _dataset,
                periods: _periods,
                whitelistpath: _whitelistpath,
                userstopwords: _userstopwords,
            },
            {
                error:"couldn't postCooccurrences"
            },
            cb
        );
    },

    /*
    curl http://localhost:8888/graph -d dataset="test_data_set" -d periods="1"
    */
    postGraph: function(_dataset, _periods, _whitelistpath, cb) {
        //console.log("calling postGraph("+_dataset+", "+cb+")");
        this._POST("graph",
            {
                dataset: _dataset,
                periods: _periods,
                whitelistpath: _whitelistpath,
            },
            {
                error:"couldn't post graph"
            },
            cb
        );
    },

    postDataset: function(_obj, cb) {
        //console.log("calling dataset("+_obj+","+cb+")");
        this._POST("dataset",
            {
                dataset: _obj
            },
            {
                error:"couldn't postDataset"
            },
            cb
        );
    },

    postCorpus: function(_dataset, _obj, cb) {
        //console.log("calling postCorpus("+_dataset+", "+_obj+","+cb+")");
        this._POST("corpus", { dataset: _dataset, id: _obj }, {error:"couldn't postCorpus"}, cb);
    },
    postNGram: function(_dataset, _obj, cb) {
        //console.log("calling postNGram("+_dataset+","+_obj+","+cb+")");
        this._POST("ngram", { dataset: _dataset, id: _obj }, {error:"couldn't postNGram"}, cb);
    },
    postDocument: function(_dataset, _obj, cb) {
        //console.log("calling postDocument("+_dataset+", "+_obj+", "+cb+")");
        this._POST("document", { dataset: _dataset, id: _obj }, {error:"couldn't postDocument"}, cb);
    },

    /**
     * do an HTTP POST request to SERVER_URL + path
     * SERVER_URL is a constant,
     * path is a parameter,
     */
    _POST: function(path, params, defaultcb, _cb) {

        // setup default values, if defined
        var cb = {};
        for (key in defaultcb) { cb[key] = defaultcb[key]; }
        if ("error" in defaultcb) {
            cb.error = function(XMLHttpRequest, textStatus, errorThrown) {
                alert("default error cb: "+defaultcb["error"]);
                console.log(XMLHttpRequest, textStatus, errorThrown);
            };
        }
        for (key in _cb) { cb[key] = _cb[key]; }

        // call the jquery ajax, passing the params and the callbacks
        $.ajax({
                // jquery to url
                url: SERVER_URL+"/"+path,
                type: "POST",
                dataType: "json",
                data: params,
                beforeSend: cb.beforeSend,
                error: cb.error,
                success: cb.success,
                complete: cb.complete
         });

    },
    /* transforms a relative path ("./user/etc/") to an http url */
    fileURL: function(relativePath) {
        var relativeURL = relativePath.split("user/");
        return SERVER_URL+"/user/"+relativeURL[1];
    },

    };

}
var TinaService = new TinaServiceClass("http://localhost:8888");
