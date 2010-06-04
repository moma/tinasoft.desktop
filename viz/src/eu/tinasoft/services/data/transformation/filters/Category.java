/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import cern.colt.map.OpenIntObjectHashMap;
import eu.tinasoft.services.data.model.NodeList;

import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;
import eu.tinasoft.services.visualization.views.ViewLevel;

/**
 *
 * @author jbilcke
 */
public class Category extends NodeFilter {

    private String KEY_CATEGORY = "category";
    private String KEY_MODE = "mode";
    private String category = "Document";
    private String oldCategory = "__OLD_CATEGORY__";
    private boolean HACK_ME = false;

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
            if (n.category.equals(category)) {
                //System.out.println("adding node "+n.category+" / "+n.uuid+" weight: "+n.weight+"  radius: "+n.radius);
                output.add(n);

            }
        }

        output.computeMetrics();
        output.normalize();

        oldCategory = category;
        return output;
    }
}
