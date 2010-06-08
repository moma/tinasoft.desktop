/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.formats.json.JSONEncoder;
import eu.tinasoft.services.formats.json.JSONException;
import eu.tinasoft.services.formats.json.JSONStringer;
import eu.tinasoft.services.formats.json.JSONWriter;

import eu.tinasoft.services.session.Attribute;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.formats.xml.XPathReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import processing.core.PVector;

/**
 * Graph container - todo rename it
 * 
 * @author Julian Bilcke
 */
public class Graph implements Cloneable {

    public static final String NS = "tina";
    public Map<Integer, eu.tinasoft.services.data.model.Node> storedNodes = null;
    public Map<String, Attribute> nodeAttributes = null;
    public Map<String, Attribute> edgeAttributes = null;
    public Map<String, Object> sessionAttributes = null;
    public AtomicBoolean locked = null;
    public AtomicBoolean needToBeReadAgain = null;
    public AtomicBoolean topologyChanged = null;
    public AtomicInteger revision;
    private Session session = null;
    //public Map<String,Metrics> categorizedMetrics = new HashMap<String,Metrics>();

    public Graph(Session session) {
        storedNodes = new HashMap<Integer, eu.tinasoft.services.data.model.Node>();
        sessionAttributes = new HashMap<String, Object>();
        nodeAttributes = new HashMap<String, Attribute>();
        edgeAttributes = new HashMap<String, Attribute>();
        locked = new AtomicBoolean(true);
        topologyChanged = new AtomicBoolean(false);
        revision = new AtomicInteger(0);
        this.session = session;
    }

    public boolean updateFromURI(String uri) {

        //Console.log("<applet> got updateFromURI(" + uri + ") from " + this);
        try {
            XPathReader xml = new XPathReader();
            xml.parseFromURI(uri);
            return parseXML(xml);
        } catch (XPathExpressionException ex) {
            Console.log(ex.toString());
        } catch (URISyntaxException ex) {
            Console.log(ex.toString());
        } catch (MalformedURLException ex) {
            Console.log(ex.toString());
        } catch (IOException ex) {
            Console.log(ex.toString());
        }
        return false;
    }

    public boolean updateFromString(String str) {
        //Console.log("<applet> got updateFromString(..) from " + this);

        try {
            XPathReader xml = new XPathReader();
            System.out.println("loading GEXF from string..");
            xml.parseFromString(str);
            //xml.parseFromString(new String(str.getBytes(), "UTF-8"));

            //Console.log("<applet> calling parse XML on "+str);
            return parseXML(xml);

        } catch (XPathExpressionException ex) {
            Console.log(ex.toString());
        }
        /* } catch (UnsupportedEncodingException ex) {
        Console.log(ex.toString());
        }*/
        return false;
    }

    public boolean updateFromInputStream(InputStream inputStream) {

        try {
            XPathReader xml = new XPathReader();
            xml.parseFromStream(inputStream);
            return parseXML(xml);
        } catch (XPathExpressionException ex) {
            Console.log(ex.toString());
        }

        return false;
    }

    public boolean updateFromNodeList(NodeList nodes) {
        addNodes(nodes);
        return true;
    }

