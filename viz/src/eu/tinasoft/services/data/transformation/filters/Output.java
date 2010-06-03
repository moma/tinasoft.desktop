/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.Metrics;
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
    public static float MIN_RADIUS_MAGNIFIER = 0.1f;
    public static float MAX_RADIUS_MAGNIFIER = 3.0f;
    public static float RADIUS_MIN = 3.0f;
    public static float RADIUS_MAX = 20.0f;

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        NodeList output = new NodeList();

        if (!view.properties.containsKey(root + KEY_NODE_SIZE_RATIO)) {
            view.properties.put(root + KEY_NODE_SIZE_RATIO, 0.125f);
        }

        Metrics metrics = input.computeMetrics();
        Object o = view.properties.get(root + KEY_NODE_SIZE_RATIO);
        Float r = (o instanceof Integer)
                ? new Float((Integer) o)
                : (o instanceof Double)
                ? new Float((Double) o)
                : (Float) o;

        r = PApplet.map(r, 0.0f, 1.0f, MIN_RADIUS_MAGNIFIER, MAX_RADIUS_MAGNIFIER);

        if (metrics.minNodeRadius == 0 && metrics.maxNodeRadius == 0) {
            // in this bad case, we use the minimal radius size
            for (Node n : input.nodes) {
                n.radius = RADIUS_MIN * r;
            }
            output.computeMetrics();
            return output;
        }

        for (Node n : input.nodes) {
            n.radius = PApplet.map(n.radius, metrics.minNodeRadius, metrics.maxNodeRadius, RADIUS_MIN, RADIUS_MAX);
            n.radius *= r;
            output.add(n);

        }

        return output;
    }
}
