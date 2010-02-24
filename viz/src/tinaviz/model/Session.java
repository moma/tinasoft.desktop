/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;


//import org.w3c.dom.*;

/**
 * Link-less design
 * @author jbilcke
 */
public class Session {


    public GlobalNetwork global = new GlobalNetwork();
    public LocalNetwork local = new LocalNetwork();

    public NetworkMode explorationMode = NetworkMode.GLOBAL;

    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;

    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);

    public Session() {
    }
   

    public synchronized Network getNetwork() {
        return (explorationMode == NetworkMode.LOCAL)
                ? local : global;
    }


    public synchronized void clear() {
        local.clear();
        global.clear();
        hasBeenRead.set(false);
    }

    public Network getGlobal() {
        return global;
    }

    public Network getLocal() {
        return local;
    }

    public synchronized void switchToLocalExploration() {
        explorationMode = NetworkMode.LOCAL;
        hasBeenRead.set(false);
    }

    public synchronized void switchToGlobalExploration() {
        explorationMode = NetworkMode.GLOBAL;
        hasBeenRead.set(false);
    }

    public synchronized String getExplorationMode() {
        return (explorationMode == NetworkMode.LOCAL)
                ? "local" : "global";
    }

 
}
