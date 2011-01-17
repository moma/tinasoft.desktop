/*
* Handles the dataset editor events logic and display
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
        /*$("#updateDocument")
            .button({
                icons: { primary:'ui-icon-check' },
                text: true,
                label: "update document indexation"
            }).click(function(event) {
                this.submitUpdateDocumentIndex(event);
            });
        $.dynaCloud.max = 100;
        $.dynaCloud.scale = 2;
        $.dynaCloud.single = false;*/
    },

    populateDocumentForm: function(documentObj, textStatus, XMLHttpRequest) {
        var self = this;
        $('#document_to_edit').removeHighlight();
        var tbody = $("#editdocument_table > tbody");
        tbody.empty();
        /*if (documentObj['highlight_content'] !== undefined) {
            var html = documentObj['highlight_content'];
        }
        else {   */
            var html = documentObj['content'];
        //}
        tbody.append(
            $("<tr class='ui-widget-content'></tr>")
                .append(
                    $("<td></td>")
                        .append(
                            $("<p></p>")
                                .append("<b>content&nbsp;:</b>&nbsp;&nbsp;")
                                .append(
                                    $("<span id='document_to_edit'></span>")
                                        .data("documentObj", documentObj)
                                        //.addClass("dynacloud")
                                        .html(html)
                                )
                        )
                )
                .append(
                    $("<td></td>")
                        .css({ width: 400 })
                        /*.append(
                            $("<h4>highest frequency suggestions</h4>")
                        )
                        .append(
                            $("<p id='dynacloud'></p>")
                        )*/
                        .append(
                            $("<p></p>")
                                .append("<b>user-defined keyphrases&nbsp;:</b>&nbsp;&nbsp;")
                                .append(
                                    $("<span id='document_keywords'></span>").html("---list of keywords---")
                                )
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
                                        datasetEditor.submitAddKeyword($("#add_document_keyword").val());
                                    })
                                )
                        )
                )
        );
        //$("#document_to_edit").dynaCloud("#dynacloud");
        //if (documentObj['highlight_content'] === undefined || datasetEditor.dataset_needs_update == true) {
            for (var ngid in documentObj['edges']['NGram']) {
                TinaService.getNGram(
                    datasetEditor.dataset_id,
                    ngid,
                    { success: datasetEditor.highlightText }
                );
            }
        /*}
        else {
            datasetEditor.toggleNGramEditor();
        }*/
    },

    submitAddKeyword: function(keyword) {

        var documentObj = $("#document_to_edit").data("documentObj");

        if (documentObj['edges']['keyword'][keyword] !== undefined) {
            alert(keyword+" is already a keyword for document "+documentObj['id']+" : aborting");
            return;
        }

        if(datasetEditor.dataset_needs_update == false) {
                datasetEditor.dataset_needs_update = true;
                $("#"+datasetEditor.dataset_id + "_update_button").show();
            }

        TinaService.postNGramForm(
            datasetEditor.dataset_id,
            keyword,
            'True',
            {
                success: function(data, textStatus, XMLHttpRequest) {
                    $("#notification").notify("create", {
                        title: 'Tinasoft Notification',
                        text: 'Successfully added the keyword "'
                            +keyword
                            +'" to '
                            +data[1]
                            +' Documents in the current data set'
                    });
                }
            }
        )

    },

    highlightText: function(data, textStatus, XMLHttpRequest) {
        for (var form_words in data['edges']['label']) {
            var pattern = new RegExp('\\b'+form_words+'\\b', 'g');
            var searchString = $("#document_to_edit")[0].innerHTML;
            var resultString = searchString.replace( pattern, "<span class='highlight' dbid='"+data['id']+"'>$&</span>" );
            $("#document_to_edit")[0].innerHTML = resultString;
        }
        datasetEditor.attachNGramEditor($("span.highlight"));
        datasetEditor.highlightTobeRemovedText($("span.highlight"));
    },

    highlightTobeRemovedText: function(selection) {
        var deleteNGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("deleteNGramFormQueue");
        for (var i=0; i<deleteNGramFormQueue.length; i++) {
            selection.each(function(){
                if (deleteNGramFormQueue[i].label == $(this).text()) {
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

    attachNGramEditor: function(selection) {
        selection.qtip({
            content: {
                text: function() {
                    var node = $(this);
                    return $('<div></div>').append(
                        $("<button></button>")
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
                        ).append(
                            $("<button></button>")
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
                                datasetEditor.submitRemoveNGramForm(node);
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

        if (documentObj['edges']['NGram'][ngid] > 0) {

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
                //'highlight_content' : $("#document_to_edit").html()
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
                            //$("#document_to_edit").html( data['highlight_content'] );
                            //datasetEditor.toggleNGramEditor();
                        }}
                    )
                }}
            );
        }
        else {
            console.log("document-ngram edge weight is already <= 0");
        }
    },

    submitRemoveNGramForm: function(node) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var ngid = node.attr("dbid");

        if (documentObj['edges']['NGram'][ngid] === undefined) {
            console.error(ngid+" is not in Document edges");
            return;
        }

        if (documentObj['edges']['NGram'][ngid] > 0) {

            // queue one storage.deleteNGramForm
            var deleteNGramFormQueue = $("#"+datasetEditor.dataset_id + "_update_button").data("deleteNGramFormQueue");
            deleteNGramFormQueue.push({
                'label':  node.text(),
                'id': node.attr("dbid")
            });
            $("#"+datasetEditor.dataset_id + "_update_button").data("deleteNGramFormQueue", deleteNGramFormQueue);

            datasetEditor.highlightTobeRemovedText($('span.highlight'));

            if(datasetEditor.dataset_needs_update == false) {
                datasetEditor.dataset_needs_update = true;
                $("#"+datasetEditor.dataset_id + "_update_button").show();
            }

            $("#"+datasetEditor.dataset_id + "_update_button").qtip('option', 'content.text',
                function() {
                    var tiptext = "NGrams to be removed : ";
                    var data = $(this).data("deleteNGramFormQueue");
                    if (data.length==0)
                        return "";
                    for(var i=0; i<data.length; i++) {
                        tiptext += data[i].label+", ";
                    }
                    return tiptext;
                }
            );

        }
        else {
            console.log("ngram edge weight is already == 0");
        }
    },

    submitUpdateDataset: function(button) {
        var deleteNGramFormQueue = button.data("deleteNGramFormQueue");
        var dataset_id = button.data("dataset_id");
        // global deleteNGramForm state indicator
        datasetEditor.updateDatasetSemaphore = deleteNGramFormQueue.length;

        if(deleteNGramFormQueue.length==0) {
            // calls graph_preprocess
            TinaService.postGraphPreprocess(dataset_id, {
                success: function(data, textStatus, XMLHttpRequest) {
                    $("#notification").notify("create", {
                        title: 'Tinasoft Notification',
                        text: 'Successfully updated index of data set "'
                            +dataset_id
                            +'"'
                    });
                }
            });
        }

        for(var i=0; i<deleteNGramFormQueue.length; i++) {
            TinaService.deleteNGramForm(
                dataset_id,
                deleteNGramFormQueue[i].label,
                deleteNGramFormQueue[i].id,
                'False',
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
                            // calls graph_preprocess
                            TinaService.postGraphPreprocess(dataset_id, {
                                success: function(data, textStatus, XMLHttpRequest) {
                                    $("#notification").notify("create", {
                                        title: 'Tinasoft Notification',
                                        text: 'Successfully updated index of data set "'
                                            +dataset_id
                                            +'"'
                                    });
                                }
                            });
                        }
                    }
                }
            );
        }
        button.hide();
        button.data("deleteNGramFormQueue", []);
        $('#document_to_edit > span').removeClass("highlight_toberemoved")
        datasetEditor.dataset_needs_update = false;
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
