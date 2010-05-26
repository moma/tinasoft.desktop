/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.session;

import eu.tinasoft.services.protocols.browser.Browser;
import eu.tinasoft.services.data.model.Graph;
import eu.tinasoft.services.visualization.views.MicroView;
import eu.tinasoft.services.visualization.views.ViewLevel;
import eu.tinasoft.services.visualization.views.View;
import eu.tinasoft.services.visualization.views.MacroView;
import eu.tinasoft.services.visualization.views.MesoView;
import java.awt.Color;
import java.io.InputStream;
import java.security.KeyException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;

//import org.w3c.dom.*;
/**
 * Link-less design
 * @author jbilcke
 */
public class Session {

    // public Graph graph = null;
    public MacroView macro = null;
    public MesoView meso = null;
    public View micro = null;
    public ViewLevel currentView = ViewLevel.MACRO;
    public float zoom = 3f;
    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);
    public Browser browser = null;

    public Session() {
        // graph = new Graph();
        browser = new Browser();
        macro = new MacroView(this);
        meso = new MesoView(this);
        micro = new MicroView(this);
    }

    public synchronized void clear() {
        // graph.clear();

        macro.clear();
        meso.clear();
        micro.clear();

    }

    public View getMacro() {
        return macro;
    }

    public View getMeso() {
        return meso;
    }

    public View getMicro() {
        return micro;
    }

    public synchronized Graph getGraph() {
        return getView().graph;
    }

    public synchronized void setView(String view) {
        if (view.equals("macro")) {
            toMacroView();
        } else if (view.equals("meso")) {
            toMesoView();
        } else if (view.equals("micro")) {
            toMicroView();
        }
    }

    public synchronized void toMacroView() {
        if (currentView != ViewLevel.MACRO) {
            currentView = ViewLevel.MACRO;
            macro.graph.touch();
            //macro.filters.popLocked.set(false); // open the pop lock!
        }
    }

    public synchronized void toMesoView() {
        if (currentView != ViewLevel.MESO) {
            currentView = ViewLevel.MESO;
            meso.graph.touch();
            //meso.filters.popLocked.set(false); // open the pop lock!
        }
    }

    public synchronized void toMicroView() {
        if (currentView != ViewLevel.MICRO) {
            currentView = ViewLevel.MICRO;
            micro.graph.touch();
            //micro.filters.popLocked.set(false); // open the pop lock!
        }
    }

    public boolean updateFromURI(String uri) {
        return getView().updateFromURI(uri);
    }

    public boolean updateFromString(String str) {
        return getView().updateFromString(str);
    }

    public boolean updateFromInputStream(InputStream inputStream) {
        return getView().updateFromInputStream(inputStream);
    }

    public boolean updateFromNodeList(NodeList nodes) {
        return getView().updateFromNodeList(nodes);
    }

    public boolean updateFromURI(String level, String uri) {
        if (level.equals("macro")) {
            return macro.updateFromURI(uri);
        } else if (level.equals("meso")) {
            return meso.updateFromURI(uri);
        } else if (level.equals("micro")) {
            return micro.updateFromURI(uri);
        } else {
            return false;
        }
    }

    public boolean updateFromString(String level, String str) {
        if (level.equals("macro")) {
            return macro.updateFromString(str);
        } else if (level.equals("meso")) {
            meso.updateFromString(str);



            return true;
        } else if (level.equals("micro")) {
            return micro.updateFromString(str);
        } else {
            return false;
        }
    }

    public synchronized String getLevel() {
        return getView().getName();
    }

    public synchronized View getView() {
        return (currentView == ViewLevel.MACRO)
                ? macro : (currentView == ViewLevel.MESO)
                ? meso : micro;
    }

    public synchronized View getView(String v) throws ViewNotFoundException {
        if (v.equalsIgnoreCase("macro")) {
            return macro;
        }
        else if (v.equalsIgnoreCase("meso")) {
            return meso;
        }
        else if (v.equalsIgnoreCase("micro")) {
            return micro;
        } else {

        throw new ViewNotFoundException("View "+v+" not found!");
        }
    }

    public void selectNode(Node n) {
        macro.selectNodeById(n.id);
        meso.selectNodeById(n.id);
        micro.selectNodeById(n.id);
    }

    public void unselectNode(Node n) {
        macro.unselectNodeById(n.id);
        meso.unselectNodeById(n.id);
        micro.unselectNodeById(n.id);
    }



    public void selectNode(String s) {
        macro.selectNodeById(s.hashCode());
        meso.selectNodeById(s.hashCode());
        micro.selectNodeById(s.hashCode());
    }

    public void unselectNode(String s) {
        macro.unselectNodeById(s.hashCode());
        meso.unselectNodeById(s.hashCode());
        micro.unselectNodeById(s.hashCode());
    }

    public void selectNode(int id) {
        macro.selectNodeById(id);
        meso.selectNodeById(id);
        micro.selectNodeById(id);
    }

    public void unselectNode(int id) {
        macro.unselectNodeById(id);
        meso.unselectNodeById(id);
        micro.unselectNodeById(id);
    }


    public void unselectAll() {
        macro.unselectAll();
        meso.unselectAll();
        micro.unselectAll();
    }

    public synchronized boolean setProperty(String key, Object value) throws KeyException {
        macro.setProperty(key, value);
        meso.setProperty(key, value);
        micro.setProperty(key, value);
        return true;
    }

    public synchronized boolean addFilter(String filterName, String root) {
        macro.addFilter(filterName, root);
        meso.addFilter(filterName, root);
        micro.addFilter(filterName, root);
        return true;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void selectNodes(List<String> ids) {
        for (String id : ids) {
            selectNode(id);
        }
    }

    public Browser getBrowser() {
        return browser;
    }
}
