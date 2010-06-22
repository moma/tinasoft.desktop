/* ***** BEGIN LICENSE BLOCK *****
 * Version: GNU GPL 3
 * ***** END LICENSE BLOCK ***** */

const HELP_URL = "http://tina.csregistry.org/";
const INTRO_URL = "chrome://tina/content/about.xul";




// wait for the DOM to be loaded
$(document).ready(function() {
    $('#waitMessage').effect('pulsate', {}, 'fast');
    //$("#tabs").tabs( { disabled: [2,3] } );;
    $("#tabs").tabs();
    $('#hide').hide();
    /* restores cache vars */
    var corporaAndPeriods = Cache.getValue( "last_selected_periods", {} );

    $("#tabs").bind('tabsselect', function(event, ui) {

        // MAGIC TRICK FOR THE JAVA IFRAME
        if (ui.index == 2) {
            if (!tinaviz.isEnabled()) {
                resizeApplet();
                tinaviz.setEnabled(true);
            }
            tinaviz.setView("macro");
        } else if (ui.index == 3) {
            if (!tinaviz.isEnabled()) {
                resizeApplet();
                tinaviz.setEnabled(true);
            }
            tinaviz.setView("meso");
        } else {
            // hide the frame; magic!
            tinaviz.setEnabled(false);
            $('#vizframe').css("height","0px");
            $('#vizframe').css("width","0px");

            $('#whitebox').css("height","0px");
            $('#whitebox').css("width","0px");

            $('#infodiv').css("height","0px");
            $('#infodiv').css("width","0px");
            $('#hide').hide();
        }
    });
    var max = 0;
    $("label").each(function(){
        if ($(this).width() > max)
            max = $(this).width();
    });
    $("label").width(max);
    $('#importFile').click(function(event) {
        submitImportfile(event);
    });
    $('#exportCorpora button').click(function(event) {
        submitExportCorpora(event)
    });
    $('#processCooc button').click(function(event) {
        submitprocessCoocGraph(event)
    });
    /*$('#exportGraph button').click(function(event) {
        submitExportGraph(event)
    });*/

    var dupldoc = $( "#duplicate_docs" ).empty().hide();


    /* Fetch data into table */
    displayListCorpora( "graph_table" );
    displayListCorpora( "corpora_table" );

    $(window).bind('resize', function() {
        if (tinaviz.isEnabled()) {
            resizeApplet();
        }
    });


    //No text selection on elements with a class of 'noSelect'
    /*
    $('.noSelect').disableTextSelect();
    $('.noSelect').hover(function() {
        $(this).css('cursor','default');
    }, function() {
        $(this).css('cursor','auto');
    });*/


    var infodiv = new InfoDiv("#infodiv");

    // auto-adjusting infodiv height
    var new_size = tinaviz.getHeight() - 40;
    $(infodiv.id).css( 'height', new_size);

    $(infodiv.id).accordion({
        fillSpace: true,
    });

    // cleans infodiv
    infodiv.reset();
    // passing infodiv to tinaviz is REQUIRED
    tinaviz.infodiv = infodiv;

    // TODO : handler to open a graph file
    $('#htoolbar input[type=file]').change(function(e){
        tinaviz.clear();
        tinaviz.readGraphJava( $(this).val() );
    });

    // all hover and c$( ".selector" ).slider( "option", "values", [1,5,9] );lick logic for buttons

    $(".fg-button:not(.ui-state-disabled)")
    .hover(
        function(){
            $(this).addClass("ui-state-hover");
        },
        function(){
            $(this).removeClass("ui-state-hover");
        }
    )
    .mousedown(function(){
        $(this).parents('.fg-buttonset-single:first').find(".fg-button.ui-state-active").removeClass("ui-state-active");
        if( $(this).is('.ui-state-active.fg-button-toggleable, .fg-buttonset-multi .ui-state-active') ) {
            $(this).removeClass("ui-state-active");
        }
        else {
            $(this).addClass("ui-state-active");
        }
    })
    .mouseup(function(){
        if(! $(this).is('.fg-button-toggleable, .fg-buttonset-single .fg-button,  .fg-buttonset-multi .fg-button') ) {
            $(this).removeClass("ui-state-active");
        }
    });

    // binds the click event to tinaviz.searchNodes()

    $("#macro-search").submit(function() {
      var txt = $("#macro-search-input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
     });
      $("#meso-search").submit(function() {
      var txt = $("#meso-search-input").val();
      if (txt=="") {
            tinaviz.unselect();
      } else {
            tinaviz.searchNodes(txt, "containsIgnoreCase");
      }
      return false;
    });
    /*
    $("#search").keypress(function() {
      var txt = $("#search-input").val();
      if (txt=="") {
        tinaviz.unselect();
      } else {
           tinaviz.highlightNodes(txt, "containsIgnoreCase");
      }
    });
    */
    $("#macro-search-button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#macro-search-input").val();
          if (txt=="") {
                tinaviz.unselect();
          } else {
                tinaviz.searchNodes(txt, "containsIgnoreCase");
          }
    });

    $("#meso-search-button").button({
        text: false,
        icons: {
            primary: 'ui-icon-search'
        }
    }).click( function(eventObject) {
          var txt = $("#meso-search-input").val();
          if (txt=="") {
                tinaviz.unselect();
          } else {
                tinaviz.searchNodes(txt, "containsIgnoreCase");
          }
    });

    // SLIDERS INIT
    $.extend($.ui.slider.defaults, {
        //range: "min",
        min: 0,
        max: 100,
        value: 100.0,
        animate: true,
        orientation: "horizontal",
    });

    // MACRO SLIDERS
    $("#macro-sliderEdgeWeight #meso-sliderEdgeWeight").slider({
        range: true,
        values: [0, 100],
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "edgeWeight/min", ui.values[0] / 100.0);
            tinaviz.setProperty("current", "edgeWeight/max", ui.values[1] / 100.0);
            tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }
    });

    $("#macro-sliderNodeWeight #meso-sliderNodeWeight").slider({
        range: true,
        values: [0, 100],
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "nodeWeight/min", ui.values[0] / 100.0);
            tinaviz.setProperty("current", "nodeWeight/max", ui.values[1] / 100.0);
            tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }
    });

    $("#macro-sliderNodeSize  #meso-sliderNodeSize").slider({
        value: 50.0,
        max: 100.0,// precision/size
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "output/nodeSizeRatio", ui.value / 100.0);
            //tinaviz.resetLayoutCounter();
            tinaviz.touch();
        }}
    );

    $("#macro-sliderSelectionZone #meso-sliderSelectionZone").slider({
        value: 1.0,
        max: 300.0, // max disk radius, in pixel
        animate: true,
        slide: function(event, ui) {
            tinaviz.setProperty("current", "selection/radius", ui.value);
            tinaviz.touch();
        }
    });

    $("#macro-toggle-showLabels #meso-toggle-showLabels").click(function(event) {
        tinaviz.toggleLabels();
    });

    $("#macro-toggle-showNodes #meso-toggle-showNodes").click(function(event) {
        tinaviz.toggleNodes();
    });

    $("#macro-toggle-showEdges #meso-toggle-showEdges").click(function(event) {
        tinaviz.toggleEdges();
    });

    $("#macro-toggle-paused #meso-toggle-paused").button({
        icons: {primary:'ui-icon-pause'},
        text: true,
        label: "pause",
    })
    .click(function(event) {
        tinaviz.togglePause();
        if( $("#macro-toggle-paused").button('option','icons')['primary'] == 'ui-icon-pause'  ) {
            $("#macro-toggle-paused").button('option','icons',{'primary':'ui-icon-play'});
            $("#macro-toggle-paused").button('option','label',"play");
        }
        else {
            $("#macro-toggle-paused").button('option','icons',{'primary':'ui-icon-pause'});
            $("#macro-toggle-paused").button('option','label',"pause");
        }
    });
    $("#meso-toggle-paused").button({
        icons: {primary:'ui-icon-pause'},
        text: true,
        label: "pause",
    })
    .click(function(event) {
        tinaviz.togglePause();
        if( $("#meso-toggle-paused").button('option','icons')['primary'] == 'ui-icon-pause'  ) {
            $("#meso-toggle-paused").button('option','icons',{'primary':'ui-icon-play'});
            $("#meso-toggle-paused").button('option','label',"play");
        }
        else {
            $("#meso-toggle-paused").button('option','icons',{'primary':'ui-icon-pause'});
            $("#meso-toggle-paused").button('option','label',"pause");
        }
    });
    $("#macro-toggle-unselect #meso-toggle-unselect").button({
        icons: {primary:'ui-icon-close'},
    }).click(function(event) {
        tinaviz.unselect();
    });

    $("#macro-toggle-autoCentering #meso-toggle-autoCentering").button({
        text: true,
        icons: {
            primary: 'ui-icon-home'
        }
    })
    .click(function(event) {
        tinaviz.autoCentering();
    });

    $("#macro-toggle-switch #meso-toggle-switch").button({
        text: true,
        icons: {
            primary: 'ui-icon-arrows'
        },
    }).click(function(event) {
        tinaviz.toggleCategory("current");
    });

    // magic trick for applet loading rights

    var DIR_SERVICE = new Components.Constructor("@mozilla.org/file/directory_service;1", "nsIProperties");
    var path = (new DIR_SERVICE()).get("AChrom", Components.interfaces.nsIFile).path;
    var appletPath;
    if (path.search(/\\/) != -1) { appletPath = path + "\\content\\applet\\index.html" }
    else { appletPath = path + "/content/applet/index.html" }
    var appletFile = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
    appletFile.initWithPath(appletPath);
    var appletURL = Components.classes["@mozilla.org/network/protocol;1?name=file"].createInstance(Components.interfaces.nsIFileProtocolHandler).getURLSpecFromFile(appletFile);
    var iframehtml = '<iframe id="vizframe" name="vizframe" class="vizframe" allowtransparency="false" scrolling="no" frameborder="0" src="'+appletURL+'"></iframe>';
    window.setTimeout("$('#container').html('"+iframehtml+"');", 3000);

    $("#tabs-1-accordion").accordion({
        autoHeight: false,
        clearStyle: true,
    });
    $("#importForm").accordion({
        autoHeight: false,
        clearStyle: true,
    });
});
