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


    public boolean showLabels = true;
    public boolean showNodes = true;
    public boolean showLinks = true;
    public boolean animationPaused = false;
    public Graph graph = null;

    public int selection = 0;
    public String selectedNodeID = "";

    public FilterChain filters = null;

    public AtomicBoolean hasBeenRead = null;

    public View(Graph graph) {
        this.graph = graph;
        filters = new FilterChain(graph);
        hasBeenRead = new AtomicBoolean(false);
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
        this.selectedNodeID = id;
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
}
