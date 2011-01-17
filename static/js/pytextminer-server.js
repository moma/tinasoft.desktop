/*
    Copyright (C) 2009-2011 CREA Lab, CNRS/Ecole Polytechnique UMR 7656 (Fr)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

//jQuery.ajaxSettings.traditional = true;

function TinaServiceClass(url) {

    var STATUS_ERROR = 666;
    var STATUS_RUNNING = 0;
    var STATUS_OK = 1;
    var SERVER_URL = url; // don't forget the "/" at the end

    return {

    SERVER_URL: SERVER_URL,

    /**
     * HTTP generic REQUEST to SERVER_URL + path using $.ajax
     * SERVER_URL is initialized with this object,
     * @param type {string} HTTP type of the request
     * @param path {string} the path of the request
     * @param params {Object} a JSON of parameters passed with the request
     * @param defaultcb {Object} default callbacks if missing in _cb
     * @param _cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
     * @params traditional {bool} telling $.ajax to switch parameters serialization method (form-urlencoded)
     * @params cache {bool} telling $.ajax to avoid browser caching if false
     * @params contentType {string} how data is sent with the request
     */
    _REQUEST: function(type, path, params, defaultcb, _cb, traditional, cache, contentType) {
        // setup default values, if defined
        if (traditional === undefined)
            traditional = true;
        if (cache === undefined)
            cache = false;
        if (contentType === undefined)
            contentType = 'application/x-www-form-urlencoded';
        var cb = {};
        for (key in defaultcb) { cb[key] = defaultcb[key]; }
        if ("error" in defaultcb) {
            cb.error = function(XMLHttpRequest, textStatus, errorThrown) {
                alert("default error cb: "+defaultcb["error"]);
                console.log(XMLHttpRequest, textStatus, errorThrown);
            };
        }
        // overwrites default with the application's parmas
        for (key in _cb) { cb[key] = _cb[key]; }
        $.ajax({
            url: SERVER_URL+"/"+path,
            type: type,
            // expected return value
            dataType: "json",
            data: params,
            beforeSend: cb.beforeSend,
            error: cb.error,
            success: cb.success,
            complete: cb.complete,
            traditional: traditional,
            cache: cache,
            contentType: contentType
        });

    },

    /**
     * HTTP GET request to SERVER_URL + path
     * @param path {string}
     * @param params {Object} a JSON of parameters passed with the request
     * @param defaultcb {Object} default callbacks if missing in _cb
     * @param _cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
     */
    _GET: function(path, params, defaultcb, _cb) {
        this._REQUEST("GET", path, params, defaultcb, _cb);
    },

    /*
     * url="http://localhost:8888/file?$path$dataset$index$format$overwrite"
    */
    getFile: function(_path, _dataset, _whitelistlabel, _format, _minoccs, _userstopwords, cb) {

        this._GET("file",
            {
                path: _path,
                dataset: this.encodeURIComponent(this.protectPath(_dataset)),
                whitelistlabel: this.encodeURIComponent(this.protectPath(_whitelistlabel)),
                format:  _format,
                minoccs: _minoccs,
                userstopwords: _userstopwords
            },
            {
                error:"couldn't getFile"
            },
            cb
        );
    },

    /*
     * Access methods to objects stored in the database
    */
    getCorpus: function(_dataset, _id, cb) {
        this._GET("corpus",
            {
                dataset: this.encodeURIComponent(_dataset),
                id: this.encodeURIComponent(_id)
            },
            {
                error:"couldn't getCorpus"
            },
            cb
        );
    },
    getNGram: function(_dataset, _id, cb) {
        this._GET("ngram",
            {
                dataset: this.encodeURIComponent(_dataset),
                id: this.encodeURIComponent(_id)
            },
            {
                error:"couldn't getNgram"
            },
            cb
        );
    },
    getDocument: function(_dataset, _id, cb) {
        this._GET("document",
            {
                dataset: this.encodeURIComponent(_dataset),
                id: this.encodeURIComponent(_id)
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
                dataset: this.encodeURIComponent(_dataset)
            },
            {
                error: "couldn't getDataset"
            },
            cb
        );
    },

    /*
    Special method listing all existing datasets
    */
    getDatasetList: function(cb) {
        this.getDataset("", cb);
    },

    getLog: function(cb) {
        this._GET("log",
            {},
            {
                error: "couldn't getLog"
            },
            cb
        );
    },


    exit: function(cb) {
        this._GET("exit", {}, {error:"couldn't exit"}, cb);
    },

    getWalkUserPath: function(_dataset, _filetype, cb) {
        this._GET("walk_user_path",
            {
                dataset: this.encodeURIComponent(_dataset),
                filetype: this.encodeURIComponent(_filetype)
            },
            {
                error: "couldn't getWalkUserPath"
            },
            cb
        );
    },

    getWalkSourceFiles: function(cb) {
        this._GET("walk_source_files",
            {},
            {
                error: "couldn't getWalkSourceFiles"
            },
            cb
        );
    },


    getOpenUserFile: function(_fileurl, cb) {
        this._GET("open_user_file",
            {
                fileurl: this.encodeURIComponent( _fileurl )
            },
            {
                error: "couldn't getOpenUserFile"
            },
            cb
        );
    },

    /**
     * HTTP POST request to SERVER_URL + path
     * @param path {string}
     * @param params {Object} a JSON of parameters passed with the request
     * @param defaultcb {Object} default callbacks if missing in _cb
     * @param _cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
     */
    _POST: function(path, params, defaultcb, _cb) {
        this._REQUEST("POST", path, params, defaultcb, _cb);
    },

    /*
     * postFile
     *  @param _path {string} source file path
     * @param _dataset {string} dataset id
     * @param _whitelistpath {string} whitelist file path
     * @param _format {string} source file format
     * @param _overwrite {string} "True" or "False" telling the Pytextminer to overwrite index or not
     * @param cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
    */
    postFile: function(_path, _dataset, _whitelistpath, _format, _overwrite, cb) {
        this._POST("file",
            {
                path: _path,
                dataset: this.encodeURIComponent(_dataset),
                whitelistpath: _whitelistpath,
                format: _format,
                overwrite: _overwrite
            },
            {
                error: "couldn't postFile"
            },
            cb
        );
    },

    /*
     * postGraph
     * @param _dataset {string} dataset id
     * @param _periods {string} a list of periods
     * @param _outpath {string} custom name of the new whitelist
     * @param _ngramoptions {Object} JSON object of parameters for the NGram Graph
     * @param _documentoptions {Object} JSON object of parameters for the Document Graph
     * @param _exportedges {string} 'True' or 'False' asking the server to export the complete graph to "current.gexf" on the local disk
     * @param cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
    */
    postGraph: function(_dataset, _periods, _outpath, _ngramoptions, _documentoptions, _exportedges, cb) {
        this._POST("graph",
            {
                dataset: _dataset,
                periods: _periods,
                outpath: this.protectPath(_outpath),
                ngramgraphconfig: $.param( _ngramoptions ),
                documentgraphconfig: $.param( _documentoptions ),
                exportedges: _exportedges
            },
            {
                error:"couldn't post graph"
            },
            cb
        );
    },

    /*
    * POST updates an prteprocessed values of an entire dataset
    * @param _dataset {String} dataset id
    * @param cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
    *
    */
    postGraphPreprocess: function(_dataset, cb) {
        this._POST("graph_preprocess", { dataset: _dataset }, { error:"couldn't postGraphPreprocess" }, cb);
    },

    /*
    * POST updates of Pytextminer nodes
    * @param _obj {Object} a JSON containing a minimal version of a Pytextminer node and only attr and edges you want to update
    * @param _redondant {String} 'True' or 'False' asking the server to rewrite edges of every linked Pytextminer nodes (from an different category)
    * @param cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
    *
    */
    postDataset: function(_obj, _redondant, cb) {
        this._POST("dataset", { dataset: _obj }, { error:"couldn't postDataset" }, cb);
    },
    postCorpus: function(_dataset, _obj, _redondant, cb) {
        this._POST("corpus", { dataset: _dataset, object: JSON.stringify(_obj), redondant: _redondant }, {error:"couldn't postCorpus"}, cb);
    },
    postNGram: function(_dataset, _obj, _redondant, cb) {
        this._POST("ngram", { dataset: _dataset, object: JSON.stringify(_obj), redondant: _redondant }, {error:"couldn't postNGram"}, cb);
    },
    postDocument: function(_dataset, _obj, _redondant, cb) {
        this._POST("document", { dataset: _dataset, object: JSON.stringify(_obj), redondant: _redondant }, {error:"couldn't postDocument"}, cb);
    },
    postNGramForm: function(_dataset, _label, _is_keyword, cb) {
        this._POST("ngramform", { dataset: _dataset, label: _label, is_keyword: _is_keyword }, {error:"couldn't postNGramForm"}, cb);
    },

    /**
     * HTTP DELETE request to SERVER_URL + path
     * @param path {string}
     * @param params {Object} ignored because of jquery.ajax
     * @param defaultcb {Object} default callbacks if missing in _cb
     * @param _cb {Object} a JSON specifying success/error/complete/beforeSend $.ajax() callbacks
     */
    _DELETE: function( path, params, defaultcb, _cb ) {
        this._REQUEST( "DELETE", path, params, defaultcb, _cb);
    },

    deleteDataset: function( _dataset, cb ) {
        this._DELETE( "dataset?dataset="
            +self.encodeURIComponent(_dataset),
            {},
            { error:"couldn't deleteDataset" },
            cb
        );
    },

    deleteNGramForm: function( _dataset, _label, _id, _is_keyword, cb ) {
        this._DELETE( "ngramform?dataset="
            +self.encodeURIComponent(_dataset)
            +"&label="
            + self.encodeURIComponent(_label)
            +"&id="
            +self.encodeURIComponent(_id)
            +"ís_keyword="
            +self.encodeURIComponent(_is_keyword),
            {},
            { error:"couldn't deleteNGramForm" },
            cb
        );
    },


    /*
     * transforms a relative path ("user/etc/") to an http:// url, compatible with windows paths
     */
    httpURL: function(relativePath) {
        var relativeURL = relativePath.split('user');
        var partURL = relativeURL[1].replace(/\\/g,"/").replace(/%5C/g,"/");
        return SERVER_URL+"/user"+partURL;
    },

    encodeURIComponent: function(component) {
        return encodeURIComponent(component).replace(/\\/g,"%5C").replace(/\//g,"%2F");
    },

    protectPath: function(label) {
        return label.replace(/\\/g,"").replace(/\//g,"").replace(/\./g,"");
    }

    };

}
var TinaService = new TinaServiceClass("http://localhost:8888");
