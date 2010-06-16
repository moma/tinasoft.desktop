/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import cern.colt.map.OpenIntObjectHashMap;
import eu.tinasoft.services.computing.MathFunctions;
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
public class EdgeWeightRangeHack extends NodeFilter {

    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);


    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {

        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_MIN)) {
            view.properties.put(root + KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root + KEY_MAX)) {
            view.properties.put(root + KEY_MAX, 1.0f);
        }

        NodeList output = new NodeList();

        String category = "";
        try {
            category = (String) session.getView().getProperty("category/category");
        } catch (KeyException ex) {
            Logger.getLogger(EdgeWeightRangeHack.class.getName()).log(Level.SEVERE, null, ex);
        }

        Metrics metrics = input.getMetrics();
        float f = metrics.maxEdgeWeight - metrics.minEdgeWeight;
        //System.out.println("f:" + f);
        //System.out.println("minEdgeWeight:" + metrics.minEdgeWeight + " maxEdgeWeight:" + metrics.maxEdgeWeight);

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
        //System.out.println("min1:" + min + " max1:" + max);
        min = MathFunctions.map(min, 0.0f, 1.0f, metrics.minEdgeWeight, metrics.maxEdgeWeight);
        max = MathFunctions.map(max, 0.0f, 1.0f, metrics.minEdgeWeight, metrics.maxEdgeWeight);

        System.out.println("min2:" + min + " max2:" + max);

        //System.out.println("threshold weight got " + input.size() + " nodes in entry");
        for (Node n : input.nodes) {
            OpenIntObjectHashMap newWeights = new OpenIntObjectHashMap();
            newWeights.ensureCapacity(n.weights.size());
            int[] elems = n.weights.keys().elements();
            for (int k : elems) {
                Float w = (Float) n.weights.get(k);
                if (w == null) {
                    System.out.println(" not kept because null");
                    continue;
                }

                System.out.print(min+" <= "+w+" <= "+max);
                if (n.selected) {
                    newWeights.put(k, w);
                    System.out.println(" kept!");
                }
                else if (n.category.equals(category) && min <= w && w <= max) {
                    newWeights.put(k, w);
                    System.out.println(" kept!");
                } else {
                    System.out.println(" not kept..");
                }
            }
            Node newNode = n.getProxyClone();
            newNode.setWeights(newWeights);
            output.add(newNode);
        }
        return output;
    }
}
