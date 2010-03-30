/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import tinaviz.graph.Node;
import tinaviz.util.MathFunctions;

/**
 *
 * @author jbilcke
 */
public class PaintTools {
    public static void arrow(PApplet ap, float x1, float y1, float x2, float y2, float radius) {
        ap.pushMatrix();
        ap.translate(x2, y2);
        ap.rotate(PApplet.atan2(x1 - x2, y2 - y1));
        ap.line(0, -radius, -1, -1 - radius);
        ap.line(0, -radius, 1, -1 - radius);
        ap.popMatrix();
    }

    public static  void arrow(PGraphics pg, float x1, float y1, float x2, float y2, float radius) {
        pg.pushMatrix();
        pg.translate(x2, y2);
        pg.rotate(PApplet.atan2(x1 - x2, y2 - y1));
        pg.line(0, -radius, -1, -1 - radius);
        pg.line(0, -radius, 1, -1 - radius);
        pg.popMatrix();

    }




}
