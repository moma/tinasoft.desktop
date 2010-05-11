package tinaviz;

//import com.nativelibs4java.opencl.CLBuildException;
import eu.tinasoft.services.visualization.layout.Layout;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.data.model.ShapeCategory;
import eu.tinasoft.services.visualization.views.ViewLevel;
import eu.tinasoft.services.visualization.views.View;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.security.KeyException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
//import megamu.mesh.Hull;

import eu.tinasoft.services.session.*;
//import processing.opengl.*;
import processing.core.*;
import processing.xml.*;
import processing.pdf.*;
import netscape.javascript.*;

import eu.tinasoft.services.protocols.browser.LiveConnector;
import eu.tinasoft.services.formats.json.JSONException;
import eu.tinasoft.services.formats.json.JSONStringer;
import eu.tinasoft.services.formats.json.JSONWriter;
import eu.tinasoft.services.data.model.Node;
//import tinaviz.layout.LayoutOpenCL;
import eu.tinasoft.services.computing.MathFunctions;
import eu.tinasoft.services.data.model.NodeList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
//import peasy.*;

public class Main extends PApplet implements MouseWheelListener {

    boolean generateRandomLocalGraph = false;
    boolean loadDefaultLocalGraph = false;
    boolean loadDefaultGlobalGraph = false;
    boolean generateRandomGlobalGraph = false;
    public PImage currentImg = null;
    public PVector ref = new PVector();
    public PVector drawerTranslation = new PVector();
    public PVector drawerLastPosition = new PVector();
    public Layout layout;
    static int MAXLINKS = 512;
    float zoomRatio = 1.0f;
    private float ARCTAN_12 = (float) (2.0 * Math.atan(1.0 / 2.0));
    public PImage brandingImage = null;
    public String brandingImageURL = "";
    public boolean showBranding = true;
    PImage nodeIcon;
    PFont font;
    XMLElement xml;
    Session session = new Session();
    // pourcentage de l'ecran pour lequel la présence des bords d'un node
    // déclenche le passage dans le mode macro
    //float screenRatioSelectNodeWhenZoomed = 0.22f;
    //float screenRatioGoToMesoWhenZoomed = 0.55f;
    float screenRatioSelectNodeWhenZoomed = 0.4f;
    float screenRatioGoToMesoWhenZoomed = 0.7f;
    AtomicBoolean zooming = new AtomicBoolean(false);
    AtomicBoolean zoomIn = new AtomicBoolean(false);
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    AtomicBoolean mouseClickLeft = new AtomicBoolean(false);
    AtomicBoolean mouseClickRight = new AtomicBoolean(false);
    AtomicBoolean debug = new AtomicBoolean(false);
    public static JSObject window = null;
    private int recordingWidth = 100;
    private int recordingHeight = 100;
    private String DEFAULT_FONT = "ArialMT-150.vlw";
    private NodeList nodes = new NodeList();
    float selectedX = 0.0f;
    float selectedY = 0.0f;
    PVector lastMousePosition = new PVector(0, 0, 0);
    float MAX_NODE_RADIUS = 0.7f; // node radius is normalized to 1.0 for each node, then mult with this value
    float MAX_EDGE_WEIGHT = 1.0f; // node radius is normalized to 1.0 for each node, then mult with this value
    float MAX_EDGE_THICKNESS = 20.0f;
    private Long selectNode = null;
    int oldScreenWidth = 0;
    int oldScreenHeight = 0;
    private Node oldSelected = null;
    private boolean useOpenCL = false;
    private boolean recenter = true;
    private boolean alwaysAntiAliasing = false;
    private float oldZoomScale = -1f;
    private float realWidth = 0.0f;
    private PVector cameraDelta = new PVector(0.0f, 0.0f, 0.0f);
    private int bezierSize = 18;
    private int backgroundColor = color(255, 255, 255);
    private int shownEdges = 0;
    private int shownNodes = 0;
    AtomicBoolean redrawScene = new AtomicBoolean(true);
    public String js_context = "";

    //PeasyCam cam;
    private void nodeSelectedLeftMouse_JS_CALLBACK(Node n) {

        if (n != null) {
            selectedX = n.x;
            selectedY = n.y;
        }

        if (window == null) {
            return; // in debug mode
        }
        if (n == null) {
            window.eval("setTimeout(\"" + js_context + "tinaviz.nodeSelected('" + session.getLevel() + "',0,0,null,null,null,'left');\",1);");
        } else {
            window.eval("setTimeout(\"" + js_context + "tinaviz.nodeSelected('" + session.getLevel() + "',"
                    + screenX(n.x, n.y) + "," + screenY(n.x, n.y) + ",'"
                    + n.uuid + "','"
                    + n.label + "', '"
                    + escape(n.getAttributesAsJSON())
                    + "','left');\",1);");
        }
    }

    private String escape(String str) {
        return str.replace("\"", "\\\"");
    }

    private void nodeSelectedRightMouse_JS_CALLBACK(Node n) {

        if (n != null) {
            selectedX = n.x;
            selectedY = n.y;
        }

        if (window == null) {
            return; // in debug mode
        }
        if (n == null) {
            window.eval("setTimeout(\"" + js_context + "tinaviz.nodeSelected('" + session.getLevel() + "',0,0,null,null,null,'right');\",1);");
        } else {
            window.eval("setTimeout(\"" + js_context + "tinaviz.nodeSelected('" + session.getLevel() + "',"
                    + screenX(n.x, n.y) + "," + screenY(n.x, n.y) + ",'"
                    + n.uuid + "','"
                    + n.label + "', '"
                    + escape(n.getAttributesAsJSON())
                    + "','right');\",1);");
        }
    }

    private void jsSwitchToMacro() {
        session.toMacroLevel();
        if (window != null) {
            window.eval("setTimeout(\"" + js_context + "tinaviz.switchedTo('macro');\",1);");
        }

    }

    private void jsSwitchToMeso() {
        session.toMesoLevel();
        if (window != null) {
            window.eval("setTimeout(\"" + js_context + "tinaviz.switchedTo('meso');\",1);");
        }
    }

    private void jsSwitchToMicro() {
        session.toMicroLevel();
        if (window != null) {
            window.eval("setTimeout(\"" + js_context + "tinaviz.switchedTo('micro');\",1);");
        }
    }

    private void drawNothing(View v) {
        translate(v.translation.x, v.translation.y);
        scale(v.sceneScale);
    }

