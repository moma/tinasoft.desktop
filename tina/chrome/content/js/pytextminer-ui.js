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

/******************************************************************************
 * Functions displaying dynamic content
 *****************************************************************************/

/* Duplicate documents found after data set import */
function displayDuplicateDocs(data) {
    var div = $( "#duplicate_docs" )
    if (data.length == 0) {
        div.empty().hide();
    }
    else {
        div.empty().show();
        div.append( "<h3>duplicate documents found ("+ (data.length) +")</h3>" );
        for ( var i=0; i < data.length; i++ ) {
            div.append( "<p class='ui-state-active'>"+data[i]['id']+"<br/>"+data[i]['label']+"</p>" );
        }
    }
}

/*
 * Return the label part of a file name
 * File names normalized :
 * $DATE-$LABEL-$TYPE.$EXT
 */
function parseFileLabel(filename) {
    return filename.split("-")[1];
}


/*
 * displays the list of existing graphs
 * for a given <TR> and a dataset id
 */
function displayGraphColumn(corpora) {
    //console.log("displayListGraph : row = " + trid + " , dataset = "+ corpora);
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'graph_list_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();

    TinaService.getGraph(
        corpora.id,
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
                        var url = TinaService.fileURL($(this).attr('value'));
                        tinaviz.readGraphAJAX("macro", url);
                    });
                    ol.append(button);
                }
            }
        }
    );
}

function displayWhitelistColumn(corpora) {
    var trid = corpora['id'] + "_tr";
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'whitelist_' + trid
    tr.append("<td>"
        + "<ol id='"
        + olid + "' >"
        + "</ol></td>"
    );
    var ol = $( "#" + olid  ).empty();
    TinaService.getWalkUserPath(
        corpora.id,
        "whitelist",
        {
            success: function(list) {
                for ( var i=0; i < list.length; i++ ) {
                    var path_comp = list[i].split(/\/|\\/).reverse();
                    var label = $("<li>"
                        + parseFileLabel(path_comp[0])
                        + "&#09;"
                        + "</li>"
                    ).draggable({
                        helper: "clone",
                    })
                    .data("whitelistpath",list[i]);
                    var open_button = $("<div></div>").attr("id",list[i]);
                    open_button.button({
                        text: false,
                        icons: {
                            primary: 'ui-icon-pencil'
                        }
                    }).click( function(eventObject) {
                        alert("make sure you save this file after editing");
                        var url = getFileUrl($(this).attr("id"));
                        window.location.assign( url );
                    });
                    label.append(open_button);
                    ol.append(label);
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
    var tr = $( "#" +trid );
    // corpus list cell
    var olid = 'selectable_corpus_' + trid
    tr.append("<td>"
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
            //console.log ( Cache.getValue( "last_selected_periods", "", 1 ) );
        },
    });
}

function displayDatasetRow(list) {
    var tbody = $("#data_table > table > tbody");
    for ( var i=0; i<list.length; i++ ) {
        // populates each row
        var dataset_id = list[i];
        var trid = dataset_id + "_tr";
        var tr = $("<tr id='"+dataset_id+"_tr'></tr>")
            //.addClass("ui-widget-content")
            .append( $("<td></td>").html(dataset_id) )
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

var initPytextminerUi = function() {
    /*
     * Initialize Pytextminer UI
     */
    /* resets cache vars */
    var corporaAndPeriods = Cache.setValue( "last_selected_periods", {} );
    var dupldoc = $( "#duplicate_docs" ).empty().hide();
    $("#duplicate_docs_toggle").button({
        icons: {primary:'ui-icon-lightbulb'},
        text: true,
    })
    .click(function(event) {
        $("#duplicate_docs").toggle("fold");
    });
    // hide by default all submit forms
    $("#import_form").hide();
    $("#toggle_import_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false,
    })
    .click(function(event) {
        $("#import_form").toggle("fold");
    });

    $("#index_form").hide();
    $("#toggle_index_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false,
    })
    .click(function(event) {
        $("#index_form").toggle("fold");
    });

    $("#processcooc_form").hide();
    $("#toggle_processcooc_form").button({
        icons: {primary:'ui-icon-plus'},
        text: false,
    })
    .click(function(event) {
        $("#processcooc_form").toggle("fold");
    });

    $("#toggle_working_session").button({
        icons: {primary:'ui-icon-carat-2-e-w'},
        text: true,
        label: "work session manager"
    })
    .click(function(event) {
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
        $("#about_tinasoft").dialog({modal: true});
    });
    /* Fetch data into table */
    displayDataTable("data_table");

    /* Init droppable elements */
    $(".whitelistdroppable").droppable({
        activeClass: "ui-state-default",
        hoverClass: "ui-state-hover",
        drop: function(event, ui) {
            $(this).empty();
            $(this).append(ui.draggable.text());
            $(this).data("whitelistpath", ui.draggable.data("whitelistpath"));
        }
    }).html("<p>drag and drop here a white list</p>");
};


$(document).ready(function() { initPytextminerUi(); });
