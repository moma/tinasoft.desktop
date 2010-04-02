/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tinaviz.graph.Node;
import tinaviz.view.NodeFilter;
import tinaviz.session.Session;
import tinaviz.view.View;

/* FIXME TODO WARNING : ADD SOME LOCKS..
 * */
/**
 *
 * @author jbilcke
 */
public class SubGraphCopy extends NodeFilter {

    private String KEY_SOURCE = "source";
    private String KEY_ITEM = "item";
    private String KEY_CATEGORY = "category";

    @Override
    public NodeList process(Session session, View localView, NodeList input) {

        if (!enabled()) {
            return input;
        }

        if (!localView.properties.containsKey(root + KEY_SOURCE)) {
            localView.properties.put(root + KEY_SOURCE, "macro");
        }

        if (!localView.properties.containsKey(root + KEY_CATEGORY)) {
            localView.properties.put(root + KEY_CATEGORY, "NGram");
        }

        if (!localView.properties.containsKey(root + KEY_ITEM)) {
            localView.properties.put(root + KEY_ITEM, -1);
        }
        // System.out.println("SubGraphCopy called!");
        String source = (String) localView.properties.get(root + KEY_SOURCE);
        View sourceView = session.getView(source);
        if (sourceView == null) {
             System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got "+source);
            return input;
        }

        String category = (String) localView.properties.get(root + KEY_CATEGORY);
        if (category==null | category.isEmpty()) {
              System.out.println("uh oh! i am a source and my 'category' parameter is totally wrong! got "+category);
        }


        Long item = 0L;
        String cat = "";
        Object o = localView.properties.get(root + KEY_ITEM);
        if (o == null) {
            //  System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return input;
        }
        if (o instanceof String) {
            cat = ((String) o).split("::")[0];
            item = Long.parseLong( ((String) o).split("::")[1] );

        } else {
            System.out.println("bad type for "+root + KEY_SOURCE+", expected this pattern: '[a-zA-Z]+::[0_9]+'");
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


        NodeList newNodes = new NodeList();

        NodeList output = new NodeList();

        Map<Long, Node> sources = sourceView.graph.storedNodes;
        Node rootNode = sources.get(item).getDetachedClone();
        newNodes.add(rootNode);
        output.add(rootNode.getProxyClone());
        // System.out.println("added root at x:"+rootNode.x+" y:"+rootNode.y+" with "+rootNode.neighbours.size()+" neighbours");

        for (Long potentialNeighbourId : sources.get(item).neighbours) {
            Node potentialNeighbour = sources.get(potentialNeighbourId);
            if (rootNode.neighbours.contains(potentialNeighbourId) | potentialNeighbour.neighbours.contains(item)) {
                if (!potentialNeighbour.category.equalsIgnoreCase(category))
                    continue;
                Node clonedNeighbourNode = potentialNeighbour.getDetachedClone();

                // a new graph, with new positions, each time we click
                clonedNeighbourNode.x = (float) Math.random() * 100f;
                clonedNeighbourNode.y = (float) Math.random() * 100f;

                //System.out.println("  - trying to add node x:"+sources.get(neighbourId).x+" y:"+sources.get(neighbourId).y+" ("+sources.get(neighbourId).neighbours.size()+" edges)");
                newNodes.add(clonedNeighbourNode);
                output.add(clonedNeighbourNode.getProxyClone());
                //System.out.println("  - added neighbour "+clonedNeighbourNode.label+ " ");

            }
        }
        localView.clear();
        localView.updateFromNodeList(newNodes);
        //localView.resetCamera();

        return output;
    }
}
