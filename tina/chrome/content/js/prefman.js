//      This program is free software; you can redistribute it and/or modify
//      it under the terms of the GNU General Public License as published by
//      the Free Software Foundation; either version 2 of the License, or
//      (at your option) any later version.
//
//      This program is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      GNU General Public License for more details.
//
//      You should have received a copy of the GNU General Public License
//      along with this program; if not, write to the Free Software
//      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//      MA 02110-1301, USA.


/*
 * Mozilla preferences handler
 */
function tinasoftdesktop_PrefManager() {
    var startPoint="tinasoft.desktop.";

    var pref=Components.classes["@mozilla.org/preferences-service;1"].
        getService(Components.interfaces.nsIPrefService).
        getBranch(startPoint);

    var observers={};

    // whether a preference exists
    this.exists=function(prefName) {
        return pref.getPrefType(prefName) != 0;
    }

    // returns the named preference, or defaultValue if it does not exist
    this.getValue=function(prefName, defaultValue) {
        var prefType=pref.getPrefType(prefName);

        // underlying preferences object throws an exception if pref doesn't exist
        if (prefType==pref.PREF_INVALID) {
            return defaultValue;
        }

        switch (prefType) {
            case pref.PREF_STRING: return pref.getCharPref(prefName);
            case pref.PREF_BOOL: return pref.getBoolPref(prefName);
            case pref.PREF_INT: return pref.getIntPref(prefName);
        }
    }

    // sets the named preference to the specified value. values must be strings,
    // booleans, or integers.
    this.setValue=function(prefName, value) {
        var prefType=typeof(value);

        switch (prefType) {
            case "string":
                /* stringify if not a raw value */

                break;
            case "boolean":
                break;
            case "number":
                if (value % 1 != 0) {
                    throw new Error("Cannot set preference to non integral number");
                }
                break;
            default:
                throw new Error("Cannot set preference "+prefName+" with datatype: " + prefType);
        }

        // underlying preferences object throws an exception if new pref has a
        // different type than old one. i think we should not do this, so delete
        // old pref first if this is the case.
        if (this.exists(prefName) && prefType != typeof(this.getValue(prefName))) {
            this.remove(prefName);
        }

        // set new value using correct method
        switch (prefType) {
            case "string": pref.setCharPref(prefName, value); break;
            case "boolean": pref.setBoolPref(prefName, value); break;
            case "number": pref.setIntPref(prefName, Math.floor(value)); break;
        }
    }

    // deletes the named preference or subtree
    this.remove=function(prefName) {
        pref.deleteBranch(prefName);
    }

    // call a function whenever the named preference subtree changes
    this.watch=function(prefName, watcher) {
        // construct an observer
        var observer={
            observe:function(subject, topic, prefName) {
                watcher(prefName);
            }
        };

        // store the observer in case we need to remove it later
        observers[watcher]=observer;

        pref.QueryInterface(Components.interfaces.nsIPrefBranchInternal).
            addObserver(prefName, observer, false);
    }

    // stop watching
    this.unwatch=function(prefName, watcher) {
        if (observers[watcher]) {
            pref.QueryInterface(Components.interfaces.nsIPrefBranchInternal)
                .removeObserver(prefName, observers[watcher]);
        }
    }
}


/* Gives the userscript access to prefmanager */
function tinasoftdesktop_ScriptStorage() {
    this.prefMan=new tinasoftdesktop_PrefManager();
}
tinasoftdesktop_ScriptStorage.prototype.setValue = function(name, val) {
    this.prefMan.setValue(name, val);
}
tinasoftdesktop_ScriptStorage.prototype.getValue = function(name, defVal) {
    return this.prefMan.getValue(name, defVal);
}
tinasoftdesktop_ScriptStorage.prototype.remove = function(name) {
    return this.prefMan.remove(name);
}
var prefManager = new tinasoftdesktop_ScriptStorage();

/*
* Generic Cache object
* providing public acces to Mozilla preferences
* and the storage accessor for LinktoolStorage sqlite results
* onLoad page event must call Cache.initCache( href, sha256 )
*/
var Cache = {

    setValue : function( key, value, rawValue ) {
        if ( rawValue ) {
            prefManager.setValue( key, value );
        }
        else {
            var string = JSON.stringify( value, null, null );
            //console.log( " Setting cache value " + key + ' = ' + string );
            prefManager.setValue( key, string );
        }
        return value;
    },
    /*
    Function: getValue
    A wrapper function for prefManager.getValue that handles non-string data better.

    Parameters:
    key - A unique string identifier
    defaultValue - This value will be returned if nothing is found.
    rawValue - Doesn't use Json encoding on stored values

    Returns:
    Either the stored value, or defaultValue if none is found.
    */
    getValue : function ( key, defaultValue, rawValue ) {
        try {
        if ( rawValue === undefined ) {
            defaultValue = JSON.stringify( defaultValue, null, null );
        }
        var result = prefManager.getValue( key, defaultValue );
        if ( result == null || result == "") {
            var json  =  JSON.parse( defaultValue );
            return json;
        }
        else if ( rawValue ) {
            return result;
        }
        else {
            var json = JSON.parse( result );
            return json;
        }
        }
        catch ( exc ) {
        }
    },

    /*resetCache: function () {
        if ( this.storage != false ) {
            unsafeWindow.globalStorage[ document.location.host ] = {};
            console.log( 'Cache have been flushed for host '  + document.location.host );
        }
    },*/

    /*showCache: function() {
        var count = 0;
        if ( this.storage ) {
            console.log('==== Cached URLs are shown below: ====');
            for( var url in this.storage ) {
                count++;
                console.log( url );
            }
        }
        console.log('==== ' + count + ' URLs are cached. ====');
    },*/
};
