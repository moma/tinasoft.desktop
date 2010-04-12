/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
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
            System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got " + source);
            return input;
        }

        String category = (String) localView.properties.get(root + KEY_CATEGORY);
        if (category == null | category.isEmpty()) {
            System.out.println("uh oh! i am a source and my 'category' parameter is totally wrong! got " + category);
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
            item = Long.parseLong(((String) o).split("::")[1]);

        } else {
            System.out.println("bad type for " + root + KEY_SOURCE + ", expected this pattern: '[a-zA-Z]+::[0_9]+'");
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
        //System.out.println("cat: " + cat + " category:" + category);

        if (cat.equals(category)) {
            //System.out.println("generating the same gender graph..");
            // same category: trivial
            for (Long potentialNeighbourId : sources.get(item).neighbours) {
                Node potentialNeighbour = sources.get(potentialNeighbourId);
                if (rootNode.neighbours.contains(potentialNeighbourId) | potentialNeighbour.neighbours.contains(item)) {
                    if (!potentialNeighbour.category.equalsIgnoreCase(category)) {
                        continue;
                    }


                    Node localNode = potentialNeighbour.getDetachedClone();

                    // a new graph, with new positions, each time we click
                    localNode.x = (float) Math.random() * 100f;
                    localNode.y = (float) Math.random() * 100f;

                    //System.out.println("  - trying to add node x:" + localNode.x + " y:" + localNode.y + " (" + localNode.neighbours.size() + " edges)");

                    newNodes.add(localNode);
                    output.add(localNode.getProxyClone());
                    //System.out.println("  - added neighbour "+clonedNeighbourNode.label+ " ");

                }
            }
        } else {
            //System.out.println("generating the hybrid graph..");
            List<String> paramList = new ArrayList<String>();
            paramList.add(rootNode.category);
            paramList.add("" + rootNode.uuid);
            paramList.add(category);

            /* JSObject document = (JSObject)session.browser.window.getMember("document");
            JSObject parent = (JSObject)document.getMember("parent");
            JSObject tinaviz = (JSObject)parent.getMember("tinaviz");
            System.out.println(tinaviz.toString());*/


            String neighboursString = (String) session.browser.window.call("getNeighbours", paramList.toArray());
            //System.out.println("neighboursString=" + neighboursString);
            String[] neighboursArray = neighboursString.split(";");
            //System.out.println("neighboursArray=" + neighboursArray);
            Map<Long, Float> neighboursMap = new HashMap<Long, Float>();

            for (String st : neighboursArray) {
                String[] neigh = st.split(",");
                long neighbourID = Long.parseLong(neigh[0]);
                float neighbourWeight = Float.parseFloat(neigh[1]);
                //System.out.println("  - "+neighbourID+": "+neighbourWeight);
                if (!localView.graph.storedNodes.containsKey(neighbourID)) {

                    if (!sources.containsKey(neighbourID)) {
                        //System.out.println("Error, creating new nodes is not implemented");
                        continue;
                    }
                    Node localNode = sources.get(neighbourID).getDetachedClone();
                    //System.out.println("  - trying to add node x:" + localNode.x + " y:" + localNode.y + " (" + localNode.neighbours.size() + " edges)");

                    newNodes.add(localNode);
                    rootNode.addNeighbour(neighbourID);
                    output.add(localNode.getProxyClone());

                } else {
                    // already in the graph
                    Node localNode = localView.graph.storedNodes.get(neighbourID);
                    // System.out.println("  - node is already in graph x:" + localNode.x + " y:" + localNode.y + " (" + localNode.neighbours.size() + " edges)");

                    rootNode.addNeighbour(neighbourID);
                    output.add(localNode.getProxyClone());
                }


            }



        }
        localView.clear();
        localView.updateFromNodeList(newNodes);

        //localView.resetCamera();

        return output;
    }
}
