/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class MicroView extends View {

 

    public MicroView(Session aThis) {
        super(aThis);
            spatializeWhenMoving = true;
    }

    @Override
    public String getName() {
        return "micro";
    }
    @Override
    public ViewLevel getLevel() {
        return ViewLevel.MICRO;
    }

}
