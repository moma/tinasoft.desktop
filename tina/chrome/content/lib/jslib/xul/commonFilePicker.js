if (typeof(JS_LIB_LOADED)=='boolean') 
{
  include(jslib_dir);

  const JS_COMMONFILEPICKER_FILE     = "commonFilePicker.js";
  const JS_COMMONFILEPICKER_LOADED   = true;
  
  const JS_CFP_CID = "@mozilla.org/filepicker;1";
  const JS_I_CFP   = "nsIFilePicker";
  
  function FilePicker () {}
  
  FilePicker.prototype = 
  {
    getFilePicker : function () 
    { 
      return jslibCreateInstance(JS_CFP_CID, JS_I_CFP); 
    },

    /*
     *
     * Both arguments are optional
     *
     * aDir
     *      can be either directory path string
     *   eg: "/tmp"
     *      or and nsIFile object
     *
     * aNewFileName
     *      a new file name string
     *   eg: "myfile.xul"
     *
     */

    saveAsXUL : function (aDir, aNewFileName) 
    {
      var fp = this.getFilePicker();

      fp.init(window, "SaveAsXULFile", 1);
      fp.appendFilters(jslibI.nsIFilePicker.filterXUL);
      
      fp.defaultString= aNewFileName ? aNewFileName : "new_file.xul";
  
      if (aDir) {
        if (typeof(aDir)=="object" && aDir.toString().match("nsIFile")) {
          if (!aDir.exists()) {
            jslibDebug("Dir: "+aDir.path+" doesn't exist");
            return null;
          }
          if (!aDir.isDirectory()) {
            jslibDebug("Dir: "+aDir.path+" is not a directory");
            return null;
          }
          fp.displayDirectory = aDir;
        }
        if (typeof(aDir)=="string") {
          var d = new Dir(aDir);
          if (!d.exists()) {
            jslibDebug("Dir: "+d.path+" doesn't exist");
            return null;
          }
          fp.displayDirectory = d.nsIFile;
        }
      }
    
      fp.show();
  
      return (fp.file.path.length > 0 ? fp.file.path : null);
    }
  };

  jslibLoadMsg(JS_COMMONFILEPICKER_FILE);
  
} else { dump("Load Failure: commonFilePicker.js\n"); }
  
