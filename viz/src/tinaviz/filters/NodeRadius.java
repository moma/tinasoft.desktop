/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;


import tinaviz.Node;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class NodeRadius extends NodeFilter {

    @Override
    public Node node(View view, Node n) {
        if (!view.properties.containsKey("node/radius")) {
            view.properties.put("node/radius", 1.0f);
        }

        Object o = view.properties.get("node/radius");
        Float r =  (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;

        n.radius *= r;
        return n;
    }
}
