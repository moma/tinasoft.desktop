if (typeof(JS_LIB_LOADED)=='boolean') 
{
  include(jslib_file);

  const JS_ZIP_LOADED       = true;
  const JS_ZIP_FILE         = "zip.js";

  const JS_ZIP_CID          = "@mozilla.org/libjar/zip-reader;1";
  const JS_ZIP_I_ZIP_READER = "nsIZipReader";

  // DEPRECATED! MARKED FOR REMOVAL
  const JS_ZIP_FILE_CID     = "@mozilla.org/file/local;1";
  const JS_ZIP_I_LOCAL_FILE = "nsILocalFile";
  const JS_ZIP_FILE_INIT    = "initWithPath";
  
  function Zip (aZipFile) 
  {
    if (!aZipFile)
      return jslibErrorMsg("NS_ERROR_XPC_NOT_ENOUGH_ARGS"); 

    // sanity check for nsIFile obj, File obj, or string path arg
    if (jslibTypeIsObj(aZipFile)) 
    {
      if (!jslibTypeIsUndef(aZipFile.nsIFile))
        this.mZipFile = aZipFile.nsIFile;
      else
        this.mZipFile = aZipFile;
    } 
    else if (jslibTypeIsStr(aZipFile)) 
    {
      this.mZipFile = new this.ZipPath(aZipFile);
    }
 
    this.mFile = new File(this.mZipFile);

    if (!this.mZipFile.exists()) 
      this.mFile.create();

      // return jslibErrorMsg("NS_ERROR_FILE_NOT_FOUND", this.mZipFile.path);

    this.init();

    return JS_LIB_OK;
  }
  
  Zip.prototype = 
  {
    ZipReader : jslibCreateInstance(JS_ZIP_CID, "nsIZipReader"),
    ZipPath   : jslibConstructor("@mozilla.org/file/local;1", 
                                 "nsILocalFile", 
                                 "initWithPath"),
    hasWriter : false,
    ZipWriter : null,
    zipW      : null,

    DIRECTORY : 0x01, // 1
    mPerm     : 0755,
    FINISHED  : "*** FINISHED . . .  .",
    mInit     : false,
    mZipFile  : null,
    mFile     : null,
    mZip      : null,
    mIsOpen   : false,
    libName   : "Zip",
    
    // initialize the zip reader
    init : function ()
    {
      try 
      {
        this.hasWriter = jslibCheckForInterface("nsIZipWriter");

        this.mZip = this.ZipReader;

        // support legacy func init
        if (this.mZip.init)
          this.mZip.init(this.mZipFile);

        if (this.hasWriter)
        {

          this.ZipWriter = jslibConstructor("@mozilla.org/zipwriter;1", "nsIZipWriter");
          this.zipW      = new this.ZipWriter;
        }

        this.mInit = true;
        this.open();
      } 
        catch (e) { jslibError(e); }
    }, 
  
    // open zip file
    open : function ()
    {
      if (!this.mInit) this.init();
  
      var rv = JS_LIB_OK;
  
      if (this.mIsOpen) return rv;
  
      try 
      { 
        if (this.mZip.init)
          this.mZip.open(); 
        else
          this.mZip.open(this.mZipFile); 

        this.mIsOpen = true;
      } 
        catch (e) { rv = jslibError(e); }
  
      return rv;
    },
    
    // close zip file
    close : function ()
    {
      if (!this.mInit || !this.mIsOpen)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv = JS_LIB_OK;
      try 
      { 
        this.mZip.close(); 
        this.mInit = false;
        this.mIsOpen = false;
      } 
        catch (e) { rv = jslibError(e); }
  
      return rv;
    },
    
    // extract zip archive
    extract : function (aDest, aDomEl) 
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      if (arguments.length < 1) 
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
      else if (!aDest)
        return jslibErrorMsg("NS_ERROR_FILE_TARGET_DOES_NOT_EXIST");
    
      var rv = JS_LIB_OK;
      var dest;
  
      // sanity check for nsIFile objects string path args
      if (jslibTypeIsObj(aDest))
        dest = aDest;
      else if (jslibTypeIsStr(aDest))
        dest = new this.ZipPath(aDest);
  
      var entry;
      var newDir;
      try 
      {
        if (!dest.exists() || !dest.isDirectory())
          dest.create( this.DIRECTORY, this.mPerm);
    
        jslibDebug("\nExtracting:\t"+this.mZipFile.path+"\nTo:\t\t"+dest.path+" . . . \n\n");
    
        var entries = this.findEntries("*");
        var destbase = new this.ZipPath(dest.path);

        // legacy support for API change from hasMoreElements to hasMore
        var targetFunc = entries.hasMoreElements ? "hasMoreElements" : "hasMore";
        
        while (entries[targetFunc]()) 
        {
          entry = entries.getNext();

          if (!jslibTypeIsString(entry))
          {
            jslibQI(entry, "nsIZipEntry");
            entry = entry.name;
          }

          dest  = new this.ZipPath(dest.path);
          dest.setRelativeDescriptor(destbase, entry);
  
          // create if entry is a dir
          if (!dest.exists() && entry.length - 1 == entry.lastIndexOf("/")) 
          {
            dest.create(this.DIRECTORY, this.mPerm);
            continue;
          } 
            else if (!dest.parent.exists()) 
          {
            dest.parent.create(this.DIRECTORY, this.mPerm);
          } 
            else if (dest.exists()) 
          {
            continue;
          }
  
          if (aDomEl)
            aDomEl.value = "extracting: "+dest.leafName;
 
          jslibDebug("extracting: ["+entry+"] To: ["+dest.path+"]");
          this.mZip.extract(entry, dest);
        }
      } 
        catch (e) { rv = jslibError(e); }
    
      jslibDebug("\n\n"+this.FINISHED+"\n\n");
    
      if (aDomEl)
        aDomEl.value = this.FINISHED;
    
      return rv;
    }, 
  
    extractEntry : function (aEntryName, aDest) 
    {
      if (!aDest || !this.ensureEntry(aEntryName))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var dest = aDest;
      if (jslibTypeIsString(aDest))
        dest = new File(aDest).nsIFile;
  
      var rv = JS_LIB_OK;
      try 
      {
        this.mZip.extract(aEntryName, dest);
      } 
        catch (e) { rv = jslibError(e); }
  
      return rv;
    },
  
    // returns simple enumerator whose elements are of type nsIZipEntry
    findEntries : function (aPattern) 
    {
      if (!this.ensureEntry(aPattern))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv = null;
      try 
      {
        rv = this.mZip.findEntries(aPattern);
      } 
        catch (e) { rv = jslibError(e); }
        
      return rv;
    }, 
  
    // returns array of all entries
    getAllEntries : function () 
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv = new Array;
      try 
      {
        var entries = this.findEntries("*");

        // legacy support for API change from hasMoreElements to hasMore
        var targetFunc = entries.hasMoreElements ? "hasMoreElements" : "hasMore";
        
        while (entries[targetFunc]()) 
        {
          var entry = entries.getNext();

          if (!jslibTypeIsString(entry))
          {
            jslibQI(entry, "nsIZipEntry");
            entry = entry.name;
          }

          rv.push(entry);
        }
      } 
        catch (e) { jslibError(e); }
        
      return rv;
    }, 
    
    // returns array of entries found w/ aPattern
    getEntries : function (aPattern) 
    {
      if (!this.ensureEntry(aPattern))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv = new Array;
      try 
      {
        var entries = this.findEntries(aPattern);

        // legacy support for API change from hasMoreElements to hasMore
        var targetFunc = entries.hasMoreElements ? "hasMoreElements" : "hasMore";
        
        while (entries[targetFunc]()) 
        {
          var entry = entries.getNext();

          if (!jslibTypeIsString(entry))
          {
            jslibQI(entry, "nsIZipEntry");
            entry = entry.name;
          }

          rv.push(entry);
        }
      } 
        catch (e) { jslibError(e); }
        
      return rv;
    }, 
    
    getEntryName : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = null;
      try 
      {
        rv = this.mZip.getEntry(aZipEntry).name;
     } 
       catch (e) { jslibError(e); }
        
      return rv;
    },
  
    // return nsIZipEntry
    getEntry : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = null;
      try 
      {
        rv = this.mZip.getEntry(aZipEntry);
      } 
        catch (e) { jslibError(e); }
        
      return rv;
    },
  
    getEntryRealSize : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = 0;
      try 
      {
        var entry = this.getEntry(aZipEntry)
        rv = entry.realSize;
      } 
        catch (e) { jslibError(e); }

      return rv;
    },
  
    getEntrySize : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = 0;
      try 
      {
        var entry = this.getEntry(aZipEntry)
        rv = entry.size;
      } 
        catch (e) { jslibError(e); }

      return rv;
    },
  
    getEntryCompression : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = 0;
      try 
      {
        var entry = this.getEntry(aZipEntry)
        rv = entry.compression;
      } 
        catch (e) { jslibError(e); }

      return rv;
    },
  
    getEntryCRC32 : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      var rv = 0;
      try 
      {
        var entry = this.getEntry(aZipEntry)
        rv = entry.CRC32;
      } 
        catch (e) { jslibError(e); }

      return rv;
    },
  
    // returns nsIFile object of initialized zip file
    get file ()
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      return this.mZip.file;
    },
  
    // returns File object of initialized zip file
    get File ()
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      return this.mFile;
    },
  
    // returns nsIFile object of initialized zip file
    get nsIFile ()
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      return this.mZipFile;
    },
  
    // returns nsIFile object of initialized zip file
    get file ()
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      return this.mZipFile;
    },
  
    // returns nsIZipReader object 
    get nsIZipReader ()
    {
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      return this.mZip;
    },
  
    // reads a zip entry
    readEntry : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv;
      try 
      {
        var sis = jslibCreateInstance("@mozilla.org/scriptableinputstream;1", 
                                      "nsIScriptableInputStream");
        sis.init(this.getInputStream(aZipEntry));
        rv = sis.read(sis.available());
        sis.close();
      } 
        catch (e) { rv = jslibError(e); }
  
      return rv;
    },
  
    // returns input stream containing contents of specified zip entry
    getInputStream : function (aZipEntry)
    {
      if (!this.ensureEntry(aZipEntry))
        return jslibErrorMsg("NS_ERROR_INVALID_ARG");
  
      if (!this.mInit)
        return jslibErrorMsg("NS_ERROR_NOT_INITIALIZED");
  
      var rv;
      try 
      {
        rv = this.mZip.getInputStream(aZipEntry);
      } 
        catch (e) { rv = jslibError(e); }
  
      return rv;
    },

    // ensure an entry arg is valid
    ensureEntry : function (aZipEntry)
    {
      return (aZipEntry && jslibTypeIsString(aZipEntry));
    },

    // ensure an entry arg is valid
    compressFile : function (aFile, aCompressLevel)
    {
      var rv = JS_LIB_OK;

      if (!aFile) return jslibErrorMsg("NS_ERROR_XPC_NOT_ENOUGH_ARGS");

      try
      {
        if (!this.hasWriter) return jslibErrorMsg("NS_ERROR_NOT_AVAILABLE");

        if (! (aCompressLevel >= 0 && aCompressLevel < 10) ) aCompressLevel = 0;

        var inFile = new File(aFile);

        if (!inFile.exists()) 
          return jslibErrorMsg("NS_ERROR_FILE_TARGET_DOES_NOT_EXIST");

                                           // PR_RDWR | PR_CREATE_FILE | PR_TRUNCATE
        this.zipW.open(this.mFile.nsIFile,   0x04  |       0x08     |    0x20);

        this.zipW.addEntryFile(inFile.leaf, aCompressLevel, inFile.nsIFile, false);

        this.zipW.close();

        jslibPrintMsg("SUCCESS", "FILE", inFile.path, "COMPRESSED TO", this.mFile.path);
      }
        catch (e) { rv = jslibError(e); }
      
      return rv;
    }
  }

  jslibLoadMsg(JS_ZIP_FILE);
  
} 
  else { dump("Load Failure: zip.js\n"); }

