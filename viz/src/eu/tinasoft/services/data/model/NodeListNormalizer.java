/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import java.util.List;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class NodeListNormalizer {

    public final static float NORMALIZED_MIN_EDGE_WEIGHT = 0.0f;
    public final static float NORMALIZED_MAX_EDGE_WEIGHT = 1.0f;
    public final static float MIN_RADIUS = 1f;
    public final static float MAX_RADIUS = 2f; // largely depends on the spatialization settings
    // TODO fix me
    public final static float NORMALIZED_MIN_NODE_WEIGHT = 0.0f;
    public final static float NORMALIZED_MAX_NODE_WEIGHT = 1.0f;
    // you can set it to something like 4 for fun
    public final static float INITIAL_SQUARE_SIZE = 100f;
    public final static float MIN_X = -INITIAL_SQUARE_SIZE;
    public final static float MIN_Y = -INITIAL_SQUARE_SIZE;
    public final static float MAX_X = INITIAL_SQUARE_SIZE;
    public final static float MAX_Y = INITIAL_SQUARE_SIZE;

    public static PVector getCenter(List<Node> nodes, String id) {
        return getCenter(nodes, id.hashCode());
    }

    public static PVector getCenter(List<Node> nodes, int id) {
        for (Node n : nodes) {
            if (id == n.id) {
                return new PVector(n.position.x, n.position.y, 0);
            }
        }

        return getBarycenter(nodes);
    }

    public static PVector getBarycenter(List<Node> nodes, String id) {
        return getBarycenter(nodes, id.hashCode());
    }

    public static PVector getBarycenter(List<Node> nodes, int id) {

        for (Node n : nodes) {
            if (id == n.id) {
                return new PVector(n.position.x, n.position.y, 0);
            }
        }

        return getBarycenter(nodes);
    }

    public static PVector getCenter(List<Node> nodes) {

        if (nodes.isEmpty()) {
            return new PVector(0.0f, 0.0f, 0.0f);
        }

        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;

        for (Node n : nodes) {
            // MIN
            minX = PApplet.min(minX, n.position.x);
            minY = PApplet.min(minY, n.position.y);

            // MAX
            maxX = PApplet.max(maxX, n.position.x);
            maxY = PApplet.max(maxY, n.position.y);
        }

        return new PVector(((maxX - minX) / 2.0f) + minX, ((maxY - minY) / 2.0f) + minY);

    }

    public static PVector getBarycenter(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return new PVector(0.0f, 0.0f, 0.0f);
        }

        float avgX = 0.0f, avgY = 0.0f;

        for (Node n : nodes) {
            avgX += n.position.x;
            avgY += n.position.y;
        }

        PVector r = new PVector(avgX, avgY);
        r.div(nodes.size());
        return r;
    }

    public static Metrics computeMetrics(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return new Metrics();
        }

        Metrics metrics = new Metrics();

        // prepare the variable for computation
        metrics.minX = Float.MAX_VALUE;
        metrics.minY = Float.MAX_VALUE;
        metrics.maxX = Float.MIN_VALUE;
        metrics.maxY = Float.MIN_VALUE;
        metrics.minNodeWeight = Float.MAX_VALUE;
        metrics.maxNodeWeight = Float.MIN_VALUE;
        metrics.minEdgeWeight = Float.MAX_VALUE;
        metrics.maxEdgeWeight = Float.MIN_VALUE;
        metrics.minNodeRadius = Float.MAX_VALUE;
        metrics.maxNodeRadius = Float.MIN_VALUE;

        metrics.nbNodes = nodes.size();
        float size = (float) metrics.nbNodes;

        for (Node n : nodes) {
            // X
            metrics.minX = PApplet.min(metrics.minX, n.position.x);
            metrics.maxX = PApplet.max(metrics.maxX, n.position.x);

            // Y
            metrics.minY = PApplet.min(metrics.minY, n.position.y);
            metrics.maxY = PApplet.max(metrics.maxY, n.position.y);

            // NODE WEIGHT
            metrics.minNodeWeight = PApplet.min(metrics.minNodeWeight, n.weight);
            metrics.maxNodeWeight = PApplet.max(metrics.maxNodeWeight, n.weight);

            // NODE WEIGHT
            metrics.minNodeRadius = PApplet.min(metrics.minNodeRadius, n.radius);
            metrics.maxNodeRadius = PApplet.max(metrics.maxNodeRadius, n.radius);

            metrics.averageNodeRadius += n.radius;
            metrics.averageNodeWeight += n.weight;

            for (int k : n.weights.keys().elements()) {
                float w = (Float) n.weights.get(k);
                metrics.minEdgeWeight = PApplet.min(metrics.minEdgeWeight, w);
                metrics.maxEdgeWeight = PApplet.max(metrics.maxEdgeWeight, w);
                metrics.averageEdgeWeight += w;
                metrics.nbEdges++;
            }
            // SUM
            metrics.baryCenter.x += n.position.x;
            metrics.baryCenter.y += n.position.y;
        }


        metrics.averageNodeWeight /= size;
        metrics.averageNodeRadius /= size;
        metrics.averageEdgeWeight /= metrics.nbEdges;
        // compute the barycenter (divide the sum by the size to get the average)
        metrics.baryCenter.div(size);

        metrics.graphWidth = (metrics.maxX - metrics.minX);
        metrics.graphHeight = (metrics.maxY - metrics.minY);
        metrics.graphRadius = (metrics.graphWidth + metrics.graphHeight) / 2.0f;


        metrics.center.set(
                (metrics.graphWidth / 2.0f) + metrics.minX,
                (metrics.graphHeight / 2.0f) + metrics.minY,
                0.0f);

        return metrics;
    }


    public static List<Node> normalize(List<Node> nodes) {
        return normalize(nodes, computeMetrics(nodes));
    }
    public static List<Node> normalize(List<Node> nodes, Metrics metrics) {
        return normalize(nodes, metrics, null, null);
    }
    public static List<Node> normalize(List<Node> nodes, String attr, Object value) {
        return normalize(nodes, computeMetrics(nodes), attr, value);
    }

    public static List<Node> normalize(List<Node> nodes, Metrics metrics, String attr, Object value) {
        //System.out.println("normalizing..");

        int i = 0;
        for (Node n : nodes) {
            
            if (attr != null) {
                if (!n.attributes.containsKey(attr)) {
                    continue;
                }
                if (!n.attributes.get(attr).equals(value)) {
                    //System.out.println("not normalizing "+n.uuid+" because "+n.category);
                    continue;
                }
            }


            n.radius = (metrics.minRadius == metrics.maxRadius)
                    ? MIN_RADIUS
                    : PApplet.map(n.radius,
                    metrics.minRadius,
                    metrics.maxRadius,
                    MIN_RADIUS,
                    MAX_RADIUS);

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

            // NORMALIZE WEIGHT
            n.weight =
                    (metrics.minNodeWeight == metrics.maxNodeWeight)
                    ? NORMALIZED_MIN_NODE_WEIGHT
                    : PApplet.map(n.weight,
                    metrics.minNodeWeight, metrics.maxNodeWeight,
                    NORMALIZED_MIN_NODE_WEIGHT, NORMALIZED_MAX_NODE_WEIGHT);

            // NORMALIZE WEIGHTS
            for (int k : n.weights.keys().elements()) {
                float w = (Float) n.weights.get(k);
                w = (metrics.minEdgeWeight == metrics.maxEdgeWeight)
                        ? NORMALIZED_MIN_EDGE_WEIGHT
                        : ((NORMALIZED_MAX_EDGE_WEIGHT * PApplet.abs(w)) / (PApplet.max(
                        PApplet.abs(metrics.minEdgeWeight), PApplet.abs(metrics.maxEdgeWeight))));
                n.weights.put(k, w);
                n.weightsDistribution.put(k, PApplet.map(i, 0, metrics.nbEdges, 0.01f, 1.0f));
            }

            i++;
        }


        return nodes;
    }

    public static List<Node> normalizePositions(List<Node> nodes) {
        return normalizePositions(nodes, computeMetrics(nodes));
    }

    public static List<Node> normalizePositions(List<Node> nodes, Metrics metrics) {

        for (Node n : nodes) {

            n.position.set((metrics.minX == metrics.maxX)
                    ? MIN_X
                    : PApplet.map(n.position.x,
                    metrics.minX,
                    metrics.maxX,
                    MIN_X,
                    MAX_X),
                    (metrics.minY == metrics.maxY)
                    ? MIN_Y
                    : PApplet.map(n.position.y,
                    metrics.minY,
                    metrics.maxY,
                    MIN_Y,
                    MAX_Y),
                    0.0f);
        }

        return nodes;

    }
}
