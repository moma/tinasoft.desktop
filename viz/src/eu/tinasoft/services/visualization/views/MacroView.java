/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.views;

import eu.tinasoft.services.session.Session;

/**
 *
 * @author jbilcke
 */
public class MacroView extends View {

    public MacroView(Session aThis) {
        super(aThis);

        resetParams();

        ZOOM_CEIL = 0.2f;
        ZOOM_FLOOR = 45.0f;

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
