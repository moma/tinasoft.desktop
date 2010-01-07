/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbilcke
 */
public class Node {

    public String uuid; // id
    public String label = "label";
    public double radius = 1.0f; // radius
    public double x = 0.0f; // rect xposition
    public double y = 0.0f; // rect yposition
    public double vx = 0.0f;
    public double vy = 0.0f;
    public double vizx = 0.0f;
    public double vizy = 0.0f;
    public double vizradius = 1.0f;
    public boolean s = false; // switch
    public int degree = 0;
    public List<Node> neighbours = new ArrayList<Node>();

    public Node(String uuid, String label, double radius, double x, double y) {
        this.uuid = uuid;
        this.label = label;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.vizx = x;
        this.vizy = y;
        this.vizradius = radius;
    }

    public Node(String uuid, String label, double x, double y) {
        this.uuid = uuid;
        this.label = label;
        this.x = x;
        this.y = y;
        this.vizx = x;
        this.vizy = y;
    }

    public Node(String uuid, String label, double radius) {
        this.uuid = uuid;
        this.label = label;
        this.radius = radius;
        vizradius = radius;
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

    public void update(Node node) {
        this.uuid = node.uuid;
        this.label = node.label;
        this.radius = node.radius;
        this.x = node.x;
        this.y = node.y;
        this.vx = node.vx;
        this.vy = node.vy;
        this.vizx = node.vizx;
        this.vizy = node.vizy;
        this.vizradius = node.vizradius;
        this.neighbours = node.neighbours;
        this.s = node.s; // switch
        this.degree = node.degree;
    }
}
