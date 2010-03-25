/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import tinaviz.Console;
import tinaviz.Node;
import processing.core.*;

/**
 *
 * @author jbilcke
 */
public class Graph implements Cloneable {

    public static final String NS = "tina";
    public Map<String, tinaviz.Node> storedNodes = null;
    public Map<String, Object> attributes = null;
    public Metrics metrics = null;
    public AtomicBoolean locked = null;
    public AtomicBoolean needToBeReadAgain = null;
    public AtomicBoolean brandNewGraph = null;
    public AtomicInteger revision;
    public float MIN_WEIGHT = 0.0f;
    public float MAX_WEIGHT = 1.0f; // desired default weight
    public float MIN_RADIUS = 0.01f;
    public float MAX_RADIUS = 1.0f; // largely depends on the spatialization settings
    public float MIN_GENERICITY = 1.0f;
    public float MAX_GENERICITY = 2.0f;
    private Session session = null;

    public Graph(Session session) {
        storedNodes = new HashMap<String, tinaviz.Node>();
        attributes = new HashMap<String, Object>();
        metrics = new Metrics();
        locked = new AtomicBoolean(true);
        brandNewGraph = new AtomicBoolean(false);
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

    public boolean updateFromNodeList(List<Node> nodes) {
        addNodes(nodes);
        return true;
    }

    private boolean parseXML(XPathReader xml) throws XPathExpressionException {
        brandNewGraph.set(storedNodes.size() == 0);
        locked.set(true);
        String meta = "/gexf/graph/tina/";
        Console.log("<applet> reading GEXF..");

        Double zoomValue = (Double) xml.read(meta + "zoom/@value", XPathConstants.NUMBER);
        attributes.put("zoom", (zoomValue != null) ? zoomValue.floatValue() : 1.0f);

        Double thresholdValue = (Double) xml.read(meta + "threshold/@min", XPathConstants.NUMBER);
        if (thresholdValue != null) {
            //lowerThreshold = thresholdValue.floatValue();
        }

        thresholdValue = (Double) xml.read(meta + "threshold/@max", XPathConstants.NUMBER);
        if (thresholdValue != null) {
            //upperThreshold = thresholdValue.floatValue();
        }

        String selected = (String) xml.read(meta + "select/@node", XPathConstants.STRING);
        attributes.put("selected", (selected != null) ? selected : "");

        Boolean cond = (Boolean) xml.read(meta + "labels/@show", XPathConstants.BOOLEAN);
        attributes.put("showLabels", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "nodes/@show", XPathConstants.BOOLEAN);
        attributes.put("showNodes", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "links/@show", XPathConstants.BOOLEAN);
        attributes.put("showLinks", (cond != null) ? cond : true);


        cond = (Boolean) xml.read(meta + "layout/@show", XPathConstants.BOOLEAN);
        attributes.put("animationPaused", (cond != null) ? cond : true);

        cond = (Boolean) xml.read(meta + "layout/@prespatialize", XPathConstants.BOOLEAN);
        attributes.put("prespatialize", (cond != null) ? cond : true);


        // reset the graph metrics
        metrics.reset();

        org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) xml.read("/gexf/graph/nodes/node",
                XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node xmlnode = nodes.item(i);

            org.w3c.dom.NamedNodeMap xmlnodeAttributes = xmlnode.getAttributes();

            String uuid = xmlnodeAttributes.getNamedItem("id").getNodeValue();

            String label = (xmlnodeAttributes.getNamedItem("label") != null)
                    ? xmlnodeAttributes.getNamedItem("label").getNodeValue()
                    : uuid;

            Node node = new Node(uuid, label, (float) Math.random() * 2f,
                    (float) Math.random() * 100f,
                    (float) Math.random() * 100f);

            node.category = "Document";

            org.w3c.dom.NodeList xmlnodeChildren = (org.w3c.dom.NodeList) xmlnode.getChildNodes();

            for (int j = 0; j < xmlnodeChildren.getLength(); j++) {
                org.w3c.dom.Node n = xmlnodeChildren.item(j);
                if (n.getNodeName().equals("attvalues")) {
                    // System.out.println("in attributes tag");
                    org.w3c.dom.NodeList xmlattribs = n.getChildNodes();
                    for (int k = 0; k < xmlattribs.getLength(); k++) {
                        org.w3c.dom.Node attr = xmlattribs.item(k);
                        if (attr.getNodeName().equals("attvalue")) {
                            // System.out.println("in attribute tag");
                            if (attr.getAttributes() != null) {
                                org.w3c.dom.Node attrID = null;
                                if (attr.getAttributes().getNamedItem("for") != null) {
                                    attrID = attr.getAttributes().getNamedItem("for");
                                } else {
                                    // maybe this is an old gexf..
                                    attrID = attr.getAttributes().getNamedItem("id");
                                }
                                if (attrID != null) {
                                    if (attrID.getNodeValue().equals("0")) {
                                        node.category = attr.getAttributes().getNamedItem("value").getNodeValue();
                                        // System.out.println(" - category: "+node.category);

                                    } else if (attrID.getNodeValue().equals("4")) {
                                        node.genericity = Float.parseFloat(attr.getAttributes().getNamedItem("value").getNodeValue());
                                    }
                                }

                            }
                        }

                    }
                } else if (n.getNodeName().equals("viz:position") || n.getNodeName().equals("position")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("x") != null) {
                        //node.x = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("x").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("y") != null) {
                        //node.y = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("y").getNodeValue());
                    }
                } else if (n.getNodeName().equals("viz:size") || n.getNodeName().equals("size")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("value") != null) {
                        // FIXME normalize radius by a max radius, so it is not too big
                        node.radius = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("value").getNodeValue());
                    }
                } else if (n.getNodeName().equals("viz:color") || n.getNodeName().equals("color")) {
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

            // HACK FOR BAD NGRAMS IN MESO DOCUMENTS GRAPHS
            if (session.macro.graph != this) {
                if (node.category.equals("NGram")) {
                    if (!session.macro.graph.storedNodes.containsKey(node.uuid)) {
                        System.out.println("Skipping node " + node.label);
                        continue;
                    }
                }
            }

            if (storedNodes.containsKey(uuid)) {
                storedNodes.get(uuid).cloneDataFrom(node);
            } else {
                storedNodes.put(uuid, node);
            }

        }

