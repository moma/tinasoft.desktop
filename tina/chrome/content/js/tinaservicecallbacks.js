
/* Tinasoft Server callback */
var SERVER_URL= "http://localhost:8888";
var TinaServiceCallback = {
    importFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains json encoded list of duplicate documents found
            displayDuplicateDocs( data );
            $( "#corpora_table" ).toggleClass("ui-state-highlight", 1);
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
            //$("#corpora_table").clone().appendTo("#graph_table");
        },
        beforeSend: function() {
            $('#importFile').removeClass("ui-state-error", 1);
            $('#importFile').addClass("ui-state-disabled", 1);
            $('#importFile').html( "please wait during import" );
            // add progress state notification
        }
    },
    extractFile : {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the whitelist extracted
            var parts = data.split("user/");
            var url = SERVER_URL + "/user/" + parts[1];
            window.location.assign( url );
            //document.load( SERVER_URL + "/user/" + parts[1] );
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
            $('#importFile').addClass("ui-state-disabled", 1);
            $('#importFile').html( "please wait during extraction" );
            // add progress state notification
        }
    },
    processCooc: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the whitelist extracted
            alert(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error", 1);
            $('#processCooc').html( "error, please report the log file to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            alert("send here exportGraph");
            //$('#processCooc').removeClass("ui-state-disabled", 1);
            //$('#processCooc').html( "Launch" );
        },
        beforeSend: function() {
            $('#processCooc').removeClass("ui-state-error", 1);
            $('#processCooc').addClass("ui-state-disabled", 1);
            $('#processCooc').html( "please wait during cooccurrences processing" );
            // add progress state notification
        }
    },
    exportGraph: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            alert(data);
            $('#processCooc').html( "Loading macro view" );
            tinaviz.clear();
            switchTab( "macro" );
            tinaviz.readGraphJava("macro", data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#processCooc').addClass("ui-state-error", 1);
            $('#processCooc').html( "error, please report the logs to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#processCooc').removeClass("ui-state-disabled", 1);
            $('#processCooc').html( "Produce a graph" );
            displayDataTable( "data_table" );
        },
        beforeSend: function() {
            $('#processCooc').removeClass("ui-state-error", 1);
            $('#processCooc').addClass("ui-state-disabled", 1);
            $('#processCooc').html( "please wait during graph exportation" );
            // add progress state notification
        }
    },
    exportWhitelist: {
        success: function(data, textStatus, XMLHttpRequest) {
            // data contains a path to the graph exported
            alert(data);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            $('#exportCorpora').addClass("ui-state-error", 1);
            $('#exportCorpora').html( "error, please report the logs to bugtracker" );
        },
        complete: function(XMLHttpRequest, textStatus) {
            $('#exportCorpora').removeClass("ui-state-disabled", 1);
            $('#exportCorpora').html( "Export a whitelist" );
        },
        beforeSend: function() {
            $('#exportCorpora').removeClass("ui-state-error", 1);
            $('#exportCorpora').addClass("ui-state-disabled", 1);
            $('#exportCorpora').html( "please wait during whitelist exportation" );
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
