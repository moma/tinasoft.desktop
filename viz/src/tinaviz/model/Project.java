/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbilcke
 */
public class Project {

    public String name;
    public String category;
    public String batch;
    public List<String> keywords = new ArrayList<String>();
    public List relatedProjects;
    public List neighbourKeywords;
}
