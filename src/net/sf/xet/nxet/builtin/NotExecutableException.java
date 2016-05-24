/*
 * Native XML Equivalent Transformation Software Development Kit (NxET)
 * Copyright (C) 2004-2005, Telematics Architecture for Play-based Adaptable System,
 * (TAPAS), Department of Telematics, 
 * Norwegian University of Science and Technology (NTNU),
 * O.S.Bragstads Plass 2, N7491, Trondheim, Norway
 *
 * This file is a part of NxET.
 *
 * NxET is a free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * NxET is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 */
package net.sf.xet.nxet.builtin;

/**
 * An exception class thrown when the built-in manager cannot
 * execute a built-in function. A reason will also be given.<br>
 * <br>
 * Intuitively, the reason is relevant to the three methods of
 * the built-in abstract class whether a built-in has the same
 * structure, still contains uninstantiated variable(s) or the
 * structure of the built-in in an XML format is totally strange 
 * and not conform or cannot be converted to the built-in abstract 
 * class.
 * 
 * @author Paramai Supadulchai
 *
 */
public class NotExecutableException extends Exception {

    // Internal universal id variable for serializable interface
    private static final long serialVersionUID = 29485L;
    
    /**
     * No reason is given (default setting)
     */ 
    public static final int UNKNOWN_REASON = 0;
    /**
     * The reason why a built-in is not executable is because 
     * the builtin is not in the builtin list.
     */
    public static final int BUILTIN_NOTFOUND = 1;
    /**
     * The reason why a built-in is not executable is because
     * the builtin has different structure.
     */
    public static final int DIFFERENT_STRUCTURE = 2;
    /**
     * The reason why a built-in is not executable is because
     * the builtin cannot be executed because it still contains 
     * variable(s).
     */
    public static final int CONTAINS_VARIABLES = 3;
    /**
     * The reason why a built-in is not executable is because
     * the built-in seems to be invalid or are not conformed to 
     * the Builtin class. (This is normally thrown by Java
     * reflection-oriented exceptions)
     */
    public static final int INVALID_BUILTIN = 4;
    /**
     * The built-in is used in a wrong place.<br>
     * A D-built-in can only be used as a D-atom.<br>
     * An N-built-in can only be used as an N-atom.<br>
     * Where some built-in can be used as either D- or N-
     * atom.
     */
    public static final int WRONG_PLACE = 5;
	/**
     * The built-in contains invalid parameters.<br>
     */
	public static final int INVALID_PARAMETERS = 6;
    
    // The variable holding the not-executable reason. 
    // The default is zero
    private int reason = 0;
    
    /**
     * Constructor to create an exception object with a reason
     * number
     * 
     * @param reason The reason of this exception
     */
    public NotExecutableException(int reason) {
        super();
        this.reason = reason;
    }

    /**
     * Constructor to create an exception object with a reason 
     * number and a reason string
     * 
     * @param reason The reason number of this exception
     * @param arg0 A more readable message.
     */
    public NotExecutableException(int reason, String arg0) {
        super(arg0);
        this.reason = reason;
    }

    /**
     * Constructor to create an exception object with a reason
     * in both a reason number, a message "string" or a 
     * "throwable" cause object
     * 
     * @param reason The reason number of this exception
     * @param arg0 A more readable reason message
     * @param arg1 A reason in a throwable object
     */
    public NotExecutableException(int reason, String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.reason = reason;
    }

    /**
     * Constructor to create an exception object with a reason
     * in both a "throwable" cause object and a reason number
     * 
     * @param reason The reason number of this exception
     * @param arg0 The reason in a throwable object
     */
    public NotExecutableException(int reason, Throwable arg0) {
        super(arg0);
        this.reason = reason;
    }
    
    /**
     * Get the reason number
     * 
     * @return Returns the reason number.
     */
    public int getReason() {
        return reason;
    }
}
