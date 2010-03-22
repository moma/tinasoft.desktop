/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations.filters;


import tinaviz.Node;
import tinaviz.transformations.NodeFilter;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class NodeRadius extends NodeFilter {

    private String KEY_VALUE = "value";

    @Override
    public Node node(Session session, View view, Node n) {
        if (!view.properties.containsKey(root+KEY_VALUE)) {
            view.properties.put(root+KEY_VALUE, 1.0f);
        }

        Object o = view.properties.get(root+KEY_VALUE);
        Float r =  (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;
        //System.out.println("radius: "+root+KEY_VALUE+" = "+r);

        n.radius *= r;
        return n;
    }
}
