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
     * do an HTTP REQUEST request to SERVER_URL + path
     * SERVER_URL is a constant,
     * path is a parameter,
     */
    _REQUEST: function(type, path, params, defaultcb, _cb, traditional, cache) {
        // setup default values, if defined
        if (traditional === undefined)
            traditional = true;
        if (cache === undefined)
            cache = false;
        //if (contentType === undefined)
        //    contentType = "application/x-www-form-urlencoded";
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
        console.log( $.ajax({
            // jquery to url
            url: SERVER_URL+"/"+path,
            type: type,
            // expected return value
            dataType: "json",
            // request parameters
            data: params,
            beforeSend: cb.beforeSend,
            error: cb.error,
            success: cb.success,
            complete: cb.complete,
            traditional: traditional,
            cache: false,
            //contentType: contentType
        }) );

    },

    /*
     * HTTP GET request
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
     * HTTP POST request
     */
    _POST: function(path, params, defaultcb, _cb) {
        this._REQUEST("POST", path, params, defaultcb, _cb);
    },

    /*
    * postFile
    * curl http://localhost:8888/file -d dataset="test_data_set" -d path="tests/data/pubmed_tina_test.csv"
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
     * curl http://localhost:8888/graph -d dataset="test_data_set" -d periods="1"
    */
    postGraph: function(_dataset, _periods, _whitelistpath, _outpath, _ngramoptions, _documentoptions, _exportedges, cb) {
        this._POST("graph",
            {
                dataset: _dataset,
                periods: _periods,
                whitelistpath: _whitelistpath,
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

    postDataset: function(_obj, cb) {
        this._POST("dataset", { dataset: _obj }, { error:"couldn't postDataset" }, cb);
    },

    postCorpus: function(_dataset, _obj, cb) {
        this._POST("corpus", { dataset: _dataset, id: _obj }, {error:"couldn't postCorpus"}, cb);
    },
    postNGram: function(_dataset, _obj, cb) {
        this._POST("ngram", { dataset: _dataset, id: _obj }, {error:"couldn't postNGram"}, cb);
    },
    postDocument: function(_dataset, _obj, cb) {
        this._POST("document", { dataset: _dataset, id: _obj }, {error:"couldn't postDocument"}, cb);
    },

    /**
     * do an HTTP DELETE request to SERVER_URL + path
     */
    _DELETE: function( path, params, defaultcb, _cb ) {
        this._REQUEST( "DELETE", path, params, defaultcb, _cb);
    },

    deleteDataset: function( _dataset, cb ) {
        this._DELETE( "dataset?dataset="+_dataset, {}, { error:"couldn't deleteDataset" }, cb );
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
