/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import tinaviz.filters.FilterChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import tinaviz.Node;
import tinaviz.filters.Channel;
import tinaviz.filters.Filter;
import tinaviz.filters.FilterChainListener;

/**
 *
 * @author jbilcke
 */
public class FilterChain {

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
            chain.finished(result);
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
            return filters.get(key).toggleEnabled();
        }
        return false;
    }

    public synchronized void finished(List<Node> result) {
        final FilterChain me = this;
        final List<Node> output = result;
        Runnable doWorkRunnable = new Runnable() {
            public void run() {
                for (FilterChainListener listener : listeners) {
                    listener.filterChainOutput(output);
                }
            }
        };
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    public void addFilterChainListener(FilterChainListener listener) {
        listeners.add(listener);
    }
}