    public enum RecordingFormat {

        NONE, CURRENT_PICTURE, PDF, BIG_PICTURE;
    };

    enum quality {

        FASTEST, // no stroke on circle, no stroke weight
        FASTER, // no stroke on circle, no stroke weight
        FAST, // no label
        NORMAL,
        SLOW // round corners
    }

    @Override
    public void setup() {
        layout = new Layout();
        /*
        try {
        layout = new LayoutOpenCL();
        } catch (CLBuildException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/



        // font = loadFont(DEFAULT_FONT);
        font = createFont("Arial", 150, true);
        //String[] fontList = PFont.list();
        //println(fontList);

        if (getParameter("opencl") != null) {
            if (getParameter("opencl").equalsIgnoreCase("true")) {
                useOpenCL = true;
            }
        }

        if (getParameter("js_context") != null) {

            js_context = getParameter("js_context");

        }

        Console.setPrefix(js_context);

        String engine = P2D;
        if (getParameter("engine") != null) {
            if (getParameter("engine").equals("software")) {
                engine = P2D;

            } else if (getParameter("engine").equals("hardware")) {
                engine = OPENGL;

            } else if (getParameter("engine").equals("hybrid")) {
                engine = JAVA2D;

            }

            window = JSObject.getWindow(this);
            session.setBrowser(new LiveConnector(window));
            int w = 200;
            int h = 200;
            /*Object o = window.call(""+js_context+"tinaviz.getWidth", null);
            if (o != null) {
            if (o instanceof Double) {
            w = ((Double) o).intValue();
            }
            }
            o = window.call(""+js_context+"tinaviz.getHeight", null);
            if (o != null) {
            if (o instanceof Double) {
            h = ((Double) o).intValue();
            }
            }*/

            size(w, h, engine);

        } else {
            session.setBrowser(new LiveConnector());
            loadDefaultGlobalGraph = true;
            size(screenWidth, screenHeight, engine);
        }

        if (engine.equals(OPENGL)) {
            smooth();
            frameRate(60);
            textFont(font, 120);
            bezierDetail(48);
        } else {
            smooth();
            frameRate(25);
            textFont(font, 32);
            bezierDetail(bezierSize);
        }

        rectMode(CENTER);

        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully


        layout = new Layout();
        /*
        if (useOpenCL) {
        try {
        layout = new LayoutOpenCL();
        } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CLBuildException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        }*/

        // currentView.showLabels = false;

        /*
        SecurityManager appsm = System.getSecurityManager();
        if (appsm != null) {
        appsm.checkPermission(new FilePermission("*","read"));
        } else {

        }
         * *
         */

