/* tinasoftdesktop_xmlhttprequester
* AJAX common object
* with automatic JSON parsing of responseText
*/



function tinasoftdesktop_xmlhttpRequester( ) {

}

// this function gets called by user scripts in content security scope to
// start a cross-domain xmlhttp request.
//
// details should look like:
// {method,url,onload,onerror,onreadystatechange,headers,data}
// headers should be in the form {name:value,name:value,etc}
// can't support mimetype because i think it's only used for forcing
// text/xml and we can't support that
tinasoftdesktop_xmlhttpRequester.prototype = {


	/*
	Function: GET
	Sends a GET request to the server.

	Parameters:
	 url Which url + method to call on the server (string)
	 parameters - Values passed with the call (object)
	 callback - (optional) A function to execute upon completion
	*/	
	GET : function( url, parameters, callback ) {
		  var request = {
				method: 'GET',
				url: url,
				onload: function( resp ) {
					 if ( callback ) {
						  try	{
								console.log( 'tried GET on : ' + url + ' - JSON= ' + resp.responseText );
								if ( resp.status == 200 ) {
									callback( resp.responseJSON, resp.responseText );
								}
								else {
									 alert( "Linktool GET request error = "
										  + resp.status + " "
										  + resp.statusText );
								}
						  }
						  catch( exc ) {
								console.log('GET exception: ' + exc );
								console.log( exc );
								console.log( parameters );
								console.log( callback );
						  }
					 }
					 else {
						  console.log('callback is not defined');
					 }
				},
				onerror: function( err ) {
					 console.log( err );
				},
				headers : {
					 'Content-type': 'application/json'
				},
		  };
		  this.contentStartRequest( request );
	},

	/*
	Function: POST
	Sends a POST request to the server.

	Parameters:
	 url - Which server + method to call on the server (string)
	 parameters - Values passed with the call (object)
	 callback - (optional) A function to execute upon completion
	*/
	POST : function( url, parameters, callback ) {
		var params = JSON.stringify( parameters );
		var request = {
			method: 'POST',
			url: url,
			headers: {
				'Content-Type': 'application/json',
				'Accept': 'application/json',
			},
			data: params,
			onload: function( resp ) {
				if ( callback ) {
					try	{
						  console.log( 'tried POST on : ' + url + ' - JSON= ' + resp.responseText);
						  if ( resp.status == 200 ) {
								callback( resp.responseJSON, resp.responseText );
						  }
						  else {
								alert( "Linktool POST request error = "
									 + resp.status + " "
									 + resp.statusText );
						  }
					}
					catch( exc ) {
						console.log('POST exception: ' + exc );
					}
				}
				else {
					console.log('POST callback is not defined');
				}
			},
			onerror: function( err ) {
				console.log( err );
			},
		  headers : {
				'Content-type': 'application/json'
		  },

		};

		this.contentStartRequest( request );
	},

	/*
	Function: DELETE
	Sends a DELETE request to the server.

	Parameters:
	 url - Which server + method to call on the server (string)
	 parameters - Values passed with the call (object)
	 callback - (optional) A function to execute upon completion
	*/	
	DELETE : function( url, parameters, callback ) {
		var request = {
			method: 'DELETE',
			url: delurl,
			onload: function( resp ) {
				if ( callback ) {
					try	{
						  console.log( 'tried DELETE on : ' + url );
						  if ( resp.status == 200 ) {
								callback( resp.responseJSON, resp.responseText );
						  }
						  else {
								alert( "Linktool DELETE request error = "
									 + resp.status + " "
									 + resp.statusText );
						  }
					}
					catch( exc ) {
						console.log('DELETE exception: ' + exc );	
					}
				}
				else {
					console.log('DELETE callback is not defined');
				}
			},
			onerror: function( err ) {
				console.log( err );
			},
		  headers : {
				'Content-type': 'application/json'
		  },

		};

		this.contentStartRequest( request );
	},

	/*
	Function: LoadFile
	  Loads a URL and executes a callback with the response

	Parameters
	  url - URL of the target file
	  callback - process the file once it's loaded
	*/
	LoadFile : function( url, callback ) {
		console.log('LoadFile: ' + url );
		// Load the URL then execute the callback
		this.contentStartRequest({
			'method': 'GET',
			'url': url,
			'onload': function( response ) {
				console.log( response.responseJSON );
				if (typeof callback == 'function') {
					callback( response );
				}
			},
			'onerror': function( response ) {
				console.log("LoadFile() call failed : " + url );
			}
		});
		return true;
	},

	 contentStartRequest : function(details) {
		 // important to store this locally so that content cannot trick us up with
		 // a fancy getter that checks the number of times it has been accessed,
		 // returning a dangerous URL the time that we actually use it.
		 var url = details.url;

		 // make sure that we have an actual string so that we can't be fooled with
		 // tricky toString() implementations.
		 if (typeof url != "string") {
			 throw new Error("Invalid url: url must be of type string");
		 }

		 var ioService=Components.classes["@mozilla.org/network/io-service;1"]
			 .getService(Components.interfaces.nsIIOService);
		 var scheme = ioService.extractScheme(url);

		 // This is important - without it, contentStartRequest can be used to get
		 // access to things like files and chrome. Careful.
		 switch (scheme) {
			 case "http":
			 case "https":
			 case "ftp":
				 window.setTimeout(
					 StackCaller.hitch(this, "chromeStartRequest", url, details), 0);
				 break;
			 default:
				 throw new Error("Invalid url: " + url);
		 }
	 },

	 // this function is intended to be called in chrome's security context, so
	 // that it can access other domains without security warning
	 chromeStartRequest : function(safeUrl, details) {
		 //var req = new this.chromeWindow.XMLHttpRequest();

		  var req = Components.classes["@mozilla.org/xmlextras/xmlhttprequest;1"]
					 .createInstance();

		  this.setupRequestEvent( req, "onload", details);
		  this.setupRequestEvent( req, "onerror", details);
		  this.setupRequestEvent( req, "onreadystatechange", details);

		  req.open(details.method, safeUrl);
		  if (details.overrideMimeType) {
				req.overrideMimeType(details.overrideMimeType);
		  }
		  if (details.headers) {
				for (var prop in details.headers) {
					 req.setRequestHeader(prop, details.headers[prop]);
				}
		  }
		  req.send(details.data);
	 },

	 // arranges for the specified 'event' on xmlhttprequest 'req' to call the
	 // method by the same name which is a property of 'details' in the content
	 // window's security context.
	 setupRequestEvent : function( req, event, details ) {
		  if (details[event]) {
				req[event] = function() {
					 var responseState = {
						  // can't support responseXML because security won't
						  // let the browser call properties on it
						  // Automatic JSON parsing responseText
						  responseJSON: JSON.parse( req.responseText ),
						  responseText : req.responseText,
						  readyState: req.readyState,
						  responseHeaders: (req.readyState==4?req.getAllResponseHeaders():''),
						  status: (req.readyState==4?req.status:0),
						  statusText: (req.readyState==4?req.statusText:'')
					 }
					 var callback = function() {
						  details[event](responseState);
					 };
					 // Pop back onto browser thread and call event handler.
					 // Have to use nested function here instead of GM_hitch because
					 // otherwise details[event].apply can point to window.setTimeout, which
					 // can be abused to get increased priveledges.
					 new XPCNativeWrapper(window, "setTimeout()")
						  .setTimeout( callback, 0 );
				}
		  }
	 },

}

var xmlhttpRequester = new tinasoftdesktop_xmlhttpRequester();
