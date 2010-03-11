/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jbilcke
 */
public class Node {

    public String uuid; // id
    public String label = "label";
    public float radius = 1.0f; // radius
    public float x = 0.0f; // rect xposition
    public float y = 0.0f; // rect yposition
    public float vx = 0.0f;
    public float vy = 0.0f;
    public boolean s = false; // switch
    public int degree = 0;
    public List<Node> neighbours = new ArrayList<Node>();
    public Map<String,Float> weights = new HashMap<String,Float>();
    public boolean selected = false;
    public boolean highlighted = false;
    public float genericity = 1.0f;
    public String category = "none";
    public float r = -1f;
    public float g = -1f;
    public float b = -1f;
    public boolean fixed = false;

    public Map<String,Object> attributes = new HashMap<String,Object>();

    public Node(String uuid, String label,  float radius, float x, float y) {
        this.uuid = uuid;
        this.label = label;
        this.radius = radius;
        this.x = x;
        this.y = y;
    }

    public Node(String uuid, String label,  float x, float y) {
        this.uuid = uuid;
        this.label = label;
        this.x = x;
        this.y = y;

    }

    public Node(String uuid, String label,  float radius) {
        this.uuid = uuid;
        this.label = label;
        this.radius = radius;
    }

    public Node(String uuid, String label) {
        this.uuid = uuid;
        this.label = label;
    }

    public Node(String uuid) {
        this.uuid = uuid;
    }

    public void addNeighbour(Node neighbour) {
        neighbours.add(neighbour);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }


    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void update(Node node) {
        this.uuid = node.uuid;
        this.label = node.label;
        this.radius = node.radius;
        this.x = node.x;
        this.y = node.y;
        this.vx = node.vx;
        this.vy = node.vy;
        this.neighbours = node.neighbours;
        this.s = node.s; // switch
        this.degree = node.degree;
        this.genericity = node.genericity;
        this.selected = node.selected;
        this.highlighted = node.highlighted;
        this.category = node.category;
        this.weights = node.weights;
    }


}
