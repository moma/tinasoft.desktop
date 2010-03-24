/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import processing.core.PVector;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */

/*
public class Metrics {

public float minX = Float.MAX_VALUE;
public float minY = Float.MAX_VALUE;
public float maxX = Float.MIN_VALUE;
public float maxY = Float.MIN_VALUE;
public float minRadius = Float.MAX_VALUE;
public float maxRadius = Float.MIN_VALUE;
public float centerX = 0.0f;
public float centerY = 0.0f;
public float minWeight = Float.MAX_VALUE;
public float maxWeight = Float.MIN_VALUE;

public void reset() {

minX = Float.MAX_VALUE;
minY = Float.MAX_VALUE;
maxX = Float.MIN_VALUE;
maxY = Float.MIN_VALUE;
minRadius = Float.MAX_VALUE;
maxRadius = Float.MIN_VALUE;
centerX = 0.0f;
centerY = 0.0f;
minWeight = Float.MAX_VALUE;
maxWeight = Float.MIN_VALUE;
}

@Override
public String toString() {
return "minX="+minX+", "
+"minY="+minY+", "
+"maxX="+maxX+", "
+"maxY="+maxY+", "
+"minRadius="+minRadius+", "
+"maxRadius="+maxRadius+", "
+"centerX="+centerX+", "
+"centerY="+centerY+", "
+"minWeight="+minWeight+", "
+"maxWeight="+maxWeight+";";
}
}
 */
/**
 *
 * @author jbilcke
 */
public class Metrics {

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

    public Metrics() {
        reset();
    }
    public void reset() {

        minX = 0.0f;
        minY = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minRadius = 0.0f;
        maxRadius = Float.MIN_VALUE;
        center = new PVector(0.0f,0.0f);
        graphRadius = 0.0f;
        graphWidth = 0.0f;
        graphHeight = 0.0f;
        minWeight = Float.MAX_VALUE;
        maxWeight = Float.MIN_VALUE;
        minGenericity = Float.MAX_VALUE;
        maxGenericity = Float.MIN_VALUE;

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
                + "graphHeight=" + graphHeight+","
                + "graphRadius="+ graphRadius+","
                + "centerX=" + center.x + ", "
                + "centerY=" + center.y + ", "
                + "minWeight=" + minWeight + ", "
                + "maxWeight=" + maxWeight + ";"
                + "minGenericity=" + minGenericity + ", "
                + "maxGenericity=" + maxGenericity + ";";
    }

    public Metrics getClone() {
        Metrics newMetrics = new Metrics();
        newMetrics.minX = minX;
        newMetrics.minY = minY;
        newMetrics.maxX = maxX;
        newMetrics.maxY = maxY;
        newMetrics.minRadius = minRadius;
        newMetrics.maxRadius = maxRadius;
        newMetrics.center.x = center.x;
        newMetrics.center.y = center.x;
        newMetrics.minWeight = minWeight;
        newMetrics.maxWeight = maxWeight;
        newMetrics.minGenericity = minGenericity;
        newMetrics.maxGenericity = maxGenericity;
        return newMetrics;
    }

    public void compute(Graph graph) {

        for (Node n : graph.storedNodes.values()) {
                        // update the graph metrics
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

        graphWidth = maxX - minX;
        graphHeight = maxY - minY;
        graphRadius = (graphWidth + graphHeight) / 2.0f;
        center.x = (graphWidth / 2.0f) + minX;
        center.y = (graphHeight / 2.0f) + minY;
    }
}
