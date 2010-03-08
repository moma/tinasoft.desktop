/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.filters;

import java.security.KeyException;
import java.util.List;
import tinaviz.Node;

/**
 *
 * @author jbilcke
 */
public interface Filter {
  public List<Node> process(List<Node> input);

  public void setField(String key, String value) throws KeyException;
  public void setField(String key, float value) throws KeyException;
  public void setField(String key, int value) throws KeyException;
  public void setField(String key, boolean value) throws KeyException;

  public Object getField(String key) throws KeyException;

}
