/*
 *  XML Equivalent Transformation Software Development Kit (XET SDK)
 *  Copyright (C) 2000-2003, Knowledge Representation Laboratory,
 *  Asian Institute of Technology
 *
 *  $Id: PropertyConfig.java,v 1.1 2004/12/28 15:17:58 paramai Exp $
 *  This file is part of XET SDK.
 *
 *  XET SDK is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  XET SDK is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.sf.xet.nxet.config;

import java.util.Properties;
import java.util.Enumeration;

/**
 * <p>Title: Configuration implementation based on Properties</p>
 * @version $Revision: 1.1 $
 */

public class PropertyConfig extends Properties implements Configuration {

    // Internal universal id variable for serializable interface
    private static final long serialVersionUID = 29487L;
    
    public String get(String key) {
        String s = getProperty(key);
        if (s == null) {
            System.err.println("Warning: Property " + key + " not found.");
        }
        return s;
        // return getProperty(key);
    }

    public String get(String key, String def) {
        return getProperty(key, def);
    }

    public void set(String key, String value) {
        setProperty(key, value);
    }
    
    public Enumeration configKeys() {
        return this.propertyNames();
    }

}
