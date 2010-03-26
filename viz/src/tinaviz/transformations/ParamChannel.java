/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations;

/**
 *
 * @author jbilcke
 */
public class ParamChannel extends Channel {

    @Override
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
        } else {
            return super.getField(key);
        }
        return "";
    }

    @Override
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
        } else {
            return super.setField(key, value);
        }

        return true;
    }


}
