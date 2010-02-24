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
public class LocalNetwork extends Network {

    public boolean centerOnSelection = true;


    public LocalNetwork () {
        storedNodes = new HashMap<String, tinaviz.Node>();
        metrics = new Metrics();
    }


   public void setCenterOnSelection(boolean value) {
       centerOnSelection = true;
   }

}
