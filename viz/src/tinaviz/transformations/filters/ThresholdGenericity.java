/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import tinaviz.Node;
import tinaviz.transformations.NodeFilter;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class ThresholdGenericity extends NodeFilter {


    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);

    @Override
        public List<Node> process(Session session, View view, List<Node> input) {
        List<Node> output = new LinkedList<Node>();
        if(!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root+KEY_MIN)) {
            view.properties.put(root+KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root+KEY_MAX)) {
            view.properties.put(root+KEY_MAX, 1.0f);
        }

        float f = view.graph.metrics.maxGenericity - view.graph.metrics.minGenericity;

        Object o = view.properties.get(root+KEY_MIN);
        min =   (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;
        min = min * f + view.graph.metrics.minGenericity;

        o = view.properties.get(root+KEY_MAX);
        max =  (o instanceof Integer)
                   ? new Float((Integer)o)
                   : (o instanceof Double)
                   ? new Float((Double)o)
                   : (Float) o;
        max = max * f + view.graph.metrics.minGenericity;

        for (Node n : input) {
            // System.out.println("n.genericity: "+n.genericity);
            if (min <= n.genericity && n.genericity <= max) output.add(n);
        }
        return output;
    }
}
