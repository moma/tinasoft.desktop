/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.security.KeyException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tinaviz.filters.FilterChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;

import tinaviz.filters.Channel;
import tinaviz.filters.Filter;
import tinaviz.filters.FilterChainListener;
import tinaviz.model.Graph;

/**
 *
 * @author jbilcke
 */
public class FilterChain {

    public List<Node> filteredNodes = null;
    public AtomicBoolean popLocked = null;
    public AtomicBoolean filterIsRunning = new AtomicBoolean(false);
    public AtomicInteger graphRevision = new AtomicInteger(0);
    private Map<String, Filter> filters = null;
    private List<FilterChainListener> listeners = null;
    public Graph graph = null;
    private FilterThread thread = null;

    private class FilterThread extends Thread {

        private List<Node> nodes;
        private FilterChain chain;

        public FilterThread(FilterChain chain) {
            this.nodes = null;
            this.chain = chain;
        }

        public FilterThread(FilterChain chain, List<Node> nodes) {
            this.nodes = nodes;
            this.chain = chain;
        }

        @Override
        public void run() {
            System.out.println("filter started!..");
            List<Node> result = (nodes != null) ? nodes : new ArrayList<Node>();
            for (Filter f : filters.values()) {
                if (interrupted()) {
                    System.out.println("we're interrupted!");
                    return;
                }
                result = f.process(result);
            }
            chain.filteredNodes = result;
            System.out.println("filter finished! setting flash 'ready' to true..");
            chain.filterIsRunning.set(false);
            chain.popLocked.set(false);
        }
    }

    public FilterChain(Graph graph) {

        filteredNodes = new ArrayList<Node>();
        popLocked = new AtomicBoolean(true);
        filters = new HashMap<String, Filter>();
        listeners = new ArrayList<FilterChainListener>();
        thread = new FilterThread(this);
        this.graph = graph;
    }

    public void addFilter(String filterKey, Filter filter) {
        if (!filters.containsKey(filterKey)) {
            filters.put(filterKey, filter);
        }
    }

    public Filter getFilter(String filterKey) {
        if (!filters.containsKey(filterKey)) {
            return filters.get(filterKey);
        }
        return null;
    }

    public void enableFilter(String key) {
        if (filters.containsKey(key)) {
            try {
                filters.get(key).setField("enabled", true);
            } catch (KeyException ex) {
                Logger.getLogger(FilterChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void disableFilter(String key) {
        if (filters.containsKey(key)) {
            try {
                filters.get(key).setField("enabled", false);
            } catch (KeyException ex) {
                Logger.getLogger(FilterChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean toggleFilter(String key) {
        if (filters.containsKey(key)) {
            try {
                filters.get(key).setField("enabled", !(Boolean) filters.get(key).getField("enabled"));
            } catch (KeyException ex) {
                Logger.getLogger(FilterChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public synchronized List<Node> popNodes() {

        // we are already filtering, please wait..
        if (filterIsRunning.get()) {
            System.out.println("Filter is already running, please wait..");
            return null;
        }

        // if we are up to date,
        if (graphRevision.get() == graph.revision.get()) {
            System.out.println("Filter is up to date, and not running..");
            // check if we already popped
            if (!popLocked.get()) {
                System.out.println("Filter is up to date, but have already been popped!..");
                popLocked.set(true); // no more popping!
                return filteredNodes;
            }
            return null;
        }

        // we have to wait for the graph to be unlocked
        if (graph.locked.get()) {
            System.out.println("Graph is locked.. probably parsing some XML!");
            return null;
        }

        System.out.println("okay, graph looks unlocked, starting new filter thread..");
        filterIsRunning.set(true);
        thread = new FilterThread(this, graph.getNodeList());
        thread.start();

        System.out.println("updating filter revision!");
        graphRevision.set(graph.revision.get());
        return null;

    }

    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
