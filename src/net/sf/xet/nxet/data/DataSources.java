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
 */
package net.sf.xet.nxet.data;

import java.io.File;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Handler;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.parser.FactParser;
import net.sf.xet.nxet.config.Configuration;

/**
 * DataSources is a pool that contains all data sources in the system.
 * The pool will be assigned to each world and will be referred later
 * by the built-in "FactQuery". The DataSources pool is reponsible to
 * create a new data source from a given "URI". The FactQuery built-in
 * will refer to the data sources pool by a URI or a name. 
 * 
 * @author Paramai Supadulchai
 * @since NxET 0.2
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataSources {

    private HashMap<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private HashMap<String, String> uriNameMap = new HashMap<String, String>();
    private Configuration config;
    private Handler logHandler;
	private String originalPath = null;
    
    public DataSources(Configuration config, Handler logHandler) {
        this.config = config;
        this.logHandler = logHandler;
    }
	
	public DataSources(Configuration config, Handler logHandler, String originalPath) {
        this.config = config;
        this.logHandler = logHandler;
		this.originalPath = originalPath;
    }
    
    /**
     * Create a new data source by arguments supplied from the data source
     * config file.
     * 
     * @param name The name of the data source to be created
     * @param uri The uri of the data source
     * @param preload The data source will be preloaded if true
     * @param reload The data source will be reloaded if true
     * @param originalPath The path referred to the original datasource file
     * 
     * @return a new data source created
     * 
     * @throws URINotSupportedException if the URI is not suppored.
     */
    public DataSource createDataSource(String name, String uri, boolean preload, 
                                       boolean reload, String originalPath) throws URINotSupportedException {
        
		// Create a new data source object and set its attributes
        DataSource dataSource = new DataSource();
        dataSource.setName(name);
        dataSource.setUri(uri);
        dataSource.setPreload(preload);
        dataSource.setReload(reload);
        dataSource.setLastReload(System.currentTimeMillis());
		this.originalPath = originalPath;
        if (preload) {
            if (uri.startsWith(DataSource.URI_FILE)) {
				loadFileDataSource(dataSource, getFileNameFromFileUri(uri));
	            // Put the created data source to the data sources
	            dataSources.put(uri, dataSource);
	            // Put also the name & data source association
	            uriNameMap.put(name, uri);
	        } else {
	            throw new URINotSupportedException("URI " + uri + " is currently not suppored");
	        }
	    }
        return dataSource;
    }
    
    /**
     * Add a new data source to the data source pool
     * (dataSources hashmap)
     * 
     * @param dataSourceName A name referred to the added data source
     * @param dataSource A new data source
     */
    public void addDataSource(String dataSourceName, DataSource dataSource) {
        dataSources.put(dataSourceName, dataSource);
    }
    
    /**
     * Get a data source by a data source name
     * 
     * @param dataSourceName The name referred to the data source
     * @return Returns the data source, return null if data source cannot be found
     * @throws URINotSupportedException If the required URI is not currented implemented
     */
    public DataSource dataSourceByName(String dataSourceName) throws URINotSupportedException {
        String uriMap = uriNameMap.get(dataSourceName);
        return dataSourceByUri(uriMap);
    }
    
    /**
     * Get a data source by specifying uri
     * 
     * @param uri The uri referred to the data source
     * @return Returns the data source, return null if data source cannot be found
     * @throws URINotSupportedException If the required URI is not currently implemented
     */
    public DataSource dataSourceByUri(String uri) throws URINotSupportedException {
        DataSource dataSource = null;
        if (uri.startsWith(DataSource.URI_DS)) {
            dataSource = dataSourceByName(getDataSourceNameFromDSUri(uri));
    	} else if (uri.startsWith(DataSource.URI_FILE)) {
            dataSource = dataSources.get(uri); 
        } else {
            throw new URINotSupportedException("URI " + uri + " is currently not suppored");
        }
        return dataSource;
    }
    
    public DataSource[] dataSourcesArray() {
        DataSource[] dataSources = new DataSource[this.dataSources.size()];
        Iterator it = this.dataSources.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            dataSources[i] = this.dataSources.get(it.next());
            i++;
        }
        return dataSources;
    }
    
    /**
     * Returns the number of data sources
     * 
     * @return Returns the number of data sources
     */
    public int numberOfDataSources() {
        return this.dataSources.size();
    }
    
    /**
     * Reload a specific data source specified by a name
     * 
     * @param dataSourceName The name referred to the data source
     * to be reloaded
     */
    public void reloadDataSourceByName(String dataSourceName) throws URINotSupportedException {
        // Get a URI associated with the given name
        String uriMap = uriNameMap.get(dataSourceName);
        // Reload the data source specified by the uri
        reloadDataSourceByUri(uriMap);
    }
    
    /**
     * Reload a specific data source specified by a uri
     * 
     * @param dataSourceUri The url referred to the data source
     * to be reloaded
     */
    public void reloadDataSourceByUri(String dataSourceUri) throws URINotSupportedException {
        // Get a data source object by a given uri
        DataSource dataSource = dataSourceByUri(dataSourceUri);
        if (dataSource.isReload()) {
            if (dataSource.getUri().startsWith(DataSource.URI_FILE)) {
	            loadFileDataSource(dataSource, getFileNameFromFileUri(dataSource.getUri()));
	            dataSource.setLastReload(System.currentTimeMillis());
	        } else {
	            throw new URINotSupportedException("URI " + dataSource.getUri() + " is currently not suppored");
	        }
        }
    }
    
    /**
     * Reload a file data source
     * 
     * @param dataSource The data source to be reloaded
     * @param Returns the file name (extracted from the data source uri)
     * @return
     */
    private void loadFileDataSource(DataSource dataSource, String fileName) {
        // Create a parser for processing the required fact file.
		FactParser factParser = FactParser.createParser(fileName, config, logHandler);
		// Parse the fact file
		factParser.parse();
		// Obtain a fact root node, which contains all fact atoms as its child nodes.
		Node fact = factParser.getFact();
		// Assign the fact to the data source
		dataSource.setData(fact);
	}
    
    /**
     * Check if a data source has already been created.
     * 
     * @param uri The uri
     * @return Returns true if the uri exists
     */
    public boolean isDataSourceExist(String uri) {
        if (uri.startsWith(DataSource.URI_DS)) {
            return isDataSourceNameExist(getDataSourceNameFromDSUri(uri));
        }
        return this.dataSources.containsKey(uri);
    }
    
    /**
     * Check if a data source name has been registeredf
     * 
     * @param name The name of the data source
     * @return Returns true if the name exists
     */
    public boolean isDataSourceNameExist(String name) {
        return this.uriNameMap.containsKey(name);
    }
    
    /**
     * Extract just the name from ds://name
     * For example the name of ds://name1 will be name1 
     * 
     * @param uri The DS uri
     * @return Returns the name within the uri
     */
    private String getDataSourceNameFromDSUri(String uri) {
        return uri.substring(DataSource.URI_DS.length());
    }
    
    /**
     * Extract just the file name from file://filepath
     * For example the file name of ds://d:/file1.xml 
     * will be d:/file1.xml 
     * 
     * @param uri The DS uri
     * @return Returns the name within the uri
     */
    private String getFileNameFromFileUri(String uri) {
        String fileName = uri.substring(DataSource.URI_FILE.length());
		File file = new File(fileName);
		if (!file.isAbsolute() && this.originalPath != null) {
			fileName = this.originalPath + File.separator + file.getPath();
		}
		return fileName;
    }
	
	/**
	 * Returns the original path
	 * 
	 * @return Returns the original path
	 */
	public String getOriginalPath() {
		return this.originalPath;
	}
    
	/**
	 * Set the original path
	 * 
	 * @param originalPath The original path to set
	 */
	public void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}
	
}
