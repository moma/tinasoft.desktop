if(typeof(JS_LIB_LOADED)=='boolean')
{
  const JS_RDFBASE_LOADED        = true;
  const JS_RDFBASE_FILE          = "rdfBase.js";

  const JS_RDFBASE_CONTAINER_PROGID       = '@mozilla.org/rdf/container;1';
  const JS_RDFBASE_CONTAINER_UTILS_PROGID = '@mozilla.org/rdf/container-utils;1';
  const JS_RDFBASE_LOCATOR_PROGID         = '@mozilla.org/filelocator;1';
  const JS_RDFBASE_RDF_PROGID             = '@mozilla.org/rdf/rdf-service;1';
  const JS_RDFBASE_RDF_DS_PROGID          = '@mozilla.org/rdf/datasource;1?name=xml-datasource';

/***************************************
 * RDFBase is the base class for all RDF classes
 *
 */
function RDFBase(aDatasource) {
  this.RDF       = Components.classes[JS_RDFBASE_RDF_PROGID].getService();
  this.RDF       = this.RDF.QueryInterface(Components.interfaces.nsIRDFService);
  this.RDFC      = Components.classes[JS_RDFBASE_CONTAINER_PROGID].getService();
  this.RDFC      = this.RDFC.QueryInterface(Components.interfaces.nsIRDFContainer);
  this.RDFCUtils = Components.classes[JS_RDFBASE_CONTAINER_UTILS_PROGID].getService();
  this.RDFCUtils = this.RDFCUtils.QueryInterface(Components.interfaces.nsIRDFContainerUtils);
  if(aDatasource) {
    this._base_init(aDatasource);
  }
}

RDFBase.prototype = {
  RDF        : null,
  RDFC       : null,
  RDFCUtils  : null,
  dsource    : null,
  valid      : false,

  _base_init : function(aDatasource) {
    this.dsource = aDatasource;
  },

  getDatasource : function()
  {
    return this.dsource;
  },

  isValid : function()
  {
    return this.valid;
  },

  setValid : function(aTruth)
  {
    if(typeof(aTruth)=='boolean') {
        this.valid = aTruth;
        return this.valid;
    } else {
        return null;
    }
  },

  flush : function()
  {
    if(this.isValid()) {
              this.dsource.QueryInterface(Components.interfaces.nsIRDFRemoteDataSource).Flush();
    }
  }

};

RDFBase.prototype.getAnonymousResource = function()
{
  jslibDebug("entering getAnonymousNode");
  if(this.isValid()) {
    var res = this.RDF.GetAnonymousResource();
    return new RDFResource("node", res.Value, null, this.dsource);
  } else {
      jslibError(null, "RDF is no longer valid!\n", "NS_ERROR_UNEXPECTED",
            JS_RDF_FILE+":getNode");
    return null;
  }
};

RDFBase.prototype.getAnonymousContainer = function(aType)
{
  jslibDebug("entering getAnonymousContainer");
  if(this.isValid()) {
    var res = this.getAnonymousResource();
    jslibDebug("making Container");
    if(aType == "bag") {
      this.RDFCUtils.MakeBag(this.dsource, res.getResource());
    } else if(aType == "alt") {
      this.RDFCUtils.MakeAlt(this.dsource, res.getResource());
    } else {
      this.RDFCUtils.MakeSeq(this.dsource, res.getResource());
    }
    jslibPrint("* made cont ..."+res.getSubject()+"\n");
    return new RDFContainer(aType, res.getSubject(),null, this.dsource);
  } else {
      jslibError(null, "RDF is no longer valid!\n", "NS_ERROR_UNEXPECTED",
            JS_RDF_FILE+":getNode");
    return null;
  }
};

jslibDebug('*** load: '+JS_RDFBASE_FILE+' OK');

} // END BLOCK JS_LIB_LOADED CHECK

else
{
    jslibPrint("JS_RDFBase library not loaded:\n"                                +
         " \tTo load use: chrome://jslib/content/jslib.js\n"            +
         " \tThen: include('chrome://jslib/content/rdf/rdf.js');\n\n");
}

