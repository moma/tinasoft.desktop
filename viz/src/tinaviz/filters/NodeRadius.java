/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

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
public class NodeRadius extends NodeFilter {

    private String KEY_VALUE = "value";

    // the range of the sliders
    public float MIN_RADIUS_MAGNIFIER = 0.1f;
    public float MAX_RADIUS_MAGNIFIER = 3.0f;

    @Override
    public NodeList process(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_VALUE)) {
            view.properties.put(root + KEY_VALUE, 0.125f);
        }

        Object o = view.properties.get(root + KEY_VALUE);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

       r = PApplet.map(r,0.0f,1.0f,MIN_RADIUS_MAGNIFIER, MAX_RADIUS_MAGNIFIER);

       //System.out.println("radius magnifier: "+r);

        for (Node n : input.nodes) {
            n.radius = n.radius * r;
        }
        return input;
    }
}
