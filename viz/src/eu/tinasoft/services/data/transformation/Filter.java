/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.tinasoft.services.data.transformation;

import eu.tinasoft.services.data.model.NodeList;
import eu.tinasoft.services.session.Session;
import eu.tinasoft.services.visualization.views.View;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public NodeList preProcessing(Session session, View view, NodeList input);
  public boolean enabled();
  public void setEnabled(boolean b);
  public void setRoot(String root);
  public String getRoot();
}
