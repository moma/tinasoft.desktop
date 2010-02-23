package tinaviz;

import tinaviz.filters.FilterChannel;
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import tinaviz.model.*;
import processing.opengl.*;
import processing.core.*;
import processing.xml.*;
import processing.pdf.*;
// import netscape.javascript.*;
//import netscape.javascript.JSObject ;
import netscape.javascript.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import tinaviz.filters.Channel;
import tinaviz.filters.Filter;
import tinaviz.filters.ForceVector;
import tinaviz.filters.ParamChannel;
import tinaviz.filters.AttributeFilter;

public class Main extends PApplet implements MouseWheelListener {

    static int MAXLINKS = 512;
    float zoomRatio = 1.0f;

    PImage nodeIcon;
    PFont font;
    float vizx = 0f;
    float vizy = 0f;
    float oldmouseX = 0f;
    float oldmouseY = 0f;
    float MAX_RADIUS = 0f;
    XMLElement xml;
    Session session = new Session();
    float MAX_ZOOM = 5f;
    float MIN_ZOOM = 0.02f;
    private boolean mouseDragging = false;
    private float inerX = 0.0f;
    private float inerY = 0.0f;
    private float inerZ = 0.0f;
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    private boolean mouseClick = false;
    private int preSpatialize = 10;
    private String currentTextSearch = "&&&&";
    private JSObject window = null;
    private int recordingWidth = 100;
    private int recordingHeight = 100;
    private boolean debugMode = true;
    private String DEFAULT_FONT = "ArialMT-150.vlw";
    AtomicBoolean screenBufferUpdated = new AtomicBoolean(false);
    AtomicBoolean screenBufferUpdating = new AtomicBoolean(false);
    private int stepCounter = 0;
    // Semaphore screenBufferLock = new Semaphore();

    private List<tinaviz.Node> nodes = new ArrayList<tinaviz.Node>();
    

        private void nodeClicked(Node n) {
            window.eval(
                    "nodeClicked(" + screenX(n.x, n.y)
                    + "," + screenY(n.x, n.y)
                    + ",\"" + n.uuid + "\""
                    + ",\"" + n.label + "\");");
    }

    private void showNodeDetails(Node n) {
        if (session.showNodeDetails) {
            return;
        }
        session.animationPaused = true;
        //currentView.colorsDesaturated = true;
        session.zoomFrozen = true;
        session.showNodeDetails = true;

        if (window != null) {
            window.eval(
                    "parent.showNodeDetails(" + screenX(n.x, n.y)
                    + "," + screenY(n.x, n.y)
                    + ",\"" + n.uuid + "\""
                    + ",\"" + n.label + "\");");
        }

    }

    private void hideNodeDetails() {
        if (!session.showNodeDetails) {
            return;
        }
        if (window != null) {
            window.eval("parent.hideNodeDetails();");
        }
        //currentView.colorsDesaturated = false;
        session.animationPaused = false;
        session.zoomFrozen = false;
        session.showNodeDetails = false;
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

    /*
    public static void main (String[] args){
    }
     */
    @Override
    public void setup() {

        //String engine = (getParameter("engine")) ? getParameter("engine") : "P2D";
        font = loadFont(DEFAULT_FONT);
        //font = createFont("Arial", 96, true);

        //String[] fontList = PFont.list();
        //println(fontList);



        String engine = P2D;
        if (getParameter("engine") != null) {
            if (getParameter("engine").equals("software")) {
                engine = P2D;

            } else if (getParameter("engine").equals("hardware")) {
                engine = OPENGL;

            }
            // engine = OPENGL;
            window = JSObject.getWindow(this);
            int w = screen.width;
            int h = screen.height;

            w = (Integer) window.call("getWidth", null);
            h = (Integer) window.call("getHeight", null);
            window.eval("appletInitialized();");
            size(w, h, engine);
        } else {
            size(screen.width, screen.height, engine);
        }

        textFont(font);
        smooth();

        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully



        session = new Session();

        // currentView.showLabels = false;
        oldmouseX = mouseX;
        oldmouseY = mouseY;



        boolean generateRandom = false;

        if (generateRandom) {
        Node node;
        System.out.println("Generating random graph..");
        float rx = random(width);
        float ry = random(height);
        float radius = 0.0f;
        for (int i = 0; i < 200; i++) {
            radius = random(3.0f, 10.0f);
            if (radius > MAX_RADIUS) {
                MAX_RADIUS = radius;
            }
            node = new Node("" + i, "node " + i, radius, random(width / 2), random(height / 2));
            node.genericity = random(1.0f);
            // System.out.println(node.genericity);
            session.getNetwork().addNode(node);

        }

        Node a;
        Node b;
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size() && i != j; j++) {
                if (random(1.0f) < 0.009) { // link density : 0.02 = a lot, 0.0002 = a few
                    nodes.get(i).addNeighbour(nodes.get(j));
                }
            }
        }

        }

