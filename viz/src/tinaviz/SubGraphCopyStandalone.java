/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import eu.tinasoft.services.session.ViewNotFoundException;

import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.data.model.Node;
import eu.tinasoft.services.data.transformation.NodeFilter;
import eu.tinasoft.services.debug.Console;
import eu.tinasoft.services.session.Session;

import eu.tinasoft.services.visualization.views.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import processing.core.PVector;

/* FIXME TODO WARNING : ADD SOME LOCKS..
 * */
/**
 *
 * @author jbilcke
 */
public class SubGraphCopyStandalone extends NodeFilter {

    private String KEY_SOURCE = "source";
    private String KEY_CATEGORY = "category";
    private String defaultSource = "macro";
    private String source = defaultSource;
    private String defaultCategory = "NO_CATEGORY";
    private String oldCategory = defaultCategory;

    @Override
    public NodeList preProcessing(Session session, View localView, NodeList input) {
        NodeList output = new NodeList();
        if (!enabled()) {
            return output;
        }

        HashMap<Integer, Node> oldSelection = new HashMap<Integer, Node>();

        System.out.println("debugging subgraph copier");

        if (!localView.properties.containsKey(root + KEY_SOURCE)) {
            localView.properties.put(root + KEY_SOURCE, "macro");
        }



        if (!localView.properties.containsKey(root + KEY_CATEGORY)) {
            localView.properties.put(root + KEY_CATEGORY, defaultCategory);
        }

        source = (String) localView.properties.get(root + KEY_SOURCE);
        System.out.println("source=" + source);

        View sourceView;
        try {
            sourceView = session.getView(defaultSource);
        } catch (ViewNotFoundException ex) {
            Console.error("Couldn't find source view " + defaultSource + ", aborting..");
            return output;
        }

        try {
            sourceView = session.getView(source);
        } catch (ViewNotFoundException ex) {
            Console.error("Couldn't find source view " + source + ", using default one..");
        }
        if (sourceView == null) {
            System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got " + source);
            return output;
        }

        String category = (String) localView.properties.get(root + KEY_CATEGORY);
        System.out.println("read KEY_CATEGORY to " + category);
        if (category == null) {
            Console.error("fatal exception, SubGraphCopyStandalone/CATEGORY is null!");
            category = defaultCategory;
        }

        if (sourceView.getGraph().size() < 1) {
            System.out.println("original graph is zero-sized.. ");
            return output;
        }
        System.out.println("MESO current view size: " + localView.getGraph().size());

        System.out.println("MESO category: " + category);
        System.out.println("MESO oldCategory: " + oldCategory);

        boolean categoryChanged = !category.equals(oldCategory);
        boolean renormalizationNeeded = false;


        for (Entry<Integer, Node> e : sourceView.getGraph().storedNodes.entrySet()) {
            if (true) break;
            if (e.getValue().selected) {

                // TODO:  update if current selection != old selection
                if (categoryChanged) {
                    Node rootNode =
                            sourceView.getGraph().getNode(e.getKey()).getDetachedClone(); // we want the clone

                    //newNodes.addWithoutTouching(rootNode);
                    for (int n : rootNode.weights.keys().elements()) {
                        Node neighbourNode = sourceView.getGraph().getNode(n).getDetachedClone();
                        if (!neighbourNode.category.equals(category)) {
                            continue;
                        }

                    }
                }

            }
        }

        if (categoryChanged) {

            System.out.println("MESO something (item or category) changed, updating subgraph copy....");
            NodeList newNodes = new NodeList();

            for (Entry<Integer, Node> e : sourceView.getGraph().storedNodes.entrySet()) {
                if (e.getValue().selected) {

                    // TODO:  update if current selection != old selection

                    if (categoryChanged) {
                        Node rootNode =
                                sourceView.getGraph().getNode(e.getKey()).getDetachedClone(); // we want the clone

                        newNodes.addWithoutTouching(rootNode);
                        for (int n : rootNode.weights.keys().elements()) {
                            Node neighbourNode = sourceView.getGraph().getNode(n).getDetachedClone();
                            if (!neighbourNode.category.equals(category)) {
                                continue;
                            }
                            neighbourNode.position = new PVector((float) Math.random() * 10f, (float) Math.random() * 10f);
                            newNodes.addWithoutTouching(neighbourNode);
                        }
                    }

                }
            }



            System.out.println("MESO computing extremums, and normalizing positions");

            newNodes.computeExtremums();
            newNodes.normalize();
            newNodes.normalizePositions();

            System.out.println("MESO newNodes size: " + newNodes.size());

            localView.getGraph().clear();
            localView.updateFromNodeList(newNodes);
            System.out.println("MESO localView size: " + localView.getGraph().size());
            output = new NodeList(localView.getGraph().getNodeListCopy());
            System.out.println("MESO output size: " + output.size());

            oldCategory = category;
        } else {
            System.out.println("MESO nothing changed, still old category");
            output = input;
        }

        return output;
    }
}

