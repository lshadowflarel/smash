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

import java.io.FileReader;
import java.io.IOException;
import java.lang.StringBuffer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.config.Configuration;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ContentHandler;

/**
 * @author Paramai Supadulchai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class XETParser implements ContentHandler {

    protected static final boolean DEFAULT_CANONICAL = false;
    protected static final String TAG_BODY		= "Body";
    protected static final String TAG_HEAD		= "Head";
    
    protected static final String TAG_XET 		= "XET";
	protected static final String TAG_EVAR		= "E-variable";
    
    protected static final String VAR_E			= "Evar";
    protected static final String VAR_E1		= "E1var";
    protected static final String VAR_I			= "Ivar";
    
    protected static final String VAR_N			= "Nvar";
    protected static final String VAR_P			= "Pvar";
    protected static final String VAR_S			= "Svar";
	
    
    // Namespaces "XET"
    protected static final String XET_URI = "http://xet.sf.net";
    
    protected static final String XML_PI		= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    
    protected static Node createNode(String nodeName, String namespacePrefix, 
            						 String namespaceUri, Node parentNode, 
            						 int nodeType) {
        Node newNode = new Node();
        newNode.setParentNode(parentNode);
        newNode.setNodeName(nodeName);
        newNode.setNamespace(namespacePrefix);
        newNode.setUri(namespaceUri);
        newNode.setNodeType(nodeType);
        return newNode;
    }
    // Determine to print the "canonical output"
    protected boolean canonical = false;
    // Configuration
    protected Configuration config = null;
    // Indent
    protected int indent = 0;
    // Logger
    protected Logger logger = null;
    // The log handler
    protected Handler logHandler = null;
    // The log-level
    protected Level logLevel = null;
    // Checks whether or not the document element has been visited
    protected boolean seenRootElement = false;
    // Prefix-URL mapper
    protected Properties uriPrefixMapper = new Properties();
    // XML Parser
    protected String xmlFile = null;
    // Parser
    protected XMLReader xmlReader = null;
	
    public XETParser(String xmlFile, String className, Configuration config, Handler logHandler) {
        this.xmlFile = xmlFile;
        this.logger = Logger.getLogger(className);
        this.logHandler = logHandler;
        if (this.logger.getHandlers().length == 0) {
            this.logger.addHandler(logHandler);
        }
        this.logger.setUseParentHandlers(false);
        this.config = config;
        this.canonical = DEFAULT_CANONICAL;
        try {
            this.xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (SAXException e) {
            System.err.println("Namespace feature is not available on this XML parser.");
        }
    }
    
    /**
     * Sets the content handler to the parser
     */
    public void assignContentHandler(ContentHandler contentHandler) {
        this.xmlReader.setContentHandler(contentHandler);
    }
    
    /**
     * Return the value of an attribute with the name given in attrName
     * 
     * @param attrs
     * @param attrName
     * @return
     */
    protected String getAttribute(Attributes attrs, String attrName) {
        if (attrs != null) {
	        // attrs = sortAttributes(attrs);
	        int len = attrs.getLength();
	        if (len > 0) {
		        for (int i = 0; i < len; i++) {
		            if (attrs.getQName(i).equals(attrName)) {
		                return attrs.getValue(i);
		            }
		        }
	        }
	    }
        return null;
    }
    
    /**
     * @return Returns the xmlFile.
     */
    public String getXmlFile() {
        return xmlFile;
    }
	
	/**
	 * Returns the uri-prefix mapper
	 * 
	 * @return Returns the uri-prefix mapper
	 */
	public Properties getUriPrefixMapper() {
		return this.uriPrefixMapper;
	}
    
    /**
     * Generate two spaces per 1 indent number.
     * 
     * @return Returns the generated space
     */
    protected String genIndent(int indent) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent-1; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
    
    protected String genAttributeText(Attributes attrs) {
        StringBuffer sb = new StringBuffer();
        if (attrs != null) {
	        int len = attrs.getLength();
	        if (len > 0) {
		        for (int i = 0; i < len; i++) {
		            sb.append(" " + attrs.getQName(i) + "=\"" + 
		                      normalize(attrs.getValue(i)) + '"');
		        }
	        }
	    }
        return(sb.toString());
    }
	
	/** 
     * Normalizes and return the given character.
     * @param c		a character to be converted
     */
    protected String normalize(char c) {
        switch (c) {
            case '<': {
                return "&lt;";
            }
            case '>': {
                return "&gt;";
            }
            case '&': {
                return "&amp;";
            }
            case '"': {
                return "&quot;";
            }
            case '\r':
            case '\n': {
                if (canonical) {
                    return "&#" + Integer.toString(c) + ';';
                }
                // else, default print char
            }
            default: {
                //return (new StringBuffer(c)).toString();
                return new Character(c).toString();
            }
        }

    }
    
    /**
     * Normalizes and return the given string
     * @param s		text to be converted.
     */
    protected String normalize(String s) {
        StringBuffer sb = new StringBuffer();
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            String result = normalize(c);
            sb.append(result);
        }
        return sb.toString();
    }
    
    public void parse() {
        try {
            xmlReader.parse(new InputSource(new FileReader(this.xmlFile)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "The parser cannot find " + xmlFile);
            e.printStackTrace();
        } catch (SAXException e) {
            logger.log(Level.SEVERE, "Invalid XML document " + xmlFile);
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract attributes from XMLAttributes and add them to a parent node
     * 
     * @param attrs the attribute list in XMLAttributes object
     * @param parentNode the node to add all attributes too
     */
    protected void parseAttribute(Attributes attrs, Node node) {
        if (attrs != null) {
	        int len = attrs.getLength();
	        if (len > 0) {
		        for (int i = 0; i < len; i++) {
		            Node attribute = new Node();
		            attribute.setParentNode(node);
		            attribute.setNodeName(attrs.getLocalName(i));
		            attribute.setNamespace(uriPrefixMapper.getProperty(attrs.getURI(i)));
		            attribute.setNodeValue(attrs.getValue(i));
		            attribute.setUri(attrs.getURI(i));
		            attribute.setAttributeNode(true);
		            if (attribute.getNodeName().startsWith(XETParser.VAR_P)) {
		                attribute.setNodeType(Node.NT_PVAR);
		            } else if (attribute.getNodeValue().startsWith(XETParser.VAR_S)) {
		                attribute.setNodeType(Node.NT_SVAR);
		            } else {
		                attribute.setNodeType(Node.NT_STRING);
		            }
		            node.addAttribute(attribute);
		        }
	        }
	    }    
    }
    
    protected String prefix(String uri) {
        return uriPrefixMapper.getProperty(uri);
    }
    
    /**
     * Sets whether output is canonical.
     * 
     * @param canonical The canonical value to set
     */
    protected void setCanonical(boolean canonical) {
        this.canonical = canonical;
    }
    
    /**
     * Sets the XML data file
     * 
     * @param xmlFile The xmlFile to set.
     */
    public void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }
    
}
