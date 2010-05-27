/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.debug;

import eu.tinasoft.services.protocols.browser.Browser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import tinaviz.Main;

/**
 *
 * @author Julian bilcke <julian.bilcke@iscpif.fr>
 */
public class Console {

    private static Browser browser = new Browser();
    
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
       browser.callAndForget("logError","'"+s+"'");
       Logger.getLogger(Main.class.getName()).log(Level.SEVERE,  "[APPLET] ERROR "+s);
       // System.out.println("[APPLET] ERROR "+s);
    }

    public static void log(String s) {
       browser.callAndForget("logNormal","'"+s+"'");
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "[APPLET] LOG "+s);
    }

    public static void debug(String s) {
       browser.callAndForget("logDeubg","'"+s+"'");
        Logger.getLogger(Main.class.getName()).log(Level.INFO,"[APPLET] DEBUG "+s);
    }

    public static void setBrowser(Browser browser) {
        Console.browser = browser;
    }
}
