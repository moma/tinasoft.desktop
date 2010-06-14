/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import eu.tinasoft.services.data.model.Metrics;
import eu.tinasoft.services.data.model.NodeList;

import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import java.security.KeyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbilcke
 */
public class NodeWeightRangeHack extends NodeFilter {

    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        NodeList output = new NodeList();

        Metrics metrics = input.getMetrics();


        String category = "";
        try {
            category = (String) session.getView().getProperty("category/category");
        } catch (KeyException ex) {
            Logger.getLogger(NodeWeightRangeHack.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_MIN)) {
            view.properties.put(root + KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root + KEY_MAX)) {
            view.properties.put(root + KEY_MAX, 1.0f);
        }

        float f = metrics.maxNodeWeight - metrics.minNodeWeight;

        Object o = view.properties.get(root + KEY_MIN);
        min = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

        o = view.properties.get(root + KEY_MAX);
        max = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

        min = min * f + metrics.minNodeWeight;

        max = max * f + metrics.minNodeWeight;

        for (Node n : input.nodes) {
            //System.out.println("genericity: ["+min+" <= "+n.weight+" <= "+max);

            if (n.selected) {
                 output.add(n.getProxyClone());
            }
            else if (n.category.equals(category) && min <= n.weight && n.weight <= max) {
                output.add(n.getProxyClone());
            }
        }
        return output;
    }
}
