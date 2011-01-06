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
            })
        });
        $("#editdataset_document").change(function(event) {
            $("#editdataset_document option:selected").each(function() {
                TinaService.getDocument(
                    datasetEditor.dataset_id,
                    $(this).val(),
                    {
                        success: self.populateDocumentForm
                    }
                );
            })
        });
        $("#updateDocument")
            .button({
                icons: { primary:'ui-icon-check' },
                text: true,
                label: "update document indexation"
            }).click(function(event) {
                this.submitUpdateDocumentIndex(event);
            });
        $.dynaCloud.max = 100;
        $.dynaCloud.scale = 2;
        $.dynaCloud.single = false;
    },

    populateDocumentForm: function(documentObj, textStatus, XMLHttpRequest) {
        var self = this;
        $('#document_to_edit').removeHighlight();
        var tbody = $("#editdocument_table > tbody");
        tbody.empty();
        tbody.append(
            $("<tr class='ui-widget-content'></tr>")
                .append(
                    $("<td></td>")
                        .append(
                            $("<p></p>")
                                .append("<b>content&nbsp;:</b>&nbsp;&nbsp;")
                                .append(
                                    $("<span id='document_to_edit'></span>")
                                        //.data("documentObj", documentObj)
                                        //.addClass("dynacloud")
                                        .text(documentObj["content"])
                                )
                        )
                )
                .append(
                    $("<td></td>")
                        .css(
                            {
                                width: 400
                            }
                        )
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
                                    $("<input></input>")
                                        //.autocomplete({
                                        //    source: $("#document_to_edit").text().split(" ")
                                        //})
                                )
                                .append(
                                    $("<button></button>").button({
                                        icons: { primary:'ui-icon-arrowrefresh-1-e' },
                                        text: true,
                                        label: "update index"
                                    }).click(function(event) {
                                        //this.submitKeywords(event);
                                    })
                                )
                        )
                )
        );
        //$("#document_to_edit").dynaCloud("#dynacloud");
        for (var ngid in documentObj['edges']['NGram']) {
            TinaService.getNGram(
                datasetEditor.dataset_id,
                ngid,
                { success: datasetEditor.highlightText }
            );
        }
    },

    highlightText: function(data, textStatus, XMLHttpRequest) {
        for (var form_words in data['edges']['label']) {
            var pattern = new RegExp('\\b'+form_words+'\\b', 'g');
            var searchString = $("#document_to_edit")[0].innerHTML;
            var resultString = searchString.replace( pattern, "<span class='highlight'>$&</span>" );
            $("#document_to_edit")[0].innerHTML = resultString;
        }
        datasetEditor.toggleNGramEditor();
    },

    appendNGramButton: function(node) {
        
        $(node).append(
            $("<button></button>").button({
                icons: { primary:'ui-icon-circle-minus' },
                text: true,
                label : $(node).text()
            }).click( function(event){  
                console.log( "edit document request" );
            })
        ); 
    },

    toggleNGramEditor: function() {
        $('span.highlight').toggle(
            function() {
                datasetEditor.appendNGramButton(this);
                /*$(this).parent('span.highlight').each(function(){
                       datasetEditor.appendNGramButton(this);
                });*/
            },
            function() {
                $(this).children().filter('button').remove();
            }
        );
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
    },

    submitUpdateDocumentIndex: function(event) {
        var documentObject = {};
        /*TinaService.postWhitelistUpdate(
            documentObject,
            TinaServiceCallback.postWhitelistUpdate
        );*/
        return true;
    }
};

jQuery.fn.highlightEntireWord = function(pat) {
    function innerHighlight(node, pat) {
        var skip = 0;
        if (node.nodeType == 3) {
            var pos = node.data.indexOf(pat);
            if (pos >= 0) {
                var spannode = document.createElement('span');
                spannode.className = 'highlight';
                var middlebit = node.splitText(pos);
                var endbit = middlebit.splitText(pat.length);
                if (endbit.data.length > 0) {
                    if( endbit.textContent[0].match(/[^a-zA-Z]/) ) {
                        //console.log(pat);
                        var middleclone = middlebit.cloneNode(true);
                        spannode.appendChild(middleclone);
                        middlebit.parentNode.replaceChild(spannode, middlebit);
                        skip = 0;
                    }
                }
            }
        }
        //else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
        else if ( node.childNodes ){
            for (var i = 0; i < node.childNodes.length; i++) {
                i += innerHighlight(node.childNodes[i], pat);
            }
        }
        return skip;
    }
    return this.each(function() {
        innerHighlight(this, pat);
    });
};
