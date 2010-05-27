/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.session;

/**
 *
 * @author jbilcke
 */
public class ViewNotFoundException extends Exception {

    ViewNotFoundException(String string) {
        super(string);
    }
    ViewNotFoundException() {
        super("view not found!");
    }
}
