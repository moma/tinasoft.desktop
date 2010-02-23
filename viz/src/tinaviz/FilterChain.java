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

/**
 *
 * @author jbilcke
 */
public class FilterChain {

    public List<Node> filteredNodes = new ArrayList<Node>();
    public AtomicBoolean filtered = new AtomicBoolean(true);

    private class FilterThread extends Thread {

        private List<Node> nodes;
        private Map<String, Channel> channels;
        private FilterChain chain;

        public FilterThread(FilterChain chain, List<Node> nodes, Map<String, Channel> channels) {
            this.nodes = nodes;
            this.channels = channels;
            this.chain = chain;
        }

        @Override
        public void run() {
            List<Node> result = new ArrayList<Node>();
            for (Filter f : filters.values()) {
                f.process(result, channels);
            }
            chain.end(result);
        }
    }

    private Map<String, Filter> filters = new HashMap<String, Filter>();
    private List<FilterChainListener> listeners = new ArrayList<FilterChainListener>();

    public FilterChain() {
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
                filters.get(key).setField("enabled", !(Boolean)filters.get(key).getField("enabled"));
            } catch (KeyException ex) {
                Logger.getLogger(FilterChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public synchronized void end(List<Node> result) {
        filteredNodes = result;
        filtered.set(true);
        /*
        final FilterChain me = this;

        final List<Node> output = result;
        Runnable doWorkRunnable = new Runnable() {
            public void run() {
                
                //for (FilterChainListener listener : listeners) {
                //    listener.filterChainOutput(output);
                //}
                me.filteredNodes = output;

            }
        };
        SwingUtilities.invokeLater(doWorkRunnable);
        */
    }

    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
