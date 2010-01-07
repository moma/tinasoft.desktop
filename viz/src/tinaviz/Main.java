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
    int sliderZoomLevel = 5;
    float zoomRatio = 0.2f;
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
    private boolean recordingMode = false;
    private String recordPath = "graph.pdf";

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
        smooth();
        addMouseWheelListener(this);
        //noStroke();
        // current sketch's "data" directory to load successfully
        System.out.println("loading font..");
        albatar = loadFont("AlBattar-48.vlw");
        System.out.println("loading empty data..");
        currentView = new View();

        oldmouseX = mouseX;
        oldmouseY = mouseY;

        vizx = (width / 2.0f);
        vizy = (height / 2.0f);

        Node node;
        System.out.println("Generating random graph..");
        float rx = random(width);
        float ry = random(height);
        float radius = 0.0f;
        for (int i = 0; i < 60; i++) {
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
            // a = nodes[i];
            for (int j = 0; j < nodes.size() && i != j; j++) {
                // b = nodes[j];
                if (random(1.0f) < 0.02) { // link density : 0.02 = a lot, 0.0002 = a few
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
        if (recordingMode) {
            // Note that #### will be replaced with the frame number. Fancy!
            textMode(SHAPE);
            beginRecord(PDF, recordPath);
            subdraw();
            endRecord();
            recordingMode = false;
            textMode(MODEL);
        }
        subdraw();
    }

    public void subdraw() {

        background(255);
        stroke(0);
        fill(120);
        strokeWeight(1.0f);
        //zoomRatio = 10.0 / (double) sliderZoomLevel;
        textFont(albatar, 48);

        Node node_a;
        Node node_b;
        Node node;

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
        float r = 0.05f;
        float a = 0.0002f;


        if (recordingMode || currentView.showPosterOverlay) {
            fill(30);
            textSize(40);
            text("TinaSoft", 15f, 50f);

            fill(80);
            textSize(18);
            text("A cool project subtitle", 18f, 70f);
            fill(120);
        }



        if (!mouseDragging) {
            /*
            fricX = (fricX <= 0.01f) ? 0f : fricX * 0.92f;
            fricY = (fricY <= 0.01f) ? 0f : fricY * 0.92f;

            // friction
            inerX *= fricX;
            inerY *= fricY;

            vizx += inerX;
            vizy += inerY;
            */
        }

        inerX *= 0.9f;
        if (abs(inerX) <= 0.0005) inerX = 0.0f;
        inerY *= 0.95f;
        if (abs(inerY) <= 0.0005) inerY = 0.0f;
        inerZ *= 0.95f;
        if (abs(inerZ) <= 0.0005) inerZ = 0.0f;

        zoomRatio += inerZ * 0.005f;
        vizx += inerX * 2.0f;
        vizy += inerY * 2.0f;

         scale(zoomRatio);
        translate(vizx, vizy);

        stroke(150, 150, 150);

        if (!mouseDragging) {
            strokeWeight(3.0f);
        }

        for (int i = 0; i < nodes.size(); i++) {
            node_a = nodes.get(i);

            for (int j = 0; j < nodes.size() && j != i; j++) {
                node_b = nodes.get(j);

                if (!mouseDragging) {
                    vx = node_b.x - node_a.x;
                    vy = node_b.y - node_a.y;
                    len = sqrt(sq(vx) + sq(vy));
                }

                if (node_a.neighbours.contains(node_b)) {

                    // ATTRACTION
                    if (!mouseDragging) {
                        node_a.vx = node_a.vx + (vx * len) * a;
                        node_a.vy = node_a.vy + (vy * len) * a;
                        node_b.vx = node_b.vx - (vx * len) * a;
                        node_b.vy = node_b.vy - (vy * len) * a;
                    }
                    // AFFICHAGE LIEN (A CHANGER)

                    float ponderated_radius =
                            ((node_a.radius + node_a.radius) * 0.5f);
                    int rgb = (int) ((255.0f / MAX_RADIUS) * ponderated_radius);

                    if (this.currentView.showLinks) {

                        // old: 150
                        stroke(rgb);
                        if (!mouseDragging) {
                            strokeWeight(((node_a.radius + node_a.radius) * 0.05f));
                        }
                        line(node_a.x, node_a.y,node_b.x,node_b.y);
                    }



                }
                // REPULSION
                if (!mouseDragging) {
                    node_a.vx = node_a.vx - (vx / len) * r;
                    node_a.vy = node_a.vy - (vy / len) * r;
                    node_b.vx = node_b.vx + (vx / len) * r;
                    node_b.vy = node_b.vy + (vy / len) * r;
                }
            } // FOR NODE B
        }   // FOr NODE A

        stroke(20, 20, 20);
        if (!mouseDragging) {
            strokeWeight(1.2f);
        }

        // UPDATE NODE POSITIONS
        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.get(i);
            node.x += node.vx;
            node.y += node.vy;
            node.vx = 0.0f;
            node.vy = 0.0f;

            int rgb = (int) ((255.0 / MAX_RADIUS) * node.radius);

            float distance = dist(
                    screenX( node.x,node.y, 0f),
                    screenY(node.x, node.y, 0f),
                    mouseX,
                    mouseY);

            if (this.currentView.showNodes) {
                if (distance <= (node.radius) * zoomRatio) {
                    // fill(200);
                    fill(150, 160, 170);
                } else {
                    // fill(120);
                    fill(218, 219, 220);

                }

                ellipse(node.x,node.y,node.radius,node.radius);

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
                textSize(node.radius);
                text(node.label,node.x + node.radius,
                        node.y + (node.radius / 2.50f));
            }

        }
    }

    public float setZoomValue(float value) {
        sliderZoomLevel = (int) value;
        /*
        if (sliderZoomLevel != 0) {
        vizx *=  (double)sliderZoomLevel * 0.1;
        vizy *=  (double)sliderZoomLevel * 0.1;
        } else {
        vizx = width/2.0;
        vizy = height/2.0;
        }*/
        return (float) sliderZoomLevel;
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

    public boolean takePDFPicture(String path) {
        recordingMode = true;
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
        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    @Override
    public void mouseDragged() {
        mouseDragging = true;
        float dragSensibility = 10.0f;
        inerX =  ((float)mouseX - oldmouseX) / (zoomRatio * dragSensibility);
        inerY =  ((float)mouseY - oldmouseY) / (zoomRatio * dragSensibility);
        vizx += inerX;
        vizy += inerY;

        oldmouseX = mouseX;
        oldmouseY = mouseY;
    }

    @Override
    public void mouseReleased() {
        mouseDragging = false;

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();

        if (notches < 0) {
            inerZ -= notches;
        } else {
            inerZ -= notches;
        }
      
        //zoomRatio = sliderZoomLevel / 30.0f;
        inerZ *= 0.95f;
        if (abs(inerZ) <= 0.0005) inerZ = 0.0f;
        zoomRatio += inerZ * 0.005f;
        
        vizx -= (mouseX - oldmouseX) * zoomRatio * 2.0f;
        vizy -= (mouseY - oldmouseY) * zoomRatio * 2.0f;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

    }

    /*
     *
     *   public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();

        if (not
                ches < 0) {

            sliderZoomLevel -= notches;
        } else {

            sliderZoomLevel -= notches;
        }
        // ne pas trop fatiguer le doigt..
        if (sliderZoomLevel >= 30) {
            sliderZoomLevel = 30;
        } else if (sliderZoomLevel <= 1) {
            sliderZoomLevel = 1;
        }

        zoomRatio = sliderZoomLevel / 30.0f;

        vizx -= (mouseX - oldmouseX) * zoomRatio;
        vizy -= (mouseY - oldmouseY) * zoomRatio;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

    }
     */
    /*
    createGradient(i, j, radius,
    color(int(random(255)), int(random(255)), int(random(255))),
    color(int(random(255)), int(random(255)), int(random(255))));
     */

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


