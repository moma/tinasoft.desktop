package tinaviz;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.FilePermission;

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

    public PVector ref = new PVector();
    public PVector drawerTranslation = new PVector();
    public PVector drawerLastPosition = new PVector();
    static int MAXLINKS = 512;
    float zoomRatio = 1.0f;
    PImage nodeIcon;
    PFont font;
    /*lastMove.set(trans);
    float oldmouseX = 0f;
    float oldmouseY = 0f;*/
    XMLElement xml;
    Session session = new Session();
    // this is the "magnification level"

    /*
    float MESO_UPPER = 22.0f;
    float MESO_LOWER = 25.0f;

    float MICRO_UPPER = 27.0f;
    float MICRO_LOWER = 30.0f;*/
    // pourcentage de l'ecran pour lequel la présence des bords d'un node
    // déclenche le passage dans le mode macro
    float screenRatioSelectNodeWhenZoomed = 0.40f;
    float screenRatioGoToMesoWhenZoomed = 0.31f;
    AtomicBoolean mouseLeftDragging = new AtomicBoolean(false);
    AtomicBoolean mouseRightDragging = new AtomicBoolean(false);
    AtomicBoolean zooming = new AtomicBoolean(false);
    AtomicBoolean zoomIn = new AtomicBoolean(false);
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    AtomicBoolean mouseClickLeft = new AtomicBoolean(false);
    AtomicBoolean mouseClickRight = new AtomicBoolean(false);
    public static JSObject window = null;
    private int recordingWidth = 100;
    private int recordingHeight = 100;
    private String DEFAULT_FONT = "ArialMT-150.vlw";
    private AtomicBoolean screenBufferUpdated = new AtomicBoolean(false);
    private AtomicBoolean screenBufferUpdating = new AtomicBoolean(false);
    private AtomicBoolean resetSelection = new AtomicBoolean(false);
    // Semaphore screenBufferLock = new Semaphore();
    private List<tinaviz.Node> nodes = new ArrayList<tinaviz.Node>();
    float selectedX = 0.0f;
    float selectedY = 0.0f;

    private void jsNodeSelected(Node n) {

        if (n != null) {
            selectedX = n.x;
            selectedY = n.y;
        }

        if (window == null) {
            return; // in debug mode
        }
        if (n == null) {
            window.eval("parent.tinaviz.nodeSelected('" + session.getLevel() + "',0,0,null,null,null);");
        } else {
            window.eval("parent.tinaviz.nodeSelected('" + session.getLevel() + "',"
                    + screenX(n.x, n.y) + ","
                    + screenY(n.x, n.y) + ",\""
                    + n.uuid + "\",\"" + n.label + "\", \"" + n.category + "\");");
        }
    }

    private void jsSwitchToMacro() {
        session.toMacroLevel();
        if (window != null) {
            window.eval("parent.tinaviz.switchedTo('macro');");
        }

    }

    private void jsSwitchToMeso() {
        session.toMesoLevel();
        if (window != null) {
            window.eval("parent.tinaviz.switchedTo('meso');");
        }
    }

    private void jsSwitchToMicro() {
        session.toMicroLevel();
        if (window != null) {
            window.eval("parent.tinaviz.switchedTo('micro');");
        }
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

            } else if (getParameter("engine").equals("optimized")) {
                engine = JAVA2D;

            }
            window = JSObject.getWindow(this);
            int w = 200;
            int h = 200;
            /*Object o = window.call("parent.tinaviz.getWidth", null);
            if (o != null) {
            if (o instanceof Double) {
            w = ((Double) o).intValue();
            }
            }
            o = window.call("parent.tinaviz.getHeight", null);
            if (o != null) {
            if (o instanceof Double) {
            h = ((Double) o).intValue();
            }
            }*/

            size(w, h, engine);
        } else {
            size(screen.width, screen.height, engine);
        }

        if (engine == OPENGL) {
            smooth();
            frameRate(30);
            textFont(font, 96);
        } else {
            smooth();
            frameRate(20);
            textFont(font, 24);
        }

        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully


        // currentView.showLabels = false;


        /*
        SecurityManager appsm = System.getSecurityManager();
        if (appsm != null) {
        appsm.checkPermission(new FilePermission("*","read"));
        } else {

        }
         * *
         */

        boolean generateRandomLocalGraph = false;
        boolean loadDefaultLocalGraph = false;
        boolean loadDefaultGlobalGraph = false;
        boolean generateRandomGlobalGraph = false;

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

            List<Node> tmp = new ArrayList<Node>();
            Node node;
            System.out.println("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 15.0f;

            Node root = new Node("root", "root node", radius, 0, 0);
            root.genericity = random(1.0f);
            root.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
            root.label = root.category + " " + root.label;
            root.fixed = true;

            for (int i = 0; i < 50; i++) {
                radius = random(3.0f, 5.0f);
                node = new Node("" + i, "node " + i, radius, random(-20), random(20));
                node.genericity = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
                node.label = node.category + " " + node.label;
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                root.addNeighbour(tmp.get(i));

            }
            tmp.add(root);
            Console.log("Generated " + tmp.size() + " nodes!");

            session.getMeso().getGraph().updateFromNodeList(tmp);



            //session.animationPaused = true;
        }

        if (loadDefaultGlobalGraph) {
            session.getMacro().getGraph().updateFromURI(
                    //  "file:///home/jbilcke/Checkouts/git/TINA/tinasoft.desktop/tina/chrome/data/graph/examples/map_dopamine_2002_2007_g.gexf"
                    //"file://default.gexf"
                    "file:///home/jbilcke/Checkouts/git/TINA/tinasoft.desktop/tina/chrome/data/graph/examples/tinaapptests-exportGraph.gexf" /* if(session.getNetwork().updateFromURI("file:///home/jbilcke/Checkouts/git/TINA"
                    + "/tinasoft.desktop/tina/chrome/content/applet/data/"
                    + "map_dopamine_2002_2007_g.gexf"))*/);
            //session.animationPaused = true;
        } else if (generateRandomGlobalGraph) {

            List<Node> tmp = new ArrayList<Node>();
            Node node;
            Console.log("Generating random graph..");
            float rx = random(width);
            float ry = random(height);
            float radius = 0.0f;
            for (int i = 0; i < 200; i++) {
                radius = random(3.0f, 10.0f);

                node = new Node("" + i, "node " + i, radius, random(width / 2), random(height / 2));
                node.genericity = random(1.0f);
                node.category = (random(1.0f) > 0.5f) ? "Document" : "NGram";
                node.label = node.category + " " + node.label;
                tmp.add(node);
            }

            for (int i = 0; i < tmp.size(); i++) {
                for (int j = 0; j < tmp.size() && i != j; j++) {
                    if (random(1.0f) < 0.009) { // link density : 0.02 = a lot, 0.0002 = a few
                        tmp.get(i).addNeighbour(tmp.get(j));
                    }
                }
            }
            session.getMacro().getGraph().updateFromNodeList(tmp);


            //session.animationPaused = true;
        }
        //cenNGramesoView();
        //session.toMesoLevel();

        // in the case the reset method take the graph radius in account to zoom (but its still not the case)
        session.meso.resetCamera(width, height);
        session.macro.resetCamera(width, height);

        session.toMacroLevel();
        // session.toMesoLevel();

        // DEBUG MODE
        session.macro.prespatializeSteps = 0;

        // fill(255, 184);

        Console.log("Starting visualization..");
        if (window != null) {
            window.eval("parent.tinaviz.init();");
        }

    }

    @Override
    public void draw() {
        // todo replace by get network
        View v = session.getView();


        if (!this.isEnabled()) {
            if (v.prespatializeSteps-- > 0) {

                spatialize(v);

            }
            return;
        }


        List<Node> n = v.popNodes();
        if (n != null) {
            System.out.println("clearing nodes..");
            nodes.clear();
            nodes.addAll(n);
            System.out.println("reset camera(" + width + "," + height + ")");
            // v.resetCamera(width, height);
            //center(); // uncomment this later
            System.out.println("got new nodes!");
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
            spatialize(v);

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

    public void spatialize(View v) {
        float len = 1f;
        float vx = 0f;
        float vy = 0f;
        float repulsion = v.repulsion;
        float attraction = v.attraction;

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                len = sqrt(sq(vx) + sq(vy)) + 0.0000001f;

                if (n1.neighbours.contains(n2)) {
                    n1.vx += (vx * len) * attraction;
                    n1.vy += (vy * len) * attraction;
                    n2.vx -= (vx * len) * attraction;
                    n2.vy -= (vy * len) * attraction;
                }
                // TODO fix this
                n1.vx -= (vx / len) * repulsion;
                n1.vy -= (vy / len) * repulsion;
                n2.vx += (vx / len) * repulsion;
                n2.vy += (vy / len) * repulsion;

            } // FOR NODE B
        }   // FOr NODE A

        for (Node n : nodes) {
            n.vx = constrain(n.vx, -200, 200);
            n.vy = constrain(n.vy, -200, 200);
            n.x += n.vx * 0.5f;
            n.y += n.vy * 0.5f;
            n.vx = 0.0f;
            n.vy = 0.0f;
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
    }

    public void drawAndSpatializeRealtime(View v) {
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        boolean _resetSelection = this.resetSelection.getAndSet(false);
        boolean _mouseLeftClick = this.mouseClickLeft.getAndSet(false);
        boolean _mouseLeftDrag = this.mouseLeftDragging.get();
        boolean _mouseRightDrag = this.mouseRightDragging.get();

        background(255);
        stroke(150, 150, 150);
        strokeWeight(1);

        if (zooming.getAndSet(false)) {
            if (zoomIn.get()) {
                v.sceneScale *= 2.0;
            } else {
                v.sceneScale *= 0.5;
            }
            System.out.println("Zoom: " + v.sceneScale);
        }

        switch (session.currentLevel) {
            case MACRO:
                break;
            case MESO:
                if (v.sceneScale > v.ZOOM_FLOOR) {
                    v.sceneScale = v.ZOOM_FLOOR;
                    System.out.println("switch in to micro");
                }
                if (v.sceneScale < v.ZOOM_CEIL) {
                    System.out.println("switch out to macro");
                    session.getMacro().sceneScale = session.getMacro().ZOOM_FLOOR - session.getMacro().ZOOM_FLOOR * 0.5f;
                    jsSwitchToMacro();
                }
                break;
        }



        v.inerX = (abs(v.inerX) <= 0.14) ? 0.0f : v.inerX * 0.89f;
        v.inerY = (abs(v.inerY) <= 0.14) ? 0.0f : v.inerY * 0.89f;
        v.inerZ = (abs(v.inerZ) <= 0.14) ? 0.0f : v.inerZ * 0.89f;

        // add some physics (will have the same effect than mouse/keyboard actions)
        v.translation.add(v.inerX * 2.0f, v.inerY * 2.0f, 0);
        v.sceneScale += v.inerZ * 0.015f;

        // user zoom

        PVector center = new PVector(width / 2f, height / 2f);
        // center.set(mouseX, mouseY, 0);

        PVector scaledCenter = PVector.mult(center, v.sceneScale);
        PVector scaledTrans = PVector.sub(center, scaledCenter);
        translate(scaledTrans.x, scaledTrans.y);

        // finally, push to the matrix
        scale(v.sceneScale);
        translate(v.translation.x, v.translation.y);

        for (Node n1 : nodes) {
            if (_resetSelection) {
                n1.selected = false;
            }
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                // todo: what happen when vx or vy are 0 ?
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                distance = sqrt(sq(vx) + sq(vy)) + 0.0000001f;

                //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                // plutot que mettre une distance minimale,
                // mettre une force de repulsion, par exemple
                // radius * (1 / distance)   // ou distance au carré



                if (n1.neighbours.contains(n2)) {

                    if (!v.animationPaused) {

                        // take node ponderation into account
                        if (n1.weights.containsKey(n2.uuid)) {
                            distance *= (n1.weights.get(n2.uuid));
                        }

                        n1.vx += (vx * distance) * attraction;
                        n1.vy += (vy * distance) * attraction;
                        n2.vx -= (vx * distance) * attraction;
                        n2.vy -= (vy * distance) * attraction;

                    }


                    if (v.showLinks || n1.selected) {
                        boolean doubleLink = false;

                        if (n2.neighbours.contains(n1)) {
                            doubleLink = true;
                        }
                        if (!doubleLink | n1.uuid.compareTo(n2.uuid) <= 0) {

                            // greyscale!
                            if (false) {
                                if (doubleLink) {
                                    if (n1.selected && n2.selected) {
                                        stroke(50);
                                    } else if (n1.selected || n2.selected) {
                                        stroke(130);
                                    } else {
                                        stroke(200);
                                    }
                                } else {
                                    if (n1.selected && n2.selected) {
                                        stroke(70);
                                    } else if (n1.selected || n2.selected) {
                                        stroke(150);
                                    } else {
                                        stroke(240);
                                    }
                                }
                            } else {
                                if (doubleLink) {
                                    if (n1.selected && n2.selected) {
                                        stroke(50);
                                    } else if (n1.selected || n2.selected) {
                                        stroke(130);
                                    } else {
                                        stroke((n1.r + n2.r) / 2, (n1.g + n2.g) / 2, (n1.b + n2.b) / 2);
                                    }
                                } else {
                                    if (n1.selected && n2.selected) {
                                        stroke(70);
                                    } else if (n1.selected || n2.selected) {
                                        stroke(150);
                                    } else {
                                        stroke((n1.r + n2.r) / 2, (n1.g + n2.g) / 2, (n1.b + n2.b) / 2);
                                    }
                                }
                            }


                            if (v.highDefinition && !_mouseRightDrag && n1.weights != null && n2.uuid != null) {
                                strokeWeight(n1.weights.get(n2.uuid) * 1.0f);
                            } else {
                                strokeWeight(1);
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
                            if (!v.cameraIsMoving()) {
                                // arrow(n2.x, n2.y, n1.x, n1.y, n1.radius);
                            }
                        }

                    }


                    // NEIGHBOUR REPULSION
                    //if (v.spatializeWhenMoving | !v.cameraIsMoving() && len != 0) {
                    if (!v.animationPaused) {

                        /*
                        n1.vx -= n1.radius * (1.0f / distance); //
                        n1.vy -= n1.radius * (1.0f / distance); //
                        n2.vx += n1.radius * (1.0f / distance); //
                        n2.vy += n1.radius * (1.0f / distance); // 0.01f
                         */
                       
                        n1.vx -= (vx / distance) * repulsion;
                        n1.vy -= (vy / distance) * repulsion;
                        n2.vx += (vx / distance) * repulsion;
                        n2.vy += (vy / distance) * repulsion;

                    }

                } else {

                    // STANDARD REPULSION
                    //if (v.spatializeWhenMoving | !v.cameraIsMoving() && len != 0) {
                    if (!v.animationPaused) {


                        n1.vx -= (vx / distance) * repulsion;
                        n1.vy -= (vy / distance) * repulsion;
                        n2.vx += (vx / distance) * repulsion;
                        n2.vy += (vy / distance) * repulsion;

                    }
                }

                //}
            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        //if (cameraIsStopped() && stepCounter > 1) {
        strokeWeight(1.0f);
        //}

        for (Node n : nodes) {

            if (!n.fixed) {

                // important, we limit the velocity!
                n.vx = constrain(n.vx, -10, 10);
                n.vy = constrain(n.vy, -10, 10);

                // update the coordinate
                n.x = constrain(n.x + n.vx * 0.5f, -30000, +30000);
                n.y = constrain(n.y + n.vy * 0.5f, -30000, +30000);

            }

            n.vx = 0.0f;
            n.vy = 0.0f;

            /*************************************************************************
             *  SCREEN-BASED SELECTION, WHEN THE NODE IS IN THE CENTER OF THE SCREEN *
             *************************************************************************/
            float nodeLeftMargin = screenX(n.x - n.radius, n.y - n.radius);
            float nodeTopMargin = screenY(n.x - n.radius, n.y - n.radius);
            float nodeRightMargin = screenX(n.x + n.radius, n.y + n.radius);
            float nodeBottomMargin = screenY(n.x + n.radius, n.y + n.radius);

            boolean massSelectionHasBegin = false;
            if (session.currentLevel == ViewLevel.MACRO
                    && nodeLeftMargin < (width * screenRatioSelectNodeWhenZoomed)
                    && nodeTopMargin < (height * screenRatioSelectNodeWhenZoomed)
                    && nodeRightMargin > (width - width * screenRatioSelectNodeWhenZoomed)
                    && nodeBottomMargin > (height - height * screenRatioSelectNodeWhenZoomed)) {
                // System.out.println("In macro view, got '"+n.label+"' in front of our screen!");
                if (!n.selected) {
                    session.selectNode(n);
                    massSelectionHasBegin = true;
                    jsNodeSelected(n);
                }
            } else if (session.currentLevel == ViewLevel.MACRO
                    && nodeLeftMargin < (width * screenRatioGoToMesoWhenZoomed)
                    && nodeTopMargin < (height * screenRatioGoToMesoWhenZoomed)
                    && nodeRightMargin > (width - width * screenRatioGoToMesoWhenZoomed)
                    && nodeBottomMargin > (height - height * screenRatioGoToMesoWhenZoomed)) {
                // System.out.println("In macro view, got '"+n.label+"' in front of our screen!");
                if (!n.selected) {
                    session.selectNode(n);
                    massSelectionHasBegin = true;
                    jsNodeSelected(n);
                }
                System.out.println("SWITCH TO MESO WITH THE BIG ZOOM METHOD");
                session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL + session.getMeso().ZOOM_CEIL * 0.5f;
                jsSwitchToMeso();
            }


            /*******************************************************************************
             *  MOUSE-BASED SELECTION, WHEN THE MOUSE CLICK OR DOUBLE-CLICK, LEFT OR RIGHT *
             *******************************************************************************/
            if (screenX(n.x - n.radius, n.y - n.radius) < mouseX && mouseX < screenX(n.x + n.radius, n.y + n.radius)
                    && screenY(n.x - n.radius, n.y - n.radius) < mouseY && mouseY < screenY(n.x + n.radius, n.y + n.radius)) {
                //if (distance <= n.radius * zoomRatio) {
                // fill(200);
                // mouseClick = false;
                if (_mouseLeftClick) {
                    if (mouseEvent != null && mouseEvent.getClickCount() == 2) {
                        mouseEvent.consume();
                        System.out.println("double-clicked on node " + n.uuid);

                        if (!n.selected) {
                            session.selectNode(n);
                            if (!massSelectionHasBegin) {
                                massSelectionHasBegin = true;
                                jsNodeSelected(n);
                            }
                        }

                        if (session.currentLevel == ViewLevel.MACRO) {
                            System.out.println("SWITCH TO MESO WITH THE DOUBLE CLICK METHOD");

                            session.getMeso().sceneScale = session.getMeso().ZOOM_CEIL + session.getMeso().ZOOM_CEIL * 0.5f;
                            jsSwitchToMeso();
                        } else if (session.currentLevel == ViewLevel.MESO) {
                            System.out.println("SWITCH TO MICRO WITH THE DOUBLE CLICK METHOD");
                            //session.getMicro().sceneScale = session.getMicro().ZOOM_CEIL + session.getMicro().ZOOM_CEIL * 0.5f;
                            //jsSwitchToMicro();
                        }
                    } else {

                        System.out.println("clicked on node " + n.uuid);

                        // deselect the node
                        if (n.selected) {
                            session.unselectNode(n);
                            jsNodeSelected(null);
                        } else {
                            session.selectNode(n);
                            if (!massSelectionHasBegin) {
                                massSelectionHasBegin = true;
                                jsNodeSelected(n);
                            }
                        }
                    }


                } else if (_mouseLeftDrag) {
                    if (screenX(n.x - n.radius * 1.5f, n.y - n.radius * 1.5f) < mouseX && mouseX < screenX(n.x + n.radius * 1.5f, n.y + n.radius * 1.5f)
                            && screenY(n.x - n.radius * 1.5f, n.y - n.radius * 1.5f) < mouseY && mouseY < screenY(n.x + n.radius * 1.5f, n.y + n.radius * 1.5f)) {
                        System.out.println("dragged left mouse over node " + n.uuid);

                        // old code to restore "selection only"
                        session.selectNode(n);
                        if (!massSelectionHasBegin) {
                            massSelectionHasBegin = true;
                            jsNodeSelected(n);
                        }


                    }
                } else {
                    n.highlighted = true;
                }
            } else {
                n.highlighted = false;
            }



            /****************************
             *  PROCESSING DRAWING CODE *
             ****************************/
            if (v.showNodes) {
                strokeWeight(1.0f);
                noStroke();

                boolean drawDisk = false;
                if (n.category.equals("NGram")) {
                    drawDisk = true;
                } else if (n.category.equals("Document")) {
                    drawDisk = false;
                }

                if (n.selected) {
                    fill(40, 40, 40);
                    if (drawDisk) {
                        ellipse(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                    } else {
                        rectMode(CENTER);
                        rect(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                        rectMode(CORNER);
                    }
                    fill(constrain(n.r - 10, 0, 255), constrain(n.g - 10, 0, 255), constrain(n.b - 10, 0, 255));
                } else if (n.highlighted) {
                    fill(110, 110, 110);
                    if (drawDisk) {
                        ellipse(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                    } else {
                        rectMode(CENTER);
                        rect(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                        rectMode(CORNER);
                    }
                    fill(constrain(n.r - 10, 0, 255), constrain(n.g - 10, 0, 255), constrain(n.b - 10, 0, 255));
                } else {
                    fill(180, 180, 180);
                    if (drawDisk) {
                        ellipse(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                    } else {
                        rectMode(CENTER);
                        rect(n.x, n.y, n.radius + n.radius * 0.4f, n.radius + n.radius * 0.4f);
                        rectMode(CORNER);
                    }
                    fill(constrain(n.r + 80, 0, 255), constrain(n.g + 80, 0, 255), constrain(n.b + 80, 0, 255));
                }

                /*
                if (net.animationPaused) {
                strokeWeight(2.0f);
                } else {
                strokeWeight(1.0f);
                }*/
                //stroke(100, 100, 100);
                if (drawDisk) {
                    ellipse(n.x, n.y, n.radius, n.radius);
                } else {
                    rectMode(CENTER);
                    rect(n.x, n.y, n.radius, n.radius);
                    rectMode(CORNER);
                }
            }
            if (n.selected) {
                fill(20);
            } else if (n.highlighted) {
                fill(60);
            } else {
                fill(100);
            }
            if (v.showLabels) {
                //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
                textSize(n.radius);
                text(n.label, n.x + n.radius, n.y + (n.radius / PI));
                //textSize(n.radius / v.sceneScale);
                //text(n.label, v.translation.x + n.x + n.radius, v.translation.y + n.y + ((n.radius/v.sceneScale) / PI));
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

    public View getView(String v) {
        return session.getView(v);
    }

    public void resetSelection() {
        resetSelection.set(true);
    }

    @Override
    public void mousePressed() {
        session.getView().storeMousePosition(mouseX, mouseY);
    }

    @Override
    public void mouseClicked() {
        if (mouseButton == LEFT) {
            mouseClickLeft.set(true);

        } else if (mouseButton == RIGHT) {
            mouseClickRight.set(true);
        }
    }

    @Override
    public void mouseDragged() {
        View v = session.getView();
        if (mouseButton == RIGHT) {
            mouseRightDragging.set(true);
            session.getView().updateTranslationFrom(mouseX, mouseY);
        } else if (mouseButton == LEFT) {
            mouseLeftDragging.set(true);
        }
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            mouseRightDragging.set(false);
            session.getView().updateTranslationFrom(mouseX, mouseY);
        } else if (mouseButton == LEFT) {
            mouseLeftDragging.set(false);
        }
        session.getView().memorizeLastPosition();
    }

    public void updateDrawerTranslationFromCurrentMouse() {
        session.getView().updateTranslationFrom(mouseX, mouseY);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() != 0) {
            zooming.set(true);
            zoomIn.set(e.getWheelRotation() < 0);
        }
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
        } else if (key == 'm') {
            zooming.set(true);
            zoomIn.set(false);
        } else if (key == 'e') {
            if (window != null) {
                window.eval("parent.tinaviz.toggleEdges('" + v.getName() + "');");
            } else {
                v.showLinks = !v.showLinks;
            }
            System.out.println("show links is now " + v.showLinks);
        } else if (key == 't') {
            if (window != null) {
                window.eval("parent.tinaviz.toggleLabels('" + v.getName() + "');");
            } else {
                v.showLabels = !v.showLabels;
            }
        } else if (key == 'n') {
            if (window != null) {
                window.eval("parent.tinaviz.toggleNodes('" + v.getName() + "');");
            } else {
                v.showNodes = !v.showNodes;
            }
            System.out.println("show nodes is now " + v.showNodes);
        } else if (key == 'a') {
            if (window != null) {
                window.eval("parent.tinaviz.togglePause('" + v.getName() + "');");
            } else {
                v.animationPaused = !v.animationPaused;
            }
            System.out.println("Animation paused is now " + v.animationPaused);
        } else if (key == 'h') {
            v.highDefinition = !v.highDefinition;
            System.out.println("HD mode is now " + v.highDefinition);
        } else if (key == 'o') {
            if ((v.attraction + 0.00001) < 0.0003) {
                v.attraction += 0.00001f;
                System.out.println("\nattraction: " + session.getView().attraction);

            }
        } else if (key == 'l') {
            if ((v.attraction - 0.00001f) > 1.5e-5) {
                v.attraction -= 0.00001f;
                System.out.println("\nattraction: " + session.getView().attraction);
            }
        }

    }

    public void clear() {
        getSession().clear();
    }
    public void clear(String view) {
        getSession().getView(view).clear();
    }
    public void resetCameras() {
        getSession().resetCamera(width, height);
    }

    public void resetCamera(String view) {
        getSession().getView(view).resetCamera(width, height);
    }
    public void select(String id) {
        getSession().selectNodeById(id);
    }

    public void unselect() {
        getSession().unselectAll();
    }

    private void arrow(float x1, float y1, float x2, float y2, float radius) {
        pushMatrix();
        translate(x2, y2);
        rotate(atan2(x1 - x2, y2 - y1));
        line(0, -radius, -1, -1 - radius);
        line(0, -radius, 1, -1 - radius);
        popMatrix();
    }

    private void arrow(PGraphics pg, float x1, float y1, float x2, float y2, float radius) {
        pg.pushMatrix();
        pg.translate(x2, y2);
        pg.rotate(atan2(x1 - x2, y2 - y1));
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
