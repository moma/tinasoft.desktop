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

    public JSObject window;
    public String jsContextPrefix;
    public String apiPrefix;

    public Browser(JSObject window, String prefix) {
        this.window = window;
        this.jsContextPrefix = prefix;
        this.apiPrefix = "tinaviz.";
    }

    public Browser() {
        this.window = null;
        this.jsContextPrefix = "";
        this.apiPrefix = "tinaviz.";
    }

    public Object call(String fnc, Object[] args) {
        // System.out.println("window: "+window+" will call "+fnc);
        return (window!=null)?window.call(fnc, args):null;
    }

    public Object callAndWait(String fnc) {
        return (window!=null)?window.call(fnc,null):null;
    }

    public Object setTimeout(Object[] message) {
        return call("setTimeout", message);
    }

    public Object callAndForget(String func, String args) {
        Object[] message = {jsContextPrefix + apiPrefix + func + "( " + args + ")", 0};
         //System.out.println("going to call setTimeout("+message[0]+");");

        return setTimeout(message);
    }

    public void buttonStateCallback(String attr, boolean state) {
        callAndForget("buttonStateCallback", "'" + attr + "'," + (state ? "true" : "false") + "");
    }

    public void init() {
        // System.out.println("going to call callAndForget(\"init\",\"\");");
        callAndForget("init","");
    }
}
