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

    public Graph graph = null;
    
    public MacroView macro = null;
    public MesoView meso = null;
    public View micro = null;

    public ViewLevel currentLevel = ViewLevel.MACRO;
    
    public float zoom = 3f;

    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);

    public Session() {
          graph = new Graph();

            macro = new MacroView(graph);
            meso = new MesoView(graph);
            micro = new View(graph);
    }

    public synchronized View getView() {
        return (currentLevel == ViewLevel.MACRO)
                ? macro : (currentLevel == ViewLevel.MESO)
                ? meso : micro;
    }

    public synchronized void clear() {
        graph.clear();
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

    public Graph getGraph() {
        return graph;
    }

    public synchronized void toMacroLevel() {
        currentLevel = ViewLevel.MACRO;
        //hasBeenRead.set(false);
    }
    public synchronized void toMesoLevel() {
        currentLevel = ViewLevel.MESO;
        //hasBeenRead.set(false);
    }
    public synchronized void toMicroLevel() {
        currentLevel = ViewLevel.MICRO;
        //hasBeenRead.set(false);
    }

   public boolean updateFromURI(String uri) {
        return graph.updateFromURI(uri);
    }

    public boolean updateFromString(String str) {
        return graph.updateFromString(str);
    }

    public boolean updateFromInputStream(InputStream inputStream) {
        return graph.updateFromInputStream(inputStream);
    }

    public boolean updateFromNodeList(List<Node> nodes) {
        return graph.updateFromNodeList(nodes);
    }

    public synchronized String getLevel() {
        return (currentLevel == ViewLevel.MACRO)
                ? "macro" : (currentLevel == ViewLevel.MESO)
                ? "meso" : "micro";
    }
}
