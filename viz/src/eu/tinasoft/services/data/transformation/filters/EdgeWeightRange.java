/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import cern.colt.map.OpenIntObjectHashMap;
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

        for (int k : n.weights.keys().elements()) {

            Float w = (Float) n.weights.get(k);


            if (w == null) {
                //System.out.println("weight null for <"+n+","+k+">");
                continue;
            }

            if (min <= w && w <= max) {
                newWeights.put(k,w);
                //System.out.println("ADDED EDGE "+w+" MIN: "+min);
                // .. and do not remove from weights
            } else {
                // .. and do not add to neighbours
                //System.out.println("REMOVED EDGE "+w+" MIN: "+min);
                n.weights.removeKey(n.id);
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


        float f = input.getMetrics().maxEdgeWeight - input.getMetrics().minEdgeWeight;
        //System.out.println("f:" + f);
        //System.out.println("minEdgeWeight:"+input.minEdgeWeight+" maxEdgeWeight:"+input.maxEdgeWeight);

        Object o = view.properties.get(root + KEY_MIN);
        min = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        //min = min * f + input.minEdgeWeight;

        o = view.properties.get(root + KEY_MAX);
        max = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        //max = max * f + input.minEdgeWeight;

        //System.out.println("min:"+min+" max:"+max);

        //System.out.println("threshold weight got "+input.size()+" nodes in entry");
        for (Node n : input.nodes) {
            node(session, view, n);
        }
        return input;
    }
}
