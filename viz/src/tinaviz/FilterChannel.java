/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.lang.String;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jbilcke
 */
public class FilterChannel {


    public enum ChannelType {

        RANGE,
        THRESHOLD,
        BINARY
    }

    public enum FilterType {

        WITHIN,
        WITHOUT
    }

    public enum AttributeType {

        STRING_BEGIN,
        STRING_END,
        STRING_EXACT,
        STRING_REGEX,
        BOOLEAN_EXACT,
        INTEGER_EXACT,
        DOUBLE_EXACT,
        FLOAT_EXACT
    }
    private String name;
    private String attributeName;
    private Object value;

    private ChannelType channelType;
    private FilterType filterType;
    private AttributeType attributeType;

    private boolean enabled = true;

    public FilterChannel(
            String name,
            AttributeType type,
            ChannelType channelType,
            FilterType filterType,
            String attributeName,
            Object value) {
        this.name = name;
        setChannelType(channelType);
        setFilterType(filterType);
        setMatcher(attributeType, value);
    }

    public void setRange(float min, float max) {
        if (channelType == FilterChannel.ChannelType.RANGE) {

         }
    }

    public void setChannelType(ChannelType type) {
        channelType = type;
    }
    public ChannelType getChannelType() {
        return channelType;
    }
    public void setFilterType(FilterType type) {
        filterType = type;
    }
    public FilterType getFilterType() {
        return filterType;
    }
    public AttributeType getAttributeTypee() {
        return attributeType;
    }
    public boolean hasName(String name) {
        return attributeName.equals(name);
    }
    public String getName() {
        return attributeName;
    }

    public boolean setEnabled(boolean enabled) {
        this.enabled = enabled;
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }

    public void setMatcher(AttributeType type, Object value) {
        this.attributeType = type;
        switch (type) {
            case STRING_BEGIN:
                this.value = (String) value;
                break;
            case STRING_END:
                this.value = (String) value;
                break;
            case STRING_EXACT:
                this.value = (String) value;
                break;
            case STRING_REGEX:
                this.value = Pattern.compile((String) value);
                break;

            case BOOLEAN_EXACT:
                this.value = (Boolean) value;
                break;

            case INTEGER_EXACT:
                this.value = (Integer) value;
                break;

            case DOUBLE_EXACT:
                this.value = (Double) value;
                break;

            case FLOAT_EXACT:
                this.value = (Float) value;
                break;

        }

    }

    public boolean match(Node node) {

        switch (attributeType) {
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

                    if (this.filterType == FilterType.WITHIN) {
                        return m.matches();
                    } else if (this.filterType == FilterType.WITHOUT) {
                        return !m.matches();
                    } else {
                        return false;
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
