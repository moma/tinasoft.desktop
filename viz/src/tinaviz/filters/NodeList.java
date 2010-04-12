/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.LinkedList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PVector;
import tinaviz.graph.Node;

/**
 *
 * @author jbilcke
 */
public class NodeList {

    public List<Node> nodes;
    public float NORMALIZED_MIN_EDGE_WEIGHT = 0.0f;
    public float NORMALIZED_MAX_EDGE_WEIGHT = 1.0f; // desired default weight
    public float NORMALIZED_MIN_RADIUS = 0.01f;
    public float NORMALIZED_MAX_RADIUS = 1.0f; // largely depends on the spatialization settings
    // TODO fix me
    public float NORMALIZED_MIN_NODE_WEIGHT = 1.0f;
    public float NORMALIZED_MAX_NODE_WEIGHT = 2.0f;
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;
    public float minRadius;
    public float maxRadius;
    public PVector center;
    public PVector baryCenter;
    public float graphWidth;
    public float graphHeight;
    public float graphRadius;
    public float minEdgeWeight;
    public float maxEdgeWeight;
    public float minNodeWeight;
    public float maxNodeWeight;
    public boolean autocenter = false;

    public NodeList(List<Node> nodes) {
        this.nodes = nodes;
    }

    public NodeList() {
        nodes = new LinkedList<Node>();
        reset();
    }

    public void clear() {
        nodes.clear();
        reset();
    }

    public void addAll(NodeList nodes) {
        this.nodes.addAll(nodes.nodes);
        this.computeExtremums();
    }

    public void reset() {

        minX = 0.0f;
        minY = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minRadius = 0.0f;
        maxRadius = Float.MIN_VALUE;
        center = new PVector(0.0f, 0.0f);
        baryCenter = new PVector(0.0f, 0.0f);
        graphRadius = 0.0f;
        graphWidth = 0.0f;
        graphHeight = 0.0f;
        minEdgeWeight = Float.MAX_VALUE;
        maxEdgeWeight = Float.MIN_VALUE;
        minNodeWeight = Float.MAX_VALUE;
        maxNodeWeight = Float.MIN_VALUE;
    }

    public int size() {
        return nodes.size();
    }

    @Override
    public String toString() {
        return "minX=" + minX + ", "
                + "minY=" + minY + ", "
                + "maxX=" + maxX + ", "
                + "maxY=" + maxY + ", "
                + "minRadius=" + minRadius + ", "
                + "maxRadius=" + maxRadius + ", "
                + "graphWidth=" + graphWidth + ","
                + "graphHeight=" + graphHeight + ","
                + "graphRadius=" + graphRadius + ","
                + "centerX=" + center.x + ", "
                + "centerY=" + center.y + ", "
                + "baryCenterX=" + baryCenter.x + ", "
                + "baryCenterY=" + baryCenter.y + ", "
                + "minEdgeWeight=" + minEdgeWeight + ", "
                + "maxEdgeWeight=" + maxEdgeWeight + ";"
                + "minNodeWeight=" + minNodeWeight + ", "
                + "maxNodeWeight=" + maxNodeWeight + ";";
    }

    public void add(Node node) {
        nodes.add(node);
        computeExtremumsFromNode(node);
    }

    public Node get(int i) {
        return nodes.get(i);
    }