        org.w3c.dom.NodeList edges = (org.w3c.dom.NodeList) xml.read("/gexf/graph/edges/edge",
                XPathConstants.NODESET);
        for (int i = 0; i < edges.getLength(); i++) {
            org.w3c.dom.Node xmledge = edges.item(i);
            org.w3c.dom.NamedNodeMap edgeAttributes = xmledge.getAttributes();
            if (edgeAttributes.getNamedItem("source") == null
                    || edgeAttributes.getNamedItem("target") == null) {
                continue;
            }

            String source = edgeAttributes.getNamedItem("source").getNodeValue();
            String target = edgeAttributes.getNamedItem("target").getNodeValue();
            Float weight = (edgeAttributes.getNamedItem("weight") != null)
                    ? Float.parseFloat(edgeAttributes.getNamedItem("weight").getNodeValue()) : 1.0f;

            if (storedNodes.containsKey(source) && storedNodes.containsKey(target)) {
                storedNodes.get(source).addNeighbour(storedNodes.get(target));
                storedNodes.get(source).weights.put(target, weight);
            }

        }

        metrics.compute(this);

        // now we need to normalize the graph
        for (Node n : storedNodes.values()) {

            // NORMALIZE RADIUS
            //System.out.println("node "+n.label+" ("+n.category+")");
            //System.out.println(" - radius avant:"+n.radius);

            n.radius = PApplet.map(n.radius,
                    metrics.minRadius, metrics.maxRadius,
                    MIN_RADIUS, MAX_RADIUS);
            // System.out.println(" -  normalized radius:"+n.radius);

            // NORMALIZE COLORS USING RADIUS
            if (n.r < 0) {
                n.r = 255 - 160 * n.radius;
            }
            if (n.g < 0) {
                n.g = 255 - 160 * n.radius;
            }
            if (n.b < 0) {
                n.b = 255 - 160 * n.radius;
            }


            // NORMALIZE GENERICITY
            n.genericity = PApplet.map(n.genericity,
                    metrics.minGenericity, metrics.maxGenericity,
                    MIN_GENERICITY, MAX_GENERICITY);
            //System.out.println("normalized genericity:"+n.genericity+"\n");

            // NORMALIZE WEIGHTS
            for (String k : n.weights.keySet()) {
                System.out.println("  - w1: "+n.weights.get(k));
                n.weights.put(k, PApplet.map(n.weights.get(k),
                        metrics.minWeight, metrics.maxWeight,
                        MIN_WEIGHT, MAX_WEIGHT));
                System.out.println("  - w2: "+n.weights.get(k));
            }


        }


        Console.log(metrics.toString());
        Console.log("applet: gexf successfully imported.");

        locked.set(false);
        touch();
        return true;
    }

    // call by the drawer when isSynced is false
    public synchronized List<tinaviz.Node> getNodeListCopy() {
        List<tinaviz.Node> res = new LinkedList<tinaviz.Node>();
        for (Node n : storedNodes.values()) {
            res.add(n.getProxyClone());
        }
        return res;
    }

    public synchronized void putNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        } else {
            storedNodes.get(node.uuid).cloneDataFrom(node);
        }
        touch();
    }

    public synchronized void addNode(tinaviz.Node node) {
        if (!storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        }
        touch();
    }

    public synchronized void updateNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.get(node.uuid).cloneDataFrom(node);
        }
        touch();
    }

    public synchronized void addNeighbour(tinaviz.Node node1, tinaviz.Node node2) {
        if (storedNodes.containsKey(node1.uuid)) {
            storedNodes.get(node1.uuid).addNeighbour(node2);
        } else {
            node1.addNeighbour(node2);
            storedNodes.put(node1.uuid, node1);

        }
        touch();
    }

    public synchronized void addNodes(List<tinaviz.Node> nodes) {
        for (tinaviz.Node node : nodes) {
            addNode(node);
        }
    }

    public synchronized int size() {
        return storedNodes.size();
    }

    public synchronized tinaviz.Node getNode(String key) {
        return storedNodes.get(key);
    }

    public synchronized void clear() {
        storedNodes.clear();
        attributes.clear();
        touch();
    }

    public int touch() {
        //System.out.println("incrementing graph revision to "+(revision.get()+1));
        return revision.incrementAndGet();
    }

    public void selectNodeById(String id) {
        boolean changed = false;
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = true;
            changed = true;
        }

        if (changed) {
            touch();
        }

    }

    public void unselectNodeById(String id) {
        boolean changed = false;
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = false;
            changed = true;
        }
        //if (changed) touch();
    }

    public void unselectAll() {
        boolean changed = false;
        for (Node n : storedNodes.values()) {
            n.selected = false;
            changed = true;
        }
        //if (changed) touch();
    }
}
