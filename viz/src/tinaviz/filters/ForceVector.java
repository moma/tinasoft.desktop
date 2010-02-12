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
public class ForceVector implements Filter {

    public static String KEY_REPULSION = "repulsion";
    public static String KEY_ATTRACTION = "attraction";
    public static String KEY_VALUE = "value";
    private boolean enabled = true;

    public List<Node> process(List<Node> input, Map<String, Channel> channels) {
        List<Node> output = input;
        if (!enabled) return output;

        float len = 1f;
        float vx = 1f;
        float vy = 1f;
        float LAYOUT_REPULSION = 0.01f;
        float LAYOUT_ATTRACTION = 0.0001f;

       if (channels.containsKey(KEY_REPULSION)) {
                LAYOUT_REPULSION = (Float) channels.get(KEY_REPULSION).getField(KEY_VALUE);
        }
          if (channels.containsKey(KEY_ATTRACTION)) {
                LAYOUT_ATTRACTION = (Float) channels.get(KEY_ATTRACTION).getField(KEY_VALUE);
        }
        
        for (Node n1 : output) {
            for (Node n2 : output) {
                if (n1 == n2) {
                    continue;
                }
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                len = (float) Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

                if (n1.neighbours.contains(n2)) {
                    n1.vx += (vx * len) * LAYOUT_ATTRACTION;
                    n1.vy += (vy * len) * LAYOUT_ATTRACTION;
                    n2.vx -= (vx * len) * LAYOUT_ATTRACTION;
                    n2.vy -= (vy * len) * LAYOUT_ATTRACTION;
                }

                // TODO fix this
                n1.vx -= (vx / len) * LAYOUT_REPULSION;
                n1.vy -= (vy / len) * LAYOUT_REPULSION;
                n2.vx += (vx / len) * LAYOUT_REPULSION;
                n2.vy += (vy / len) * LAYOUT_REPULSION;

            } // FOR NODE B
        }   // FOr NODE A

        for (Node n : output) {
            n.x += n.vx;
            n.y += n.vy;
            n.vx = 0.0f;
            n.vy = 0.0f;
        }

        return output;
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
