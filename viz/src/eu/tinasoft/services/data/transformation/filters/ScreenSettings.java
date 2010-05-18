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
    public float MIN_RADIUS_MAGNIFIER = 10.0f;
    public float MAX_RADIUS_MAGNIFIER = 300.0f;

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        for (Node n : input.nodes) {
            n.radius = PApplet.map(n.radius,0.0f,1.0f,MIN_RADIUS_MAGNIFIER, MAX_RADIUS_MAGNIFIER);
        }
        return input;
    }
}
