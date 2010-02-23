/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import processing.xml.XMLElement;


import javax.xml.xpath.XPathConstants;
import tinaviz.FilterChain;
import tinaviz.filters.FilterChannel;
import tinaviz.Node;
import tinaviz.filters.Channel;
//import org.w3c.dom.*;

/**
 * Link-less design
 * @author jbilcke
 */
public class Session {

    public static final String NS = "tina";
    public int selection = 0;
    public float zoom = 0.5f;
    public float upperThreshold = 1.0f;
    public float lowerThreshold = 0.0f;
    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean showPosterOverlay = false;
    public boolean animationPaused = false;
    public boolean colorsDesaturated = false;
    public boolean zoomFrozen = false;
    public boolean showNodeDetails = false;
    public String selectedNodeID = "";
    public boolean prespatialize = true;
    
    GlobalNetwork global = new GlobalNetwork();
    LocalNetwork local = new LocalNetwork();

    public NetworkMode explorationMode = NetworkMode.GLOBAL;

    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public int maxdeepness = 10;

    public FilterChain filters = new FilterChain();


    // RUNTIME DATA, NOT SERIALIZED
    public float MAX_RADIUS = 0.0f;
    public AtomicBoolean isSynced = new AtomicBoolean(false);

    public Session() {
    }

    public Session(String uri) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {
        updateFromURI(uri);
    }

    public boolean updateFromURI(String uri) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {
        XPathReader xml = new XPathReader();
        xml.parseFromURI(uri);
        return parseXML(xml);
    }

    public boolean updateFromString(String str) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {
        XPathReader xml = new XPathReader();
        xml.parseFromString(str);
        return parseXML(xml);
    }

    public boolean updateFromInputStream(InputStream inputStream) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {
        XPathReader xml = new XPathReader();
        xml.parseFromStream(inputStream);
        return parseXML(xml);
    }

