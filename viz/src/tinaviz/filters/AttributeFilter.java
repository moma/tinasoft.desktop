/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.security.KeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class AttributeFilter implements Filter {

    public static String KEY_ENABLED = "enabled";

    public static String KEY_ATTRIBUTE = "attrubute";
    public static String KEY_TYPE = "type";
    public static String KEY_MODEL = "model";
    public static String KEY_CATEGORY = "category";
    public static String KEY_REGEX = "regex";

    /*
    applet.filterConfig("cat_filter", "attribute", "category");
    applet.filterConfig("cat_filter", "type", "regex");
    applet.filterConfig("cat_filter", "model", "ngrams");
   *
     *
     */
    public Map<String,Object> params;

    public AttributeFilter() {
        params = new HashMap<String,Object>();
        params.put(KEY_ENABLED, true);
    }

    public List<Node> process(List<Node> input) {
        List<Node> output = new ArrayList<Node>();

        if( ! (Boolean) params.get(KEY_ENABLED) ) {
            return output;
        }
        /*
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
         * 
         */
        return output;
    }

  public void setField(String key, String value) throws KeyException {
        throw new KeyException("key "+key+" not found");
    }

    public void setField(String key, float value) throws KeyException {
            throw new KeyException("key "+key+" not found");
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
