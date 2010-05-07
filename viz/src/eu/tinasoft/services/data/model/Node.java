/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;


import cern.colt.map.OpenIntObjectHashMap;
import java.util.HashMap;

import java.util.Map;
import processing.core.PImage;

/**
 *
 * @author jbilcke
 */
public class Node {

    public int id; // id
    public String uuid = "";

    public String label = "label";
    public String shortLabel = "label";
    public float radius = 1.0f; // radius
    public float boxWidth = 1.0f;
    public float boxHeight = 1.0f;
    public float x = 0.0f; // rect xposition
    public float y = 0.0f; // rect yposition
    public float vx = 0.0f;
    public float vy = 0.0f;
    public boolean s = false; // switch
    public int degree = 0;

    public OpenIntObjectHashMap weights = new OpenIntObjectHashMap();

    //public Set<Long> neighbours = new HashSet<Long>(32);
    //public Map<Long, Float> weights = new HashMap<Long, Float>();

    public boolean selected = false;
    public boolean highlighted = false;
    public float weight = 1.0f;
    public String category = "Document";
    public ShapeCategory shape = ShapeCategory.DISK;
    public float r = -1f;
    public float g = -1f;
    public float b = -1f;
    public boolean fixed = false;
    public boolean visibleToScreen = false;
    public float screenX = 0.0f;
    public float screenY = 0.0f;
    public Map<String, Object> attributes = new HashMap<String, Object>();
    public Node original = null;
    public PImage image = null;
    public String imageURL = "http://cssociety.org/tiki-show_user_avatar.php?user=";

    public Node(int uuid, String label, float radius, float x, float y) {

        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
        this.x = x;
        this.y = y;
    }

    public Node(int uuid, String label, float x, float y) {
        this.id = uuid;
        this.uuid = "";
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.x = x;
        this.y = y;
    }

    public Node(int uuid, String label, float radius) {
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
        if (!weights.containsKey(neighbour.id))
            weights.put(neighbour.id, 0.25f); // put a temporary weight
        
    }

    public void addNeighbour(int nuuid) {
        if (!weights.containsKey(nuuid))
            weights.put(nuuid, 0.25f); // put a temporary weight
    }


    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }


    public void cloneDataFrom(Node node) {

        this.id = 0 + node.id;
        this.uuid = "" + node.uuid;
        this.label = ""+ node.label;
        this.shortLabel = ""+ node.shortLabel;
        this.radius = 0f + node.radius;
        this.x = 0f + node.x;
        this.y = 0f + node.y;

        this.vx = 0f + node.vx;
        this.vy = 0f + node.vy;

        this.r = 0f + node.r;
        this.g = 0f + node.g;
        this.b = 0f + node.b;

        screenX = 0f + node.screenX;
        screenY = 0f + node.screenY;

        this.fixed = (node.fixed);
        this.shape = node.shape;
        this.visibleToScreen = (node.visibleToScreen);
        

        this.s = (node.s); // switch
        this.degree = 0 + node.degree;
        this.weight = 0f + node.weight;
        //System.out.println("this weight: "+this.weight+ " node.weight:"+node.weight);
        this.selected = (node.selected);
        this.highlighted = (node.highlighted);
        this.category = ""+ node.category;

        this.weights = new OpenIntObjectHashMap();
        this.weights.ensureCapacity(node.weights.size());
        for (int k : node.weights.keys().elements()) {
            this.weights.put(k, node.weights.get(k));
        }

    }

    public Node getProxyClone() {
        Node node = new Node(this.id);
        // hard copy
        node.cloneDataFrom(this);
        
        // soft copy
        node.x = this.x;
        node.y = this.y;
        
        node.original = this;
        
        return node;
    }
    
    public Node getDetachedClone() {
        Node node = new Node(this.id);
        node.cloneDataFrom(this);
        node.original = null;
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
}
