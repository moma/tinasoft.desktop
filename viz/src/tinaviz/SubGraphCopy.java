/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.session.Session;

import eu.tinasoft.services.visualization.views.View;
import processing.core.PVector;

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
    private int oldItem = -0;
    private int item = -0;

    private String category = "NGram";
    private String oldCategory = "NGram";
    private String source = "macro";
    @Override
    public NodeList preProcessing(Session session, View localView, NodeList input) {

        //Console.log("generating subgraph..");
        if (!enabled()) {
            return input;
        }
        System.out.println("debugging subgraph copier");

        if (!localView.properties.containsKey(root + KEY_SOURCE)) {
            localView.properties.put(root + KEY_SOURCE, "macro");
        }

        if (!localView.properties.containsKey(root + KEY_CATEGORY)) {
            localView.properties.put(root + KEY_CATEGORY, "NGram");
        }
        if (!localView.properties.containsKey(root + KEY_ITEM)) {
            localView.properties.put(root + KEY_ITEM, -1);
        }

        source = (String) localView.properties.get(root + KEY_SOURCE);
        System.out.println("source="+source);
        View sourceView = session.getView(source);
        if (sourceView == null) {
            System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got " + source);
            return input;
        }

        oldCategory = category;
        category = (String) localView.properties.get(root + KEY_CATEGORY);
        if (category == null | category.isEmpty()) {
            System.out.println("uh oh! i am a source and my 'category' parameter is totally wrong! got " + category);
            return input;
        }



        oldItem = item;
        String cat = "";
        Object o = localView.properties.get(root + KEY_ITEM);
        if (o == null) {
            System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return input;
        }


        if (o instanceof String) {

            if (((String)o).contains("::")) {
                cat = ((String) o).split("::")[0];
                o = ((String) o).split("::")[1];
            } else {
                cat = "NO_CATEGORY";
            }
            item = ((String)o).hashCode();
            
        } else {
            Console.error("bad type for " + root + KEY_ITEM + ", expected this pattern: '[a-zA-Z]+::[0_9]+'");
            return input;
        }
        System.out.println("root is \""+item+"\"");


        if (sourceView.graph.size() < 1) {
            System.out.println("original graph is zero-sized.. ");
            return input;
        }
        System.out.println("current view size: "+localView.graph.size());
        if (localView.graph.size() > 0) {
            System.out.println("view.graph.size() > 0 is TRUE !");
            return input;
        }

        if (!sourceView.graph.storedNodes.containsKey(item)) {
            System.out.println("the key doesn't exists! but that's probably not that bad.");
            return input;
        }



        NodeList newNodes = new NodeList();

        NodeList output = new NodeList();

        Map<Integer, Node> sources = sourceView.graph.storedNodes;
        Node rootNode = sources.get(item).getDetachedClone();
        newNodes.add(rootNode);
        output.add(rootNode.getProxyClone());
        System.out.println("added root at x:"+rootNode.position.x+" y:"+rootNode.position.y+" with "+rootNode.weights.size()+" neighbours");
        System.out.println("cat: " + cat + " category:" + category);

        if (cat.equals(category)) {
            //System.out.println("generating the same gender graph..");
            // same category: trivial
            for (int potentialNeighbourId : sources.get(item).weights.keys().elements()) {
                Node potentialNeighbour = sources.get(potentialNeighbourId);
                if (rootNode.weights.containsKey(potentialNeighbourId) | potentialNeighbour.weights.containsKey(item)) {
                    if (!potentialNeighbour.category.equalsIgnoreCase(category)) {
                        continue;
                    }


                    Node localNode = potentialNeighbour.getDetachedClone();

                    // a new graph, with new positions, each time we click
                    localNode.position = new PVector((float) Math.random() * 100f,(float) Math.random() * 100f);

                    //System.out.println("  - trying to add node x:" + localNode.x + " y:" + localNode.y + " (" + localNode.neighbours.size() + " edges)");
                     //System.out.println("  - trying to add "+localNode.category+" " + localNode.label + " with weight "+ localNode.weight + " ");

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
            if (neighboursString==null) {
                // Console.log("asked for getNeighbours(), but got NULL as reply");
                return input;
            }
            // System.out.println("neighboursString=" + neighboursString);
            String[] neighboursArray = neighboursString.split(";");
            //System.out.println("neighboursArray=" + neighboursArray);
            Map<Long, Float> neighboursMap = new HashMap<Long, Float>();

            for (String st : neighboursArray) {
                String[] neigh = st.split(",");
                int neighbourID = neigh[0].hashCode();
                float neighbourWeight = Float.parseFloat(neigh[1]);
                //System.out.println("  - "+neighbourID+": "+neighbourWeight);
                if (!localView.graph.storedNodes.containsKey(neighbourID)) {

                    if (!sources.containsKey(neighbourID)) {
                        //System.out.println("Error, creating new nodes is not implemented");
                        continue;
                    }
                    Node localNode = sources.get(neighbourID).getDetachedClone();
                    //System.out.println("  - trying to add node x:" + localNode.x + " y:" + localNode.y + " (" + localNode.neighbours.size() + " edges)");
                    localNode.position = new PVector((float) Math.random() * 100f,(float) Math.random() * 100f);

                    
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
        newNodes.computeExtremums();
        newNodes.normalize();
        localView.updateFromNodeList(newNodes);
        output.computeExtremums();
        output.normalize();

        //localView.resetCamera();
        return output;
    }
}
