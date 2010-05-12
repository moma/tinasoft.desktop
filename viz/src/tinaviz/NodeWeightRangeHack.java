/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.LinkedList;
import java.util.List;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import processing.core.*;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.debug.Console;

/**
 *
 * @author jbilcke
 */
public class NodeWeightRangeHack extends NodeFilter {

    private String KEY_MIN = "min";
    private String KEY_MAX = "max";
    private String KEY_EXCEPT = "except";
    private Float min = new Float(0.0f);
    private Float max = new Float(1.0f);
    private Long except = -1L;

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        NodeList output = new NodeList();

        if (!view.properties.containsKey(root + KEY_MIN)) {
            view.properties.put(root + KEY_MIN, 0.0f);
        }

        if (!view.properties.containsKey(root + KEY_MAX)) {
            view.properties.put(root + KEY_MAX, 1.0f);
        }

        if (!view.properties.containsKey(root + KEY_EXCEPT)) {
            view.properties.put(root + KEY_EXCEPT, -1L);
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



        String cat = "";

        o = view.properties.get(root + KEY_EXCEPT);
        if (o == null) {
            //  System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return input;
        }


        if (o instanceof String) {
            if (((String) o).contains("::")) {
                cat = ((String) o).split("::")[0];

                except = Long.parseLong(((String) o).split("::")[1]);
            } else if (((String) o).isEmpty()) {
                //
            } else {
                Console.error("Invalid ID: " + (String) o);
                return input;
            }


        } else {
            Console.error("bad type for " + root + KEY_EXCEPT + ", expected this pattern: '[a-zA-Z]+::[0_9]+'");
            return input;
        }

        //System.out.println("minNodeWeight:"+input.minNodeWeight+" maxNodeWeight:"+input.maxNodeWeight);
        //System.out.println("min:"+min+" max:"+max);
        for (Node n : input.nodes) {
            //System.out.println("genericity: ["+min+" <= "+n.weight+" <= "+max);

            if ((min <= n.weight && n.weight <= max)) {
                output.add(n);
            }
        }
        return output;
    }
}
