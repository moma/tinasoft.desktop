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

/*******************************************************************************
 * Functions submitting requests to Tinaserver
 * see tinaservice.js and tinaservicecallbacks.js
*******************************************************************************/

/*
 * Requests to import/extract a data set source file
 */
$(function() {
    var submitImportfile = function(event) {
        var corpora = $("#importdatasetid");
        var path = $("#importfilepath");

        if ( corpora.val() == '' ) {
            corpora.addClass('ui-state-error');
            console.log( "missing the corpora field" );
            return false;
        }
        if ( path.val() == "" ) {
            path.addClass('ui-state-error');
            console.log( "missing the path field" );
            return false;
        }

        var whitelistlabel = $("#importwhitelistlabel");

        var filetype = $("#importfiletype");
        var userstopwords = $("#importuserstopwords");
        overwrite = 'False';

        var minoccs = parseInt($("#extractminoccs").val());
        if( ! IsNumeric(minoccs) ) {
            alert("minimum occurrences parameter must be an integer");
            $("#extractminoccs").addClass("ui-state-error");
            return false;
        }
        //var extract = $("#importextract:checked");
        //if (extract.val() !== undefined) {
            TinaService.getFile(
                path.val(),
                corpora.val(),
                whitelistlabel.val(),
                filetype.val(),
                overwrite,
                minoccs,
                userstopwords.val(),
                TinaServiceCallback.extractFile
            );
            //return true;
        //}
        /*else {
            TinaService.postFile(
                corpora.val(),
                path.val(),
                filetype.val(),
                overwrite,
                TinaServiceCallback.importFile
            );
            //return true;
        }*/

    };
    /*
     * Requests indexation of a source file
     */

    var submitIndexFile = function(event) {
        var corpora = $("#indexdatasetid");
        var path = $("#indexfilepath");
        var filetype = $("#indexfiletype");
        if ( corpora.val() == '' ) {
            corpora.addClass('ui-state-error');
            return false;
        }
        if ( path.val() == "" ) {
            path.addClass('ui-state-error');
            return false;
        }
        var whitelistpath = $("#index_whitelist").data("whitelistpath");
        if ( whitelistpath == '' ||  whitelistpath === undefined ) {
            $("#index_whitelist").addClass('ui-state-error');
            alert("please select a white list");
            return false;
        }
        var overwrite = 'False';
        TinaService.postFile(
            path.val(),
            corpora.val(),
            whitelistpath,
            filetype.val(),
            overwrite,
            TinaServiceCallback.postFile
        );
    };
    /*
     * Requests to process cooccurrences
     * then to generate a graph
     */

    var submitprocessCoocGraph = function(event) {
        var whitelistpath = $("#graph_whitelist").data("whitelistpath");
        if ( whitelistpath == '' ||  whitelistpath === undefined ) {
            $("#graph_whitelist").addClass('ui-state-error');
            alert("please select a white list");
            return false;
        }
        var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
        if( Object.size(corporaAndPeriods) == 0) {
            alert("please select one or more periods");
            return false;
        }
        var userfilterspath  = $("#userstopwordsfile_graph");
        TinaServiceCallback.postCooc.success = function(){
            TinaService.postGraph(
                corpora,
                corporaAndPeriods[corpora],
                whitelistpath,
                TinaServiceCallback.postGraph
            );
        };
        for (corpora in corporaAndPeriods) {
            TinaService.postCooccurrences(
                corpora,
                corporaAndPeriods[corpora],
                whitelistpath,
                userfilterspath.val(),
                TinaServiceCallback.postCooc
            );
            break;
        }
        //return true;
    };


    /* Writing a data set's graph action controler */

    /*
    var submitExportGraph = function(event) {
        var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
        var whitelistpath = $("#whitelistfile")
        // DEBUG
        if ( whitelistpath.val() == '' ) {
            whitelistpath.addClass('ui-state-error');
            whitelistpath.addClass('ui-state-error');
            console.log( "missing the white list path field" );
            return false;
        }
        threshold = [0,1];
        for (corpora in corporaAndPeriods) {
            TinaService.runExportGraph(
                corpora,
                corporaAndPeriods[corpora],
                threshold,
                whitelistpath.val()
            );
            return true;
        }
    };
    * /

    /* Requests to export a data set's whitelist csv */

    var submitExportWhitelist = function(event) {
        var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
        if( Object.size(corporaAndPeriods) == 0) {
            alert("please select one or periods");
            return false;
        }
        var complementwhitelistfile = $("#complementwhitelistfile");
        var userstopwordsfile = $("#userstopwordsfile_whitelist");
        var whitelistlabel = $("#whitelistlabel");
        var minoccs = parseInt($("#exportminoccs").val());
        if( IsNumeric(minoccs) === false ) {
            alert("minimum occurrences parameter must be an integer");
            $("#extractminoccs").addClass("ui-state-error");
            return false;
        }
        if ( whitelistlabel.val() == '' ) {
            whitelistlabel.addClass('ui-state-error');
            alert( "please choose a white list label" );
            return false;
        }

        for (corpora in corporaAndPeriods) {
            TinaService.getWhitelist(
                corpora,
                corporaAndPeriods[corpora],
                whitelistlabel.val(),
                complementwhitelistfile.val(),
                userstopwordsfile.val(),
                minoccs,
                TinaServiceCallback.getWhitelist
            );
            return true;
        }
    };
    $('#importFile').click(function(event) {
        submitImportfile(event);
    });
    $('#indexFile').click(function(event) {
        submitIndexFile(event);
    });
    /*$('#exportWhitelist').click(function(event) {
        submitExportWhitelist(event)
    });*/
    $('#processCooc').click(function(event) {
        submitprocessCoocGraph(event)
    });
    /*$('#exportGraph button').click(function(event) {
        submitExportGraph(event)
    });*/

});
