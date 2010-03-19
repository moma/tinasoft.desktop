/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.transformations;

import java.util.List;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public interface FilterChainListener {
    public void filterChainOutput(List<Node> nodes);
}
