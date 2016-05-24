/*
 * Created on 20 Á.¤. 2548
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.xet.nxet.tool;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LogFormatter extends Formatter {
    
    // This method is called for every log records
    public String format(LogRecord rec) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append(formatMessage(rec));
        sb.append('\n');
        return sb.toString();
    }

    // This method is called just after the handler using this
    // formatter is created
    public String getHead(Handler h) {
        return "";
    }

    // This method is called just after the handler using this
    // formatter is closed
    public String getTail(Handler h) {
        return "";
    }
}
