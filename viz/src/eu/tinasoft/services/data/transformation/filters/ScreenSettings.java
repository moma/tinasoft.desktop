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
public class ScreenSettings extends NodeFilter {


    // the range of the sliders
    public final float MIN_SCREEN_RADIUS = 1.0f;
    public final float MAX_SCREEN_RADIUS = 4.0f;

    public final float MIN_SCREEN_EDGE_WEIGHT = 1.0f;
    public final float MAX_SCREEN_EDGE_WEIGHT = 4.0f;

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        System.out.println("scaling to screen..");

        for (Node n : input.nodes) {
            n.radius = PApplet.map(n.radius,0.0f,1.0f,MIN_SCREEN_RADIUS, MAX_SCREEN_RADIUS);

            for (Object w : n.weights.values().elements()) {
                w = (Float) PApplet.map((Float)w,0.0f,1.0f,MIN_SCREEN_EDGE_WEIGHT, MAX_SCREEN_EDGE_WEIGHT);
            }
        }
        return input;
    }
}
