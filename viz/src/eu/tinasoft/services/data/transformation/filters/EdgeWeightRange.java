/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import cern.colt.map.OpenIntObjectHashMap;
import eu.tinasoft.services.computing.MathFunctions;
import eu.tinasoft.services.data.model.Metrics;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class EdgeWeightRange extends NodeFilter {

    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);

    @Override
    public Node node(Session session, View view, Node n) {

        //System.out.println("fmin:"+min+" fmax:"+max);
        OpenIntObjectHashMap newWeights = new OpenIntObjectHashMap();
        newWeights.ensureCapacity(n.weights.size());

        int[] elems = n.weights.keys().elements();

        for (int k : elems) {

            Float w = (Float) n.weights.get(k);

            if (w == null) {
                //System.out.println("weight null for <"+n+","+k+">");
                continue;
            }

            //System.out.println("w: "+w);
            if (min <= w && w <= max) {
                newWeights.put(k,w);
               // System.out.println("ADDED EDGE "+w+" MIN: "+min+" MAX: "+max);
                // .. and do not remove from weights
            } else {
                // .. and do not add to neighbours
                //System.out.println("REMOVED EDGE "+w+" MIN: "+min);
                //n.weights.removeKey(k);
            }
        }

        n.weights = newWeights;
        return n;
    }

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


        Metrics metrics = input.getMetrics();
        float f = metrics.maxEdgeWeight - metrics.minEdgeWeight;
        System.out.println("f:" + f);
        System.out.println("minEdgeWeight:"+metrics.minEdgeWeight+" maxEdgeWeight:"+metrics.maxEdgeWeight);

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
        
        min = MathFunctions.map(min, 0.0f,1.0f, metrics.minEdgeWeight, metrics.maxEdgeWeight);
        max = MathFunctions.map(max, 0.0f,1.0f, metrics.minEdgeWeight, metrics.maxEdgeWeight);

        System.out.println("min:"+min+" max:"+max);

        System.out.println("threshold weight got "+input.size()+" nodes in entry");
        for (Node n : input.nodes) {
            node(session, view, n);
        }
        return input;
    }
}
