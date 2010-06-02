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
public class NodeFunction extends NodeFilter {


    @Override
    public NodeList preProcessing(Session session, View view, NodeList input) {
        if (!enabled()) {
            return input;
        }

        for (Node n : input.nodes) {
              // System.out.println(" node funct: "+(n.radius * n.weight)+ " = "+n.radius+" * "+n.weight);
              n.radius =  n.weight;
 
        }

        // hmm..
        System.out.println("(NodeFunction.java / line 34) RADIUS HACK, FIX ME");
        input.getMetrics().minRadius = input.getMetrics().minNodeWeight;
        input.getMetrics().maxRadius = input.getMetrics().maxNodeWeight;

        return input;
    }
}
