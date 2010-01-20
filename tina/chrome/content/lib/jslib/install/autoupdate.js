if (typeof(JS_LIB_LOADED)=='boolean') 
{
  const JS_AUTOUPDATE_LOADED   = true;
  const JS_AUTOUPDATE_FILE     = 'autoupdate.js';

  // Constructor
  function 
  AutoUpdate (aRemoteFile, aPackageName, aContent, aCallback)
  {
    // pull in dependent libs
    include (jslib_remotefile);
    include (jslib_dir);
    include (jslib_file);
    include (jslib_dirutils);
    include (jslib_prefs);
    include (jslib_packageinfo);
    include (jslib_networkutils);

    if (!(aRemoteFile && aPackageName))
      throw "Missing: aRemoteFile or aPackageName";

    var ext = aRemoteFile.substring(aRemoteFile.lastIndexOf(".")+1, 
                                    aRemoteFile.length);

    switch (ext)
    {
      case "xpi":
      case "jar":
      case "rdf":
        break;
      default:
        jslibDebugMsg("Invalid Mozilla package file", aRemoteFile);
        return jslibError("NS_ERROR_INVALID_ARG");
    }

    this.mNU = new NetworkUtils;

    this.mPackageExt = ext;
    this.mURI = this.mNU.fixupURI(aRemoteFile);

    if (!this.mURI)
      return jslibError("NS_ERROR_INVALID_ARG");
    else
      this.mRemoteFile = this.mURI.spec;

    this.mPackageName = aPackageName;
    this.mPackageInfo = new PackageInfo(aPackageName);

    var displayName = this.mPackageInfo.displayName;

    if (displayName)
      this.mDisplayName = displayName
    else
      this.mDisplayName = this.mPackageName;

    if (jslibTypeIsObj(aContent))
      this.mContent = aContent;
    else if (jslibTypeIsObj(_content))
      this.mContent = _content;
    else
      throw "no dom window context found ...";

    this.mCallback = aCallback;

    this.mInit = true;

    return JS_LIB_OK;
  } 

  AutoUpdate.prototype = 
  {
    libName: "AutoUpdate",
    mRemoteFile:null,
    mPackageName:null,
    mDisplayName:null,
    mPackageExt:null,
    mPackageInfo:null,
    mRemoteVersion:null,
    mLocalVersion:null,
    mPackageURI:null,
    mDistURI:null,
    mTargetURI:null,
    mContent:null,
    mDataSource:null,
    mCallback:null,
    mRefresh:null,
    mUsePrompts:true,
    mInit:false,
    mNU:null,
    mURI:null,
    mValidURI:false,
    mValidPackageURI:false,
    mAsyncCallBack:null,
    CHROME_NS:"http://www.mozilla.org/rdf/chrome#",

    asyncValidate : function (aCallback) 
    {
      if (!aCallback || !jslibTypeIsFunc(aCallback)) 
        return jslibError("NS_ERROR_INVALID_ARG");

      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      this.isValidURI = false;
      this.mAsyncCallBack = null;

      this.mAsyncCallBack = aCallback;
      var self = this;

      var cb = function (aIsValid)
      {
        self.isValidURI = aIsValid;
        self.mAsyncCallBack();
      }

      this.mNU.callback = cb;
      this.mNU.validateURI(this.mRemoteFile);

      return JS_LIB_OK;
    },

    get isValidURI ()
    {
      return this.mValidURI;
    },

    set isValidURI (aVal)
    {
      this.mValidURI = aVal;
    }, 

    get isValidPackageURI ()
    {
      return this.mValidPackageURI;
    },

    set isValidPackageURI (aVal)
    {
      this.mValidPackageURI = aVal;
    }, 

    // main API use to check for updates
    checkForUpdate : function (aManual) 
    {
      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      this.mRefresh = aManual;
      var rv;
      switch (this.mPackageExt)
      {
        case "xpi":
          rv = this.checkByTimeStamp();
          break;
        case "rdf":
          rv = this.checkByRDF();
          break;
      }
      return rv;
    },

    // checking by time stamp is one way to do it
    // not necessarily the best
    checkByTimeStamp : function () 
    {
      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      var rf = new RemoteFile (this.mRemoteFile);

      if (!rf.exists()) 
        throw this.mRemoteFile + " wasn't found";

      // get the remote xpi time stamp
      var lastModified = new Date(rf.lastModified);

      // check and see what and where the package file is
      var pkgTS = this.packageTimeStamp();

      var remoteTS = lastModified.getTime();
      var localTS = pkgTS.getTime() + 200000; // fuzz add two hours 
                                              // local and server clocks
                                              // are never exact so we ballpark

      jslibDebug("Remote Package Time Stamp: "+remoteTS);
      jslibDebug("Local Package Time Stamp: "+localTS);

      var rv = (remoteTS > localTS);

      return rv;
    },
    
    packageTimeStamp : function () 
    {
      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      var du = new DirUtils();
      var fTest = du.getChromeDir()+"/"+this.mPackageName;
      var d = new Dir(fTest);
      var dateModified;
      // check global chrome first
      if (d.isDir()) {
        dateModified = d.dateModified;
      } else {
        fTest += ".jar";
        var f = new File(fTest);
        if (f.exists())
          dateModified = f.dateModified;
      }

      // check user chrome 
      if (!dateModified) {
        fTest = du.getUserChromeDir()+"/"+this.mPackageName+".jar";
        f = new File(fTest);
        if (f.exists())
          dateModified = f.dateModified;
        jslibDebug(dateModified);
        jslibDebug(f.path);
      }

      return dateModified;
    },

    checkByRDF : function (aBlocking) 
    {
      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      var RDF = this.RDF;

      var observer = {
        onBeginLoad : function (sink) {},
        onInterrupt : function (sink) {},
        onResume    : function (sink) {},
        onError     : function (sink,status,msg) 
        { 
          this.o.alert("Update unsuccessful. Please try again later.")
        },
 
        onEndLoad : function (sink) 
        {
          var RDF = this.o.RDF;
          var packres = RDF.GetResource("urn:mozilla:package:" 
                                        + this.o.mPackageName);
          var packageVersionPred = RDF.GetResource(this.o.CHROME_NS 
                                                   + "packageVersion")
          var version = this.o.mDataSource.GetTarget(packres, 
                                                     packageVersionPred, 
                                                     true);
          var packageURLPred = RDF.GetResource(this.o.CHROME_NS + "packageURL")
          var url = this.o.mDataSource.GetTarget(packres, packageURLPred, true);

          var distroURLPred = RDF.GetResource(this.o.CHROME_NS + "distroURL")
          var distURL = this.o.mDataSource.GetTarget(packres, distroURLPred, true);

          if (version instanceof jslibI.nsIRDFLiteral)
            var packageVersion = version.Value;

          this.o.mRemoteVersion = packageVersion;

          if (url instanceof jslibI.nsIRDFLiteral)
            var packageURL = url.Value;

          this.o.mPackageURI = this.o.mTargetURI = packageURL;

          jslibDebugMsg("packageURL", packageURL);

          if (distURL) 
          {
            if (distURL instanceof jslibI.nsIRDFLiteral)
              this.o.mDistURI = distURL.Value;

            jslibPrintMsg("distURL", this.o.mDistURI);
          }

          // callback function
          if (jslibTypeIsFunc(this.o.mCallback))
            this.o.mCallback();

          if (this.o.usePrompts)
            this.o.update();

          sink.removeXMLSinkObserver(this);
          jslibQI(sink, "nsIRDFDataSource");
        }
      };

      try {
        var ds;
        if (!aBlocking) {
          ds = RDF.GetDataSource(this.mRemoteFile);
          this.mDataSource = ds;

          jslibQI(ds, "nsIRDFXMLSink");

          ds.addXMLSinkObserver(observer);

          if (this.mRefresh) {
            var rds = jslibQI(ds, "nsIRDFRemoteDataSource");
            rds.Refresh(aBlocking);
          }

          // add autoupdate object as property to observer
          observer.o = this;

        } else {
          ds = RDF.GetDataSourceBlocking(this.mRemoteFile);
          this.mDataSource = ds;

          if (this.mRefresh) {
            rds = jslibQI(ds, "nsIRDFRemoteDataSource");
            rds.Refresh(true);
          }

          var packres = RDF.GetResource("urn:mozilla:package:" 
                                        + this.mPackageName);
          var packageVersionPred = RDF.GetResource(this.CHROME_NS 
                                                   + "packageVersion")
          var version = this.mDataSource.GetTarget(packres, 
                                                   packageVersionPred, 
                                                   true);
          var packageURLPred = RDF.GetResource(this.CHROME_NS + "packageURL")
          var url = this.mDataSource.GetTarget(packres, packageURLPred, true);

          var distroURLPred = RDF.GetResource(this.CHROME_NS + "distroURL")
          var distURL = this.mDataSource.GetTarget(packres, distroURLPred, true);

          if (version instanceof jslibI.nsIRDFLiteral)
            var packageVersion = version.Value;

          this.mRemoteVersion = packageVersion;

          if (url instanceof jslibI.nsIRDFLiteral)
            var packageURL = url.Value;

          this.mPackageURI = this.mTargetURI = packageURL;
          jslibDebugMsg("packageURL", packageURL);

          if (distURL) 
          {
            if (distURL instanceof jslibI.nsIRDFLiteral)
              this.mDistURI = distURL.Value;

            jslibPrintMsg("distURL", this.mDistURI);
          }

          // callback function
          if (jslibTypeIsFunc(this.mCallback))
            this.mCallback();

          if (this.usePrompts)
            this.update();
        }
      } catch (e) { jslibError(e); }

      return JS_LIB_OK;
    },
    
    handleDotVersioning : function () 
    {
      if (!this.mDistURI       || 
          !this.mRemoteVersion || 
          !this.mLocalVersion)     
        return;

      var rDots = this.mRemoteVersion.split(".");
      var lDots = this.mLocalVersion.split(".");

      if (rDots.length == 3 && lDots.length == 3)
      {
        for (var i=0; i<3; i++)
        {
          if (rDots[i].length < lDots[i].length) 
          {
            for (var j=rDots[i].length; j<lDots[i].length; j++)
              rDots[i] = rDots[i].concat(0);
          } 
          else if (lDots[i].length < rDots[i].length) 
          {
            for (j=lDots[i].length; j<rDots[i].length; j++)
              lV = lDots[i].concat(0);
          }
        }

        if (parseInt(rDots[0]) > parseInt(lDots[0]))
          this.mTargetURI = this.mDistURI;

        if (parseInt(rDots[1]) > parseInt(lDots[1]))
          this.mTargetURI = this.mDistURI;
      }

      jslibPrintMsg("targetURI", this.mTargetURI);
    },

    verifyVersions : function () 
    {
      var rv = false;

      if (this.mInit) 
      {
        if (!this.mLocalVersion)
          this.mLocalVersion = this.getLocalVersion();

        jslibDebugMsg("mRemoteVersion", this.mRemoteVersion);
        jslibDebugMsg("mLocalVersion", this.mLocalVersion);
        
        // rV = remote version, lV = local version
        var rV = this.mRemoteVersion.replace(/\./g, "");
        var lV = this.mLocalVersion.replace(/\./g, "");

        if (rV.length < lV.length) {
          for (var i=rV.length; i<lV.length; i++)
            rV = rV.concat(0);
        } else if (lV.length < rV.length) {
            for (i=lV.length; i<rV.length; i++)
              lV = lV.concat(0);
        }

        rV = parseInt(rV, 10);
        lV = parseInt(lV, 10);

        rv = (rV > lV);
      }

      jslibDebugMsg(this.packageName + " requires update:", rv);

      return rv;
    },
    
    // returns the installed local package version from chrome/chrome.rdf
    getLocalVersion : function () 
    {
      return this.mPackageInfo.version || this.mLocalVersion;
    },

    get RDF ()
    {
      return jslibGetService("@mozilla.org/rdf/rdf-service;1", "nsIRDFService");
    },

    get datasource ()
    {
      return this.mDataSource;
    },

    get refresh ()
    {
      return this.mRefresh;
    },

    set refresh (aVal)
    {
      this.mRefresh = aVal;
    },

    get usePrompts ()
    {
      return this.mUsePrompts;
    },

    set usePrompts (aVal)
    {
      this.mUsePrompts = aVal;
    },

    get updateIsAvailable ()
    {
      this.checkByRDF(true);

      return this.verifyVersions();
    },

    get packageName ()
    {
      return this.mPackageName;
    },

    alert : function (aMsg) 
    {
      if (this.usePrompt) alert(aMsg);
    },

    promptUpdate : function () 
    {
      var message = this.mDisplayName
                  + " version: "
                  + this.mRemoteVersion
                  + " is now available for download."
                  + "\nWould you like to update now?";
      var ps = jslibGetService("@mozilla.org/embedcomp/prompt-service;1", 
                               "nsIPromptService");
      // check to see if Ignore Updates is set
      var prefs = new Prefs;
      const pref = "jslib.autoupdate.checkForUpdates";
      var prefValue;
      if (prefs.getType(pref) == 0)
        prefValue = true;
      else
        prefValue = prefs.getBool(pref);
          
      jslibDebug("type: "+prefValue);
      jslibDebug("check: "+prefs.getBool(pref));

      // if no pref value we bolt
      if (!prefValue) return;

      var checkValue = { value:prefValue };
      try {
        // XXX yes, I know these strings need localizaton
        var conf = ps.confirmCheck(this.mContent,
                                   "AutoUpdate",
                                   message,
                                   "Always Check for New Updates",
                                   checkValue);
      } catch(e) { jslibError(e); }

      // XXX if checkbox is set let's set the pref
      prefs.setBool(pref, checkValue.value);
      prefs.save();

      if (checkValue.value && conf)
        this.autoupdate();
      else 
        jslibDebug("No Update Made");
    },

    update : function () 
    {
      if (!this.mInit) return jslibError("NS_ERROR_NOT_INITIALIZED");

      if (this.verifyVersions()) 
      {
        this.handleDotVersioning();
        
        if (this.usePrompts) 
          this.promptUpdate();
        else
          this.autoupdate();

      } else {
        jslibDebug("Local Version: "+this.mLocalVersion);
        jslibDebug("Remote Version: "+this.mRemoteVersion);
        jslibDebug("No Update Necessary");
        // a manual update was called so alert user on status
        if (this.mRefresh)
          this.alert(this.mDisplayName+" is up-to-date.");
      }

      return JS_LIB_OK;
    },

    ensurePackageURI : function () 
    {
      this.handleDotVersioning();

      var uri = this.mNU.fixupURI(this.mTargetURI);

      if (!this.mURI)
        alert(this.mTargetURI + " is invalid ...");
      else
        this.mTargetURI = uri.spec;

      var self = this;
      var cb = function (aIsValid)
      {
        self.isValidPackageURI = aIsValid;
        self._autoupdate();
      }

      this.mNU.callback = cb;
      this.mNU.validateURI(this.mTargetURI);
    },

    autoupdate : function () 
    {
      this.ensurePackageURI();
    },

    _autoupdate : function () 
    {
      if (!this.isValidPackageURI) {
        const msg = "Update Warning: invalid update URI: \n\n"+this.mPackageURI; 
        alert(msg);
        return;
      }

      if (typeof(loadURI) == "function") 
        loadURI(this.mTargetURI);
      else 
        this.mContent.location = this.mTargetURI;
    }
  }

  jslibLoadMsg(JS_AUTOUPDATE_FILE);

} else { dump("Load Failure: autoupdate.js\n"); }
