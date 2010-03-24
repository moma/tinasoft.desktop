/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import tinaviz.model.ShapeCategory;

/**
 *
 * @author jbilcke
 */
public class Node {

    public String uuid; // id
    public String label = "label";
    public String shortLabel = "label";
    public float radius = 1.0f; // radius
    public float x = 0.0f; // rect xposition
    public float y = 0.0f; // rect yposition
    public float vx = 0.0f;
    public float vy = 0.0f;
    public boolean s = false; // switch
    public int degree = 0;
    public Set<String> neighbours = new HashSet<String>(32);
    public Map<String,Float> weights = new HashMap<String,Float>();
    public boolean selected = false;
    public boolean highlighted = false;
    public float genericity = 1.0f;
    public String category = "Document";
    public ShapeCategory shape = ShapeCategory.DISK;
    public float r = -1f;
    public float g = -1f;
    public float b = -1f;
    public boolean fixed = false;
    public boolean visibleToScreen = false;
    public float screenX = 0.0f;
    public float screenY = 0.0f;

    public Map<String,Object> attributes = new HashMap<String,Object>();
    public Node original = null;

    public Node(String uuid, String label,  float radius, float x, float y) {

        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
        this.x = x;
        this.y = y;
    }

    public Node(String uuid, String label,  float x, float y) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.x = x;
        this.y = y;
    }

    public Node(String uuid, String label,  float radius) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
    }

    public Node(String uuid, String label) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
    }

    public Node(String uuid) {
        this.uuid = uuid;
    }

    public Node(Node n) {
        cloneDataFrom(n);
        original = n;
    }

    public void addNeighbour(Node neighbour) {
        neighbours.add(neighbour.uuid);
    }
    public void addNeighbour(String nuuid) {
        neighbours.add(nuuid);
    }
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }


    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void cloneDataFrom(Node node) {

        this.uuid = node.uuid;
        this.label = node.label;
        this.shortLabel = node.shortLabel;
        this.radius = node.radius;
        this.x = node.x;
        this.y = node.y;

        this.vx = node.vx;
        this.vy = node.vy;

        this.r = node.r;
        this.g = node.g;
        this.b = node.b;

        screenX = node.screenX;
        screenY = node.screenY;

        this.fixed = (node.fixed);
        this.shape = node.shape;
        this.visibleToScreen = node.visibleToScreen;
        this.neighbours = new HashSet<String>(node.neighbours.size());
        for (String k : node.neighbours) {
            this.neighbours.add(k);
        }
        this.s = (node.s); // switch
        this.degree = node.degree;
        this.genericity = node.genericity;
        this.selected = (node.selected);
        this.highlighted = (node.highlighted);
        this.category = node.category;
        this.weights = new HashMap<String,Float>();
        for (String k : node.weights.keySet()) {
            this.weights.put(k,node.weights.get(k));
        }
    }
    public Node getProxyClone() {
        return new Node(this);
    }
    public Node getDetachedClone() {
        Node node = new Node(this.uuid);
        node.cloneDataFrom(this);
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
