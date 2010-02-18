/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.security.InvalidParameterException;
import java.security.KeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class ForceVector implements Filter {
    public static String KEY_ENABLED = "enabled";
    public static String KEY_REPULSION = "repulsion";
    public static String KEY_ATTRACTION = "attraction";
 
    private final HashMap<String,Object> params;

    public ForceVector() {
        params = new HashMap<String,Object>();
        params.put(KEY_ENABLED, true);
        params.put(KEY_REPULSION, 0.01f);
        params.put(KEY_ATTRACTION, 0.0001f);
    }
    public List<Node> process(List<Node> input, Map<String, Channel> channels) {
        List<Node> output = input;

        if( ! (Boolean) params.get(KEY_ENABLED) ) {
            return output;
        }

        float len = 1f;
        float vx = 1f;
        float vy = 1f;

        float REPULSION = (Float) params.get(KEY_REPULSION);
        float ATTRACTION = (Float) params.get(KEY_ATTRACTION);


        for (Node n1 : output) {
  
            for (Node n2 : output) {
                if (n1 == n2) {
                    continue;
                }
                float weight = n1.weights.get(n2.uuid);
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                len = (float) Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));

                if (n1.neighbours.contains(n2)) {
                    n1.vx += (vx * len) * ATTRACTION * weight;
                    n1.vy += (vy * len) * ATTRACTION * weight;
                    n2.vx -= (vx * len) * ATTRACTION * weight;
                    n2.vy -= (vy * len) * ATTRACTION * weight;
                }

                // TODO fix this
                n1.vx -= (vx / len) * REPULSION;
                n1.vy -= (vy / len) * REPULSION;
                n2.vx += (vx / len) * REPULSION;
                n2.vy += (vy / len) * REPULSION;

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


    public void setField(String key, String value) throws KeyException {
        throw new KeyException("key "+key+" not found");
    }

    public void setField(String key, float value) throws KeyException {
        if (key.equals(KEY_ATTRACTION) || key.equals(KEY_REPULSION)) {
            params.put(key, value);
        } else {
            throw new KeyException("key "+key+" not found");
        }
    }

    public void setField(String key, int value) throws KeyException {
        throw new KeyException("key "+key+" not found");
    }

    public void setField(String key, boolean value) throws KeyException {
        if (key.equals(KEY_ENABLED)) {
            params.put(key, value);
        } else {
            throw new KeyException("key "+key+" not found");
        }
    }

    public Object getField(String key) throws KeyException {
        return params.get(key);
    }

}
