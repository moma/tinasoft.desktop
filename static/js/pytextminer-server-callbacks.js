/*
    Copyright (C) 2009-2011 CREA Lab, CNRS/Ecole Polytechnique UMR 7656 (Fr)
#
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
#
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
#
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


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
            $('#importFile').button("enable");
            $('#importFile').html( "Launch" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#importfilepath").removeClass("ui-state-error", 1);
            $("#importdatasetid").removeClass("ui-state-error", 1);
            $("#extractminoccs").removeClass("ui-state-error", 1);
            $('#importFile').removeClass("ui-state-error", 1);
            $('#importFile').button("disable");
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
            $('#indexFile').addClass("ui-state-error");
            $('#indexFile').button( "option", "label", ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#indexFile').button("enable");
            $('#indexFile').button( "option", "label", "launch" );
            /* Fetch data into table */
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $("#indexfilepath").removeClass("ui-state-error");
            $("#indexdatasetid").removeClass("ui-state-error");
            $("#indexwhitelistpath").removeClass("ui-state-error");
            $('#indexFile').removeClass("ui-state-error");
            $('#indexFile').button("disable");
            $('#indexFile').button( "option", "label", "indexation in progress" );
        }
    },

    postGraph: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            $('#processCooc').button( "option", "label", "loading graph viz" );
            loadGraph(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error");
            $('#processCooc').button( "option", "label", ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#processCooc').button("enable");
            $('#processCooc').button( "option", "label", "launch" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $('#processCooc').removeClass("ui-state-error");
            $('#processCooc').button("disable");
            $('#processCooc').button( "option", "label", "generating the graph" );
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
            $("#exit_server").addClass('ui-state-error');
        }
    },

    getLog: {
        success: function(data, textStatus, XMLHttpRequest) {
            if (data.length == 0) {
                $.doTimeout( 5000, function(){
                    TinaService.getLog(TinaServiceCallback.getLog);
                });
            }
            else {
                for (line in data) {
                    console.log("pytextminer server : " + data[line]);
                }
                $.doTimeout( 1000, function(){
                    TinaService.getLog(TinaServiceCallback.getLog);
                });
            }
        },
        complete: function(XMLHttpRequest, textStatus) {
        },
        beforeSend: function() {
        },
        error: function(XMLHttpRequest, textStatus) {
            console.error("error getting server's log, please open tinasoft-log.txt file instead");
        },
    }
};
