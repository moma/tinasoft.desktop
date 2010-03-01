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

    public boolean centerOnSelection = true;
    
    public MesoView (Graph graph) {
        super(graph);
    }

   public void setCenterOnSelection(boolean value) {
       centerOnSelection = true;
   }

}
