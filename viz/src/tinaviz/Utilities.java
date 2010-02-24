/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author uxmal
 */
public class Utilities {
    /**
     * Given a Throwable, gets the full stack trace for the
     * Exception or Error as a String.  Returns an empty string
     * if something went wrong (so the caller won't fail with a
     * null pointer exception later).
     * @param t
     * @return
     * @author kolichko Mark Kolich
     */
    public static String getStackTraceAsString(Throwable t) {

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        String trace = new String();

        try {
            t.printStackTrace(pw);
            pw.flush();
            sw.flush();
            trace = sw.toString();
        } catch (Exception e) {
        } finally {
            try {
                sw.close();
                pw.close();
            } catch (Exception e) {
            }
        }
        return trace;
    }
}
