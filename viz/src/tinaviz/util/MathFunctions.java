/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.util;

import processing.core.PApplet;

/**
 *
 * @author jbilcke
 */
public class MathFunctions {

    public static float logify(float x) {
        if (PApplet.abs(x) < 0.01f) {
            return 0.0f;


        }
        return (x > 0) ? log100((int) (PApplet.abs(x) * 100.0f)) : -log100((int) (PApplet.abs(x) * 100.0f));


    }

     public static float log100(int x) {
        return (PApplet.log(x) / ((float) PApplet.log(100)));


    }
    public static float[] rotation(float x, float y, float centerX, float centerY, float theta) {
        float[] rc = new float[2];
        rc[0] = (float) (centerX + (x - centerX) * PApplet.cos(theta) - (y - centerY) * PApplet.sin(theta));
        rc[1] = (float) (centerY + (x - centerX) * PApplet.sin(theta) + (y - centerY) * PApplet.cos(theta));

        return rc;

    }
}
