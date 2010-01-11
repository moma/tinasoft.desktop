package tinaviz;

import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import tinaviz.model.*;
import processing.opengl.*;
import processing.core.*;
import processing.xml.*;
import processing.pdf.*;

public class Main extends PApplet implements MouseWheelListener {

    static int MAXLINKS = 512;
    float zoomRatio = 1.0f;
    List<tinaviz.Node> nodes = new ArrayList<tinaviz.Node>();
    PImage nodeIcon;
    PFont albatar;
    float vizx = 0f;
    float vizy = 0f;
    float oldmouseX = 0f;
    float oldmouseY = 0f;
    float MAX_RADIUS = 0f;
    XMLElement xml;
    View currentView = new View();
    private boolean locked = false;
    float MAX_ZOOM = 5f;
    float MIN_ZOOM = 0.02f;
    private boolean mouseDragging = false;
    private float inerX = 0.0f;
    private float inerY = 0.0f;
    private float inerZ = 0.0f;
    private float fricX = 1.0f;
    private float fricY = 1.0f;
    private RecordingFormat recordingMode = RecordingFormat.NONE;
    private String recordPath = "graph.pdf";
    private boolean mouseClick = false;
    private int preloading = 30;
    private float preloadingAlpha = 10;

    public enum RecordingFormat {

        NONE, JPG, PNG, PDF
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

        size(850, 550, OPENGL);
        fill(255, 184);
        frameRate(60);
        // smooth();
        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully
        System.out.println("loading font..");
        albatar = loadFont("AlBattar-48.vlw");
        System.out.println("loading empty data..");
        currentView = new View();

        //currentView.showLabels = false;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

        vizx = (width / 2.0f);
        vizy = (height / 2.0f);

        Node node;
        System.out.println("Generating random graph..");
        float rx = random(width);
        float ry = random(height);
        float radius = 0.0f;
        for (int i = 0; i < 200; i++) {
            radius = random(10.0f, 20.0f);
            if (radius > MAX_RADIUS) {
                MAX_RADIUS = radius;
            }
            node = new Node("" + i, "node " + i, radius, random(width / 2), random(height / 2));
            nodes.add(node);
        }

