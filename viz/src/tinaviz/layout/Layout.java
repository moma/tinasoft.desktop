/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.layout;

import java.util.List;
import processing.core.PApplet;
import tinaviz.graph.Node;
import tinaviz.view.View;

/**
 *
 * @author uxmal
 */
public class Layout {

    public void fast(View v, List<Node> nodes) {
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        float gravity = 0.00001f;

        for (Node n1 : nodes) {
            // gravity
            vx = 0 - n1.x;
            vy = 0 - n1.y;

            distance = PApplet.sqrt(PApplet.sq(vx) + PApplet.sq(vy)) + 0.0000001f;
            n1.vx += vx * distance * gravity;
            n1.vy += vy * distance * gravity;

            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }

                // todo: what happen when vx or vy are 0 ?
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                distance = PApplet.sqrt(PApplet.sq(vx) + PApplet.sq(vy)) + 0.0000001f;

                //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                // plutot que mettre une distance minimale,
                // mettre une force de repulsion, par exemple
                // radius * (1 / distance)   // ou distance au carré
                if (n1.neighbours.contains(n2.uuid)) {
                    float w = 1.0f + n1.weights.get(n2.uuid);
                    n1.vx += vx * distance * w * attraction;
                    n1.vy += vy * distance * w * attraction;
                    n2.vx -= vx * distance * w * attraction;
                    n2.vy -= vy * distance * w * attraction;
                } else {
                    // STANDARD REPULSION
                    n1.vx -= (vx / distance) * repulsion;
                    n1.vy -= (vy / distance) * repulsion;
                    n2.vx += (vx / distance) * repulsion;
                    n2.vy += (vy / distance) * repulsion;
                }

                //}
            } // FOR NODE B
            // important, we limit the velocity!
            n1.vx = PApplet.constrain(n1.vx, -300, 300);
            n1.vy = PApplet.constrain(n1.vy, -300, 300);

            // update the coordinate
            // also set the bound box for the whole scene
            n1.x = PApplet.constrain(n1.x + n1.vx * 0.5f, -8000, +8000);
            n1.y = PApplet.constrain(n1.y + n1.vy * 0.5f, -8000, +8000);

            if (n1.original != null) {
                n1.original.x = n1.x;
                n1.original.y = n1.y;
            }

            n1.vx = 0.0f;
            n1.vy = 0.0f;
        }   // FOr NODE A
    }

    public void precise(View v, List<Node> nodes) {
        float distance = 1f;
        float vx = 1f;
        float vy = 1f;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 == n2) {
                    continue;
                }
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;
                // distance = FastSquareRoot.fast_sqrt(sq(vx)+sq(vy)) +  0.0000001f;
                distance = PApplet.sqrt(PApplet.sq(vx) + PApplet.sq(vy)) + 0.0000001f;
                // int badSquare = FastSquareRoot.fastSqrt((int)((sq(vx)+sq(vy))*1000));
                //distance = ((float) badSquare) / 1000.0f;
                //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                // plutot que mettre une distance minimale,
                // mettre une force de repulsion, par exemple
                // radius * (1 / distance)   // ou distance au carré
                if (n1.neighbours.contains(n2.uuid)) {
                    float w = 1.0f + n1.weights.get(n2.uuid);
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


        for (Node n : nodes) {
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
