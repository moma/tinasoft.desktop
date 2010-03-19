/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.transformations;

import java.util.List;
import tinaviz.Node;
import tinaviz.model.Session;
import tinaviz.model.View;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public List<Node> process(Session session, View view, List<Node> input);
  public boolean enabled();
  public void setEnabled(boolean b);
  public void setRoot(String root);
  public String getRoot();
}
