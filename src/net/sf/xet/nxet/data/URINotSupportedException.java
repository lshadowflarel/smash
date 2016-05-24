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
package net.sf.xet.nxet.data;

/**
 * @author Paramai Supadulchai
 * @since NxET 0.2
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class URINotSupportedException extends Exception {

    // Internal universal id variable for serializable interface
    private static final long serialVersionUID = 29488L;
    
    /**
     * Constructor to create an exception object
     * 
     */
    public URINotSupportedException() {
        super();
    }

    /**
     * Constructor to create an exception object
     * 
     * @param arg0 reason
     */
    public URINotSupportedException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor to create an exception object with a reason
     * in a "throwable" cause object
     * 
     * @param arg0 reason in string
     * @param arg1 reason in a throwable object
     */
    public URINotSupportedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor to create an exception object with a reason
     * in a "throwable" cause object
     * 
     * @param arg0 reason in a throwable object
     */
    public URINotSupportedException(Throwable arg0) {
        super(arg0);
    }
    
}
