/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.view;

import java.util.List;
import tinaviz.filters.NodeList;
import tinaviz.graph.Node;
import tinaviz.session.Session;
import tinaviz.view.View;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public NodeList process(Session session, View view, NodeList input);
  public boolean enabled();
  public void setEnabled(boolean b);
  public void setRoot(String root);
  public String getRoot();
}
