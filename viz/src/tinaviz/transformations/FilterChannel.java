/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public class FilterChannel extends ParamChannel {
    private Float min = 0.0f;
    private Float max = 1.0f;

    public enum FilteringMode {

        STRING_BEGIN,
        STRING_END,
        STRING_EXACT,
        STRING_REGEX,
        BOOLEAN_EXACT,
        INTEGER_EXACT,
        DOUBLE_EXACT,
        FLOAT_EXACT
    }

    private String attributeName = "";
    private FilteringMode filteringMode = FilteringMode.STRING_REGEX;

    public FilterChannel() {
        value = Pattern.compile(".*");
    }
 
    public boolean setField(String key, Object value) {
        if (key.equals("mode")) {
          if (((String)value).equals("regex")) {
              filteringMode = FilteringMode.STRING_REGEX;
          }
        } else if (key.equals("pattern") | key.equals("regex") | key.equals("mask")) {
          if (((String)value).equals("regex")) {
              this.value = Pattern.compile((String)value, Pattern.CASE_INSENSITIVE);
          }
        } else if (key.equals("attribute") | key.equals("attr")) {
              attributeName = (String) value;
        }
        else if (key.equals("min")) {
            if (channelType != ChannelType.RANGE) {
                return false;
            }
            this.min = (Float)min;
        } else if (key.equals("max")) {
            if (channelType != ChannelType.RANGE) {
                return false;
            }
            this.max = (Float)max;
        } else {
            return super.setField(key, value);
        }
        return true;
    }

    public boolean match(Node node) {

        switch (filteringMode) {
            case STRING_BEGIN:
                break;
            case STRING_END:
                break;
            case STRING_EXACT:
                break;
            case STRING_REGEX:
                Pattern p = (Pattern) value;
                String s = (String)node.attributes.get(attributeName);
                if (p != null) {
                    Matcher m = p.matcher(s);

                    if (including) {
                        return m.matches();
                    } else {
                        return !m.matches();
                    } 
                }
                break;

            case BOOLEAN_EXACT:
                //this.value = (Boolean) value;
                break;

            case INTEGER_EXACT:
                //this.value = (Integer) value;
                break;

            case DOUBLE_EXACT:
                //this.value = (Double) value;
                break;

            case FLOAT_EXACT:
                //this.value = (Float) value;
                break;

        }
    return false;

    }


}
