/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import tinaviz.Console;
import tinaviz.FilterChain;
import tinaviz.Node;
import tinaviz.filters.AttributeFilter;
import tinaviz.filters.Filter;
import tinaviz.filters.ForceVector;

/**
 *
 * @author jbilcke
 */
public class View {

    public static final String NS = "tina";
    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean showPosterOverlay = false;
    public boolean animationPaused = false;
    public boolean colorsDesaturated = false;
    public boolean zoomFrozen = false;
    public int selection = 0;
    public float zoom = 0.5f;
    public String selectedNodeID = "";
    public boolean prespatialize = true;
    public FilterChain filters = new FilterChain();
    public Map<String, tinaviz.Node> storedNodes;
    public Metrics metrics;
    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);

    /*
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
     */
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

            xml.parseFromString(str);

            //Console.log("<applet> calling parse XML on "+str);
            return parseXML(xml);

        } catch (URISyntaxException ex) {
            Console.log(ex.toString());
        } catch (MalformedURLException ex) {
            Console.log(ex.toString());
        } catch (IOException ex) {
            Console.log(ex.toString());
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
        } catch (URISyntaxException ex) {
            Console.log(ex.toString());
        } catch (MalformedURLException ex) {
            Console.log(ex.toString());
        } catch (IOException ex) {
            Console.log(ex.toString());
        }
        return false;
    }

    public boolean updateFromNodeList(List<Node> nodes) {
        addNodes(nodes);
        return true;
    }

    private boolean parseXML(XPathReader xml) throws XPathExpressionException {
        String meta = "/gexf/graph/tina/";
            Console.log("<applet> parsing XML..");

        Double zoomValue = (Double) xml.read(meta + "zoom/@value", XPathConstants.NUMBER);

        if (zoomValue != null) {
            System.out.println("zoom: " + zoom);
            this.zoom = zoomValue.floatValue();
        }

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


        // reset the graph metrics
        metrics.minX = 0.0f;
        metrics.minY = 0.0f;
        metrics.maxX = 0.0f;
        metrics.maxY = 0.0f;
        metrics.minRadius = 0.0f;
        metrics.maxRadius = 0.0f;

            Console.log("<applet> parsing XML: reading nodes..");
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
                    (float) Math.random() * 100f,
                    (float) Math.random() * 100f);//, posx, posy);

            node.category = "term";
            
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
                                if (attr.getAttributes().getNamedItem("for") != null) {
                                    if (attr.getAttributes().getNamedItem("for").getNodeValue().equals("0")) {
                                        node.category = attr.getAttributes().getNamedItem("value").getNodeValue();
                                        // System.out.println(" - category: "+node.category);

                                    } else if (attr.getAttributes().getNamedItem("for").getNodeValue().equals("1")) {
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
                        node.radius = Float.parseFloat(xmlnodePositionAttributes.getNamedItem("value").getNodeValue()) * 0.7f;
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
            }

        }

        metrics.centerX = metrics.maxX - metrics.minX;
        metrics.centerY = metrics.maxY - metrics.minY;

        hasBeenRead.set(false);
        //traverseNodes(thirdProject);
        return true;
    }

    // call by the drawer when isSynced is false
    public synchronized List<tinaviz.Node> getNodes() {

        if (true) {
            return new ArrayList(storedNodes.values());
        } else {
            if (filters.filtered.get()) {
                hasBeenRead.set(true);
                return filters.filteredNodes;
            } else {
                // filter still not ready, drawer will have to wait a bit more
                return null;
            }
        }
    }

    public void putNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        } else {
            storedNodes.get(node.uuid).update(node);
        }
    }

    public void addNode(tinaviz.Node node) {
        if (!storedNodes.containsKey(node.uuid)) {
            storedNodes.put(node.uuid, node);
        }
    }

    public void updateNode(tinaviz.Node node) {
        if (storedNodes.containsKey(node.uuid)) {
            storedNodes.get(node.uuid).update(node);
        }
    }

    public void addNeighbour(tinaviz.Node node1, tinaviz.Node node2) {
        if (storedNodes.containsKey(node1.uuid)) {
            storedNodes.get(node1.uuid).addNeighbour(node2);
        } else {
            node1.addNeighbour(node2);
            storedNodes.put(node1.uuid, node1);

        }
    }

    public void addNodes(List<tinaviz.Node> nodes) {
        for (tinaviz.Node node : nodes) {
            addNode(node);
        }
    }

    public int size() {
        return storedNodes.size();
    }

    public tinaviz.Node getNode(String key) {
        return storedNodes.get(key);
    }

    public void clear() {
        storedNodes.clear();
    }

    public synchronized boolean toggleLinks() {
        showLinks = !showLinks;
        return showLinks;
    }

    public synchronized boolean toggleLabels() {
        showLabels = !showLabels;
        return showLabels;
    }

    public synchronized boolean toggleNodes() {
        showNodes = !showNodes;
        return showNodes;
    }

    public synchronized boolean togglePosterOverlay() {
        showPosterOverlay = !showPosterOverlay;
        return showPosterOverlay;
    }

    public synchronized boolean togglePause() {
        animationPaused = !animationPaused;
        return animationPaused;
    }

    public synchronized boolean createFilter(String filterName, String model) {
        Filter f = null;
        if (model.equals("RegexMatch")) {
            f = new AttributeFilter();
        } else if (model.equals("ForceVector")) {
            f = new ForceVector();
        }
        filters.addFilter(filterName, f);
        return true;
    }

    public synchronized boolean filterConfig(String filterName, String key, String value) throws KeyException {
        filters.getFilter(filterName).setField(key, value);
        return true;
    }

    public synchronized boolean filterConfig(String filterName, String key, float value) throws KeyException {
        filters.getFilter(filterName).setField(key, value);
        return true;
    }

    public synchronized boolean filterConfig(String filterName, String key, int value) throws KeyException {
        filters.getFilter(filterName).setField(key, value);
        return true;
    }

    public synchronized boolean filterConfig(String filterName, String key, boolean value) throws KeyException {
        filters.getFilter(filterName).setField(key, value);
        return true;
    }

    public synchronized Object filterConfig(String filterName, String key) throws KeyException {
        return filters.getFilter(filterName).getField(key);
    }

    public void selectNodeById(String id) {
        for (Node n : storedNodes.values()) {
            n.selected = true;
        }
    }
}
