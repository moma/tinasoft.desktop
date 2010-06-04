/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import eu.tinasoft.services.computing.MathFunctions;
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

        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;

        for (Node n : nodes) {
            // MIN
            if (n.position.x < minX) minX = n.position.x;
            if (n.position.y < minX) minY = n.position.y;

            // MAX
            if (n.position.x > maxX) maxX = n.position.x;
            if (n.position.y > maxX) maxY = n.position.y;
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
        
        // prepare the variable computation
        metrics.minX = Float.POSITIVE_INFINITY;
        metrics.minY = Float.POSITIVE_INFINITY;
        metrics.maxX = Float.NEGATIVE_INFINITY;
        metrics.maxY = Float.NEGATIVE_INFINITY;
        metrics.minNodeWeight = Float.POSITIVE_INFINITY;
        metrics.maxNodeWeight = Float.NEGATIVE_INFINITY;
        metrics.minEdgeWeight = Float.POSITIVE_INFINITY;
        metrics.maxEdgeWeight = Float.NEGATIVE_INFINITY;
        metrics.minNodeRadius = Float.POSITIVE_INFINITY;
        metrics.maxNodeRadius = Float.NEGATIVE_INFINITY;

        metrics.nbNodes = nodes.size();
        float size = (float) metrics.nbNodes;

        for (Node n : nodes) {
            if (n.position.x < metrics.minX) metrics.minX = n.position.x;
            if (n.position.y < metrics.minY) metrics.minY = n.position.y;
            if (n.position.x > metrics.maxX) metrics.maxX = n.position.x;
            if (n.position.y > metrics.maxY) metrics.maxY = n.position.y;
            if (n.weight < metrics.minNodeWeight) metrics.minNodeWeight = n.weight;
            if (n.weight > metrics.maxNodeWeight) metrics.maxNodeWeight = n.weight;
            if (n.radius < metrics.minNodeRadius) metrics.minNodeRadius = n.radius;
            if (n.radius > metrics.maxNodeRadius) metrics.maxNodeRadius = n.radius;
            metrics.averageNodeRadius += n.radius;
            metrics.averageNodeWeight += n.weight;

            for (int k : n.weights.keys().elements()) {
                float w = (Float) n.weights.get(k);
                if (w < metrics.minEdgeWeight) metrics.minEdgeWeight = w;
                if (w > metrics.maxEdgeWeight) metrics.maxEdgeWeight = w;
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
        System.out.println("normalizing..");

        int i = 0;
        for (Node n : nodes) {
            
            if (attr != null) {
                if (!n.attributes.containsKey(attr)) {
                    continue;
                }
                if (!n.attributes.get(attr).equals(value)) {
                    continue;
                }
            }

            // todo: check it isn't bigger than max
            n.radius = MathFunctions.map(n.radius,
                    metrics.minNodeRadius,
                    metrics.maxNodeRadius,
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
            // todo check is < max
            n.weight = MathFunctions.map(n.weight,
                    metrics.minNodeWeight, metrics.maxNodeWeight,
                    NORMALIZED_MIN_NODE_WEIGHT, NORMALIZED_MAX_NODE_WEIGHT);

            // NORMALIZE WEIGHTS
            for (int k : n.weights.keys().elements()) {
                float w = (Float) n.weights.get(k);
                /*
                w = (metrics.minEdgeWeight == metrics.maxEdgeWeight)
                        ? ( metrics.minEdgeWeight == 0 ? NORMALIZED_MIN_EDGE_WEIGHT : metrics.minEdgeWeight )
                        : ((NORMALIZED_MAX_EDGE_WEIGHT * PApplet.abs(w)) / (PApplet.max(
                        PApplet.abs(metrics.minEdgeWeight), PApplet.abs(metrics.maxEdgeWeight))));*/
                w = MathFunctions.map(w, metrics.minEdgeWeight, metrics.maxEdgeWeight, NORMALIZED_MIN_EDGE_WEIGHT, NORMALIZED_MAX_EDGE_WEIGHT);
                //System.out.println("EDGE NORMLAZ = "+w+" = PApplet.map("+w+","+metrics.minEdgeWeight+","+metrics.maxEdgeWeight+","+NORMALIZED_MIN_EDGE_WEIGHT+","+NORMALIZED_MAX_EDGE_WEIGHT+")");
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
