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
public class Browser {

    private boolean enabled = false;
    public JSObject window = null;

    public String prefix = "";

    public Browser(JSObject window, String prefix) {
        this.window = window;
        this.enabled = true;
    }
 
    public Browser() {
        this.enabled = true;
    }

   public Object eval(String cmd) {
         return window.eval(cmd);
    }
   public Object tinaviz(String cmd) {
       if (window==null) return null;
         return window.eval(prefix + "tinaviz."+cmd+";");
         // .call("parent.tinaviz.getWidth", null)
         //return window.call()
    }
   public void buttonStateCallback(String attr, boolean state) {
       tinaviz("buttonStateCallback('"+attr+"',"+(state ? "true":"false")+")");
   }
}