    private boolean parseXML(XPathReader xml) throws XPathExpressionException {
        String meta = "/gexf/graph/tina/";

        Double zoomValue = (Double) xml.read(meta + "zoom/@value", XPathConstants.NUMBER);
        if (zoomValue != null) {
            System.out.println("zoom: " + zoom);
            this.zoom = zoomValue.floatValue();
        }


        Double thresholdValue = (Double) xml.read(meta + "threshold/@min", XPathConstants.NUMBER);
        if (thresholdValue != null)
            lowerThreshold = thresholdValue.floatValue();

         thresholdValue = (Double) xml.read(meta + "threshold/@max", XPathConstants.NUMBER);
         if (thresholdValue != null)
             upperThreshold = thresholdValue.floatValue();

        System.out.println("threshold: [" + lowerThreshold + "," + upperThreshold + "]");


        String selected = (String) xml.read(meta + "select/@node", XPathConstants.STRING);
        if (selected != null) {
            selectedNodeID = selected;
            System.out.println("selected node: " + selectedNodeID);
        }

        Boolean cond = (Boolean) xml.read(meta + "labels/@show", XPathConstants.BOOLEAN);
        if (cond != null) {
            showLabels = cond;
            System.out.println("showLabels: " + showLabels);
        }

        cond = (Boolean) xml.read(meta + "nodes/@show", XPathConstants.BOOLEAN);
        if (cond != null) {
            showNodes = cond;
            System.out.println("showNodes: " + showNodes);
        }

        cond = (Boolean) xml.read(meta + "links/@show", XPathConstants.BOOLEAN);
        if (cond != null) {
            showLinks = cond;
            System.out.println("showLinks: " + showLinks);
        }

         cond = (Boolean) xml.read(meta + "layout/@show", XPathConstants.BOOLEAN);
        if (cond != null) {
            animationPaused = cond;
            System.out.println("animationPaused: " + animationPaused);
        }

         cond = (Boolean) xml.read(meta + "layout/@prespatialize", XPathConstants.BOOLEAN);
        if (cond != null) {
            prespatialize = cond;
            System.out.println("prespatialize: " + prespatialize);
        }

        Network net = getNetwork();


        // reset the graph metrics
        net.metrics.minX = 0.0f;
        net.metrics.minY = 0.0f;
        net.metrics.maxX = 0.0f;
        net.metrics.maxY = 0.0f;


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



            Node node = new Node(uuid, label, (float) Math.random() * 10f,
                    0.0f,
                    0.0f);//, posx, posy);
            // update the graph metrics
            if (node.x < net.metrics.minX) {
                net.metrics.minX = node.x;
            }
            if (node.x > net.metrics.maxX) {
                net.metrics.maxX = node.x;
            }
            if (node.y < net.metrics.minY) {
                net.metrics.minY = node.y;
            }
            if (node.y > net.metrics.maxY) {
                net.metrics.maxY = node.y;
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
                                if (attr.getAttributes().getNamedItem("id") != null) {
                                    if (attr.getAttributes().getNamedItem("id").getNodeValue().equals("0")) {
                                        node.category = attr.getAttributes().getNamedItem("value").getNodeValue();
                                        // System.out.println(" - category: "+node.category);

                                    } else if (attr.getAttributes().getNamedItem("id").getNodeValue().equals("1")) {
                                        //node.genericity = Float.parseFloat(attr.getAttributes().getNamedItem("value").getNodeValue());
                                        // System.out.println("  - genericity: "+node.genericity );
                                    }
                                }
                            }
                        }

                    }
                } else if (n.getNodeName().equals("viz:position") || n.getNodeName().equals("position")) {
                    org.w3c.dom.NamedNodeMap xmlnodePositionAttributes = n.getAttributes();
                    if (xmlnodePositionAttributes.getNamedItem("size") != null) {
                        node.radius = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("size").getNodeValue()) * 1.0f;
                    }
                    if (xmlnodePositionAttributes.getNamedItem("x") != null) {
                        node.x = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("x").getNodeValue());
                    }
                    if (xmlnodePositionAttributes.getNamedItem("y") != null) {
                        node.y = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("y").getNodeValue());
                    }
                }
                // update the graph metrics
                if (node.x < net.metrics.minX) {
                    net.metrics.minX = node.x;
                }
                if (node.x > net.metrics.maxX) {
                    net.metrics.maxX = node.x;
                }
                if (node.y < net.metrics.minY) {
                    net.metrics.minY = node.y;
                }
                if (node.y > net.metrics.maxY) {
                    net.metrics.maxY = node.y;
                }
                if (node.radius < net.metrics.minRadius) {
                    net.metrics.minRadius = node.radius;
                }
                if (node.radius > net.metrics.maxRadius) {
                    net.metrics.maxRadius = node.radius;
                }

            }

            //System.out.println("selectedNodeID: " + selectedNodeID);

            //if (selectedNodeID.equals(uuid)) {
            //    node.selected = true;
            //}

            if (net.storedNodes.containsKey(uuid)) {
                //System.out.println("updating node " + uuid);
                net.storedNodes.get(uuid).update(node);
            } else {
                //System.out.println("adding node " + uuid);
                net.storedNodes.put(uuid, node);
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

            if (net.storedNodes.containsKey(source) && net.storedNodes.containsKey(target)) {
                net.storedNodes.get(source).addNeighbour(net.storedNodes.get(target));

                // add the weight
                //System.out.println("adding edge "+i+" <"+source+","+target+">");
                net.storedNodes.get(source).weights.put(target, weight);
            }

        }

        isSynced.set(false);
        //traverseNodes(thirdProject);
        return true;
    }


    // call by the drawer when isSynced is false
    public synchronized List<tinaviz.Node> getNodes() {
        
        if (true) {
            return new ArrayList(getStoredNodes().values());
        } else {
            if (filters.filtered.get()) {
                isSynced.set(true);
                return filters.filteredNodes;
            } else {
                // filter still not ready, drawer will have to wait a bit more
                return null;
            }
        }
    }

   public void putNode(Node node) {
        getNetwork().putNode(node);
    }

   public void addNode(Node node) {
        getNetwork().addNode(node);
    }

   public void updateNode(Node node) {
        getNetwork().addNode(node);
    }

   public void addNeighbour(Node node1, Node node2) {
       getNetwork().addNeighbour(node1,node2);
    }

    public Network getNetwork() {
        return (explorationMode == NetworkMode.LOCAL)
                ? local : global;
    }

    public Map<String, tinaviz.Node> getStoredNodes() {
        return (explorationMode == NetworkMode.LOCAL)
                ? local.storedNodes : global.storedNodes;
    }

    public void clear() {
        local.clear();
        global.clear();
        isSynced.set(false);
    }

    public void switchToLocalExploration() {
        explorationMode = NetworkMode.LOCAL;
        isSynced.set(false);
    }

    public void switchToGlobalExploration() {
        explorationMode = NetworkMode.GLOBAL;
        isSynced.set(false);
    }
}
