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
    public AtomicBoolean filteredNodesAreReadable = null;
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
        public void run()  {
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
            chain.filteredNodesAreReadable.set(true);
        }
    }

    public FilterChain(Graph graph) {

        filteredNodes = new ArrayList<Node>();
        filteredNodesAreReadable = new AtomicBoolean(true);
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


    public synchronized List<Node> getNodes() {

        if (!graph.hasBeenReadByFilter.get())
            System.out.println("FilterChain 1/2 getNodes() called; graph has changed, still not filtered!");
        // if we have a new filtering task, 
         if (!graph.hasBeenReadByFilter.get()) {
              System.out.println("Waiting for the end of the previous filter..");
            try {
                //thread.interrupt();
                thread.join();
            } catch (InterruptedException ex) {
                Console.error("Fatal error with thread: "+ex);
            }
            filteredNodesAreReadable.set(false);
            graph.hasBeenReadByFilter.set(true);

            thread = new FilterThread(this, graph.getNodeList());
            System.out.println("starting new filter..");
            thread.start();
            System.out.println("filter should be started..");
        }
        if (!filteredNodesAreReadable.get()) 
            System.out.println("FilterChain 2/2 getNodes() called; filtered nodes are not ready!");
        return (filteredNodesAreReadable.get()) ? filteredNodes : null;
    }


    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
