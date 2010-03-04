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
import processing.core.PVector;
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

    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean animationPaused = false;
    public boolean spatializeWhenMoving = true;
    public boolean centerOnSelection = true;



    public PVector mousePosition = new PVector(0.0f, 0.0f);
    public PVector translation = new PVector(0.0f, 0.0f);
    public PVector lastPosition = new PVector(0.0f, 0.0f);
    public float sceneScale = 1.0f;
    public float inerX;
    public float inerY;
    public float inerZ;

    public float camZ;

    public float ZOOM_CEIL = 2.0f;
    public float ZOOM_FLOOR = 25.0f;
    public float repulsion = 0.01f;
    public float attraction = 0.0001f;

    public Graph graph = null;

    public FilterChain filters = null;
    public AtomicBoolean hasBeenRead = null;

    public View(Graph graph) {
        this.graph = graph;
        filters = new FilterChain(graph);
        hasBeenRead = new AtomicBoolean(false);

        inerX = 0f;
        inerY = 0f;
        inerZ = 0f;

        camZ = 1.0f;

        repulsion = 0.01f;
        attraction = 0.0001f;

        resetCamera();
    }

    public View() {
        graph = new Graph();
        filters = new FilterChain(graph);
        hasBeenRead = new AtomicBoolean(false);

        inerX = 0f;
        inerY = 0f;
        inerZ = 0f;

        camZ = 1.0f;

        repulsion = 0.01f;
        attraction = 0.0001f;
        resetCamera();
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

    public synchronized boolean togglePause() {
        animationPaused = !animationPaused;
        return animationPaused;
    }

    public String getName() {
        return "";
    }

    public boolean cameraIsMoving() {
        return Math.abs(inerX + inerY + inerZ) != 0.0f;
    }

    public boolean cameraIsMoving(float threshold) {
        return Math.abs(inerX + inerY + inerZ) >= threshold;
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

    public void resetCamera() {

        mousePosition = new PVector(0.0f, 0.0f);
        translation = new PVector(0.0f, 0.0f);
        lastPosition = new PVector(0.0f, 0.0f);
        sceneScale = 1.0f;

    }

    public void resetCamera(float width, float height) {

        mousePosition = new PVector(0.0f, 0.0f);
        translation = new PVector(0.0f, 0.0f);
        lastPosition = new PVector(0.0f, 0.0f);
        sceneScale = 1.0f;

        
        // initializes zoom
        PVector box = new PVector(graph.metrics.maxX - graph.metrics.minX , graph.metrics.maxY - graph.metrics.minY);
        float ratioWidth = width / box.x;
        float ratioHeight = height / box.y;
        if (sceneScale == 0) {
        sceneScale = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

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

         

    }

    public void selectNodeById(String id) {
        graph.selectNodeById(id);
    }

    public void unselectNodeById(String id) {
        graph.unselectNodeById(id);
    }

    public Graph getGraph() {
        return graph;
    }
    // call by the drawer when isSynced is false

    public synchronized List<tinaviz.Node> getNodes() {
        List<Node> nodes = filters.getNodes();
        if (!hasBeenRead.get() && nodes != null) {
            hasBeenRead.set(true);
            return nodes;
        }
        return null;
    }

    public void clear() {
        graph.clear();
    }

    public void updateTranslationFrom(int x, int y) {
        Vector tmp = new Vector(0, 0);
        tmp.set(x, y, 0);
        tmp.sub(mousePosition);
        tmp.div(sceneScale); // ensure const. moving speed whatever the zoom is
        tmp.add(lastPosition);

        // todo lock here
        translation.set(tmp);
    }

    public void memorizeLastPosition() {
        lastPosition.set(translation);
    }

    public void storeMousePosition(int x, int y) {
        mousePosition.set(x, y, 0);
    }
}