    private boolean parseXML(XPathReader xml) throws XPathExpressionException {
        topologyChanged.set(storedNodes.size() == 0);
        locked.set(true);
        String meta = "/gexf/graph/tina/";
        Console.log("parsing..");
        Map<String, List<Node>> normalizeMyNodes = new HashMap<String, List<Node>>();

        Double zoomValue = (Double) xml.read(meta + "zoom/@value", XPathConstants.NUMBER);
        sessionAttributes.put("zoom", (zoomValue != null) ? zoomValue.floatValue() : 1.0f);

        Double thresholdValue = (Double) xml.read(meta + "threshold/@min", XPathConstants.NUMBER);
        if (thresholdValue != null) {
            //lowerThreshold = thresholdValue.floatValue();
        }

        thresholdValue = (Double) xml.read(meta + "threshold/@max", XPathConstants.NUMBER);
        if (thresholdValue != null) {
            //upperThreshold = thresholdValue.floatValue();
        }

        String selected = (String) xml.read(meta + "select/@node", XPathConstants.STRING);
        sessionAttributes.put("selected", (selected != null) ? selected : "");

        Boolean cond = (Boolean) xml.read(meta + "labels/@show", XPathConstants.BOOLEAN);
        sessionAttributes.put("showLabels", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "nodes/@show", XPathConstants.BOOLEAN);
        sessionAttributes.put("showNodes", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "links/@show", XPathConstants.BOOLEAN);
        sessionAttributes.put("showLinks", (cond != null) ? cond : true);


        cond = (Boolean) xml.read(meta + "layout/@show", XPathConstants.BOOLEAN);
        sessionAttributes.put("animationPaused", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "layout/@prespatialize", XPathConstants.BOOLEAN);
        sessionAttributes.put("prespatialize", (cond != null) ? cond : true);

        org.w3c.dom.NodeList attributesXML = (org.w3c.dom.NodeList) xml.read(
                "/gexf/graph/attributes",
                XPathConstants.NODESET);

        for (int i = 0; i < attributesXML.getLength(); i++) {
            org.w3c.dom.Node attributeXML = attributesXML.item(i);
            org.w3c.dom.NamedNodeMap nodeAttributesXML = attributeXML.getAttributes();
            String attrsClass = nodeAttributesXML.getNamedItem("class").getNodeValue();
            if (attrsClass.equalsIgnoreCase("node")) {
                org.w3c.dom.NodeList xmlnodeChildren = attributeXML.getChildNodes();
                for (int j = 0; j < xmlnodeChildren.getLength(); j++) {
                    org.w3c.dom.Node n = xmlnodeChildren.item(j);
                    if (n.getNodeName().equalsIgnoreCase("attribute")) {
                        Attribute attr = new Attribute(n);
                        if (n.getFirstChild() != null) {
                            if (n.getFirstChild().getFirstChild() != null) {
                                attr.setDefaultValue(n.getFirstChild().getFirstChild().getNodeValue());
                            }
                        }
                        this.nodeAttributes.put(attr.id, attr);

                    }
                }
            } else if (attrsClass.equalsIgnoreCase("edge")) {
                org.w3c.dom.NodeList xmlnodeChildren = attributeXML.getChildNodes();
                for (int j = 0; j < xmlnodeChildren.getLength(); j++) {
                    org.w3c.dom.Node n = xmlnodeChildren.item(j);
                    if (n.getNodeName().equalsIgnoreCase("attribute")) {
                        Attribute attr = new Attribute(n);
                        if (n.getFirstChild() != null) {
                            if (n.getFirstChild().getFirstChild() != null) {
                                attr.setDefaultValue(n.getFirstChild().getFirstChild().getNodeValue());
                            }
                        }
                        this.edgeAttributes.put(attr.id, attr);
                    }
                }
            }

        }


        org.w3c.dom.NodeList nodesXML = (org.w3c.dom.NodeList) xml.read("/gexf/graph/nodes/node",
                XPathConstants.NODESET);
        for (int i = 0; i < nodesXML.getLength(); i++) {
            org.w3c.dom.Node xmlnode = nodesXML.item(i);

            org.w3c.dom.NamedNodeMap xmlnodeAttributes = xmlnode.getAttributes();

            String uuid = xmlnodeAttributes.getNamedItem("id").getNodeValue();
            String cat = "BAD_CATEGORY";

            if (uuid.contains("::")) {
                cat = uuid.split("::")[0];
                uuid = uuid.split("::")[1];
                System.out.println("got category "+cat+" via ID!");
            }

            int id = uuid.hashCode();

            String label = (xmlnodeAttributes.getNamedItem("label") != null)
                    ? xmlnodeAttributes.getNamedItem("label").getNodeValue()
                    : "" + id;

            Node node = new Node(id, label, (float) Math.random() * 2f,
                    (float) Math.random() * 100f,
                    (float) Math.random() * 100f);
            node.attributes.put("label", label);
            node.category = cat;
            node.uuid = uuid;
            node.weight = 1.0f;

            if (node.category.equals("NGram")) {
                node.r = 110;
                node.g = 100;
                node.b = 150;
            } else {
                node.r = 150;
                node.g = 100;
                node.b = 110;
            }

            org.w3c.dom.NodeList xmlnodeChildren = xmlnode.getChildNodes();

            for (int j = 0; j < xmlnodeChildren.getLength(); j++) {
                org.w3c.dom.Node n = xmlnodeChildren.item(j);

                if (n.getNodeName().equalsIgnoreCase("attvalues")) {
                    // System.out.println("in attributes tag");
                    org.w3c.dom.NodeList xmlattribs = n.getChildNodes();
                    for (int k = 0; k < xmlattribs.getLength(); k++) {
                        org.w3c.dom.Node attr = xmlattribs.item(k);
                        if (attr.getNodeName().equalsIgnoreCase("attvalue")) {
                            // System.out.println("in attribute tag");
                            if (attr.getAttributes() != null) {
                                org.w3c.dom.Node AttributeIdXML = null;
                                if (attr.getAttributes().getNamedItem("for") != null) {
                                    AttributeIdXML = attr.getAttributes().getNamedItem("for");
                                } else {
                                    // maybe this is an old gexf..
                                    AttributeIdXML = attr.getAttributes().getNamedItem("id");
                                }
                                if (AttributeIdXML != null) {

                                    String attributeId = AttributeIdXML.getNodeValue();
                                    Attribute attrib = nodeAttributes.get(attributeId);
                                    String nodeValue = attr.getAttributes().getNamedItem("value").getNodeValue();
                                    // System.out.println("found attribute "+attrib.toString()+" with key "+attrib.key+" !");

                                    if (attrib.key.equalsIgnoreCase("weight")) {

                                        if (attrib.type == Float.class | attrib.type == Integer.class | attrib.type == Double.class) {
                                            node.weight = Float.parseFloat(nodeValue);

                                        }
                                    } else if (attrib.key.equalsIgnoreCase("category")) {
                                        if (attrib.type == String.class) {
                                            node.category = nodeValue;
                                            //System.out.println("could read category: "+node.category);
                                        } else {
                                            System.out.println("couldn't read category "+nodeValue);
                                        }
                                    }

                                    // store the attributes in the node map
                                    // System.out.println("storing attribute "+attrib.key+" with attr id "+attributeId);

                                    node.attributes.put(attrib.key,
                                            (attrib.type == Integer.class)
                                            ? Integer.parseInt(nodeValue)
                                            : (attrib.type == Float.class)
                                            ? Float.parseFloat(nodeValue)
                                            : (attrib.type == String.class)
                                            ? nodeValue
                                            : nodeValue);

                                }

                            }
                        }

                    }
                } else if (n.getNodeName().equalsIgnoreCase("viz:position") || n.getNodeName().equalsIgnoreCase("position")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("x") != null) {
                        node.position.x = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("x").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("y") != null) {
                        node.position.y = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("y").getNodeValue());
                    }

                } else if (n.getNodeName().equalsIgnoreCase("viz:size") || n.getNodeName().equalsIgnoreCase("size")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("value") != null) {
                        // FIXME normalize radius by a max radius, so it is not too big
                        node.radius = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("value").getNodeValue());
                    }
                } else if (n.getNodeName().equalsIgnoreCase("viz:color") || n.getNodeName().equalsIgnoreCase("color")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("r") != null) {
                        node.r = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("r").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("g") != null) {
                        node.g = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("g").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("b") != null) {
                        node.b = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("b").getNodeValue());
                    }
                }

                if (node.category.equals("NGram")) {
                    node.shape = ShapeCategory.DISK;
                } else {
                    node.shape = ShapeCategory.SQUARE;
                }



            }

            // debug
            //System.out.println("json: "+node.getAttributesAsJSON());

            //System.out.println(node.category + " " + node.label + " with weight " + node.weight);

            // HACK FOR BAD NGRAMS IN MESO DOCUMENTS GRAPHS
            if (session.macro.graph != this) {
                if (node.category.equals("NGram")) {
                    if (!session.macro.graph.storedNodes.containsKey(node.id)) {
                        System.out.println("Skipping node " + node.label);
                        continue;
                    }
                }
            }

            if (storedNodes.containsKey(id)) {
                storedNodes.get(id).cloneDataFrom(node);
            } else {
                storedNodes.put(id, node);
            }

                if (!normalizeMyNodes.containsKey(node.category)) {
                    normalizeMyNodes.put(node.category, new ArrayList<Node>());
                }
                 normalizeMyNodes.get(node.category).add(node);

        }

        org.w3c.dom.NodeList edges = (org.w3c.dom.NodeList) xml.read("/gexf/graph/edges/edge",
                XPathConstants.NODESET);
        for (int i = 0; i < edges.getLength(); i++) {
            org.w3c.dom.Node xmledge = edges.item(i);
            org.w3c.dom.NamedNodeMap edgeAttributesXML = xmledge.getAttributes();
            if (edgeAttributesXML.getNamedItem("source") == null
                    || edgeAttributesXML.getNamedItem("target") == null) {
                continue;
            }

            String src = edgeAttributesXML.getNamedItem("source").getNodeValue();

            if (src.contains("::")) {
                src = src.split("::")[1];
            }

            int source = src.hashCode();

            String trg = edgeAttributesXML.getNamedItem("target").getNodeValue();

            if (trg.contains("::")) {
                trg = trg.split("::")[1];
            }

            int target = trg.hashCode();

            String type = (edgeAttributesXML.getNamedItem("type") != null)
                    ? edgeAttributesXML.getNamedItem("type").getNodeValue()
                    : "undirected";
            Float weight = (edgeAttributesXML.getNamedItem("weight") != null)
                    ? Float.parseFloat(edgeAttributesXML.getNamedItem("weight").getNodeValue()) : 1.0f;

            if (storedNodes.containsKey(source) && storedNodes.containsKey(target)) {
                storedNodes.get(source).addNeighbour(storedNodes.get(target), weight);
                if (type.equalsIgnoreCase("undirected") | type.equalsIgnoreCase("mutual")) {
                    storedNodes.get(target).addNeighbour(storedNodes.get(source), weight);
                }
            }

        }

        Console.log("normalizing graph..");

        for (Entry<String,List<Node>> e : normalizeMyNodes.entrySet()) {
            //System.out.println("computing metrics for "+e.getKey());
              Metrics metrics = NodeListNormalizer.computeMetrics(e.getValue());
              // System.out.println("normalizing "+e.getKey());
              NodeListNormalizer.normalize(e.getValue(), metrics, "category", e.getKey());
              NodeListNormalizer.normalizePositions(e.getValue(), metrics, "category", e.getKey());
              
              /*
              int i = 0;
              for (Node n : e.getValue()) {
                  n.position.x()
                  //n.position.set((float)Math.random()*100.0f,(float)Math.random()*100.0f,0);
              }*/
        }

 
        locked.set(false);
        commitProperties();
        return true;
    }

    public synchronized NodeList getNodeListCopy() {
        return new NodeList(storedNodes.values());
    }

    public synchronized void putNode(eu.tinasoft.services.data.model.Node node) {
        if (storedNodes.containsKey(node.id)) {
            storedNodes.put(node.id, node);
        } else {
            storedNodes.get(node.id).cloneDataFrom(node);
        }
        commitProperties();
    }

    public synchronized void addNode(eu.tinasoft.services.data.model.Node node) {
        if (!storedNodes.containsKey(node.id)) {
            storedNodes.put(node.id, node);
        }
        commitProperties();
    }

    public synchronized void updateNode(eu.tinasoft.services.data.model.Node node) {
        if (storedNodes.containsKey(node.id)) {
            storedNodes.get(node.id).cloneDataFrom(node);
        }
        commitProperties();
    }

    public synchronized void addNeighbour(eu.tinasoft.services.data.model.Node node1, eu.tinasoft.services.data.model.Node node2, Float weight) {
        if (storedNodes.containsKey(node1.id)) {
            storedNodes.get(node1.id).addNeighbour(node2, weight);
        } else {
            node1.addNeighbour(node2, weight);
            storedNodes.put(node1.id, node1);
        }
        commitProperties();
    }

    public synchronized void addNodes(NodeList nodes) {
        for (eu.tinasoft.services.data.model.Node node : nodes.nodes) {
            addNode(node);
        }
        // TODO touch?
        //System.out.println("Graph.addNodes() and touch() but not computed new metrics!");
        commitProperties();
    }

    public synchronized int size() {
        return storedNodes.size();
    }

    public synchronized eu.tinasoft.services.data.model.Node getNode(int key) {
        return storedNodes.get(key);
    }

    public synchronized void clear() {
        storedNodes.clear();
        nodeAttributes.clear();
        edgeAttributes.clear();
        sessionAttributes.clear();
        commitProperties();
    }


    public synchronized int commitProperties() {
               //System.out.println("incrementing graph revision to "+(revision.get()+1));
        return revision.incrementAndGet();


    }

    @Deprecated
    public synchronized int touch() {
        //System.out.println("incrementing graph revision to "+(revision.get()+1));
        return commitProperties();
    }

    public void selectNodeById(int id) {
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = true;
            //System.out.println("node selected, NOT touching..");
            //commitProperties();
        }
    }

    public void unselectNodeById(int id) {
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = false;
            //commitProperties();
        }

    }

    public synchronized int unselectAll() {
        for (Node n : storedNodes.values()) {
            n.selected = false;
        }
        return 0;// commitProperties();
    }

    public void highlightNodeById(String str) {
        int id = str.hashCode();
        for (Node n : storedNodes.values()) {
            n.isFirstHighlight = (n.id == id);
        }
    }

    public String getNeighbourhoodAsJSON(String id) {

        Node node = getNode(id.hashCode());

        if (node == null) {
            return "{}";
        }
        JSONWriter writer = null;


        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }

        try {
            for (int nodeId : node.weights.keys().elements()) {
                Node n = getNode(nodeId);
                writer.key(n.uuid).object();
                for (Entry<String, Object> entry : n.getAttributes().entrySet()) {
                    writer.key(entry.getKey()).value(JSONEncoder.valueEncoder(entry.getValue()));
                }
                writer.endObject();
            }

        } catch (JSONException jSONException) {
            Console.error("cannot create json: " +jSONException.getMessage());
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error("cannot create json: "+ex.getMessage());
            return "{}";
        }
        if (true) System.out.println("GRAPH.getNeighbourhoodAsJSON: " + writer.toString());
        return writer.toString();
    }

    public String getSelectedNodesAsJSON() {
        String result = "";
        JSONWriter writer = null;
        try {
            writer = new JSONStringer().object();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }

        try {
            for (Entry<Integer,Node> e : storedNodes.entrySet()) {
                Node node = e.getValue();

                if (node.selected) {
                    //System.out.println("node is selected");
                    writer.key(node.uuid).object();
                    writer.key("id").value(node.uuid);
                    for (Entry<String, Object> entry : node.getAttributes().entrySet()) {
                        writer.key(entry.getKey()).value(JSONEncoder.valueEncoder(entry.getValue()));
                    }
                    writer.endObject();
                }
            }

        } catch (JSONException jSONException) {
            Console.error(jSONException.getMessage());
            return "{}";
        }
        try {
            writer.endObject();
        } catch (JSONException ex) {
            Console.error(ex.getMessage());
            return "{}";
        }
       System.out.println("GRAPH.getSelectedNodesAsJSON():"+ writer.toString() );
        return writer.toString();
    }

}
