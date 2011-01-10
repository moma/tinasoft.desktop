/*
* Handles the dataset editor events logic and display
*/


var datasetEditor = {

    dataset_id: undefined,

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
        if (documentObj['highlight_content'] !== undefined) {
            var html = documentObj['highlight_content'];
        }
        else {
            var html = documentObj['content'];
        }
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
                                .append(
                                    $("<h4></h4>").text("user defined keywords (comma separated)")
                                )
                                .append(
                                    $("<input type='text'></input>")
                                        .autocomplete({
                                            source: documentObj['content'].split(" ")
                                        })
                                )
                                .append(
                                    $("<button></button>").button({
                                        icons: { primary:'ui-icon-arrowrefresh-1-e' },
                                        text: true,
                                        label: "update index"
                                    }).click(function(event) {
                                        //datasetEditor.submitKeywords($(this).parent(input).val().split(","));
                                    })
                                )
                        )
                )
        );
        //$("#document_to_edit").dynaCloud("#dynacloud");
        if (documentObj['highlight_content'] === undefined) {
            for (var ngid in documentObj['edges']['NGram']) {
                TinaService.getNGram(
                    datasetEditor.dataset_id,
                    ngid,
                    { success: datasetEditor.highlightText }
                );
            }
        }
        else {
            datasetEditor.toggleNGramEditor();
        }
    },

    highlightText: function(data, textStatus, XMLHttpRequest) {
        for (var form_words in data['edges']['label']) {
            var pattern = new RegExp('\\b'+form_words+'\\b', 'g');
            var searchString = $("#document_to_edit")[0].innerHTML;
            var resultString = searchString.replace( pattern, "<span class='highlight' dbid='"+data['id']+"'>$&</span>" );
            $("#document_to_edit")[0].innerHTML = resultString;
        }
        datasetEditor.toggleNGramEditor();
    },

    /*appendNGramButton: function(node) {
        $(node).append(
            $("<button></button>").button({
                icons: { primary:'ui-icon-circle-minus' },
                text: true,
                label : $(node).text()
            })
            .click(function(event){
                datasetEditor.submitRemoveNode(node);
            })
        ); 
    },*/

    toggleNGramEditor: function() {
        $('span.highlight').qtip({
            content: {
                text: function() {
                    var node = $(this);
                    return $("<button></button>")
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
                        });
                }
            },
            hide: {
                //event: 'mouseleave'
                delay : 1000
            },
            /*position: {
                my: 'center',
                at: 'top left'
                //container: $(this)
                //target: this
            },*/
            show: {
                solo: true
            }

         });
    },

    submitRemoveNode: function(node) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var ngid = node.attr("dbid");
        if (documentObj['edges']['NGram'][ngid] > 0) {
            documentObj['edges']['NGram'][ngid] -= 1;

            // update stored html string
            $('span.highlight').qtip('api').destroy();
            node.removeClass("highlight");
            documentObj['highlight_content'] = $("#document_to_edit").html();
            
            TinaService.postDocument(
                datasetEditor.dataset_id,
                documentObj,
                'True',
                { success : function(data) {
                    TinaService.getDocument(
                        datasetEditor.dataset_id,
                        documentObj.id,
                        { success: function(data) {
                            $("#document_to_edit").data("documentObj", data);
                            $("#document_to_edit").html( data['highlight_content'] );
                            datasetEditor.toggleNGramEditor();
                        }}
                    )
                }}
            );
        }
        else {
            console.log(documentObj['edges']['NGram'][ngid]);
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
