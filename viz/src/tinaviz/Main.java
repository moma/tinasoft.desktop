package tinaviz;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import tinaviz.model.*;
import processing.opengl.*;
import processing.core.*;
import processing.xml.*;
import processing.pdf.*;
import netscape.javascript.*;


public class Main extends PApplet implements MouseWheelListener {

    static int MAXLINKS = 512;
    float zoomRatio = 1.0f;
    PImage nodeIcon;
    PFont font;
    float vizx = 0f;
    float vizy = 0f;
    float oldmouseX = 0f;
    float oldmouseY = 0f;
    XMLElement xml;
    Session session = new Session();
    // this is the "magnification level"
    float MACRO_UPPER = 2.0f;
    float MACRO_LOWER = 60.0f;
    /*
    float MESO_UPPER = 22.0f;
    float MESO_LOWER = 25.0f;

    float MICRO_UPPER = 27.0f;
    float MICRO_LOWER = 30.0f;*/
                // pourcentage de l'ecran pour lequel la présence des bords d'un node
            // déclenche le passage dans le mode macro
     float screenRatioSelectNodeWhenZoomed = 0.30f;
     float screenRatioGoToMesoWhenZoomed = 0.25f;

    private boolean mouseDragging = false;
    private float inerX = 0.0f;
    private float inerY = 0.0f;
    private float inerZ = 0.0f;
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    AtomicBoolean mouseClick = new AtomicBoolean(false);
    private int preSpatialize = 10;
    public static JSObject window = null;
    private int recordingWidth = 100;
    private int recordingHeight = 100;
    private String DEFAULT_FONT = "ArialMT-150.vlw";
    AtomicBoolean screenBufferUpdated = new AtomicBoolean(false);
    AtomicBoolean screenBufferUpdating = new AtomicBoolean(false);
    AtomicBoolean resetSelection = new AtomicBoolean(false);
    private int stepCounter = 0;
    // Semaphore screenBufferLock = new Semaphore();
    private List<tinaviz.Node> nodes = new ArrayList<tinaviz.Node>();

    private void jsNodeSelected(Node n) {
        if (window == null) {
            return; // in debug mode
        }
        window.eval(session.getLevel() + "NodeSelected("
                + screenX(n.x, n.y) + ","
                + screenY(n.x, n.y) + ",\""
                + n.uuid + "\",\"" + n.label + "\", \"" + n.category + "\");");
    }

    private void jsSwitchToMacro() {
        if (window == null) {
            return; // in debug mode
        }
        window.call("switchToMacro", null);
    }

    private void jsSwitchToMeso() {
        if (window == null) {
            return; // in debug mode
        }
        window.call("switchToMeso", null);
    }

    private void jsSwitchToMicro() {
        if (window == null) {
            return; // in debug mode
        }
        window.call("switchToMicro", null);
    }

    private void jsSwitchToUpper() {
        if (session.currentLevel == ViewLevel.MACRO) {
            // nothing to do..
        } else if (session.currentLevel == ViewLevel.MESO) {
            jsSwitchToMacro();
        } else {
            jsSwitchToMeso();
        }
    }

    private void jsSwitchToLower() {
        if (session.currentLevel == ViewLevel.MACRO) {
            jsSwitchToMeso();
        } else if (session.currentLevel == ViewLevel.MESO) {
            jsSwitchToMicro();
        } else {
            // nothing to do
        }
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
            size(w, h, engine);
        } else {
            size(screen.width, screen.height, engine);
        }

        textFont(font);
        smooth();


        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully


        // currentView.showLabels = false;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

        boolean generateRandomLocalGraph = true;
        boolean loadDefaultGlobalGraph = true;

