package tinaviz;

import eu.tinasoft.services.visualization.layout.Layout;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.data.model.ShapeCategory;
import eu.tinasoft.services.visualization.views.View;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.security.KeyException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.tinasoft.services.session.*;
import processing.core.*;
import processing.pdf.*;
import netscape.javascript.*;

import eu.tinasoft.services.protocols.browser.Browser;
import eu.tinasoft.services.formats.json.JSONException;
import eu.tinasoft.services.formats.json.JSONStringer;
import eu.tinasoft.services.formats.json.JSONWriter;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.computing.MathFunctions;
import eu.tinasoft.services.data.model.Metrics;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.transformation.filters.Output;
import eu.tinasoft.services.formats.json.JSONEncoder;
import eu.tinasoft.services.visualization.rendering.drawing.RecordingFormat;
import eu.tinasoft.services.visualization.views.ViewLevel;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class Main extends PApplet implements MouseWheelListener {

    String PATH_TO_TEST_FILE =
            // "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/French_bipartite_graph.gexf"
            "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/FET60bipartite_graph_cooccurrences_.gexf" //"file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/CSSScholarsMay2010.gexf";
            // "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/test.gexf"
            //  "file:///home/jbilcke/Checkouts/git/TINA/tinaweb/html/CSSScholarsMay2010.gexf"
            ;
    String VENDOR_URL = "http://tinaweb.sciencemapping.com";
    boolean autocenter = true;
    float SELECTION_DISK_RADIUS = 150f;
    boolean generateRandomLocalGraph = false;
    boolean loadDefaultLocalGraph = false;
    boolean loadDefaultGlobalGraph = false;
    boolean generateRandomGlobalGraph = false;
    public PImage currentImg = null;
    public PVector ref = new PVector();
    public PVector drawerTranslation = new PVector();
    public PVector drawerLastPosition = new PVector();
    public boolean graphStillVisible = true;
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
    Session session = new Session();
    float screenRatioSelectNodeWhenZoomed = 0.4f;
    float screenRatioGoToMesoWhenZoomed = 0.7f;
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    AtomicBoolean zooming = new AtomicBoolean(false);
    AtomicBoolean zoomIn = new AtomicBoolean(false);
    AtomicBoolean mouseClickLeft = new AtomicBoolean(false);
    AtomicBoolean mouseClickRight = new AtomicBoolean(false);
    AtomicBoolean debug = new AtomicBoolean(false);
    AtomicBoolean redrawScene = new AtomicBoolean(true);
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
    int oldScreenWidth = 0;
    int oldScreenHeight = 0;
    private PVector cameraDelta = new PVector(0.0f, 0.0f, 0.0f);
    private int bezierSize = 18;
    private int visibleEdges = 0;
    private int visibleNodes = 0;
    public String js_context = "";
    private boolean centerOnSelection = false;
    private boolean loading = true;

    private void drawNothing(View v) {
        translate(v.translation.x, v.translation.y);
        scale(v.sceneScale);
    }

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

        js_context = (getParameter("js_context") != null) ? getParameter("js_context") : "";

        String engine = P2D;
        if (getParameter("engine") != null) {
            if (getParameter("engine").equals("software")) {
                engine = P2D;

            } else if (getParameter("engine").equals("hardware")) {
                engine = OPENGL;

            } else if (getParameter("engine").equals("hybrid")) {
                engine = JAVA2D;

            }
            session.setBrowser(new Browser(JSObject.getWindow(this), js_context));
            Console.setBrowser(session.getBrowser());
            int w = 200;
            int h = 200;
            size(w, h, engine);

        } else {
            session.setBrowser(new Browser());
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

        if (loadDefaultGlobalGraph) {
            Console.log("loading default graph..");
            session.getMacro().getGraph().updateFromURI(
                    PATH_TO_TEST_FILE);

            try {
                session.getMacro().setProperty("category/category", "NGram");
            } catch (KeyException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            session.getMacro().addFilter("Category", "category");
            //session.getMacro().addFilter("NodeWeightRange", "nodeWeight");
            session.getMacro().addFilter("EdgeWeightRange", "edgeWeight");
            session.getMacro().addFilter("NodeFunction", "radiusByWeight");

            // output denormalize and decxide the final screen size (it does not normalize to 0 -> 1 !)
            session.getMacro().addFilter("Output", "output");

            session.getView().paused = false;

        }
        session.toMacroView();

        lastMousePosition = new PVector(width / 2.0f, height / 2.0f, 0);

        brandingImage = loadImage("tina_icon.png");

        session.getBrowser().init();
        Console.log("Visualization started..");
    }

    @Override
    public void draw() {
        View v = session.getView();
        v.screenWidth = width;
        v.screenHeight = height;
        if (!this.isEnabled()) {
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

    public void stopAutoCentering() {
        if (autocenter) {
            autocenter = false;
            session.getBrowser().buttonStateCallback("autoCentering", autocenter);
        }
        centerOnSelection = false;
    }

    public void checkRecentering(View v) {
        if (autocenter) {
            Metrics metrics = nodes.computeMetrics();
            float graphHeight = metrics.graphHeight * v.sceneScale;
            float graphWidth = metrics.graphWidth * v.sceneScale;

            if ((graphWidth * height) / (graphHeight) < (width * 1.0f)) {
                v.tryToSetZoom(v.sceneScale * height / graphHeight / v.RECENTERING_MARGIN);
            } else {
                v.tryToSetZoom(v.sceneScale * width / graphWidth / v.RECENTERING_MARGIN);
            }
            PVector center = new PVector();
            if (centerOnSelection) {
                center = nodes.getSelectedNodesCenter();
            } else {
                center = metrics.center;
            }
            PVector translate = new PVector();
            translate.add(new PVector(width / 2.0f, height / 2.0f, 0));
            translate.sub(PVector.mult(center, v.sceneScale));
            v.translation.set(translate);
        }
    }

    public void draw2(View v, NodeList n) {

        if (n != null) {
            nodes = n;
        }

        if (nodes.size() > 0) {
            checkRecentering(v);
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

        } else {
            drawAndSpatializeRealtime(v);
        }
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

        if (visibleEdges < 150) {
            resolution = 60;
        } else if (visibleEdges < 300) {
            resolution = 50;
        } else if (visibleEdges < 600) {
            resolution = 40;
        } else if (visibleEdges < 900) {
            resolution = 35;
        } else if (visibleEdges < 1500) {
            resolution = 30;
        } else if (visibleEdges < 3000) {
            resolution = 25;
        } else if (visibleEdges < 4000) {
            resolution = 20;
        } else if (visibleEdges < 5000) {
            resolution = 15;
        } else if (visibleEdges < 6000) {
            resolution = 12;
        } else if (visibleEdges < 8000) {
            resolution = 10;
        } else if (visibleEdges < 10000) {
            resolution = 7;
        } else if (visibleEdges < 15000) {
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
            // the layout force the pause?
            if (v.paused) {
                session.getBrowser().buttonStateCallback("paused", v.paused);
            }
        }


        // TODO optimize here
        nodes.sortBySelectionStatus();

        Metrics metrics = nodes.getMetrics();

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

        metrics.nbVisibleNodes = (visibleNodes < 1) ? 1 : visibleNodes;
        metrics.nbVisibleEdges = (visibleEdges < 1) ? 1 : visibleEdges;

        if (debug.get()) {
            text("" + ((int) frameRate) + " img/sec", 10f, 13f);
            text("" + metrics.nbVisibleNodes + "/" + metrics.nbNodes + " nodes", 80f, 13f);
            text("" + metrics.nbVisibleEdges + "/" + metrics.nbEdges + " edges", 190f, 13f);
            text("aliasing: " + (resolution >= 40) + "    resolution: " + resolution, 310f, 13f);
            fill(0);
        }
        visibleNodes = 0;
        visibleEdges = 0;
        //pushMatrix();

        translate(v.translation.x, v.translation.y);
        scale(v.sceneScale);
        noFill();

        /************************************
         * Check if nodes are visibles
         ************************************/
        for (Node n : nodes.nodes) {
            n.screenPosition.set(
                    screenX(n.position.x, n.position.y),
                    screenY(n.position.x, n.position.y), 0.0f);
            n.visibleToScreen = (n.screenPosition.x > -(width / 4.0f)
                    && n.screenPosition.x < width + (width / 4.0f)
                    && n.screenPosition.y > -(height / 4.0f)
                    && n.screenPosition.y < height + (height / 4.0f));
        }

        /************************************
         * Parse all edges
         ************************************/
        for (Node n1 : nodes.nodes) {
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
                        //System.out.println("same node wieght, choosing an arbitrary direction..");
                        if (n1.id > n2.id) {
                            continue;
                        }

                    }
                }


                visibleEdges++;

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

                float powd = PApplet.dist(n1.screenPosition.x, n1.screenPosition.y, n2.screenPosition.x, n2.screenPosition.y);
                float modulator = constrain(PApplet.map(powd, 8, width, 1, 90), 1, 90);
                bezierDetail((int) modulator);

                float mn = PApplet.min(n1.radius, n2.radius);
                float minRad = mn == 0 ? Output.RADIUS_MIN : mn * 0.2f;
                float maxRad = mn == 0 ? Output.RADIUS_MIN : mn * 0.5f;
                float screenWeight =
                        (metrics.minEdgeWeight == metrics.maxEdgeWeight)
                        ? maxRad
                        : PApplet.map(w,
                        metrics.minEdgeWeight,
                        metrics.maxEdgeWeight,
                        minRad,
                        maxRad);

                strokeWeight(screenWeight * v.sceneScale);
                drawCurve(n1.position.x, n1.position.y, n2.position.x, n2.position.y);
            } // FOR NODE B
        }   // FOR NODE A

        noStroke();

        for (Node n : nodes.nodes) {
            if (!n.visibleToScreen) {
                continue;
            }

            boolean highlighted = (n.isFirstHighlight | n.isSecondHighlight);
            float nx = n.position.x;
            float ny = n.position.y;
            float rad = n.radius;
            float rad2 = rad * 1.3f;

            float nodeScreenDiameter = screenX(nx + rad2, ny) - screenX(nx - rad2, ny);
            if (nodeScreenDiameter < 1) {
                continue;
            }

            float alpha = 240;
            if (n.selected) {
                alpha = 200;
            } else if (highlighted) {
                alpha = 150;
            } else {

                // TODO it works but very bad design..
                // should be simplified and refactored
                float minRad = 12.0f, maxRad = 20.0f;
                int minRadColor = 1, maxRadColor = 200;
                float tRatio = 1.0f / (maxRad - minRad);
                float nsdRatio = constrain((nodeScreenDiameter - minRad) * tRatio, 0, 1);
                alpha = minRadColor + nsdRatio * (float) (maxRadColor - minRadColor);

            }

            if (v.showNodes) {
                if (n.selected | highlighted) {
                    rad *= 1.0f;
                    rad2 *= 1.0f;
                }

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
                visibleNodes++;
            } // end of "if show nodes"

            // skip label drawing for small nodes
            // or if we have to hide labels
            if (nodeScreenDiameter < 1 | !(v.showLabels | n.selected | highlighted)) {
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
        if (SELECTION_DISK_RADIUS > 1) {
            scale(1.0f / v.sceneScale);
            translate(-v.translation.x, -v.translation.y);
            PVector p = new PVector(0, 0, 0);
            stroke(0, 0, 0, 40);
            strokeWeight(1.0f);
            fill(00, 100, 200, 29);
            p.add(mouseX, mouseY, 0);
            ellipse(p.x, p.y, SELECTION_DISK_RADIUS, SELECTION_DISK_RADIUS);

            // back-compatibility hack for Processing
            translate(v.translation.x, v.translation.y);
            scale(v.sceneScale);
        }
    }

    public void drawBranding(View v, PVector p) {
        if (!showBranding) {
            return;
        }
        image(brandingImage, p.x, p.y,
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
        return session;
    }

    public View getView() {
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

        if (mouseX > width - 30 && mouseY > height - 25) {
            cursor(HAND);
        } else {
            cursor(ARROW);
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

    public enum MouseButtonAction {

        NONE, LEFT, DOUBLELEFT, RIGHT;
    };

    @Override
    public void mouseClicked() {
        List<Integer> selectedIDs = new ArrayList<Integer>();
        List<Integer> unselectedIDs = new ArrayList<Integer>();
        MouseButtonAction mouseSide = MouseButtonAction.NONE;

        if (mouseX > width - 30 && mouseY > height - 25) {
            link(VENDOR_URL, "_new");
            return;
        }

        for (Node n : nodes.nodes) {
            float nsx = screenX(n.position.x, n.position.y);
            float nsy = screenY(n.position.x, n.position.y);
            boolean match = false;

            if (SELECTION_DISK_RADIUS > 1) {
                if ((dist(mouseX, mouseY, nsx, nsy) < SELECTION_DISK_RADIUS / 2.0f)) {
                } else {
                    continue;
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
                } else {
                    continue;
                }
            }

            if (mouseButton == LEFT) {
                if (mouseEvent != null && mouseEvent.getClickCount() == 2) {
                    mouseSide = MouseButtonAction.DOUBLELEFT;
                    selectedIDs.add(n.id);
                    n.selected = true;
                } else {
                    mouseSide = MouseButtonAction.LEFT;
                    if (n.selected) {
                        unselectedIDs.add(n.id);
                    } else {
                        selectedIDs.add(n.id);
                    }
                    n.selected = !n.selected;
                }

            } else if (mouseButton == RIGHT) {
            }
        }


        // if we have a double click, we unselect nodes in all views, graphs..
        if (mouseSide == MouseButtonAction.DOUBLELEFT) {
            unselect();
        }
        //    else {
        for (int i : unselectedIDs) {
            getSession().unselectNode(i);
        }
        //}

        // we select our new nodes in all views, graphs..
        for (int i : selectedIDs) {
            nodes.selectNode(i);
            getSession().selectNode(i);
        }


        // in all cases, we call the callback function
        if (mouseSide != MouseButtonAction.NONE) {
            System.out.println("calling callback(" + mouseSide + ")");
            nodeSelected_JS_CALLBACK(getView().getName(), mouseSide);
        }

        lastMousePosition.set(mouseX, mouseY, 0);
        redrawIfNeeded();
    }

    void redrawIfNeeded() {
        redrawScene.set(true);
    }

    @Override
    public void mouseDragged() {
        stopAutoCentering();
        if (mouseButton == RIGHT | mouseButton == LEFT) {
            View v = session.getView();
            PVector oldTranslation = new PVector(v.translation.x, v.translation.y, 0.0f);
            v.translation.sub(lastMousePosition);
            lastMousePosition.set(mouseX, mouseY, 0);
            v.translation.add(lastMousePosition);
            cameraDelta.set(PVector.sub(oldTranslation, v.translation));
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
        stopAutoCentering();
        if (e.getUnitsToScroll() == 0) {
            return;
        }

        showBranding = false;
        View v = getView();


        lastMousePosition.set(mouseX, mouseY, 0);

        // TODO check for limits before
        if (e.getWheelRotation() < 0) { // ZOOM-IN
            if (v.tryToMultiplyZoom(4.f / 3.f)) {
                v.translation.sub(lastMousePosition);
                v.translation.mult(4.f / 3.f);
                v.translation.add(lastMousePosition);
            }
        } else { // ZOOM-OUT

            if (v.tryToMultiplyZoom(3.f / 4.f)) {
                v.translation.sub(lastMousePosition);
                v.translation.mult(3.f / 4.f);
                v.translation.add(lastMousePosition);
            } else {
                if (v.getLevel() == ViewLevel.MESO) {
                    System.out.println("GOING BACK TO MACRO VIEW");
                    setView("macro");
                }
            }
        }


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
                    if (!bestMatchForSwitch.selected) {
                        bestMatchForSwitch.selected = true;
                        session.selectNode(bestMatchForSwitch);
                        nodeSelected_JS_CALLBACK(v.getName(), MouseButtonAction.DOUBLELEFT);
                    }
                    //redrawIfNeeded();
                    return;
                } else if (bestMatchForSelection != null) {
                    if (!bestMatchForSelection.selected) {
                        bestMatchForSelection.selected = true;
                        session.selectNode(bestMatchForSelection);
                        nodeSelected_JS_CALLBACK(v.getName(), MouseButtonAction.LEFT);
                    }
                    //redrawIfNeeded();
                    return;
                }
            }
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
            v.showLinks = !v.showLinks;
            session.getBrowser().buttonStateCallback("showEdges", v.showLinks);
        } else if (key == 't') {
            v.showLabels = !v.showLabels;
            session.getBrowser().buttonStateCallback("showLabels", v.showLabels);
        } else if (key == 'n') {
            v.showNodes = !v.showNodes;
            session.getBrowser().buttonStateCallback("showNodes", v.showNodes);
        } else if (key == 'r') {
            autoCentering();
        } else if (key == 's') {
            centerOnSelection();
            autoCentering();
        } else if (key == 'a') {
            v.paused = !v.paused;
            session.getBrowser().buttonStateCallback("paused", v.paused);
            System.out.println("Animation paused is now " + v.paused);

        } else if (key == 'h') {
            /* v.highDefinition = !v.highDefinition;
            System.out.println("HD mode is now " + v.highDefinition);*/
        } else if (key == 'o') {
            /* if ((v.attraction + 0.00001) < 0.0004) {
            v.attraction += 0.00001f;
            System.out.println("\nattraction: " + session.getView().attraction);
            }*/
        } else if (key == 'l') {
            /*if ((v.attraction - 0.00001f) > 1.5e-5) {
            v.attraction -= 0.00001f;
            System.out.println("\nattraction: " + session.getView().attraction);
            }*/
        } else if (key == 'g') {
            /*if ((v.gravity + 0.001f) < 0.5f) {
            v.gravity += 0.001f;
            System.out.println("\ngravity: " + session.getView().gravity);
            }*/
        } else if (key == 'b') {
            /* if ((v.gravity - 0.001f) > 0.0f) {
            v.gravity -= 0.001f;
            System.out.println("\ngravity: " + session.getView().gravity);
            }*/
        } else if (key == 'd') {
            debug.set(!debug.get());

        }
        redrawIfNeeded();
    }

    public void dispatchCallbackStates() {
        View v = getView();
        Browser b = getSession().getBrowser();
        b.buttonStateCallback("showEdges", v.showLinks);
        b.buttonStateCallback("showLabels", v.showLabels);
        b.buttonStateCallback("showNodes", v.showNodes);
        b.buttonStateCallback("autoCentering", autocenter);
        b.buttonStateCallback("paused", v.paused);
        //System.out.println("dispatched toolbar buttons states!");
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
    public void autoCentering() {
        System.out.println("activating auto-centering");
        autocenter = true;
        session.getBrowser().buttonStateCallback("autoCentering", autocenter);
        redrawIfNeeded();
    }

    /**
     * "Touch" a given view (will cause the current view to update)
     * @param view
     * @return
     * @throws ViewNotFoundException
     */
    public synchronized int touch(String view) throws ViewNotFoundException {

        return getView(view).getGraph().commitProperties();
    }

    @Deprecated
    public synchronized int touch() {
        return commitProperties();
    }

    public synchronized int commitProperties() {
        return getView().getGraph().commitProperties();
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
     */
    public boolean setProperty(String view, String key, Object value) {
        //System.out.println("setProperty " + view + "." + key + " = " + value + "");
        try {
            return view.equalsIgnoreCase("all")
                    ? getSession().setProperty(key, value)
                    : view.equalsIgnoreCase("current")
                    ? getView().setProperty(key, value)
                    : getView(view).setProperty(key, value);
        } catch (KeyException ex) {
            System.out.println("error: " + ex);
            Console.error(ex.getMessage());
        } catch (ViewNotFoundException ex) {
            System.out.println("error: " + ex);
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
     */
    public boolean setProperty(String key, Object value) {
        // System.out.println("setProperty " + key + " = " + value + "");
        try {
            return getView().setProperty(key, value);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Get a property from a given view
     *
     * @param view
     * @param key
     * @return
     */
    public Object getProperty(String view, String key) {

        Object o = "";
        try {
            o = view.equalsIgnoreCase("current") ? getView().getProperty(key)
                    : getView(view).getProperty(key);
        } catch (KeyException ex) {
            System.out.println("error, got key exception " + ex);
            Console.error(ex.getMessage());
        } catch (ViewNotFoundException ex) {
            System.out.println("error, got key exception " + ex);
            Console.error(ex.getMessage());
        }
        if (!(view.equals("current") && key.equals("selection/radius"))) {
            //System.out.println(o + " = getProperty(" + view + "," + key + ")");
        }

        return o;
    }

    /**
     * Get a property from the current view
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        System.out.println("getProperty(" + key + ")");

        Object o = "";
        try {
            o = getView().getProperty(key);
        } catch (KeyException ex) {
            Console.error(ex.getMessage());


        }
        if (!key.equals("selection/radius")) {
            System.out.println(o + " = getProperty(" + key + ")");
        }

        return o;
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void selectFromId(String str) {
        System.out.println("BEFORE selectFromId(" + str + ")...");
        Session s = getSession();
        s.selectNode(str);
        nodes.selectNode(str);
        System.out.println("DURING selectFromId(" + str + "), called s.selectNode(..) and nodes.selectNode(..)");
        nodeSelected_JS_CALLBACK(s.getView().getName(), MouseButtonAction.LEFT);
        System.out.println("AFTER selectFromId(" + str + ") called from javascript, calling nodeSelected_JS_CALLBACK(" + s.getView().getName() + ",MouseButton.LEFT)");
    }

    /**
     * select a node from it's ID in all views
     * @param str
     */
    public void highlightFromId(String str) {
        nodes.highlightNodeById(str);
        getSession().getGraph().highlightNodeById(str);

    }

    /**
     * Unselect all nodes in all views
     */
    public void unselect() {
        getSession().unselectAll();
        nodes.unselectAll();
    }

    /**
     * Get all nodes
     *
     * @param view - can be either "current", "all", or the view name
     * @param category
     * @return
     * @throws UnsupportedEncodingException
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
                writer.key("id").value(n.uuid).key("label").value(JSONEncoder.valueEncoder(n.label));
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
        System.out.println(writer.toString() + "= getNodes(String view, String category)");
        return writer.toString();
    }

    /**
     * Get a node map by label in the current view
     *
     * @param label
     * @param mode
     * @return
     * @throws UnsupportedEncodingException
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
                    writer.key(entry.getKey()).value(JSONEncoder.valueEncoder(entry.getValue()));
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
        System.out.println(writer.toString() + "= MAIN.getNodesByLabel(" + label + "," + mode + ")");
        return writer.toString();
    }

    /**
     * Get node attributes in the current view
     * @param id
     * @return
     */
    public String getNodeAttributes(String id) {
        Node node = nodes.getNode(id);
        return (node == null) ? "{}" : node.getAttributesAsJSON();
    }

    /**
     * Get a node's neighbourhood
     * @param view
     * @param id
     * @return
     * @throws UnsupportedEncodingException
     * @throws ViewNotFoundException
     */
    public String getNeighbourhood(String view, String id) throws UnsupportedEncodingException, ViewNotFoundException {

        return (view == null | view.isEmpty() | view.equalsIgnoreCase("current"))
                ? nodes.getNeighbourhoodAsJSON(id)
                : getView(view).getGraph().getNeighbourhoodAsJSON(id);
    }

    public void centerOnSelection() {
        // check if the node is in the current view
        centerOnSelection = true;
    }

    public boolean setView(String view) {
        //System.out.println("setView(" + view + ")");
        if (view.equalsIgnoreCase("current")) {
            viewChanged_JS_CALLBACK(view);
            nodeSelected_JS_CALLBACK(view, MouseButtonAction.LEFT);
            return true;
        }

        if (session.setView(view)) {
            viewChanged_JS_CALLBACK(view);
            nodeSelected_JS_CALLBACK("macro", MouseButtonAction.LEFT);
        } else {
            Console.error("set view failed..");
            return false;
        }

        return true;
    }

    private String getSelectNodesJSON(String view) {
        // System.out.println("getSelectedNodesAsJSON(" + view + ")");
        if (view.equalsIgnoreCase("current")) {
            return nodes.getSelectedNodesAsJSON();
        }
        try {
            return getView(view).getGraph().getSelectedNodesAsJSON();
        } catch (ViewNotFoundException ex) {
            Console.error("couldn't get select nodes as JSON: " + ex + ", returning {}");
            return "{}";
        }
    }

    private void nodeSelected_JS_CALLBACK(String view, MouseButtonAction mouseSide) {
        getSession().getBrowser().callAndForget("selected", "'" + view + "','"
                + getSelectNodesJSON(view) + "','" + (mouseSide == MouseButtonAction.DOUBLELEFT ? "doubleLeft"
                : mouseSide == MouseButtonAction.LEFT ? "left"
                : mouseSide == MouseButtonAction.RIGHT ? "right"
                : "none") + "'");
    }

    private void viewChanged_JS_CALLBACK(String view) {
        //System.out.println("calling getSession().getBrowser().async(\"switchedTo\", \"'" + view + "'\");");
        getSession().getBrowser().callAndForget("switchedTo", "'" + view + "'");
        dispatchCallbackStates();
    }
}
