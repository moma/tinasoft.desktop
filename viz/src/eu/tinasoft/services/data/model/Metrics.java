/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.model;

import processing.core.PVector;

/**
 *
 * @author jbilcke
 */
public class Metrics {

    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    public PVector center;
    public PVector baryCenter;
    public float graphWidth;
    public float graphHeight;
    public float graphRadius;
    public float minEdgeWeight;
    public float maxEdgeWeight;
    public float minNodeWeight;
    public float maxNodeWeight;
    public float minNodeRadius;
    public float maxNodeRadius;
    public int nbNodes;
    public int nbEdges;
    public float averageNodeWeight;
    public float averageNodeRadius;
    public float averageEdgeWeight;
    public int nbVisibleNodes;
    public int nbVisibleEdges;
    //public Map<Float,List<> valueMapper;

    Metrics() {
        reset();
    }

    public void reset() {
        minX = 0.0f;
        minY = 0.0f;
        maxX = 0.0f;
        maxY = 0.0f;
        center = new PVector(0.0f, 0.0f);
        baryCenter = new PVector(0.0f, 0.0f);
        graphWidth = 0.0f;
        graphHeight = 0.0f;
        graphRadius = 0.0f;
        minEdgeWeight = 0.0f;
        maxEdgeWeight = 0.0f;
        minNodeWeight = 0.0f;
        maxNodeWeight = 0.0f;
        minNodeRadius = 0.0f;
        maxNodeRadius = 0.0f;
        nbNodes = 0;
        nbEdges = 0;
        nbVisibleNodes = 0;
        nbVisibleEdges = 0;
        averageNodeWeight = 0.0f;
        averageNodeRadius = 0.0f;
        averageEdgeWeight = 0.0f;
        //valueMapper = new HashMap<Float, Integer>();
    }

        @Override
    public String toString() {
        return "minX=" + minX + ", "
                //+ "minY=" + minY + ", \n"
               // + "maxX=" + maxX + ", "
                //+ "maxY=" + maxY + ", \n"
                + "minNodeRadius=" + minNodeRadius + ", "
                + "maxNodeRadius=" + maxNodeRadius + ", \n"
                //+ "graphWidth=" + graphWidth + ","
                //+ "graphHeight=" + graphHeight + ","
                //+ "graphRadius=" + graphRadius + ", \n"
                //+ "centerX=" + center.x + ", "
                //+ "centerY=" + center.y + ", "
                //+ "baryCenterX=" + baryCenter.x + ", "
                //+ "baryCenterY=" + baryCenter.y + ", \n"
                + "minEdgeWeight=" + minEdgeWeight + ", "
                + "maxEdgeWeight=" + maxEdgeWeight + ";"
                + "minNodeWeight=" + minNodeWeight + ", "
                + "maxNodeWeight=" + maxNodeWeight
                + ";\n";
    }
}
