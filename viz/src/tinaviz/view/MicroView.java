/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.view;

import processing.core.PVector;
import tinaviz.session.Session;

/**
 *
 * @author jbilcke
 */
public class MicroView extends View {

 

    public MicroView(Session aThis) {
        super(aThis);
            spatializeWhenMoving = true;
            graph.locked.set(false);
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
