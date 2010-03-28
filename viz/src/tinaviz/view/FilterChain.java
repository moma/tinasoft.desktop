/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import tinaviz.graph.Node;
import tinaviz.filters.ThresholdWeight;


import tinaviz.filters.Category;
import tinaviz.filters.NodeRadius;
import tinaviz.session.Session;
import tinaviz.view.View;
import tinaviz.filters.Explorer;
import tinaviz.filters.Layout;
import tinaviz.filters.NodeFunction;
import tinaviz.filters.SubGraphCopy;
import tinaviz.filters.ThresholdGenericity;
import tinaviz.filters.WeightSize;

/**
 *
 * @author jbilcke
 */
public class FilterChain {

    public List<Node> filteredNodes = null;
    public AtomicBoolean popLocked = null;
    public AtomicBoolean filterIsRunning = new AtomicBoolean(false);
    public AtomicInteger graphRevision = new AtomicInteger(0);
    private LinkedList<Filter> filters = null;
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
            //System.out.println("filter started!..");
            List<Node> result = (nodes != null) ? nodes : new LinkedList<Node>();
            for (Filter f : filters) {
                //System.out.println("processing filter "+f.getRoot());
                if (interrupted()) {
                    System.out.println("we're interrupted!");
                    return;
                }
                result = f.process(session, view, result);
            }
            chain.filteredNodes = result;
            //System.out.println("filter finished to process "+result.size()+" nodes! setting flash 'ready' to true..");
            chain.filterIsRunning.set(false);
            chain.popLocked.set(false);
        }
    }

    public FilterChain(Session session, View view) {

        filteredNodes = new LinkedList<Node>();
        popLocked = new AtomicBoolean(true);
        filters = new LinkedList<Filter>();
        listeners = new ArrayList<FilterChainListener>();
        thread = null;
        this.view = view;
        this.session = session;
    }

    public boolean addFilter(String filterName, String root) {
        System.out.println("adding filter "+root+"");
        Filter f = null;
        if (filterName.equals("ThresholdWeight")) {
            f = new ThresholdWeight();
        } else if (filterName.equals("NodeRadius")) {
            f = new NodeRadius();
        }else if (filterName.equals("WeightSize")) {
            f = new WeightSize();
        }  else if (filterName.equals("NodeFunction")) {
            f = new NodeFunction();
        }  else if (filterName.equals("Category")) {
            f = new Category();
        } else if (filterName.equals("Explorer")) {
            f = new Explorer();
        } else if (filterName.equals("SubGraphCopy")) {
            f = new SubGraphCopy();
        }  else if (filterName.equals("Layout")) {
            f = new Layout();

        } else if (filterName.equals("ThresholdGenericity")) {
             f = new ThresholdGenericity();
    } else {
            return false;
        }
         f.setRoot(root);
         filters.add(f);
        //System.out.println("addFilter SUCCESS!");
        return true;
    }

    public Filter getFilter(String filterKey) {
        for (Filter f : filters) {
            if (f.getRoot().equals(filterKey)) return f;
        }
        return null;
    }

    public void enableFilter(String filterKey) {
        for (Filter f : filters) {
            if (f.getRoot().equals(filterKey)) {
                f.setEnabled(true);
                break;
            }
        }
    }

    public void disableFilter(String filterKey) {
        for (Filter f : filters) {
            if (f.getRoot().equals(filterKey)) {
                f.setEnabled(false);
                break;
            }
        }
    }

    public boolean toggleFilter(String filterKey) {
        for (Filter f : filters) {
            if (f.getRoot().equals(filterKey)) {
                f.setEnabled(!f.enabled());
                return f.enabled();
            }
        }
        return false;
    }

    public synchronized List<Node> popNodes() {

        // we are already filtering, please wait..
        if (filterIsRunning.get()) {
            //System.out.println("Filters are already running, please wait..");
            return null;
        }

        // if we are up to date,
        if (graphRevision.get() == view.graph.revision.get()) {
            // check if we already popped
            if (!popLocked.get()) {
                //System.out.println("Filters are up to date, not running, and not popped.. we return filtered nodes!");
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
                //System.out.println("Filters are up to date, not running, and not popped.. we return filtered nodes!");
                popLocked.set(true); // no more popping!
                return filteredNodes;
         }


        //System.out.println("Filter is outdated, checking if we can run a new filter thread..");

        // we have to wait for the graph to be unlocked
        if (view.graph.locked.get()) {
            //System.out.println("Graph is locked.. probably parsing some XML!");
            return null;
        }

        //System.out.println("okay, graph looks unlocked, starting new filter thread..");
        filterIsRunning.set(true);
        thread = new FilterThread(this, session, view, view.graph.getNodeListCopy());
        thread.start();

        //System.out.println("updating filter's graph revision!");
        graphRevision.set(view.graph.revision.get());
        return null;

    }

    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
