/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author jbilcke
 */
public class SimpleCSS {

    /*
    span {
    background: transparent url(img/btn_right.gif) no-repeat right top;
    display: block;
    float: left;
    padding: 0px 4px 0px 0px; // sliding doors padding
    margin: 0px;
    height: 33px;
    }*/
    // (\\s*\\(w+)\\s*:\\s*\\((?:'|\")?[a-zA-Z0-9#]+(?:'|\")?)\\s*;)*\\s*\\" +
    //        "
    Pattern block = Pattern.compile("((\\w))*", Pattern.CASE_INSENSITIVE);
    Pattern attr = Pattern.compile("(\\s*(\\w+)\\s*:\\s+(?:'|\")?[a-zA-Z0-9#]+(?:'|\")?)\\s*;)*", Pattern.CASE_INSENSITIVE);
    Map<String, String> attributes = new HashMap<String, String>();

    public SimpleCSS(String content) {

        Matcher matcher = block.matcher(content);
        while (matcher.find()) {
            //attributes.add(matcher.group(2).trim());
                    /*
            String[] items = p.split(content);
            for(String s : items) {
            System.out.println(s);
            }*/
        }



    }

    /* getFloat("view.node.width"); */
    public float getFloat(String path, float def) {

        return def;
    }

    /*
    import java.util.regex.Pattern;
    import java.util.regex.Matcher;

    public class SplitDemo {

    private static final String REGEX = ":";
    private static final String INPUT = "one:two:three:four:five";

    public static void main(String[] args) {
    Pattern p = Pattern.compile(REGEX);
    String[] items = p.split(INPUT);
    for(String s : items) {
    System.out.println(s);
    }
    }
    }

     */
}
