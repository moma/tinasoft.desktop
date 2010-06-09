
const Cc = Components.classes;
const Ci = Components.interfaces;

function getString(key) {
  return document.getElementById("strings").getString(key);
}

function getMainString(key) {
  return document.getElementById("mainStrings").getString(key);
}

function fileChosen() {
  var file = document.getElementById("file").file;
  if (file && file.exists()) {
    try {
      // TODO ELISHOWK ::: PUT HERe THE ACTION TO CALL WHEN BUTTON "LOAD FILE" IS PRESSED 
      // by example:
      //gCorpus = ImportCSVFactory.loadCorpusFromFile(file);
      
      return;
    }
    catch (e) {
      Components.utils.reportError("Error when loading CSV: " + e);
      alert(getString("corpus.import.errors.read"));
    }
  }
  document.getElementById("save").disabled = false;
}



