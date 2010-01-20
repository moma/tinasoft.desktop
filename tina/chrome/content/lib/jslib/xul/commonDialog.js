const JS_CD_MESSAGE          = "message";
const JS_CD_ALERT            = "alert";
const JS_CD_ERROR            = "error";
const JS_CD_QUESTION         = "question";

const JS_CD_YESNO            = "yesno";
const JS_CD_YES              = "yes";
const JS_CD_NO               = "no";
const JS_CD_OKCANCEL         = "okcancel";
const JS_CD_OK               = "ok";
const JS_CD_CANCEL           = "cancel";

const JS_CD_RIGHT            = "right";
const JS_CD_LEFT             = "left";
const JS_CD_CENTER           = "center";

/**** Internal consts ****/
const JS_CD_BTN_OK           = "OK";
const JS_CD_BTN_CANCEL       = "Cancel";
const JS_CD_BTN_YES          = "Yes";
const JS_CD_BTN_NO           = "No";

const JS_CD_FILE             = "commonDialog.js";

/****************** Globals **********************/

if(typeof(JS_LIB_LOADED)=='boolean')
{

/****************** Common Dialog Object Class *********************/

function CommonDialog(aType, aButtontype, aButtonAlign) {

  return this.initDialog(aType, aButtontype, aButtonAlign);

} // constructor

CommonDialog.prototype  = {

  mDialogArgs      : null,
  mDone            : false,

/********************* INIT Dialog ************************
* void initDialog
*
*   INTERNAL MEMBER FUNCTION
*   This initializes the object and sets the type, buttonType
*   and buttonAlign for the object (if passed in).
*
* param name and discription
*   aType           A dialog type.
*   aButtonType     A "pre-built" set of buttons
*   aButtonAlign    A button alignment
*
* return values on success and failure
*   NA
*
* useage:
*   this.initDialog(type, buttontype, buttonalign);
****************************************************/
initDialog : function(aType, aButtonType, aButtonAlign)
{
  this.mDialogArgs = new Object;
  if(aType) {
    this.type = aType;
  }
  if(aButtonType) {
    this.buttonType = aButtonType;
  }
  if(aButtonAlign) {
    this.mDialogArgs.buttonalign = aButtonAlign;
  }

  // set the left most button as the default button.
  this.buttonDefault = 0;
},

/********************* Type ************************
* void getter Type
*
*   Returns a valid dialog type (if set), null otherwise.
*   Types can be: JS_CD_MESSAGE, JS_CD_ALERT, JS_CD_ERROR 
*   or JS_CD_QUESTION
*
* return values on success and failure
*   A valid type or null if unset.
*
* useage:
*   <type> = obj.type;
*
****************************************************/

get type()
{
  return this.mDialogArgs.type;
},

/********************* Type ************************
* void setter Type
*
*   Sets the type of dialog.  Types can be:
*   JS_CD_MESSAGE, JS_CD_ALERT, JS_CD_ERROR or JS_CD_QUESTION
*
*   This really only changes the icon, but may change other attributes
*   in the future.
*   
* param name and discription
*   aType   A dialog type.
*
* return values on success and failure
*   NA
* useage:
*   obj.type = <type>
****************************************************/
set type(aType)
{
  if(!aType) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set type");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.type = aType;
},

/********************* buttonType ************************
* void setter buttonType
*
*   Sets the buttons to one of the "pre-built" buttons:  
*   JS_CD_MESSAGE, JS_CD_ALERT, JS_CD_ERROR or JS_CD_QUESTION.
*
* param name and discription
*   aType   A "pre-built" set of buttons
*
* return values on success and failure
*   NA
* useage:
*   obj.buttonType = <type>
****************************************************/
set buttonType(aType)
{
  if(!aType) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set buttonType");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.buttonList = new Array;
  if(aType == JS_CD_OKCANCEL) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_OK;
    this.mDialogArgs.buttonList[1] = JS_CD_BTN_CANCEL;
  } else if(aType == JS_CD_OK) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_OK;
  } else if(aType == JS_CD_CANCEL) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_CANCEL;
  } else if(aType == JS_CD_YESNO) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_YES;
    this.mDialogArgs.buttonList[1] = JS_CD_BTN_NO;
  } else if(aType == JS_CD_YES) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_YES;
  } else if(aType == JS_CD_NO) {
    this.mDialogArgs.buttonList[0] = JS_CD_BTN_NO;
  }
},

/********************* defaultButton ************************
* void getter defaultButton
*
*   Returns the number coresponding to the default button.  
*   Buttons are numbered from left to right starting with zero.
*
* return values on success and failure
*   aNumber   The number coresponding to the default button
*
* useage:
*   <number> = obj.defaultButton;
****************************************************/
get defaultButton()
{
  return this.mDialogArgs.defaultButton;
},

/********************* defaultButton ************************
* void setter defaultButton
*
*   Sets he number coresponding to the default button.  Buttons 
*   are numbered from left to right starting with zero.
*
* param name and discription
*   aNum   A number coresponding to the default button.
*
* return values on success and failure
*   NA
*
* useage:
*   obj.buttonType = <type>
****************************************************/
set defaultButton(aNum)
{
  if(!aNum) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set defaultButton");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.defaultButton = aNum;
},

/********************* message ************************
* void getter message
*
*   Returns the current message that is displayed to the user.
*
* return values on success and failure
*   aMessage    The current message.
*
* useage:
*   <string> = obj.message;
****************************************************/
get message()
{
  return this.mDialogArgs.message;
},

