/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations;

import java.security.KeyException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import tinaviz.Node;
import tinaviz.transformations.filters.ThresholdWeight;

import tinaviz.transformations.Filter;
import tinaviz.transformations.FilterChainListener;
import tinaviz.transformations.filters.Category;
import tinaviz.transformations.filters.NodeRadius;
import tinaviz.model.Session;
import tinaviz.model.View;
import tinaviz.transformations.filters.Explorer;
import tinaviz.transformations.filters.Layout;
import tinaviz.transformations.filters.SubGraphCopy;

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
    public View view = null;
    public Session session = null;
    private FilterThread thread = null;


    private class FilterThread extends Thread {

        private List<Node> nodes;
        private FilterChain chain;
        private View view;
        private Session session;


        public FilterThread(FilterChain chain, Session session, View view, List<Node> nodes) {
            this.session = session;
            this.view = view;
            this.nodes = nodes;
            this.chain = chain;
        }

        @Override
        public void run() {
            System.out.println("filter started!..");
            List<Node> result = (nodes != null) ? nodes : new LinkedList<Node>();
            for (Filter f : filters.values()) {
                System.out.println("processing filter "+f.getRoot());
                if (interrupted()) {
                    System.out.println("we're interrupted!");
                    return;
                }
                result = f.process(session, view, result);
            }
            chain.filteredNodes = result;
            System.out.println("filter finished to process "+result.size()+" nodes! setting flash 'ready' to true..");
            chain.filterIsRunning.set(false);
            chain.popLocked.set(false);
        }
    }

    public FilterChain(Session session, View view) {

        filteredNodes = new LinkedList<Node>();
        popLocked = new AtomicBoolean(true);
        filters = new HashMap<String, Filter>();
        listeners = new ArrayList<FilterChainListener>();
        thread = null;
        this.view = view;
        this.session = session;
    }

    public boolean addFilter(String filterName, String root) {
        System.out.println("addFilter called! filter:"+filterName+" root:"+root);
        if (filterName.equals("ThresholdWeight")) {
            Filter f = new ThresholdWeight();
            f.setRoot(root);
            filters.put("ThresholdWeight "+filters.size(), f);
        } else if (filterName.equals("NodeRadius")) {
            Filter f = new NodeRadius();
            f.setRoot(root);
            filters.put("NodeRadius "+filters.size(), f);
        } else if (filterName.equals("Category")) {
            Filter f = new Category();
            f.setRoot(root);
            filters.put("Category "+filters.size(), f);
        } else if (filterName.equals("Explorer")) {
            Filter f = new Explorer();
            f.setRoot(root);
            filters.put("Explorer "+filters.size(), f);
        } else if (filterName.equals("SubGraphCopy")) {
            Filter f = new SubGraphCopy();
            f.setRoot(root);
            filters.put("SubGraphCopy "+filters.size(), f);
        }  else if (filterName.equals("Layout")) {
            Filter f = new Layout();
            f.setRoot(root);
            filters.put("Layout "+filters.size(), f);
        }  else {
            return false;
        }
        System.out.println("addFilter SUCCESS!");
        return true;
    }

    public Filter getFilter(String filterKey) {
        if (!filters.containsKey(filterKey)) {
            return filters.get(filterKey);
        }
        return null;
    }

    public void enableFilter(String key) {
        if (filters.containsKey(key)) {
            filters.get(key).setEnabled(true);
        }
    }

    public void disableFilter(String key) {
        if (filters.containsKey(key)) {
            filters.get(key).setEnabled(false);
        }
    }

    public boolean toggleFilter(String key) {
        if (filters.containsKey(key)) {
            filters.get(key).setEnabled(!filters.get(key).enabled());
        }
        return false;
    }

    public synchronized List<Node> popNodes() {

        // we are already filtering, please wait..
        if (filterIsRunning.get()) {
            System.out.println("Filters are already running, please wait..");
            return null;
        }

        // if we are up to date,
        if (graphRevision.get() == view.graph.revision.get()) {
            // check if we already popped
            if (!popLocked.get()) {
                System.out.println("Filters are up to date, not running, and not popped.. we return filtered nodes!");
                popLocked.set(true); // no more popping!
                return filteredNodes;
            } else {
                // the most common case, so don't show this log..
                //System.out.println("Filter is up to date, but have already been popped!..");
                return null;
            }

        }

        // experimental
        
        if (!popLocked.get()) {
                System.out.println("Filters are up to date, not running, and not popped.. we return filtered nodes!");
                popLocked.set(true); // no more popping!
                return filteredNodes;
         }


        System.out.println("Filter is outdated, checking if we can run a new filter thread..");

        // we have to wait for the graph to be unlocked
        if (view.graph.locked.get()) {
            System.out.println("Graph is locked.. probably parsing some XML!");
            return null;
        }

        System.out.println("okay, graph looks unlocked, starting new filter thread..");
        filterIsRunning.set(true);
        thread = new FilterThread(this, session, view, view.graph.getNodeListCopy());
        thread.start();

        System.out.println("updating filter's graph revision!");
        graphRevision.set(view.graph.revision.get());
        return null;

    }

    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
