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

    public MacroView macro = new MacroView();
    public MesoView meso = new MesoView();
    public View micro = new View();
    
    public ViewLevel currentLevel = ViewLevel.MACRO;
    
    public Color background = new Color(12, 12, 12);
    public int fontsize = 12;
    public AtomicBoolean hasBeenRead = new AtomicBoolean(false);

    public Session() {
    }

    public synchronized View getView() {
        return (currentLevel == ViewLevel.MACRO)
                ? macro : (currentLevel == ViewLevel.MESO)
                ? meso : micro;
    }

    public synchronized void clear() {
        meso.clear();
        macro.clear();
        micro.clear();
        hasBeenRead.set(false);
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

    public synchronized void toMacroLevel() {
        currentLevel = ViewLevel.MACRO;
        hasBeenRead.set(false);
    }
    public synchronized void toMesoLevel() {
        currentLevel = ViewLevel.MESO;
        hasBeenRead.set(false);
    }
    public synchronized void toMicroLevel() {
        currentLevel = ViewLevel.MICRO;
        hasBeenRead.set(false);
    }

    public synchronized String getLevel() {
        return (currentLevel == ViewLevel.MACRO)
                ? "macro" : (currentLevel == ViewLevel.MESO)
                ? "meso" : "micro";
    }
}
