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

/*
 *  convert the serverDir to a file:// URI
 */
/*function getFileUrl(serverRelativePath) {

    var arr = serverRelativePath.split(/\/|\\/), i;
    // service producing URI
    var ios = Components.classes["@mozilla.org/network/io-service;1"].
                    getService(Components.interfaces.nsIIOService);

    // directory service
    var dirService = Components.classes["@mozilla.org/file/directory_service;1"].getService(Components.interfaces.nsIProperties);
    var tinavizDir = dirService.get("resource:app", Components.interfaces.nsIFile);
    var parentDir = tinavizDir.parent;
    for (i = 0; i < arr.length; ++i) {
        parentDir.append(arr[i]);
    }
    var ios = Components.classes["@mozilla.org/network/io-service;1"].
                    getService(Components.interfaces.nsIIOService);
    return ios.newFileURI(parentDir).spec;

}*/

var ERROR_MSG = "error, please report the logs to bug tracker";

/* Tinasoft Server callback */
var SERVER_URL= "http://localhost:8888";

var TinaServiceCallback = {

    extractFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the whitelist extracted
            editUserFile(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#importFile').addClass("ui-state-error", 1);
            $('#importFile').html( ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#importFile').removeClass("ui-state-disabled", 1);
            $('#importFile').html( "Launch" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#importfilepath").removeClass("ui-state-error", 1);
            $("#importdatasetid").removeClass("ui-state-error", 1);
            $("#extractminoccs").removeClass("ui-state-error", 1);
            $('#importFile').removeClass("ui-state-error", 1);
            $('#importFile').addClass("ui-state-disabled", 1);
            $('#importFile').html( "please wait during extraction" );
            // add progress state notification
        }
    },

    postFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains json encoded list of duplicate documents found
            displayDuplicateDocs( data );
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#indexFile').addClass("ui-state-error", 1);
            $('#indexFile').html( ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#indexFile').removeClass("ui-state-disabled", 1);
            $('#indexFile').html( "Launch" );
            /* Fetch data into table */
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#indexfilepath").removeClass("ui-state-error", 1);
            $("#indexdatasetid").removeClass("ui-state-error", 1);
            $("#indexwhitelistpath").removeClass("ui-state-error", 1);
            $('#indexFile').removeClass("ui-state-error", 1);
            $('#indexFile').addClass("ui-state-disabled", 1);
            $('#indexFile').html( "please wait during indexation" );
            // add progress state notification
        }
    },

    postGraph: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            $('#processCooc').html( "Loading macro view" );
            loadGraph(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error", 1);
            $('#processCooc').html( ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#processCooc').removeClass("ui-state-disabled", 1);
            $('#processCooc').html( "Launch" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $('#processCooc').removeClass("ui-state-error", 1);
            $('#processCooc').addClass("ui-state-disabled", 1);
            $('#processCooc').html( "please wait during graph production" );
            // add progress state notification
        }
    },

    exit: {
        success: function(data, textStatus, XMLHttpRequest) {
        },
        complete: function(XMLHttpRequest, textStatus) {
        },
        beforeSend: function() {
            $("#exit_server").button("disable");
            $("#exit_server").button("option", "icons", { primary: "ui-icon-alert" });
            $("#exit_server").addClass('ui-state-error')
        }
    }
};
