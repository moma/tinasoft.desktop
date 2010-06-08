/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import cern.colt.map.OpenIntObjectHashMap;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.formats.json.JSONException;
import eu.tinasoft.services.formats.json.JSONStringer;
import eu.tinasoft.services.formats.json.JSONWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class Node implements Comparable {

    public int id; // id
    public String uuid = "";
    public String label = "label";
    public String shortLabel = "label";
    public float radius = 1.0f; // radius
    public float boxWidth = 1.0f;
    public float boxHeight = 1.0f;
    public PVector position = new PVector();

    public boolean     s = false; // switch
    public int degree = 0;
    public OpenIntObjectHashMap weights = new OpenIntObjectHashMap();

    // for each weight: -> it's position in the calculated distribution
    public OpenIntObjectHashMap weightsDistribution = new OpenIntObjectHashMap();
    
    //public Set<Long> neighbours = new HashSet<Long>(32);
    //public Map<Long, Float> weights = new HashMap<Long, Float>();
    public boolean selected = false;
    public boolean isFirstHighlight = false;
    public float weight = 1.0f;
    public String category = "Document";
    public ShapeCategory shape = ShapeCategory.DISK;
    public float r = -1f;
    public float g = -1f;
    public float b = -1f;
    public boolean fixed = false;
    public boolean visibleToScreen = false;
    public PVector screenPosition = new PVector();

    public Map<String, Object> attributes = new HashMap<String, Object>();
    public Node original = null;
    public PImage image = null;
    public String imageURL = "http://cssociety.org/tiki-show_user_avatar.php?user=";
    public boolean isSecondHighlight = false;


    public Node(int uuid, String label, float radius, Float x, Float y) {

        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
        this.position = new PVector(x,y);
    }

    public Node(int uuid, String label, Float x, Float y) {
        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.position = new PVector(x,y);
    }

    public Node(int uuid, String label, Float radius) {
        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
    }

    public Node(int uuid, String label) {
        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
    }

    public Node(int uuid) {
        this.id = uuid;
        this.uuid = "";
    }

    public Node(Node n) {
        cloneDataFrom(n);
        original = n;
    }

    public void addNeighbour(Node neighbour, Float weight) {
        weights.put(neighbour.id, weight);
    }

    public void addNeighbour(int nuuid, Float weight) {
        weights.put(nuuid, weight);
    }

    public void addNeighbour(Node neighbour) {
        if (!weights.containsKey(neighbour.id)) {
            weights.put(neighbour.id, 0.25f); // put a temporary weight
        }
    }

    public void addNeighbour(int nuuid) {
        if (!weights.containsKey(nuuid)) {
            weights.put(nuuid, 0.25f); // put a temporary weight
        }
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void cloneDataFrom(Node node) {

        this.original = node;
        
        this.id = 0 + node.id;
        this.uuid = "" + node.uuid;
        this.label = "" + node.label;
        this.shortLabel = "" + node.shortLabel;
        this.radius = new Float(node.radius);
        this.position = node.position;
        this.r = new Float(node.r);
        this.g = new Float(node.g);
        this.b = new Float(node.b);

        this.fixed = (node.fixed);
        this.shape = node.shape;
        this.visibleToScreen = (node.visibleToScreen);

        this.s = (node.s); // switch
        this.degree = 0 + node.degree;
        this.weight = 0f + node.weight;
        //System.out.println("this weight: "+this.weight+ " node.weight:"+node.weight);
        this.selected = (node.selected);
        this.isFirstHighlight = (node.isFirstHighlight);
        this.category = "" + node.category;

        this.weights = new OpenIntObjectHashMap();
        this.weights.ensureCapacity(node.weights.size());
        for (int k : node.weights.keys().elements()) {
            this.weights.put(k, node.weights.get(k));
        }
        
        for (Entry<String,Object> e : node.attributes.entrySet()) {
            this.attributes.put(e.getKey(), e.getValue());
        }
    }

    public Node getProxyClone() {
        Node node = new Node(this.id);
        node.cloneDataFrom(this);
        node.original = this;
        return node;
    }

    public Node getDetachedClone() {
        Node node = new Node(this.id);
        node.cloneDataFrom(this);
        node.original = null;
        node.position = new PVector(0.0f+position.x,0.0f+position.y);
        return node;
    }

    public String reduceLabel(String label) {
        return reduceLabel(label, 30);
    }

    public String reduceLabel(String label, int len) {
        return (label.length() > len)
                ? label.substring(0, len - 2) + ".."
                : label;
    }

    public Map<String, Object> getAttributes() {
        return (original == null) ? this.attributes : original.attributes;
    }
    public Object valueEncoder(Object o) throws UnsupportedEncodingException {
        return (o instanceof String) ? URLEncoder.encode((String)o, "UTF-8") : o;
    }

    public String getAttributesAsJSON() {

        JSONWriter writer = null;
        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            return "{}";
        }


        try {
            // writer.key("category").value(category).endObject();
            writer.key("x").value(position.x);
            writer.key("y").value(position.y);

            for (Entry<String, Object> entry : getAttributes().entrySet()) {
                try {
                    //System.out.println(" writing " + ((String) entry.getKey()) + " => " + entry.getValue());
                    writer.key(entry.getKey()).value(valueEncoder(entry.getValue()));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (JSONException jSONException) {
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
        System.out.println("json: " + writer.toString());
        return writer.toString();
    }

    @Override
    public int compareTo(Object o) {
        return label.compareTo(((Node)o).label);
    }

    public void setWeights(OpenIntObjectHashMap newWeights) {
        weights = newWeights;
    }
}
