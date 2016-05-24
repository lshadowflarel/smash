/*
 * Created on 20 Á.¤. 2548
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.xet.nxet.tool;

import java.util.logging.Level;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Tool {
    
    private boolean console = false;
    
    private String line = null;
    private String originName = null;
    private PrintStream out = null;
    private boolean previousPrint = false;
    private boolean print = false;
    private boolean showTime = false;
    
    public static final String LINE = "------------------------------" +
    								  "------------------------------" +
    								  "-------------------";
    public static final String ESTR = "";
    public static final String ELINE = "\n";
    
    private SimpleDateFormat smf = new SimpleDateFormat("kk:mm:ss");
    private PrintWriter writer = null;

    public void emptyLine() {
        if (print)
        out.println("");
    }
    
    public String fillSpace(String text, int length) {
        
        if (text.length() > length) {
            return (text.substring(0, length - 1) + " ");
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(text);
            int moreSpace = length - text.length();
            for (int i = 0; i < moreSpace; i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
        
    }
    
    public void forceEmptyLine() {
        if (console) {
            out.println("");
        } else {
            writer.write("\n");
        }
    }
    
    public void forceLine() {
        if (console) {
            out.println(line);
        } else {
            writer.write(line + "\n");
        }
    }
    
    public void forcePrint(String text) {
        if (console) {
            out.print(((showTime)? this.getTime() + " " : "") +
                      text);
        } else {
            writer.write(((showTime)? this.getTime() + " " : "") +
                         text);
        }
    }
    
    public void forcePrintln(String text) {
        if (console) {
            out.println(((showTime)? this.getTime() + " " : "") +
                	    text);
        } else {
            writer.write(((showTime)? this.getTime() + " " : "") +
            	         text);
        }
    }
    
    public String getLine() {
        return this.line;
    }
    
    private String getTime() {
        return this.smf.format(new Date());
    }
    
    
    /**
     * @return Returns the console.
     */
    public boolean isConsole() {
        return console;
    }
 
    /**
     * Returns whether to print or not
     * 
     * @return Returns the print.
     */
    public boolean isPrint() {
        return print;
    }
    
    /**
     * Return the level object
     * 
     * @param level The string representation of level
     * @return The level object
     */
    public static Level level(String level) {
        if (level.equalsIgnoreCase("Off")) {
            return Level.OFF;
        } else if (level.equalsIgnoreCase("Severe")) {
            return Level.SEVERE;
        } else if (level.equalsIgnoreCase("Warning")) {
            return Level.WARNING;
        } else if (level.equalsIgnoreCase("Info")) {
            return Level.INFO;
        } else if (level.equalsIgnoreCase("Config")) {
            return Level.CONFIG;
        } else if (level.equalsIgnoreCase("Fine")) {
            return Level.FINE;
        } else if (level.equalsIgnoreCase("Finer")) {
            return Level.FINER;
        } else if (level.equalsIgnoreCase("Finest")) {
            return Level.FINEST;
        } else if (level.equalsIgnoreCase("All")) {
            return Level.ALL;
        } else {
            return Level.OFF;
        }
    }
    
    public void line() {
        if (print)
        this.println(line);
    }
    
    public String middleTextDash(String text, int length) {
        
        if (text.length() > length) {
            return (text.substring(0, length - 1));
        } else {
            StringBuffer sb = new StringBuffer();
            int moreSpace = Math.round((length - text.length())/2);
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append("-");
            }
            sb.append(" " + text + " ");
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append("-");
            }
            return sb.toString();
        }
        
    }
    
    public String middleTextSpace(String text, int length) {
        
        if (text.length() > length) {
            return (text.substring(0, length - 1));
        } else {
            StringBuffer sb = new StringBuffer();
            int moreSpace = Math.round((length - text.length())/2);
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append(" ");
            }
            sb.append(" " + text + " ");
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
        
    }
    
    public String middleTextStar(String text, int length) {
        
        if (text.length() > length) {
            return (text.substring(0, length - 1));
        } else {
            StringBuffer sb = new StringBuffer();
            int moreSpace = Math.round((length - text.length())/2);
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append("*");
            }
            sb.append(" " + text + " ");
            for (int i = 0; i < moreSpace - 1; i++) {
                sb.append("*");
            }
            return sb.toString();
        }
        
    }
    
    public void print(String text) {
        if (print && console) {
            out.print(((showTime)? this.getTime() + " " : "") +
            	      text);
        } else if (print) {
            writer.write(((showTime)? this.getTime() + " " : "") +
          	             text);
        }
    }

    public void println(String text) {
        if (print && console) {
            out.println(((showTime)? this.getTime() + " " : "") +
                	    text);
        } else if (print) {
            writer.write(((showTime)? this.getTime() + " " : "") +
                	     text);
        }
    }
    
    public void restorePreviousPrint() {
        this.print = this.previousPrint;
    }
    /**
     * Decide whether to print to the console
     * or to the 
     * 
     * @param console The console to set.
     */
    public void setConsole(boolean console) {
        this.console = console;
    }
    
    /**
     * Assign the output printstream
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }
    
    /**
     * Set whether to print out or not
     * 
     * @param print The print to set.
     */
    public void setPrint(boolean print) {
        this.previousPrint = this.print;
        this.print = print;
    }
    
}
