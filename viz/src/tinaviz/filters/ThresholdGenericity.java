/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.LinkedList;
import java.util.List;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;
import processing.core.*;

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

        // HACK HACK HACK ** POUR DAVID DEMO FET60 ** HACK HACK HACK HACK
        view.graph.metrics.maxGenericity = 1.0f;

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
             // System.out.println("genericity: ["+min+" <= "+n.genericity+" <= "+max);

            if (min <= n.genericity && n.genericity <= max) output.add(n);
        }
        return output;
    }
}
