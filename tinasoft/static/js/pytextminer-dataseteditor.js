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
    ngramformreverse: {},
    ngramcache: {},
    startpattern: "\\b((?:<span class='[^']'>)*",
    middlepattern: "(?:<\/span>)*\\s(?:<span class='[^']'>)*",
    endpattern: "(?:<\/span>)*)\\b",

    init: function() {
        var self = this;
        // hide by default all submit forms
        $("#editdocument_form").hide();
        $("#update_dataset_button").hide().button({
            text: true,
            label: "update the dataset to validate changes",
            icons: {
                primary: 'ui-icon-refresh'
            }
        })
        .attr("title", "click to update dataset's database")
        .click( function(eventObject) {
            datasetEditor.submitUpdateDataset();
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

        $("#add_document_keyword_button").button({
            icons: { primary:'ui-icon-arrowrefresh-1-e' },
            text: true,
            label: "add a keyphrase"
        }).click(function(event) {
            if ($("#add_document_keyword").val() != "" && $("#add_document_keyword").val() !== undefined) {
                datasetEditor.pushAddKeyword($("#add_document_keyword").val());
            }
        });

        $("#editprevious_document").button({
            icons: { primary:'ui-icon-triangle-1-w' },
            text: false
        }).click( function(event){
            if($("#editdataset_document option:selected")['prev']!==undefined){
                $("#editdataset_document option:selected").prev().attr('selected', 'selected');
                $("#editdataset_document").change();
            }
        });
        $("#editnext_document").button({
            icons: { primary:'ui-icon-triangle-1-e' },
            text: false
        }).click( function(event){
            if($("#editdataset_document option:selected")['next']!==undefined){
                $("#editdataset_document option:selected").next().attr('selected', 'selected');
                $("#editdataset_document").change();
            }
        });
        /*$
        $.dynaCloud.max = 100;
        $.dynaCloud.scale = 2;
        $.dynaCloud.single = false;*/
    },

    getNGramFormQueue: function() {
        return $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue");
    },

    setNGramFormQueue: function(newValue) {
        $("#"+datasetEditor.dataset_id + "_update_button").data("NGramFormQueue", newValue);
    },

    populateDocumentForm: function(documentObj, textStatus, XMLHttpRequest) {
        var self = this;
        var tbody = $("#editdocument_table > tbody");
        tbody.empty();

        if(documentObj == "" || documentObj === undefined) {
            console.error("database returned empty document");
            tbody.text("document not found");
            return;
        }

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
                .append( $("<td></td>").append( $("<p id='display_document_object'></p>")
                .append( content )
                .append( keywords )
                .append(queued_keywords) ) )
        );
        $("#add_document_keyword").autocomplete({
            source: documentObj['content'].split(" ")
        });
        //$("#document_to_edit").dynaCloud("#dynacloud");
        // welcome to the async world
        datasetEditor.ngramcache={};
        datasetEditor.ngramformreverse={};
        datasetEditor.NGramSemaphore = 0;
        var keys = Object.keys(documentObj['edges']['NGram']);
        datasetEditor.TotalNGram = keys.length;
        for (var ngid in documentObj['edges']['NGram']) {
            TinaService.getNGram(
                datasetEditor.dataset_id,
                ngid,
                {
                    success: function(ngramObj, textStatus, XMLHttpRequest){
                        datasetEditor.NGramSemaphore += 1;
                        datasetEditor.ngramcache[ngramObj['id']]=ngramObj;
                        if (datasetEditor.NGramSemaphore==datasetEditor.TotalNGram) {
                            var sortedSizeNGram = Object.values( datasetEditor.ngramcache );
                            sortedSizeNGram = numericLengthSort(sortedSizeNGram, "content");

                            for (var i=0; i<sortedSizeNGram.length; i++) {
                                datasetEditor.searchAndReplaceNGrams(sortedSizeNGram[i]);
                            }
                            datasetEditor.highlightToBeDeleted($("span.highlight"));
                            datasetEditor.highlightToBeAdded();
                            datasetEditor.attachNGramEditor($("span.highlight"));
                        }
                    }
                }
            );
        }
    },

    searchAndReplaceNGrams: function(ngramObj){
        var htmlString = $("#document_to_edit")[0].innerHTML;
        var totaloccs = 0;

        for (var form_words in ngramObj['edges']['label']) {
            datasetEditor.ngramformreverse[form_words.toLowerCase()]=ngramObj['id'];
            var words = form_words.split(" ");
            var patternstring = datasetEditor.startpattern + words.join(datasetEditor.middlepattern) + datasetEditor.endpattern;
            var pattern = new RegExp(patternstring, 'gim');
            var test = pattern.test(htmlString);
            if(test == false){
                datasetEditor.displayDocumentKeyword(form_words, ngramObj, totaloccs);
            }
            else {
                htmlString = htmlString.replace( pattern, "<span class='highlight'>$&</span>" );
            }
        }
        $("#document_to_edit")[0].innerHTML = htmlString;
    },

    displayDocumentKeyword: function(keyword, ngramObj, totaloccs) {
        var documentObj = $("#document_to_edit").data("documentObj");
        var list_of_keywords = $("#document_keywords");

        if (documentObj.edges.keyword[keyword] !== undefined) {
            list_of_keywords.append(
                "<span class='doc_keyword highlight'>"+keyword+"</span>&nbsp;&nbsp;"
            );
        }
        // ADDS A TITLE
        if($("#document_keywords > span").size() > 0){
            $("#document_keywords_title").text("user defined keyphrases :  ");
        }
    },

    highlightToBeDeleted: function(selection) {
        var NGramFormQueue = datasetEditor.getNGramFormQueue();
        var documentObj = $("#document_to_edit").data("documentObj");
        
        for (var i=0; i<NGramFormQueue['delete'].length; i++) {
            selection.each(function(){
                if (NGramFormQueue['delete'][i].docid != "" && documentObj.id != NGramFormQueue['delete'][i].docid) {
                    //console.log("not highlight_toberemoved");
                    return;
                }
                if (NGramFormQueue['delete'][i].label == $(this).text()) {
                    $(this).removeClass("highlight");
                    $(this).addClass("highlight_toberemoved");
                    // removes qtip on matching nodes
                    if($(this).qtip('api') !== undefined) {
                        $(this).qtip('disable');
                    }
                    // TODO : attach undo
                }
                else {
                    $(this).removeClass("highlight_toberemoved");
                }
            });
        }
    },

    highlightToBeAdded: function() {
        var documentObj = $("#document_to_edit").data("documentObj");

        var NGramFormQueue = datasetEditor.getNGramFormQueue();
        var queued_keywords_span = $("#document_queued_keywords").empty();
        var searchString = $("#document_to_edit")[0].innerHTML;
        for (var i=0; i<NGramFormQueue['add'].length; i++) {
            var words = NGramFormQueue['add'][i].label.split(" ");
            var patternstring = datasetEditor.startpattern + words.join(datasetEditor.middlepattern) + datasetEditor.endpattern;
            var pattern = new RegExp(patternstring, 'gim');
            // searches in the documents' contents
            searchString = searchString.replace( pattern, "<span class='highlight_tobeadded'>$&</span>" );
            // append to a list
            if (documentObj.id == NGramFormQueue['add'][i].id) {
                queued_keywords_span.append(
                    "<span class='highlight_tobeadded'>"+NGramFormQueue['add'][i].label+"</span>&nbsp;&nbsp;"
                );
            }
        }
        $("#document_to_edit")[0].innerHTML = searchString
        // displays A TITLE
        if($("#document_queued_keywords > span").size() > 0) {
            $("#document_keywords_title").text("user defined keyphrases :  ");
        }

    },

    attachNGramEditor: function(selection) {
        selection.qtip({
            content: {
                text: function() {
                    var node = $(this);
                    var ngramid = datasetEditor.ngramformreverse[ node.text().toLowerCase() ];
                    var ngramObj = datasetEditor.ngramcache[ ngramid ];
                    if (ngramObj===undefined) {
                        //alert("ngram editor could not find ngram obj cache");
                        return "Error : ngram editor could not find object in cache";
                    }
                    var occs = 0;
                    for (var corpid in ngramObj['edges']['Corpus']) {
                        occs += ngramObj['edges']['Corpus'][corpid];
                    }
                    var occurrences_text = "";
                    if (occs > 1) {
                        occurrences_text = "occurring "+occs+" times";
                    }
                    else {
                        occurrences_text = "occurring once";
                    }
                    return $('<div></div>').append(
                            $("<p></p>").text(node.text()).addClass("qtip_text")
                        ).append($("<button></button>")
                            .button({
                                text: true,
                                label : "delete from this document"
                            })
                            .css({
                                "font-size": "0.8em"
                            })
                            .click(function(event){
                                //datasetEditor.submitRemoveFromDocument(node);
                                datasetEditor.pushDeleteNGramForm(node, true);
                            })
                        ).append($("<button></button>")
                            .button({
                                text: true,
                                label : "delete from all"
                            })
                            .css({
                                "font-size": "0.8em"
                            })
                            .click(function(event){
                                datasetEditor.pushDeleteNGramForm(node, false);
                            })
                        ).append(
                            $("<p></p>").text(occurrences_text).addClass("qtip_text")
                        )
                }
            },
            hide: { delay : 2000 },
            show: { solo: true }
            //position: { corner: { target: 'bottomMiddle' } }
         });
    },

    pushAddKeyword: function(keyword) {
        $("#add_document_keyword").val("");

        var documentObj = $("#document_to_edit").data("documentObj");
        if (documentObj['edges']['keyword'][keyword] !== undefined) {
            alert("Sorry, you can't index twice an existing keyphrase : aborting");
            return;
        }
        if (datasetEditor.ngramformreverse[keyword.toLowerCase()]!== undefined) {
            alert("Sorry, you can't index twice an existing keyphrase : aborting");
            return
        }
        var NGramFormQueue = datasetEditor.getNGramFormQueue();
        NGramFormQueue['add'].push({
            'label':  keyword,
            'id': documentObj.id,
            'is_keyword': 'True'
        });
        datasetEditor.setNGramFormQueue(NGramFormQueue);
        datasetEditor.highlightToBeAdded();
        // refresh qtip on this modified html
        datasetEditor.attachNGramEditor($("span.highlight"));

        if(datasetEditor.dataset_needs_update == false) {
            datasetEditor.dataset_needs_update = true;
        }
        datasetEditor.updateDatasetButton();
    },

    pushDeleteNGramForm: function(node, deleteone) {
        var documentObj = $("#document_to_edit").data("documentObj");

        var ngid = datasetEditor.ngramformreverse[ node.text().toLowerCase() ];
        //var ngramObj = datasetEditor.ngramcache[ ngid ];

        if (documentObj['edges']['NGram'][ngid] === undefined) {
            console.error(ngid+" is not in Document edges");
            return;
        }

        else if (documentObj['edges']['NGram'][ngid] > 0) {
            var is_keyword = 'False';
            if (node.hasClass("doc_keyword")) {
                is_keyword = 'True';
            }
            // queue one storage.deleteNGramForm
            var NGramFormQueue = datasetEditor.getNGramFormQueue();
            var docid = "";
            if (deleteone == true) {
                docid = documentObj.id;
            }
            NGramFormQueue['delete'].push({
                'label':  node.text(),
                'id': ngid,
                'is_keyword': is_keyword,
                'docid': docid
            });
            datasetEditor.setNGramFormQueue(NGramFormQueue);
            
            datasetEditor.highlightToBeDeleted($('span.highlight'));

            if(datasetEditor.dataset_needs_update == false) {
                datasetEditor.dataset_needs_update = true;
            }
            datasetEditor.updateDatasetButton();
        }
        else {
            console.log("document-ngram edge weight is <= 0, will submitGraphPreprocess to clean the database");
            datasetEditor.submitGraphPreprocess(datasetEditor.dataset_id);
        }
    },

    updateDatasetButton: function() {
        if (datasetEditor.dataset_needs_update == false) {
            $("#"+datasetEditor.dataset_id + "_update_button").hide();
            $("#update_dataset_button").hide();
            return;
        }
        $("#update_dataset_button").addClass("ui-state-highlight").show();
        $("#"+datasetEditor.dataset_id + "_update_button").addClass("ui-state-highlight").show();
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

    submitUpdateDataset: function() {
        $("#update_dataset_button").button('disable');
        $("#"+datasetEditor.dataset_id + "_update_button").button('disable');
        $("#indexFileButton").button('disable');
        $("#generateGraphButton").button('disable');

        var NGramFormQueue = datasetEditor.getNGramFormQueue();
        var dataset_id = $("#"+datasetEditor.dataset_id + "_update_button").data("dataset_id");

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
                $("#update_dataset_button").button("enable").hide();
                $("#"+datasetEditor.dataset_id + "_update_button")
                    .hide()
                    .button("enable");
                    //.data("NGramFormQueue", { "delete":[], "add": [] });
                datasetEditor.dataset_needs_update = false;
                //$("#editdataset_document").change();
                displayDataTable("sessions");
            },
            complete: function() {
                $("#indexFileButton").button('enable');
                $("#generateGraphButton").button('enable');
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
                NGramFormQueue[i].docid,
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
        var document_select = $("#editdataset_document").empty();//.append($("<option value=''></option>"));
        var id_list = Object.keys(data['edges']['Document']);
        id_list.sort();
        for (var i=0; i< id_list.length;i++) {
            var doc_id = id_list[i];
            document_select.append($("<option></option>").attr('value', doc_id).text(doc_id));
        }
        document_select.change();
    },

    displayCorpusSelect: function(data, textStatus, XMLHttpRequest) {
        var corpus_select = $("#editdataset_corpus").empty();//append($("<option value=''></option>"));
        for (var corp_id in data['edges']['Corpus']) {
            corpus_select.append($("<option value='"+corp_id+"'>"+htmlEncode(corp_id)+"</option>"));
        }
        corpus_select.change();
    }
};
