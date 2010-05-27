/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.formats.json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author jbilcke
 */
public class JSONEncoder {
    static public Object valueEncoder(Object o) {
        try {
            return (o instanceof String) ? URLEncoder.encode((String) o, "UTF-8") : o;
        } catch (UnsupportedEncodingException ex) {
            return "__ENCODING_ERROR__";
        }
    }
}
