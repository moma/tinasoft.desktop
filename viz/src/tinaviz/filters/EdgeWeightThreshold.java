/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tinaviz.Node;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class EdgeWeightThreshold extends NodeFilter {

    private String KEY_MIN = "threshold/weight/min";
    private String KEY_MAX = "threshold/weight/max";
    @Override
    public Node node(View view, Node n) {

        if (!view.properties.containsKey(KEY_MIN)) {
            view.properties.put(KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(KEY_MAX)) {
            view.properties.put(KEY_MAX, 1.0f);
        }

       // System.out.println("min:"+view.graph.metrics.minWeight+" max:"+view.graph.metrics.maxWeight);

        float f = view.graph.metrics.maxWeight - view.graph.metrics.minWeight;
       // System.out.println("f:"+f);

        Object o = view.properties.get(KEY_MIN);
        Float min =   (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;
        min = min * f + view.graph.metrics.minWeight;

        o = view.properties.get(KEY_MAX);
        Float max =  (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;
        max = max * f + view.graph.metrics.minWeight;

        //System.out.println("fmin:"+min+" fmax:"+max);
        Set<String> newNeighbours = new HashSet<String>(n.neighbours.size());

        for (String k : n.neighbours) {

            Float w = n.weights.get(k);

            if (w == null) {
                System.out.println("weight null for <"+n+","+k+">");
                continue;
            }
            if (min <= w && w <= max) {
                newNeighbours.add(k);
            } else {
                n.weights.remove(n.uuid);
            }
        }
        n.neighbours = newNeighbours;
        return n;
    }
}
