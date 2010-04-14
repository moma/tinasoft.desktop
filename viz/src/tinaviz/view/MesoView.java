/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.view;

import tinaviz.session.Session;

/**
 *
 * @author jbilcke
 */
public class MesoView extends View {



    public MesoView(Session aThis) {
        super(aThis);
        spatializeWhenMoving = true;
        centeringMode = CenteringMode.SELECTED_GRAPH_BARYCENTER;
        ZOOM_CEIL = 0.1f;
        ZOOM_FLOOR = 250.0f;
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