        else {
            try {

            updateViewFromURI("file:///home/jbilcke/Checkouts/git/TINA"+
                    "/tinasoft.desktop/tina/chrome/content/applet/data/" +
                    "map_dopamine_2002_2007_g.gexf");
             session.prespatialize = false;
             session.animationPaused = true;
            } catch (URISyntaxException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

        // fill(255, 184);
        frameRate(60);
        smooth();

        textFont(font, 48);

        center();

        System.out.println("Starting visualization..");

    }

    @Override
    public void draw() {

        if (!this.isEnabled()) return;

        if (!session.isSynced.get()) {
            List<Node> n = session.getNodes();
            if (n != null) {
                nodes.clear();
                nodes.addAll(n);
            }
            //session.animationPaused = tmp; // TODO replace by a lock here
            //preSpatialize = 60;
            center(); // dynamic recenter
        }

        // TODO put this in another thread
        if (recordingMode != RecordingFormat.NONE) {

            if (recordingMode == RecordingFormat.PDF) {
                pdfDrawer(width, height);
            } else if (recordingMode == RecordingFormat.CURRENT_PICTURE) {
                pictureDrawer(width, height);
                //save(recordPath);
            } else if (recordingMode == RecordingFormat.BIG_PICTURE) {
                pictureDrawer(recordingWidth, recordingHeight);
            }
            recordingMode = RecordingFormat.NONE;

        } else if (session.prespatialize && preSpatialize-- > 0) {
            drawLoading();
            spatialize();
        } else {
            drawAndSpatializeRealtime();
        }
    }

    public void drawLoading() {
        background(255);
        fill(200 - 3 * preSpatialize);
        textSize(40);
        String base = "Loading";
        float x = width / 2.0f - 50;
        float y = height / 2.0f;
        if (preSpatialize > 50) {
            text(base + ".", x, y);
        } else if (preSpatialize > 40) {
            text(base + "..", x, y);
        } else if (preSpatialize > 30) {
            text(base + "...", x, y);
        } else if (preSpatialize > 20) {
            text(base + ".", x, y);
        } else if (preSpatialize > 10) {
            text(base + "..", x, y);
        } else if (preSpatialize > 0) {
            text(base + "...", x, y);
        }
    }

    public void spatialize() {

        float len = 1f;
        float vx = 0f;
        float vy = 0f;
        float LAYOUT_REPULSION = 0.005f;
        float LAYOUT_ATTRACTION = 0.0001f;

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                len = sqrt(sq(vx) + sq(vy));

                if (n1.neighbours.contains(n2)) {
                    n1.vx += (vx * len) * LAYOUT_ATTRACTION;
                    n1.vy += (vy * len) * LAYOUT_ATTRACTION;
                    n2.vx -= (vx * len) * LAYOUT_ATTRACTION;
                    n2.vy -= (vy * len) * LAYOUT_ATTRACTION;
                }

                if (len != 0) {

                    // TODO fix this
                    n1.vx -= (vx / len) * LAYOUT_REPULSION;
                    n1.vy -= (vy / len) * LAYOUT_REPULSION;
                    n2.vx += (vx / len) * LAYOUT_REPULSION;
                    n2.vy += (vy / len) * LAYOUT_REPULSION;

                }
            } // FOR NODE B
        }   // FOr NODE A

