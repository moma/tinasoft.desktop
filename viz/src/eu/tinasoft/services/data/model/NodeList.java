/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.formats.json.JSONEncoder;
import eu.tinasoft.services.formats.json.JSONException;
import eu.tinasoft.services.formats.json.JSONStringer;
import eu.tinasoft.services.formats.json.JSONWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class NodeList {
    private boolean mayNeedRecentering = false;

    public NodeList(NodeList nodeList) {
        reset();
        //System.out.println("copying nodes from another node list, but clearing state..");
        mayNeedRecentering = nodeList.getMayNeedRecentering();
        nodes = new LinkedList<Node>();
        for (Node n : nodeList.nodes) {
            nodes.add(n);
        }
        computeExtremums();
    }
    private Node getNode(int nodeId) {
        for (Node node : nodes) {
            if (node.id == nodeId) {
                return node;
            }
        }
        return null;
    }


    public Node getNode(String id) {
        return getNode(id.hashCode());
    }

    /** getNodesByLabel
     * @param label : String
     * @param mode : String (equalsIgnoreCase,equals,startsWith,endsWith,contains)
     */
    public List<Node> getNodesByLabel(String label, String mode) {

        List<Node> results = new ArrayList<Node>();

        if (mode.equalsIgnoreCase("equalsIgnoreCase")) {
            for (Node n : nodes) {
                //System.out.println("checking if "+n.label+" contains ("+label+")")
                if (n.label.equalsIgnoreCase(label)) {
                    //System.out.println("okay, adding");
                    results.add(n);
                }
            }
        } else if (mode.equalsIgnoreCase("equals")) {
            for (Node n : nodes) {
                if (n.label.equals(label)) {
                    results.add(n);
                }
            }
        } else if (mode.equalsIgnoreCase("startsWith")) {
            for (Node n : nodes) {
                if (n.label.startsWith(label)) {
                    results.add(n);
                }
            }
        } else if (mode.equalsIgnoreCase("endsWith")) {
            for (Node n : nodes) {
                if (n.label.endsWith(label)) {
                    results.add(n);
                }
            }
        } else if (mode.equalsIgnoreCase("contains")) {
            for (Node n : nodes) {
                //System.out.println("checking if "+n.label+" contains ("+label+")");
                if (n.label.contains(label)) {
                    //System.out.println("okay, adding");
                    results.add(n);
                }
            }
        } else if (mode.equalsIgnoreCase("containsIgnoreCase")) {
            for (Node n : nodes) {
                //System.out.println("checking if "+n.label+" contains ("+label+")");
                if (n.label.toLowerCase().contains(label.toLowerCase())) {
                    //System.out.println("okay, adding");
                    results.add(n);
                }
            }
        }

        return results;
    }

    public List<Node> getNodesByCategory(String category) {
        List<Node> results = new ArrayList<Node>();
        for (Node n : nodes) {
            if (n.category.equals(category)) {
                results.add(n);
            }
        }
        return results;
    }

    public void selectNode(String str) {
        for (Node n : nodes) {
            if (n.id == str.hashCode()) {
                n.selected = true;
                break;
            }
        }
    }

    public PVector getSelectedNodesBarycenter() {
        PVector bary = new PVector(0.0f, 0.0f, 0.0f);
        Float mx = null;
        Float my = null;
        int i = 0;
        for (Node n : nodes) {
            if (!n.selected)
                continue;
            mx = (mx == null) ? n.position.x : mx + n.position.x;
            my = (my == null) ? n.position.y : my + n.position.y;
            i++;
        }
        if (mx != null && my != null) {
            if (i != 0) {
            bary.set(mx / i, my / i, 0);
            } 
        }
        return bary;
    }

    public void selectNodes(List<String> ids) {
        for (String id : ids) {
            selectNode(id);
        }
    }

    public void unselect(String id) {
        for (Node n : nodes) if (n.id == id.hashCode()) n.selected = false;
    }

    public PVector computeBaryCenter() {
        PVector bary = new PVector(0.0f, 0.0f, 0.0f);
        Float mx = 0.0f;
        Float my = 0.0f;
        int i = 0;
        for (Node n : nodes) {
            mx = (mx == null) ? n.position.x : mx + n.position.x;
            my = (my == null) ? n.position.y : my + n.position.y;
            i++;
        }
        if (mx != null && my != null) {
            if (i != 0) {
            bary.set(mx / i, my / i, 0);
            } 
        }
        baryCenter = bary;
        return baryCenter;
    }

    public float computeRadius() {
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;
        if (nodes.size() > 0) {
             for (Node n : nodes) {
                 maxX = PApplet.max(maxX,n.position.x);
                 minX = PApplet.min(minX,n.position.x);
                 maxY = PApplet.max(maxY,n.position.y);
                 minY = PApplet.min(minY,n.position.y);
            }
        } else {
             maxX =  1.0f;
             minX = -1.0f;
             maxY =  1.0f;
             minY = -1.0f;
        }
        graphWidth = maxX - minX;
        graphHeight = maxY - minY;
        graphRadius = (graphWidth + graphHeight) / 2.0f;
        return graphRadius;
    }

    public boolean getMayNeedRecentering() {
        return mayNeedRecentering;
    }

    public void highlightNodeById(String str) {
         int id = str.hashCode();
        for (Node n : nodes) {
            n.isFirstHighlight = (n.id == id);
        }
    }

    public String getNeighbourhoodAsJSON(String id) {
        String result = "";

        Node node = getNode(id);

        if (node == null) {
            return "{}";
        }
        JSONWriter writer = null;


        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }

        try {
            for (int nodeId : node.weights.keys().elements()) {
                Node n = getNode(nodeId);
                writer.key(n.uuid).object();
                for (Entry<String, Object> entry : n.getAttributes().entrySet()) {
                    writer.key(entry.getKey()).value(JSONEncoder.valueEncoder(entry.getValue()));
                }
                writer.endObject();
            }

        } catch (JSONException jSONException) {
            Console.error(jSONException.getMessage());
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        //System.out.println("data: " + writer.toString());
        return writer.toString();
    }

    public String getSelectedNodesAsJSON() {
        String result = "";
        JSONWriter writer = null;
        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }

        try {
            for (Node node : nodes) {

                if (node.selected) {
                    writer.key(node.uuid).object();
                    writer.key("id").value(node.uuid);
                    for (Entry<String, Object> entry : node.getAttributes().entrySet()) {
                        writer.key(entry.getKey()).value(JSONEncoder.valueEncoder(entry.getValue()));
                    }
                    writer.endObject();
                }
            }

        } catch (JSONException jSONException) {
            Console.error(jSONException.getMessage());
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        System.out.println("data: " + writer.toString());
        return writer.toString();
    }



    public class SelectedComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;

            if (n1.selected) {
                if (n2.selected) {
                    return 0;
                } else if (n2.isFirstHighlight) {
                    return +1;
                } else {
                    return +1;
                }
            } else if (n1.isFirstHighlight) {
                if (n2.selected) {
                    return -1;
                } else if (n2.isFirstHighlight) {
                    return 0;
                } else {
                    return +1;
                }
            } else {
                if (n2.selected) {
                    return -1;
                } else if (n2.isFirstHighlight) {
                    return -1;
                } else {
                    return 0;
                }
            }

        }
    }
    public List<Node> nodes;
    // should stay to 0 and 1
    public float NORMALIZED_MIN_EDGE_WEIGHT = 0.0f;
    public float NORMALIZED_MAX_EDGE_WEIGHT = 1.0f;
    public float MIN_RADIUS = 1f;
    public float MAX_RADIUS = 2f; // largely depends on the spatialization settings
    // TODO fix me
    public float NORMALIZED_MIN_NODE_WEIGHT = 0.0f;
    public float NORMALIZED_MAX_NODE_WEIGHT = 1.0f;
    // you can set it to something like 4 for fun
    public float INITIAL_SQUARE_SIZE = 100f;
    public float MIN_X = -INITIAL_SQUARE_SIZE;
    public float MIN_Y = -INITIAL_SQUARE_SIZE;
    public float MAX_X = INITIAL_SQUARE_SIZE;
    public float MAX_Y = INITIAL_SQUARE_SIZE;
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
    public int nbEdges;
    private Comparator comp = new SelectedComparator();

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

    public void sortBySelectionStatus() {

        Collections.sort(nodes, comp);
    }

    public void reset() {

        minX = 0.0f;
        minY = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minRadius = Float.MAX_VALUE;
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
        nbEdges = 0;
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

    public void addWithoutTouching(Node node) {
        nodes.add(node);
    }

    public void add(Node node) {
                mayNeedRecentering = true;
    }

    public void setMayNeedRecentering(boolean value) {
        mayNeedRecentering = value;
    }
    public Node get(int i) {
        return nodes.get(i);
    }

    public synchronized void normalizePositions() {
        //System.out.println("normalizing positions..");
        for (Node n : nodes) {

            // NORMALIZE RADIUS
            //System.out.println("node "+n.label+" ("+n.category+")");
            //System.out.println(" - radius avant:"+n.radius);


            n.position.set(PApplet.map(n.position.x, minX, maxX, MIN_X, MAX_X),
                     PApplet.map(n.position.y, minY, maxY, MIN_Y, MAX_Y),
                    0.0f);

        }
    }

    public synchronized void normalize() {
        //System.out.println("normalizing..");

        for (Node n : nodes) {
            n.radius = (minRadius == maxRadius)
                    ? MIN_RADIUS
                    : PApplet.map(n.radius,
                    minRadius,
                    maxRadius,
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

            if (false) System.out.println("n.weight = " + "PApplet.map(" + n.weight + ","
                    + minNodeWeight + ", " + maxNodeWeight + "," + NORMALIZED_MIN_NODE_WEIGHT + ", " + NORMALIZED_MAX_NODE_WEIGHT + ");");

            // NORMALIZE WEIGHT

            n.weight =
                    (minNodeWeight == maxNodeWeight)
                    ? NORMALIZED_MIN_NODE_WEIGHT
                    : PApplet.map(n.weight,
                    minNodeWeight, maxNodeWeight,
                    NORMALIZED_MIN_NODE_WEIGHT, NORMALIZED_MAX_NODE_WEIGHT) /*
                    ((NORMALIZED_MAX_NODE_WEIGHT * PApplet.abs(n.weight)) / (
                    PApplet.max(
                    PApplet.abs( minNodeWeight ),PApplet.abs( maxNodeWeight )
                    )
                    ))*/;

            if (false) System.out.println("n.weight = " + n.weight);

            // NORMALIZE WEIGHTS
            for (int k : n.weights.keys().elements()) {
                //System.out.println("  - w1: "+n.weights.get(k));

                float w = (Float) n.weights.get(k);

                w =
                        // si pas de min ni d emax
                        (minEdgeWeight == maxEdgeWeight)
                        ? NORMALIZED_MIN_EDGE_WEIGHT
                        : 
                            ((NORMALIZED_MAX_EDGE_WEIGHT * PApplet.abs(w)) / (PApplet.max(
                        PApplet.abs(minEdgeWeight), PApplet.abs(maxEdgeWeight))));
                         // sinon
                        // entre 0 et NORMALIZED_MAX_EDGE_WEIGHT
                        //(0 < minEdgeWeight && maxEdgeWeight < 1) ?
                        
                       
                // entre 1 et NORMALIZED_MAX_EDGE_WEIGHT
                        /*: PApplet.map(w,
                minEdgeWeight, maxEdgeWeight,
                NORMALIZED_MIN_EDGE_WEIGHT, NORMALIZED_MAX_EDGE_WEIGHT);*/

                n.weights.put(k, w);
                //System.out.println("  - w: "+w);
            }

        }
        minNodeWeight = 0.0f;
        maxNodeWeight = 1.0f;
        minEdgeWeight = 0.0f;
        maxEdgeWeight = 1.0f;

    }

    public PVector getBarycenter(String id) {
        for (Node n : nodes) {
            if (id.equals(n.uuid)) {
                return new PVector(n.position.x, n.position.y, 0);
            }
        }
        return baryCenter;
    }

    public PVector getBarycenter(Collection<String> nodesIds) {
        PVector bary = new PVector(0.0f, 0.0f, 0.0f);
        Float mx = null;
        Float my = null;
        int i = 0;
        for (Node n : nodes) {
            if (!nodesIds.contains(n.uuid)) {
                continue;
            }
            mx = (mx == null) ? n.position.x : mx + n.position.x;
            my = (my == null) ? n.position.y : my + n.position.y;
            i++;
        }
        if (mx != null && my != null) {
            if (i != 0) {
            bary.set(mx / i, my / i, 0);
            }
        }
        return bary;
    }

    public synchronized void computeExtremums() {
        //System.out.println("computing extremums");
        reset();

        Float mx = 0.0f;
        Float my = 0.0f;
        for (Node n : nodes) {
            computeExtremumsKernel(n);
            mx = (mx == null) ? n.position.x : mx + n.position.x;
            my = (my == null) ? n.position.y : my + n.position.y;
        }

        baryCenter.set(0.0f,0.0f,0.0f);
        if (mx != null && my != null) {
            if (nodes.size()!=0) {
                baryCenter.set(mx / nodes.size(), my / nodes.size(), 0);
            }
        }

        aftermath();
        System.out.println(this);
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
        if (n.position.x < minX) {
            minX = n.position.x;
        }
        if (n.position.x > maxX) {
            maxX = n.position.x;
        }
        if (n.position.y < minY) {
            minY = n.position.y;
        }
        if (n.position.y > maxY) {
            maxY = n.position.y;
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

        nbEdges += n.weights.size();

        for (Object weightObj : n.weights.values().elements()) {
            Float weight = (Float) weightObj;
            if (weight > maxEdgeWeight) {
                maxEdgeWeight = weight;
            }
            if (weight < minEdgeWeight) {
                minEdgeWeight = weight;
            }
        }

    }
}
