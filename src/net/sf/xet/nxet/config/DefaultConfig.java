/*
 *  XML Equivalent Transformation Software Development Kit (XET SDK)
 *  Copyright (C) 2000-2003, Knowledge Representation Laboratory,
 *  Asian Institute of Technology
 *
 *  $Id: DefaultConfig.java,v 1.2 2005/01/16 10:23:19 paramai Exp $
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

/**
 * <p>Title: Default Configuration</p>
 * <p>Description: </p>
 * @version $Revision: 1.2 $
 */

public class DefaultConfig extends PropertyConfig {

    // Internal universal id variable for serializable interface
    private static final long serialVersionUID = 29486L;
    
    public static final String TRUE							= "True";
    public static final String FALSE						= "False";
    
    public DefaultConfig() {
        setProperty(ConfigKey.LEVEL_EXECUTOR, "Info");
        setProperty(ConfigKey.LEVEL_MATCHER, "Finest");
        setProperty(ConfigKey.LEVEL_SET_OF, "Finest");
        setProperty(ConfigKey.LEVEL_CONFIG_PARSER, "Finest");
        setProperty(ConfigKey.LEVEL_RULE_PARSER, "Finest");
        setProperty(ConfigKey.LEVEL_FACT_PARSER, "Finest");
        setProperty(ConfigKey.LEVEL_DATA_SOURCE_PARSER, "Finest");
        setProperty(ConfigKey.LEVEL_QUERY_PARSER, "Finest");
    }

}