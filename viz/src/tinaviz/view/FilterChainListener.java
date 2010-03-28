/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.view;

import java.util.List;
import tinaviz.graph.Node;

/**
 *
 * @author jbilcke
 */
public interface FilterChainListener {
    public void filterChainOutput(List<Node> nodes);
}
