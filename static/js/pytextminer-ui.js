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


/*
 * Displays all duplicate documents
 * returned after data set indexation
 */
function displayDuplicateDocs(data) {
    var div = $( "#duplicate_docs" )
    if (data.length == 0) {
        div.empty().hide();
    }
    else {
        div.empty().show();
        div.append( "<h3>duplicate documents found ("+ (data.length) +")</h3>" );
        for ( var i=0; i < data.length; i++ ) {
            div.append(
                $("<p></p>").addClass('ui-state-active')
                    .text( (data[i]['id'])+ " - " + data[i]['label'])
                );
        }
    }
}

/*
 * Return the label part of a file name
 * File names normalized :
 * $DATE-$LABEL-$TYPE.$EXT
 */
function parseFileLabel(filename) {
    return htmlEncode(filename.split("-")[0]);
}

/*
 * transforms an absolute path ("/home/toto/tinasoft/user/etc/") to an file:// url,
 * compatible with windows paths
 * for use in AJAX Request
 */
function getFileURL(absPath) {
    if ( /\\/.test(absPath) == true ) {
        return "file:///"+absPath;
    }
    return "file://"+absPath;
}

/*
 * Common function sending signal to the server that externally opens a file
 */
function editUserFile(path) {
    var url = getFileURL(path);
    alert("WHITE LIST EDITION\n"
    +"Tips :\n"
    +"1- choose a spreadsheet or text editor to open the requested CSV file\n"
    +"2- choose keyphrases you want to index by writing w in the status column\n"
    +"3- make sure you save this file in place when finished\n"
    +"We recommend using OpenOffice.org Calc, or any other spreadsheet editor than can handle CSV file without changing the native encoding and formatting (default utf-8, comma separated values, double-quoted text cells)\n"
    +"Warning : if nothing happens  after you close this window (e.g. blowser security blocking), please retry then copy and paste this URL in your browser's address bar :\n"+ url
    );
    TinaService.getOpenUserFile(url);
}

/*
 * Commen function to ask Tinaviz to open a graph
 */
function loadGraph(data) {

    //var url = TinaService.httpURL(data);
    // HACK FOR THE DEMO: STATIC GEXF FILE
    var url = "http://localhost:8888/user/current.gexf";
    // HACK
    tinaviz.open({
        view: "macro",
        clear: true, // clean everything before adding the graph
        url: url
    });
}

/*
 * displays the list of existing graphs
 * for a given <TR> and a dataset id (using the same TR id)
 */
function displayGraphColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'graph_list_' + trid
    tr.append("<td class='ui-widget-content'>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();

    TinaService.getWalkUserPath(
        corpora['id'],
        "gexf",
        {
            success: function(graphList) {
                for ( var i=0; i < graphList.length; i++ ) {
                    var path_comp = graphList[i].split(/\/|\\/).reverse();
                    var button = $("<button class='ui-state-default ui-corner-all' value='"
                        + graphList[i]
                        + "'>"
                        + parseFileLabel(path_comp[0])
                        + "</button><br/>"
                    ).click(function(event) {
                        loadGraph($(this).attr('value'));
                    });
                    ol.append(button);
                }
            }
        }
    );
}

/*
 * Adds the source files list to the dataset row (using the same TR id)
 */
function displaySourcesColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" + trid );
    var olid = 'sources_' + trid
    tr.append("<td class='ui-widget-content'>"
        + "<ol id='"
        + olid + "' class='sortable_ol'>"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    for ( var sourcefile in corpora['edges']['Source']) {
        var path = corpora['edges']['Source'][sourcefile];
        var item = $("<li></li>")
            .text(sourcefile)
            .addClass("sortable_li")
            .addClass("ui-widget-content")
            .attr("title","sources indexed within this session");

        var edit_link = $("<a href='#' title='click to edit in an external software'></a>")
        .button({
            text: false,
            icons: {
                primary: 'ui-icon-pencil'
            }
        }).attr("path", path).click( function(eventObject) {
            editUserFile($(this).attr("path"));
        });
        item.append(edit_link);
        ol.append(item);
    }
    alphabeticJquerySort(ol, "li", "");
    ol.sortable();
    ol.disableSelection();
}

/*
 * Adds the whitelist files list to the dataset row (using the same TR id)
 */
function displayWhitelistColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" + trid );
    // corpus list cell
    var olid = 'whitelist_' + trid
    tr.append("<td class='ui-widget-content'>"
        + "<ol id='"
        + olid + "' class='sortable_ol' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    var whitelists = Cache.setValue('whitelists', {});
    for ( var wllabel in corpora['edges']['Whitelist']) {
        var path = corpora['edges']['Whitelist'][wllabel];
        var whitelist_item = $("<li></li>")
            .text(wllabel)
            .addClass("sortable_li").addClass("ui-widget-content")
            .attr("title", 'click on the pencil button the edit the whitelist');

        if (whitelists[wllabel]!==undefined) {
            var edit_link = $("<a href='#' title='click to edit in an external software'></a>")
            .button({
                text: false,
                icons: {
                    primary: 'ui-icon-pencil'
                }
            }).attr("path", path).click( function(eventObject) {
                editUserFile($(this).attr("path"));
            });

            var delete_link = $("<a href='#' title='click to remove'></a>")
            .button({
                text: false,
                icons: {
                    primary: 'ui-icon-trash'
                }
            }).attr("path", path).click( function(eventObject) {
                TinaService.deleteWhitelist($(this).attr("path"), TinaServiceCallback.deleteWhitelist);
            });
            whitelist_item.append(edit_link).append(delete_link);
        }
        else {
            whitelist_item.addClass("ui-state-error");
        }
        ol.append(whitelist_item);
    }
    alphabeticJquerySort(ol, "li", "");
    ol.sortable();
    ol.disableSelection();
}

/*
 * Displays the list of corpus (selectable buttons)
 * for a given corpora and a <TR>
 */
function displayPeriodColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" + trid );
    // corpus list cell
    var olid = 'selectable_corpus_' + trid
    tr.append("<td class='ui-widget-content'>"
        + "<ol id='"
        + olid + "' class='selectable'>"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty()
    for ( var id in corpora['edges']['Corpus'] ) {
        ol.append("<li id='"
            + id
            + "' class='ui-widget-content ui-state-default'>"
            + id
        +"</li>");
    }

    selectableCorpusInit( ol, corpora );
}

/*
 * Resets any li from any 'selectable' class div
 */
function selectableReset() {
    /* Resets any li from any 'selectable' class div */
    $("li",".selectable").each(function(){
        var li = $(this);
        li.removeClass('ui-state-active');
    });
}

/*
 * Create selectable buttons on a list of corpus
 */
function selectableCorpusInit( ol, corpora ) {
    ol.selectable({
        stop: function(){
            selectableReset();
            corporaAndPeriods = {};
            corporaAndPeriods[corpora.id] = [];
            $(".ui-selected",this).each(function(){
                var selected_li = $(this);
                selected_li.addClass('ui-state-active');
                corporaAndPeriods[corpora.id].push(( selected_li.html() ));
            });
            Cache.setValue( "last_selected_periods", corporaAndPeriods );
            $(".periodselectable").html(
                "<p>please select periods<br/>(ctrl key for multiple selection)</p>"
                );
            var selecttext = $("<p></p>");
            selecttext.append("current selection in data set "+corpora.id+" :");
            var plist = $("<ul></ul>");
            for (var selperiod in corporaAndPeriods[corpora.id]) {
                var li = $("<li></li>").append(corporaAndPeriods[corpora.id][selperiod]);
                plist.append(li);
            }
            selecttext.append(plist);
            $(".periodselectable").empty().append(selecttext);
        }
    });
}

/*
 * Displays the list of all available whitelists
 */
function displayWhitelists(div_id){

    var div = $( "#"+div_id ).empty();
    $( "#"+div_id ).addClass("sortable_ol");
    TinaService.getWalkUserPath(
        "None",
        "whitelist",
        {
            success: function(list) {
                var labels = [];
                var whitelists = {};
                for ( var i=0; i < list.length; i++ ) {
                    // gets the filename from a path (first position of the list)
                    var path_comp = list[i].split(/\/|\\/).reverse();
                    var label = parseFileLabel(path_comp[0]);
                    whitelists[label] = list[i];
                    if( /csv$/.test(list[i]) == false )
                        continue;

                    var whitelist_item = $("<li></li>")
                        .text(label)
                        .addClass("sortable_li")
                        .addClass("ui-widget-content")
                        .attr("title","click on the pencil button the edit the whitelist");

                    labels.push(label);
                    
                    var edit_link = $("<a href='#' title='click to open in an external software'></a>").button({
                        text: false,
                        icons: {
                            primary: 'ui-icon-pencil'
                        }
                    }).attr("path",list[i]).click( function(eventObject) {
                        editUserFile($(this).attr("path"));
                    });
                    
                     var delete_link = $("<a href='#' title='click to remove'></a>").button({
                        text: false,
                        icons: {
                            primary: 'ui-icon-trash'
                        }
                    }).attr("path", list[i]).click( function(eventObject) {
                        TinaService.deleteWhitelist($(this).attr("path"), TinaServiceCallback.deleteWhitelist)
                    });

                    whitelist_item.append(edit_link).append(delete_link);
                    div.append(whitelist_item);
                }
                alphabeticJquerySort(div, "li", "");
                div.sortable();
                div.disableSelection();
                Cache.setValue('whitelists',whitelists);
                $("#index_whitelist").autocomplete({ source: labels });
            }
        }
    );
}

