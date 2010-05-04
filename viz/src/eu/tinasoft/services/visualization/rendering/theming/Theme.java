/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.visualization.rendering.theming;

import java.awt.Color;
import processing.core.*;

/**
 *
 * @author jbilcke
 */
public class Theme {

    public static enum Shape {

        CIRCLE, SQUARE, ICON
    }

    public static class background {

        final static public Color color_default = new Color(255, 255, 255, 255);
    }

    public static class node {

        final static public Color color = new Color(242, 241, 241, 255);
        final static public int size_default = 14;

        public static class project {

            final static public Shape shape = Shape.CIRCLE;
            final static public Color color_default = new Color(242, 241, 241, 255);
            final static public Color color_highlight = new Color(242, 241, 241, 255);
            final static public float stroke_width = 2.0f;
            final static public Color stroke_default = new Color(242, 241, 241, 255);
            final static public Color stroke_highlight = new Color(242, 241, 241, 255);
        }

        public static class keyword {

            final static public Color color_default = new Color(242, 241, 241, 255);
            final static public Color color_highlight = new Color(242, 241, 241, 255);
            final static public float stroke_width = 2.0f;
            final static public Color stroke_default = new Color(242, 241, 241, 255);
            final static public Color stroke_highlight = new Color(242, 241, 241, 255);
        }
    }

    public static class edge {

        final static public Color color = new Color(242, 241, 241, 255);

        public static class project {

            final static public Color color_default = new Color(242, 241, 241, 255);
            final static public Color color_highlight = new Color(242, 241, 241, 255);
            final static public Color stroke_default = new Color(242, 241, 241, 255);
            final static public Color stroke_highlight = new Color(242, 241, 241, 255);
        }

        public static class keyword {

            final static public Color color_default = new Color(242, 241, 241, 255);
            final static public Color color_highlight = new Color(242, 241, 241, 255);
            final static public Color stroke_default = new Color(242, 241, 241, 255);
            final static public Color stroke_highlight = new Color(242, 241, 241, 255);
        }
    }

    public static class font {

        final static public String name = "Arial";
        final static public int size = 14;
    }

    public static class algo {

        final static public float edgeStrength = 0.002f;
        final static public float edgeLength = 20f;
        final static public float spacerMinimumDistance = 20;
        final static public float spacerStrength = -2000f;
    }
    final static public float smoothing = 0.9f;
}
