/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import java.util.List;
import java.util.Map;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public List<Node> process(List<Node> input, Map<String, Channel> channels);
  public void setEnabled(boolean enabled);
  public boolean getEnabled();
  public boolean toggleEnabled();
}
