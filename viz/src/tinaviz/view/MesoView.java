/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.view;

import tinaviz.graph.Node;
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
        ZOOM_CEIL = 10f;
        ZOOM_FLOOR = 200.0f;
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
