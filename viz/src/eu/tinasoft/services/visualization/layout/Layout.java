/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.layout;

import processing.core.PApplet;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author Julian Bilcke
 */
public class Layout {

    static final float GLOBAL_SCALE = 0.00000001f;
    static final float EPSILON = 0.0000001f;


    /**
     * Force Blender - a cool layout for macro view
     *
     * - Gravity depends on the number of nodes
     * - Every node is attracted to the center
     * - Nodes with high VAR_1 are MORE attracted to the center
     * - Every node repulse each other
     * - Neighbours (in term of linking) are attracted
     *
     * @param v
     * @param nodes
     */
    public void macroViewLayout_ForceBlender(View v, NodeList nodes) {
        float distance = 1f;
        float gdistance = 1f;
        float dx = 1f;
        float dy = 1f;
        float gdx = 1f;
        float gdy = 1f;

        // scene scale (depends on scene coordinates)


        // how each node is attracted to each other when there is a link
        float attraction = 0.1f; // 9000 is max, after, nodes are too attached

        // the more the node has neighbours, the more it will repulse
        // TODO: except if the other node has few degree too
        float repulsion_when_big_degree = 100f;

        // this tend to push nodes with high degree to the periphery
        float repulsion_when_big_degree_diff = 0.01f;

        float globalRepulsion = 20.0f; // mini: 1.0f ,  default: 1.5f

        // should we center a bit more nodes with higher degree ? (maybe genericity?)
        float centerNodesWithHighDegree = 1.005f; // 1.0 = very small, 0.0 not used, 1.1f = default
        
        // depends on the number of elements

        float gravity = 100f;
        gravity = nodes.size() * 20.0f;

        repulsion_when_big_degree *= GLOBAL_SCALE;
        attraction *= GLOBAL_SCALE;
        gravity *= GLOBAL_SCALE;
        repulsion_when_big_degree_diff *= GLOBAL_SCALE;



        float borderDistance = EPSILON;
        int n1_degree = 0, n2_degree = 0;
        float dix = 0;
        float diy = 0;
        float weight = 0;

        float n1x=0.0f,n1y=0.0f,n2x=0.0f,n2y=0.0f,n1vx=0.0f, n1vy=0.0f,n2vx=0.0f,n2vy=0.0f;

        for (Node n1 : nodes.nodes) {

            n1_degree = n1.weights.size();

            n1x = n1.position.x;
            n1y = n1.position.y;

            gdx = nodes.baryCenter.x - n1x;
            gdy = nodes.baryCenter.y - n1y;

            gdistance = PApplet.sqrt(gdx * gdx + gdy * gdy) + EPSILON;

            float centering = 1.0f + centerNodesWithHighDegree * PApplet.sqrt(n1_degree);
            
            n1vx += gdx * gdistance * gravity * centering;
            n1vy += gdy * gdistance * gravity * centering;

            for (Node n2 : nodes.nodes) {
                if (n1 == n2) {
                    continue;
                }

                n2x = n2.position.x;
                n2y = n2.position.y;
                n2_degree = n2.weights.size();

                dx = n2x - n1x;
                dy = n2y - n1y;

                distance = PApplet.sqrt(dx * dx + dy * dy) + EPSILON;

                borderDistance = distance - n1.radius - n2.radius;
                if (borderDistance <= 0.0f) {
                    dix = (dx / distance) * (n1.radius + n2.radius) * 1.5f;
                    diy = (dy / distance) * (n1.radius + n2.radius) * 1.5f;

                    //System.out.println(" n1.vx = " + n1.vx + dix);
                    //System.out.println(" n2.vx = " + n2.vx + dix);

                    // si les noeuds se chevauchent
                    n1vx -= dix;
                    n1vy -= diy;

                    n2vx += dix;
                    n2vy += diy;

                    
                } else {
                    // sinon on les attire en fonction du nb de liens
                    // et du poids

                    // ATTRACTION QUAND ON A UN LIEN
                    if (n1.weights.containsKey(n2.id)) {
                        // TODO prendre le + grand des 2
                        weight = (Float) n1.weights.get(n2.id);

                        dix = dx * borderDistance * weight * attraction;
                        diy = dy * borderDistance * weight * attraction;

                        n1vx += dix;
                        n1vy += diy;
                        n2vx -= dix;
                        n2vy -= diy;
                    }


                    // REPULSION
                    // TODO fonction qui, lorsqu'on s'approche très près, tend vers une force infinie
                    float rep = repulsion_when_big_degree * PApplet.sqrt(n1_degree);
                    //rep = 0.0000001f + repulsion_when_big_degree_diff * PApplet.abs(n1_degree - n2_degree);
                    //rep = repulsion_when_big_degree;
                    //0.01; 1.
                    rep = globalRepulsion + 0.1f * PApplet.sqrt(n2_degree);
                    
                    //float di = distance * PApplet.sqrt(distance);
                
                    n1vx -= (dx / (distance*distance))  * rep;
                    n1vy -= (dy / (distance*distance))  * rep;

                    n2vx += (dx / (distance*distance)) * rep;
                    n2vy += (dy / (distance*distance))  * rep;

                }




                //}
            } // FOR NODE B
            // important, we limit the velocity!
            //n1.vx = PApplet.constrain(n1.vx, -30, 30);
            //n1.vy = PApplet.constrain(n1.vy, -30, 30);

            /*
            int vlimit = 30;
            if (PApplet.abs(n1.vx) > vlimit | PApplet.abs(n1.vy) > vlimit) {
            n1.vx = (n1.vx / distance) * vlimit;
            n1.vy = (n1.vy / distance) * vlimit;
            }*/

            //System.out.println("\nn1.x + n1.vx = " + n1.x + " + " + n1.vx);
            //System.out.println("n1.y + n1.vy = " + n1.y + " + " + n1.vy + "\n");

            // update the coordinate
            // also set the bound box for the whole scene
            n1x = PApplet.constrain(n1x + n1vx, -6000, +6000);
            n1y = PApplet.constrain(n1y + n1vy, -6000, +6000);

            n1vx = 0.0f;
            n1vy = 0.0f;

            // update the position into the node
            // todo: lock here?
            n1.position.set(n1x, n1y, 0.0f);
        }   // FOR NODE A

    }
    
}
