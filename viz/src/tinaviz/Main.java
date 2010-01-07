package tinaviz;

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

public class Main extends PApplet implements MouseWheelListener {

    static int MAXLINKS = 512;
    int sliderZoomLevel = 5;
    double zoomRatio = 0.2;
    List<tinaviz.Node> nodes = new ArrayList<tinaviz.Node>();
    PImage nodeIcon;
    PFont albatar;
    double vizx = 0;
    double vizy = 0;
    int oldmouseX = 0;
    int oldmouseY = 0;
    double MAX_RADIUS = 0;
    XMLElement xml;
    View currentView = new View();
    private boolean locked = false;
    double MAX_ZOOM = 5;
    double MIN_ZOOM = 0.02;
    private boolean mouseDragging = false;
    private double inerX = 0.0;
    private double inerY = 0.0;
    private double fricX = 1.0;
    private double fricY = 1.0;

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
        for (int i = 0; i < 300; i++) {
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

        background(255);
        stroke(0);
        fill(120);
        strokeWeight(1.0f);

        Node node_a;
        Node node_b;
        Node node;

        double delta = 1;
        double len = 1;
        double vx = 1;
        double vy = 1;
        double r = 0.05;
        double a = 0.0002;


        //zoomRatio = 10.0 / (double) sliderZoomLevel;
        textFont(albatar, 48);

        scale((float) ((double) zoomRatio * (MAX_ZOOM - MIN_ZOOM) + MIN_ZOOM));
        //scale((float) zoomRatio * 0.6f);

        if (!mouseDragging) {
           fricX =(fricX <= 0.01) ? 0 : fricX * 0.92;
             fricY =(fricY <= 0.01) ? 0 : fricY * 0.92;

            // friction
             inerX *= fricX;
             inerY *= fricY;

            vizx += inerX;
            vizy += inerY;

        }
        translate((float) vizx, (float) vizy);
        double dx = 0.0;
        double dy = 0.0;

        boolean neighbour;

        //fill(200,200,200,20);
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
                    len = sqrt(sq((float) vx) + sq((float) vy));
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

                    double ponderated_radius =
                            ((node_a.radius + node_a.radius) * 0.5);
                    int rgb = (int) ((255.0 / MAX_RADIUS) * ponderated_radius);

                    if (this.currentView.showLinks) {


                        // old: 150
                        stroke(rgb);
                        if (!mouseDragging) {
                            strokeWeight((float) ((node_a.radius + node_a.radius) * 0.05));
                        }
                        line((float) node_a.vizx,
                                (float) node_a.vizy,
                                (float) node_b.vizx,
                                (float) node_b.vizy);

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

            node.x = node.x + node.vx;
            node.y = node.y + node.vy;

            node.vizx = node.x;
            node.vizy = node.y;
            node.vizradius = node.radius;

            node.vx = 0.0f;
            node.vy = 0.0f;


            int rgb = (int) ((255.0 / MAX_RADIUS) * node.radius);

            float distance = dist((float) node.vizx, (float) node.vizy, (float) mouseX, (float) mouseY);

            if (this.currentView.showNodes) {
                if (distance < node.vizradius) {
                    // fill(200);
                    fill(150, 160, 170);
                } else {
                    // fill(120);
                    fill(218, 219, 220);

                }

                ellipse((float) node.vizx,
                        (float) node.vizy,
                        (float) node.vizradius,
                        (float) node.vizradius);
            }
            if (this.currentView.showLabels && !mouseDragging) {
                fill(120);
                //fill((int) ((100.0f / MAX_RADIUS) * node.radius ));
                textSize((float) node.radius);
                text(node.label,
                        (float) (node.vizx + node.vizradius),
                        (float) (node.vizy + (node.vizradius / 2.50)));
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

    public boolean showNodes(boolean value) {
        this.currentView.showNodes = value;
        return this.currentView.showNodes;
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
        vizx = width / 2.0;
        vizy = height / 2.0;
        zoomRatio = 1.0;
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
       inerX = ((double) (mouseX - oldmouseX)) / (zoomRatio * 5.0);
        inerY = ((double) (mouseY - oldmouseY)) / (zoomRatio * 5.0);
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

        zoomRatio = (double) sliderZoomLevel / 30.0;

        vizx -= ((double) (mouseX - oldmouseX)) * zoomRatio;
        vizy -= ((double) (mouseY - oldmouseY)) * zoomRatio;
        oldmouseX = mouseX;
        oldmouseY = mouseY;

    }
}


