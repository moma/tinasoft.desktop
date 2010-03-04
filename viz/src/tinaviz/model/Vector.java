/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class Vector extends PVector {
    
    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public Vector() {
        super(0.0f, 0.0f);
    }

    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public Vector(float x, float y) {
        super(x, y);
    }

    /**
     * Constructor.
     *
     * @param vector  the source vector
     */
    public Vector(Vector vector) {
        this(vector.x, vector.y);
    }


}
