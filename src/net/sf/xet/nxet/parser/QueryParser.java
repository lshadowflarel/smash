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
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Clause;
import net.sf.xet.nxet.tool.Tool;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * QueryParser parse an XML query file and return a query clause.
 * An XML query file can contain either a query constructor or
 * a full query clause with a head and a body. If the former case,
 * a new query clause will be created just from the query constructor.
 * Both the head and the body of the clause will be the query 
 * constructor. However, users also have a choice to specify the
 * full clause in the query file, which offers more flexibility
 * in the XML transformation.
 * 
 * @author Paramai Supadulchai
 */

public class QueryParser extends XETParser {
    
    private static final String TAG_NQUERY 				= "Query";
    private static final String TAG_DQUERY 				= "DQuery";
    private static final String TAG_QUERYCLAUSE 		= "QueryClause";
	private static final String TAG_NAME				= "name";
    
    public QueryParser(String xmlFile, String className, Configuration config, Handler logHandler) {
        super(xmlFile, className, config, logHandler);
        this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_QUERY_PARSER));
    }
    
    public static QueryParser createParser(String xmlFile, Configuration config, Handler logHandler) {
        // Use an instance of ourselves as the SAX event handler
        QueryParser queryParser = new QueryParser(xmlFile, QueryParser.class.getName(), config, logHandler);
        queryParser.assignContentHandler(queryParser);
        return queryParser;
    }
    
    private Vector childNodes = null;
    
    // The default clause type is "Deterministic Clause"
    private int clauseType = Clause.D_CLAUSE;
    
    // The variable used in parsing
    private Node currentNode = null;
    private Node parentNode = null;
    private Node previousSibling = null;
    // The query component
    private Node query = null;
    
    // The query clause
    private Clause queryClause = null;
    private Node queryClauseBody = null;
    private Node queryClauseHead = null;
    
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
     * Get the processed query clause object An XML query file 
     * can contain either a query constructor or a full query 
     * clause with a head and a body. If the former case,
     * a new query clause will be created just from the query 
     * constructor. Both the head and the body of the clause 
     * will be the query constructor. Otherwise, the query
     * clause will be created directly from the query clause
     * specified in the query file.
     * 
     * @return Returns the processed query clause
     * @since NxET 0.2
     */
    public Clause getQueryClause() {
        // Create a new clause
        queryClause = new Clause();
        queryClause.setClauseName("Clause 1");
        queryClause.setClauseType(this.clauseType);
        // If users provide the query clause, it has priority
        if ((queryClauseHead != null) && (queryClauseBody != null)) {
            queryClause.setHeadAtom(queryClauseHead);
            queryClause.addBodyAtom(queryClauseBody);
        } else if (query != null) {
            queryClause.setHeadAtom(query);
            queryClause.addBodyAtom(query.cloneNode());
        } else {
            System.err.println(Tool.LINE);
            System.err.println("Neither query constructor nor query clause is specified in the query file.");
        }
        return queryClause;
    }
    
    public void startDocument() {}

    public void startElement(String namespaceURI, String lName, 
			 				 String qName, Attributes attrs) throws SAXException {
        
		if (lName.equals(XETParser.TAG_XET) && 
            namespaceURI.equals(XETParser.XET_URI)) {
            return;
        // Create a new query if the current element is Query node
        // Set the newly created query to the current query
        } if (lName.equals(QueryParser.TAG_NQUERY) && 
              namespaceURI.equals(XETParser.XET_URI)) {
            
            Node newNode = createNode(QueryParser.TAG_NQUERY, prefix(namespaceURI), 
                                      namespaceURI, null, Node.NT_META);
            parentNode = newNode;
            clauseType = Clause.N_CLAUSE;
            
        // Create a new query if the current element is Query node
        // Set the newly created query to the current query
        } else if (lName.equals(QueryParser.TAG_DQUERY) && 
                   namespaceURI.equals(XETParser.XET_URI)) {
            
            Node newNode = createNode(QueryParser.TAG_DQUERY, prefix(namespaceURI),
                                      namespaceURI, null, Node.NT_META);
            parentNode = newNode;
            clauseType = Clause.D_CLAUSE;
            
        // This tag is just to make a structure for the head and the body of a query clause
        } else if (lName.equals(QueryParser.TAG_QUERYCLAUSE)  && 
                   namespaceURI.equals(XETParser.XET_URI)) {
            return;
            
        // Create a new head from the current element
        } else if (lName.equals(XETParser.TAG_HEAD)  && 
                   namespaceURI.equals(XETParser.XET_URI)) {
            
            Node newNode = createNode(XETParser.TAG_HEAD, prefix(namespaceURI),
                                      namespaceURI, null, Node.NT_HEAD);
            parentNode = newNode;
            
        // Create a body atom place holder
        } else if (lName.equals(XETParser.TAG_BODY)  && 
                   namespaceURI.equals(XETParser.XET_URI)) {
            
            Node newNode = createNode(XETParser.TAG_BODY, prefix(namespaceURI),
                                      namespaceURI, null, Node.NT_BODY);
            parentNode = newNode;
	    
        // Allow E-variable declaration in an XML element
		// Example: <xet:E-variable xet:name="Evar_name"/>
		// Added in version 3.0
	    } else if (lName.equals(XETParser.TAG_EVAR)  && 
				   namespaceURI.equals(XETParser.XET_URI)) {
	            
			Node newNode = createNode("", prefix(namespaceURI),
	                    			  namespaceURI, parentNode, Node.NT_EVAR);
			parseAttribute(attrs, newNode);
			if (newNode.hasAttributeByLocalName(QueryParser.TAG_NAME)) {
				if (newNode.attributeByLocalName(QueryParser.TAG_NAME).getNodeValue().startsWith(XETParser.VAR_E)) {
					newNode.setNodeName(newNode.attributeByLocalName(QueryParser.TAG_NAME).getNodeValue());
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
            
            int newNodeType;
            if (lName.startsWith(XETParser.VAR_N)) {
                newNodeType = Node.NT_NVAR;
            } else if (lName.startsWith(XETParser.VAR_I)) {
                newNodeType = Node.NT_IVAR;
            } else {
                newNodeType = Node.NT_DEFAULT;
            }
            Node newNode = createNode(lName, prefix(namespaceURI), namespaceURI, parentNode, newNodeType);
            if (parentNode.getNodeName().equals(QueryParser.TAG_DQUERY) ||
                parentNode.getNodeName().equals(QueryParser.TAG_NQUERY)) {
                query = newNode;
            } else if (parentNode.getNodeName().equals(XETParser.TAG_HEAD)) {
                queryClauseHead = newNode;
            } else if (parentNode.getNodeName().equals(XETParser.TAG_BODY)) {
                queryClauseBody = newNode;
            } else {
                parentNode.addChildNode(newNode);
            }
            parentNode = newNode;
            parseAttribute(attrs, parentNode);
        }
    }
    
    public void endDocument() throws SAXException {
        logger.log(logLevel, Tool.LINE);
        logger.log(logLevel, "Parsed query : " + this.xmlFile);
        logger.log(logLevel, "Type: " + ((query != null)? "Statement" : "Clause"));
        logger.log(logLevel, Tool.LINE);
        logger.log(logLevel, getQueryClause().toString());
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uriPrefixMapper.put(uri, prefix);
	}
    
    public void skippedEntity(String name) throws SAXException {}
    
    public void endPrefixMapping(String prefix) throws SAXException {}
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    
    public void setDocumentLocator(Locator locator) {}
    
    public void processingInstruction(String target, String data) throws SAXException {}
    
}