function displayDeleteDatasetDialog(dataset_id) {
    $("#dialog-confirm-delete-dataset").dialog({
        title: "Erase "+dataset_id+" ?",
        resizable: false,
        position: ['center','top'],
        modal: true,
        buttons: {
            'Delete' : function(eventObject) {
                TinaService.deleteDataset($(this).data("dataset_id"), TinaServiceCallback.deleteDataset);
                $(this).dialog('close');
            },
            Cancel: function() {
                $(this).dialog('close');
            }
        }
    }).data("dataset_id", dataset_id);
}

function displayDatasetRow(parent_div_id, dataset_id) {
    if (dataset_id=='create'){
        $(".fold_form:visible:not(#index_form)").hide("fold");
        $("#index_form").show("fold");
        return;
    }
    if (dataset_id==''){
        return;
    }
    var tbody = $("#"+parent_div_id+" > div > table > tbody");
    tbody.empty();
    // populates and attach table rows
    var trid = dataset_id + "_tr";

    var delete_dataset = $("<a href='#'></a>")
        .button({
            text: false,
            icons: {
                primary: 'ui-icon-trash'
            }
        })
        .attr("title", "click to definitely remove all dataset's files")
        .data("dataset_id", dataset_id)
        .click( function(eventObject) {
            displayDeleteDatasetDialog($(this).data("dataset_id"));
        });

    var edit_dataset = $("<a href='#'></a>")
        .button({
            text: false,
            icons: {
                primary: 'ui-icon-pencil'
            }
        })
        .attr("title", "click to edit dataset's contents")
        .data("dataset_id", dataset_id)
        .click( function(eventObject) {
            datasetEditor.toggleEditionForm($(this).data("dataset_id"));
        });


    var update_dataset = $("<a href='#'></a>")
        .button({
            text: false,
            icons: {
                primary: 'ui-icon-refresh'
            }
        })
        .attr("id", dataset_id+"_update_button")
        .attr("title", "click to update dataset's database")
        .data("dataset_id", dataset_id)
        .data("NGramFormQueue", { "add": [], "delete": [] })
        .click( function(eventObject) {
            datasetEditor.submitUpdateDataset($(this));
        })
        .hide()
        .qtip({
            content: {
                text: ""
            },
            hide: {
                delay : 1000
            }
        });

    // appends action buttons to the dataset's row
    var tr = $("<tr class='ui-widget-content' id='"+trid+"'></tr>")
        .append( $("<td class='ui-widget-content'></td>")
            .append(delete_dataset)
            .append(edit_dataset)
            .append(update_dataset)
        )
        .append( $("<td class='ui-widget-content'></td>").text(dataset_id) );

    tbody.append(tr);
    TinaService.getDataset(dataset_id, {
        success: function(dataset) {
            if(dataset != "") {
                displayWhitelistColumn( dataset );
                displaySourcesColumn( dataset );
                displayPeriodColumn( dataset );
                displayGraphColumn( dataset );
            }
        }
    });
}

function displayDatasetSelect(parent_div_id, list){
    list.sort();
    var select = $("#dataset_select")
        .empty()
        .append(
            $('<option></option>')
        ).append(
            $('<option></option>').attr('value','create').text('new session')
        );
    for (var i=0; i<list.length; i++){
        var option = $('<option></option>').attr('value',list[i]).text(list[i]);
        select.append(option);
        if (Cache.getValue('dataset_id')==list[i]){
            option.attr("selected", "selected");
        }
    }
    select.change();
}

/*
 * Gets the list of datasets
 * and populates a table
 * with corpus and graphs
 */
