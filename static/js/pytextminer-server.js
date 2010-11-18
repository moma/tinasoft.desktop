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

    SERVER_URL: SERVER_URL,

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
                url: SERVER_URL+"/"+path,
                data: params,
                type: "GET",
                dataType: "json",
                beforeSend: cb.beforeSend,
                error: cb.error,
                success: cb.success,
                complete: cb.complete,
                cache: false
         });
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

    /************************************************************************
     * POST
     ************************************************************************/

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
    curl http://localhost:8888/graph -d dataset="test_data_set" -d periods="1"
    */
    postGraph: function(_dataset, _periods, _whitelistpath, _outpath, _ngramoptions, _documentoptions, cb) {
        this._POST("graph",
            {
                dataset: _dataset,
                periods: _periods,
                whitelistpath: _whitelistpath,
                outpath: this.protectPath(_outpath),
                ngramgraphconfig: $.param( _ngramoptions ),
                documentgraphconfig: $.param( _documentoptions )
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
     * do an HTTP POST request to SERVER_URL + path
     * SERVER_URL is a constant,
     * path is a parameter,
     */
    _POST: function(path, params, defaultcb, _cb, traditional) {
        if (traditional === undefined)
            traditional = true;
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
            complete: cb.complete,
            cache: false,
            traditional: traditional
            /*processData: false,
            contentType: "application/json"*/
        });

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
