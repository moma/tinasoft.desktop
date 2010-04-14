/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;

/**
 *
 * @author jbilcke
 */
public class Layout extends NodeFilter {

    private String KEY_CATEGORY = "value";
    private String KEY_MODE = "mode";
    
    @Override
    public NodeList process(Session session, View v, NodeList input) {
   
        if(!enabled()) {
            return input;
        }


/*
        if (!view.properties.containsKey(root+KEY_CATEGORY)) {
            view.properties.put(root+KEY_CATEGORY, "Document");
        }

        if (!view.properties.containsKey(root+KEY_MODE)) {
            view.properties.put(root+KEY_MODE, "keep");
        }


        String category = (String) view.properties.get(root+KEY_CATEGORY);
        String mode = (String) view.properties.get(root+KEY_MODE);
 */


        double distance = 1;
        double vx = 1;
        double vy = 1;

        float repulsion = v.repulsion;
        float attraction = v.attraction;

        for (Node n1 : input.nodes) {
            for (Node n2 : input.nodes) {
                if (n1 == n2) {
                    continue;
                }

                // todo: what happen when vx or vy are 0 ?
                vx = n2.x - n1.x;
                vy = n2.y - n1.y;

                distance = Math.sqrt(Math.pow(vx,2) + Math.pow(vy,2)) + 0.0000001;


                //if (distance < (n1.radius + n2.radius)*2) distance = (n1.radius + n2.radius)*2;
                // plutot que mettre une distance minimale,
                // mettre une force de repulsion, par exemple
                // radius * (1 / distance)   // ou distance au carré
                if (n1.neighbours.contains(n2.uuid)) {
                    distance *= (n1.weights.get(n2.uuid));
                    n1.vx += (vx * distance) * attraction;
                    n1.vy += (vy * distance) * attraction;
                    n2.vx -= (vx * distance) * attraction;
                    n2.vy -= (vy * distance) * attraction;
                }

                // STANDARD REPULSION
                n1.vx -= (vx / distance) * repulsion;
                n1.vy -= (vy / distance) * repulsion;
                n2.vx += (vx / distance) * repulsion;
                n2.vy += (vy / distance) * repulsion;

            }
         } // FOR NODE B
        for (Node n : input.nodes) {
            // important, we limit the velocity!

            if (n.vx < -5) n.vx = -5;
            if (n.vx > 5) n.vx = 5;
            if (n.vy < -5) n.vx = -5;
            if (n.vy > 5) n.vx = 5;

            // update the coordinate
            // also set the bound box for the whole scene
            n.x = n.x + n.vx * 0.5f;
            n.y = n.y + n.vy * 0.5f;

            if (n.x < -5) n.vx = -5000;
            if (n.x > 5) n.vx = 5000;
            if (n.y < -5) n.vx = -5000;
            if (n.y > 5) n.vx = 5000;

            // update the original, "stored" node
            n.original.x = n.x;
            n.original.y = n.y;

            n.vx = 0.0f;
            n.vy = 0.0f;
        }
        return input;
    }

}
