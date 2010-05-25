/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.NodeList;
import java.util.List;
import processing.core.PApplet;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class Output extends NodeFilter {

    private String KEY_NODE_SIZE_RATIO = "nodeSizeRatio";

    // the range of the sliders
    public float MIN_RADIUS_MAGNIFIER = 0.1f;
    public float MAX_RADIUS_MAGNIFIER = 3.0f;

    public float RADIUS_MIN = 5.0f;
    public float RADIUS_MAX = 20.0f;

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_NODE_SIZE_RATIO)) {
            view.properties.put(root + KEY_NODE_SIZE_RATIO, 0.125f);
        }

        Object o = view.properties.get(root + KEY_NODE_SIZE_RATIO);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

       r = PApplet.map(r,0.0f,1.0f,MIN_RADIUS_MAGNIFIER, MAX_RADIUS_MAGNIFIER);
       r = 1.0f;

            /*
            for (Object w : n.weights.values().elements()) {

                 w = (Float) (
                         (MAX_SCREEN_EDGE_WEIGHT * PApplet.abs((Float)w))
                         /
                         (PApplet.max(
                        PApplet.abs(input.minEdgeWeight),
                        PApplet.abs(input.maxEdgeWeight)))
                     );

               // w = (Float) PApplet.map((Float)w,input.,1.0f,MIN_SCREEN_EDGE_WEIGHT, MAX_SCREEN_EDGE_WEIGHT);
            }
             */
       //System.out.println("radius magnifier: "+r);

        for (Node n : input.nodes) {

            n.radius = PApplet.map(n.radius * r, input.minRadius, input.maxRadius, RADIUS_MIN, RADIUS_MAX);
            //System.out.println(n.radius + "= PApplet.map("+n.radius * r+", "+input.minRadius+", "+input.maxRadius+", "+RADIUS_MIN+", "+RADIUS_MAX+");");

        }
        return input;
    }
}
