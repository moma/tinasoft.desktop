/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.data.transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class NodeFilter implements Filter {

    protected AtomicBoolean enabled = new AtomicBoolean(false);
    protected String root = "/";
    
    public NodeFilter() {
        enabled.set(true);
        // System.out.println("Filter "+this+" created!");
    }

    protected Node node(Session session, View view, Node n) {
        return n;
    }
    
    public NodeList preProcessing(Session session, View view, NodeList input) {
        NodeList output = new NodeList();
        if(!enabled()) {
            return input;
        }
        for (Node n : input.nodes) {
            Node n2 = node(session, view, n);
            if (n2 != null) output.add(n2);
        }
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
