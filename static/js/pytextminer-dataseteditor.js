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
                    { success: self.populateDocumentForm }
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
    },
    
    populateDocumentForm: function(documentObj, textStatus, XMLHttpRequest) {
        var self = this;
        var tbody = $("#editdocument_table > tbody");
        tbody.empty();
        tbody.append(
            $("<tr class='ui-widget-content'></tr>")
                .append(
                    $("<th class='ui-widget-content'></th>").text("content")
                )
                .append(
                    $("<td class='ui-widget-content' id='document_to_edit'></td>").text(documentObj["content"])
                )
            );
        for (var ngid in documentObj['edges']['NGram']) {
            TinaService.getNGram(
                datasetEditor.dataset_id,
                ngid,
                { success: datasetEditor.highlightNGram }
            );
        }
    },
    
    highlightNGram: function(data, textStatus, XMLHttpRequest) {
        var self = this;
        console.log(data);
        for(var form in data['edges']['label']) {
            console.log(form);
            $("#document_to_edit:contains('"+form+"')")
                .wrapAll(
                    $("<span class='ui-widget-highlight'></span>").click(self.displayNGramEditor)
                );
        }
    },
    
    displayNGramEditor: function() {
        console.log(ngramObj);
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
        var document_select = $("#editdataset_document").empty().append($("<option value=''></option>"));
        for (var doc_id in data['edges']['Document']) {
            document_select.append($("<option value='"+doc_id+"'>"+htmlEncode(doc_id)+"</option>"));
        }
    },
    
    displayCorpusSelect: function(data, textStatus, XMLHttpRequest) {
        var self = this;
        var corpus_select = $("#editdataset_corpus").empty().append($("<option value=''></option>"));
        for (var corp_id in data['edges']['Corpus']) {
            corpus_select.append($("<option value='"+corp_id+"'>"+htmlEncode(corp_id)+"</option>"));
        };
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