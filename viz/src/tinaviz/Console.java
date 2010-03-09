/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tinaviz;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Julian bilcke <julian.bilcke@iscpif.fr>
 */
public class Console {

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

    /**
     * Given a Throwable, gets the full stack trace for the
     * Exception or Error as a String.
     */
    public static void catchExceptionWithError(Throwable t) {
        error(Console.getStackTraceAsString(t));
    }

    public static void catchExceptionWithLog(Throwable t) {
        log(Console.getStackTraceAsString(t));
    }

    public static void catchExceptionWithDebug(Throwable t) {
        debug(Console.getStackTraceAsString(t));
    }

    public static void error(String s) {
        if (Main.window != null) {
            Main.window.eval("consoleError(\"" + s + "\");");
        }
        System.out.println("ERROR "+s);
        //Logger.getLogger("console").log(Level.SEVERE, s, null);

    }

    public static void log(String s) {
        if (Main.window != null) {
            Main.window.eval("consoleLog(\"" + s + "\");");
        }
        System.out.println("LOG "+s);
       // Logger.getLogger("console").log(Level.INFO, s, null);

    }

    public static void debug(String s) {
        if (Main.window != null) {
            Main.window.eval("consoleDebug(\"" + s + "\");");
        }
        System.out.println("DEBUG "+s);
       // Logger.getLogger("console").log(Level.FINE, s, null);

    }
}
