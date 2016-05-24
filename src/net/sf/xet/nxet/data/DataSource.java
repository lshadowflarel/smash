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

import net.sf.xet.nxet.core.Node;

/**
 * A Data Source is a wrapper class that contains a fact "Node"
 * loaded from a source of data, which can be a file, an ftp site,
 * a http website, a web service etc. A data source can be 
 * defined by a data source configuration file. A data source can
 * also be referred from a "FactQuery" built-in. In the former
 * case, a data source will be reloaded if a pre-load parameter 
 * is set. In the latter case, the pre-load parameter will not be
 * considered and the data source will be automatically reloaded.<br>
 * <br>
 * A data source will be reloaded whenever its re-load flag is set.  
 * 
 * @author Paramai Supadulchai
 * @since NxET 0.2
 */
public class DataSource {

    /**
     * DataSource will be referred to the existing data source.
     */
    public static final String URI_DS		= "ds://";
    /**
     * DataSource will be loaded from a data source file.
     */
    public static final String URI_FILE 	= "file://";
    /**
     * DataSource will be loaded from an FTP site.
     */
    public static final String URI_FTP 		= "ftp://";
    /**
     * DataSource will be loaded from an HTTP website.
     */
    public static final String URI_HTTP 	= "http://";
    /**
     * DataSource will be loaded from a web service.
     */
    public static final String URI_WS		= "ws://";
    
    private Node data = null;
    private long lastReload;
    private String name = null;
    private boolean preload = false;
    private boolean reload = false;
    private String uri = null;
    
    /**
     * Returns the fact data of this data source.
     * 
     * @return Returns the data.
     */
    public Node getData() {
        return data;
    }
    
    /**
     * Returns the timestamp when the data is
     * last reloaded.
     * 
     * @return Returns the lastReload.
     */
    public long getLastReload() {
        return lastReload;
    }
    
    /**
     * Returns the name of the data source.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the URI of the data source.
     * 
     * @return Returns the uri.
     */
    public String getUri() {
        return uri;
    }
    
    /**
     * Returns the preload parameter.
     * 
     * @return Returns the preload parameter.
     */
    public boolean isPreload() {
        return preload;
    }
    
    /**
     * Returns the reload parameter.
     * 
     * @return Returns the reload parameter.
     */
    public boolean isReload() {
        return reload;
    }
    
    /**
     * Assign the fact data Node to this data source.
     * 
     * @param data The fact data node to set.
     */
    public void setData(Node data) {
        this.data = data;
    }
    
    /**
     * Assign the latest timestamp when the data Node
     * is last reloaded.
     * 
     * @param lastReload The lastReload to set.
     */
    public void setLastReload(long lastReload) {
        this.lastReload = lastReload;
    }
    
    /**
     * Assign the name of the data source
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Assign the preload parameter of the data source.
     * 
     * @param preload The preload to set.
     */
    public void setPreload(boolean preload) {
        this.preload = preload;
    }
    
    /**
     * Assign the reload parameter of the data source.
     * 
     * @param reload The reload to set.
     */
    public void setReload(boolean reload) {
        this.reload = reload;
    }
    
    /**
     * Assign the uri of the data source.
     * 
     * @param uri The uri to set.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