    public synchronized void normalize() {

        // now we need to normalize the graph
        for (Node n : nodes) {

            // NORMALIZE RADIUS
            //System.out.println("node "+n.label+" ("+n.category+")");
            //System.out.println(" - radius avant:"+n.radius);

            n.radius =  (minRadius == maxRadius) ? NORMALIZED_MIN_RADIUS : PApplet.map(n.radius,
                    minRadius, maxRadius,
                    NORMALIZED_MIN_RADIUS, NORMALIZED_MAX_RADIUS);
            // System.out.println(" -  normalized radius:"+n.radius);

            // NORMALIZE COLORS USING RADIUS
            if (n.r < 0) {
                n.r = 255 - 160 * n.radius;
            }
            if (n.g < 0) {
                n.g = 255 - 160 * n.radius;
            }
            if (n.b < 0) {
                n.b = 255 - 160 * n.radius;
            }

            //System.out.println("n.weight = " + "PApplet.map(" + n.weight + ","
              //      + minNodeWeight + ", " + maxNodeWeight + "," + NORMALIZED_MIN_NODE_WEIGHT + ", " + NORMALIZED_MAX_NODE_WEIGHT + ");");

            // NORMALIZE WEIGHT

            n.weight = (minNodeWeight == maxNodeWeight) ? NORMALIZED_MIN_NODE_WEIGHT : PApplet.map(n.weight,
                    minNodeWeight, maxNodeWeight,
                    NORMALIZED_MIN_NODE_WEIGHT, NORMALIZED_MAX_NODE_WEIGHT);

            //System.out.println("normalized genericity:"+n.genericity+"\n");
            //System.out.println("n.weight = " + n.weight);

            // NORMALIZE WEIGHTS
            for (Long k : n.weights.keySet()) {
                //System.out.println("  - w1: "+n.weights.get(k));

                n.weights.put(k, (minEdgeWeight == maxEdgeWeight) ? NORMALIZED_MIN_EDGE_WEIGHT : PApplet.map(n.weights.get(k),
                        minEdgeWeight, maxEdgeWeight,
                        NORMALIZED_MIN_EDGE_WEIGHT, NORMALIZED_MAX_EDGE_WEIGHT));
                //System.out.println("  - w2: "+n.weights.get(k));
            }

        }
    }

    public synchronized void computeExtremums() {
        reset();
        Float mx = null;
        Float my = null;
        for (Node n : nodes) {
            computeExtremumsKernel(n);
            mx = (mx == null) ? n.x : mx + n.x;
            my = (my == null) ? n.y : my + n.y;
        }
        if (mx != null && my != null) {
            baryCenter.set(mx / nodes.size(), my / nodes.size(), 0);
        }
        aftermath();
    }

    public synchronized void computeExtremumsFromNode(Node node) {
        computeExtremumsKernel(node);
        aftermath();
    }

    // TODO MOYENNE
    private void aftermath() {
        // simple heuristic to correct the values, just in case we didn't found anything
        if (minEdgeWeight == Float.MAX_VALUE) {
            minEdgeWeight = (maxEdgeWeight != Float.MIN_VALUE) ? maxEdgeWeight - 1 : Float.MIN_VALUE + 1;
        }
        if (maxEdgeWeight == Float.MIN_VALUE) {
            maxEdgeWeight = (minEdgeWeight != Float.MAX_VALUE) ? minEdgeWeight + 1 : Float.MAX_VALUE - 1;
        }
        if (minNodeWeight == Float.MAX_VALUE) {
            minNodeWeight = (maxNodeWeight != Float.MIN_VALUE) ? maxNodeWeight - 1 : Float.MIN_VALUE + 1;
        }
        if (maxNodeWeight == Float.MIN_VALUE) {
            maxNodeWeight = (minNodeWeight != Float.MAX_VALUE) ? minNodeWeight + 1 : Float.MAX_VALUE - 1;
        }

        graphWidth = maxX - minX;
        graphHeight = maxY - minY;
        graphRadius = (graphWidth + graphHeight) / 2.0f;
        center.x = (graphWidth / 2.0f) + minX;
        center.y = (graphHeight / 2.0f) + minY;
    }

    private void computeExtremumsKernel(Node n) {
        if (n.x < minX) {
            minX = n.x;
        }
        if (n.x > maxX) {
            maxX = n.x;
        }
        if (n.y < minY) {
            minY = n.y;
        }
        if (n.y > maxY) {
            maxY = n.y;
        }
        if (n.radius < minRadius) {
            minRadius = n.radius;
        }
        if (n.radius > maxRadius) {
            maxRadius = n.radius;
        }
        if (n.weight < minNodeWeight) {
            minNodeWeight = n.weight;
        }
        if (n.weight > maxNodeWeight) {
            maxNodeWeight = n.weight;
        }


        for (Float weight : n.weights.values()) {
            if (weight > maxEdgeWeight) {
                maxEdgeWeight = weight;
            }
            if (weight < minEdgeWeight) {
                minEdgeWeight = weight;
            }
        }
    }
}
