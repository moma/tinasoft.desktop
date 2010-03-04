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

    public MesoView(Graph graph) {
        super(graph);
        spatializeWhenMoving = true;
        centerOnSelection = true;
        ZOOM_CEIL = 1.0f;
        ZOOM_FLOOR = 10.0f;

    }

    public MesoView() {
        super();
        ZOOM_CEIL = 1.0f;
        ZOOM_FLOOR = 10.0f;

    }

    public void setCenterOnSelection(boolean value) {
        centerOnSelection = value;
    }

    @Override
    public String getName() {
        return "meso";
    }
}
