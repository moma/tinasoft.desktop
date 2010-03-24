/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tinaviz.Node;
import tinaviz.transformations.NodeFilter;
import tinaviz.model.Session;
import tinaviz.model.View;

/* FIXME TODO WARNING : ADD SOME LOCKS..
 * */


/**
 *
 * @author jbilcke
 */

public class SubGraphCopy extends NodeFilter {

    private String KEY_SOURCE = "source";
    private String KEY_ITEM = "item";

    @Override
    public List<Node> process(Session session, View localView, List<Node> input) {

        if(!enabled()) {
            return input;
        }

        if (!localView.properties.containsKey(root+KEY_SOURCE)) {
            localView.properties.put(root+KEY_SOURCE, "macro");
        }

        if (!localView.properties.containsKey(root+KEY_ITEM)) {
            localView.properties.put(root+KEY_ITEM, "");
        }
       // System.out.println("SubGraphCopy called!");
        String source = (String) localView.properties.get(root+KEY_SOURCE);
        View sourceView = session.getView(source);
        if (sourceView==null) {
           // System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got "+source);
            return input;
        }

        String item = (String) localView.properties.get(root+KEY_ITEM);
        if (item==null) {
          //  System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return input;
        }

        if (sourceView.graph.size() < 1) {
           // System.out.println("original graph is zero-sized.. ");
            return input;
        }
        //System.out.println("current view size: "+localView.graph.size());
        if (localView.graph.size() > 0) {
            //System.out.println("view.graph.size() > 0 is TRUE !");
            return input;
        }

        if (!sourceView.graph.storedNodes.containsKey(item)) {
            // System.out.println("the key doesn't exists! but that's probably not that bad.");
            return input;
        }


        List<Node> newNodes = new LinkedList<Node>();

        List<Node> output = new LinkedList<Node>();
        
        Map<String,Node> sources = sourceView.graph.storedNodes;
        Node rootNode = sources.get(item).getDetachedClone();
        newNodes.add(rootNode);
        output.add(rootNode.getProxyClone());
       // System.out.println("added root at x:"+rootNode.x+" y:"+rootNode.y+" with "+rootNode.neighbours.size()+" neighbours");

        for (String neighbourId : sources.get(item).neighbours) {

            Node clonedNeighbourNode = sources.get(neighbourId).getDetachedClone();
            
            // a new graph, with new positions, each time we click
            clonedNeighbourNode.x =  (float) Math.random() * 100f;
            clonedNeighbourNode.y =  (float) Math.random() * 100f;

            //System.out.println("  - trying to add node x:"+sources.get(neighbourId).x+" y:"+sources.get(neighbourId).y+" ("+sources.get(neighbourId).neighbours.size()+" edges)");
            newNodes.add(clonedNeighbourNode);
            output.add(clonedNeighbourNode.getProxyClone());
            //System.out.println("  - added neighbour "+clonedNeighbourNode.label+ " ");

        }
         localView.updateFromNodeList(newNodes);
         localView.resetCamera();
       // System.out.println("added "+output.size()+" nodes to local view ("+localView.getName()+")");

        // FIXED we also need to copy the graph metrics! yeah baby!
        localView.graph.metrics = sourceView.graph.metrics.getClone();

        return output;
    }
}
