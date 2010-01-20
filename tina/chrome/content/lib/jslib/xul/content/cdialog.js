var gOptions = null;
var gButtonList = null;

function onStartupLoad()
{
  if (window.arguments && window.arguments[0])
  {
    gOptions = window.arguments[0];
    setupImage();
    setupMessage();
    setupButtons();
    if (gOptions.title)
      setTimeout("setupTitle();", 10);
  }
}

//----------------------------------------------------------------------------------
function setupImage()
{
  // setup the image...
  if (gOptions.type) {
    var imagebox = document.getElementById("imageBox");
    var image = document.createElement("image");
    if (gOptions.type == "message") {
      image.setAttribute("class", "message-icon");
    }
    else if (gOptions.type == "alert") {
      image.setAttribute("class", "alert-icon");
    }
    else if (gOptions.type == "error") {
      image.setAttribute("class", "error-icon");
    }
    else if (gOptions.type == "question") {
      image.setAttribute("class", "question-icon");
    }
    imagebox.appendChild(image);
  }
}

function setupMessage()
{
  // setup up the text...
  if (gOptions.message) {
    try {
      var el = document.getElementById("messageText");
      el.firstChild.data = gOptions.message;
    } catch (e) { dump(e); }
  }
}

function setupButtons()
{
  var align = "center";
  var bbox = document.getElementById("buttonBox");

  gButtonList = new Array;

  // set up the buttons.
  if (gOptions.buttontype) {
    if (gOptions.buttontype == "okcancel") {
      gButtonList[0] = "OK";
      gButtonList[1] = "Cancel";
    } else if (gOptions.buttontype == "ok") {
      gButtonList[0] = "OK";
    } else if (gOptions.buttontype == "cancel") {
      gButtonList[0] = "Cancel";
    } else if (gOptions.buttontype == "yesno") {
      gButtonList[0] = "Yes";
      gButtonList[1] = "No";
    } else if (gOptions.buttontype == "yes") {
      gButtonList[0] = "Yes";
    } else if (gOptions.buttontype == "no") {
      gButtonList[0] = "No";
    } 
  } else {
    gButtonList = gOptions.buttonList;
  }

  if (!gOptions.defaultButton) {
    gOptions.defaultButton = gButtonList.length -1;
  }

  if (gOptions.buttonalign) {
    align = gOptions.buttonalign;
  }

  // left spring
  var lspring = document.createElement("spacer");
  if (align == "left") {
    lspring.setAttribute("style", "width: 10px;");
  } else {
    lspring.setAttribute("flex", "1");
  }
  bbox.appendChild(lspring);

  // buttons...
  for(var i=0; i<gButtonList.length; i++) {
    var button = document.createElement("button");
    button.setAttribute("label", gButtonList[i]);
    button.setAttribute("style", "margin-left: 5px; margin-right:5px;");
    button.setAttribute("oncommand", "doButton("+i+");");
    button.setAttribute("bnum", i);
    button.setAttribute("id", "bID"+i);
    if (i == gOptions.defaultButton) {
      button.setAttribute("default", "true");
    }
    bbox.appendChild(button);
  }

  // right spring
  var rspring = document.createElement("spacer");
  if (align == "right") {
    rspring.setAttribute("style", "width: 10px;");
  } else {
    rspring.setAttribute("flex", "1");
  }
  bbox.appendChild(rspring);

  button = document.getElementById("bID"+gOptions.defaultButton);
  if (button) {
    button.focus();
  }
}

//----------------------------------------------------------------------------------
function setupTitle() { document.title = gOptions.title; }

function doButton(which) {
  var button = document.getElementById("bID"+which);
  if (button) {
    gOptions.buttonHitText = button.getAttribute("label");
    gOptions.buttonHit = button.getAttribute("bnum");

    window.close();
  }
}

function doEnter() {
  var button = document.getElementById("bID"+gOptions.defaultButton);
  gOptions.buttonHitText = button.getAttribute("label");
  gOptions.buttonHit = button.getAttribute("bnum");
  
  window.close();
}

function doEsc() {
  gOptions.buttonHit = "_none_";
  window.close();
}