/********************* message ************************
* void setter message
*
*   Sets the current message.
*
* param name and discription
*   aMessage   A string to set the message to.
*
* return values on success and failure
*   NA
*
* useage:
*   obj.message = <string>
****************************************************/
set message(aMessage)
{
  if(!aMessage) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set message");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.message = aMessage;
},

/********************* buttonAlign ************************
* void getter buttonAlign
*
*   Returns the current alignment:  JS_CD_LEFT, JS_CD_CENTER, 
*   JS_CD_RIGHT.
*
* return values on success and failure
*   aAlign    Current button alignment
*
* useage:
*   <alignment> = obj.buttonAlign;
****************************************************/
get buttonAlign()
{
  return this.mDialogArgs.buttonalign;
},

/********************* buttonAlign ************************
* void setter buttonAlign
*
*   Sets the current alignment:  JS_CD_LEFT, JS_CD_CENTER, 
*   JS_CD_RIGHT.
*
* param name and discription
*   aAlign    A button alignment
*
* return values on success and failure
*   NA
*
* useage:
*   obj.buttonAlign = <alignment>
****************************************************/
set buttonAlign(aAlignment)
{
  if(!aAlignment) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set buttonAlign");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.buttonalign = aAlignment;
},

/********************* appendButton ************************
* void setter appendButton
*
*   Creates a button and appends it to the right side of the 
*   existing buttons.
*
* param name and discription
*   aLabel    The label for the button
*   aDefault  (optional) set to "true" if it is the default
*             button.
*
* return values on success and failure
*   NA
*
* useage:
*   obj.appendButton(label, true);
****************************************************/
appendButton : function (aLabel, aDefault) 
{
  if(!aLabel) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set appendButton");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  if(!this.mDialogArgs.buttonList) {
    this.mDialogArgs.buttonList = new Array;
  }
  this.mDialogArgs.buttonList.push(aLabel);
  if(aDefault) {
    this.defaultButton = this.mDialogArgs.buttonList.length -1;
  }
},

/********************* title ************************
* void getter title
*
*   Returns the current dialog title.
*
* return values on success and failure
*   aTitle    Current dialog title.
*
* useage:
*   <string> = obj.title;
****************************************************/
get title()
{
  return this.mDialogArgs.title;
},

/********************* title ************************
* void setter buttonAlign
*
*   Sets the current dialog title.
*
* param name and discription
*   aTitle    Sets the current dialog title.
*
* return values on success and failure
*   NA
*
* useage:
*   obj.title = <string>
****************************************************/
set title(aTitle)
{
  if(!aTitle) {
    jslibError(null, "Missing argument\n", 
        "NS_ERROR_XPC_NOT_ENOUGH_ARGS", JS_CD_FILE+": set appendButton");
    throw Components.results.NS_ERROR_XPC_NOT_ENOUGH_ARGS;
  }
  this.mDialogArgs.title = aTitle;
},

/********************* show ************************
* void show
*
*   Creates the dialog and waits for a button push.
*
* param name and discription
*   NA
*
* return values on success and failure
*   aNumber   Returns the button that was clicked.
*
* useage:
*   <number> = obj.show();
****************************************************/
show : function ()
{
  window.openDialog("chrome://jslib/content/xul/content/cdialog.xul",
      "_blank","chrome,resizeable=no,modal,titlebar,close", this.mDialogArgs);
  this.mDone = true;
  return this.mDialogArgs.buttonHit;
},

/********************* result *****************************
* void getter result
*
*   Returns an object with two members:  object.id and 
*   object.text.  "id" is the number ot the button that 
*   was pushed and "label" was the buttons label.
*
* return values on success and failure
*   aObj   An object with the button that was pushed and 
*          the label of that button.
*
* useage:
*   var rv = obj.result;
*   var which = rv.id;
*   var text = rv.text;
****************************************************/
get result() 
{ 
  if (this.mDone) {
    var rv = new Object;
    rv.id = this.mDialogArgs.buttonHit;
    rv.text = this.mDialogArgs.buttonHitText;
    return rv;
  } else {
    return null;
  }
},

/********************* help *****************************
* void getter help
*
*   Returns the methods in this object
*
* return values on success and failure
*   aStr   The methods in this object
*
* useage:
*   <string> = obj.help();
****************************************************/
help  : function()
{

  const help =

    "\n\nFunction and Attribute List:\n"                  +
    "\n"                                                  +
    "   initDialog(aType, aButtonType, aButtonAlign);\n"  +
    "   type = aType;\n"                                  +
    "   buttonType = aButtonType;\n"                      +
    "   defaultButton = aNum;\n"                          +
    "   message = aMessage;\n"                            +
    "   buttonAlign = aAlignment;\n"                      +
    "   appendButton(aLabel, aDefault);\n"                 +
    "   title = aTitle;\n"                                +
    "   show();\n"                                        +
    "   result;\n"

  return help;
} 
} 

} // END BLOCK JS_LIB_LOADED CHECK

// If jslib base library is not loaded, dump this error.
else
{
   dump("JS_BASE library not loaded:\n"
        + " \tTo load use: chrome://jslib/content/jslib.js\n" 

        + " \tThen: include('chrome://jslib/content/xul/commonDialog.js');\n\n");

}; // END FileSystem Class
