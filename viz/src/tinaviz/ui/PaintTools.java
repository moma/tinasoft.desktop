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



    public static  void drawCurve(PApplet ap, Node n1, Node n2) {
        float xa0 = (6 * n1.x + n2.x) / 7, ya0 = (6 * n1.y + n2.y) / 7;
        float xb0 = (n1.x + 6 * n2.x) / 7, yb0 = (n1.y + 6 * n2.y) / 7;
        float[] xya1 = MathFunctions.rotation(xa0, ya0, n1.x, n1.y, PApplet.PI / 2);
        float[] xyb1 = MathFunctions.rotation(xb0, yb0, n2.x, n2.y, -PApplet.PI / 2);
        float xa1 = (float) xya1[0], ya1 = (float) xya1[1];
        float xb1 = (float) xyb1[0], yb1 = (float) xyb1[1];
        ap.bezier(n1.x, n1.y, xa1, ya1, xb1, yb1, n2.x, n2.y);
    }
}
