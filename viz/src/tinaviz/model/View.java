/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;


import java.io.InputStream;
import java.security.KeyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import processing.core.PVector;

import tinaviz.FilterChain;
import tinaviz.Node;


/**
 *
 * @author jbilcke
 */
public class View {

    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean animationPaused = false;
    public boolean highDefinition = false;
    public boolean spatializeWhenMoving = true;
    public boolean centerOnSelection = true;

    public PVector translation = new PVector(0.0f, 0.0f);
    public PVector lastPosition = new PVector(0.0f, 0.0f);
    public float sceneScale = 1.0f;
    public float inerX;
    public float inerY;
    public float inerZ;
    public float ZOOM_CEIL = 0.7f;
    public float ZOOM_FLOOR = 25.0f;
    public float repulsion = 0.01f;
    public float attraction = 0.0001f;
    public Graph graph = null;
    public FilterChain filters = null;
    public AtomicBoolean hasBeenRead = null;
    public int prespatializeSteps = 0;

    public Map properties = new HashMap<String,Object>();

    public View(Session session) {
        graph = new Graph(session);
        filters = new FilterChain(this);
        hasBeenRead = new AtomicBoolean(false);


        inerX = 0f;
        inerY = 0f;
        inerZ = 0f;

        repulsion = 0.01f;
        attraction = 0.0001f;
        prespatializeSteps = 0;
        resetCamera();
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
        animationPaused = !animationPaused;
        return animationPaused;
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

    public synchronized boolean updateFromNodeList(List<Node> nodes) {
        return graph.updateFromNodeList(nodes);
    }

    public synchronized void resetCamera() {

        translation = new PVector(0.0f, 0.0f);
        lastPosition = new PVector(0.0f, 0.0f);
        sceneScale = 2.0f;

    }

    public synchronized void resetCamera(float width, float height) {

        translation = new PVector(width / 2.0f, width / 2.0f);
        lastPosition = new PVector(0.0f, 0.0f);
        sceneScale = 1.0f;


        // initializes zoom
        PVector box = new PVector(graph.metrics.maxX - graph.metrics.minX, graph.metrics.maxY - graph.metrics.minY);
        float ratioWidth = width / box.x;
        float ratioHeight = height / box.y;
        if (sceneScale == 0) {
            sceneScale = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;
        }

        // initializes move
        PVector semiBox = PVector.div(box, 2);
        PVector topLeftVector = new PVector(graph.metrics.minX, graph.metrics.minY);
        PVector center = new PVector(width / 2f, height / 2f);
        PVector scaledCenter = PVector.add(topLeftVector, semiBox);
        translation.set(center);
        translation.sub(scaledCenter);
        lastPosition.set(translation);
        System.out.println("automatic scaling..");


    }

    public void selectNodeById(String id) {
        graph.selectNodeById(id);
    }

    public void unselectNodeById(String id) {
        graph.unselectNodeById(id);
    }

    public void unselectAll() {
        graph.unselectAll();
    }

    public Graph getGraph() {
        return graph;
    }
    // call by the drawer when isSynced is false

    public synchronized List<tinaviz.Node> popNodes() {
        return filters.popNodes();
    }

    public synchronized void clear() {
        graph.clear();
    }

    public boolean addFilter(String name) {
        return filters.addFilter(name);
    }

    public float setRepulsion(float a) {
        return setAttractionRelative(a,1.0f);
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
