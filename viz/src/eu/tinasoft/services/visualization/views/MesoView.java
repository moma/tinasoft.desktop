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
public class MesoView extends View {

    public MesoView(Session aThis) {
        super(aThis);

        ZOOM_CEIL = 0.56f;
        ZOOM_FLOOR = 30.0f;
        RECENTERING_MARGIN = 1.5f;

        resetParams();
        graph.locked.set(false);
    }

    @Override
    public String getName() {
        return "meso";
    }

    @Override
    public ViewLevel getLevel() {
        return ViewLevel.MESO;
    }
}
