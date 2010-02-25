/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

import java.util.HashMap;

/**
 *
 * @author jbilcke
 */
public class MesoView extends View {

    public boolean centerOnSelection = true;


    public MesoView () {
        storedNodes = new HashMap<String, tinaviz.Node>();
        metrics = new Metrics();
    }


   public void setCenterOnSelection(boolean value) {
       centerOnSelection = true;
   }

}
