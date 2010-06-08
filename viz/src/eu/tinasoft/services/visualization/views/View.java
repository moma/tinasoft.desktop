/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.views;

import eu.tinasoft.services.data.transformation.FilterChain;
import java.io.InputStream;
import java.security.KeyException;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import processing.core.PVector;
import eu.tinasoft.services.data.model.NodeList;

import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.model.Graph;
import eu.tinasoft.services.session.Session;

/**
 *
 * @author jbilcke
 */
public class View {

    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean paused = false;

    public PVector translation = new PVector(0.0f, 0.0f);
    public float sceneScale = 1.0f;
    public float inerX;
    public float inerY;
    public float inerZ;
    public float ZOOM_CEIL = 0.026f;
    public float ZOOM_FLOOR = 25.0f;

    public Graph graph = null;
    public FilterChain filters = null;
    public AtomicBoolean hasBeenRead = null;

    public int screenWidth = 100;
    public int screenHeight = 100;
    public Map<String, Object> properties = new HashMap<String, Object>();
    public PVector dragDelta = new PVector(0.0f, 0.0f);

    public int layoutIterationCount = 0;
    public float RECENTERING_MARGIN = 1.2f;

    public View(Session session) {
        graph = new Graph(session);
        filters = new FilterChain(session, this);
        hasBeenRead = new AtomicBoolean(false);

        resetParams();
    }

    protected void resetParams() {
        inerX = 0f;
        inerY = 0f;
        inerZ = 0f;
        sceneScale = 10.0f;

        layoutIterationCount = 0;
    }

    public synchronized boolean tryToMultiplyZoom (float ratio) {
        return tryToSetZoom(sceneScale*ratio);
    }

    public synchronized boolean tryToSetZoom (float newValue) {
        if ((newValue >= ZOOM_CEIL)&&(newValue <= ZOOM_FLOOR)) {
            sceneScale = newValue;
            return true;
        }

        if ((sceneScale <= ZOOM_CEIL && newValue >= sceneScale)|(sceneScale >= ZOOM_FLOOR && newValue <= sceneScale)) {
            sceneScale = newValue;
            return true;
        }
        return false;
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
        properties.put(key, value);
        return true;
    }

    public synchronized Object getProperty(String key) throws KeyException {
        return properties.get(key);
    }

    public synchronized boolean updateFromURI(String uri) {
        resetLayoutCounter();
        return graph.updateFromURI(uri);
    }

    public synchronized boolean updateFromString(String str) {
        resetLayoutCounter();
        return graph.updateFromString(str);
    }

    public synchronized boolean updateFromInputStream(InputStream inputStream) {
        resetLayoutCounter();
        return graph.updateFromInputStream(inputStream);
    }

    public synchronized boolean updateFromNodeList(NodeList nodes) {
        resetLayoutCounter();
        return graph.updateFromNodeList(nodes);

    }

    public void selectNodeById(int id) {
        graph.selectNodeById(id);
    }

    public void unselectNodeById(int id) {
        graph.unselectNodeById(id);
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

    public Node getNode(int nodeId) {
        return getGraph().getNode(nodeId);
    }

    public void resetLayoutCounter() {
        layoutIterationCount = 0;
    }
}
