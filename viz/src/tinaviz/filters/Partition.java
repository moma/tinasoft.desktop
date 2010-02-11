/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

/**
 *
 * @author jbilcke
 */
public class Partition implements Filter {
    private boolean enabled = true;

    public List<Node> process(List<Node> input, Map<String, Channel> channels) {
        //
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }
}