        for (Node n : nodes) {
            n.x += n.vx;
            n.y += n.vy;
            n.vx = 0.0f;
            n.vy = 0.0f;
        }
    }

    public void pdfDrawer(int w, int h) {
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

        genericDrawer(pdf, w, h, vizx, vizy);
        pdf.dispose();
        pdf.endDraw();


    }

    public void pictureDrawer(int w, int h) {
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

        genericDrawer(pg, w, h, vizx, vizy);
        pg.endDraw();
        pg.save(recordPath);
    }

    public void genericDrawer(PGraphics pg, int w, int h, float vizx, float vizy) {


        //////////// POSITION ////////////////////////////////
        pg.translate(vizx, vizy);
        pg.scale(zoomRatio);

        pg.stroke(150, 150, 150);
        pg.fill(120);
        pg.strokeWeight(1.0f);

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                if (n1.neighbours.contains(n2)) {

                    // AFFICHAGE LIEN (A CHANGER)
                    float rpond = ((n1.radius + n2.radius) * 0.5f);
                    int rgb = (int) ((255.0f / MAX_RADIUS) * rpond);

                    // old: 150
                    pg.stroke(rgb);

                    pg.strokeWeight(((n1.radius + n2.radius) * 0.05f));


                    if ((n1.genericity <= session.upperThreshold
                            && n1.genericity >= session.lowerThreshold)
                            && (n2.genericity <= session.upperThreshold
                            && n2.genericity >= session.lowerThreshold)) {
                        pg.line(n1.x, n1.y, n2.x, n2.y);
                        arrow(pg, n2.x, n2.y, n1.x, n1.y, n1.radius);
                    }
                }

            } // FOR NODE B
        }   // FOr NODE A

        pg.stroke(20, 20, 20);
        // fill(200, 200, 200);
        pg.fill(218, 219, 220);
        pg.strokeWeight(1.2f);


        for (Node n : nodes) {
            if (!(n.genericity <= session.upperThreshold
                    && n.genericity >= session.lowerThreshold)) {
                continue;
            }

            int rgb = (int) ((255.0 / MAX_RADIUS) * n.radius);

            pg.ellipse(n.x, n.y, n.radius, n.radius);

            pg.fill(120);
            //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
            pg.textSize(n.radius * 0.08f);

            pg.fill(150);//old: 110
            pg.text(n.label, n.x + n.radius,
                    n.y + (n.radius / 2.50f));

        }

    }


    public void drawAndSpatializeRealtime() {

        stepCounter++;
        
        Node node_a, node_b, node;

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
        float LAYOUT_REPULSION = 0.01f;
        float LAYOUT_ATTRACTION = 0.0001f;


        background(255);
        stroke(0);
        fill(120);


        if (session.showPosterOverlay) {
            fill(30);
            textSize(40);
            text("TinaSoft", 15f, 50f);

            fill(80);
            textSize(18);
            text("A cool project subtitle", 18f, 70f);
            fill(120);
        }

        if (!mouseDragging) {

            // todo: make it proportionnal to the zoom level ?


            //  0.01 = sticky, 0.001 smoothie
            inerX = (abs(inerX) <= 0.09) ? 0.0f : inerX * 0.9f;
            inerY = (abs(inerY) <= 0.09) ? 0.0f : inerY * 0.9f;
            inerZ = (abs(inerZ) <= 0.06) ? 0.0f : inerZ * 0.9f;
            vizx += inerX * 2.0f;
            vizy += inerY * 2.0f;
            zoomRatio += inerZ * 0.015f;
        }

        translate(vizx, vizy);

        if (zoomRatio > 10f) {
            zoomRatio = 10f;
            inerZ = 0.0f;
        }
        if (zoomRatio < 0.1f) {
            zoomRatio = 0.1f;
            inerZ = 0.0f;
        }
        scale( zoomRatio * (log(zoomRatio)));

        stroke(150, 150, 150);

        if (cameraIsStopped()) {
            strokeWeight(3.0f);
        }

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                if (cameraIsStopped()) {
                    vx = n2.x - n1.x;
                    vy = n2.y - n1.y;
                    len = sqrt(sq(vx) + sq(vy));

                }

                if (n1.neighbours.contains(n2)) {

                    // ATTRACTION
                    if (!mouseDragging) {
                        if (!session.animationPaused) {
                            n1.vx += (vx * len) * LAYOUT_ATTRACTION;
                            n1.vy += (vy * len) * LAYOUT_ATTRACTION;
                            n2.vx -= (vx * len) * LAYOUT_ATTRACTION;
                            n2.vy -= (vy * len) * LAYOUT_ATTRACTION;
                        }
                    }
                    // AFFICHAGE LIEN (A CHANGER)

                    float rpond = ((n1.radius + n2.radius) * 0.5f);
                    int rgb = (int) ((255.0f / MAX_RADIUS) * rpond);

                    if (this.session.showLinks) {
                        // old: 150
                        
                        if (cameraIsStopped() && stepCounter > 1) {
                            stroke(rgb);
                            strokeWeight(n1.weights.get(n2.uuid) * 1.0f);
                            line(n2.x, n2.y, n1.x, n1.y);
                            arrow(n2.x, n2.y, n1.x, n1.y, n1.radius);
                        }

                    }

                }
                // REPULSION
                if (cameraIsStopped() && len != 0) {

                    if (!session.animationPaused) {

                        // TODO fix this
                        n1.vx -= (vx / len) * LAYOUT_REPULSION;
                        n1.vy -= (vy / len) * LAYOUT_REPULSION;

                        n2.vx += (vx / len) * LAYOUT_REPULSION;
                        n2.vy += (vy / len) * LAYOUT_REPULSION;
                    }

                }
            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        if (cameraIsStopped() && stepCounter > 1) {
            strokeWeight(1.0f);
        }

        // ITERATE OVER NODES
        // COMPUTE NODE DATA, DRAW NODE..
        for (Node n : nodes) {

            n.x += n.vx;
            n.y += n.vy;
            n.vx = 0.0f;
            n.vy = 0.0f;

            int rgb = (int) ((255.0 / MAX_RADIUS) * n.radius);

            float distance = dist(
                    screenX(n.x, n.y),
                    screenY(n.x, n.y),
                    mouseX,
                    mouseY);

            if (this.session.showNodes) {
                if (distance <= (n.radius) * zoomRatio) {
                    // fill(200);
                    if (mouseClick) {
                        mouseClick = false;
                        System.out.println("clicked on node " + n.uuid + " (selected node: " + session.selectedNodeID + ")");
                         n.selected = !n.selected;
                    }
                }


                /*
                if (n.label.startsWith(currentTextSearch)) {
                    fill(200, 100, 100);
                    strokeWeight(1.8f);
                } else {*/

                    if (n.selected) {
                        fill(200, 100, 100);
                        strokeWeight(1.8f);
                    } else {
                        // fill(200, 200, 200);
                        fill(218, 219, 220);
                        strokeWeight(1.2f);
                    }
                //}

                ellipse(n.x, n.y, n.radius, n.radius);

                /*
                createGradient(node.vizx,
                node.vizy,
                node.vizradius,
                13, 426);
                 */
            }
            if (this.session.showLabels && cameraIsStopped() && stepCounter > 2) {
                fill(120);
                //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
                textSize(n.radius*0.2f);

                if (n.label.startsWith(currentTextSearch)) {
                    fill(60);
                    text(n.label, n.x + n.radius,
                            n.y + (n.radius / 2.50f));

                } else {

                    fill(150);//old: 110
                    text(n.label, n.x + n.radius,
                            n.y + (n.radius / 2.50f));
                }
            }

        }
        if (mouseClick) {
            // the event was not catched.. so we assume we clicked somewhere else
            session.selectedNodeID = "";
            mouseClick = false;
        }


    }

    public float setZoomValue(float value) {

        session.zoom = value;
        return session.zoom;
    }

    public void centerOnNodeById(int id) {
        //return sliderZoomLevel;
    }

    public synchronized boolean clearView()
            throws
            URISyntaxException,
            MalformedURLException,
            IOException,
            XPathExpressionException {
        session.clear();

        return postUpdateView();
    }

    public synchronized boolean updateViewFromURI(String uri)
            throws
            URISyntaxException,
            MalformedURLException,
            IOException,
            XPathExpressionException {
        session.updateFromURI(uri);
        return postUpdateView();
    }
    public synchronized boolean updateViewFromString(String src)
            throws
            URISyntaxException,
            MalformedURLException,
            IOException,
            XPathExpressionException {
        session.updateFromString(src);
        return postUpdateView();
    }
    private synchronized boolean postUpdateView() {
        // prepare the scene depending on graph metrics
        //boolean tmp = session.animationPaused;
        //session.animationPaused = true; // TODO replace by a lock here

        return true;
    }



    // setParameter("key", min, max)
    public synchronized boolean createFilter(String filterName, String model) {

        Filter f = null;
        if (model.equals("RegexMatch")) {
            f = new AttributeFilter();
        } else if (model.equals("ForceVector")) {
            f = new ForceVector();
        }
      
       session.filters.addFilter(filterName, f);
       return true;
    }


    public synchronized boolean filterConfig(String filterName, String key, String value) throws KeyException {
       session.filters.getFilter(filterName).setField(key, value);
       return true;
       // return session.filters.get(filterName).setField(key, value);
    }

     public synchronized boolean filterConfig(String filterName, String key, float value) throws KeyException {
       session.filters.getFilter(filterName).setField(key, value);
       return true;
       // return session.filters.get(filterName).setField(key, value);
    }
    public synchronized boolean filterConfig(String filterName, String key, int value) throws KeyException {
       session.filters.getFilter(filterName).setField(key, value);
       return true;
       // return session.filters.get(filterName).setField(key, value);
    }
    public synchronized boolean filterConfig(String filterName, String key, boolean value) throws KeyException {
       session.filters.getFilter(filterName).setField(key, value);
       return true;
       // return session.filters.get(filterName).setField(key, value);
    }

    public synchronized Object filterConfig(String filterName, String key) throws KeyException {
        return session.filters.getFilter(filterName).getField(key);
    }


    public synchronized boolean toggleLabels() {
        this.session.showLabels = !this.session.showLabels;
        return this.session.showLabels;
    }

    public synchronized boolean showLabels(boolean value) {
        this.session.showLabels = value;
        return this.session.showLabels;
    }

    public synchronized boolean toggleNodes() {
        this.session.showNodes = !this.session.showNodes;
        return this.session.showNodes;
    }

    public synchronized boolean togglePosterOverlay() {
        this.session.showPosterOverlay = !this.session.showPosterOverlay;
        return this.session.showPosterOverlay;
    }

    public synchronized boolean togglePause() {
        this.session.animationPaused = !this.session.animationPaused;
        return this.session.animationPaused;
    }

    public synchronized boolean showNodes(boolean value) {
        this.session.showNodes = value;
        return this.session.showNodes;
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

    public synchronized boolean toggleLinks() {
        this.session.showLinks = !this.session.showLinks;
        return this.session.showLinks;
    }

    public synchronized boolean showLinks(boolean value) {
        this.session.showLinks = value;
        return this.session.showLinks;
    }

    public synchronized void center() {
        vizx = 0;
        vizy = 0;
        //vizx = (float)width/2.0f;
        //vizy = (float)height/2.0f;
        //vizx += (session.metrics.maxX - session.metrics.minX);
        //vizy +=  (session.metrics.maxY - session.metrics.minY);
        zoomRatio = 3.0f;
    }

    public synchronized float setLowerThreshold(float threshold) {
        return session.lowerThreshold = threshold;
    }

    public synchronized float getLowerThreshold() {
        return session.lowerThreshold;
    }

    public synchronized float setUpperThreshold(float threshold) {
        return session.upperThreshold = threshold;
    }

    public synchronized float getUpperThreshold() {
        return session.upperThreshold;
    }

    public void setGenericityRange(int from, int to, int precision) {
        setLowerThreshold(((float) from) / (float) precision);
        setUpperThreshold(((float) to) / (float) precision);
    }

    public String getExplorationMode() {
        return (session.explorationMode == NetworkMode.LOCAL) 
                ? "local" : "global";
    }

    public void switchToLocalExploration() {
        session.switchToLocalExploration();
    }

    public void switchToGlobalExploration() {
        session.switchToGlobalExploration();
    }

    public boolean cameraIsMoving() {
        return !(abs(inerX + inerY + inerZ) == 0.0f);
    }

    public boolean cameraIsStopped() {
        return (abs(inerX + inerY + inerZ) == 0.0f);
    }

    @Override
    public void mousePressed() {

        // hideNodeDetails();
        // oldmouseX = mouseX;
        //oldmouseY = mouseY;
        // mousePress = true;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

        // mouse "brake"
        if (mouseButton == RIGHT) {
            inerZ *= 0.01; // smooth brake
        }
    }

    @Override
    public void mouseClicked() {
        // hideNodeDetails();
        mouseClick = true;
    }

    @Override
    public void mouseDragged() {
        // hideNodeDetails();
        mouseDragging = true;
        float dragSensibility = 3.0f;
        
        // OLD "INERTIAL" DRAG
        
        inerX = ((float) mouseX - oldmouseX) / (zoomRatio * dragSensibility);
        inerY = ((float) mouseY - oldmouseY) / (zoomRatio * dragSensibility);
        vizx += inerX * 2.0f;
        vizy += inerY * 2.0f;
         
        
        // NEW "DIRECT" DRAG
        /*
        if (zoomRatio != 0) {
            vizx += ((float) mouseX - oldmouseX);
            vizy += ((float) mouseY - oldmouseY);
        }*/

        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    @Override
    public void mouseReleased() {
        mouseDragging = false;
        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (session.zoomFrozen) {
            return;
        }

        //System.out.println("new inerZ="+inerZ);
        //inerZ = -e.getWheelRotation() * 2;
        //this.zoomRatio -= (float) e.getWheelRotation() * 0.7f / zoomRatio;
        inerZ = - (float) e.getWheelRotation() /  (1.0f / zoomRatio);
        stepCounter = 10;
        //System.out.println("new inerZ="+inerZ);

    }


    private void arrow(float x1, float y1, float x2, float y2, float radius) {
        pushMatrix();
        translate(x2, y2);
        float a = atan2(x1 - x2, y2 - y1);
        rotate(a);
        line(0, -radius, -2, -2 - radius);
        line(0, -radius, 2, -2 - radius);
        popMatrix();
    }

    private void arrow(PGraphics pg, float x1, float y1, float x2, float y2, float radius) {
        pg.pushMatrix();
        pg.translate(x2, y2);
        float a = atan2(x1 - x2, y2 - y1);
        pg.rotate(a);
        pg.line(0, -radius, -2, -2 - radius);
        pg.line(0, -radius, 2, -2 - radius);
        pg.popMatrix();
    }

    private float logify (float x) {
  if (abs(x) < 0.01f) return 0.0f;
  return (x>0) ?  log100((int)(abs(x)*100.0f)) : -log100((int)(abs(x)*100.0f));
}
private float log100 (int x) {
  return (log(x) / ((float)log(100)));
}


/**
 * Given a Throwable, gets the full stack trace for the
 * Exception or Error as a String.  Returns an empty string
 * if something went wrong (so the caller won't fail with a
 * null pointer exception later).
 * @param t
 * @return
 * @author kolichko Mark Kolich
 */
public static String getStackTraceAsString ( Throwable t ) {

    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    String trace = new String();

    try {
      t.printStackTrace( pw );
      pw.flush();
      sw.flush();
      trace = sw.toString();
    }
    catch ( Exception e ) { }
    finally {
      try {
        sw.close();
        pw.close();
      }
      catch ( Exception e ) { }
    }

    return trace;

}

   
}


