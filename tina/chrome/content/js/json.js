/*
* JSON object
* Firefox 3.* and 3.5 cross support
*/

if ( typeof(JSON) == "undefined" ) {
	 Components.utils.import("resource://gre/modules/JSON.jsm");
	 JSON.parse = JSON.fromString;
	 JSON.stringify = JSON.toString;
}


