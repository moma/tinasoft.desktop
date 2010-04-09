/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.view;

import tinaviz.filters.FilterChain;
import java.io.InputStream;
import java.security.KeyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import processing.core.PVector;
import tinaviz.filters.NodeList;

import tinaviz.graph.Node;
import tinaviz.graph.Graph;
import tinaviz.session.Session;

/**
 *
 * @author jbilcke
 */
public class View {

    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean paused = false;
    
    public boolean highDefinition = false;
    public boolean spatializeWhenMoving = true;
    public PVector translation = new PVector(0.0f, 0.0f);
    public float sceneScale = 10.0f;
    public float inerX;
    public float inerY;
    public float inerZ;
    public float ZOOM_CEIL = 0.7f;
    public float ZOOM_FLOOR = 25.0f;
    public float repulsion = 0.01f;
    public float attraction = 0.001f;

    public Graph graph = null;
    public FilterChain filters = null;
    public AtomicBoolean hasBeenRead = null;
    public int prespatializeSteps = 0;
    public int screenWidth = 100;
    public int screenHeight = 100;
    public Map<String,Object> properties = new HashMap<String, Object>();
    public PVector dragDelta = new PVector(0.0f, 0.0f);
    public CenteringMode centeringMode = CenteringMode.FREE_MOVE;

    public View(Session session) {
        graph = new Graph(session);
        filters = new FilterChain(session, this);
        hasBeenRead = new AtomicBoolean(false);

        repulsion = 0.01f;
        attraction = 0.0001f;
        prespatializeSteps = 0;

        resetParams();
    }

    protected void resetParams() {
        inerX = 0f;
        inerY = 0f;
        inerZ = 0f;
        sceneScale = 10.0f;
    }

    // TODO refactor these two..
    public synchronized boolean toggleLinks() {
        showLinks = !showLinks;
        return showLinks;
    }

    public synchronized boolean toggleEdges() {
        return toggleLinks();
    }

    public synchronized boolean toggleLabels() {
        showLabels = !showLabels;
        return showLabels;
    }

    public synchronized boolean toggleNodes() {
        showNodes = !showNodes;
        return showNodes;
    }

    public synchronized boolean togglePause() {
        paused = !paused;
        return paused;
    }

    public String getName() {
        return "";
    }

    public ViewLevel getLevel() {
        return null;
    }

    public boolean cameraIsMoving() {
        return Math.abs(inerX + inerY + inerZ) != 0.0f;
    }

    public boolean cameraIsMoving(float threshold) {
        return Math.abs(inerX + inerY + inerZ) >= threshold;
    }

    public synchronized boolean setProperty(String key, Object value) throws KeyException {
        // System.out.println("set property "+key+" to "+value+" for view "+getName());
        properties.put(key, value);
        return true;
    }

    public synchronized Object getProperty(String key) throws KeyException {
        return properties.get(key);
    }

    public synchronized boolean updateFromURI(String uri) {
        return graph.updateFromURI(uri);
    }

    public synchronized boolean updateFromString(String str) {
        return graph.updateFromString(str);
    }

    public synchronized boolean updateFromInputStream(InputStream inputStream) {
        return graph.updateFromInputStream(inputStream);
    }

    public synchronized boolean updateFromNodeList(NodeList nodes) {
        return graph.updateFromNodeList(nodes);

    }

    /*
     public void resetCamera() {
        resetZoom();
        switch (centeringMode) {
            case SELECTED_GRAPH_BARYCENTER:
                resetToSelectionBarycenter();
                break;
            default:
                resetToGraphBarycenter();
        }

    }

    public void resetZoom() {

        float screenRadius =
                (screenWidth + screenHeight)
                / 2.0f;

        float zoomScale = 1.0f / (screenRadius / graph.metrics.graphRadius);
        System.out.println("zoomscale = screenRadius / graphRadius = " + screenRadius + " / " + graph.metrics.graphRadius + " = " + zoomScale);
        sceneScale =  1.0f;
    }


    public synchronized void resetToGraphBarycenter() {

        translation.set(graph..center);
        System.out.println("translation1 x:" + translation.x + " y:" + translation.y);
        PVector screenCenter = new PVector(screenWidth/2.0f, screenHeight/2.0f, 0);
        //screenCenter.add(graph.metrics.center);

        translation.sub(screenCenter);
           System.out.println("translation2  x:" + translation.x + " y:" + translation.y);
        translation.mult(sceneScale);
           System.out.println("translation3  x:" +translation.x + " y:" + translation.y);
        translation.add(screenCenter);
         //  System.out.println("translation4  x:" + translation.x + " y:" + translation.y);
    }

    public synchronized void resetToSelectionBarycenter() {
        resetToGraphBarycenter();

        float minX = 0.0f, minY = 0.0f, maxX = 0.0f, maxY = 0.0f, graphWidth = 0.0f, graphHeight = 0.0f;

        boolean ok = false;
        for (Node n : graph.storedNodes.values()) {

            // update the graph metrics
            if (!n.selected) {
                continue;
            }
             ok = true;
            if (n.x < minX) {
                minX = n.x;
            }
            if (n.x > maxX) {
                maxX = n.x;
            }
            if (n.y < minY) {
                minY = n.y;
            }
            if (n.y > maxY) {
                maxY = n.y;
            }

        }
        // no selection..
        if (!ok) return;

        graphWidth = maxX - minX;
        graphHeight = maxY - minY;
        PVector center = new PVector((graphWidth / 2.0f) + minX, (graphHeight / 2.0f) + minY);
        System.out.println("centering to selection with x:" + center.x + " y:" + center.y);
        this.translation.set(center);
    }
    */

    public void selectNodeById(Long id) {
        graph.selectNodeById(id);
    }

    public void unselectNodeById(Long id) {
        graph.unselectNodeById(id);
    }

    public void selectNodeById(String id) {
        graph.selectNodeById(Long.parseLong(id));
    }

    public void unselectNodeById(String id) {
        graph.unselectNodeById(Long.parseLong(id));
    }
    public void unselectAll() {
        graph.unselectAll();
    }

    public Graph getGraph() {
        return graph;
    }
    // call by the drawer when isSynced is false

    public synchronized NodeList popNodes() {
        return filters.popNodes();
    }

    public synchronized void clear() {
    resetParams();
        graph.clear();
    }

    public boolean addFilter(String name, String root) {
        return filters.addFilter(name, root);
    }

    public float setRepulsion(float a) {
        return setAttractionRelative(a, 1.0f);
    }

    public float setAttractionRelative(float a, float scale) {

        float maxAttraction = 0.0004f;
        float minAttraction = 1.5e-5f;
        float ratio = maxAttraction / scale;

        float newValue = minAttraction + a * ratio;
        if (newValue > minAttraction && newValue < maxAttraction) {
            attraction = newValue;
        }
        return a;
    }

    public float getRepulsion() {
        return getAttractionRelative(1.0f);
    }

    public float getAttractionRelative(float scale) {
        return attraction / scale;
    }
}
