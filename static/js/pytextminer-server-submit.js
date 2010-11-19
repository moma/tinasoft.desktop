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
 * Requests Pytextminer services
 */
$(function() {

    var safeString = function(unsafe) {
        return unsafe.replace(/[^\w]/g,"");
    };

    var submitExtractFile = function(event) {
        var corpora = $("#importdatasetid");
        if ( corpora.val() == '' ) {
            corpora.addClass('ui-state-error');
            return false;
        }
        var path = $("#importfilepath");
        if ( path.val() == "" ) {
            path.addClass('ui-state-error');
            return false;
        }

        var whitelistlabel = $("#importwhitelistlabel");

        var filetype = $("#importfiletype");

        var userstopwords = $("#importuserstopwords");

        overwrite = 'False';

        //var minoccs = parseInt($("#extractminoccs").val());
        var minoccs = $("#extractminoccs").spinner('value');

        if( ! IsNumeric(minoccs) ) {
            alert("minimum occurrences must be an integer");
            $("#extractminoccs").addClass("ui-state-error");
            return false;
        }

        TinaService.getFile(
            path.val(),
            safeString(corpora.val()),
            safeString(whitelistlabel.val()),
            filetype.val(),
            minoccs,
            userstopwords.val(),
            TinaServiceCallback.extractFile
        );

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
            safeString(corpora.val()),
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

    var submitGenerateGraph = function(event) {
        var whitelistpath = $("#graph_whitelist").data("whitelistpath");
        if ( whitelistpath == '' ||  whitelistpath === undefined ) {
            $("#graph_whitelist").addClass('ui-state-error');
            alert("please select a white list");
            return false;
        }
        var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );
        if( Object.size(corporaAndPeriods) == 0) {
            $("#graph_periods").addClass('ui-state-error');
            alert("please select one or more periods");
            return false;
        }
        for ( var corpname in corporaAndPeriods ) {
            var corpora = corpname;
            break;
        }

        var label = $("#graphlabel");
        var ngramGraphOptions = {
            alpha: $("#graphalpha").spinner('value'),
            proximity: $("#ngrams-graph-type").val(),
            /*edgethreshold: [
                $("#graph-ngrams-edges-min").spinner('value'),
                $("#graph-ngrams-edges-max").spinner('value')
            ],
            nodethreshold: [
                $("#graph-ngrams-nodes-min").spinner('value'),
                $("#graph-ngrams-nodes-max").spinner('value')
            ]*/
        };
        var documentGraphOptions = {
            proximity: $("#documents-graph-type").val(),
            /*edgethreshold: [
                $("#graph-documents-edges-min").spinner('value'),
                $("#graph-documents-edges-max").spinner('value')
            ],
            nodethreshold: [
                $("#graph-documents-nodes-min").spinner('value'),
                $("#graph-documents-nodes-max").spinner('value')
            ]*/
        };
        TinaService.postGraph(
            corpora,
            corporaAndPeriods[corpora],
            whitelistpath,
            safeString( label.val() ),
            ngramGraphOptions,
            documentGraphOptions,
            TinaServiceCallback.postGraph
        );
    };

    $('#extractFile').click(function(event) {
        submitExtractFile(event);
    });
    $('#indexFile').click(function(event) {
        submitIndexFile(event);
    });
    $('#genGraph').click(function(event) {
        submitGenerateGraph(event)
    });


});