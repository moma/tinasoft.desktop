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
public class MacroView extends View {
    public MacroView () {
        storedNodes = new HashMap<String, tinaviz.Node>();
        metrics = new Metrics();
    }
}
