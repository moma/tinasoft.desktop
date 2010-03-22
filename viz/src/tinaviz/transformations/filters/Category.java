/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.transformations.filters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tinaviz.Node;
import tinaviz.transformations.NodeFilter;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class Category extends NodeFilter {

    private String KEY_CATEGORY = "value";
    private String KEY_MODE = "mode";

    @Override
    public List<Node> process(Session session, View view, List<Node> input) {
        List<Node> output = new LinkedList<Node>();
        if(!enabled()) {
            return input;
        }

        if (!view.properties.containsKey(root+KEY_CATEGORY)) {
            view.properties.put(root+KEY_CATEGORY, "Document");
        }

        if (!view.properties.containsKey(root+KEY_MODE)) {
            view.properties.put(root+KEY_MODE, "keep");
        }



        String category = (String) view.properties.get(root+KEY_CATEGORY);
        String mode = (String) view.properties.get(root+KEY_MODE);

        boolean keep = mode.equals("keep");

        System.out.println("we are going to "+mode+" the category "+category+" got "+input.size()+" nodes in entry");

        for (Node n : input) {
            // System.out.println("  - n category == "+n.category);

            if (n.category.equals(category)) {
                if (keep) output.add(n);
            } else {
               if (!keep) output.add(n);
            }
        }
        return output;
    }

}
