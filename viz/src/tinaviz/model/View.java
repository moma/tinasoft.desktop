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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import processing.xml.XMLElement;


import javax.xml.xpath.XPathConstants;
import tinaviz.Node;
//import org.w3c.dom.*;

/**
 *
 * @author jbilcke
 */
public class View {

    public static final String NS = "tina";
    public int selection = 0;
    public float zoom = 0.5f;
    public float threshold = 0.5f;
    
    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean showPosterOverlay = false;

    public List projects = new ArrayList<Project>();
    public List keywords = new ArrayList<Keyword>();
    public boolean animationPaused = true;
    public boolean colorsDesaturated = false;
    public boolean zoomFrozen = false;
    public boolean showNodeDetails = false;

    public class Showprojects {

        public boolean neighbours = false;
        public boolean all = false;
    }

    public class Showkeywords {

        public boolean project = false;
        public boolean batch = false;
        public boolean worldwide = false;
    }
    public Showprojects showProjects = new Showprojects();
    public Showkeywords showKeywords = new Showkeywords();
    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public int maxdeepness = 10;
    public float MAX_RADIUS = 0.0f;
    public Map<String,tinaviz.Node> nodeMap = new HashMap<String, tinaviz.Node>();
    //public List<tinaviz.Node> nodeList = new ArrayList<tinaviz.Node>();

    public View() {
       
    }

    public View(String uri) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {

            update(uri);

    }

      public boolean update(String uri) throws URISyntaxException, MalformedURLException, IOException, XPathExpressionException {

        XPathReader xml = null;

            xml = new XPathReader(uri);

        String expression = "zoom/@value";
        Double zoomLevel = (Double) xml.read(expression, XPathConstants.NUMBER);
        System.out.println("zoom: " + zoomLevel + "\n");

        this.zoom = zoomLevel.floatValue();

        expression = "/gexf/graph/nodes/node";
        org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList)xml.read(expression,
		XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node xmlnode = nodes.item(i);

            /*if (xmlnode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE){
                continue;
            }*/
            //System.out.println(xmlnode.getNodeValue());
            
            org.w3c.dom.NamedNodeMap nodeAttributes = xmlnode.getAttributes();
            
            org.w3c.dom.NodeList nodeProperties = (org.w3c.dom.NodeList) xmlnode.getChildNodes();

            //String uuid = nodeAttributes.getNamedItem("id").getNodeValue();
            //String label = nodeAttributes.getNamedItem("label").getNodeValue();

            
            String uuid = (nodeAttributes.getNamedItem("id") != null) 
                    ? nodeAttributes.getNamedItem("id").getNodeValue()
                : "id_"+Math.random();

            String label =(nodeAttributes.getNamedItem("label") != null)
                    ? nodeAttributes.getNamedItem("label").getNodeValue()
                : uuid;

            
            
            org.w3c.dom.Node position = xmlnode.getChildNodes().item(0);
/*
            Double posx = Double.parseDouble(
                    position.getAttributes().getNamedItem("x").getNodeValue()
                    );
               Double posy = Double.parseDouble(
                    position.getAttributes().getNamedItem("y").getNodeValue()
                    );*/

             Node node = new Node(uuid, label, (float)Math.random() * 30f,
                     (float)Math.random() * 300f,
                     (float)Math.random() * 300f );//, posx, posy);

             if (nodeMap.containsKey(uuid)) {
                 nodeMap.get(uuid).update(node);
             } else {
                nodeMap.put(uuid, node);
                //nodeList.add(node);
             }


        }

          expression = "/gexf/graph/edges/edge";
        org.w3c.dom.NodeList edges = (org.w3c.dom.NodeList)xml.read(expression,
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
             if (nodeMap.containsKey(source) && nodeMap.containsKey(target)) {
             nodeMap.get(source).addNeighbour(nodeMap.get(target));
             }

        }

        //traverseNodes(thirdProject);
        return true;
    }

}
