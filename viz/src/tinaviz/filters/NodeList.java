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
    public float NORMALIZED_MIN_WEIGHT = 0.0f;
    public float NORMALIZED_MAX_WEIGHT = 1.0f; // desired default weight
    public float NORMALIZED_MIN_RADIUS = 0.01f;
    public float NORMALIZED_MAX_RADIUS = 1.0f; // largely depends on the spatialization settings
    public float NORMALIZED_MIN_GENERICITY = 1.0f;
    public float NORMALIZED_MAX_GENERICITY = 2.0f;
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;
    public float minRadius;
    public float maxRadius;
    public PVector center;
    public float graphWidth;
    public float graphHeight;
    public float graphRadius;
    public float minWeight;
    public float maxWeight;
    public float minGenericity;
    public float maxGenericity;

    public NodeList(List<Node> nodes) {
        this.nodes = nodes;
        computeExtremums();
        normalize();
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
        graphRadius = 0.0f;
        graphWidth = 0.0f;
        graphHeight = 0.0f;
        minWeight = Float.MAX_VALUE;
        maxWeight = Float.MIN_VALUE;
        minGenericity = Float.MAX_VALUE;
        maxGenericity = Float.MIN_VALUE;
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
                + "minWeight=" + minWeight + ", "
                + "maxWeight=" + maxWeight + ";"
                + "minGenericity=" + minGenericity + ", "
                + "maxGenericity=" + maxGenericity + ";";
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

            n.radius = PApplet.map(n.radius,
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


            // NORMALIZE GENERICITY
            n.genericity = PApplet.map(n.genericity,
                    minGenericity, maxGenericity,
                    NORMALIZED_MIN_GENERICITY, NORMALIZED_MAX_GENERICITY);
            //System.out.println("normalized genericity:"+n.genericity+"\n");

            // NORMALIZE WEIGHTS
            for (Long k : n.weights.keySet()) {
                //System.out.println("  - w1: "+n.weights.get(k));
                n.weights.put(k, PApplet.map(n.weights.get(k),
                        minWeight, maxWeight,
                        NORMALIZED_MIN_WEIGHT, NORMALIZED_MAX_WEIGHT));
                //System.out.println("  - w2: "+n.weights.get(k));
            }

        }
    }

    public synchronized void computeExtremums() {
        reset();
        for (Node n : nodes) {
            computeExtremumsKernel(n);
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
        if (minWeight == Float.MAX_VALUE) minWeight = (maxWeight!=Float.MIN_VALUE) ? maxWeight -1 : Float.MIN_VALUE + 1;
        if (maxWeight == Float.MIN_VALUE) maxWeight = (minWeight!=Float.MAX_VALUE) ? minWeight +1 : Float.MAX_VALUE - 1;
        if (minGenericity == Float.MAX_VALUE) minGenericity = (maxGenericity!=Float.MIN_VALUE) ? maxGenericity -1 : Float.MIN_VALUE + 1;
        if (maxGenericity == Float.MIN_VALUE) maxGenericity = (minGenericity!=Float.MAX_VALUE) ? minGenericity +1 : Float.MAX_VALUE - 1;

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
        if (n.genericity < minGenericity) {
            minGenericity = n.genericity;
        }
        if (n.genericity > maxGenericity) {
            maxGenericity = n.genericity;
        }


        for (Float weight : n.weights.values()) {
            if (weight > maxWeight) {
                maxWeight = weight;
            }
            if (weight < minWeight) {
                minWeight = weight;
            }
        }
    }
}
