/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import eu.tinasoft.services.data.transformation.filters.Category;
import eu.tinasoft.services.data.transformation.filters.EdgeWeightRange;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.data.transformation.filters.NodeFunction;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.transformation.filters.Output;
import tinaviz.SubGraphCopy;
import tinaviz.SubGraphCopyStandalone;
import eu.tinasoft.services.data.transformation.filters.NodeWeightRange;
import eu.tinasoft.services.data.transformation.filters.Output;
import eu.tinasoft.services.data.transformation.filters.WeightSize;
import tinaviz.NodeWeightRangeHack;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class FilterChain {

    public NodeList filteredNodes = null;
    public AtomicBoolean popLocked = null;
    public AtomicBoolean filterIsRunning = new AtomicBoolean(false);
    public AtomicInteger graphRevision = new AtomicInteger(0);
    public LinkedList<Filter> filters = null;
    public View view = null;
    public Session session = null;
    private FilterThread thread = null;

    private class FilterThread extends Thread {

        private NodeList nodes;
        private FilterChain chain;
        private View view;
        private Session session;

        public FilterThread(FilterChain chain, Session session, View view, NodeList nodes) {

            this.session = session;
            this.view = view;
            this.nodes = nodes;
            this.chain = chain;
        }

        @Override
        public void run() {

            for (Filter f : filters) {
                if (interrupted()) {
                    return;
                }
                nodes = f.preProcessing(session, view, nodes);
            }

            System.out.println("Finalizing scaling to screen..");

            // this assignation should be safe if we create a new node list
            chain.filteredNodes = new NodeList(nodes);
            chain.filterIsRunning.set(false);
            chain.popLocked.set(false);
        }
    }

    public FilterChain(Session session, View view) {

        filteredNodes = new NodeList();
        popLocked = new AtomicBoolean(true);
        filters = new LinkedList<Filter>();
        thread = null;
        this.view = view;
        this.session = session;
    }

    public boolean addFilter(String filterName, String root) {
        //System.out.println("adding filter "+root+"");
        Filter f = null;
        if (filterName.equals("EdgeWeightRange")) {
            f = new EdgeWeightRange();
        } else if (filterName.equals("WeightSize")) {
            f = new WeightSize();
        } else if (filterName.equals("NodeFunction")) {
            f = new NodeFunction();
        } else if (filterName.equals("Category")) {
            f = new Category();
        } else if (filterName.equals("SubGraphCopy")) {
            f = new SubGraphCopy();
        } else if (filterName.equals("SubGraphCopyStandalone")) {
            f = new SubGraphCopyStandalone();
        } else if (filterName.equals("NodeWeightRange")) {
            f = new NodeWeightRange();
        } else if (filterName.equals("NodeWeightRangeHack")) {
            f = new NodeWeightRangeHack();
        } else if (filterName.equals("Output")) {
            f = new Output();
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
            if (f.getRoot().equals(filterKey)) {
                return f;
            }
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

    public synchronized NodeList popNodes() {

        // we are already filtering, please wait..
        if (filterIsRunning.get()) {
            return null;
        }

        // if we are up to date,
        if (graphRevision.get() == view.graph.revision.get()) {
            // check if we already popped
            if (popLocked.get()) {
                return null;
            }
            popLocked.set(true);
            return filteredNodes;
        } // if filters are up to date, not running, and not popped.. we return filtered nodes!
        if (!popLocked.get()) {
            popLocked.set(true); // no more popping!
            return filteredNodes;
        } // we have to wait for the graph to be unlocked
        if (view.graph.locked.get()) {
            return null;


        }

        //System.out.println("okay, graph looks unlocked, starting new filter thread..");
        filterIsRunning.set(true);
        thread = new FilterThread(this, session, view, view.graph.getNodeListCopy());
        thread.start();

        graphRevision.set(view.graph.revision.get());
        return null;
    }
}
