/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.browser;

import netscape.javascript.JSObject;

/**
 *
 * @author jbilcke
 */
public class Browser {

    private boolean enabled = false;
    public JSObject window = null;

    public Browser(JSObject window) {
        this.window = window;
        this.enabled = true;
    }
   public Browser() {
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
