/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.protocols.browser;

import netscape.javascript.JSObject;

/**
 *
 * @author jbilcke
 */
public class LiveConnector {

    private boolean enabled = false;
    public JSObject window = null;

    public LiveConnector(JSObject window) {
        this.window = window;
        this.enabled = true;
    }
   public LiveConnector() {
    }

   public Object eval(String cmd) {
         return window.eval(cmd);
    }
   public Object tinaviz(String cmd) {
         return window.eval("parent.tinaviz."+cmd+";");
         // .call("parent.tinaviz.getWidth", null)
         //return window.call()
    }
}
