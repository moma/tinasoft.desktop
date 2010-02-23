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
    public LocalNetwork () {
        storedNodes = new HashMap<String, tinaviz.Node>();
        metrics = new Metrics();
    }
}
