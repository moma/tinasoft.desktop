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
        var filetype = $("#importfiletype");
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
        var overwrite = $("#importoverwrite:checked");
        //alert(overwrite.val());
        if (overwrite.val() !== undefined) {
            overwrite = 'True';
        }
        else {
            overwrite = 'False';
        }
        var minoccs = parseInt($("#extractminoccs").val());
        if( ! IsNumeric(minoccs) ) {
            alert("minimum occurrences parameter must be an integer");
            $("#extractminoccs").addClass("ui-state-error");
            return false;
        }
        var extract = $("#importextract:checked");
        var callback = false;
        //alert(extract.val());
        if (extract.val() !== undefined) {
            TinaService.getFile(
                path.val(),
                corpora.val(),
                filetype.val(),
                overwrite,
                minoccs,
                TinaServiceCallback.extractFile
            );
            //return true;
        }
        else {
            TinaService.postFile(
                corpora.val(),
                path.val(),
                filetype.val(),
                overwrite,
                TinaServiceCallback.importFile
            );
            //return true;
        }

    };

    /*
     * Requests to process cooccurrences
     * then to generate a graph
     */

    var submitprocessCoocGraph = function(event) {
        var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
        if( Object.size(corporaAndPeriods) == 0) {
            alert("please select one or periods");
            return false;
        }
        var whitelistpath = $("#whitelistfile")
        var userfilterspath  = $("#userstopwordsfile_graph")
        if ( whitelistpath.val() == '' ) {
            whitelistpath.addClass('ui-state-error');
            console.log( "missing the white list path field" );
            return false;
        }
        TinaServiceCallback.postCooc.success = function(){
            TinaService.postGraph(
                corpora,
                corporaAndPeriods[corpora],
                whitelistpath.val(),
                TinaServiceCallback.postGraph
            );
        };
        for (corpora in corporaAndPeriods) {
            TinaService.postCooccurrences(
                corpora,
                corporaAndPeriods[corpora],
                whitelistpath.val(),
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
    $('#exportWhitelist').click(function(event) {
        submitExportWhitelist(event)
    });
    $('#processCooc').click(function(event) {
        submitprocessCoocGraph(event)
    });
    /*$('#exportGraph button').click(function(event) {
        submitExportGraph(event)
    });*/

});