        if (generateRandomLocalGraph) {
            session.toMesoLevel();

            List<Node> tmp = new ArrayList<Node>();
            Node node;
            System.out.println("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 0.0f;
            for (int i = 0; i < 200; i++) {
                radius = random(3.0f, 10.0f);

                node = new Node("" + i, "node " + i, radius, random(width / 2), random(height / 2));
                node.genericity = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "project" : "term";
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                for (int j = 0; j < tmp.size() && i != j; j++) {
                    if (random(1.0f) < 0.009) { // link density : 0.02 = a lot, 0.0002 = a few
                        tmp.get(i).addNeighbour(tmp.get(j));
                    }
                }
            }
            // session.updateFromNodeList(tmp);


            //session.animationPaused = true;
        }

        if (loadDefaultGlobalGraph) {
            session.toMacroLevel();
            session.getGraph().updateFromURI(
                    "file:///home/jbilcke/Checkouts/git/TINA/tinasoft.desktop/tina/chrome/data/graph/examples/map_dopamine_2002_2007_g.gexf");

            /* if(session.getNetwork().updateFromURI("file:///home/jbilcke/Checkouts/git/TINA"
            + "/tinasoft.desktop/tina/chrome/content/applet/data/"
            + "map_dopamine_2002_2007_g.gexf"))*/

            //session.animationPaused = true;
        }
        // fill(255, 184);
        frameRate(30);
        smooth();
        textFont(font, 48);
        center();
        System.out.println("Starting visualization..");
        if (window != null) {
            window.eval("appletInitialized();");
        }
    }

    @Override
    public void draw() {
        if (!this.isEnabled()) {
            return;
        }

        // todo replace by get network
        View net = session.getView();


        List<Node> n = net.getNodes();
        if (n != null) {
            nodes.clear();
            nodes.addAll(n);
            //center(); // uncomment this later
            System.out.println("got new nodes!");
        }

        //session.animationPaused = tmp; // TODO replace by a lock here
        //preSpatialize = 60;

        // TODO put this in another thread
        if (recordingMode != RecordingFormat.NONE) {

            if (recordingMode == RecordingFormat.PDF) {
                pdfDrawer(net, width, height);
            } else if (recordingMode == RecordingFormat.CURRENT_PICTURE) {
                pictureDrawer(net, width, height);
                //save(recordPath);
            } else if (recordingMode == RecordingFormat.BIG_PICTURE) {
                pictureDrawer(net, recordingWidth, recordingHeight);
            }
            recordingMode = RecordingFormat.NONE;

            //} else if (net.prespatialize && preSpatialize-- > 0) {
            //     drawLoading(net);
            //    spatialize(net);
            //} else {

            //}
        } else {
            drawAndSpatializeRealtime(net);
        }
    }

    public void drawLoading(View net) {
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

    public void spatialize(View net) {
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

    public void pdfDrawer(View net, int w, int h) {
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

        genericDrawer(net, pdf, w, h, vizx, vizy);
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

        genericDrawer(net, pg, w, h, vizx, vizy);
        pg.endDraw();
        pg.save(recordPath);
    }

    public void genericDrawer(View net, PGraphics pg, int w, int h, float vizx, float vizy) {


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
                    int rgb = (int) ((255.0f / (net.getGraph().metrics.maxRadius - net.getGraph().metrics.minRadius)) * rpond);
                    // old: 150
                    pg.stroke(rgb);
                    pg.strokeWeight(((n1.radius + n2.radius) * 0.05f));
                    pg.line(n1.x, n1.y, n2.x, n2.y);
                    // arrow(pg, n2.x, n2.y, n1.x, n1.y, n1.radius);

                }

            } // FOR NODE B
        }   // FOr NODE A

        pg.stroke(20, 20, 20);
        pg.fill(218, 219, 220);
        pg.strokeWeight(1.2f);

