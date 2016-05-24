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
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.tool.Tool;
import net.sf.xet.nxet.tool.Project;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Parse a NxET project file in the following format<br>
 * <br>
 * <pre>
 * &lt;xet:XET xmlns:xet="http://xet.sf.net"&gt;
 *   &lt;xet:Project xet:name="Project Name"&gt;
 *     &lt;xet:Meta&gt;
 *       &lt;xet:Description&gt;Project Descrption&lt;/xet:Description&gt;
 *     &lt;/xet:Meta&gt;
 *     &lt;xet:RuleFile xet:uri="rule-file-uri"/&gt;
 *     &lt;xet:QueryFile xet:uri="query-file-uri"/&gt;
 *     &lt;xet:DatasourceFile xet:uri="datasource-file-uri"/&gt;
 *     &lt;xet:ConfigFile xet:uri="config-file-uri"/&gt;
 *   &lt;/xet:Project&gt;
 * &lt;/xet:XET&gt;
 * </pre>
 * <br>
 * Returns the net.sf.xet.nxet.tool.Project object
 * 
 * @author Paramai Supadulchai
 */
public class ProjectFileParser extends XETParser {
    
    private static final String TAG_PROJECT 		= "Project";
	private static final String TAG_RULE_FILE 		= "RuleFile";
	private static final String TAG_QUERY_FILE 		= "QueryFile";
	private static final String TAG_DATASOURCE_FILE = "DatasourceFile";
	private static final String TAG_CONFIG_FILE 	= "ConfigFile";
	private static final String TAG_NAME 			= "name";
	private static final String TAG_URI 			= "uri";
	private static final String TAG_META 			= "Meta";
	private static final String TAG_DESCRIPTION		= "Description";
	
    private Project project = new Project();
	private Node parentNode = null;
    
    public ProjectFileParser(String xmlFile, String className, Configuration config, Handler logHandler) {
        super(xmlFile, className, config, logHandler);
        this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_DATA_SOURCE_PARSER));
    }
    
    public static ProjectFileParser createParser(String xmlFile, Configuration config, Handler logHandler) {
        // Use an instance of ourselves as the SAX event handler
		ProjectFileParser projectFileParser = new ProjectFileParser(xmlFile, ProjectFileParser.class.getName(), 
                                                                 config, logHandler);
		projectFileParser.assignContentHandler(projectFileParser);
        return projectFileParser;
    }
    
    /**
     * Get the parsed project
     * 
     * @return Returns the parsed project
     */
    public Project getProject() {
        return this.project;
    }
    
    public void startDocument() {
        // The parent node is now the document element
        seenRootElement = false;
    }

    public void startElement(String namespaceURI, String lName, 
			 				 String qName, Attributes attrs) throws SAXException {
        
		Node newNode = null;
		
        if (!seenRootElement) {
            // This is probably the first "xet:XET" document element.
            seenRootElement = true;
        } else if ((lName.equals(ProjectFileParser.TAG_PROJECT) && 
                	namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_PROJECT, prefix(namespaceURI), 
                    	   		 namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			project.setProjectName(newNode.attributeByLocalName(ProjectFileParser.TAG_NAME).getNodeValue());
		} else if ((lName.equals(ProjectFileParser.TAG_META) && 
        			namespaceURI.equals(XETParser.XET_URI))) {
			// Do nothing
		} else if ((lName.equals(ProjectFileParser.TAG_DESCRIPTION) && 
        			namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_DESCRIPTION, prefix(namespaceURI), 
	                	   		 namespaceURI, null, Node.NT_META);
		} else if ((lName.equals(ProjectFileParser.TAG_RULE_FILE) && 
            		namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_RULE_FILE, prefix(namespaceURI), 
	                	   		 namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			project.setRuleFileUri(newNode.attributeByLocalName(ProjectFileParser.TAG_URI).getNodeValue());
		} else if ((lName.equals(ProjectFileParser.TAG_QUERY_FILE) && 
        			namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_QUERY_FILE, prefix(namespaceURI), 
	                	   		 namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			project.setQueryFileUri(newNode.attributeByLocalName(ProjectFileParser.TAG_URI).getNodeValue());
		} else if ((lName.equals(ProjectFileParser.TAG_DATASOURCE_FILE) && 
					namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_DATASOURCE_FILE, prefix(namespaceURI), 
	                	   		 namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			project.setDatasourceFileUri(newNode.attributeByLocalName(ProjectFileParser.TAG_URI).getNodeValue());
		} else if ((lName.equals(ProjectFileParser.TAG_CONFIG_FILE) && 
    				namespaceURI.equals(XETParser.XET_URI))) {
			newNode = createNode(ProjectFileParser.TAG_CONFIG_FILE, prefix(namespaceURI), 
	                	   		 namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			project.setConfigFileUri(newNode.attributeByLocalName(ProjectFileParser.TAG_URI).getNodeValue());
		} else {
			
			
	    }
		parentNode = newNode;
    }
    
    public void characters(char buf[], int offset, int len) throws SAXException {
		if (parentNode != null) {
			if (parentNode.getNodeName().equals(ProjectFileParser.TAG_DESCRIPTION)) {
				project.setProjectDescription(new String(buf, offset, len).trim());
			}
		}
		parentNode = null;
    }
    
    public void endElement(String namespaceURI, String lName, String qName) throws SAXException {}
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        uriPrefixMapper.put(uri, prefix);
    }
    
    public void skippedEntity(String name) throws SAXException {}
    
    public void endPrefixMapping(String prefix) throws SAXException {}
    
    public void endDocument() throws SAXException {}
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    
    public void setDocumentLocator(Locator locator) {}
    
    public void processingInstruction(String target, String data) throws SAXException {}
 
}
