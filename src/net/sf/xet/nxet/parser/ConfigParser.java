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

import java.util.logging.Handler;
import java.util.Enumeration;
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
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
public class ConfigParser extends XETParser {

    private boolean printOutput = false;
    private Node parentNode = null;
    
    public ConfigParser(String xmlFile, String className, 
                        Configuration config, Handler logHandler) {
        super(xmlFile, className, config, logHandler);
        this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_CONFIG_PARSER));
    }
    
    public static ConfigParser createParser(String xmlFile, Configuration config, Handler logHandler) {
        // Use an instance of ourselves as the SAX event handler
        ConfigParser configParser = new ConfigParser(xmlFile, ConfigParser.class.getName(), 
                                                     config, logHandler);
        configParser.assignContentHandler(configParser);
        return configParser;
    }
    
    public void startDocument() {}

    public void startElement(String namespaceURI, String lName, 
			 				 String qName, Attributes attrs) throws SAXException {
        StringBuffer logMessage = new StringBuffer();
        String nodeName = (parentNode != null) ? parentNode.getNodeName() + "." + lName : lName;
            
        Node newNode = XETParser.createNode(nodeName, prefix(namespaceURI), 
                                            namespaceURI, parentNode, Node.NT_META);
	    parentNode = newNode;
	}
    
    public void characters(char buf[], int offset, int len) throws SAXException {
        String text = new String(buf, offset, len).trim();
        if (!(text.length() ==  0)) {
            config.set(parentNode.getNodeName(), text);
        }
    }
    
    public void endElement(String namespaceURI, String lName, String qName) throws SAXException {
        if ((parentNode != null) && (parentNode.getParentNode() != null)) {
            parentNode = parentNode.getParentNode();
        }
    }
   
    public Configuration getConfiguration() {
        return this.config;
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uriPrefixMapper.put(uri, prefix);
    }
    
    public void skippedEntity(String name) throws SAXException {}
    
    public void endPrefixMapping(String prefix) throws SAXException {}
    
    public void endDocument() throws SAXException {
        StringBuffer sb = new StringBuffer();
        sb.append(Tool.LINE + "\n");
        sb.append("Configuration : " + this.xmlFile + "\n");
        sb.append(Tool.LINE);
        Enumeration configKeys = config.configKeys();
        while (configKeys.hasMoreElements()) {
            String configKey = (String)configKeys.nextElement();
            sb.append("\nKey [" + configKey + "]" + " Value [" + config.get(configKey) + "]");
        }
        logger.log(logLevel, sb.toString());
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    
    public void setDocumentLocator(Locator locator) {}
    
    public void processingInstruction(String target, String data) throws SAXException {}
    
}
