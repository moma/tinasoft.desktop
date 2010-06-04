/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.computing;

import processing.core.PApplet;


/**
 *
 * @author jbilcke
 */
public class MathFunctions {



    public static float logify(float x) {
        return (PApplet.abs(x) < 0.01f) 
                ? 0.0f
                : (x > 0)
                    ? log100((int) (PApplet.abs(x) * 100.0f))
                    : -log100((int) (PApplet.abs(x) * 100.0f));
    }

     public static float log100(int x) {
        return (PApplet.log(x) / (PApplet.log(100)));
    }

    public static float[] rotation(float x, float y, float centerX, float centerY, float theta) {
        float[] rc = new float[2];
        rc[0] = (centerX + (x - centerX) * PApplet.cos(theta) - (y - centerY) * PApplet.sin(theta));
        rc[1] = (centerY + (x - centerX) * PApplet.sin(theta) + (y - centerY) * PApplet.cos(theta));
        return rc;
    }
    
    public static float map(float x, float imin, float imax, float omin, float omax) {
        return (imin==imax) ? omin : PApplet.map(x, imin, imax, omin, omax);
    }
    
}
