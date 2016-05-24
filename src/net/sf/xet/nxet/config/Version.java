/*
 *  XML Equivalent Transformation Software Development Kit (XET SDK)
 *  Copyright (C) 2000-2003, Knowledge Representation Laboratory,
 *  Asian Institute of Technology
 *
 *  $Id: Version.java,v 1.2 2005/01/16 10:23:19 paramai Exp $
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

import net.sf.xet.nxet.tool.Tool;

/**
 * <p>Title: Version Information</p>
 * <p>Description: XET Version Information</p>
 * @version $Revision: 1.2 $
 */

public class Version {
    
    private static final String NEW_LINE = "\n";
    
    public static final String PRODUCT_NAME = "NxET SDK";
    public static final String PRODUCT_URL = "http://xet.sf.net/";
    public static final String PRODUCT_URL1 = "http://tapas.item.ntnu.no/NxET";
    public static final String PRODUCT_URL2 = "http://kr.cs.ait.ac.th/XET";
    
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 4;
    public static final int VERSION_LEVEL = 0;
    public static final String VERSION_TAG = "(Alaska)";

    public static final String BUILD_NO = "2006.08.11.01";
    public static final String BUILD_DATE = "11 August 2006";
    public static final String BUILD_NAME = "";

    public static final String COPYRIGHT_YEAR1 = "2004-2007";
    public static final String COPYRIGHT_YEAR2 = "2002-2005";

    public static final String VERSION_STRING = Integer.toString(VERSION_MAJOR) + "." +
                                            	Integer.toString(VERSION_MINOR) + "." +
                                            	Integer.toString(VERSION_LEVEL) + " " +
                                            	VERSION_TAG;

    public static String getVersionInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append(Tool.LINE + NEW_LINE);
        sb.append(PRODUCT_NAME + " version " + VERSION_STRING + " (" + BUILD_NO + ")" + NEW_LINE);
        sb.append(Tool.LINE);
        return sb.toString();
    }
    
    public static String getCopyRightInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Copyright(C) " + COPYRIGHT_YEAR1 + ", Telematics Architecture for Play-based Adaptable System" + NEW_LINE);
        sb.append("Telematics, Norwegian University of Technology and Science (NTNU)" + NEW_LINE);
        sb.append(PRODUCT_URL1 + NEW_LINE + NEW_LINE);
        sb.append("Copyright(C) " + COPYRIGHT_YEAR2 + ", Knowledge Representation Laboratory" + NEW_LINE);
        sb.append("CSIM, Asian Institute of Technology (AIT)" + NEW_LINE);
        sb.append(PRODUCT_URL2 + NEW_LINE);
        return sb.toString();
    }
}
