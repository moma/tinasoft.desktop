/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tinaviz.Node;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class NodeFilter implements Filter {

    private AtomicBoolean enabled = new AtomicBoolean(false);

    public NodeFilter() {
        enabled.set(true);
    }

    protected Node node(View view, Node n) {
        return n;
    }
    
    public List<Node> process(View view, List<Node> input) {
        List<Node> output = new ArrayList<Node>();
        if(!enabled()) {
            return input;
        }
        for (Node n : input) output.add(node(view, n));
        return output;
    }

    public boolean enabled() {
        return enabled.get();
    }

    public void setEnabled(boolean b) {
        enabled.set(b);
    }
}
