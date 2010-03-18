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
    public String category = "none";
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
        this.original = this;
    }

    public Node(String uuid, String label,  float x, float y) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.x = x;
        this.y = y;
        this.original = this;

    }

    public Node(String uuid, String label,  float radius) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.radius = radius;
        this.original = this;
    }

    public Node(String uuid, String label) {
        this.uuid = uuid;
        this.label = label;
        this.shortLabel = reduceLabel(label, 40);
        this.original = this;
    }

    public Node(String uuid) {
        this.uuid = uuid;
        this.original = this;
    }

    public Node(Node n) {
        this.original = n;
        update(n);
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

    public void update(Node node) {

        this.uuid = new String(node.uuid);
        this.label = new String(node.label);
        this.shortLabel = new String(node.shortLabel);
        this.radius = new Float(node.radius);
        this.x = new Float(node.x);
        this.y = new Float(node.y);

        this.vx = new Float(node.vx);
        this.vy = new Float(node.vy);

        this.r = new Float(node.r);
        this.g = new Float(node.g);
        this.b = new Float(node.b);

        screenX = node.screenX;
        screenY = node.screenY;

        this.fixed = (node.fixed);
        this.shape = node.shape;
        this.visibleToScreen = node.visibleToScreen;
        this.neighbours = new HashSet<String>(node.neighbours.size());
        for (String k : node.neighbours) {
            this.neighbours.add(new String(k));
        }
        this.s = (node.s); // switch
        this.degree = new Integer (node.degree);
        this.genericity = new Float(node.genericity);
        this.selected = (node.selected);
        this.highlighted = (node.highlighted);
        this.category = new String(node.category);
        this.weights = new HashMap<String,Float>();
        for (String k : node.weights.keySet()) {
            this.weights.put(new String(k),new Float(node.weights.get(k)));
        }
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
