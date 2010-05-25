/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.NodeList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import eu.tinasoft.services.visualization.views.ViewLevel;

/**
 *
 * @author jbilcke
 */
public class Normalize extends NodeFilter {

    private String KEY_CATEGORY = "value";
    private String KEY_MODE = "mode";
    private String category = "Document";
    private String oldCategory = "__OLD_CATEGORY__";

    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {

        //System.out.println("CATEGORY FILTER 1");
        NodeList output = new NodeList();

        if (!enabled()) {
            return input;
        }
        //System.out.println("CATEGORY FILTER 2");
        for (String k : view.properties.keySet()) {
            //System.out.println("prop "+k+" "+view.properties.get(k));
        }
        if (!view.properties.containsKey(root + KEY_CATEGORY)) {
            view.properties.put(root + KEY_CATEGORY, "Document");
        }

        if (!view.properties.containsKey(root + KEY_MODE)) {
            view.properties.put(root + KEY_MODE, "keep");
        }


        // get the new category
        // if this is a switch: we suppose we need to refresh the view

        category = (String) view.properties.get(root + KEY_CATEGORY);

        String mode = (String) view.properties.get(root + KEY_MODE);

        boolean keep = mode.equals("keep");

        //System.out.println("we are going to " + mode + " the category " + category + " got " + input.size() + " nodes in entry");

        for (Node n : input.nodes) {
            //  System.out.println("  - n category == "+n.category);
            // HACK the category selector doesn't remove selecte dnodes
            if (view.getLevel() == ViewLevel.MESO && n.selected) {
                output.add(n);
            } else {

                if (n.category.equals(category)) {
                    if (keep) {
                        output.add(n);
                        //System.out.println("  - kept " + n.category + " " + n.label + " = " + n.weight + "\n");
                    }
                } else {
                    if (!keep) {
                        output.add(n);
                        //System.out.println("  - n category == "+n.category+" added!\n");
                    }
                }
            }
        }


        output.computeExtremums();
        output.normalize();

        // should we normalize positions as well?

        if (false) {
            if (view.graph.topologyChanged.get() | !oldCategory.equals(category)) {
                output.normalizePositions();
            }
        }
        oldCategory = category;

        //System.out.println("OUTPUT OF THe NORMALIZATION="+output.toString());
        return output;
    }
}
