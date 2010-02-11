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

    public class Showprojects {

        public boolean neighbours = false;
        public boolean all = false;
    }

    public class Showkeywords {

        public boolean project = false;
        public boolean batch = false;
        public boolean worldwide = false;
    }

    public class Metrics {

        public float minX = 0.0f;
        public float minY = 0.0f;
        public float maxX = 1.0f;
        public float maxY = 1.0f;
    }

    public class Filter {

        public Map<String, String> keepNodesWith = new HashMap<String, String>();

        public void clear() {
            keepNodesWith.clear();
        }

        public Map<String, tinaviz.Node> keepNodesWith(Map<String, tinaviz.Node> nodes) {
            Map<String, tinaviz.Node> results = new HashMap<String, tinaviz.Node>();

            for (String nk : nodes.keySet()) {

                Node n = nodes.get(nk);

                boolean mustSkip = false;
                for (String k : keepNodesWith.keySet()) {

                    Field f = null;
                    String value = "";

                    try {
                        f = n.getClass().getField(k);
                    } catch (NoSuchFieldException ex) {
                        Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        value = (String) f.get(n);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(Filter.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    if (!value.matches(keepNodesWith.get(k))) {
                        mustSkip = true;
                        break;
                    }

                }

                if (mustSkip) {
                    continue;
                }
                Node result = new Node("");
                result.update(n);
                results.put(result.uuid, result);
            }
            return results;

        }
    }


    public Metrics metrics = new Metrics();
    public Showprojects showProjects = new Showprojects();
    public Showkeywords showKeywords = new Showkeywords();
    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public int maxdeepness = 10;
    public FilterChain filters = new FilterChain();
    public Map<String, tinaviz.Node> storedNodes = new HashMap<String, tinaviz.Node>();
    public Map<String, tinaviz.Node> filteredNodes = new HashMap<String, tinaviz.Node>();

    // public Map<String, tinaviz.Node> nodeMap = new HashMap<String, tinaviz.Node>();
    //public List<tinaviz.Node> nodeList = new ArrayList<tinaviz.Node>();
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
        this.zoom = zoomValue.floatValue();
        System.out.println("zoom: " + zoom);
        Double thresholdValue = (Double) xml.read(meta + "threshold/@value", XPathConstants.NUMBER);
        this.upperThreshold = thresholdValue.floatValue();
        System.out.println("threshold: " + upperThreshold);
        this.lowerThreshold = 0.0f;

        this.selectedNodeID = (String) xml.read(meta + "preselect/@node", XPathConstants.STRING);
        System.out.println("preselected: " + selectedNodeID);

        this.showLabels = (Boolean) xml.read(meta + "labels/@show", XPathConstants.BOOLEAN);
        this.showNodes = (Boolean) xml.read(meta + "nodes/@show", XPathConstants.BOOLEAN);
        this.showLinks = (Boolean) xml.read(meta + "links/@show", XPathConstants.BOOLEAN);

        // reset the graph metrics
        metrics.minX = 0.0f;
        metrics.minY = 0.0f;
        metrics.maxX = 0.0f;
        metrics.maxY = 0.0f;

        System.out.println("showLabels: " + showLabels + "\n");
        System.out.println("showNodes: " + showNodes + "\n");
        System.out.println("showLinks: " + showLinks + "\n");

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


            //org.w3c.dom.Node position = xmlnode.getChildNodes().item(0);
            //org.w3c.dom.Node position = xmlnode.getChildNodes().item(0);
            /*
            Double posx = Double.parseDouble(
            position.getAttributes().getNamedItem("x").getNodeValue()
            );
            Double posy = Double.parseDouble(
            position.getAttributes().getNamedItem("y").getNodeValue()
            );*/

            Node node = new Node(uuid, label, (float) Math.random() * 10f,
                    (float) Math.random() * 400f,
                    (float) Math.random() * 400f);//, posx, posy);

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


            org.w3c.dom.NodeList xmlnodeChildren = (org.w3c.dom.NodeList) xmlnode.getChildNodes();

            for (int j = 0; j < xmlnodeChildren.getLength(); j++) {
                org.w3c.dom.Node n = xmlnodeChildren.item(j);
                if (n.getNodeName() == "attvalues") {
                    // System.out.println("in attributes tag");
                    org.w3c.dom.NodeList xmlattribs = n.getChildNodes();
                    for (int k = 0; k < xmlattribs.getLength(); k++) {
                        org.w3c.dom.Node attr = xmlattribs.item(k);
                        if (attr.getNodeName() == "attvalue") {
                            // System.out.println("in attribute tag");
                            if (attr.getAttributes().getNamedItem("id").getNodeValue().equals("0")) {
                                node.category = attr.getAttributes().getNamedItem("value").getNodeValue();
                                // System.out.println(" - category: "+node.category);

                            } else if (attr.getAttributes().getNamedItem("id").getNodeValue().equals("1")) {
                                node.genericity = Float.parseFloat(attr.getAttributes().getNamedItem("value").getNodeValue());
                                // System.out.println("  - genericity: "+node.genericity );

                            }
                        }

                    }
                }
            }

            System.out.println("selectedNodeID: " + selectedNodeID);
            System.out.println("node uuid: " + uuid);
            //if (selectedNodeID.equals(uuid)) {
            //    node.selected = true;
            //}

            if (storedNodes.containsKey(uuid)) {
                storedNodes.get(uuid).update(node);
            } else {
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
            if (storedNodes.containsKey(source) && storedNodes.containsKey(target)) {
                storedNodes.get(source).addNeighbour(storedNodes.get(target));
            }

        }

        isSynced.set(false);
        //traverseNodes(thirdProject);
        return true;
    }
}
