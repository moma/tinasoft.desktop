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

/*******************************************************************************
 * Functions submitting requests to Tinaserver
 * see tinaservice.js and tinaservicecallbacks.js
*******************************************************************************/


/*
 * Prepares requests to Pytextminer Server
 */
$(document).ready(function() {

    var safeString = function(unsafe) {
        return unsafe.replace(/[^\w]/g,"");
    };

    /*
     * Requests extraction of a source file's keyphrases
     */
    var submitExtractFile = function(event) {

        /*var corpora = $("#importdatasetid");
        if ( corpora.val() == '' ) {
            corpora.addClass('ui-state-error');
            return false;
        }*/
        var path = $("#importfilepath");
        if ( path.val() == "" ) {
            path.addClass('ui-state-error');
            return false;
        }

        var whitelistlabel = $("#importwhitelistlabel");
        var filetype = $("#importfiletype");
        //var userstopwords = $("#importuserstopwords");
        var minoccs = $("#extractminoccs").spinner('value');
        if( ! IsNumeric(minoccs) ) {
            alert("minimum occurrences must be an integer");
            $("#extractminoccs").addClass("ui-state-error");
            return false;
        }
        TinaService.getFile(
            path.val(),
            //safeString(corpora.val()),
            safeString(whitelistlabel.val()),
            filetype.val(),
            minoccs,
            //userstopwords.val(),
            TinaServiceCallback.extractFile
        );
        return true;
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
        var wlcache = Cache.getValue('whitelists',{});
        var whitelistlabel = $("#index_whitelist").val();
        if ( whitelistlabel == '' ||  whitelistlabel === undefined ) {
            if ( wlcache[whitelistlabel] === undefined ) {
                $("#index_whitelist").addClass('ui-state-error');
                alert("please select a white list");
                return false;
            }
        }
        var whitelistpath = wlcache[whitelistlabel];
        if (whitelistpath===undefined){
            alert("please select a whitelist from the list");
            return false;
        }
        // secure overwrite value
        var overwrite = 'False';
        TinaService.postFile(
            path.val(),
            safeString(corpora.val()),
            whitelistpath,
            filetype.val(),
            overwrite,
            TinaServiceCallback.postFile
        );
        Cache.setValue('dataset_id', safeString(corpora.val()));
        return true;
    };

    /*
     * Requests to generate a new graph
     */
    var submitGenerateGraph = function(event) {

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
            //alpha: $("#graphalpha").spinner('value'),
            proximity: $("#ngrams-graph-type").val()
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
            proximity: $("#documents-graph-type").val()
            /*edgethreshold: [
                $("#graph-documents-edges-min").spinner('value'),
                $("#graph-documents-edges-max").spinner('value')
            ],
            nodethreshold: [
                $("#graph-documents-nodes-min").spinner('value'),
                $("#graph-documents-nodes-max").spinner('value')
            ]*/
        };
        /*var exportGexf = $("#export-gexf").attr("checked");
        if (exportGexf == true) exportGexf = 'True';
        else exportGexf = 'False';*/

        // HACK
        exportGexf = 'True';

        TinaService.postGraph(
            corpora,
            corporaAndPeriods[corpora],
            safeString( label.val() ),
            ngramGraphOptions,
            documentGraphOptions,
            exportGexf,
            TinaServiceCallback.postGraph
        );
        return true;
    };


    $("#extractFileButton")
        .button({
            text: true,
            label: "launch"
        }).click(function(event) {
            submitExtractFile(event);
        });
    $("#extractFileButton").button('enable');
    $("#indexFileButton")
        .button({
            text: true,
            label: "launch"
        }).click(function(event) {
            submitIndexFile(event);
        });
    $("#indexFileButton").button('enable');
    $("#generateGraphButton")
        .button({
            text: true,
            label: "launch"
        }).click(function(event) {
            submitGenerateGraph(event);
        });
    //$("#export-gexf").button();
    $("#generateGraphButton").button('enable');
});
