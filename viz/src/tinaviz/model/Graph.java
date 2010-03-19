/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import tinaviz.Console;
import tinaviz.Node;

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
    public AtomicInteger revision;
    public float MAX_WEIGHT = 1.0f; // alternative to scale : alpha
    public float MAX_RADIUS = 3.5f; // empirical
    private Session session = null;

    public Graph(Session session) {
        storedNodes = new HashMap<String, tinaviz.Node>();
        attributes = new HashMap<String, Object>();
        metrics = new Metrics();
        locked = new AtomicBoolean(true);
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

            //Console.log("<applet> calling parse XML on "+str);
            return parseXML(xml);

        } catch (XPathExpressionException ex) {
            Console.log(ex.toString());
        }
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

        //System.out.println("threshold: [" + lowerThreshold + "," + upperThreshold + "]");


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

            /*if (xmlnode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE){
            continue;
            }*/
            //System.out.println(xmlnode.getNodeValue());

            org.w3c.dom.NamedNodeMap xmlnodeAttributes = xmlnode.getAttributes();


            //String uuid = nodeAttributes.getNamedItem("id").getNodeValue();
            //String label = nodeAttributes.getNamedItem("label").getNodeValue();



            String uuid = xmlnodeAttributes.getNamedItem("id").getNodeValue();


            String label = (xmlnodeAttributes.getNamedItem("label") != null)
                    ? xmlnodeAttributes.getNamedItem("label").getNodeValue()
                    : uuid;


            Node node = new Node(uuid, label, (float) Math.random() * 2f,
                    (float) Math.random() * 100f,
                    (float) Math.random() * 100f);//, posx, posy);

            node.category = "NGram";

            // update the graph metrics
            if (node.x < metrics.minX) {
                metrics.minX = node.x;
            }
            if (node.x > metrics.maxX) {
                metrics.maxX = node.x;
            }
            if (node.y < metrics.minY) {
                metrics.minY = node.y;
            }
            if (node.y > metrics.maxY) {
                metrics.maxY = node.y;
            }
            if (node.radius < metrics.minRadius) {
                metrics.minRadius = node.radius;
            }
            if (node.radius > metrics.maxRadius) {
                metrics.maxRadius = node.radius;
            }

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

                                    } else if (attrID.getNodeValue().equals("1")) {
                                        //node.genericity = Float.parseFloat(attr.getAttributes().getNamedItem("value").getNodeValue());
                                        // System.out.println("  - genericity: "+node.genericity );
                                    }
                                }
                                
                            }
                        }

                    }
                } else if (n.getNodeName().equals("viz:position") || n.getNodeName().equals("position")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("x") != null) {
                        node.x = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("x").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("y") != null) {
                        node.y = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("y").getNodeValue());
                    }
                } else if (n.getNodeName().equals("viz:size") || n.getNodeName().equals("size")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("value") != null) {
                        // FIXME normalize radius by a max radius, so it is not too big
                        node.radius = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("value").getNodeValue()) * 0.3f;
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
                
                // update the graph metrics
                if (node.x < metrics.minX) {
                    metrics.minX = node.x;
                }
                if (node.x > metrics.maxX) {
                    metrics.maxX = node.x;
                }
                if (node.y < metrics.minY) {
                    metrics.minY = node.y;
                }
                if (node.y > metrics.maxY) {
                    metrics.maxY = node.y;
                }
                if (node.radius < metrics.minRadius) {
                    metrics.minRadius = node.radius;
                }
                if (node.radius > metrics.maxRadius) {
                    metrics.maxRadius = node.radius;
                }


            }

            //System.out.println("selectedNodeID: " + selectedNodeID);

            //if (selectedNodeID.equals(uuid)) {
            //    node.selected = true;
            //}

            // HACK FOR BAD NGRAMS IN MESO DOCUMENTS GRAPHS
            if (session.macro.graph != this)
                if (node.category.equals("NGram"))
                    if (!session.macro.graph.storedNodes.containsKey(node.uuid)) {
                        System.out.println("Skipping node "+node.label);
                        continue;
                    }


            if (storedNodes.containsKey(uuid)) {
                //System.out.println("updating node " + uuid);
                storedNodes.get(uuid).update(node);
            } else {
                //System.out.println("adding node " + uuid);
                storedNodes.put(uuid, node);
                //nodeList.add(node);
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

                // add the weight
                //System.out.println("adding edge "+i+" <"+source+","+target+">");
                storedNodes.get(source).weights.put(target, weight);
                if (weight > metrics.maxWeight) {
                    metrics.maxWeight = weight;
                }
                if (weight < metrics.minWeight) {
                    metrics.minWeight = weight;
                }
            }

        }

        metrics.centerX = metrics.maxX - metrics.minX;
        metrics.centerY = metrics.maxY - metrics.minY;

        // now we need to normalize the graph
        for (Node n : storedNodes.values()) {


            // NORMALIZE COLORS
            if (n.r < 0) {
                n.r = 255 - ((160f / metrics.maxRadius)*n.radius);
            }
            if (n.g < 0) {
                n.g = 255 - ((180f / metrics.maxRadius)*n.radius);
            }
            if (n.b < 0) {
                n.b = 255 - ((150f / metrics.maxRadius)*n.radius);
            }

            // NORMALIZE RADIUS
            n.radius *= MAX_RADIUS / metrics.maxRadius;

            // NORMALIZE WEIGHTS
            float ratio = MAX_WEIGHT / metrics.maxWeight;
            for (String k : n.weights.keySet()) {
                n.weights.put(k,n.weights.get(k)*ratio);
                // System.out.println(""+k+"="+n.weights.get(k)*ratio);
            }

        }

        // TODO update metrics
        metrics.maxRadius = MAX_RADIUS;
        metrics.maxWeight = MAX_WEIGHT;

        Console.log(metrics.toString());

        Console.log("<graph> GEXF loaded!");

        locked.set(false);
        touch();
        return true;
    }

    // call by the drawer when isSynced is false
    public synchronized List<tinaviz.Node> getNodeList() {
        List<tinaviz.Node> res = new ArrayList<tinaviz.Node>();
        for (Node n : storedNodes.values()) {
            res.add(new Node(n));
        }
        return res;
    }

    public synchronized void putNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        } else {
            storedNodes.get(node.uuid).update(node);
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
            storedNodes.get(node.uuid).update(node);
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

        // if (changed) touch();

    }

    public void unselectNodeById(String id) {
         boolean changed = false;
        if (storedNodes.containsKey(id)) {
            storedNodes.get(id).selected = false;
            changed = true;
        }
        // if (changed) touch();
    }

    public void unselectAll() {
        boolean changed = false;
        for (Node n : storedNodes.values()) {
            n.selected = false;
            changed = true;
        }
         // if (changed) touch();
    }


}
