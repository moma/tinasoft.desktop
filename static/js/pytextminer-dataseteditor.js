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
* Handles the dataset editor logic and display
*/

var datasetEditor = {

    dataset_id: undefined,
    dataset_needs_update: false,

    init: function() {
        var self = this;
        // hide by default all submit forms
        $("#editdocument_form").hide();
        $("#editdataset_corpus").change(function(event) {
            $("#editdataset_corpus option:selected").each(function() {
                TinaService.getCorpus(
                    datasetEditor.dataset_id,
                    $(this).val(),
                    { success: self.displayDocumentSelect }
                );
            });
        });
        $("#editdataset_document").change(function(event) {
            $("#editdataset_document option:selected").each(function() {
                TinaService.getDocument(
                    datasetEditor.dataset_id,
                    $(this).val(),
                    { success: self.populateDocumentForm }
                );
            })
        });
        /*$
        $.dynaCloud.max = 100;
        $.dynaCloud.scale = 2;
        $.dynaCloud.single = false;*/
    },

    populateDocumentForm: function(documentObj, textStatus, XMLHttpRequest) {
        var self = this;
        var tbody = $("#editdocument_table > tbody");
        tbody.empty();

        var content = $("<span id='document_to_edit'></span>");
        content.data("documentObj", documentObj);

        for (var i=0; i < documentObj['target'].length; i++) {
            content.append($("<b></b>").html(documentObj['target'][i]+"&nbsp;:"));
            content.append( "&nbsp;&nbsp;" + documentObj[documentObj['target'][i]] + "<br/>" );
        }
        var keywords = $("<span id='document_keywords' title='user defined keyphrases'><b id='document_keywords_title'></b></span>");
        var queued_keywords = $("<span id='document_queued_keywords'><b id='document_queued_keywords_title'></b></span>");
        tbody.append(
            $("<tr class='ui-widget-content'></tr>")
                .append( $("<td></td>").append( $("<p id='display_document_object'></p>").append( content ).append( keywords ).append(queued_keywords) ) )
                .append(
                    $("<td class='ui-widget-content ui-corner-all'></td>")
                        .css({ width: 400 })
                        /*.append($("<h4>highest frequency suggestions</h4>")).append($("<p id='dynacloud'></p>"))*/
                        .append(
                            $("<p></p>")
                                .append(
                                    $("<input type='text' id='add_document_keyword'></input>")
                                        .autocomplete({
                                            source: documentObj['content'].split(" ")
                                        })
                                )
                                .append(
                                    $("<button></button>").button({
                                        icons: { primary:'ui-icon-arrowrefresh-1-e' },
                                        text: true,
                                        label: "add a keyphrase"
                                    }).click(function(event) {
                                        datasetEditor.pushAddKeyword($("#add_document_keyword").val());
                                    })
                                )
                        )
                )
        );
        //$("#document_to_edit").dynaCloud("#dynacloud");
        var ngrams = Object.keys(documentObj['edges']['NGram']);
        var total_ngrams =ngrams.length;
        for (var ngid in documentObj['edges']['NGram']) {
            if (total_ngrams==0){
                TinaService.getNGram(
                    datasetEditor.dataset_id,
                    ngid,
                    { success: datasetEditor.highlightNGramFormFinal }
                );
            }
            else {
                TinaService.getNGram(
                    datasetEditor.dataset_id,
                    ngid,
                    { success: datasetEditor.highlightNGramForm }
                );
            }
            total_ngrams--;
        }
        datasetEditor.highlightToBeAdded();
    },

    searchAndReplaceNGrams: function(ngramObj, textStatus, XMLHttpRequest){
        var htmlString = $("#document_to_edit")[0].innerHTML;
        for (var form_words in ngramObj['edges']['label']) {
            var words = form_words.split(" ");
            var pattern = new RegExp("((<span class='[^']' dbid='[^']'>)|(\\b)|(<\/span>))"+words.join("((<\/span>)*( )(<span class='[^']' dbid='[^']'>)*)")+"((\\b)|(<\/span>)|(<span class='[^']' dbid='[^']'>))", 'gi');
            var test = pattern.test(htmlString);
            if(test == false){
                datasetEditor.displayDocumentKeyword(form_words);
            }
            else {
                $("#document_to_edit")[0].innerHTML = htmlString.replace( pattern, "<span class='highlight' dbid='"+ngramObj['id']+"'>$&</span>" );
            }
        }
    },

    highlightNGramForm: function(ngramObj, textStatus, XMLHttpRequest) {
        datasetEditor.searchAndReplaceNGrams(ngramObj, textStatus, XMLHttpRequest);
    },

    HighlghtNGramFormFinal: function(ngramObj, textStatus, XMLHttpRequest) {
        datasetEditor.searchAndReplaceNGrams(ngramObj, textStatus, XMLHttpRequest);
        datasetEditor.attachNGramEditor($("span.highlight"));
        datasetEditor.highlightToBeDeleted($("span.highlight"));
        datasetEditor.highlightToBeAdded();
    },

    displayDocumentKeyword: function(keyword) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var list_of_keywords = $("#document_keywords");

        if (documentObj.edges.keyword[keyword] !== undefined) {
            list_of_keywords.append(
                "<span class='doc_keyword'>"+keyword+"</span>&nbsp;&nbsp;"
            );
        }

        // ADDS A TITLE
        if($("#document_keywords > span").size() > 0){
            $("#document_keywords_title").text("user defined keyphrases :  ");
        }
    },

    highlightToBeDeleted: function(selection) {
        var NGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue");
        for (var i=0; i<NGramFormQueue['delete'].length; i++) {
            selection.each(function(){
                if (NGramFormQueue['delete'][i].label == $(this).text()) {
                    $(this).removeClass("highlight");
                    $(this).addClass("highlight_toberemoved");
                    if($(this).qtip('api') !== undefined)
                        $(this).qtip('disable');
                    // TODO : attach undo
                }
                else {
                    $(this).removeClass("highlight_toberemoved");
                }
            });
        }
    },

    highlightToBeAdded: function() {
        var NGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue");
        var queued_keywords_span = $("#document_queued_keywords").empty();
        for (var i=0; i<NGramFormQueue['add'].length; i++) {
            var words = NGramFormQueue['add'][i].label.split(" ");
            var pattern = new RegExp("((<span class='[^']' dbid='[^']'>)|(\\b)|(<\/span>))"+words.join("((<\/span>)*( )(<span class='[^']' dbid='[^']'>)*)")+"((\\b)|(<\/span>)|(<span class='[^']' dbid='[^']'>))", 'gi');
            var searchString = $("#document_to_edit")[0].innerHTML;
            $("#document_to_edit")[0].innerHTML = searchString.replace( pattern, "<span class='highlight_tobeadded' >$&</span>" );
            queued_keywords_span.append(
                "<span class='highlight_tobeadded'>"+NGramFormQueue['add'][i].label+"</span>&nbsp;&nbsp;"
            );
        }
        // ADDS A TITLE
        if($("#document_queued_keywords > span").size() > 0){
            $("#document_keywords_title").text("user defined keyphrases :  ");
        }
        // refresh qtip on this modified html
        datasetEditor.attachNGramEditor($("span.highlight"));

    },

    //attachKeywordEditor: function(selection) {
    //    selection.qtip({
    //        content: {
    //            text: function() {
    //                var node = $(this);
    //                return $('<div></div>').append(
    //                    $("<button></button>")
    //                        .button({
    //                            //icons: { primary:'ui-icon-circle-minus' },
    //                            text: true,
    //                            label : "delete keyword"
    //                        })
    //                        .css({
    //                            "font-size": "0.8em"
    //                            //"line-height": 1,0
    //                        })
    //                        .click(function(event){
    //                            datasetEditor.submitDeleteKeyword(node);
    //                        })
    //                    );
    //            }
    //        },
    //        hide: {
    //            delay : 1000
    //        },
    //        show: {
    //            solo: true
    //        }
    //     });
    //},

    attachNGramEditor: function(selection) {
        selection.qtip({
            content: {
                text: function() {
                    var node = $(this);
                    return $('<div></div>').append(
                            $("<p></p>").text(node.text()).css({"font-size": "0.8em","line-height": "1.0"})
                        ).append($("<button></button>")
                            .button({
                                //icons: { primary:'ui-icon-circle-minus' },
                                text: true,
                                label : "delete this one"
                            })
                            .css({
                                "font-size": "0.8em"
                                //"line-height": 1,0
                            })
                            .click(function(event){
                                datasetEditor.submitRemoveNode(node);
                            })
                        ).append($("<button></button>")
                            .button({
                                //icons: { primary:'ui-icon-circle-minus' },
                                text: true,
                                label : "delete all"
                            })
                            .css({
                                "font-size": "0.8em"
                                //"line-height": 1,0
                            })
                            .click(function(event){
                                datasetEditor.pushDeleteNGramForm(node);
                            })
                        )
                }
            },
            hide: {
                delay : 1000
            },
            show: {
                solo: true
            }
         });
    },

    submitRemoveNode: function(node) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var ngid = node.attr("dbid");

        if (documentObj['edges']['NGram'][ngid] === undefined) {
            console.log(ngid+" is not in Document edges");
        }
        else if (documentObj['edges']['NGram'][ngid] > 0) {

            if(datasetEditor.dataset_needs_update == false) {
                datasetEditor.dataset_needs_update = true;
                $("#"+datasetEditor.dataset_id + "_update_button").show();
            }

            node.removeClass("highlight");
            // will decrement the value on update
            updateDocument = {
                "py/object": "tinasoft.pytextminer.document.Document",
                'id': documentObj.id,
                'edges': {
                    'NGram' : {}
                }
            };
            updateDocument.edges.NGram[ngid] = -1;

            TinaService.postDocument(
                datasetEditor.dataset_id,
                updateDocument,
                'True',
                { success : function(data) {
                    TinaService.getDocument(
                        datasetEditor.dataset_id,
                        documentObj.id,
                        { success: function(data) {
                            $("#document_to_edit").data("documentObj", data);
                        }}
                    )
                }}
            );
        }
        else {
            console.log("document-ngram edge weight is <= 0, will submitGraphPreprocess to clean the database");
            datasetEditor.submitGraphPreprocess(datasetEditor.dataset_id);
        }
    },

    pushAddKeyword: function(keyword) {
        $("#add_document_keyword").val("");

        var documentObj = $("#document_to_edit").data("documentObj");
        if (documentObj['edges']['keyword'][keyword] !== undefined) {
            alert(keyword+" is already a keyword for document "+documentObj['id']+" : aborting");
            return;
        }

        var NGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue");
        NGramFormQueue['add'].push({
            'label':  keyword,
            'id': documentObj.id,
            'is_keyword': 'True'
        });

        $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue", NGramFormQueue);
        datasetEditor.highlightToBeAdded();
        if(datasetEditor.dataset_needs_update == false) {
            datasetEditor.dataset_needs_update = true;
            $("#"+datasetEditor.dataset_id + "_update_button").show();
        }
        datasetEditor.updateDatasetButton();
    },

    pushDeleteNGramForm: function(node) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var ngid = node.attr("dbid");

        if (documentObj['edges']['NGram'][ngid] === undefined) {
            console.error(ngid+" is not in Document edges");
            return;
        }
        else if (documentObj['edges']['NGram'][ngid] > 0) {
            // queue one storage.deleteNGramForm
            var NGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue");
            NGramFormQueue['delete'].push({
                'label':  node.text(),
                'id': node.attr("dbid"),
                'is_keyword': 'False'
            });
            $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue", NGramFormQueue);

            datasetEditor.highlightToBeDeleted($('span.highlight'));

            if(datasetEditor.dataset_needs_update == false) {
                datasetEditor.dataset_needs_update = true;
                $("#"+datasetEditor.dataset_id + "_update_button").show();
            }
            datasetEditor.updateDatasetButton();
        }
        else {
            console.log("document-ngram edge weight is <= 0, will submitGraphPreprocess to clean the database");
            datasetEditor.submitGraphPreprocess(datasetEditor.dataset_id);
        }
    },

    updateDatasetButton: function() {
        $("#"+datasetEditor.dataset_id + "_update_button").qtip('option', 'content.text',
            function() {
                var tiptext = "Keyphrases modifications : ";
                var data = $(this).data("NGramFormQueue");
                for(var type in data) {
                    tiptext += "<br/>"+type+" : ";
                    labels=[];
                    for(var i=0; i<data[type].length; i++) {
                        labels.push(data[type][i].label);
                    }
                    tiptext += labels.join(", ");
                }
                return tiptext;
            }
        );

    },

    submitUpdateDataset: function(button) {
        button.hide();
        var NGramFormQueue = button.data("NGramFormQueue");
        var dataset_id = button.data("dataset_id");
        // global deleteNGramForm state indicator
        datasetEditor.updateDatasetSemaphore = NGramFormQueue["delete"].length + NGramFormQueue["add"].length;
        if (datasetEditor.updateDatasetSemaphore > 0) {
            datasetEditor.submitDeleteNGramFormQueue(dataset_id, NGramFormQueue["delete"]);
            datasetEditor.submitAddNGramFormQueue(dataset_id, NGramFormQueue["add"]);
        }
        else {
            datasetEditor.submitGraphPreprocess(dataset_id);
        }
    },

    submitGraphPreprocess: function(dataset_id) {
        TinaService.postGraphPreprocess(dataset_id, {
            success: function(data, textStatus, XMLHttpRequest) {
                $("#notification").notify("create", {
                    title: 'Tinasoft Notification',
                    text: 'Successfully updated index of data set "'
                        +dataset_id
                        +'"'
                });
                $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue", { "delete":[], "add": [] });
                $('#document_to_edit > span').removeClass("highlight_toberemoved");
                $('#document_to_edit > span').removeClass("highlight_tobeadded");
                datasetEditor.dataset_needs_update = false;
                $("#"+datasetEditor.dataset_id + "_update_button").hide();
            },
            complete: function() {
                $("#editdataset_document").change();
            }
        });
    },

    submitAddNGramFormQueue: function(dataset_id, NGramFormQueue){
        for(var i=0; i<NGramFormQueue.length; i++) {
            TinaService.postNGramForm(
                dataset_id,
                NGramFormQueue[i].label,
                NGramFormQueue[i].id,
                NGramFormQueue[i].is_keyword,
                {
                    success: function(data, textStatus, XMLHttpRequest) {
                        $("#notification").notify("create", {
                            title: 'Tinasoft Notification',
                            text: 'Successfully added the keyword "'
                                + data[0]
                                + '" to '
                                + data[1]
                                + ' Documents'
                        });
                        datasetEditor.updateDatasetSemaphore -= 1;
                        if (datasetEditor.updateDatasetSemaphore==0){
                            datasetEditor.submitGraphPreprocess(dataset_id);
                        }
                    }
                }
            )
        }
    },

    submitDeleteNGramFormQueue: function(dataset_id, NGramFormQueue){
        for(var i=0; i<NGramFormQueue.length; i++) {
            TinaService.deleteNGramForm(
                dataset_id,
                NGramFormQueue[i].label,
                NGramFormQueue[i].id,
                NGramFormQueue[i].is_keyword,
                {
                    success: function(data, textStatus, XMLHttpRequest) {
                        $("#notification").notify("create", {
                        title: 'Tinasoft Notification',
                        text: 'Successfully removed all occurrences of "'
                            +data[0]
                            +'" in data set "'
                            +dataset_id
                            +'" (appearing in '
                            +data[1]
                            +' documents)'
                        });
                        datasetEditor.updateDatasetSemaphore -= 1;
                        if (datasetEditor.updateDatasetSemaphore==0){
                            datasetEditor.submitGraphPreprocess(dataset_id);
                        }
                    }
                }
            );
        }
    },

    toggleEditionForm: function(dataset_id) {
        var self = this;
        // fills the form if it's going to be visible
        if ( $("#editdocument_form:visible").length == 0 ) {
            datasetEditor.dataset_id = dataset_id;
            TinaService.getDataset(
                dataset_id,
                { success: datasetEditor.displayCorpusSelect }
            );
        }
        $(".fold_form:visible:not(#editdocument_form)").hide("fold");
        $("#editdocument_form").toggle("fold");
    },

    displayDocumentSelect: function(data, textStatus, XMLHttpRequest) {
        var self = this;
        var document_select = $("#editdataset_document").empty();//.append($("<option value=''></option>"));
        for (var doc_id in data['edges']['Document']) {
            document_select.append($("<option value='"+doc_id+"'>"+htmlEncode(doc_id)+"</option>"));
        }
        document_select.change();
    },

    displayCorpusSelect: function(data, textStatus, XMLHttpRequest) {
        var self = this;
        var corpus_select = $("#editdataset_corpus").empty();//append($("<option value=''></option>"));
        for (var corp_id in data['edges']['Corpus']) {
            corpus_select.append($("<option value='"+corp_id+"'>"+htmlEncode(corp_id)+"</option>"));
        }
        corpus_select.change();
    }
};
