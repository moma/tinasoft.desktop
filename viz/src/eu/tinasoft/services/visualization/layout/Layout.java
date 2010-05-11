/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.layout;

import processing.core.PApplet;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.visualization.views.View;
import java.math.*;

/**
 *
 * @author uxmal
 */
public class Layout {

    static final float GLOBAL_SCALE = 0.00000001f;
    static final float EPSILON = 0.0000001f;

    public void macroLayout_approximate(View v, NodeList nodes) {
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

        for (Node n1 : nodes.nodes) {

            n1_degree = n1.weights.size();


            gdx = nodes.baryCenter.x - n1.x;
            gdy = nodes.baryCenter.y - n1.y;

            gdistance = PApplet.sqrt(gdx * gdx + gdy * gdy) + EPSILON;
            float centering =1.0f + centerNodesWithHighDegree * PApplet.sqrt(n1_degree);
            n1.vx += gdx * gdistance * gravity * centering;
            n1.vy += gdy * gdistance * gravity * centering;

            for (Node n2 : nodes.nodes) {
                if (n1 == n2) {
                    continue;
                }

                n2_degree = n2.weights.size();

                dx = n2.x - n1.x;
                dy = n2.y - n1.y;

                distance = PApplet.sqrt(dx * dx + dy * dy) + EPSILON;

                borderDistance = distance - n1.radius - n2.radius;
                if (borderDistance <= 0.0f) {
                    dix = (dx / distance) * (n1.radius + n2.radius) * 1.5f;
                    diy = (dy / distance) * (n1.radius + n2.radius) * 1.5f;

                    //System.out.println(" n1.vx = " + n1.vx + dix);
                    //System.out.println(" n2.vx = " + n2.vx + dix);

                    // si les noeuds se chevauchent
                    n1.vx -= dix;
                    n1.vy -= diy;

                    n2.vx += dix;
                    n2.vy += diy;

                    
                } else {
                    // sinon on les attire en fonction du nb de liens
                    // et du poids

                    // ATTRACTION QUAND ON A UN LIEN
                    if (n1.weights.containsKey(n2.id)) {
                        // TODO prendre le + grand des 2
                        weight = (Float) n1.weights.get(n2.id);

                        dix = dx * borderDistance * weight * attraction;
                        diy = dy * borderDistance * weight * attraction;

                        n1.vx += dix;
                        n1.vy += diy;
                        n2.vx -= dix;
                        n2.vy -= diy;
                    }


                    // REPULSION
                    // TODO fonction qui, lorsqu'on s'approche très près, tend vers une force infinie
                    float rep = repulsion_when_big_degree * PApplet.sqrt(n1_degree);
                    //rep = 0.0000001f + repulsion_when_big_degree_diff * PApplet.abs(n1_degree - n2_degree);
                    //rep = repulsion_when_big_degree;
                    //0.01; 1.
                    rep = globalRepulsion + 0.1f * PApplet.sqrt(n2_degree);
                    
                    //float di = distance * PApplet.sqrt(distance);
                
                    n1.vx -= (dx / (distance*distance))  * rep;
                    n1.vy -= (dy / (distance*distance))  * rep;

                    n2.vx += (dx / (distance*distance)) * rep;
                    n2.vy += (dy / (distance*distance))  * rep;

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
            n1.x = PApplet.constrain(n1.x + n1.vx, -6000, +6000);
            n1.y = PApplet.constrain(n1.y + n1.vy, -6000, +6000);


            if (n1.original != null) {
                n1.original.x = n1.x;
                n1.original.y = n1.y;
            }


            n1.vx = 0.0f;
            n1.vy = 0.0f;
        }   // FOr NODE A
    }
    public void slowWithLabelAdjust(View v, NodeList nodes) {

        System.out.println("HELLO");
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        for (Node n1 : nodes.nodes) {
            for (Node n2 : nodes.nodes) {
                if (n1 == n2) {
                    continue;
                }
                vx = n2.x - n1.x;// - (n2.anticolRadius - n1.anticolRadius);
                vy = n2.y - n1.y;// - (n2.anticolRadius - n1.anticolRadius);

                /*
                if (Math.abs(vx) < 1f) {
                n2.x += 1f;
                vx = 1f;
                } else if (Math.abs(vy) < 1f) {
                n2.y += 1f;
                vy = 1f;
                }
                 */
                // distance = FastSquareRoot.fast_sqrt(sq(vx)+sq(vy)) +  0.0000001f;
                distance = PApplet.sqrt(vx * vx + vy * vy);


                // we do not want the nodes to be glued together
                if (distance > 1000f) {


                    // int badSquare = FastSquareRoot.fastSqrt((int)((sq(vx)+sq(vy))*1000));
                    //distance = ((float) badSquare) / 1000.0f;
                    //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                    // plutot que mettre une distance minimale,
                    // mettre une force de repulsion, par exemple
                    // radius * (1 / distance)   // ou distance au carré
                    if (n1.weights.containsKey(n2.id)) {
                        float w = 20f * (Float) n1.weights.get(n2.id);
                        n1.vx += vx * distance * w * attraction;
                        n1.vy += vy * distance * w * attraction;
                        n2.vx -= vx * distance * w * attraction;
                        n2.vy -= vy * distance * w * attraction;

                        System.out.println("distance: " + distance + " w: " + w);
                    } else {

                        // STANDARD REPULSION
                        n1.vx += (vx / distance) * repulsion;
                        n1.vy += (vy / distance) * repulsion;
                        n2.vx -= (vx / distance) * repulsion;
                        n2.vy -= (vy / distance) * repulsion;
                    }
                } else {
                    n1.vx += 100f;
                    n1.vy += 100f;
                    n2.vx -= 100f;
                    n2.vy -= 100f;
                }

            } // FOR NODE B
        }   // FOr NODE A

        /*
        float labelAdjustRepulsionStrength = 150.0f;
        for (Node n1 : nodes.nodes) {
        for (Node n2 : nodes.nodes) {
        if (n1 != n2) {

        float xDist = Math.abs(n1.x - n2.x);
        float label_occupied_width = 0.5f * (n1.boxWidth + n2.boxWidth);
        float yDist = Math.abs(n1.y - n2.y);
        float label_occupied_height = 0.5f * (n1.boxHeight + n2.boxHeight);
        if (xDist < label_occupied_width && yDist < label_occupied_height) {
        vx = n2.x - n1.x;// - (n2.anticolRadius - n1.anticolRadius);
        vy = n2.y - n1.y;// - (n2.anticolRadius - n1.anticolRadius);
        // distance = FastSquareRoot.fast_sqrt(sq(vx)+sq(vy)) +  0.0000001f;
        distance = PApplet.sqrt(vx * vx + vy * vy);

        if (distance > 0.1f) {
        float c = ((0.8f + 0.4f * (float) Math.random()) * labelAdjustRepulsionStrength);
        float f = 0.001f * c / distance;
        float verticalization = 0.005f * label_occupied_width;

        n1.vx += xDist / distance * f;
        n1.vy += verticalization * yDist / distance * f;
        n2.vx -= xDist / distance * f;
        n2.vy -= verticalization * yDist / distance * f;
        } else {
        n1.vx += 100f;
        n1.vy += 100f;
        n2.vx -= 100f;
        n2.vy -= 100f;
        }

        }

        }
        }
        }

         * *
         */
        for (Node n : nodes.nodes) {
            // important, we limit the velocity!
            n.vx = PApplet.constrain(n.vx, -10, 10);
            n.vy = PApplet.constrain(n.vy, -10, 10);

            // update the coordinate
            // also set the bound box for the whole scene
            n.x = PApplet.constrain(n.x + n.vx * 0.5f, -3000, +3000);
            n.y = PApplet.constrain(n.y + n.vy * 0.5f, -3000, +3000);

            // update the original, "stored" node
            if (n.original != null) {
                n.original.x = n.x;
                n.original.y = n.y;
            }

            n.vx = 0.0f;
            n.vy = 0.0f;
        }
    }

    public void precise(View v, NodeList nodes) {
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        for (Node n1 : nodes.nodes) {
            for (Node n2 : nodes.nodes) {
                if (n1 == n2) {
                    continue;
                }
                vx = n2.x - n1.x;// - (n2.anticolRadius - n1.anticolRadius);
                vy = n2.y - n1.y;// - (n2.anticolRadius - n1.anticolRadius);
                // distance = FastSquareRoot.fast_sqrt(sq(vx)+sq(vy)) +  0.0000001f;
                distance = PApplet.sqrt(vx * vx + vy * vy) + 0.0000001f;
                // int badSquare = FastSquareRoot.fastSqrt((int)((sq(vx)+sq(vy))*1000));
                //distance = ((float) badSquare) / 1000.0f;
                //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                // plutot que mettre une distance minimale,
                // mettre une force de repulsion, par exemple
                // radius * (1 / distance)   // ou distance au carré
                if (n1.weights.containsKey(n2.id)) {
                    float w = 1.0f + (Float) n1.weights.get(n2.id);
                    n1.vx += vx * distance * w * attraction;
                    n1.vy += vy * distance * w * attraction;
                    n2.vx -= vx * distance * w * attraction;
                    n2.vy -= vy * distance * w * attraction;

                }


                // STANDARD REPULSION
                n1.vx -= (vx / distance) * repulsion;
                n1.vy -= (vy / distance) * repulsion;
                n2.vx += (vx / distance) * repulsion;
                n2.vy += (vy / distance) * repulsion;

            } // FOR NODE B
        }   // FOr NODE A


        float labelAdjustRepulsionStrength = 150.0f;
        for (Node n1 : nodes.nodes) {
            for (Node n2 : nodes.nodes) {
                if (n1 != n2) {

                    float xDist = Math.abs(n1.x - n2.x);
                    float label_occupied_width = 0.5f * (n1.boxWidth + n2.boxWidth);
                    float yDist = Math.abs(n1.y - n2.y);
                    float label_occupied_height = 0.5f * (n1.boxHeight + n2.boxHeight);
                    if (xDist < label_occupied_width && yDist < label_occupied_height) {
                        vx = n2.x - n1.x;// - (n2.anticolRadius - n1.anticolRadius);
                        vy = n2.y - n1.y;// - (n2.anticolRadius - n1.anticolRadius);
                        // distance = FastSquareRoot.fast_sqrt(sq(vx)+sq(vy)) +  0.0000001f;
                        distance = PApplet.sqrt(vx * vx + vy * vy) + 0.0000001f;

                        if (distance > 0) {
                            float c = ((0.8f + 0.4f * (float) Math.random()) * labelAdjustRepulsionStrength);
                            float f = 0.001f * c / distance;
                            float verticalization = 0.005f * label_occupied_width;

                            n1.vx += xDist / distance * f;
                            n1.vy += verticalization * yDist / distance * f;
                            n2.vx -= xDist / distance * f;
                            n2.vy -= verticalization * yDist / distance * f;
                        }

                    }

                }
            }
        }

        for (Node n : nodes.nodes) {
            // important, we limit the velocity!
            n.vx = PApplet.constrain(n.vx, -5, 5);
            n.vy = PApplet.constrain(n.vy, -5, 5);

            // update the coordinate
            // also set the bound box for the whole scene
            n.x = PApplet.constrain(n.x + n.vx * 0.5f, -3000, +3000);
            n.y = PApplet.constrain(n.y + n.vy * 0.5f, -3000, +3000);

            // update the original, "stored" node
            if (n.original != null) {
                n.original.x = n.x;
                n.original.y = n.y;
            }

            n.vx = 0.0f;
            n.vy = 0.0f;
        }


    }
}
