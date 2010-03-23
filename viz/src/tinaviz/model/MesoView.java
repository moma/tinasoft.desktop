/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class MesoView extends View {



    MesoView(Session aThis) {
        super(aThis);
        spatializeWhenMoving = true;
        centerOnSelection = true;
        ZOOM_CEIL = 0.5f;
        ZOOM_FLOOR = 50.0f;
        graph.locked.set(false);
    }

    public void setCenterOnSelection(boolean value) {
        centerOnSelection = value;
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
