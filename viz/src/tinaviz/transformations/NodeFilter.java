/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.transformations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tinaviz.Node;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class NodeFilter implements Filter {

    protected AtomicBoolean enabled = new AtomicBoolean(false);
    protected String root = "/";
    
    public NodeFilter() {
        enabled.set(true);
        System.out.println("Filter "+this+" created!");
    }

    protected Node node(Session session, View view, Node n) {
        return n;
    }
    
    public List<Node> process(Session session, View view, List<Node> input) {
        List<Node> output = new LinkedList<Node>();
        if(!enabled()) {
            return input;
        }
        for (Node n : input) output.add(node(session, view, n));
        return output;
    }

    public boolean enabled() {
        return enabled.get();
    }

    public void setEnabled(boolean b) {
        enabled.set(b);
    }

    public void setRoot(String root) {
        this.root = root + "/";
    }
    public String getRoot() {
        return root;
    }
}
