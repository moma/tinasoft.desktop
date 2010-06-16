/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.data.transformation.filters;

import eu.tinasoft.services.data.model.NodeList;

import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public class Category extends NodeFilter {

    private String KEY_CATEGORY = "category";
    private String category = "Document";
 
    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {

        NodeList output = new NodeList();

        if (!enabled()) {
            return input;
        }
        if (!view.properties.containsKey(root + KEY_CATEGORY)) {
            view.properties.put(root + KEY_CATEGORY, "Document");
        }

        category = (String) view.properties.get(root + KEY_CATEGORY);

        for (Node n : input.nodes) {
            if (n.category.equals(category)) {
                output.add(n);

            }
        }


        output.computeMetrics();
        output.normalize();

        return output;
    }
}