        Node a;
        Node b;
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size() && i != j; j++) {
                if (random(1.0f) < 0.01) { // link density : 0.02 = a lot, 0.0002 = a few
                    nodes.get(i).addNeighbour(nodes.get(j));
                }
            }
        }
    }

    @Override
    public void draw() {
        if (locked) {
            return;
        }

        if (recordingMode != RecordingFormat.NONE) {
            // Note that #### will be replaced with the frame number. Fancy!
            textMode(SHAPE);

            if (recordingMode == RecordingFormat.PDF) {
                beginRecord(PDF, recordPath);
                picturedraw();
                endRecord();
            } else {
                picturedraw();
                saveFrame(recordPath);
            }

            recordingMode = RecordingFormat.NONE;
            textMode(MODEL);
            // } else if (preloading-- > 0) {
            //    spatialize();
        } else {
            subdraw();
        }
    }

    public void spatialize() {

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
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

    public void picturedraw() {

        background(255);
        stroke(0);
        fill(120);
        strokeWeight(1.0f);
        //zoomRatio = 10.0 / (double) sliderZoomLevel;
        textFont(albatar, 48);

        Node node_a, node_b, node;

        fill(30);
        textSize(40);
        text("TinaSoft", 15f, 50f);

        fill(80);
        textSize(18);
        text("A cool project subtitle", 18f, 70f);
        fill(120);


        translate(vizx, vizy);
        scale(zoomRatio);

        stroke(150, 150, 150);
        strokeWeight(3.0f);

        for (int i = 0; i < nodes.size(); i++) {
            node_a = nodes.get(i);

            for (int j = 0; j < nodes.size() && j != i; j++) {
                node_b = nodes.get(j);

                if (node_a.neighbours.contains(node_b)) {

                    float rpond = ((node_a.radius + node_a.radius) * 0.5f);
                    int rgb = (int) ((255.0f / MAX_RADIUS) * rpond);

                    if (this.currentView.showLinks) {
                        strokeWeight(((node_a.radius + node_a.radius) * 0.05f));
                        line(node_a.x, node_a.y, node_b.x, node_b.y);
                        arrow(node_b.x, node_b.y, node_a.x, node_a.y, node_a.radius);
                    }

                }

            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        strokeWeight(1.2f);


        // ITERATE OVER NODES
        // COMPUTE NODE DATA, DRAW NODE..
        for (Node n : nodes) {

            int rgb = (int) ((255.0 / MAX_RADIUS) * n.radius);

            fill(218, 219, 220);
            strokeWeight(1.2f);
            ellipse(n.x, n.y, n.radius, n.radius);


            if (this.currentView.showLabels) {
                fill(120);
                textSize(n.radius);
                text(n.label, n.x + n.radius,
                        n.y + (n.radius / 2.50f));
            }
        }
    }

    public void subdraw() {

        background(255);
        stroke(0);
        fill(120);
        strokeWeight(1.0f);
        //zoomRatio = 10.0 / (double) sliderZoomLevel;
        textFont(albatar, 48);

        Node node_a, node_b, node;

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
        float LAYOUT_REPULSION = 0.01f;
        float LAYOUT_ATTRACTION = 0.0001f;

        if (currentView.showPosterOverlay) {
            fill(30);
            textSize(40);
            text("TinaSoft", 15f, 50f);

            fill(80);
            textSize(18);
            text("A cool project subtitle", 18f, 70f);
            fill(120);
        }

        if (!mouseDragging) {


            //  0.01 = sticky, 0.001 smoothie
            inerX = (abs(inerX) <= 0.006) ? 0.0f : inerX * 0.9f;
            inerY = (abs(inerY) <= 0.006) ? 0.0f : inerY * 0.9f;
            inerZ = (abs(inerZ) <= 0.006) ? 0.0f : inerZ * 0.96f;
            vizx += inerX * 2.0f;
            vizy += inerY * 2.0f;
            zoomRatio += inerZ * 0.015f;
        }

        translate(vizx, vizy);

        if (zoomRatio > 5f) {
            zoomRatio = 5f;
            inerZ = 0.0f;
        }
        if (zoomRatio < 0.05f) {
            zoomRatio = 0.05f;
            inerZ = 0.0f;
        }
        scale(zoomRatio);

        stroke(150, 150, 150);

        if (!mouseDragging) {
            strokeWeight(3.0f);
        }

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                if (!mouseDragging) {
                    vx = n2.x - n1.x;
                    vy = n2.y - n1.y;
                    len = sqrt(sq(vx) + sq(vy));
                }

                if (n1.neighbours.contains(n2)) {

                    // ATTRACTION
                    if (!mouseDragging) {
                        n1.vx += (vx * len) * LAYOUT_ATTRACTION;
                        n1.vy += (vy * len) * LAYOUT_ATTRACTION;
                        n2.vx -= (vx * len) * LAYOUT_ATTRACTION;
                        n2.vy -= (vy * len) * LAYOUT_ATTRACTION;
                    }
                    // AFFICHAGE LIEN (A CHANGER)

                    float rpond = ((n1.radius * 2) * 0.5f);
                    int rgb = (int) ((255.0f / MAX_RADIUS) * rpond);

                    if (this.currentView.showLinks) {
                        // old: 150
                        stroke(rgb);
                        if (!mouseDragging) {
                            strokeWeight(((n1.radius + n1.radius) * 0.05f));
                        }
                        line(n1.x, n1.y, n2.x, n2.y);
                        arrow(n2.x, n2.y, n1.x, n1.y, n1.radius);
                    }

                }
                // REPULSION
                if (!mouseDragging && len != 0) {

                    // TODO fix this
                    n1.vx -= (vx / len) * LAYOUT_REPULSION;
                    n1.vy -= (vy / len) * LAYOUT_REPULSION;

                    n2.vx +=  (vx / len) * LAYOUT_REPULSION;
                    n2.vy += (vy / len) * LAYOUT_REPULSION;

                }
            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        if (!mouseDragging) {
            strokeWeight(1.2f);
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

            if (this.currentView.showNodes) {
                if (distance <= (n.radius) * zoomRatio) {
                    // fill(200);
                    if (mouseClick && !n.selected) {
                        n.selected = true;

                    } else if (mouseClick && n.selected) {
                        n.selected = false;
                    }
                } else {
                    // fill(120);
                    fill(218, 219, 220);
                    if (mouseClick) {
                        n.selected = false;
                    }
                }

                if (n.selected) {
                    fill(200, 160, 160);
                    strokeWeight(2.0f);
                } else {
                    strokeWeight(1.2f);
                }

                ellipse(n.x, n.y, n.radius, n.radius);

                /*
                createGradient(node.vizx,
                node.vizy,
                node.vizradius,
                13, 426);
                 */
            }
            if (this.currentView.showLabels && !mouseDragging
                    && abs(inerX) < 0.11
                    && abs(inerY) < 0.11
                    && abs(inerZ) < 0.11) {
                fill(120);
                //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
                textSize(n.radius);
                text(n.label, n.x + n.radius,
                        n.y + (n.radius / 2.50f));
            }

        }
        mouseClick = false;
    }

    public float setZoomValue(float value) {

        return 0.0f;
    }

    public void setGSValue(float value) {
        //return sliderZoomLevel;
    }

    public boolean toggleLabels() {
        this.currentView.showLabels = !this.currentView.showLabels;
        return this.currentView.showLabels;
    }

    public boolean showLabels(boolean value) {
        this.currentView.showLabels = value;
        return this.currentView.showLabels;
    }

    public boolean toggleNodes() {
        this.currentView.showNodes = !this.currentView.showNodes;
        return this.currentView.showNodes;
    }

    public boolean togglePosterOverlay() {
        this.currentView.showPosterOverlay = !this.currentView.showPosterOverlay;
        return this.currentView.showPosterOverlay;
    }

    public boolean showNodes(boolean value) {
        this.currentView.showNodes = value;
        return this.currentView.showNodes;
    }

    public boolean takePNGPicture(String path) {
        recordingMode = RecordingFormat.PNG;
        return true;
    }

    public boolean takePDFPicture(String path) {
        recordingMode = RecordingFormat.PDF;
        return true;
    }

    public boolean toggleLinks() {
        this.currentView.showLinks = !this.currentView.showLinks;
        return this.currentView.showLinks;
    }

    public boolean showLinks(boolean value) {
        this.currentView.showLinks = value;
        return this.currentView.showLinks;
    }

    public void center() {
        vizx = width / 2.0f;
        vizy = height / 2.0f;
        zoomRatio = 1.0f;
    }

    public int setLiterratureLevel(int value) {
        return 0;
    }

    public boolean updateView(String uri)
            throws
            URISyntaxException,
            MalformedURLException,
            IOException,
            XPathExpressionException {
        // locked = true;
        boolean result = false;

        //try {
        System.out.println("updating view..");
        // locked = true;
        result = currentView.update(uri);
        nodes = new ArrayList<Node>(currentView.nodeMap.values());
        //nodes = currentView.nodeList;
        //locked = false;
            /*
        } catch (MalformedURLException ex) {
        Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
        Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        locked = false;
        }
         */
        // update the scene

        return result;
    }

    @Override
    public void mousePressed() {
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

    public void mouseClicked() {
        mouseClick = true;
    }

    @Override
    public void mouseDragged() {
        mouseDragging = true;
        float dragSensibility = 3.0f;
        inerX = ((float) mouseX - oldmouseX) / (zoomRatio * dragSensibility);
        inerY = ((float) mouseY - oldmouseY) / (zoomRatio * dragSensibility);
        vizx += inerX * 2.0f;
        vizy += inerY * 2.0f;
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
        inerZ = -e.getWheelRotation();
    }

    void arrow(float x1, float y1, float x2, float y2, float radius) {
        pushMatrix();
        translate(x2, y2);
        float a = atan2(x1 - x2, y2 - y1);
        rotate(a);
        line(0, -radius, -2, -2 - radius);
        line(0, -radius, 2, -2 - radius);
        popMatrix();
    }

    private void createGradient(double x, double y, double radius, int c1, int c2) {
        double px = 0, py = 0, angle = 0;


        // calculate differences between color components
        double deltaR = red(c2) - red(c1);
        double deltaG = green(c2) - green(c1);
        double deltaB = blue(c2) - blue(c1);
        // hack to ensure there are no holes in gradient
        // needs to be increased, as radius increases
        double gapFiller = 8.0;

        for (int i = 0; i < radius; i++) {
            for (double j = 0; j < 360; j += 1.0 / gapFiller) {
                px = x + cos(radians((float) angle)) * i;
                py = y + sin(radians((float) angle)) * i;
                angle += 1.0 / gapFiller;
                int c = color((float) (red(c1) + (i) * (deltaR / radius)),
                        (float) (green(c1) + (i) * (deltaG / radius)),
                        (float) (blue(c1) + (i) * (deltaB / radius)));
                set((int) px, (int) py, c);
            }
        }
        // adds smooth edge
        // hack anti-aliasing
        noFill();
        strokeWeight(3);
        ellipse((float) x, (float) y, (float) radius * 2, (float) radius * 2);
    }
}


