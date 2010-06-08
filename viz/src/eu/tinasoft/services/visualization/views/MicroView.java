/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.views;

import eu.tinasoft.services.session.Session;

/**
 *
 * @author jbilcke
 */
public class MicroView extends View {

    public MicroView(Session aThis) {
        super(aThis);
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
