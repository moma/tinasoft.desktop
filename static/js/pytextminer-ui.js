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

/******************************************************************************
 * Functions displaying dynamic content
 *****************************************************************************/

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
            div.append( "<p class='ui-state-active'>"
                +htmlEncode(data[i]['id'])
                +"<br/>"
                +htmlEncode(data[i]['label'])
                +"</p>"
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
    var url = TinaService.httpURL(data);
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
 * Adds the whitelist files list to the dataset row (using the same TR id)
 */
function displayWhitelistColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" + trid );
    // corpus list cell
    var olid = 'whitelist_' + trid
    tr.append("<td class='ui-widget-content'>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    TinaService.getWalkUserPath(
        corpora['id'],
        "whitelist",
        {
            success: function(list) {
                for ( var i=0; i < list.length; i++ ) {
                    // gets the filename from a path (first position of the list)
                    var path_comp = list[i].split(/\/|\\/).reverse();
                    var whitelist_item = $("<li title='drag and drop to select this whitelist'>"
                        + parseFileLabel(path_comp[0])
                        + "&#09;"
                        + "</li>"
                    ).draggable({
                        helper: "clone"
                    })
                    .data("whitelistpath", list[i]);
                    var edit_link = $("<a href='#' title='click to open in an external software'></a>")
                    .button({
                        text: false,
                        icons: {
                            primary: 'ui-icon-pencil'
                        }
                    })
                    .attr("id",list[i])
                    .click( function(eventObject) {
                        editUserFile($(this).attr("id"));
                    });
                    whitelist_item.append(edit_link);
                    ol.append(whitelist_item);
                }
            }
        }
    );
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

function displayDatasetRow(list) {
    var tbody = $("#data_table > table > tbody");
    for ( var i=0; i<list.length; i++ ) {
        // populates and attach table rows
        var dataset_id = list[i];
        var trid = dataset_id + "_tr";
        var delete_dataset = $("<a href='#'></a>")
            .button({
                text: false,
                icons: {
                    primary: 'ui-icon-trash'
                }
            })
            .attr("title", "click to definitely remove all dataset's files")
            //.data("id", dataset_id)
            .click( dataset_id ,function(eventObject) {
                $("#dialog-confirm-delete-dataset").dialog({
			resizable: false,
                        position: ['center','top'],
			modal: true,
			buttons: {
                            'Delete': function(eventObject) {
                                TinaService.deleteDataset(dataset_id, TinaServiceCallback.deleteDataset);
                                $(this).dialog('close');
                            },
                            Cancel: function() {
                                $(this).dialog('close');
                            }
			}
		});
            });
        var edit_dataset = $("<a href='#'></a>")
            .button({
                text: false,
                icons: {
                    primary: 'ui-icon-pencil'
                }
            })
            .attr("title", "click to edit dataset's contents")
            .data("id", dataset_id)
            .click( function(eventObject) {
                // fills the form
                var id = $(this).data("id");
                $(".fold_form:visible:not(#editdocument_form)").hide("fold");
                $("#editdocument_form").toggle("fold");
            });
            
        var tr = $("<tr id='"+trid+"'></tr>")
            .append( $("<td class='ui-widget-content'></td>").append(delete_dataset).append(edit_dataset) )
            .append( $("<td class='ui-widget-content'></td>").html(htmlEncode(dataset_id)) )
        ;
        
        tbody.append(tr);
        TinaService.getDataset(dataset_id, {
            success: function(dataset) {
                if(dataset != "") {
                    displayWhitelistColumn( dataset );
                    displayPeriodColumn( dataset );
                    displayGraphColumn( dataset );
                }
            }
        });
    }
}

/*
 * Gets the list of datasets
 * and populates a table
 * with corpus and graphs
 */
function displayDataTable(parent_div) {
    // populates each row
    var tbody = $( "<tbody></tbody>" );
    $("#"+parent_div+" > table > tbody").remove();
    $("#"+parent_div+" > table").append(tbody);
    TinaService.getDatasetList({
        success: function(list) {
            displayDatasetRow(list);
        }
        //Cache.setValue("last_data_table", table);
    });
}

function loadSourceFiles(select_id) {
    var select = $(select_id);
    TinaService.getWalkSourceFiles({
        success: function(list) {
            select.empty().append($("<option value=''></option>"));
            for ( var i=0; i < list.length; i++ ) {
                select.append($("<option value='"+list[i]+"'>"+htmlEncode(list[i])+"</option>"))
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

    // hide by default all submit forms
    $("#editdocument_form").hide();
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

    $("#toggle_working_session").button({
        icons: {primary:'ui-icon-carat-2-e-w'},
        text: true,
        label: "work session manager"
    })
    .click(function(event) {
        $(".fold_form:visible").hide("fold");
        $("#data_table").toggle("fold");
        // TODO : display current state of the session in the button label
    });

    $("#about_tinasoft").hide();

    $("#toggle_about").button({
        icons: {primary:'ui-icon-info'},
        text: true,
        label: "about tinasoft"
    })
    .click(function(event) {
        $("#about_tinasoft").dialog(
            {
                modal: true,
                position: ['right','top'],
                maxWidth: 300
            }
        );
    });

    $("#exit_server")
        .button({
            icons: { primary:'ui-icon-power' },
            text: true,
            label: "shutdown server"
        }).click(function(event) {
            TinaService.exit(TinaServiceCallback.exit);
        });
    $("#exit_server").button("enable");

    /* Fetch data into table */
    displayDataTable("data_table");

    /* Init droppable elements */
    $(".whitelistdroppable").droppable({
        activeClass: "ui-state-default",
        hoverClass: "ui-state-hover",
        drop: function(event, ui) {
            $(this).empty();
            $(this).append("chosen whitelist : " + ui.draggable.text());
            $(this).data("whitelistpath", ui.draggable.data("whitelistpath"));
        }
    }).html("<p>drag and drop here a white list</p>");

    $(".periodselectable").html("<p>select periods<br/>(ctrl key for multiple selection)</p>");
    /* Init every upload file handler */
    /*var extract_input_upload = new UploadFileClass("#importfilepath", TinaService.SERVER_URL + "/uploadpath");
    $("#importfilepath").get(0).addEventListener( "change", extract_input_upload.handleDrop, false );*/

    loadSourceFiles("#importfilepath");
    loadSourceFiles("#indexfilepath");

    $("#graphalpha").spinner();
    $("#graph-ngrams-edges-min").spinner();
    $("#graph-ngrams-edges-max").spinner();
    $("#graph-documents-edges-min").spinner();
    $("#graph-documents-edges-max").spinner();
    $("#graph-ngrams-nodes-min").spinner();
    $("#graph-ngrams-nodes-max").spinner();
    $("#graph-documents-nodes-min").spinner();
    $("#graph-documents-nodes-max").spinner();
    $("#extractminoccs").spinner();
    $(".ui-spinner-buttons").height(12);
    $(".ui-spinner-button").height(6);

    $("#updateDocument")
        .button({
            icons: { primary:'ui-icon-check' },
            text: true,
            label: "update document indexation"
        }).click(function(event) {
            TinaService.postWhitelistUpdate(TinaServiceCallback.postWhitelistUpdate);
        });
    $("#updateDocument").button("enable");

    TinaService.getLog(TinaServiceCallback.getLog);
};


$(document).ready(function() { initPytextminerUi(); });
