/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;

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
        Set<Long> newNeighbours = new HashSet<Long>(n.neighbours.size());

        for (Long k : n.neighbours) {

            Float w = n.weights.get(k);

            if (w == null) {
                //System.out.println("weight null for <"+n+","+k+">");
                continue;
            }
            if (min <= w && w <= max) {
                newNeighbours.add(k);
                // .. and do not remove from weights
            } else {
                // .. and do not add to neighbours
                n.weights.remove(n.uuid);
            }
        }
        n.neighbours = newNeighbours;
        return n;
    }

    @Override
    public NodeList process(Session session, View view, NodeList input) {

        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_MIN)) {
            view.properties.put(root + KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root + KEY_MAX)) {
            view.properties.put(root + KEY_MAX, 1.0f);
        }


        float f = input.maxEdgeWeight - input.minEdgeWeight;
        //System.out.println("f:" + f);

        Object o = view.properties.get(root + KEY_MIN);
        min = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        min = min * f + input.minEdgeWeight;

        o = view.properties.get(root + KEY_MAX);
        max = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        max = max * f + input.minEdgeWeight;
        // System.out.println("minEdgeWeight:"+input.minEdgeWeight+" maxEdgeWeight:"+input.maxEdgeWeight);
        //System.out.println("min:"+min+" max:"+max);

        //System.out.println("threshold weight got "+input.size()+" nodes in entry");
        for (Node n : input.nodes) {
            node(session, view, n);
        }
        return input;
    }
}
