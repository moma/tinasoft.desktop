/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.model;

/**
 *
 * @author jbilcke
 */

/*
public class Metrics {

    public float minX = Float.MAX_VALUE;
    public float minY = Float.MAX_VALUE;
    public float maxX = Float.MIN_VALUE;
    public float maxY = Float.MIN_VALUE;
    public float minRadius = Float.MAX_VALUE;
    public float maxRadius = Float.MIN_VALUE;
    public float centerX = 0.0f;
    public float centerY = 0.0f;
    public float minWeight = Float.MAX_VALUE;
    public float maxWeight = Float.MIN_VALUE;

    public void reset() {

        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
        minRadius = Float.MAX_VALUE;
        maxRadius = Float.MIN_VALUE;
        centerX = 0.0f;
        centerY = 0.0f;
        minWeight = Float.MAX_VALUE;
        maxWeight = Float.MIN_VALUE;
    }

    @Override
    public String toString() {
        return "minX="+minX+", "
        +"minY="+minY+", "
        +"maxX="+maxX+", "
        +"maxY="+maxY+", "
        +"minRadius="+minRadius+", "
        +"maxRadius="+maxRadius+", "
        +"centerX="+centerX+", "
        +"centerY="+centerY+", "
        +"minWeight="+minWeight+", "
        +"maxWeight="+maxWeight+";";
    }
}
*/
/**
 *
 * @author jbilcke
 */


public class Metrics {
 public float minX = 0.0f;
    public float minY = 0.0f;
    public float maxX = 0.0f;
    public float maxY = 0.0f;
    public float minRadius = 0.0f;
    public float maxRadius = 0.0f;
    public float centerX = 0.0f;
    public float centerY = 0.0f;
    public float minWeight = 0.0f;
    public float maxWeight = 0.0f;
    public float minGenericity = 0.0f;
    public float maxGenericity = 0.0f;

    public void reset() {

        minX = 0.0f;
        minY = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        minRadius = 0.0f;
        maxRadius = 0.0f;
        centerX = 0.0f;
        centerY = 0.0f;
        minWeight = 0.0f;
        maxWeight = 0.0f;
        minGenericity = 0.0f;
        maxGenericity = 0.0f;
    }

    @Override
    public String toString() {
        return "minX="+minX+", "
        +"minY="+minY+", "
        +"maxX="+maxX+", "
        +"maxY="+maxY+", "
        +"minRadius="+minRadius+", "
        +"maxRadius="+maxRadius+", "
        +"centerX="+centerX+", "
        +"centerY="+centerY+", "
        +"minWeight="+minWeight+", "
        +"maxWeight="+maxWeight+";"
        +"minGenericity="+minGenericity+", "
        +"maxGenericity="+maxGenericity+";";
    }
}