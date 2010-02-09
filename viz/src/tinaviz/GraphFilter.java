/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbilcke
 */
public class GraphFilter {

    public GraphFilter() {
    }

    public List<Node> filter(List<FilterChannel> channels, List<Node> nodes) {
        List<Node> output = new ArrayList<Node>();

        for (Node n : nodes) {
            for (FilterChannel f : channels) {

               if (f.match(n))
                   output.add(n);
            }
        }
        return output;
    }

}
