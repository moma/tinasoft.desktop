/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import java.util.List;
import tinaviz.Node;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public List<Node> process(View view, List<Node> input);
  public boolean enabled();
  public void setEnabled(boolean b);
}
