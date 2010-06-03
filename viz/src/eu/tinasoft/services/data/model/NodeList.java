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

    public List<Node> nodes;
    private Comparator comp = new SelectedComparator();
    private Metrics metrics = new Metrics();

    public NodeList(NodeList nodeList) {
        reset();
        //System.out.println("copying nodes from another node list, but clearing state..");
        nodes = new LinkedList<Node>();
        for (Node n : nodeList.nodes) {
           add(n);
        }
        computeMetrics();
    }

    NodeList(Collection<Node> values) {
        reset();
        for (Node n : values) {
            add(n);
        }
        computeMetrics();
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
            if (!n.selected) {
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

    public void selectNodes(List<String> ids) {
        for (String id : ids) {
            selectNode(id);
        }
    }

    public void unselect(String id) {
        for (Node n : nodes) {
            if (n.id == id.hashCode()) {
                n.selected = false;
            }
        }
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
        System.out.println("NodeList.getNeighbourhoodAsJSON() = "+writer.toString() );
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
        System.out.println( writer.toString() + "= NodeList.getSelectedNodesAsJSON()");
        return writer.toString();
    }

    public PVector getSelectedNodesCenter() {
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        if (nodes.size() > 0) {
            for (Node n : nodes) {
                if (!n.selected) {
                    continue;
                }
                maxX = PApplet.max(maxX, n.position.x);
                minX = PApplet.min(minX, n.position.x);
                maxY = PApplet.max(maxY, n.position.y);
                minY = PApplet.min(minY, n.position.y);
            }
        } else {
            return metrics.center;
        }
        return new PVector(minX + (maxX - minX) / 2.0f, minY + (maxY - minY) / 2.0f, 0.0f);
    }

    public void unselectAll() {
        for (Node n : nodes) {
            n.selected = false;
        }
    }

    public boolean hasNode(int nb) {

        for (Node n : nodes) {
            if (n.id==nb) return true;
        }
        return false;
    }

    public void selectNode(int id) {
        setSelectNode(id,true);
    }
    public void setSelectNode(int id, boolean value) {
        for (Node n : nodes) {
            if (n.id==id) n.selected = value;
        }
    }

    public Metrics computeMetrics() {
       metrics = NodeListNormalizer.computeMetrics(nodes);
       return metrics;
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

    public NodeList(List<Node> nodes) {
        this.nodes = nodes;
        metrics.reset();
    }

    public NodeList() {
        nodes = new LinkedList<Node>();
        metrics.reset();
    }

    public void reset() {
        nodes = new LinkedList<Node>();
        metrics.reset();
    }

    public void addAll(NodeList nodes) {
        this.nodes.addAll(nodes.nodes);
        metrics.nbNodes+=nodes.size();
    }

    public void sortBySelectionStatus() {
        Collections.sort(nodes, comp);
    }

    public int size() {
        return nodes.size();
    }

    @Override
    public String toString() {
        return metrics.toString();
    }

    public void add(Node node) {
        nodes.add(node);
        metrics.nbNodes++;
    }

    public Node get(int i) {
        return nodes.get(i);
    }

    public synchronized void normalizePositions() {
        nodes = NodeListNormalizer.normalize(nodes, metrics);
        computeMetrics();
    }

    public synchronized void normalize() {
        nodes = NodeListNormalizer.normalize(nodes, metrics);
        computeMetrics();
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public PVector getBarycenter(String id) {
        for (Node n : nodes) {
            if (id.equals(n.uuid)) {
                return new PVector(n.position.x, n.position.y, 0);
            }
        }
        return metrics.baryCenter;
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

  
    //
}
