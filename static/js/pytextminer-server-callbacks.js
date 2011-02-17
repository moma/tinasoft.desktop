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


var ERROR_MSG = "error, please report the logs to the maintainers";

var SERVER_URL= "http://localhost:8888";

/*
 * Pytextminer server callbacks
 */
var TinaServiceCallback = {

    extractFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the whitelist extracted
            editUserFile(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#extractFileButton').addClass("ui-state-error");
            $('#extractFileButton').button( "option", "label", ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#extractFileButton').button("enable");
            $('#extractFileButton').button( "option", "label", "Launch" );
            displayDataTable( "sessions" );
        },
        beforeSend: function() {
            $("#importfilepath").removeClass("ui-state-error");
            //$("#importdatasetid").removeClass("ui-state-error");
            $("#extractminoccs").removeClass("ui-state-error");
            $('#extractFileButton').removeClass("ui-state-error");
            $('#extractFileButton').button("disable");
            $('#extractFileButton').button( "option", "label", "please wait during extraction" );
        }
    },

    postFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains json encoded list of duplicate documents found
            displayDuplicateDocs( data );
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#indexFileButton').addClass("ui-state-error");
            $('#indexFileButton').button( "option", "label", ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#indexFileButton').button("enable");
            $('#indexFileButton').button( "option", "label", "launch" );
            /* Updates data into table */
            displayDataTable("sessions");
        },
        beforeSend: function() {
            $("#indexfilepath").removeClass("ui-state-error");
            $("#indexdatasetid").removeClass("ui-state-error");
            $("#indexwhitelistpath").removeClass("ui-state-error");
            $('#indexFileButton').removeClass("ui-state-error");
            $('#indexFileButton').button("disable");
            $('#indexFileButton').button( "option", "label", "indexation in progress" );
        }
    },

    postGraph: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            $('#generateGraphButton').button( "option", "label", "loading graph visualization" );
            loadGraph(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#generateGraphButton').addClass("ui-state-error");
            $('#generateGraphButton').button( "option", "label", ERROR_MSG );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#generateGraphButton').button("enable");
            $('#generateGraphButton').button( "option", "label", "launch" );
            displayDataTable( "sessions" );
        },
        beforeSend: function() {
            $('#generateGraphButton').removeClass("ui-state-error");
            $('#generateGraphButton').button("disable");
            $('#generateGraphButton').button( "option", "label", "generating the graph" );
            // add progress state notification
        }
    },

    exit: {
        success: function(data, textStatus, XMLHttpRequest) {},
        complete: function(XMLHttpRequest, textStatus) {},
        beforeSend: function() {
            $("#exit_server").button("disable");
            $("#exit_server").button("option", "icons", { primary: "ui-icon-alert" });
            $("#exit_server").addClass('ui-state-error');
            $('#indexFileButton').button("disable");
            $('#generateGraphButton').button("disable");
            $('#extractFileButton').button("disable");
        },
        error: function() {
            console.log("TinaService.exit error callback, ignored");
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
        complete: function(XMLHttpRequest, textStatus) {},
        beforeSend: function() {},
        error: function(XMLHttpRequest, textStatus) {
            console.log("error getting server's log, please open tinasoft-log.txt file instead");
        }
    },

    deleteDataset: {
        success: function(data, textStatus, XMLHttpRequest) {},
        complete: function(XMLHttpRequest, textStatus) {
            /* Updates data into table */
            displayDataTable("sessions");
        }
    }
};
