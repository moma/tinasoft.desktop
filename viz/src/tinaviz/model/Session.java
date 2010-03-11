/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.awt.Color;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tinaviz.Node;

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
    public ViewLevel currentLevel = ViewLevel.MACRO;
    public float zoom = 3f;
    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);

    public Session() {
        // graph = new Graph();

        macro = new MacroView();
        meso = new MesoView();
        micro = new MicroView();
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

    public synchronized void setLevel(String level) {
        if (level.equals("macro")) {
            toMacroLevel();
        } else if (level.equals("meso")){
            toMesoLevel();
        } else if (level.equals("micro")){
            toMicroLevel();
        }
    }
    public synchronized void toMacroLevel() {
        if (currentLevel != ViewLevel.MACRO) {
            currentLevel = ViewLevel.MACRO;
            macro.filters.popLocked.set(false); // open the pop lock!
        }
    }

    public synchronized void toMesoLevel() {
        if (currentLevel != ViewLevel.MESO) {
            currentLevel = ViewLevel.MESO;
           meso.filters.popLocked.set(false); // open the pop lock!
        }
    }

    public synchronized void toMicroLevel() {
        if (currentLevel != ViewLevel.MICRO) {
            currentLevel = ViewLevel.MICRO;
           micro.filters.popLocked.set(false); // open the pop lock!
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

    public boolean updateFromNodeList(List<Node> nodes) {
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
            return meso.updateFromString(str);
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
        return (currentLevel == ViewLevel.MACRO)
                ? macro : (currentLevel == ViewLevel.MESO)
                ? meso : micro;
    }

    public synchronized View getView(String v) {
        if (v.contains("macro")) return macro;
        if (v.contains("meso")) return meso;
         if (v.contains("micro")) return micro;
        return null;
    }

    public void selectNode(Node n) {
        macro.selectNodeById(n.uuid);
        meso.selectNodeById(n.uuid);
        micro.selectNodeById(n.uuid);
    }

    public void unselectNode(Node n) {
        macro.unselectNodeById(n.uuid);
        meso.unselectNodeById(n.uuid);
        micro.unselectNodeById(n.uuid);
    }

    public void selectNodeById(String id) {
        macro.selectNodeById(id);
        meso.selectNodeById(id);
        micro.selectNodeById(id);
    }

    public void unselectNodeById(String id) {
        macro.unselectNodeById(id);
        meso.unselectNodeById(id);
        micro.unselectNodeById(id);
    }

    public void resetCamera(float width, float height) {
        macro.resetCamera(width, height);
        meso.resetCamera(width, height);
        micro.resetCamera(width, height);
    }

    public void unselectAll() {
        macro.unselectAll();
        meso.unselectAll();
        micro.unselectAll();
    }
}
