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
public class GlobalNetwork extends Network {
    public GlobalNetwork () {
        storedNodes = new HashMap<String, tinaviz.Node>();
        metrics = new Metrics();
    }
}
