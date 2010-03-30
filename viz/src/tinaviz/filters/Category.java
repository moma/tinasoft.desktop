/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;
import tinaviz.view.ViewLevel;

/**
 *
 * @author jbilcke
 */
public class Category extends NodeFilter {

    private String KEY_CATEGORY = "value";
    private String KEY_MODE = "mode";

    @Override
    public NodeList process(Session session, View view, NodeList input) {

        //System.out.println("CATEGORY FILTER 1");
        NodeList output = new NodeList();
        if (!enabled()) {
            return input;
        }
        //System.out.println("CATEGORY FILTER 2");
        for (String k : view.properties.keySet()) {
            System.out.println("prop "+k+" "+view.properties.get(k));
        }
        if (!view.properties.containsKey(root + KEY_CATEGORY)) {
            view.properties.put(root + KEY_CATEGORY, "Document");
        }

        if (!view.properties.containsKey(root + KEY_MODE)) {
            view.properties.put(root + KEY_MODE, "keep");
        }


        String category = (String) view.properties.get(root + KEY_CATEGORY);
        String mode = (String) view.properties.get(root + KEY_MODE);

        boolean keep = mode.equals("keep");

        System.out.println("we are going to " + mode + " the category " + category + " got " + input.size() + " nodes in entry");

        for (Node n : input.nodes) {
            //  System.out.println("  - n category == "+n.category);
            // HACK the category selector doesn't remove selecte dnodes
            if (view.getLevel() == ViewLevel.MESO && n.selected) {
                output.add(n);
            } else {

                if (n.category.equals(category)) {
                    if (keep) {
                        output.add(n);
                        //System.out.println("  - kept "+n.category+" "+n.uuid+" = "+n.genericity+"\n");
                    }
                } else {
                    if (!keep) {
                        output.add(n);
                        //System.out.println("  - n category == "+n.category+" added!\n");
                    }
                }
            }
        }

        System.out.println("NORMALIZING NODES WEIGHTS AFTER CATEGORY FILTERING");
        output.normalize();
        System.out.println("OUTPUT OF THe NORMALIZATION="+output.toString());
        return output;
    }
}
