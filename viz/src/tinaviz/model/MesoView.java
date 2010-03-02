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

    public MesoView (Graph graph) {
        super(graph);
        spatializeWhenMoving = true;
        centerOnSelection = true;
    }

    public MesoView() {
        super();
    }

   public void setCenterOnSelection(boolean value) {
       centerOnSelection = value;
   }
    @Override
    public String getName() {
        return "meso";
    }

    @Override
    public void resetCamera() {
        camZ = 8.0f;
        camX = 800;
        camY = 400;
    }
    @Override
    public void resetCamera(float width,float height) {
        camZ = 1.0f;
        camX = (width / 2.0f) ;
        camY = (height / 2.0f) ;
    }
}
