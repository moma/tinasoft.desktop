/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

/**
 *
 * @author jbilcke
 */
public class MacroView extends View {

    public MacroView (Graph graph) {
        super(graph);
    }

    public MacroView() {
        super();
    }

    public String getName() {
        return "macro";
    }

     public void resetCamera() {
        camX = 0;
        camY = 0;
        //vizx = (float)width/2.0f;
        //vizy = (float)height/2.0f;
        //vizx += (session.metrics.maxX - session.metrics.minX);
        //vizy +=  (session.metrics.maxY - session.metrics.minY);
        camZ = 4.0f;
    }

    public void resetCamera(float width,float height) {
        camX = 0;
        camY = 0;
        camZ = 4.0f;
    }


}
