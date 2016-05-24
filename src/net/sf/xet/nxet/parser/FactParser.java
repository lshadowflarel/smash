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

import java.util.Vector;
import java.util.logging.Handler;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.tool.Tool;
import org.xml.sax.*;
import org.xml.sax.SAXException;

/**
 * The fact parser parses fact files. *This fact file
 * does not include any ability to parse variables.*
 * 
 * @author Paramai Supadulchai
 */
public class FactParser extends XETParser {
    
    private static final String TAG_FACT 				= "Fact";
	private static final String TAG_NAME				= "name";
    
    private Node fact = null;
    private Node currentNode = null;
    private Node parentNode = null;
    private Node previousSibling = null;
    private Vector childNodes = null;   
    
    public FactParser(String xmlFile, String className, Configuration config, Handler logHandler) {
        super(xmlFile, className, config, logHandler);
        this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_FACT_PARSER));
        this.fact = new Node();
    }
    
    public static FactParser createParser(String xmlFile, Configuration config, Handler logHandler) {
        // Use an instance of ourselves as the SAX event handler
        FactParser factParser = new FactParser(xmlFile, FactParser.class.getName(), 
                							   config, logHandler);
        factParser.assignContentHandler(factParser);
        return factParser;
    }
    
    public void startDocument() {
        // The parent node is now the document element
        parentNode = fact;
        fact.setParentNode(null);
        seenRootElement = false;
    }

    public void startElement(String namespaceURI, String lName, 
            				 String qName, Attributes attrs) throws SAXException {
        
        String namespacePrefix = prefix(namespaceURI);
        
        // Check the first document element
        if (!seenRootElement) {
            seenRootElement = true;
        } else if ((lName.equals(FactParser.TAG_FACT) && 
                    namespaceURI.equals(XETParser.XET_URI))) {
            fact.setNodeName(lName);
            fact.setNamespace(namespacePrefix);
            fact.setUri(namespaceURI);
            // For builtin-function
            fact.setNodeType(Node.NT_DEFAULT);
            parentNode = fact;
            parseAttribute(attrs, fact);
        // Allow E-variable declaration in an XML element
		// Example: <xet:E-variable xet:name="Evar_name"/>
		// Added in version 3.0
	    } else if (lName.equals(XETParser.TAG_EVAR)  && 
				   namespaceURI.equals(XETParser.XET_URI)) {
	            
			Node newNode = createNode("", prefix(namespaceURI), namespaceURI, parentNode, Node.NT_EVAR);
			parseAttribute(attrs, newNode);
			if (newNode.hasAttributeByLocalName(FactParser.TAG_NAME)) {
				if (newNode.attributeByLocalName(FactParser.TAG_NAME).getNodeValue().startsWith(XETParser.VAR_E)) {
					newNode.setNodeName(newNode.attributeByLocalName(FactParser.TAG_NAME).getNodeValue());
				} else {
					logger.severe("[QueryParser] The name of an E-variable must begin with 'Evar'.");
					System.exit(0);	
				}
			} else {
				logger.severe("[QueryParser] An E-variable does not have a proper name assigned.");
				System.exit(0);
			}
			parentNode.addChildNode(newNode);
			parentNode = newNode;
        } else {
			Node newNode = null;
			int newNodeType;
            if (lName.startsWith(XETParser.VAR_N)) {
                newNodeType = Node.NT_NVAR;
            } else if (lName.startsWith(XETParser.VAR_I)) {
                newNodeType = Node.NT_IVAR;
            } else {
                newNodeType = Node.NT_DEFAULT;
            }
            newNode = XETParser.createNode(lName, namespacePrefix, namespaceURI, parentNode, newNodeType);
            parentNode.addChildNode(newNode);
            parentNode = newNode;
            // Extract attributes from XML elements
	        parseAttribute(attrs, parentNode);
        }
    }
    
    public void characters(char buf[], int offset, int len) throws SAXException {
        String text = new String(buf, offset, len).trim();
        if (parentNode != null) {
            if (text.length() !=  0) {
	            Node newNode = new Node();
	            newNode.setParentNode(parentNode);
	            if (text.startsWith(XETParser.VAR_E)) {
	                newNode.setNodeName(text);
	                newNode.setNodeType(Node.NT_EVAR);
	            } else if (text.startsWith(XETParser.VAR_E1)) {
	                newNode.setNodeName(text);
	                newNode.setNodeType(Node.NT_E1VAR);
	            } else if (text.startsWith(XETParser.VAR_S)) {
	                newNode.setNodeName(text);
	                newNode.setNodeType(Node.NT_SVAR);
	            } else {
	                newNode.setNodeName(null);
	                newNode.setNodeType(Node.NT_STRING);
	                newNode.setNodeValue(text);
	            }
	            parentNode.addChildNode(newNode);
	        }
        }
		
    }
    
    public void endElement(String namespaceURI, String lName, String qName) throws SAXException {
        if (parentNode != null) {
            parentNode = parentNode.getParentNode();
        }
    }
    
    /**
     * Returns the parsed fact file
     * 
     * @return The parsed fact file
     */
    public Node getFact() {
        return fact;
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uriPrefixMapper.put(uri, prefix);
    }
    
    public void skippedEntity(String name) throws SAXException {}
    
    public void endPrefixMapping(String prefix) throws SAXException {}
    
    public void endDocument() throws SAXException {
        logger.log(logLevel, Tool.LINE);
        logger.log(logLevel, "Parsed fact : " + this.xmlFile);
        logger.log(logLevel, Tool.LINE);
        logger.log(logLevel, fact.toString());
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    
    public void setDocumentLocator(Locator locator) {}
    
    public void processingInstruction(String target, String data) throws SAXException {}
    
}