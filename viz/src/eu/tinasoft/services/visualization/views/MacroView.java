/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.views;

import processing.core.PVector;
import eu.tinasoft.services.session.Session;

/**
 *
 * @author jbilcke
 */
public class MacroView extends View {

    public MacroView(Session aThis) {
        super(aThis);
        prespatializeSteps = 84;
         ZOOM_CEIL = 0.025f;
         ZOOM_FLOOR = 25.0f;
         resetParams();
         graph.locked.set(false);
    }

    @Override
    public String getName() {
        return "macro";
    }
    @Override
    public ViewLevel getLevel() {
        return ViewLevel.MACRO;
    }

}
