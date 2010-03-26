/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz.transformations.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import tinaviz.Node;
import tinaviz.transformations.NodeFilter;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public class Explorer extends NodeFilter {

    private String KEY_SOURCE = "source";
    private String KEY_ITEM = "item";

    @Override
    public List<Node> process(Session session, View view, List<Node> input) {
        List<Node> output = new LinkedList<Node>();
        if(!enabled()) {
            return output;
        }

        if (!view.properties.containsKey(root+KEY_SOURCE)) {
            view.properties.put(root+KEY_SOURCE, "macro");
        }

        if (!view.properties.containsKey(root+KEY_ITEM)) {
            view.properties.put(root+KEY_ITEM, "");
        }

       // System.out.println("Explorer called!");
        String source = (String) view.properties.get(root+KEY_SOURCE);
        View v = session.getView(source);
        if (v==null) {
            //System.out.println("uh oh! i am a source and my 'source' parameter is totally wrong! got "+source);
            return output;
        }

        Long item = (Long) view.properties.get(root+KEY_ITEM);
        if (item==null) {
            //System.out.println("uh oh! i am a source and my 'item' parameter is null! you're gonna have a bad day man.. ");
            return output;
        }

        if (!session.macro.graph.storedNodes.containsKey(item)) {
            // System.out.println("the key doesn't exists! but that's probably not that bad.");
            return output;
        }

        for (Long nodeID : ((Node)session.macro.graph.storedNodes.get(item)).neighbours) {
            output.add(session.macro.graph.storedNodes.get(nodeID));
        }
        return output;
    }
}
