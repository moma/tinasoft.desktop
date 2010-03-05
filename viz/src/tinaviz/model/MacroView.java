/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

/**
 *
 * @author jbilcke
 */
public class MacroView extends View {

    public MacroView() {
        super();
        prespatializeSteps = 84;
    }

    @Override
    public String getName() {
        return "macro";
    }
}
