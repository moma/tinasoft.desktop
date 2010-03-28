/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.List;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;

/**
 *
 * @author jbilcke
 */
public class WeightSize extends NodeFilter {

    private String KEY_VALUE = "value";

    @Override
    public List<Node> process(Session session, View view, List<Node> input) {
        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_VALUE)) {
            view.properties.put(root + KEY_VALUE, 1.0f);
        }

        Object o = view.properties.get(root + KEY_VALUE);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

        for (Node n : input) {
            for (Long k : n.weights.keySet()) {
                n.weights.put(k,n.weights.get(k) * r);
            }
        }
        return input;
    }
}
