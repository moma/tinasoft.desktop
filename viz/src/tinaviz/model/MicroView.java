/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

/**
 *
 * @author jbilcke
 */
public class MicroView extends View {

    public MicroView (Graph graph) {
        super(graph);
        spatializeWhenMoving = true;
        centerOnSelection = true;
    }

    public MicroView() {
        super();
    }

   public void setCenterOnSelection(boolean value) {
       centerOnSelection = value;
   }
    public String getName() {
        return "micro";
    }
}
