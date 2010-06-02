/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.layout;

import eu.tinasoft.services.data.model.Metrics;
import processing.core.PApplet;
import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.model.NodeListNormalizer;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author Julian Bilcke
 */
public class Layout {

    static final float EPSILON = 0.0000001f;

    public void macroViewLayout_TinaForce(View v, NodeList nodes) {

        if (nodes.size() < 1) {
            return;
        }

        v.layoutIterationCount++;

        Metrics metrics = nodes.getMetrics();

        ////////////////////////////////////////////////////////////////////////////////

        float attraction = 5f;

        float globalRepulsion = 5f;// old= 5


        float cooling = (float) (0.05f / PApplet.sqrt(PApplet.sqrt(metrics.nbNodes + 1f)));

        float vlimit_max = 100f;

        float decay = PApplet.exp(-v.layoutIterationCount * cooling);

        float gravityFactor = 200f; // 200 est bien pour les projets

        float graphWidth = metrics.graphWidth;

        float gravity = 0;


        // si le graphe est déjà un peu spatialisé, on met la gravité normale, sans actualisation
        // sinon on met une gravité progressive
        if (metrics.graphWidth > 2 * NodeListNormalizer.MAX_RADIUS * PApplet.sqrt(metrics.nbEdges)) {
            gravity = gravityFactor / (1 + PApplet.log(metrics.nbEdges));
        }  else {
            gravity = gravityFactor / (1 + PApplet.log(metrics.nbEdges)) * (1 - decay);
        }

        // pause is forced!
        /*if (cooling <= 0.015f) {
        //System.out.println("forced back to pause mode");
        //v.paused = true;
        //v.layoutIterationCount = 0;
        }*/

        //float gravity = 0.05f / (1 + PApplet.sqrt(PApplet.sqrt(nbNodes))) * (1 - decay);

        //float gravity = 0.05f / (1 + PApplet.sqrt(PApplet.sqrt(nbNodes))) * (1 - decay);

        ////////////////////////////////////////////////////////////////////////////////


        if (false && Math.random() < 0.5f) {
            System.out.println("decay: " + decay + " = PApplet.exp(-" + v.layoutIterationCount + " * " + cooling + ")");
            System.out.println("gravity: " + gravity);

        }

        float borderDist = EPSILON;
        int n1_degree = 0, n2_degree = 0;
        float dix = 0;
        float diy = 0;
        float weight = 0;
        float dist = 1f;
        float gdistance = 1f;

        float dx = 1f;
        float dy = 1f;
        float gdx = 1f;
        float gdy = 1f;

        float n1x = 0.0f, n1y = 0.0f, n2x = 0.0f, n2y = 0.0f,
                n1vx = 0.0f, n1vy = 0.0f, n2vx = 0.0f, n2vy = 0.0f, n2gvx = 0.0f, n2gvy = 0.0f;

        for (Node n1 : nodes.nodes) {
            n1_degree = n1.weights.size();
            n1x = n1.position.x;
            n1y = n1.position.y;

            for (Node n2 : nodes.nodes) {
                if (n1 == n2) {
                    continue;
                }

                n2_degree = n2.weights.size();


                n2vx = 0.0f;
                n2vy = 0.0f;
                n2gvx = 0.0f;
                n2gvy = 0.0f;

                n2x = n2.position.x;
                n2y = n2.position.y;

                // GRAVITY
                gdx = metrics.baryCenter.x - n2x;
                gdy = metrics.baryCenter.y - n2y;
                gdistance = PApplet.sqrt(gdx * gdx + gdy * gdy) + EPSILON;
                n2gvx += (gdx / gdistance) * gravity;
                n2gvy += (gdy / gdistance) * gravity;

                dx = n2x - n1x;
                dy = n2y - n1y;

                dist = PApplet.sqrt(dx * dx + dy * dy) + EPSILON;
                float sqDist = dist * dist;

                float radiusSum = (n1.radius + n2.radius);
                borderDist = dist - radiusSum;
                float sqBorderDist = (borderDist * borderDist) / radiusSum;
                float desiredDist = radiusSum * 1.5f;

                if (borderDist <= 0.0f && decay < 0.7) {
                    if (Math.random() < 0.05f) {
                        float theta = 2 * PApplet.PI * (float) Math.random();
                        dix = ((PApplet.cos(theta) - PApplet.sin(theta))) * desiredDist;
                        diy = ((PApplet.cos(theta) + PApplet.sin(theta))) * desiredDist;
                    } else {
                        dix = (dx / dist) * desiredDist;
                        diy = (dy / dist) * desiredDist;
                    }

                    n2vx += dix;
                    n2vy += diy;

                    // limit the force
                    /*
                    float vlimit = PApplet.min(vlimit_max, borderDist);
                    if (PApplet.abs(n2vx) > vlimit | PApplet.abs(n2vy) > vlimit) {
                    n2vx = (dx / dist) * vlimit;
                    n2vy = (dy / dist) * vlimit;
                    }
                     */
                    n2x += n2vx * decay;
                    n2y += n2vy * decay;

                }

                // ATTENTION ATTENTION ATTE ATTENTION ATTENTION ATTENTION
                // LE DEGREE A L'AIR INVALIDE (CF VISUAL ANALYTICS)
                // ATTENTION ATTENTION ATTE ATTENTION ATTENTION ATTENTION

                boolean nodeisAloneAndFar = (n2_degree <= 1 && dist > NodeListNormalizer.MAX_RADIUS * 20.0f);

                /*if (n2.label.equals("visual analytics")) {

                System.out.println("("+n2_degree+" == 1 && "+dist+" > "+nodes.MAX_RADIUS+" * 10.0f)");
                System.out.println("visual analytics is alone and far? => "+nodeisAloneAndFar);
                }*/
                // si le noeud est tout seul, isolé et trop loin, on le laisse revenir
                if ((borderDist > 0.0f && decay > 0.1) | nodeisAloneAndFar) {

                    if (true) {
                        if (borderDist < radiusSum) {
                            n2vx += dx / dist * ((radiusSum - borderDist) / radiusSum);
                            n2vy += dy / dist * ((radiusSum - borderDist) / radiusSum);
                        }
                    }

                    // ATTRACTION QUAND ON A UN LIEN

                    if (n1.weights.containsKey(n2.id)) {
                        //System.out.println("dist: "+dist);
                        weight = (Float) n1.weights.get(n2.id) + 1;

                        // si le noeud n'a pas d'autres voisins
                        if (n2_degree <= 1) {
                            attraction = attraction * 2.0f;
                        }
                        //
                        dix = (dx / dist) * (PApplet.sqrt(PApplet.sqrt(weight)) + 0.5f) * attraction * PApplet.log(1 + PApplet.abs(borderDist) / 2);
                        diy = (dy / dist) * (PApplet.sqrt(PApplet.sqrt(weight)) + 0.5f) * attraction * PApplet.log(1 + PApplet.abs(borderDist) / 2);
                        n2vx -= dix;
                        n2vy -= diy;
                    }

                    // REPULSION
                    float rep = globalRepulsion;

                    if (n1.weights.containsKey(n2.id)) {
                        rep = globalRepulsion * PApplet.log(n1_degree);

                        if (n2_degree == 1) {
                            rep = rep * 2.0f;
                        }
                    }
                    n2vx += (dx / sqBorderDist) * rep;
                    n2vy += (dy / sqBorderDist) * rep;



                    // limit the force
                    float vlimit = PApplet.min(vlimit_max, borderDist);
                    if (PApplet.abs(n2vx + n2gvx) > vlimit | PApplet.abs(n2vy + n2gvy) > vlimit) {
                        float tmpdist = PApplet.sqrt(PApplet.sq(n2vx + n2gvx) + PApplet.sq(n2vy + n2gvy));

                        n2vx = ((n2vx + n2gvx) / tmpdist) * vlimit;
                        n2vy = ((n2vy + n2gvy) / tmpdist) * vlimit;
                    }

                    // limit the gravity force
                    /*
                    vlimit = PApplet.min(vlimit_max, gdistance);
                    if (PApplet.abs(n2gvx) > vlimit | PApplet.abs(n2gvy) > vlimit) {
                    n2gvx = (gdx / gdistance) * vlimit;
                    n2gvy = (gdy / gdistance) * vlimit;
                    }
                     *
                     */

                    // apply the forces
                    n2x += n2vx * decay;
                    n2y += n2vy * decay;
                }

                // enforce a global size limit then save the new position
                n2.position.set(
                        PApplet.constrain(n2x, -8000, +8000),
                        PApplet.constrain(n2y, -8000, +8000),
                        0.0f);
            }
        }
    }

}
