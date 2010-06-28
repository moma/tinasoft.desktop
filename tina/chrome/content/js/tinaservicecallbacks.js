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
/* Tinasoft Server callback */
var SERVER_URL= "http://localhost:8888";
var TinaServiceCallback = {
    importFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains json encoded list of duplicate documents found
            displayDuplicateDocs( data );
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#importFile').addClass("ui-state-error", 1);
            $('#importFile').html( "error, please report the log file to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#importFile').removeClass("ui-state-disabled", 1);
            $('#importFile').html( "Launch" );
            /* Fetch data into table */
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#importfilepath").removeClass("ui-state-error", 1);
            $("#importdatasetid").removeClass("ui-state-error", 1);
            $('#importFile').removeClass("ui-state-error", 1);
            $('#importFile').addClass("ui-state-disabled", 1);
            $('#importFile').html( "please wait during import" );
            // add progress state notification
        }
    },
    extractFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the whitelist extracted
            var url = tinaviz.fileURL(data);
            window.location.assign( url );
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#importFile').addClass("ui-state-error", 1);
            $('#importFile').html( "error, please report the log file to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#importFile').removeClass("ui-state-disabled", 1);
            $('#importFile').html( "Launch" );
        },
        beforeSend: function() {
            $("#extractminoccs").removeClass("ui-state-error", 1);
            $('#importFile').removeClass("ui-state-error", 1);
            $('#importFile').addClass("ui-state-disabled", 1);
            $('#importFile').html( "please wait during extraction" );
            // add progress state notification
        }
    },
    postCooc: {
        success: function(data, textStatus, XMLHttpRequest) {
            console.log("postCooc success");
            // data contains a path to the whitelist extracted
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error", 1);
            $('#processCooc').html( "error, please report the log file to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#processCooc').removeClass("ui-state-disabled", 1);
            $('#processCooc').html( "Launch" );
        },
        beforeSend: function() {
            $("#whitelistfile").removeClass("ui-state-error", 1);
            $('#processCooc').removeClass("ui-state-error", 1);
            $('#processCooc').addClass("ui-state-disabled", 1);
            $('#processCooc').html( "please wait during cooccurrences processing" );
            // add progress state notification
        }
    },
    postGraph: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            $('#processCooc').html( "Loading macro view" );
            //tinaviz.clear();
            //switchTab( "macro" );
            var url = tinaviz.fileURL(data);
            tinaviz.readGraphAJAX("macro", url);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error", 1);
            $('#processCooc').html( "error, please report the logs to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#processCooc').removeClass("ui-state-disabled", 1);
            $('#processCooc').html( "Launch" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $('#processCooc').removeClass("ui-state-error", 1);
            $('#processCooc').addClass("ui-state-disabled", 1);
            $('#processCooc').html( "please wait during graph exportation" );
            // add progress state notification
        }
    },
    getWhitelist: {
        success: function(data, textStatus, XMLHttpRequest) {
            var url = tinaviz.fileURL(data);
            window.location.assign( url );
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#exportWhitelist').addClass("ui-state-error", 1);
            $('#exportWhitelist').html( "error, please report the logs to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#exportWhitelist').removeClass("ui-state-disabled", 1);
            $('#exportWhitelist').html( "Export a whitelist" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#whitelistlabel").removeClass("ui-state-error");
            $("#extractminoccs").removeClass("ui-state-error");
            $('#exportWhitelist').removeClass("ui-state-error", 1);
            $('#exportWhitelist').addClass("ui-state-disabled", 1);
            $('#exportWhitelist').html( "please wait during whitelist exportation" );
            // add progress state notification
        }
    }
};
/* Setting Tinasoft observers */
/*
var ObserverServ = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
// Observers registering
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runImportFile_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportCorpora_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportCorpora_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCoocGraph_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runProcessCoocGraph_running_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportGraph_finish_status" , false );
ObserverServ.addObserver ( tinasoftTaskObserver , "tinasoft_runExportGraph_running_status" , false );
*/