function displayDataTable(parent_div_id) {
    displayWhitelists( "whitelist_items" );
    TinaService.getDatasetList({
        success: function(list) {
            displayDatasetSelect(parent_div_id, list);
            $("#importdatasetid").autocomplete({ source: list });
            $("#indexdatasetid").autocomplete({ source: list });
        }
    });
}

function loadSourceFiles(select_id) {
    var select = $(select_id);
    TinaService.getWalkSourceFiles({
        success: function(list) {
            select.empty().append($("<option value=''></option>"));
            for ( var i=0; i < list.length; i++ ) {
                select.append($("<option value='"+list[i]+"'>"+htmlEncode(list[i])+"</option>"));
            }
        }
    });
}

/*
 * Initialize Pytextminer UI
 */
var initPytextminerUi = function() {
    /* resets cache vars */
    var corporaAndPeriods = Cache.setValue( "last_selected_periods", {} );
    var dupldoc = $( "#duplicate_docs" ).empty().hide();

    $("#duplicate_docs_toggle").button({
        icons: {primary:'ui-icon-lightbulb'},
        text: true
    })
    .click(function(event) {
        $("#duplicate_docs").toggle("fold");
    });

    $("#dialog-confirm-delete-dataset").hide();

    $("#import_form").hide();
    $("#toggle_import_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false
    })
    .click(function(event) {
        $(".fold_form:visible:not(#import_form)").hide("fold");
        $("#import_form").toggle("fold");
    });

    $("#index_form").hide();
    $("#toggle_index_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false
    })
    .click(function(event) {
        $(".fold_form:visible:not(#index_form)").hide("fold");
        $("#index_form").toggle("fold");
    });

    $("#processcooc_form").hide();
    $("#toggle_processcooc_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false
    })
    .click(function(event) {
        $(".fold_form:visible:not(#processcooc_form)").hide("fold");
        $("#processcooc_form").toggle("fold");
    });

    $("#toggle_sessions").button({
        icons: {primary:'ui-icon-carat-2-e-w'},
        text: true,
        label: "session manager"
    }).click(function(event) {
        $(".fold_form:visible").hide("fold");
        $("#sessions").toggle("fold");
    });
    $("#toggle_sessions").button('enable');
    $("#sessions").hide();

    $("#about_tinasoft").hide();

    $("#toggle_about").button({
        icons: {primary:'ui-icon-info'},
        text: true,
        label: "about tinasoft"
    }).click(function(event) {
        $("#about_tinasoft").dialog(
            {
                modal: true,
                position: ['right','top'],
                maxWidth: 300
            }
        );
    });

    $("#exit_server").button({
        icons: { primary:'ui-icon-power' },
        text: true,
        label: "shutdown server"
    }).click(function(event) {
        TinaService.exit(TinaServiceCallback.exit);
    });
    $("#exit_server").button("enable");

    /* loads a session table */
    $("#dataset_select").change(function(event) {
        $("#dataset_select option:selected").each(function() {
            if ($(this).val()=='create'){
                Cache.setValue("dataset_id", $(this).val());
            }
            displayDatasetRow("sessions", $(this).val());
        })
    });
    /* Fetch data into session box */
    displayDataTable("sessions");

    $(".periodselectable").html("<p>select periods<br/>(ctrl key for multiple selection)</p>");
    /* Init every upload file handler */
    /*var extract_input_upload = new UploadFileClass("#importfilepath", TinaService.SERVER_URL + "/uploadpath");
    $("#importfilepath").get(0).addEventListener( "change", extract_input_upload.handleDrop, false );*/

    loadSourceFiles("#importfilepath");
    loadSourceFiles("#indexfilepath");

    /*$("#graphalpha").spinner();
    $("#graph-ngrams-edges-min").spinner();
    $("#graph-ngrams-edges-max").spinner();
    $("#graph-documents-edges-min").spinner();
    $("#graph-documents-edges-max").spinner();
    $("#graph-ngrams-nodes-min").spinner();
    $("#graph-ngrams-nodes-max").spinner();
    $("#graph-documents-nodes-min").spinner();
    $("#graph-documents-nodes-max").spinner();*/
    $("#extractminoccs").spinner();
    $(".ui-spinner-buttons").height(12);
    $(".ui-spinner-button").height(6);

    datasetEditor.init();

    $("#notification").notify();

    TinaService.getLog(TinaServiceCallback.getLog);

    TinaService.getDatasetList( { success: function(list) {
        $("#importdatasetid").autocomplete({ source: list });
        $("#indexdatasetid").autocomplete({ source: list });
    }});
};


$(document).ready(function() { initPytextminerUi(); });
