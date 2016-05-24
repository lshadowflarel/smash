/*
 *  XML Equivalent Transformation Software Development Kit (XET SDK)
 *  Copyright (C) 2000-2003, Knowledge Representation Laboratory,
 *  Asian Institute of Technology
 *
 *  $Id: Configuration.java,v 1.1 2004/12/28 15:17:58 paramai Exp $
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

import java.util.Enumeration;

/**
 * <p>Title: Configuration Inreface</p>
 * @version $Revision: 1.1 $
 */

public interface Configuration {
    
    public String get(String key);
    public String get(String key, String def);
    public void set(String key, String value);
    public Enumeration configKeys();
  
}
