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
    public float MIN_RADIUS_MAGNIFIER = 1.0f;
    public float MAX_RADIUS_MAGNIFIER = 100.0f;

    @Override
    public List<Node> process(Session session, View view, List<Node> input) {
        if (!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root + KEY_VALUE)) {
            view.properties.put(root + KEY_VALUE, 1.0f);
        }

        Object o = view.properties.get(root + KEY_VALUE);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

       r = PApplet.map(r,0.0f,1.0f,MIN_RADIUS_MAGNIFIER, MAX_RADIUS_MAGNIFIER);

        for (Node n : input) {
            // we dot not want a "zero sized" node
            n.radius = 1 + n.radius * r;

        }
        return input;
    }
}
