/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.views;

/**
 *
 * @author jbilcke
 */
public class Channel {

    protected ChannelType channelType = ChannelType.BINARY;
    protected Object value;
    protected boolean enabled = true;
    protected boolean including = true;

    public Object getField(String key) {
        if (key.equals("type")) {
            switch (channelType) {
                case RANGE:
                    return "range";
                case THRESHOLD:
                    return "threshold";
                case BINARY:
                    return "binary";
            }
        } else if (key.equals("including")) {
            return (including) ? true : false;
        } else if (key.equals("value")) {
            return value;
        }
        return "";
    }

    public boolean setField(String key, Object value) {
        if (key.equals("type")) {
            if (((String)value).equals("range")) {
                this.channelType = ChannelType.RANGE;
            } else if (((String)value).equals("threshold")) {
                this.channelType = ChannelType.THRESHOLD;
            } else {
                this.channelType = ChannelType.BINARY;
            }
        } else if (key.equals("including")) {
            if (((Boolean)value) == true) {
                this.including = true;
            } else {
                this.including = false;
            }
        } else if (key.equals("value")) {
            this.value = value;
        } else {
            return false;
        }

        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = true;
    }
}
