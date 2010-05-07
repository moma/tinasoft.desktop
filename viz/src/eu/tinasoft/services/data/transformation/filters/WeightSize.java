/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.NodeList;
import java.util.List;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class WeightSize extends NodeFilter {

    private String KEY_VALUE = "value";

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
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

        for (Node n : input.nodes) {
            for (int k : n.weights.keys().elements()) {
                n.weights.put(k,((Float) n.weights.get(k)) * r);
            }
        }
        return input;
    }
}
