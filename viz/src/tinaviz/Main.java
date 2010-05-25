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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
//import peasy.*;

public class Main extends PApplet implements MouseWheelListener {

    String PATH_TO_TEST_FILE =
            "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/FET60bipartite_graph_cooccurrences_.gexf" //"file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/CSSScholarsMay2010.gexf";
            //"file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/test.gexf"
            //  "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/CSSScholarsMay2010.gexf"
            ;

    ;
    String VENDOR_URL = "http://sciencemapping.com/tinaweb";
    float SELECTION_DISK_RADIUS = 150f;
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
    public final float ARCTAN_12 = (float) (2.0 * Math.atan(1.0 / 2.0));
    public final float PI_ON_TWO = PApplet.PI / 2;
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
    float MAX_NODE_RADIUS = 1.0f; // node radius is normalized to 1.0 for each node, then mult with this value
    float MAX_EDGE_WEIGHT = 1.0f; // node radius is normalized to 1.0 for each node, then mult with this value
    float MAX_EDGE_THICKNESS = 20.0f;
    private Long selectNode = null;
    int oldScreenWidth = 0;
    int oldScreenHeight = 0;
    private Node oldSelected = null;
    private boolean recenter = true;
    private float oldZoomScale = -1f;
    private float realWidth = 0.0f;
    private PVector cameraDelta = new PVector(0.0f, 0.0f, 0.0f);
    private int bezierSize = 18;
    private int shownEdges = 0;
    private int shownNodes = 0;
    AtomicBoolean redrawScene = new AtomicBoolean(true);
    public String js_context = "";
    private int currenthighlighted = 0;
    private boolean centerOnSelection = false;
    private boolean loading = true;

    private String getSelectedNodesAsJSON() {

        String result = "";
        JSONWriter writer = null;
        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }

        try {
            for (Node node : nodes.nodes) {

                if (node.selected) {
                    writer.key(node.uuid).object();
                    writer.key("id").value(node.uuid);
                    for (Entry<String, Object> entry : node.getAttributes().entrySet()) {
                        writer.key(entry.getKey()).value(valueEncoder(entry.getValue()));
                    }
                    writer.endObject();
                }
            }

        } catch (JSONException jSONException) {
            Console.error(jSONException.getMessage());
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        System.out.println("data: " + writer.toString());
        return writer.toString();

    }

    private void nodeSelected_JS_CALLBACK(boolean left) {

        /*String cmd = "setTimeout(\"" + js_context + "tinaviz.selected('" + session.getLevel() + "','"
        + getSelectedNodesAsJSON() + "','" + (left ? "left" : "right") + "');\",1);";
        System.out.println("cmd: " + cmd);*/
        if (window == null) {
            return; // in debug mode
        }

        String fnc = js_context + "tinaviz.selected('" + session.getLevel() + "','"
                + getSelectedNodesAsJSON() + "','" + (left ? "left" : "right") + "')";

        Object[] message = {fnc, 0};
        window.call("setTimeout", message);
        // window.eval(cmd);
    }

    private void viewChanged_JS_CALLBACK(String view) {

        /*String cmd = "setTimeout(\"" + js_context + "tinaviz.selected('" + session.getLevel() + "','"
        + getSelectedNodesAsJSON() + "','" + (left ? "left" : "right") + "');\",1);";
        System.out.println("cmd: " + cmd);*/
        if (window == null) {
            return; // in debug mode
        }
        String fnc = js_context + "tinaviz.switchedTo('" + view + "')";
        Console.debug("viewChanged_JS_CALLBACK(" + view + ")");
        Object[] message = {fnc, 0};
        window.call("setTimeout", message);
        // window.eval(cmd);
    }

