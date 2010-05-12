/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.NodeList;
import java.util.LinkedList;
import java.util.List;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import processing.core.*;

/**
 *
 * @author jbilcke
 */
public class NodeWeightRange extends NodeFilter {

    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        NodeList output = new NodeList();

        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_MIN)) {
            view.properties.put(root + KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root + KEY_MAX)) {
            view.properties.put(root + KEY_MAX, 1.0f);
        }

        float f = input.maxNodeWeight - input.minNodeWeight;

        Object o = view.properties.get(root + KEY_MIN);
        min = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        min = min * f + input.minNodeWeight;

        o = view.properties.get(root + KEY_MAX);
        max = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;
        max = max * f + input.minNodeWeight;
        
        //System.out.println("minNodeWeight:"+input.minNodeWeight+" maxNodeWeight:"+input.maxNodeWeight);
        //System.out.println("min:"+min+" max:"+max);
        for (Node n : input.nodes) {
            //System.out.println("genericity: ["+min+" <= "+n.weight+" <= "+max);

            if ((min <= n.weight && n.weight <= max) ) {
                output.add(n);
            }
        }
        return output;
    }
}
