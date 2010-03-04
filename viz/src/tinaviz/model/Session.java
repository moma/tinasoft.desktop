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

    public synchronized void toMacroLevel() {
        currentLevel = ViewLevel.MACRO;
        macro.hasBeenRead.set(false);
    }
    public synchronized void toMesoLevel() {
        currentLevel = ViewLevel.MESO;
        meso.hasBeenRead.set(false);
    }
    public synchronized void toMicroLevel() {
        currentLevel = ViewLevel.MICRO;
        micro.hasBeenRead.set(false);
    }

   public boolean updateFromURI(String uri) {
        return getGraph().updateFromURI(uri);
    }

    public boolean updateFromString(String str) {
        return getGraph().updateFromString(str);
    }

    public boolean updateFromInputStream(InputStream inputStream) {
        return getGraph().updateFromInputStream(inputStream);
    }

    public boolean updateFromNodeList(List<Node> nodes) {
        return getGraph().updateFromNodeList(nodes);
    }

    public synchronized String getLevel() {
        return getView().getName();
    }
        public synchronized View getView() {
        return (currentLevel == ViewLevel.MACRO)
                ? macro : (currentLevel == ViewLevel.MESO)
                ? meso : micro;
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
}
