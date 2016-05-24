/*
 *  XML Equivalent Transformation Software Development Kit (XET SDK)
 *  Copyright (C) 2000-2003, Knowledge Representation Laboratory,
 *  Asian Institute of Technology
 *
 *  $Id: ConfigKey.java,v 1.2 2005/01/16 10:23:19 paramai Exp $
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
 * <p>Title: Configuration Keys </p>
 * <p>Description: This helps reduce typing mistake</p>
 * @version $Revision: 1.2 $
 */

public class ConfigKey {
    
    /* Debugger */
    public static final String DEBUGGER_STEP 				= "XET.Debugger.Step";
    public static final String DEBUGGER_STEPTYPE 			= "XET.Debugger.StepType";
    
    /* Logger */
    public static final String LEVEL_EXECUTOR				= "XET.Logger.LogLevel.Executor";
    public static final String LEVEL_MATCHER				= "XET.Logger.LogLevel.Matcher";
    public static final String LEVEL_SET_OF					= "XET.Logger.LogLevel.Set-of";
    public static final String LEVEL_FACT_PARSER			= "XET.Logger.LogLevel.FactParser";
    public static final String LEVEL_RULE_PARSER			= "XET.Logger.LogLevel.RuleParser";
    public static final String LEVEL_QUERY_PARSER			= "XET.Logger.LogLevel.QueryParser";
    public static final String LEVEL_DATA_SOURCE_PARSER		= "XET.Logger.LogLevel.DataSourceParser";
    public static final String LEVEL_CONFIG_PARSER			= "XET.Logger.LogLevel.ConfigParser";
}