    public Object valueEncoder(Object o) {
        try {
            return (o instanceof String) ? URLEncoder.encode((String) o, "UTF-8") : o;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private void jsSwitchToMacro() {
        session.toMacroView();
        viewChanged_JS_CALLBACK("macro");
    }

    private void jsSwitchToMeso() {
        session.toMesoView();
        viewChanged_JS_CALLBACK("meso");
    }

    private void jsSwitchToMicro() {
        session.toMicroView();
        viewChanged_JS_CALLBACK("micro");
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


        // font = loadFont(DEFAULT_FONT);
        font = createFont("Arial", 150, true);
        //String[] fontList = PFont.list();
        //println(fontList);

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
            size(w, h, engine);

        } else {
            session.setBrowser(new LiveConnector());
            loadDefaultGlobalGraph = true;
            size(screenWidth - 400, screenHeight - 100, engine);
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

        layout = new Layout();

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

            Node root = new Node(0, "root node", radius, 0.0f, 0.0f);
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
        if (generateRandomGlobalGraph) {

            NodeList tmp = new NodeList();
            Node node;
            Console.log("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 0.0f;
            for (int i = 0; i < 2000; i++) {
                radius = random(3.0f, 10.0f);

                node = new Node(i, "node " + i, radius, random(width / 2), random(height / 2));
                node.weight = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
                node.label = node.category + " " + node.label;
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                for (int j = 0; j < tmp.size() && i != j; j++) {
                    if (random(1.0f) < 0.001) { // link density : 0.02 = a lot, 0.0002 = a few
                        tmp.get(i).addNeighbour(tmp.get(j), random(1.0f));
                    }
                }
            }
            session.getMacro().getGraph().updateFromNodeList(tmp);


            //session.animationPaused = true;
        } else if (loadDefaultGlobalGraph) {
            Console.log("loading default graph..");
            session.getMacro().getGraph().updateFromURI(
                    PATH_TO_TEST_FILE);


            try {
                session.getMacro().setProperty("category/value", "NGram");
            } catch (KeyException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            session.getMacro().addFilter("Category", "category");
            session.getMacro().addFilter("NodeWeightRange", "nodeWeight");
            session.getMacro().addFilter("EdgeWeightRange", "edgeWeight");
            session.getMacro().addFilter("NodeFunction", "radiusByWeight");
            session.getMacro().addFilter("Output", "output");
            //session.getView().paused = true;

        }
        //cenNGramesoView();
        //session.toMesoLevel();

        // in the case the reset method take the graph radius in account to zoom (but its still not the case)
        session.toMacroView();
        // session.toMesoLevel();

        // DEBUG MODE
        session.macro.prespatializeSteps = 0;

        lastMousePosition = new PVector(width / 2.0f, height / 2.0f, 0);
        // fill(255, 184);

        //brandingImage = getImage(new URL("tina_icon.gif"));
        brandingImage = loadImage("tina_icon.png");

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
                if (!v.paused) {
                    layout.macroViewLayout_TinaForce(v, nodes);
                }
            }
            return;
        }

        NodeList n = v.popNodes();
        if (n != null) {
            // loading = false;
            //System.out.println("a");
            redrawScene.set(true);

        }

        /*if (loading) {
        System.out.println("draw loading 1");
        drawLoading(v);
        return;
        }*/



        if (redrawScene.get()) {
            draw2(v, n);
        } else {
            // hack (we need to setup the matrix so processing can store object's positions)
            drawNothing(v);
        }


        redrawScene.set(!v.paused);

    }

    public void draw2(View v, NodeList n) {

        boolean autocenter = false;
        if (n != null) {
            //System.out.println("pop nodes gave something! overwriting node screen cache..");
            nodes = n;

            autocenter = v.graph.topologyChanged.getAndSet(false);
            if (autocenter) {
                oldZoomScale = -1f;
            }

        }

        if (recenter | autocenter) {

            float screenRadius = (width + height) / 2.0f;

            v.sceneScale = nodes.graphRadius > 0 ? (screenRadius * 0.3f / nodes.graphRadius) : 1.0f;
            PVector baryCenter = new PVector(nodes.baryCenter.x, nodes.baryCenter.y);
            if (centerOnSelection) {
                baryCenter = nodes.getSelectedNodesBarycenter();
                centerOnSelection = false;
            }

            PVector translate = new PVector();
            translate.set(baryCenter);

            translate.set(PVector.div(translate, v.sceneScale));

            PVector screenCenter = new PVector(width / 2.0f, height / 2.0f, 0);
            translate.add(screenCenter);

            v.translation.set(translate);

            if (abs(oldZoomScale - v.sceneScale) <= 0.5) {
                recenter = false;
                System.out.println("Automatic centering is now disabled.");
                oldZoomScale = v.sceneScale;
            } else {
                oldZoomScale = v.sceneScale;
            }
        }

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
            if (!v.paused) {
                layout.macroViewLayout_TinaForce(v, nodes);
            }

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
            layout.macroViewLayout_TinaForce(v, nodes);
        }

        // TODO optimize here
        nodes.sortBySelectionStatus();


        background(255);



        //image(brandingImage, -v.translation.x - 98, -v.translation.y - 20);
        //p.sub(v.translation);
        //p.div(v.sceneScale);
        //drawBranding(v,p);



        //background(53,59,61);

        stroke(150, 150, 150);
        strokeWeight(1);

        image(brandingImage, width - 30, height - 25);
        if (mouseX > width - 30 && mouseY > height - 25) {
            cursor(HAND);
        }
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
            n.screenX = screenX(n.position.x, n.position.y);
            n.screenY = screenY(n.position.x, n.position.y);
            n.visibleToScreen = (n.screenX > -(width / 4.0f)
                    && n.screenX < width + (width / 4.0f)
                    && n.screenY > -(height / 4.0f)
                    && n.screenY < height + (height / 4.0f));
        }

        for (Node n1 : nodes.nodes) {

            /*
            if (selectNode != null) {
            if (selectNode == n1.id) {
            n1.selected = true;
            }
            }
             */


            for (Node n2 : nodes.nodes) {

                if (n1 == n2) {
                    continue;
                }



                if (!v.showLinks) {
                    if (!n1.selected && !n1.isFirstHighlight) {
                        continue;
                    }
                }
                if (!n1.visibleToScreen && !n2.visibleToScreen) {
                    continue;
                }
                // skip the node if the following cases



                boolean n2_in_n1 = n1.weights.containsKey(n2.id);

                if (!n2_in_n1) {
                    continue;
                }
                boolean n1_in_n2 = n2.weights.containsKey(n1.id);

                float w = (Float) n1.weights.get(n2.id);


                if (n1.weight > n2.weight) {

                    continue;
                } else {
                    if (n1.weight == n2.weight) {
                        if (n1.id > n2.id) {
                            continue;
                        }
                    }
                }

                shownEdges++;



                // here, minRadius should contain the non-normalized min radius
                // (eg. 1.0 or 4.4)
                float minrad = nodes.minRadius;

                // compute the edge thickness

                // since mutal edges might have incomplete
                // weight information, we have to try to
                // get the weight in both nodes


                // default

                // if we want to draw the links, or if we clicked on a node
                // or if we put the mouse over a node

                //n2.isSecondHighlight = (n1.isFirstHighlight);

                // compute the average node color
                float cr = (n1.r + n2.r) / 2;
                float cg = (n1.g + n2.g) / 2;
                float cb = (n1.b + n2.b) / 2;

                float m = 160.0f;
                float r = (255.0f - m) / 255.0f;

                if (n1.selected || n2.selected) {
                    stroke(constrain((m + cr * r) * 0.35f, 0, 200),
                            constrain((m + cg * r) * 0.35f, 0, 200),
                            constrain((m + cb * r) * 0.35f, 0, 200),
                            120);
                } else if (n1.isFirstHighlight || n2.isFirstHighlight) {
                    stroke(constrain((m + cr * r) * 0.7f, 0, 230),
                            constrain((m + cg * r) * 0.7f, 0, 230),
                            constrain((m + cb * r) * 0.7f, 0, 230),
                            120);
                } else {

                    stroke(constrain((m + cr * r) * 1.0f, 0, 255),
                            constrain((m + cg * r) * 1.0f, 0, 255),
                            constrain((m + cb * r) * 1.0f, 0, 255),
                            120);
                }


                float powd = PApplet.dist(n1.screenX, n1.screenY, n2.screenX, n2.screenY);
                float modulator = constrain(PApplet.map(powd, 8, width, 1, 90), 1, 90);

                bezierDetail((int) modulator);

                float screenWeight = PApplet.map(w, 0.0f, 1.0f, minrad * 0.1f, minrad * 0.4f);
                strokeWeight(screenWeight * v.sceneScale);

                drawCurve(n1.position.x, n1.position.y, n2.position.x, n2.position.y);
            } // FOR NODE B
        }   // FOr NODE A

        noStroke();
        for (Node n : nodes.nodes) {
            if (!n.visibleToScreen) {
                continue;
            }


            boolean highlighted = (n.isFirstHighlight | n.isSecondHighlight);

            float nx = n.position.x;
            float ny = n.position.y;

            float rad = n.radius;// * MAX_NODE_RADIUS;

            float rad2 = rad * 1.3f;
            //rad = rad * 0.9f;

            float nodeScreenDiameter = screenX(nx + rad2, ny) - screenX(nx - rad2, ny);
            if (nodeScreenDiameter < 1) {
                continue;
            }

            float alpha = 240;
            if (n.selected) {
                alpha = 200;
                //hasSelected = n;
            } else if (highlighted) {
                alpha = 150;
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
                if (n.selected | highlighted) {
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

                /*
                fill(0,0,0, 20);


                if (n.shape == ShapeCategory.DISK) {
                ellipse(nx, ny, rad2+0.1f, rad2+0.1f);
                } else {
                rect(nx, ny, rad2+0.1f, rad2+0.1f);
                }
                 */
                if (n.selected) {
                    fill(constrain(n.r * 0.4f, 0, 255), constrain(n.g * 0.4f, 0, 255), constrain(n.b * 0.4f, 0, 255), alpha * 0.9f);
                } else if (n.isFirstHighlight) {
                    fill(constrain(n.r * 0.4f, 0, 255), constrain(n.g * 0.4f, 0, 255), constrain(n.b * 0.4f, 0, 255), alpha * 0.7f);
                } else {
                    fill(constrain(n.r * 0.5f, 0, 255), constrain(n.g * 0.5f, 0, 255), constrain(n.b * 0.5f, 0, 255), alpha * 0.3f);
                }


                if (n.shape == ShapeCategory.DISK) {
                    ellipse(nx, ny, rad2, rad2);
                } else {
                    rect(nx, ny, rad2, rad2);
                }

                if (n.selected) {
                    fill(constrain(n.r, 0, 255), constrain(n.g, 0, 255), constrain(n.b, 0, 255), 255);
                } else if (n.isFirstHighlight) {
                    fill(constrain(n.r + 40, 0, 255), constrain(n.g + 40, 0, 255), constrain(n.b + 40, 0, 255), 200);
                } else {
                    fill(constrain(n.r + 80, 0, 255), constrain(n.g + 80, 0, 255), constrain(n.b + 80, 0, 255), alpha * 0.9f);
                }

                if (n.shape == ShapeCategory.DISK) {
                    ellipse(nx, ny, rad, rad);
                } else {
                    rect(nx, ny, rad, rad);
                }
            } // end of "if show nodes"
            shownNodes++;
            // skip label drawing for small nodes
            // or if we have to hide labels
            if (nodeScreenDiameter < 11 | !(v.showLabels | n.selected | highlighted)) {
                continue;
            }
            if (n.selected) {
                fill(0, 0, 0, 255);
            } else if (n.isFirstHighlight) {
                fill(0, 0, 0, 200);
            } else if (n.isSecondHighlight) {
                fill(60, 60, 60, 180);
            } else {
                fill(0, 0, 0, alpha * 0.7f); // we want text a bit more transparent
            }
            textSize(rad);
            n.boxHeight = rad2 * 2.0f;
            n.boxWidth = (rad2 * 2.0f + rad2 * 0.3f) + textWidth((highlighted) ? n.label : n.shortLabel) * 1.0f;
            // float sw = textWidth(s)
            text((highlighted) ? n.label : n.shortLabel, nx + rad, ny + (rad / PI));

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


        //////////////////////
        // SELECTION DISK => DISABLED
        if (SELECTION_DISK_RADIUS > 1) {

            scale(1.0f / v.sceneScale);
            translate(-v.translation.x, -v.translation.y);

            PVector p = new PVector(0, 0, 0);
            //fill(0, 0, 100, 100);
            stroke(0, 0, 0, 40);
            strokeWeight(1.0f);
            fill(00, 100, 200, 29);
            p.add(mouseX, mouseY, 0);
            //p.sub(v.translation);
            //p.mult(v.sceneScale);
            ellipse(p.x, p.y, SELECTION_DISK_RADIUS, SELECTION_DISK_RADIUS);
            translate(v.translation.x, v.translation.y);
            scale(v.sceneScale);
        }


    }

    public void drawBranding(View v, PVector p) {
        if (!showBranding) {
            return;
        }
        image(brandingImage,
                p.x,
                p.y,
                brandingImage.width / v.sceneScale,
                brandingImage.height / v.sceneScale);

    }

    public void drawCurve(float n1x, float n1y, float n2x, float n2y) {
        float xa0 = (6 * n1x + n2x) / 7, ya0 = (6 * n1y + n2y) / 7;
        float xb0 = (n1x + 6 * n2x) / 7, yb0 = (n1y + 6 * n2y) / 7;
        float[] xya1 = MathFunctions.rotation(xa0, ya0, n1x, n1y, PI_ON_TWO);
        float[] xyb1 = MathFunctions.rotation(xb0, yb0, n2x, n2y, -PI_ON_TWO);
        beginShape();
        bezier(n1x, n1y, xya1[0], xya1[1], xyb1[0], xyb1[1], n2x, n2y);
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

    public View getView(String v) throws ViewNotFoundException {
        return session.getView(v);
    }

    @Override
    public void mousePressed() {
        lastMousePosition.set(mouseX, mouseY, 0);
        redrawIfNeeded();
    }

    @Override
    public void mouseMoved() {
        currenthighlighted = 0;
        if (mouseX > width - 30 && mouseY > height - 25) {
            cursor(HAND);
        } else {
            cursor(ARROW);
        }
        Object o = this.getProperty("current", "selection/radius");
        if (o != null) {
            if (o instanceof Float) {
                SELECTION_DISK_RADIUS = (Float) o;
            } else if (o instanceof Integer) {
                SELECTION_DISK_RADIUS = ((Integer) o).floatValue();
            } else if (o instanceof Double) {
                SELECTION_DISK_RADIUS = ((Double) o).floatValue();
            }
        }

        Node candidate = null;
        for (Node n : nodes.nodes) {
            n.isFirstHighlight = false;
            float nsx = screenX(n.position.x, n.position.y);
            float nsy = screenY(n.position.x, n.position.y);

            boolean match = false;
            if (SELECTION_DISK_RADIUS > 1) {
                if ((dist(mouseX, mouseY, nsx, nsy) < SELECTION_DISK_RADIUS / 2.0f)) {
                    match = true;
                }
            } else {
                float rad = n.radius * MAX_NODE_RADIUS;
                float rad2 = rad + rad * 0.4f;
                float nsr = screenX(n.position.x + rad2, n.position.y) - nsx;
                if (nsr < 2) {
                    continue;
                }



                if ((dist(mouseX, mouseY, nsx, nsy) < nsr)) {
                    match = true;
                }
            }

            if (match) {
                candidate = n;
                n.isFirstHighlight = true;

            }
            /*
            if (SELECTION_DISK_RADIUS > 1) {

                if (dist(mouseX, mouseY, nsx, nsy) < SELECTION_DISK_RADIUS) {

                    if (candidate == null) {
                        candidate = n;
                    } else if (n.radius > candidate.radius) {
                        candidate = n;
                    }
                    candidate.isFirstHighlight = true;
                }
            } else {
                //System.out.println("got candidate at x:"+nsx+",y:"+nsy+"");
                float rad = n.radius * MAX_NODE_RADIUS;

                //System.out.println("rad: "+(n.radius * MAX_NODE_RADIUS)+"= "+n.radius+" * "+MAX_NODE_RADIUS+" = n.radius * MAX_NODE_RADIUS");
                float rad2 = rad + rad * 0.4f;
                float nsr = screenX(n.position.x + rad2, n.position.y) - nsx;

                //System.out.println("node ratio "+nsr);
                if (nsr < 0.02) {
                    continue;
                }

                if (dist(mouseX, mouseY, nsx, nsy) < nsr) {

                    if (candidate == null) {
                        candidate = n;
                    } else if (n.radius > candidate.radius) {
                        candidate = n;
                    }
                    candidate.isFirstHighlight = true;
                }
            }*/

        }
        if (SELECTION_DISK_RADIUS > 1) {
            redrawIfNeeded();
        } else {
            if (candidate != null) {
                cursor(HAND);
                redrawIfNeeded();
            }
        }

    }

    @Override
    public void mouseClicked() {


        List<String> selectedIDs = new ArrayList<String>();

        System.out.println("mouse clicked");

        if (mouseX > width - 30 && mouseY > height - 25) {
            link(VENDOR_URL, "_new");
        }

        for (Node n : nodes.nodes) {
            float nsx = screenX(n.position.x, n.position.y);
            float nsy = screenY(n.position.x, n.position.y);
            boolean match = false;

            if (SELECTION_DISK_RADIUS > 1) {
                if ((dist(mouseX, mouseY, nsx, nsy) < SELECTION_DISK_RADIUS / 2.0f)) {
                    match = true;
                }
            } else {
                float rad = n.radius * MAX_NODE_RADIUS;
                float rad2 = rad + rad * 0.4f;
                float nsr = screenX(n.position.x + rad2, n.position.y) - nsx;
                if (nsr < 2) {
                    continue;
                }



                if ((dist(mouseX, mouseY, nsx, nsy) < nsr)) {
                    match = true;
                }
            }


            if (match) {
                // LEFT CLICK ON NODES
                if (mouseButton == LEFT) {
                    //System.out.println("left mouse clicked!");


                    if (mouseEvent != null && mouseEvent.getClickCount() == 2) {

                        // cannot unselect the selected node in meso view
                        /*
                        if (n.selected && session.getView().getLevel() == ViewLevel.MESO) {
                            redrawIfNeeded();
                            return;
                        }*/

                        //if (!n.selected) {
                        //    System.out.println("adding " + n.uuid);
                            leftSelectFromId(n.uuid);
                       // } else {
                        //    unselectFromId(n.uuid);
                       // }
                        if (session.currentView == ViewLevel.MACRO) {
                            System.out.println("SWITCH TO MESO WITH THE DOUBLE CLICK METHOD");
                            session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL * 2f;
                            jsSwitchToMeso();
                        } else if (session.currentView == ViewLevel.MESO) {
                            System.out.println("SWITCH TO MICRO WITH THE DOUBLE CLICK METHOD");
                            //session.getMicro().sceneScale = session.getMicro().ZOOM_CEIL + session.getMicro().ZOOM_CEIL * 0.5f;
                            //jsSwitchToMicro();
                        }
                    } else {

                        //if (!n.selected) {
                           // System.out.println("adding " + n.uuid);
                            leftSelectFromId(n.uuid);
                       // } else {
                         //   unselectFromId(n.uuid);
                        //}
                    }

                    // RIGHT MOUSE
                } else if (mouseButton == RIGHT) {
                    rightSelectFromId(n.uuid);
                }
                
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
        System.out.println("Zoom: " + v.sceneScale);



        if (e.getWheelRotation() < 0) {



            Node bestMatchForSwitch = null;
            Node bestMatchForSelection = null;


            for (Node n : nodes.nodes) {

                float rad = n.radius * MAX_NODE_RADIUS;
                float rad2 = rad + rad * 0.4f;
                float nsx = screenX(n.position.x, n.position.y);
                float nsy = screenY(n.position.x, n.position.y);

                if (!(nsx > width * 0.2f
                        && nsx < width * 0.8f
                        && nsy > height * 0.2f
                        && nsy < height * 0.8f)) {
                    continue;
                }

                float nsr = screenX(n.position.x + (rad2), n.position.y) - nsx;
                if (nsr < 2) {
                    continue;
                }

                float screenRatio = (((nsr * 2.0f) / (float) width) + ((nsr * 2.0f) / (float) height)) / 2.0f;
                //System.out.println("nsr: " + nsr);
                //System.out.println("'- screen ratio: " + screenRatio);
                if (v.showNodes) {

                    switch (session.currentView) {
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
                        nodeSelected_JS_CALLBACK(true);
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
                        nodeSelected_JS_CALLBACK(true);
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

    /**
     * Clear and reset everything
     */
    public void clear() {
        getSession().clear();
        redrawIfNeeded();
    }

    /**
     * Clear a view
     *
     * @param view
     */
    public void clear(String view) {
        try {
            getSession().getView(view).clear();
        } catch (ViewNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        redrawIfNeeded();
    }

    /**
     * Recenter the view
     */
    public void recenter() {
        recenter = true;
        redrawIfNeeded();
    }

    /**
     * "Touch" a given view (will cause the current view to update)
     * @param level
     */
    public void touch(String view) throws ViewNotFoundException {
        getView(view).getGraph().touch();
    }

    public void touch() {
        getView().getGraph().touch();
    }

    public void resetLayoutCounter() {
        getView().resetLayoutCounter();
    }
    /**
     * Dispatch a property to all views (this method is here to facilitate
     * configuration of a lot of filters on a lot of views)
     *
     * @param key
     * @param value
     * @return
     * @throws KeyException
     */
    public boolean dispatchProperty(String key, Object value) throws KeyException {
        return getSession().setProperty(key, value);
    }

    /**
     * Set a property in a view
     *
     * @param view
     * @param key
     * @param value
     * @return true or false
     * @throws KeyException
     */
    public boolean setProperty(String view, String key, Object value) {

        System.out.println("getProperty(" + view + "," + key + "," + value + ")");

        try {

            return view.equalsIgnoreCase("all")
                    ? getSession().setProperty(key, value)
                    : view.equalsIgnoreCase("current")
                    ? getView().setProperty(key, value)
                    : getView(view).setProperty(key, value);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());
        } catch (ViewNotFoundException ex) {
            Console.error(ex.getMessage());
        }
        return false;
    }

    /**
     * Set a property in the current view
     *
     * @param key
     * @param value
     * @return true or false
     * @throws KeyException
     */
    public boolean setProperty(String key, Object value) {
        try {
            return getView().setProperty(key, value);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Get a property from a given view
     * @param level
     * @param key
     * @return
     * @throws KeyException
     */
    public Object getProperty(String view, String key) {
        Object o = null;
        try {
            o = view.equalsIgnoreCase("current") ? getView().getProperty(key)
                    : getView(view).getProperty(key);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());
            return "";
        } catch (ViewNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // System.out.println("getProperty(" + view + "," + key + ") = " + o);

        return o;
    }

    /**
     * Get a property from the current view
     * @param key
     * @return
     * @throws KeyException
     */
    public Object getProperty(String key) {
        try {
            return getView().getProperty(key);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());
            return "";
        }
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void selectFromId(String str) {
        getSession().selectNode(str);
        nodes.selectNode(str); // so that the callback will now work
        nodeSelected_JS_CALLBACK(true);
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void leftSelectFromId(String str) {
        getSession().selectNode(str);
        nodes.selectNode(str); // so that the callback will now work
        nodeSelected_JS_CALLBACK(true);
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void rightSelectFromId(String str) {
        getSession().selectNode(str);
        nodes.selectNode(str); // so that the callback will now work
        nodeSelected_JS_CALLBACK(false);
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void selectFromIds(List<String> ids) {
        getSession().selectNodes(ids);
        nodes.selectNodes(ids); // so that the callback will now work
        nodeSelected_JS_CALLBACK(true);
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void leftSelectFromIds(List<String> ids) {
        getSession().selectNodes(ids);
        nodes.selectNodes(ids); // so that the callback will now work
        nodeSelected_JS_CALLBACK(true);
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void rightSelectFromIds(List<String> ids) {
        getSession().selectNodes(ids);
        nodes.selectNodes(ids); // so that the callback will now work
        nodeSelected_JS_CALLBACK(false);
    }

    /**
     * Unselect all nodes in all views
     */
    public void unselect() {
        getSession().unselectAll();
    }

    private void unselectFromId(String id) {
        getSession().unselectNode(id);
        nodes.unselect(id); // so that the callback will now work
        nodeSelected_JS_CALLBACK(true);

    }

    /**
     * Get all nodes
     * 
     * @param view - can be either "current", "all", or the view name
     * @param category
     * @return
     */
    public String getNodes(String view, String category) throws UnsupportedEncodingException {

        String def = "[]";

        List<Node> results = new ArrayList<Node>();
        try {
            results = view.contains("current")
                    ? nodes.getNodesByCategory(category)
                    : view.contains("all")
                    ? getSession().getView("macro").getGraph().getNodeListCopy().getNodesByCategory(category)
                    : getSession().getView(view).getGraph().getNodeListCopy().getNodesByCategory(category);
        } catch (ViewNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("view: " + view + " category: " + category + " --- resulting array size: " + results.size());
        if (results.size() == 0) {
            return def;
        }

        JSONWriter writer = null;

        try {
            writer = new JSONStringer().array();
        } catch (JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return def;
        }

        Collections.sort(results);

        try {
            for (Node n : results) {
                // { id: '23a53f-442c5', label: 'hello world' }
                writer.object();
                writer.key("id").value(n.uuid).key("label").value(valueEncoder(n.label));
                writer.endObject();
            }
        } catch (JSONException jSONException) {
            return def;
        }
        try {
            writer.endArray();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return def;
        }
        System.out.println("data: " + writer.toString());
        return writer.toString();
    }

    /**
     * Get a node map by label in the current view
     * 
     * @param label
     * @param mode
     * @return
     */
    public String getNodesByLabel(String label, String mode) throws UnsupportedEncodingException {
        List<Node> results = nodes.getNodesByLabel(label, mode);
        String def = "[]";

        System.out.println("label: " + label + " size: " + results.size());
        if (results.size() == 0) {
            Console.error(" no results");
            return def;
        }

        String result = "";
        JSONWriter writer = null;
        try {
            writer = new JSONStringer().array();
        } catch (JSONException ex) {
            Console.error("error when creating the json array: " + ex.getMessage());
            return def;
        }


        for (Node n : results) {
            try {
                writer.object();
                writer.key("id").value(n.uuid);
                for (Entry<String, Object> entry : n.getAttributes().entrySet()) {
                    writer.key(entry.getKey()).value(valueEncoder(entry.getValue()));
                }
                writer.endObject();
            } catch (JSONException jSONException) {
                Console.error("error when adding  attributes: " + jSONException.getMessage());
            }
        }

        try {
            writer.endArray();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return def;
        }
        //System.out.println("data: " + writer.toString());
        return writer.toString();
    }

    /**
     * Get node attributes in the current view
     * @param id
     * @return
     */
    public String getNodeAttributes(String id) {
        Node node = nodes.getNode(id);


        if (node == null) {
            return "{}";


        }
        return node.getAttributesAsJSON();


    }

    /**
     * Get a node's neighbourhood
     * @param id
     * @return
     */
    public String getNeighbourhood(String id) throws UnsupportedEncodingException {
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
                    writer.key(entry.getKey()).value(valueEncoder(entry.getValue()));
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

    public void centerOnSelection() {
        // check if the node is in the current view
        centerOnSelection = true;
    }
}
