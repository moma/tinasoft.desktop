/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class RegexMatch implements Filter {

    private boolean enabled = true;

    public Map<String,Channel> params = new HashMap<String,Channel>();

    public RegexMatch(String name) {
        super();

    }

    public List<Node> process(List<Node> input, Map<String, Channel> channels) {
        List<Node> output = new ArrayList<Node>();
        if (!enabled) return input;

        for (Node n : input) {
            boolean match = true;
            for (Channel f : channels.values()) {
               if (f instanceof FilterChannel) {
                  if (!((FilterChannel)f).match(n)) {
                      match = false;
                      break;
                  }
               }
            }
            if (match) output.add(n);
        }
        return output;
    }

    public Map<String,Channel> getParams() {
        return params;
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
