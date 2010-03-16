/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

/**
 *
 * @author jbilcke
 */
public class MesoView extends View {



    public MesoView() {
        super();
        spatializeWhenMoving = true;
        centerOnSelection = true;
        ZOOM_CEIL = 1.0f;
        ZOOM_FLOOR = 50.0f;
    }

    public void setCenterOnSelection(boolean value) {
        centerOnSelection = value;
    }

    @Override
    public String getName() {
        return "meso";
    }
}
