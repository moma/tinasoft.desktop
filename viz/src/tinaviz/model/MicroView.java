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

 

    public MicroView(Session aThis) {
        super(aThis);
            spatializeWhenMoving = true;
        centerOnSelection = true;
    }

   public void setCenterOnSelection(boolean value) {
       centerOnSelection = value;
   }
    @Override
    public String getName() {
        return "micro";
    }
}
