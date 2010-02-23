/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author jbilcke
 */
public class Network {

    public Map<String, tinaviz.Node> storedNodes;
    public Metrics metrics;

    public void putNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        } else {
            storedNodes.get(node.uuid).update(node);
        }
    }

    public void addNode(tinaviz.Node node) {
        if (!storedNodes.containsKey(node.uuid))
            storedNodes.put(node.uuid, node);
    }

    public void updateNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid))
            storedNodes.get(node.uuid).update(node);
    }

    public void addNeighbour(tinaviz.Node node1, tinaviz.Node node2) {
        if (storedNodes.containsKey(node1.uuid)) {
            storedNodes.get(node1.uuid).addNeighbour(node2);
        } else {
            node1.addNeighbour(node2);
            storedNodes.put(node1.uuid, node1);

        }
    }

    public void clear() {
        storedNodes.clear();
    }
}
