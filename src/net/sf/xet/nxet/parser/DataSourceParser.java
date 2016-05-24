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

package net.sf.xet.nxet.parser;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.data.DataSource;
import net.sf.xet.nxet.data.DataSources;
import net.sf.xet.nxet.data.URINotSupportedException;
import net.sf.xet.nxet.tool.Tool;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataSourceParser extends XETParser {
    
    private static final String TAG_DATASOURCES = "DataSources";
    private DataSources dataSources = null;
    
    public DataSourceParser(String xmlFile, String className, Configuration config, Handler logHandler) {
        super(xmlFile, className, config, logHandler);
        this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_DATA_SOURCE_PARSER));
        this.dataSources = new DataSources(this.config, this.logHandler);
    }
    
    public static DataSourceParser createParser(String xmlFile, Configuration config, Handler logHandler) {
        // Use an instance of ourselves as the SAX event handler
        DataSourceParser dataSourceParser = new DataSourceParser(xmlFile, DataSourceParser.class.getName(), 
                                                                 config, logHandler);
        dataSourceParser.assignContentHandler(dataSourceParser);
        return dataSourceParser;
    }
    
    /**
     * Get the parsed data sources
     * 
     * @return The parsed data sources
     */
    public DataSources getDataSources() {
        return this.dataSources;
    }
    
    public void startDocument() {
        // The parent node is now the document element
        dataSources = new DataSources(this.config, this.logHandler);
        seenRootElement = false;
    }

    public void startElement(String namespaceURI, String lName, 
			 				 String qName, Attributes attrs) throws SAXException {
        
        Node newNode = null;
        if (!seenRootElement) {
            // This is probably the first "xet:XET" document element.
            seenRootElement = true;
        } else if ((lName.equals(DataSourceParser.TAG_DATASOURCES) && 
                	namespaceURI.equals(XETParser.XET_URI))) {
        } else {
            if (attrs == null) {
                logger.log(Level.SEVERE, "DataSource does not contain any attribute.");
            }
            int len = attrs.getLength();
            if (len > 0) {
		        String name = null;
                String uri = null;
                boolean preload = true;
                boolean reload = false;
                for (int i = 0; i < len; i++) {
		            if (attrs.getLocalName(i).equals("name") && 
                        attrs.getURI(i).equals(XETParser.XET_URI)) {
		            
		                name = attrs.getValue(i);
		                
                    } else if (attrs.getLocalName(i).equals("uri") && 
                               attrs.getURI(i).equals(XETParser.XET_URI)) {
                        
                        uri = attrs.getValue(i);
                        
                    } else if (attrs.getLocalName(i).equals("preload") && 
                               attrs.getURI(i).equals(XETParser.XET_URI)) {
                        
                        // Be careful. Even though the preload is assigned
                        // true by default, if a user specify something else
                        // such as "Yes" or "t". The static function parseBoolean
                        // will automatically return "false" value and the
                        // data will not be load. Therefore the best thing
                        // is to not use the preload attribute or properly
                        // assign a value either "true" or "false".
                        preload = Boolean.parseBoolean(attrs.getValue(i));
                        
                    } else if (attrs.getLocalName(i).equals("reload") &&
                               attrs.getURI(i).equals(XETParser.XET_URI)) {
                        
                        // Similar to the preload description above, reload
                        // can only be triggered if and only if the value
                        // is "True", TruE", "true", "tRue" ...
                        // "parseBoolean()" function processes the true
                        // string by ignoring cases.
                        reload = Boolean.parseBoolean(attrs.getValue(i));
                    }
                }
                
                try {
                    dataSources.createDataSource(name, uri, 
												 preload, reload, 
												 new File(this.xmlFile).getParentFile().getAbsolutePath());
                } catch (URINotSupportedException e) {
                    logger.log(Level.SEVERE, e.getMessage());    
                }
	        }
	    }
    }
    
    public void characters(char buf[], int offset, int len) throws SAXException {}
    
    public void endElement(String namespaceURI, String lName, String qName) throws SAXException {}
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uriPrefixMapper.put(uri, prefix);
    }
    
    public void skippedEntity(String name) throws SAXException {}
    
    public void endPrefixMapping(String prefix) throws SAXException {}
    
    public void endDocument() throws SAXException {
        DataSource[] dataSourceArray = dataSources.dataSourcesArray();
        StringBuffer sb = new StringBuffer();
        sb.append(Tool.LINE + "\n");
        sb.append("Registered data source(s) : " + this.xmlFile + "\n");
        sb.append(Tool.LINE);
        for (int i = 0; i < dataSourceArray.length; i++) {
            sb.append("\nds://" + dataSourceArray[i].getName() + ' ');
            sb.append("uri=\"" + dataSourceArray[i].getUri() + "\"");
        }
        logger.log(logLevel, sb.toString());
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    
    public void setDocumentLocator(Locator locator) {}
    
    public void processingInstruction(String target, String data) throws SAXException {}
   
}