        if (loadDefaultLocalGraph) {

            String gexf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gexf><graph><attributes class=\"node\">"
                    + "</attributes><tina></tina><nodes><node id=\"432561326751248\" label=\"this is an ngram\">"
                    + "<attvalues><attvalue for=\"0\" value=\"NGram\" /></attvalues></node><node id=\"715643267560489\" label=\"TINA PROJECT\">"
                    + "<attvalues><attvalue for=\"0\" value=\"project\" />"
                    + "  </attvalues>"
                    + "  </node>"
                    + " </nodes>"
                    + " <edges>"
                    + "<edge id=\"0\" source=\"9\" target=\"432561326751248\" weight=\"1.0\" />"
                    + " <edge id=\"1\" source=\"9\" target=\"715643267560489\" weight=\"1.0\"/>"
                    + "</edges>"
                    + "</graph></gexf>";
            session.getMeso().getGraph().updateFromString(gexf);

        } else if (generateRandomLocalGraph) {

            NodeList tmp = new NodeList();
            Node node;
            System.out.println("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 15.0f;

            Node root = new Node(0, "root node", radius, 0, 0);
            root.weight = random(1.0f);
            root.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
            root.label = root.category + " " + root.label;
            root.fixed = true;

            for (int i = 0; i < 50; i++) {
                radius = random(3.0f, 5.0f);
                node = new Node(i, "node " + i, radius, random(-20), random(20));
                node.weight = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
                node.label = node.category + " " + node.label;
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                root.addNeighbour(tmp.get(i), 0.1f + random(1.0f));

            }
            tmp.add(root);
            Console.log("Generated " + tmp.size() + " nodes!");

            session.getMeso().getGraph().updateFromNodeList(tmp);

            //session.animationPaused = true;
        }

        if (loadDefaultGlobalGraph) {
            Console.log("loading default graph..");
            session.getMacro().getGraph().updateFromURI(
                    "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/FET60bipartite_graph_cooccurrences_.gexf");
                    //"file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/CSSScholarsMay2010.gexf");

            try {
                session.getMacro().setProperty("cat/value", "Document");
            } catch (KeyException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            session.getMacro().addFilter("Category", "cat");
            session.getView().paused = true;

        } else if (generateRandomGlobalGraph) {

            NodeList tmp = new NodeList();
            Node node;
            Console.log("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 0.0f;
            for (int i = 0; i < 200; i++) {
                radius = random(3.0f, 10.0f);

                node = new Node(i, "node " + i, radius, random(width / 2), random(height / 2));
                node.weight = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
                node.label = node.category + " " + node.label;
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                for (int j = 0; j < tmp.size() && i != j; j++) {
                    if (random(1.0f) < 0.009) { // link density : 0.02 = a lot, 0.0002 = a few
                        tmp.get(i).addNeighbour(tmp.get(j), 0.01f + random(1.0f));
                    }
                }
            }
            session.getMacro().getGraph().updateFromNodeList(tmp);


            //session.animationPaused = true;
        }
        //cenNGramesoView();
        //session.toMesoLevel();

        // in the case the reset method take the graph radius in account to zoom (but its still not the case)
        session.toMacroLevel();
        // session.toMesoLevel();

        // DEBUG MODE
        session.macro.prespatializeSteps = 0;

        lastMousePosition = new PVector(width / 2.0f, height / 2.0f, 0);
        // fill(255, 184);

        //brandingImage = getImage(new URL("tina_icon.gif"));
        //brandingImage = loadImage("moma-crea.png");

        //Console.log("Starting visualization..");
        if (window != null) {
            window.eval("" + js_context + "tinaviz.init();");
        }
        Console.log("Visualization started..");

        /*
        cam = new PeasyCam(this, 100);
        cam.setMinimumDistance(50);
        cam.setMaximumDistance(500);*/

    }

    @Override
    public void draw() {
        View v = session.getView();

        // HACK
        v.screenWidth = width;
        v.screenHeight = height;

        if (!this.isEnabled()) {
            if (v.prespatializeSteps-- > 0) {
                layout.slowWithLabelAdjust(v, nodes);

            }
            return;
        }

        NodeList n = v.popNodes();
        if (n != null) {
            redrawScene.set(true);
        }


        if (redrawScene.get()) {
            draw2(v, n);
        } else {
            // hack (we need to setup the matrix so processing can store object's positions)
            drawNothing(v);
        }


        redrawScene.set(!v.paused);

    }

    public void draw2(View v, NodeList n) {

        //if (!doLoop.get()) noLoop();


        // todo replace by get network

        //boolean cameraUpdateNeeded = false;
        // System.out.println("now working on view "+v);

        nodes.autocenter = false;
        if (n != null) {
            //System.out.println("pop nodes gave something! overwriting node screen cache..");

            //System.out.println(n.toString());
            /*
            for (Node nd : n.nodes) {
            if (nd.original != null) {
            nd.x = nd.original.x;
            nd.y = nd.original.y;
            }
            }
             */
            nodes.clear();
            nodes.addAll(n);

            // HACK
            nodes.autocenter = v.graph.topologyChanged.getAndSet(false);
            if (nodes.autocenter) {
                oldZoomScale = -1f;
            }
            //if (nodes.size() < 1) return;

        } else {
        }

        if (recenter | nodes.autocenter) {

            float screenRadius = (width + height) / 2.0f;
            nodes.computeExtremums();

            v.sceneScale = nodes.graphRadius > 0 ? (screenRadius * 0.3f / nodes.graphRadius) : 1.0f;


            //System.out.println("got " + nodes.size() + " nodes");
            /*! TODO !*/
            //System.out.println("width: " + width + " heihgt: " + height );
            //System.out.println("zoomscale = screenRadius / graphRadius = " + screenRadius + " / " + nodes.graphRadius + " = " + v.sceneScale);
            PVector baryCenter = new PVector(nodes.baryCenter.x, nodes.baryCenter.y);
            PVector translate = new PVector();
            translate.set(baryCenter);
            //System.out.println("baryCenter x:" + nodes.baryCenter.x + " y:" + nodes.baryCenter.y);

            //System.out.println("translation1 x:" + translate.x + " y:" + translate.y);

            translate.set(PVector.div(translate, v.sceneScale));

            PVector screenCenter = new PVector(width / 2.0f, height / 2.0f, 0);
            //PVector screenCenterScaled = PVector.div(screenCenter);
            translate.add(screenCenter);

            //PVector screenCenterScaled = PVector.div(screenCenter, v.sceneScale);

            //System.out.println("translation1 x:" + translate.x + " y:" + translate.y);

            v.translation.set(translate);

            if (abs(oldZoomScale - v.sceneScale) <= 0.5) {
                recenter = false;
                System.out.println("stabilization reached, disabling recentering");
                oldZoomScale = v.sceneScale;
            } else {

                oldZoomScale = v.sceneScale;

            }
        }
        /*
        switch (v.centeringMode) {
        case FREE_MOVE:

        if (cameraUpdateNeeded) {
        System.out.println("Camera Update Needed in FREE MOVE mode!");
        // metrics are already recomputed
        v.resetZoom();
        v.resetToGraphBarycenter();
        }

        break;
        case GLOBAL_GRAPH_BARYCENTER:
        System.out.println("GLOBAL_GRAPH_BARYCENTER!");

        if (cameraUpdateNeeded) {

        v.resetZoom();
        }
        v.resetToGraphBarycenter();


        break;
        case SELECTED_GRAPH_BARYCENTER:
        System.out.println("SELECTED_GRAPH_BARYCENTER!");
        if (cameraUpdateNeeded) {

        v.resetZoom();
        }
        v.resetToSelectionBarycenter();

        break;
        }*
         *
         */


        //session.animationPaused = tmp; // TODO replace by a lock here
        //preSpatialize = 60;

        // TODO put this in another thread
        if (recordingMode != RecordingFormat.NONE) {

            if (recordingMode == RecordingFormat.PDF) {
                pdfDrawer(v, width, height);
            } else if (recordingMode == RecordingFormat.CURRENT_PICTURE) {
                pictureDrawer(v, width, height);
                //save(recordPath);
            } else if (recordingMode == RecordingFormat.BIG_PICTURE) {
                pictureDrawer(v, recordingWidth, recordingHeight);
            }
            recordingMode = RecordingFormat.NONE;

        } else if (v.prespatializeSteps-- > 0) {
            drawLoading(v);
            layout.macroLayout_approximate(v, nodes);

        } else {
            drawAndSpatializeRealtime(v);
        }
    }

    public void drawLoading(View net) {
        background(255);
        fill(123);
        textSize(80);
        String base = "Loading";
        float x = width / 2.0f - 150;
        float y = height / 2.0f;

        if (net.prespatializeSteps > 55) {
        } else if (net.prespatializeSteps > 50) {
            base = base + ".";
        } else if (net.prespatializeSteps > 45) {
            base = base + "..";
        } else if (net.prespatializeSteps > 40) {
            base = base + "...";
        } else if (net.prespatializeSteps > 30) {
            base = base + ".";
        } else if (net.prespatializeSteps > 25) {
            base = base + "..";
        } else if (net.prespatializeSteps > 20) {
            base = base + "...";
        } else if (net.prespatializeSteps > 10) {
            base = base + ".";
        } else if (net.prespatializeSteps > 5) {
            base = base + "..";
        } else {
            base = base + "...";
        }

        if (net.prespatializeSteps > 0 && net.prespatializeSteps < 30) {
            fill(240 - 4 * net.prespatializeSteps);
        } else if (net.prespatializeSteps <= 0) {
            fill(255);
        }
        text(base, x, y);
    }

    public void pdfDrawer(View v, int w, int h) {
        PGraphicsPDF pdf = (PGraphicsPDF) createGraphics(w, h, PDF, "/tmp/out.pdf");
        pdf.beginDraw();

        pdf.textMode(MODEL); // SHAPE : bigger file size, not searchable

        // doesn't work.. yet.
        PFont f = createFont("FreeSansBoldOblique.ttf", 96, true);
        pdf.textFont(f); // TODO: try "createFont"

        pdf.hint(ENABLE_NATIVE_FONTS);

        //beginRecord(pdf);
        pdf.smooth();
        println(pdf.listFonts());
        //pdf.textMode(MODEL); // TODO: try "MODEL"
        pdf.background(255);
        pdf.fill(30);
        pdf.textSize(40);
        pdf.text("TinaSoft", 15f, 50f);
        pdf.fill(80);
        pdf.textSize(18);
        pdf.text("A cool project subtitle", 18f, 70f);
        pdf.fill(120);

        genericDrawer(v, pdf, w, h);

        pdf.dispose();
        pdf.endDraw();
    }

    public void pictureDrawer(View net, int w, int h) {
        // todo: we can create a tilling system right here ;)
        PGraphics pg = createGraphics(w, h, JAVA2D);
        //PFont f = createFont("Arial", 96, true);

        pg.beginDraw();
        pg.textMode(MODEL);
        pg.smooth();
        pg.textFont(font);
        pg.background(255);

        pg.fill(30);
        pg.textSize(40);
        pg.text("TinaSoft", 15f, 50f);

        pg.fill(80);
        pg.textSize(18);
        pg.text("A cool project subtitle", 18f, 70f);
        pg.fill(120);

        genericDrawer(net, pg, w, h);
        pg.endDraw();
        pg.save(recordPath);
    }

    public void genericDrawer(View net, PGraphics pg, int w, int h) {
        bezierDetail(90);
        background(255);
        stroke(150, 150, 150);
        strokeWeight(1);

        nodes.sortBySelectionStatus();
    }

    public void drawAndSpatializeRealtime(View v) {

        // sadly, we can't use a linear scale, it would not be efficient
        boolean antialiasing = false;
        int resolution = 0;

        if (shownEdges < 150) {
            resolution = 60;
        } else if (shownEdges < 300) {
            resolution = 50;
        } else if (shownEdges < 600) {
            resolution = 40;
        } else if (shownEdges < 900) {
            resolution = 35;
        } else if (shownEdges < 1500) {
            resolution = 30;
        } else if (shownEdges < 3000) {
            resolution = 25;
        } else if (shownEdges < 4000) {
            resolution = 20;
        } else if (shownEdges < 5000) {
            resolution = 15;
        } else if (shownEdges < 6000) {
            resolution = 12;
        } else if (shownEdges < 8000) {
            resolution = 10;
        } else if (shownEdges < 10000) {
            resolution = 7;
        } else if (shownEdges < 15000) {
            resolution = 6;
        } else {
            resolution = 6;
        }

        antialiasing = (resolution >= 50);
        bezierDetail(resolution);

        if (antialiasing) {
            smooth();
        } else {
            noSmooth();
        }


        if (!v.paused) {
            layout.macroLayout_approximate(v, nodes);
        }

        // TODO optimize here
        nodes.sortBySelectionStatus();


        background(255);
        //background(53,59,61);
        stroke(150, 150, 150);
        strokeWeight(1);


        /*

        loadPixels();
        for (float y = 0; y < height; y++) {
        float grad = (float) (y * 30f / (float)height);
        int colo = (int) color(255f-grad, 255f-grad, 255f-grad);

        for (float x = 0; x < width; x++) {
        pixels[(int) (x + y * width)] = colo;
        }

        }
        updatePixels();
         *
         */

        fill(80);
        textSize(12);

        if (debug.get()) {
            text("" + ((int) frameRate) + " img/sec", 10f, 13f);
            text("" + shownNodes + "/" + nodes.size() + " nodes", 80f, 13f);
            text("" + shownEdges + "/" + nodes.nbEdges + " edges", 190f, 13f);
            text("aliasing: " + (resolution >= 40) + "    resolution: " + resolution, 310f, 13f);
            fill(0);
        }
        shownNodes = 0;
        shownEdges = 0;
        //pushMatrix();

        translate(v.translation.x, v.translation.y);
        scale(v.sceneScale);

        //pushMatrix();
        Node hasSelected = null;

        /*

        int x=0;
        float[][] points = new float[nodes.nodes.size()][2];
        for (Node n : nodes.nodes) {
        if (n.selected) {
        points[x][0] = n.x;
        points[x][1] = n.b;
        x++;
        }
        }

        Hull myHull = new Hull( points );
        fill(0,130,200);
        points = myHull.getRegion().getCoords();
        beginShape();
        for (int i=0;i<points.length;i++) {
        curveVertex(points[i][0], points[i][1]);
        }
        endShape();
         */
        noFill();

        for (Node n : nodes.nodes) {
            n.screenX = screenX(n.x, n.y);
            n.screenY = screenY(n.x, n.y);
            n.visibleToScreen = (n.screenX > -(width / 4.0f)
                    && n.screenX < width + (width / 4.0f)
                    && n.screenY > -(height / 4.0f)
                    && n.screenY < height + (height / 4.0f));
        }

        for (Node n1 : nodes.nodes) {


            if (selectNode != null) {
                if (selectNode == n1.id) {
                    n1.selected = true;
                }
            }



            for (Node n2 : nodes.nodes) {

                if (n1 == n2) {
                    continue;
                }
                if (!v.showLinks) {
                    if (!n1.selected && !n1.highlighted) {
                        continue;
                    }
                }
                if (!n1.visibleToScreen && !n2.visibleToScreen) {
                    continue;
                }
                // skip the node if the following cases


                boolean directed = n1.weights.containsKey(n2.id);
                boolean mutual = n2.weights.containsKey(n1.id);
                if (!directed) {
                    if (!mutual) {
                        continue;
                    }
                }

                float weightN1_2_N2 = (Float) n1.weights.get(n2.id);
                float weightN2_2_N1 = (Float) n2.weights.get(n1.id);

                // only print the edge in one direction
                if (n1.weight > n2.weight) {
                    // print
                } else if (weightN1_2_N2 > weightN2_2_N1) {
                    // print
                } else if (n1.id > n2.id) {
                    // print
                } else {
                    continue;
                }


                shownEdges++;
                // if we want to draw the links, or if we clicked on a node
                // or if we put the mouse over a node




                // compute the average node color
                float cr = (n1.r + n2.r) / 2;
                float cg = (n1.g + n2.g) / 2;
                float cb = (n1.b + n2.b) / 2;

                // compute the edge color
                /*
                if (mutual) {
                float m = 180.0f;
                float r = (255.0f - m) / 255.0f;

                if (n1.selected && n2.selected) {
                stroke(constrain(m + cr * r, 0, 255),
                constrain(m + cg * r, 0, 255),
                constrain(m + cb * r, 0, 255),
                constrain(n1.weights.get(n2.id) * 205, 50, 255));
                } else if (n1.selected || n2.selected) {
                stroke(constrain(m + cr * r, 0, 255),
                constrain(m + cg * r, 0, 255),
                constrain(m + cb * r, 0, 255),
                constrain(n1.weights.get(n2.id) * 205, 50, 255));
                } else {
                stroke(constrain(m + cr * r, 0, 255),
                constrain(m + cg * r, 0, 255),
                constrain(m + cb * r, 0, 255),
                constrain(n1.weights.get(n2.id) * 205, 50, 255));
                }
                } else if (directed) {*/
                float m = 160.0f;
                float r = (255.0f - m) / 255.0f;
                if (n1.selected || n2.selected) {
                    stroke(constrain((m + cr * r) * 0.4f, 0, 255),
                            constrain((m + cg * r) * 0.4f, 0, 255),
                            constrain((m + cb * r) * 0.4f, 0, 255),
                            constrain((Float) n1.weights.get(n2.id) * 205, 50, 255));
                } else if (n1.highlighted || n2.highlighted) {
                    stroke(constrain((m + cr * r) * 0.8f, 0, 255),
                            constrain((m + cg * r) * 0.8f, 0, 255),
                            constrain((m + cb * r) * 0.8f, 0, 255),
                            constrain((Float) n1.weights.get(n2.id) * 205, 50, 255));
                } else {

                    stroke(constrain(m + cr * r, 0, 255),
                            constrain(m + cg * r, 0, 255),
                            constrain(m + cb * r, 0, 255),
                            constrain((Float) n1.weights.get(n2.id) * 205, 50, 255));
                }
                //}



                float powd = PApplet.dist(n1.screenX, n1.screenY, n2.screenX, n2.screenY);

                float modulator = constrain(PApplet.map(powd, 8, width, 1, 90), 1, 90);

                bezierDetail((int) modulator);

                // compute the edge thickness
                if (v.highDefinition) {
                    // since mutal edges might have incomplete
                    // weight information, we have to try to
                    // get the weight in both nodes
                    float w = n1.weights.containsKey(n2.id)
                            ? (Float) n1.weights.get(n2.id) // node 2
                            : n2.weights.containsKey(n1.id)
                            ? (Float) n2.weights.get(n1.id) // node 1
                            : 1.0f; // default


                    strokeWeight(
                            constrain(w * v.sceneScale * 1.5f, 1.0f, 30.0f));

                } else {
                    strokeWeight(1);
                }


                drawCurve(n2, n1);
            } // FOR NODE B
        }   // FOr NODE A

        noStroke();
        for (Node n : nodes.nodes) {
            if (!n.visibleToScreen) {
                continue;
            }

            float rad = n.radius;// * MAX_NODE_RADIUS;
            float rad2 = rad * 1.5f;

            float nodeScreenDiameter = screenX(n.x + rad2, n.y) - screenX(n.x - rad2, n.y);
            if (nodeScreenDiameter < 1) {
                continue;
            }

            float alpha = 255;
            if (n.selected) {
                alpha = 220;
                hasSelected = n;
            } else if (n.highlighted) {
                alpha = 200;
            } else {

                // degrade du radius [r=14 level=255, r=40 level=80]
                float minRad = 12.0f;
                float maxRad = 25.0f;
                int maxRadColor = 200;
                int minRadColor = 1;

                float tRatio = 1.0f / (maxRad - minRad);
                float nsdRatio = constrain((nodeScreenDiameter - minRad) * tRatio, 0, 1);
                alpha = minRadColor + nsdRatio * (float) (maxRadColor - minRadColor);

            }
            // best match for the "screen" selection

            /****************************
             *  PROCESSING DRAWING CODE *
             ****************************/
            // if we don't want to show the nodes.. we skip
            if (v.showNodes) {


                if (n.selected | n.highlighted) {
                    rad *= 1.0f;
                    rad2 *= 1.0f;
                }

                /*
                if (n.selected) {
                fill(40, 40, 40, alpha);
                } else if (n.highlighted) {
                fill(110, 110, 110, alpha);
                } else {
                fill(180, 180, 180, alpha);
                }*/

                if (n.selected) {
                    fill(constrain(n.r * 0.4f, 0, 255), constrain(n.g * 0.4f, 0, 255), constrain(n.b * 0.4f, 0, 255), alpha);
                } else if (n.highlighted) {
                    fill(constrain(n.r * 0.4f + 40, 0, 255), constrain(n.g * 0.4f + 40, 0, 255), constrain(n.b * 0.4f + 40, 0, 255), alpha);
                } else {
                    fill(constrain(n.r * 0.4f + 80, 0, 255), constrain(n.g * 0.4f + 80, 0, 255), constrain(n.b * 0.4f + 80, 0, 255), alpha);
                }


                if (n.shape == ShapeCategory.DISK) {
                    ellipse(n.x, n.y, rad2, rad2);
                } else {
                    rect(n.x, n.y, rad2, rad2);
                }

                if (n.selected) {
                    fill(constrain(n.r, 0, 255), constrain(n.g, 0, 255), constrain(n.b, 0, 255), alpha);
                } else if (n.highlighted) {
                    fill(constrain(n.r + 40, 0, 255), constrain(n.g + 40, 0, 255), constrain(n.b + 40, 0, 255), alpha);
                } else {
                    fill(constrain(n.r + 80, 0, 255), constrain(n.g + 80, 0, 255), constrain(n.b + 80, 0, 255), alpha);
                }

                if (n.shape == ShapeCategory.DISK) {
                    ellipse(n.x, n.y, rad, rad);
                } else {
                    rect(n.x, n.y, rad, rad);
                }

            } // end of "if show nodes"

            shownNodes++;
            // skip label drawing for small nodes
            // or if we have to hide labels
            if (nodeScreenDiameter < 12 | !(v.showLabels | n.selected)) {
                continue;
            }

            if (n.selected) {
                fill(0, 0, 0, 255);
            } else if (n.highlighted) {
                fill(0, 0, 0, 220);
            } else {
                fill(0, 0, 0, alpha);
            }
            textSize(rad2);
            n.boxHeight = rad2 * 2.0f;
            n.boxWidth = (rad2 * 2.0f + rad2 * 0.3f) + textWidth((n.highlighted) ? n.label : n.shortLabel) * 1.0f;
            text((n.highlighted) ? n.label : n.shortLabel, n.x + rad, n.y + (rad2 / PI));

        } // END FOR EACH NODE


        //popMatrix();

        /*
        if (hasSelected != null) {
        float sx = screenX(hasSelected.x, hasSelected.y);
        float sy = screenY(hasSelected.x, hasSelected.y);

        float width = 300;
        float height = 400;
        fill(0, 0, 0, 100);

        pushMatrix();
        translate(sx - width / 2.0f, sy - height / 2.0f);
        ellipse(0, 0, 10f, 10f);
        ellipse(width, 0, 10f, 10f);
        ellipse(0, height, 10f, 10f);
        ellipse(width, height, 10f, 10f);
        rect(0, 0, width, height);
        fill(230, 230, 234, 230);
        ellipse(0, 0, 10f, 10f);
        ellipse(width, 0, 10f, 10f);
        ellipse(0, height, 10f, 10f);
        ellipse(width, height, 10f, 10f);
        rect(0, 0, width, height);
        fill(80, 80, 80, 230);
        textSize(18);
        text(hasSelected.label, 100, 15);

        if (hasSelected != oldSelected) {
        currentImg = null;
        oldSelected = hasSelected;
        }
        if (currentImg == null) {
        currentImg = requestImage(hasSelected.imageURL);
        } else {
        if (currentImg.width == 0) {
        // Image is not yet loaded
        } else if (currentImg.width == -1) {
        // This means an error occurred during image loading
        } else {
        // Image is ready to go, draw it
        image(currentImg, 0, 0);
        }
        }
        }*/
        /*
        popMatrix();

        fill(0,0,100,100);
        stroke(0,0,0,200);
        strokeWeight(1.0f);
        ellipse(15, 15, 5, 5);
        line(15, 15, 15, 5);
        line(15, 16, 15, 17);
         */
        selectNode = null;

        PVector p = new PVector(0, 0, 0);
        //image(brandingImage, -v.translation.x - 98, -v.translation.y - 20);
        //p.sub(v.translation);
        //p.div(v.sceneScale);
        //drawBranding(v,p);

    }

    public void drawBranding(View v, PVector p) {
        if (showBranding) {

            image(brandingImage,
                    p.x, p.y,
                    brandingImage.width / v.sceneScale, brandingImage.height / v.sceneScale);
        }
    }

    public void drawCurve(Node n1, Node n2) {
        float xa0 = (6 * n1.x + n2.x) / 7, ya0 = (6 * n1.y + n2.y) / 7;
        float xb0 = (n1.x + 6 * n2.x) / 7, yb0 = (n1.y + 6 * n2.y) / 7;
        float[] xya1 = MathFunctions.rotation(xa0, ya0, n1.x, n1.y, PApplet.PI / 2);
        float[] xyb1 = MathFunctions.rotation(xb0, yb0, n2.x, n2.y, -PApplet.PI / 2);
        beginShape();
        bezier(n1.x, n1.y, xya1[0], xya1[1], xyb1[0], xyb1[1], n2.x, n2.y);
        endShape();
    }

    public synchronized boolean takePicture(String path) {
        recordPath = path;
        recordingWidth = width;
        recordingHeight = height;
        recordingMode = RecordingFormat.CURRENT_PICTURE;
        return true;
    }

    /* create an HD picture (or a tiled picture?) */
    public synchronized boolean takePicture(String path, int width, int height) {
        recordPath = path;
        recordingWidth = width;
        recordingHeight = height;
        recordingMode = RecordingFormat.BIG_PICTURE;
        return true;
    }

    public synchronized boolean takePDFPicture(String path) {
        recordPath = path;
        recordingWidth = width;
        recordingHeight = height;
        recordingMode = RecordingFormat.PDF;
        return true;
    }

    public Session getSession() {
        //Console.log("<applet> returning session " + session);
        return session;
    }

    public View getView() {
        //Console.log("<applet> returning network " + session.getNetwork());
        return session.getView();
    }

    public View getView(String v) {
        return session.getView(v);
    }

    @Override
    public void mousePressed() {
        lastMousePosition.set(mouseX, mouseY, 0);
        redrawIfNeeded();
    }

    @Override
    public void mouseMoved() {

        cursor(ARROW);
        Node candidate = null;
        for (Node n : nodes.nodes) {
            n.highlighted = false;
            float nsx = screenX(n.x, n.y);
            float nsy = screenY(n.x, n.y);
            //System.out.println("got candidate at x:"+nsx+",y:"+nsy+"");
            float rad = n.radius * MAX_NODE_RADIUS;

            //System.out.println("rad: "+(n.radius * MAX_NODE_RADIUS)+"= "+n.radius+" * "+MAX_NODE_RADIUS+" = n.radius * MAX_NODE_RADIUS");
            float rad2 = rad + rad * 0.4f;
            float nsr = screenX(n.x + rad2, n.y) - nsx;

            //System.out.println("node ratio "+nsr);
            if (nsr < 0.02) {
                continue;
            }

            if (dist(mouseX, mouseY, nsx, nsy) < nsr) {

                if (candidate == null) {
                    candidate = n;
                }
                if (n.radius > candidate.radius) {
                    candidate = n;
                }
            }

        }
        if (candidate != null) {
            candidate.highlighted = true;
            cursor(HAND);
            redrawIfNeeded();
        }

    }

    @Override
    public void mouseClicked() {

        System.out.println("mouse clicked");
        for (Node n : nodes.nodes) {
            float nsx = screenX(n.x, n.y);
            float nsy = screenY(n.x, n.y);
            float rad = n.radius * MAX_NODE_RADIUS;
            float rad2 = rad + rad * 0.4f;
            float nsr = screenX(n.x + rad2, n.y) - nsx;
            if (nsr < 2) {
                continue;
            }

            if ((dist(mouseX, mouseY, nsx, nsy) < nsr)) {

                // LEFT CLICK ON NODES
                if (mouseButton == LEFT) {
                    //System.out.println("left mouse clicked!");
                    if (mouseEvent != null && mouseEvent.getClickCount() == 2) {


                        // cannot unselect the selected node in meso view
                        if (n.selected && session.getView().getLevel() == ViewLevel.MESO) {
                            redrawIfNeeded();
                            return;
                        }

                        // double click also select nodes!
                        n.selected = true;
                        session.selectNode(n);
                        nodeSelectedLeftMouse_JS_CALLBACK(n);

                        if (session.currentLevel == ViewLevel.MACRO) {
                            System.out.println("SWITCH TO MESO WITH THE DOUBLE CLICK METHOD");

                            session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL * 2f;
                            jsSwitchToMeso();
                        } else if (session.currentLevel == ViewLevel.MESO) {
                            System.out.println("SWITCH TO MICRO WITH THE DOUBLE CLICK METHOD");
                            //session.getMicro().sceneScale = session.getMicro().ZOOM_CEIL + session.getMicro().ZOOM_CEIL * 0.5f;
                            //jsSwitchToMicro();
                        }
                    } else {
                        if (!n.selected) {
                            n.selected = true;
                            session.selectNode(n);
                            nodeSelectedLeftMouse_JS_CALLBACK(n);

                        } else {
                            n.selected = false;
                            session.unselectNode(n);
                            nodeSelectedLeftMouse_JS_CALLBACK(null);
                        }
                    }

                    // RIGHT MOUSE
                } else if (mouseButton == RIGHT) {
                    if (!n.selected) {
                        n.selected = true;
                        session.selectNode(n);
                    }
                    nodeSelectedRightMouse_JS_CALLBACK(n);
                }
                break;
            }

        }
        lastMousePosition.set(mouseX, mouseY, 0);
        redrawIfNeeded();
    }

    void redrawIfNeeded() {
        redrawScene.set(true);
    }

    @Override
    public void mouseDragged() {
        recenter = false;
        if (mouseButton == RIGHT) {
            View v = session.getView();
            PVector oldTranslation = new PVector(v.translation.x, v.translation.y, 0.0f);

            v.translation.sub(lastMousePosition);
            lastMousePosition.set(mouseX, mouseY, 0);
            v.translation.add(lastMousePosition);

            cameraDelta.set(oldTranslation.x - v.translation.x, oldTranslation.y - v.translation.y, 0.0f);
        }
        redrawIfNeeded();
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == RIGHT) {
        } else if (mouseButton == LEFT) {
        }
        redrawIfNeeded();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {


        if (e.getUnitsToScroll() == 0) {
            return;
        }
        showBranding = false;
        View v = getView();

        lastMousePosition.set(mouseX, mouseY, 0);
        v.translation.sub(lastMousePosition);

        // TODO check for limits before
        if (e.getWheelRotation() < 0) {
            v.sceneScale *= 4.f / 3.f;
            v.translation.mult(4.f / 3.f);
        } else {
            v.sceneScale *= 3.f / 4.f;
            v.translation.mult(3.f / 4.f);
        }
        v.translation.add(lastMousePosition);
        // System.out.println("Zoom: " + v.sceneScale);



        if (e.getWheelRotation() < 0) {



            Node bestMatchForSwitch = null;
            Node bestMatchForSelection = null;


            for (Node n : nodes.nodes) {

                float rad = n.radius * MAX_NODE_RADIUS;
                float rad2 = rad + rad * 0.4f;
                float nsx = screenX(n.x, n.y);
                float nsy = screenY(n.x, n.y);

                if (!(nsx > width * 0.2f
                        && nsx < width * 0.8f
                        && nsy > height * 0.2f
                        && nsy < height * 0.8f)) {
                    continue;
                }

                float nsr = screenX(n.x + (rad2), n.y) - nsx;
                if (nsr < 2) {
                    continue;
                }

                float screenRatio = (((nsr * 2.0f) / (float) width) + ((nsr * 2.0f) / (float) height)) / 2.0f;
                //System.out.println("nsr: " + nsr);
                //System.out.println("'- screen ratio: " + screenRatio);
                if (v.showNodes) {

                    switch (session.currentLevel) {
                        case MACRO:

                            if (screenRatio > screenRatioGoToMesoWhenZoomed) {
                                if (bestMatchForSwitch != null) {

                                    if (rad2 > bestMatchForSwitch.radius) {
                                        bestMatchForSelection = null;
                                        bestMatchForSwitch = n;
                                    }
                                } else {
                                    bestMatchForSelection = null;
                                    bestMatchForSwitch = n;
                                }
                            } else if (screenRatio > screenRatioSelectNodeWhenZoomed) {
                                if (bestMatchForSelection != null) {

                                    if (rad2 > bestMatchForSelection.radius) {
                                        bestMatchForSelection = n;
                                    }
                                } else {
                                    bestMatchForSelection = n;
                                }
                            }
                            break;
                    }
                }

                if (bestMatchForSwitch != null) {

                    // disable the macro to meso switch when the nodes can't be shown

                    if (!bestMatchForSwitch.selected) {
                        bestMatchForSwitch.selected = true;
                        session.selectNode(bestMatchForSwitch);
                        nodeSelectedLeftMouse_JS_CALLBACK(bestMatchForSwitch);
                    }
                    System.out.println("SWITCH TO MESO WITH THE BIG ZOOM METHOD");
                    session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL + session.getMeso().ZOOM_CEIL * 0.5f;
                    jsSwitchToMeso();

                    redrawIfNeeded();
                    return;

                } else if (bestMatchForSelection != null) {
                    if (!bestMatchForSelection.selected) {
                        bestMatchForSelection.selected = true;
                        session.selectNode(bestMatchForSelection);
                        nodeSelectedLeftMouse_JS_CALLBACK(bestMatchForSelection);
                    }

                    redrawIfNeeded();
                    return;
                }
            }
        }

        switch (v.getLevel()) {
            case MACRO:
                if (v.sceneScale < v.ZOOM_CEIL) {
                    v.sceneScale = v.ZOOM_CEIL;
                }
                break;
            case MESO:
                System.out.println("scele scale: " + v.sceneScale + " zoom floor:" + v.ZOOM_FLOOR + " zoom ceil:" + v.ZOOM_CEIL);
                if (v.sceneScale > v.ZOOM_FLOOR) {
                    v.sceneScale *= 3.f / 4.f;
                    v.translation.mult(3.f / 4.f);
                    //v.sceneScale = v.ZOOM_FLOOR;
                    //System.out.println("switch in to micro");
                }
                if (v.sceneScale < v.ZOOM_CEIL) {
                    System.out.println("switch out to macro");
                    session.getMacro().sceneScale = session.getMacro().ZOOM_FLOOR - session.getMacro().ZOOM_FLOOR * 0.5f;
                    // TODO center the graph to the current selection
                    session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL * 2.0f;
                    jsSwitchToMacro();
                }
                break;
        }

        redrawIfNeeded();
    }

    @Override
    public void keyPressed() {
        View v = session.getView();
        if (key == CODED) {
            if (keyCode == UP) {
                v.translation.add(0.0f, 10f, 0.0f);
            } else if (keyCode == DOWN) {
                v.translation.add(0.0f, -10f, 0.0f);
            } else if (keyCode == LEFT) {
                v.translation.add(10f, 0.0f, 0.0f);
            } else if (keyCode == RIGHT) {
                v.translation.add(-10f, 0.0f, 0.0f);
            }
        } else if (key == 'p') {
            zooming.set(true);
            zoomIn.set(true);
            lastMousePosition.set(width / 2.0f, height / 2.0f, 0);
        } else if (key == 'm') {
            zooming.set(true);
            zoomIn.set(false);
            lastMousePosition.set(width / 2.0f, height / 2.0f, 0);
        } else if (key == 'e') {
            if (window != null) {
                window.eval("" + js_context + "tinaviz.toggleEdges('" + v.getName() + "');");
            } else {
                v.showLinks = !v.showLinks;
            }
            System.out.println("show links is now " + v.showLinks);
        } else if (key == 't') {
            if (window != null) {
                window.eval("" + js_context + "tinaviz.toggleLabels('" + v.getName() + "');");
            } else {
                v.showLabels = !v.showLabels;
            }
        } else if (key == 'n') {
            if (window != null) {
                window.eval("" + js_context + "tinaviz.toggleNodes('" + v.getName() + "');");
            } else {
                v.showNodes = !v.showNodes;
            }
            System.out.println("show nodes is now " + v.showNodes);
        } else if (key == 'r') {
            if (window != null) {
                window.eval("" + js_context + "tinaviz.recenter();");
            } else {
                recenter = true;
            }
            System.out.println("show nodes is now " + v.showNodes);
        } else if (key == 'a') {
            if (window != null) {
                window.eval("" + js_context + "tinaviz.togglePause('" + v.getName() + "');");
            } else {
                v.paused = !v.paused;
            }
            System.out.println("Animation paused is now " + v.paused);

        } else if (key == 'h') {
            v.highDefinition = !v.highDefinition;
            System.out.println("HD mode is now " + v.highDefinition);
        } else if (key == 'o') {
            if ((v.attraction + 0.00001) < 0.0004) {
                v.attraction += 0.00001f;
                System.out.println("\nattraction: " + session.getView().attraction);
            }
        } else if (key == 'l') {
            if ((v.attraction - 0.00001f) > 1.5e-5) {
                v.attraction -= 0.00001f;
                System.out.println("\nattraction: " + session.getView().attraction);
            }
        } else if (key == 'g') {
            if ((v.gravity + 0.001f) < 0.5f) {
                v.gravity += 0.001f;
                System.out.println("\ngravity: " + session.getView().gravity);
            }
        } else if (key == 'b') {
            if ((v.gravity - 0.001f) > 0.0f) {
                v.gravity -= 0.001f;
                System.out.println("\ngravity: " + session.getView().gravity);
            }
        } else if (key == 'd') {
            debug.set(!debug.get());

        }
        redrawIfNeeded();
    }

    public void storeResolution() {
        oldScreenWidth = 0;
        oldScreenHeight = 0;
    }

    public void clear() {
        getSession().clear();
        redrawIfNeeded();
    }

    public void clear(String view) {
        getSession().getView(view).clear();
        redrawIfNeeded();
    }

    public void recenter() {
        recenter = true;
        redrawIfNeeded();
    }

    public void touch(String level) {
        getView(level).getGraph().touch();
    }

    public boolean dispatchProperty(String key, Object value) throws KeyException {
        return getSession().setProperty(key, value);
    }

    public boolean setProperty(String level, String key, Object value) throws KeyException {
        return getView(level).setProperty(key, value);
    }

    public Object getProperty(String level, String key) throws KeyException {
        return getView(level).getProperty(key);
    }

    public boolean setAntiAliasing(boolean a) {
        alwaysAntiAliasing = a;
        redrawIfNeeded();
        return a;
    }

    public void setBezier(int nb) {
        this.bezierSize = nb;
        bezierDetail(bezierSize);
        redrawIfNeeded();
    }

    public void resetCamera(String view) {
        /*
        View v = getSession().getView(view);
        v.resetCamera();
         *
         */
        System.out.println("resetCamera(" + view + ") called, but NOT IMPLEMENTED");
        redrawIfNeeded();
    }

    // db id=   "Document::6657-45645"
    public void selectFromId(String str) {
        getSession().selectNode(str);
    }

    public void unselect() {
        getSession().unselectAll();
    }

    /*public String getNeighbourhood(String id) {
    String result = "{";
    Node node = nodes.getNode(id);
    if (node == null) {
    return "{}";
    }
    for (Long nodeId : node.neighbours) {
    Node n = getView().getNode(nodeId);
    result += "\""+n.uuid+"\":{}"
    }
    return result + "}";

    }*/
    public String getNodesByLabel(String label, String mode) {
        List<Node> results = nodes.getNodesByLabel(label, mode);

        if (results.size() == 0) {
            return "{}";
        }

        String result = "";
        JSONWriter writer = null;
        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "{}";
        }

        try {
            for (Node n : results) {
                writer.key(n.uuid).object();
                for (Entry<String, Object> entry : n.getAttributes().entrySet()) {
                    writer.key(entry.getKey()).value(entry.getValue());
                }
                writer.endObject();
            }
        } catch (JSONException jSONException) {
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        //System.out.println("data: " + writer.toString());
        return writer.toString();
    }

    public String getNodeAttributes(String id) {
        Node node = nodes.getNode(id);


        if (node == null) {
            return "{}";


        }
        return node.getAttributesAsJSON();


    }

    public String getNeighbourhood(String id) {
        String result = "";

        Node node = nodes.getNode(id);

        if (node == null) {
            return "{}";
        }
        JSONWriter writer = null;


        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "{}";
        }

        try {
            for (int nodeId : node.weights.keys().elements()) {
                Node n = getView().getNode(nodeId);
                writer.key(n.uuid).object();
                for (Entry<String, Object> entry : n.getAttributes().entrySet()) {
                    writer.key(entry.getKey()).value(entry.getValue());
                }
                writer.endObject();
            }

        } catch (JSONException jSONException) {
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        //System.out.println("data: " + writer.toString());
        return writer.toString();

    }
}
