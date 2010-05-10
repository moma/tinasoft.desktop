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

import eu.tinasoft.services.session.Attribute;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.formats.xml.XPathReader;

/**
 *
 * @author jbilcke
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
                org.w3c.dom.NodeList xmlnodeChildren = (org.w3c.dom.NodeList) attributeXML.getChildNodes();
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
                org.w3c.dom.NodeList xmlnodeChildren = (org.w3c.dom.NodeList) attributeXML.getChildNodes();
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

            String uuid = (String) xmlnodeAttributes.getNamedItem("id").getNodeValue();
            String cat = "BAD_CATEGORY";

            if (uuid.contains("::")) {
                cat = uuid.split("::")[0];
                uuid = uuid.split("::")[1];
            }

            int id = uuid.hashCode();
            
            String label = (xmlnodeAttributes.getNamedItem("label") != null)
                    ? xmlnodeAttributes.getNamedItem("label").getNodeValue()
                    : "" + id;

            Node node = new Node(id, label, (float) Math.random() * 2f,
                    (float) Math.random() * 100f,
                    (float) Math.random() * 100f);

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

            org.w3c.dom.NodeList xmlnodeChildren = (org.w3c.dom.NodeList) xmlnode.getChildNodes();

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
                                    // System.out.println("found attribute "+attrib.toString()+" with key "+attrib.key+" !");

                                    if (attrib.key.equalsIgnoreCase("weight")) {
                             
                                        if (attrib.type == Float.class | attrib.type == Integer.class | attrib.type == Double.class) {
                                            node.weight = Float.parseFloat(attr.getAttributes().getNamedItem("value").getNodeValue());

                                        }
                                    } else if (attrib.key.equalsIgnoreCase("category")) {
                                        if (attrib.type == String.class) {
                                            node.category = attr.getAttributes().getNamedItem("value").getNodeValue();
                                        }
                                    }

                                    // store the attributes in the node map
                                    String nodeValue = attr.getAttributes().getNamedItem("value").getNodeValue();
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
                        node.x = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("x").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("y") != null) {
                        node.y = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("y").getNodeValue());
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
            System.out.println("json: "+node.getAttributesAsJSON());
            
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

            int source  = src.hashCode();

            String trg = edgeAttributesXML.getNamedItem("target").getNodeValue();

            if (trg.contains("::")) {
                trg = trg.split("::")[1];
            }

            int target = trg.hashCode();

            String type = (edgeAttributesXML.getNamedItem("type") != null)
                    ? (String) edgeAttributesXML.getNamedItem("type").getNodeValue()
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

        Console.log("graph loaded!");

        locked.set(false);
        touch();
        return true;
    }

    // call by the drawer when isSynced is false
    public synchronized NodeList getNodeListCopy() {
        NodeList res = new NodeList();
        for (Node n : storedNodes.values()) {
            res.add(n.getProxyClone());
        }
        return res;
    }

    public synchronized void putNode(eu.tinasoft.services.data.model.Node node) {
        if (storedNodes.containsKey(node.id)) {
            storedNodes.put(node.id, node);
        } else {
            storedNodes.get(node.id).cloneDataFrom(node);
        }
        touch();
    }

    public synchronized void addNode(eu.tinasoft.services.data.model.Node node) {
        if (!storedNodes.containsKey(node.id)) {
            storedNodes.put(node.id, node);
        }
        touch();
    }

    public synchronized void updateNode(eu.tinasoft.services.data.model.Node node) {
        if (storedNodes.containsKey(node.id)) {
            storedNodes.get(node.id).cloneDataFrom(node);
        }
        touch();
    }

    public synchronized void addNeighbour(eu.tinasoft.services.data.model.Node node1, eu.tinasoft.services.data.model.Node node2, Float weight) {
        if (storedNodes.containsKey(node1.id)) {
            storedNodes.get(node1.id).addNeighbour(node2, weight);
        } else {
            node1.addNeighbour(node2, weight);
            storedNodes.put(node1.id, node1);
        }
        touch();
    }

    public synchronized void addNodes(NodeList nodes) {
        for (eu.tinasoft.services.data.model.Node node : nodes.nodes) {
            addNode(node);
        }
        // TODO touch?
        System.out.println("Graph.addNodes() and touch() but not computed new metrics!");
        touch();

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
        touch();
    }

    public int touch() {
        //System.out.println("incrementing graph revision to "+(revision.get()+1));
        return revision.incrementAndGet();
    }

    public void selectNodeById(int id) {
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = true;
            System.out.println("node selected, touching..");
              touch();
        }
    }

    public void unselectNodeById(int id) {
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = false;
            touch();
        }

    }

  
    public void unselectAll() {
        for (Node n : storedNodes.values()) {
            n.selected = false;
        }
        touch();
    }
}
