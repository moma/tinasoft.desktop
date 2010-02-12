/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import java.util.List;
import java.util.Map;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class Partition implements Filter {
    private boolean enabled = true;

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

    public List<Node> process(List<Node> input, Map<String, Channel> channels) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
