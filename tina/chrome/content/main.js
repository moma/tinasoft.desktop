
/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const Cc = Components.classes;
const Ci = Components.interfaces;

const HELP_URL          = "http://tina.csregistry.org/tiki-index.php?page=HomePage&bl=y";
const INTRO_URL         = "chrome://tina/content/about.xul";

/* Tinasoft SINGLETON */

if ( typeof(TinaService) == "undefined" ) {
    cls = Cc["Python.Tinasoft"];
    var TinaService = cls.getService(Ci.ITinasoft);
}

var submitImportfile = function(event) {
    corpora = $("#corpora")
    path = $("#csvfile")
    config  = $("#configfile")
    // TODO DEBUG
    path.val("/home/elishowk/code/Tinasoft/tests/pubmed_tina_test.csv");
    config.val("import.yaml");
    if ( corpora.val() == '' ) {
        corpora.addClass('ui-state-error');
        console.log( "missing the corpora field" );
        return false;
    }
    if ( path.val() == "" ) {
        path.addClass('ui-state-error');
        console.log( "missing the path field" );
        return false;
    }
    if ( config.val() == "" ) {
        config.addClass('ui-state-error');
        console.log( "missing the config file field" );
        return false;
    }
    TinaService.runImportFile(
        path.val(),
        config.val(),
        corpora.val(),
        false,
        false,
        'tina'
    );
    console.log( "end of submitting file " + path);
    return true;
};