        for (Node n : nodes) {

            pg.ellipse(n.x, n.y, n.radius, n.radius);

            pg.fill(120);
            //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
            pg.textSize(n.radius * 0.08f);

            pg.fill(150);//old: 110
            pg.text(n.label, n.x + n.radius,
                    n.y + (n.radius / 2.50f));
        }

    }

    public void drawAndSpatializeRealtime(View net) {

        stepCounter++;

        Node node_a, node_b, node;

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
        float LAYOUT_REPULSION = 0.01f;
        float LAYOUT_ATTRACTION = 0.0001f;

        boolean _resetSelection = this.resetSelection.getAndSet(false);
        boolean _mouseClick = this.mouseClick.getAndSet(false);


        background(255);
        stroke(0);
        fill(120);

        if (false) {
            fill(30);
            textSize(40);
            text("TinaSoft", 15f, 50f);

            fill(80);
            textSize(18);
            text("A cool project subtitle", 18f, 70f);
            fill(120);
        }

        //if (!mouseDragging) {
        // todo: make it proportionnal to the zoom level ?
        //  0.01 = sticky, 0.001 smoothie

        // important de les faire réduire à la même vitesse
        inerX = (abs(inerX) <= 0.1) ? 0.0f : inerX * 0.9f;
        inerY = (abs(inerY) <= 0.1) ? 0.0f : inerY * 0.9f;
        inerZ = (abs(inerZ) <= 0.1) ? 0.0f : inerZ * 0.9f;

        vizx += inerX * 2.0f;
        vizy += inerY * 2.0f;

        zoomRatio += inerZ * 0.015f;
        //}

        translate(vizx, vizy);

        if (zoomRatio > MACRO_LOWER) {
            zoomRatio = MACRO_LOWER;
            //jsSwitchToLower();
        }
        if (zoomRatio < MACRO_UPPER) {
            zoomRatio = MACRO_UPPER;
            //jsSwitchToUpper();
        }

        // TODO FIXME
        scale(zoomRatio * (log(zoomRatio)));

        stroke(150, 150, 150);

        strokeWeight(1);

        for (Node n1 : nodes) {
            if (_resetSelection) {
                n1.selected = false;
            }

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
                    //if (!mouseDragging) {
                    if (!net.animationPaused) {
                        n1.vx += (vx * len) * LAYOUT_ATTRACTION;
                        n1.vy += (vy * len) * LAYOUT_ATTRACTION;
                        n2.vx -= (vx * len) * LAYOUT_ATTRACTION;
                        n2.vy -= (vy * len) * LAYOUT_ATTRACTION;
                    }
                    //}
                    // AFFICHAGE LIEN (A CHANGER)

                    if (net.showLinks) {
                        if (stepCounter > 1) {

                            if (n1.selected && n2.selected) {
                                stroke(50);
                            } else if (n1.selected || n2.selected) {
                                stroke(150);
                            } else {
                                stroke(220);
                            }

                            if (net.animationPaused) {
                                strokeWeight(n1.weights.get(n2.uuid) * 1.0f);
                            } else {
                                strokeWeight(1.0f);
                            }

                            if (false) {
                                line(n2.x, n2.y, n1.x, n1.y);
                            } else {
                                
                                noFill();

                                float xa0 = (6 * n1.x + n2.x) / 7, ya0 = (6 * n1.y + n2.y) / 7;
                                float xb0 = (n1.x + 6 * n2.x) / 7, yb0 = (n1.y + 6 * n2.y) / 7;
                                float[] xya1 = rotation(xa0, ya0, n1.x, n1.y, PI / 2);
                                float[] xyb1 = rotation(xb0, yb0, n2.x, n2.y, -PI / 2);
                                float xa1 = (float) xya1[0], ya1 = (float) xya1[1];
                                float xb1 = (float) xyb1[0], yb1 = (float) xyb1[1];
                                bezier(n1.x, n1.y, xa1, ya1, xb1, yb1, n2.x, n2.y);
                            }
                            if (cameraIsStopped()) {
                                // arrow(n2.x, n2.y, n1.x, n1.y, n1.radius);
                            }
                        }
                    }
                }
                // REPULSION
                if (cameraIsStopped() && len != 0) {
                    if (!net.animationPaused) {
                        n1.vx -= (vx / len) * LAYOUT_REPULSION;
                        n1.vy -= (vy / len) * LAYOUT_REPULSION;
                        n2.vx += (vx / len) * LAYOUT_REPULSION;
                        n2.vy += (vy / len) * LAYOUT_REPULSION;
                    }
                }
            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        //if (cameraIsStopped() && stepCounter > 1) {
        strokeWeight(1.0f);
        //}

        for (Node n : nodes) {
            n.x += n.vx;
            n.y += n.vy;
            n.vx = 0.0f;
            n.vy = 0.0f;



            float nodeLeftMargin = screenX(n.x - n.radius, n.y - n.radius);
            float nodeTopMargin = screenY(n.x - n.radius, n.y - n.radius);
            float nodeRightMargin = screenX(n.x + n.radius, n.y + n.radius);
            float nodeBottomMargin = screenY(n.x + n.radius, n.y + n.radius);


            int rgb = (int) ((n.radius / (net.getGraph().metrics.maxRadius - net.getGraph().metrics.minRadius)) * 255.0f);
            float distance = dist(
                    screenX(n.x, n.y),
                    screenY(n.x, n.y),
                    mouseX,
                    mouseY);
            // temporary boolean used locally for the node selection
            boolean hasBeenSelectedAlready = false;
            if (session.currentLevel == ViewLevel.MACRO
                    && nodeLeftMargin < (width * screenRatioSelectNodeWhenZoomed)
                    && nodeTopMargin < (height * screenRatioSelectNodeWhenZoomed)
                    && nodeRightMargin > (width - width * screenRatioSelectNodeWhenZoomed)
                    && nodeBottomMargin > (height - height * screenRatioSelectNodeWhenZoomed)) {
                // System.out.println("In macro view, got '"+n.label+"' in front of our screen!");
                if (!n.selected) {
                    n.selected = true;
                    hasBeenSelectedAlready = true;
                    jsNodeSelected(n);
                }
            } else if (session.currentLevel == ViewLevel.MACRO
                    && nodeLeftMargin < (width * screenRatioGoToMesoWhenZoomed)
                    && nodeTopMargin < (height * screenRatioGoToMesoWhenZoomed)
                    && nodeRightMargin > (width - width * screenRatioGoToMesoWhenZoomed)
                    && nodeBottomMargin > (height - height * screenRatioGoToMesoWhenZoomed)) {
                // System.out.println("In macro view, got '"+n.label+"' in front of our screen!");
                if (!n.selected) {
                    n.selected = true;
                    hasBeenSelectedAlready = true;
                    jsNodeSelected(n);
                }
                jsSwitchToMeso();
            }

            if (net.showNodes) {
                if (_mouseClick) {
                    if (distance <= (n.radius) * zoomRatio) {
                        // fill(200);
                        // mouseClick = false;
                        System.out.println("clicked on node " + n.uuid);
                        n.selected = !n.selected;
                        // only call this once
                        if (n.selected && !hasBeenSelectedAlready) {
                            jsNodeSelected(n);
                        }
                    }
                }

                /*
                if (n.label.startsWith(currentTextSearch)) {
                fill(200, 100, 100);
                strokeWeight(1.8f);
                } else {*/

                strokeWeight(1.0f);
                noStroke();
                if (n.selected) {
                    fill(70, 70, 70);
                    ellipse(n.x, n.y, n.radius + n.radius * 0.3f, n.radius + n.radius * 0.3f);
                } else {
                    fill(180, 180, 180);
                    ellipse(n.x, n.y, n.radius + n.radius * 0.3f, n.radius + n.radius * 0.3f);
                }

                /*
                if (net.animationPaused) {
                strokeWeight(2.0f);
                } else {
                strokeWeight(1.0f);
                }*/
                //stroke(100, 100, 100);
                fill(n.r, n.g, n.b);
                ellipse(n.x, n.y, n.radius, n.radius);

            }
            if (n.selected) {
                fill(70);
            } else {
                fill(150);
            }
            if (net.showLabels && stepCounter > 2) {

                //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
                textSize(n.radius);

                text(n.label, n.x + n.radius,
                        n.y + (n.radius / PI));

            }
        }
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

    public synchronized void center() {
        vizx = 0;
        vizy = 0;
        //vizx = (float)width/2.0f;
        //vizy = (float)height/2.0f;
        //vizx += (session.metrics.maxX - session.metrics.minX);
        //vizy +=  (session.metrics.maxY - session.metrics.minY);
        zoomRatio = 4.0f;
    }

    public void unselect() {
        resetSelection.set(true);
    }

    public boolean cameraIsMoving() {
        return !(abs(inerX + inerY + inerZ) == 0.0f);
    }

    public boolean cameraIsStopped() {
        return (abs(inerX + inerY + inerZ) == 0.0f);
    }

    @Override
    public void mousePressed() {
        // oldmouseX = mouseX;
        //oldmouseY = mouseY;
        // mousePress = true;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

        // mouse "brake"
        if (mouseButton == LEFT) {
            inerZ = 0; // smooth brake
        }
    }

    @Override
    public void mouseClicked() {
        // hideNodeDetails();
        mouseClick.set(true);
    }

    @Override
    public void mouseDragged() {
        // hideNodeDetails();
        if (mouseButton == RIGHT) {
            mouseDragging = true;
            float dragSensibility = 0.1f;
            // OLD "INERTIAL" DRAG
            if (zoomRatio != 0 && (1.0f / zoomRatio) != 0) {
                inerX = ((float) mouseX - oldmouseX) * dragSensibility / (1.0f / zoomRatio);
                inerY = ((float) mouseY - oldmouseY) * dragSensibility / (1.0f / zoomRatio);
            }
            vizx += inerX * 2.0f;
            vizy += inerY * 2.0f;

        }
        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            mouseDragging = false;
        }
        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        // 0.01 = very slow, 1.0f = very fast
        float zoomSensibility = 0.5f;

        if (zoomRatio != 0 && (1.0f / zoomRatio) != 0) {
            inerZ += -(float) e.getWheelRotation() * zoomSensibility / (1.0f / zoomRatio);
        }

        if (e.getWheelRotation() > 0) {
            if (zoomRatio >= MACRO_UPPER) {
                vizx -= (width / 2.0f - mouseX) / zoomRatio * zoomSensibility * 0.01f;
                vizy -= (height / 2.0f - mouseY) / zoomRatio * zoomSensibility * 0.01f;
            }
        } else {
            if (zoomRatio <= MACRO_LOWER) {
                vizx += (width / 2.0f - mouseX) / zoomRatio * zoomSensibility * 0.01f;
                vizy += (height / 2.0f - mouseY) / zoomRatio * zoomSensibility * 0.01f;
            }
        }

        stepCounter = 10;
    }

    private void arrow(float x1, float y1, float x2, float y2, float radius) {
        pushMatrix();
        translate(
                x2, y2);


        float a = atan2(x1 - x2, y2 - y1);
        rotate(
                a);
        line(
                0, -radius, -1, -1 - radius);
        line(
                0, -radius, 1, -1 - radius);
        popMatrix();


    }

    private void arrow(PGraphics pg, float x1, float y1, float x2, float y2, float radius) {
        pg.pushMatrix();
        pg.translate(x2, y2);


        float a = atan2(x1 - x2, y2 - y1);
        pg.rotate(a);
        pg.line(0, -radius, -1, -1 - radius);
        pg.line(0, -radius, 1, -1 - radius);
        pg.popMatrix();


    }

    private float logify(float x) {
        if (abs(x) < 0.01f) {
            return 0.0f;


        }
        return (x > 0) ? log100((int) (abs(x) * 100.0f)) : -log100((int) (abs(x) * 100.0f));


    }

    private float log100(int x) {
        return (log(x) / ((float) log(100)));

    }

    public static float[] rotation(float x, float y, float centerX, float centerY, float theta) {

        float[] rc = new float[2];
        rc[0] = (float) (centerX + (x - centerX) * cos(theta) - (y - centerY) * sin(theta));
        rc[1] = (float) (centerY + (x - centerX) * sin(theta) + (y - centerY) * cos(theta));
        return rc;

    }
}
