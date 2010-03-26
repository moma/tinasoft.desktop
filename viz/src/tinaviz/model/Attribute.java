/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import org.w3c.dom.*;

/**
 *
 * @author jbilcke
 */
public class Attribute {

    public Class type;
    public String id;
    public String key;
    public Object defaultValue;

    public Attribute(Node xml) {
        //System.out.println("   - getting id..");
        setID(xml.getAttributes().getNamedItem("id").getNodeValue());
        //System.out.println("   - got id " + id);
        //System.out.println("   - getting key..");
        setKey(xml.getAttributes().getNamedItem("title").getNodeValue());
        //System.out.println("   - got key " + key);
        //System.out.println("   - getting type..");
        setType(xml.getAttributes().getNamedItem("type").getNodeValue());
        //System.out.println("   - got type " + type);
    }

    public Attribute(String id, String key, String type) {
        setKey(key);
        setType(type);
        setID(id);
        setDefaultValue(null);
    }

    public Attribute(String id, String key, String type, Object def) {
        setKey(key);
        setType(type);
        setID(id);
        setDefaultValue(def);
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setType(String type) {
        if (type.equalsIgnoreCase("float")) {
            this.type = Float.class;
        } else if (type.equalsIgnoreCase("string")) {
            this.type = String.class;
        } else if (type.equalsIgnoreCase("boolean")) {
            this.type = Boolean.class;
        } else if (type.equalsIgnoreCase("integer")) {
            this.type = Integer.class;
        } else {
            System.out.println("unknow attribute type");
            this.type = String.class;
        }

        setDefaultValue(null);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDefaultValue(Object obj) {
        this.defaultValue = obj;
        if (type==Float.class) {
            defaultValue = 1.0f;
        } else if (type==Integer.class) {
            defaultValue = 1;
        } else if (type==Boolean.class) {
            defaultValue = true;
        } else {
            defaultValue = "";
        }
    }

    @Override
    public String toString() {
        return "attvalue id=" + id + " key=" + key + " type=" + type + " default=" + defaultValue;
    }
}
