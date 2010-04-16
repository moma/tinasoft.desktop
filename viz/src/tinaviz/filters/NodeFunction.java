/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.LinkedList;
import java.util.List;
import processing.core.PApplet;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;

/**
 *
 * @author jbilcke
 */
public class NodeFunction extends NodeFilter {

    private String KEY_VALUE = "value";
    private String KEY_SOURCE = "none";
    private String KEY_TARGET = "none";

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_VALUE)) {
            view.properties.put(root + KEY_VALUE, 0.5f);
        }
        if (!view.properties.containsKey(root + KEY_SOURCE)) {
            view.properties.put(root + KEY_SOURCE, "none");
        }
          if (!view.properties.containsKey(root + KEY_TARGET)) {
            view.properties.put(root + KEY_TARGET, "none");
        }

        Object o = view.properties.get(root + KEY_VALUE);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

       String source = (String) view.properties.get(root+KEY_SOURCE);
       String target = (String) view.properties.get(root+KEY_TARGET);


        for (Node n : input.nodes) {
             //System.out.println(" node funct: "+(n.radius * n.weight)+ " = "+n.radius+" * "+n.weight);
              n.radius = n.radius * n.weight;
       
               // n.radius = 5 + n.radius * (PApplet.sqrt(5)*n.weight);
              //  n.radius =  (float) Math.pow((double)n.radius, (double)(1.0f/5f));
 
        }
        return input;
    }
}
