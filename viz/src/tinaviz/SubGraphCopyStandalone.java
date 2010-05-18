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
public class SubGraphCopyStandalone extends NodeFilter {

    private String KEY_SOURCE = "source";
    private String KEY_ITEM = "item";
    private int oldItem = -0;
    private int item = -0;
    private String source = "macro";

    @Override
    public NodeList preProcessing(Session session, View localView, NodeList input) {
        NodeList output = new NodeList();
        if (!enabled()) {
            return output;
        }

        System.out.println("debugging subgraph copier");

        if (!localView.properties.containsKey(root + KEY_SOURCE)) {
            localView.properties.put(root + KEY_SOURCE, "macro");
        }


        if (!localView.properties.containsKey(root + KEY_ITEM)) {
            localView.properties.put(root + KEY_ITEM, -1);
        }

        source = (String) localView.properties.get(root + KEY_SOURCE);
        System.out.println("source=" + source);
        View sourceView = session.getView(source);
        if (sourceView == null) {
            System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got " + source);
            return output;
        }


        Object o = localView.properties.get(root + KEY_ITEM);
        if (o == null) {
            System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return output;
        }


        if (o instanceof String) {
            item = ((String) o).hashCode();
        } else {
            Console.error("bad type for " + root + KEY_ITEM);
            return output;
        }
        System.out.println("root is \"" + item + "\"");


        if (sourceView.getGraph().size() < 1) {
            System.out.println("original graph is zero-sized.. ");
            return output;
        }
        System.out.println("current view size: " + localView.getGraph().size());

        /*if (localView.getGraph().size() > 0) {
            System.out.println("view.graph.size() > 0 is TRUE !");
            return output;
        }*/

        if (!sourceView.getGraph().storedNodes.containsKey(item)) {
            System.out.println("the key doesn't exists! but that's probably not that bad.");
            return output;
        }

        if (item != oldItem) {
            System.out.println("updating..");
            NodeList newNodes = new NodeList();

            // do a clean copy
            Node rootNode = sourceView.getGraph().getNode(item).getDetachedClone();
            newNodes.add(rootNode);
            for (int n : rootNode.weights.keys().elements()) {
                Node neighbourNode = sourceView.getGraph().getNode(n).getDetachedClone();
                neighbourNode.position = new PVector((float) Math.random() * 100f, (float) Math.random() * 100f);
                newNodes.add(neighbourNode);
            }
            newNodes.computeExtremums();
            newNodes.normalize();

            localView.getGraph().clear();
            localView.updateFromNodeList(newNodes);
            output = new NodeList(localView.getGraph().getNodeListCopy());
            oldItem = item;
        } else {
            System.out.println("nothing changed, still old category");
            output = input;
        }

        return output;
    }
}
