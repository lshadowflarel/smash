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

package net.sf.xet.nxet.core;

import java.util.Vector;
import java.util.HashMap;
//import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map; 

/**
 * Node is a smallest element that is used to construct the data structure
 * "snowflake". Node is almost equivalent to each XML element in Document
 * Object Model (DOM). However, Node can represents attributes and all types
 * of XML-variables as well. A node can be classified into several types.
 * The type of a node is specified by the variable "nodeType", which can be
 * specified as follows:<br>
 * <br>
 * <pre>
 * 1.  A default node 	- a node that is compared with a normal XML element in
 *                        the XML schema.<br>
 * 2.  A builtin node 	- a node that represents a built-in function<br>
 * 3.  A metadata node	- a node that will be considered as a metadata. It will
 *                        not be included as part of the whole snowflake data
 *                        structure. Intuitively, a metadata node is used just
 *                        to transform XML metadata into variables in Java class.<br>
 * 4.  A head node	- a node that represents "xet:Head", the head of a rule<br>
 * 5.  A body node	- a node that represents "xet:Body", the body of a rule<br>
 * 6.  A condition node	- a node that represents "xet:Condition", the condition of
 *                        a rule<br>
 * 7.  An exec seq node	- a node that represents "xet:ExecutionSequence", the
 *                        execution sequence of a rule<br>
 * 8.  A string node	- a node that contains just a text. This is compared with
 *                        a value of an XML element in XML schema<br>
 * 9.  An S-variable	- a node that represents an XML S-variable<br>
 * 10. An E1-variable	- a node that represents an XML E1-variable<br>
 * 11. An E-variable	- a node that represents an XML E-variable<br>
 * 12. An I-variable	- a node that represents an XML I-variable<br>
 * 13. An P-variable	- a node that represents an XML P-variable<br>
 * 14. An N-variable	- a node that represents an XML N-variable<br>
 * </pre>
 * (A node will be considered as an XML attribute when the XML attribute variable
 * is set to "true"). Therefore, a P-variable must always have this attribute
 * variable set to true.<br>
 * <br>
 * An important attribute of a node is its parent, childnodes and attributes.
 * A node can be a child node of another node called its parent. It can have
 * zero or more child and attribute nodes.<br>
 * <br>
 * A node may belong to a namespace specified by a "prefix" and a "uri". NxET
 * matches two nodes by the equality of their fullnames. A fullname is generated
 * from concatinating the node prefix and the node name of a node. For example,
 * if the name, the prefix and the uri of a node is "element1", "ns1" and 
 * "http://ns1.org" respectively, the fullname will be "ns1:element1". The
 * combination of the uri and the name of a node is called "uriName". Up until
 * NxET 0.2, the uriName does not have any significant in the matching of
 * nodes and attributes. However, the uriName is used when NxET is trying to
 * match a node with a built-in function. The built-in function must belong
 * exclusively to a namespace and a URI.<br>
 * <br>
 * @author Paramai Supadulchai
 */

public class Node implements Cloneable, Comparable {
    
    // Textual representation of fault types
    protected static String[] NODE_TYPE = new String[] { 
            "DEFAULT",
            "QUERYINFO",
            "BUILTIN",
            "META",
            "HEAD", 
            "BODY", 
            "CONDITION",
            "EXECSEQ",
            "STRING",
            "SVAR",
            "E1VAR",
            "EVAR",
            "IVAR",
            "PVAR",
            "NVAR"
            };
    
    // Short textual representation of fault types
    protected static String[] NODE_TYPE_SHORT = new String[] { 
            "DEF",
            "QIF",
            "BTN",
            "MET",
            "HEA", 
            "BOD", 
            "CON",
            "EXE",
            "STR",
            "SVR",
            "E1V",
            "EVR",
            "IVR",
            "PVR",
            "NVR"
            };
    
    /**
     * Indicates that a node is a default node
     */
    public static final int NT_DEFAULT = 0;
    /**
     * Indicates that a node is a query info node
     */
    public static final int NT_QUERYINFO = 1;
    /**
     * Indicates that a node is a built-in node
     */
    public static final int NT_BUILTIN = 2;
    /**
     * Indicates that a node is a metadata node.
     * (Not included in the snowflake schema)
     */
    public static final int NT_META = 3;
    /**
     * Indicates that a node is the head of a rule.
     * (xet:Head) 
     */
    public static final int NT_HEAD = 4;
    /**
     * Indicates that a node is the body of a rule
     * (xet:Body)
     */
    public static final int NT_BODY = 5;
    /**
     * Indicates that a body is the condition of a
     * rule (xet:Condition)
     */
    public static final int NT_CONDITION = 6;
    /**
     * Indicates that a node is the execution
     * sequence of a rule (xet:ExecutionSequence)
     */
    public static final int NT_EXECSEQ = 7;
    /**
     * Indicates that a node is the value of an
     * XML element
     */
    public static final int NT_STRING = 8;
    /**
     * Indicates that a node is an S-variable
     */
    public static final int NT_SVAR = 9;
    /**
     * Indicates that a node is an E1-variable
     */
    public static final int NT_E1VAR = 10;
    /**
     * Indicates that a node is an E-variable
     */
    public static final int NT_EVAR = 11;
    /**
     * Indicates that a node is an I-variable
     */
    public static final int NT_IVAR = 12;
    /**
     * Indicates that a node is an P-variable
     */
    public static final int NT_PVAR = 13;
    /**
     * Indicates that a node is an N-variable
     */
    public static final int NT_NVAR = 14;
    // Node can also be used as an attribute
    protected boolean attributeNode = false;
    // The list of all attribute of this node
    protected HashMap<String, Node> attributes = new HashMap<String, Node>();
    // The list of all child nodes of this node
    protected Vector<Node> childNodes = new Vector<Node>();
    // Namespace
    protected String namespace = null;
    // Node name
    protected String nodeName = null;
    // Integer specifying node type
    protected int nodeType = 0;
    // Node value
    protected String nodeValue = null;
    // The parent of this node
    protected Node parentNode = null;
    // The child position of the parent node
    protected int childPosition = 0;
    // URI
    protected String uri = null;
    
    /**
     * Add a new XML attribute to this node
     * 
     * @param attribute an attribute node to add
     */
    public void addAttribute(Node attribute) {
        this.attributes.put(attribute.fullName(), attribute);
    }
    
    /**
     * Add a new child node to this node. The position 
     * of the child node will be dynamically generated. 
     * When a child node is added, it is normally added
     * to the end of the node list. Therefore, its
     * child position will be equal to the former size
     * of the list. For example, if the list is empty,
     * the first added child node has its child position
     * "0".<br>
     * <br>
     * Caution: this method is currently not a thread-safe
     * and it is possible that two threads may be trying
     * to add a child node at the same time. The consequence
     * is that two child nodes may get the same child 
     * position.
     * 
     * @param childNode a child node to add
     */
    public void addChildNode(Node childNode) {
    	childNode.setChildPosition(this.childNodes.size());
        childNode.setParentNode(this);
        this.childNodes.add(childNode);
    }
    
    /**
     * Get attribute by referring to the node's fullname.
     * A fullname is the combination of a prefix and and
     * a node name. For example, if a node has a namespace
     * and a name "ns1" and "node1" respectively, its full
     * name will be ns1:node1
     * 
     * @param fullName the fullname of the node
     * @return the node specified by the fullname
     */
    public Node attribute(String fullName) {
        
        return this.attributes.get(fullName);
    }
    
    /**
     * Get attribute by referring to the node's localname.
     * A local name is the name without a prefix.<br>
     * <br>
     * <b>Caution:</b> this function assumes that an 
     * attribute must have the same namespace as its host node.
     * Otherwise, it will return NULL if the attribute you
     * need have the same name but different namespace.
     * 
     * @param localName the local name of the node
     * @return the node specified by the local name
     */
    public Node attributeByLocalName(String localName) {
        //return attribute(this.namespace + ":" + localName);
		return attribute(this.uri + "#" + localName);
    }
    
    /**
     * Retrieve all the keys of the attributes. An attribute's
     * key is the fullname of the attribute.
     * 
     * @return iterator object containing all attribute's key
     */
    public Iterator attributeKey() {
        
        return this.attributes.keySet().iterator();
        
    }
    
    /**
     * Return a child node of this node specifying by an index
     * 
     * @param nodeIndex the index of a child node to get
     * @return the child node specified by the index
     */
    public Node childNode(int nodeIndex) {
        
        return this.childNodes.get(nodeIndex);
        
    }
    
    public Vector<Node> getChildNodes(String localName) {
    	Vector<Node> matched = new Vector<Node>();
    	for(Node child:childNodes) {
    		if(child.nodeName.equals(localName))
    			matched.add(child);
    	}
    	
    	return matched;
    }
    
    /**
     * This method clones a node and all its child nodes
     * 
     * @return a cloned node as an object node
     */
    public Object clone() {
        
        Node n = new Node();
        
        n.setAttributeNode(this.isAttributeNode());
        n.setChildPosition(this.getChildPosition());
        n.setNamespace(this.getNamespace());
        n.setNodeName(this.getNodeName());
        n.setNodeValue(this.getNodeValue());
        n.setNodeType(this.getNodeType());
        n.setUri(this.getUri());
        
        // n.setParentNode(this.getParentNode());
        for (int i = 0; i < this.childNodes.size(); i++) {
            Node newChildNode = this.childNodes.get(i).cloneNode();
            newChildNode.setParentNode(n);
            n.addChildNode(newChildNode);
        }
        
        Iterator<String> attrs = this.attributes.keySet().iterator();
        
        while(attrs.hasNext()) {
            
            Node newAttributeNode = this.attributes.get(attrs.next()).cloneNode();
            newAttributeNode.setParentNode(n);
            n.addAttribute(newAttributeNode);
        }
        
        return n;
        
    }
    
    /**
     * This method is a wrapper method, that clones the
     * current node and cast it back from an object type
     * to the "Node" type
     *
     * @return Returns a cloned node as a "Node" type
     */
    public Node cloneNode() {
        return (Node)(this.clone());
    }
    
    /**
     * This methods implements the comparable interface
     * and compares the "fullname" of two nodes using
     * the compareTo function (from the String's class).<br>
     * <br>
     * The function returns less-than-zero value if the 
     * fullname of the current node comes before the 
     * fullname of another node (specified in the
     * parameter).<br>
     * <br>
     * The function returns zero value if a fullname of
     * a node is exactly equal to the fullname of another 
     * node (specified in the parameter).<br>
     * <br>
     * The function returns more-than-zero if the full
     * name of the a node comes after the fullname of
     * another node (specified in the parameter).<br>
     * <br>
     * For example:<br>
     * <br>
     * <pre>
     * xet:node1	compareTo	xet:node1	result =  0<br>
     * xet:node1	compareTo	xet:node2	result = -1<br>
     * xet:node2	compareTo	xet:node1	result =  1<br>
     * </pre>
     */
    public int compareTo(Object obj) {
        Node otherNode = (Node) obj;
        return this.fullName().compareTo(otherNode.fullName());
    }
    
    /**
     * This method copy a set of nodes
     * 
     * @return a set of cloned nodes
     */
    public Object copySetOfNodes(int beginCopyIndex, int endOfCopyIndex) {
        
        Node n = new Node();
        
        n.setAttributeNode(this.isAttributeNode());
        n.setChildPosition(this.getChildPosition());
        n.setNamespace(this.getNamespace());
        n.setNodeName(this.getNodeName());
        n.setNodeValue(this.getNodeValue());
        n.setNodeType(this.getNodeType());
        n.setUri(this.getUri());
        
        // n.setParentNode(this.getParentNode());
        for (int i = beginCopyIndex; i <= endOfCopyIndex; i++) {
            Node newChildNode = this.childNodes.get(i).cloneNode();
            newChildNode.setParentNode(n);
            n.addChildNode(newChildNode);
        }
        
        Iterator<String> attrs = this.attributes.keySet().iterator();
        
        while(attrs.hasNext()) {
            
            Node newAttributeNode = this.attributes.get(attrs.next()).cloneNode();
            newAttributeNode.setParentNode(n);
            n.addAttribute(newAttributeNode);
        }
        
        return n;
        
    }    
    
    /**
     * Return the fullname of this node. The fullname is
     * composed from the namespace (prefix) and the name
     * of this node. (If the namespace is nothing, only
     * the name will be given back. This prevents Java
     * from outputing the word "null" as a prefix.)
     * 
     * @return Returns the fullname of this node
     */
    public String fullName() {
        //return ((this.namespace == null)? this.getNodeName() : this.getNamespace() + ":" + this.getNodeName());
		return ((this.uri == null)? this.getNodeName() : this.uri + "#" + this.getNodeName());
    }
    
    /**
     * Get the list (vector) containing all child nodes
     * of this node.
     * 
     * @return Returns the list of child nodes
     */
    public Vector getChildNodes() {
        return this.childNodes;
    }
    
    /**
     * Get the child position of this node. The child
     * position is the position of its parent's childnodes
     * vector.
     * 
     * @return Returns the position of this node.
     */
    public int getChildPosition() {
        return childPosition;
    }
    
    /**
     * Get the namespace of this node. If the
     * namespace is nothing, the function returns
     * null.
     * 
     * @return Returns the namespace of this node
     */
    public String getNamespace() {
        return this.namespace;
    }
    
    /**
     * Get the name of this node.
     * 
     * @return Returns the name of this node
     */
    public String getNodeName() {
        return this.nodeName;
    }
    
    /**
     * Get the node type of this node.
     * 
     * @return Returns the node type of this node.
     */
    public int getNodeType() {
        return this.nodeType;
    }
    
    /**
     * Get the node value of this node.
     * 
     * @return Returns the node value of this node.
     */
    public String getNodeValue() {
        return this.nodeValue;
    }
    
    /**
     * Get the parent node of this node.
     * 
     * @return Returns the parent node of this node.
     */
    public Node getParentNode() {
        return this.parentNode;
    }
    
    /**
     * Get the uri of this node
     * 
     * @return Returns the uri of this nodes.
     */
    public String getUri() {
        return this.uri;
    }
    
    /**
     * Check if this node has a single attribute or not.
     * 
     * @return Returns true if the node has an attribute in the list.
     */
    public boolean hasAttribute() {
        return ((attributes != null) && (attributes.size() > 0));
    }
    
    /**
     * Check if an attribute specified by its "fullname" exists
     * in this node or not
     * 
     * @param fullAttributeName the "fullname" of an attribute
     * @return Returns true if the attribute exists.
     */
    public boolean hasAttribute(String fullAttributeName) {
        return attributes.containsKey(fullAttributeName);
    }
    
    /**
     * Check if an attribute specified by its "nodeName" exists
     * in this node or not.<br> 
     * <br>
     * <b>Caution:</b> Since this function uses the namespace of the 
     * hosted node, it will return "false" immediately if the attribute 
     * you need has a different namespace. Therefore, use this function 
     * if you need to query the attribute name that you are sure it 
     * has the same namespace as the hosted node.
     * 
     * @param localAttributeName the "nodeName" of an attribute
     * @return Returns true if the attribute exists
     */
    public boolean hasAttributeByLocalName(String localAttributeName) {
        //return attributes.containsKey(this.namespace + ":" + localAttributeName);
		return attributes.containsKey(this.uri + "#" + localAttributeName);
    }
    
    /**
     * Check if this node has a single child node or not.
     * 
     * @return Returns true if the node has a child node in the list.
     */
    public boolean hasChildNodes() {
        return ((childNodes != null) && (childNodes.size() > 0));
    }
    
    /**
     * A private function used to output "spaces" at a specific amount.
     * The function is used by "printXML" to properly indent all the tags.
     * The input parameter "indentLevel" will be multiplied by "  " (two
     * empty spaces). 
     * 
     * @return Returns a specific amount of spaces.
     */
    private String indent(int indentLevel) {
        StringBuffer sb = new StringBuffer();
        String indent = "  ";
        for (int i = 0; i < indentLevel; i++) {
            sb.append(indent);
        }
        return sb.toString();
    }
    
    /**
     * Insert a child node into a specific position in the child node list
     * 
     * @param positionIndex The position to insert a child node
     * @param childNode The child node to be inserted.
     */
    public void insertNode(int positionIndex, Node childNode) {
        childNode.setChildPosition(positionIndex);
        childNode.setParentNode(this);
        this.childNodes.insertElementAt(childNode, positionIndex);
    }
    
    /**
     * Check if this node is an attribute node or not. The function
     * simply returns a boolean variable "attributeNode".
     * 
     * @return Returns true if the node is specified as an attribute.
     */
    public boolean isAttributeNode() {
        return this.attributeNode;
    }
    
    /**
     * Check if this node is a built-in or not. The function simply
     * compares the node type with Node.NT_BUILTIN
     * 
     * @return Returns true if the node has "NT_BUILTIN" node type.
     */
    public boolean isBuiltin() {
        return (this.nodeType == Node.NT_BUILTIN);
    }
    
    /**
     * Check if this node is a variable or not. The function simply
     * compares the node type if the node type equals to or greater
     * than S-variable or not. If the node type is less than NT.SVAR,
     * the node is not a variable. If it is greater than NT.SVAR,
     * the node is a variable.
     * 
     * @return Returns true if the node type is greater than or
     *         equal to NT.SVAR
     */
    public boolean isVariable() {
        return ((this.nodeType >= NT_SVAR)? true: false);
    }
    
    /**
     * Generate a (longer) textual representation of the node type 
     * of this node.
     * 
     * @return Returns the (longer) textual representation of this node. 
     */
    public String nodeTypeText() {
        return NODE_TYPE[this.getNodeType()];
    }
    
    /**
     * Generate a (shorter) textual representation of the node type 
     * of this node.
     * 
     * @return Returns the (shorter) textual representation of this node. 
     */
    public String nodeTypeShortText() {
        return NODE_TYPE_SHORT[this.getNodeType()];
    }
    
    /**
     * Returns the number of attributes of this node.
     * 
     * @return Returns the number of attributes of this node.
     */
    public int numberOfAttributes() {
        return (this.attributes == null)? 0 : this.attributes.size();
    }
    
    /**
     * Returns the number of child nodes of this node.
     * 
     * @return Returns the number of attributes of this node.
     */
    public int numberOfChildNodes() {
        return (this.childNodes == null)? 0 : this.childNodes.size();
    }
    
    /**
     * Find the number of uninstantiated variables in this node.
     * An uninstantiated variable is exactly a variable node.
     * 
     * @return Returns the number of uninstantiates variables in this node.
     */
    public int numberOfUninstantiatedVariable() {
        return numberOfUninstantiatedVariable(this);
    }
    
    /**
     * Find the number of uninstantiated variables in a specific node.
     * An uninstantiated variable is exactly a variable node. This
     * function is required by numberOfUninstantiatedVariable() function.
     * 
     * @param n the node to find the number of uninstantiated variables
     * @return Returns the number of uninstantiates variables in a node.
     */
    protected int numberOfUninstantiatedVariable(Node n) {
        int variableNo = n.isVariable()? 1: 0;
        if (n.attributes != null) {
	        for (int i = 0; i < n.attributes.size(); i++) {
	            if (n.isVariable()) {
	                variableNo++;
	            }
	        }
        }
        if (n.childNodes != null) {
	        for (int i = 0; i < n.childNodes.size(); i++) {
	            variableNo += numberOfUninstantiatedVariable(n.childNode(i));
	        }
        }
        return variableNo;
    }
    
    /**
     * Print the XML presentation of all attributes in this node. 
     * 
     * @return Returns the XML presentation of all attributes in this node.
     */
    public String printAttribute() {
        StringBuffer attribute = new StringBuffer();
        Iterator<String> attrs = this.attributes.keySet().iterator();
        int numberOfAttribute = 0;
        while(attrs.hasNext()) {
            String attrName = attrs.next();
            attribute.append(this.attribute(attrName));
            numberOfAttribute++;
        }
        return attribute.toString();
    }
   
    /**
     * Print the XML representation of this node including all the attributes
     * and the child nodes.
     * 
     * @return Returns the XML representation of this node.
     */
    public String printXML() {
        StringBuffer sb = new StringBuffer();
        printXML(sb, this, 0);
        return sb.toString();
    }
    
    /**
     * Print the XML representation of this node including all the attributes
     * and the child nodes. The indentation starts from a given "level".
     * 
     * @return Returns the XML representation of this node.
     */
    public String printXML(int level) {
        StringBuffer sb = new StringBuffer();
        printXML(sb, this, level);
        return sb.toString();
    }
    
    /**
     * Print the XML presentation of a node including all the attributes
     * and the child nodes. The indentation starts from a given "level".
     * The output is accumulated in a StringBuffer instance passed by
     * its parent node.
     * 
     * @param sb The accumulated StringBuffer object
     * @param n The node to print its XML representation
     * @param level The indentation level
     */
    protected void printXML(StringBuffer sb, Node n, int level) {
        
        if (n.isAttributeNode()) {
            
            sb.append(" ");
            sb.append((n.getNamespace() == null)? "" : n.getNamespace() + ":");
            sb.append(n.nodeName + "=\"" + n.nodeValue + "\"");
            
    	} else if (n.getNodeType() == Node.NT_STRING) {
            try {
                sb.append(n.getNodeValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ((n.getNodeType() == Node.NT_DEFAULT) || 
                   (n.getNodeType() == Node.NT_BUILTIN) || 
                   (n.getNodeType() == Node.NT_NVAR)) {
            
            sb.append(((level==0)?"":"\n") + indent(level) + "<");
            sb.append((n.getNamespace() == null)? "" :n.getNamespace() + ":");
            sb.append(n.getNodeName());
            
            Iterator attrs = n.attributes.keySet().iterator();
            while(attrs.hasNext()) {
                String attrName = (String)attrs.next();
                printXML(sb, n.attribute(attrName), level);
            }
            
            if (n.numberOfChildNodes() > 0) {
                sb.append(">");
	            for (int i = 0; i < n.numberOfChildNodes(); i++) {
	                printXML(sb, n.childNode(i), level + 1);
	            }
	            if ((n.numberOfChildNodes() == 1) && 
	                (n.childNode(0).getNodeType() >= Node.NT_STRING) &&
	                (n.childNode(0).getNodeType() != Node.NT_IVAR) && 
	                (n.childNode(0).getNodeType() != Node.NT_NVAR)) {
	                sb.append("</");
	                sb.append((n.getNamespace() == null)? "" :n.getNamespace() + ":");
	                sb.append(n.getNodeName() + ">");
	            } else if ((n.numberOfChildNodes() == 1) && 
	                       (n.childNode(0).getNodeType() == Node.NT_IVAR)) {
	                sb.append(indent(level) + "</");
	                sb.append((n.getNamespace() == null)? "" :n.getNamespace() + ":");
	                sb.append(n.getNodeName() + ">");
	            } else if (n.childNode(0).getNodeType() == Node.NT_NVAR) {
	                sb.append("\n" + indent(level) + "</");
	                sb.append((n.getNamespace() == null)? "" :n.getNamespace() + ":");
	                sb.append(n.getNodeName() + ">");
	            } else {
	                sb.append("\n" + indent(level) + "</");
	                sb.append((n.getNamespace() == null)? "" :n.getNamespace() + ":");
	                sb.append(n.getNodeName() + ">");
	            }
            } else {
                sb.append("/>");
            }
        } else if (n.getNodeType() == Node.NT_IVAR) {
            sb.append("\n" + indent(level) + "<" + n.fullName() + ">");
            //printXML(sb, n.childNode(0), level + 1);
            for (int i = 0; i < n.numberOfChildNodes(); i++) {
                printXML(sb, n.childNode(i), level + 1);
            }
            sb.append("\n" + indent(level) + "</" + n.fullName() + ">\n");
        } else if (n.getNodeType() == Node.NT_E1VAR) {
            if (n.getParentNode().numberOfChildNodes() == 1) {
                sb.append(n.nodeName);
            } else {
                sb.append("\n" + indent(level) + n.nodeName);
            }
        } else if (n.getNodeType() == Node.NT_EVAR) {
            if ((n.getParentNode()!= null ) && n.getParentNode().numberOfChildNodes() == 1) {
                sb.append(n.nodeName);
            } else {
                sb.append("\n" + indent(level) + n.nodeName);
            }
        } else if (n.getNodeType() == Node.NT_SVAR) {
            if (!n.isAttributeNode()) {
                if (n.getParentNode().numberOfChildNodes() == 1) {
                    sb.append(n.nodeName);
                } else {
                    sb.append("\n" + indent(level) + n.nodeName);
                }
            } else {
                sb.append(" " + n.nodeName + "=\"" + n.nodeValue + "\"");
            }
        } else {
            System.out.println(nodeTypeText() + n.nodeName);
        }
    }
    
    /**
     * Remove an attribute that is specified by an attribute name
     * 
     * @param attrName The name of the attribute to be removed
     */
    public void removeAttribute(String attrName) {
        this.attributes.remove(attrName);
    }
    
    /**
     * Remove a child node and replace it with a new child node
     * at the same position.
     * 
     * @param oldChildNode The child node to be removed
     * @param newChildNode The new child node to be inserted
     * @since NxET 0.2
     */
    public void replaceChildNode(Node oldChildNode, Node newChildNode) {
        int oldChildNodePosition = oldChildNode.getChildPosition();
        this.removeNode(oldChildNodePosition);
        this.insertNode(oldChildNodePosition, newChildNode);
    }
    
    /**
     * Replace itself in its parent node with a new node
     * 
     * @param replacingNode The new child node to replace the current one
     * @since NxET 0.2
     */
    public void replaceSelf(Node replacingNode) {
    	this.parentNode.removeNode(this.getChildPosition());
        this.parentNode.insertNode(this.getChildPosition(), replacingNode);        
    }
    
    /**
     * Remove a child node that is specified by an index
     * 
     * @param childIndex The index of the child node to remove
     */
    public void removeNode(int childIndex) {
        this.childNodes.remove(childIndex);
    }
    
    /**
     * Specify whether this node is an attribute node
     * 
     * @param attribute The boolean value true means this node is
     *                  an attribute node and vice versa.
     */
    public void setAttributeNode(boolean attribute) {
        this.attributeNode = attribute;
    }
    
    /**
     * Set a child position from this node. The child
     * position is set automatically when the method
     * addChildNode of the parent of this node is invoked.
     * 
     * @param childPosition The child position to set.
     * @since NxET 0.2
     */
    public void setChildPosition(int childPosition) {
        this.childPosition = childPosition;
    }
    
    /**
     * Set a namespace for this node. The input namespace 
     * will be assigned to the private variable of this node
     * if the namespace string is not null and the namespace 
     * does not contain just empty "space(s)".<br>
     * <br>
     * 
     * @param namespace The namespace to set
     */
    public void setNamespace(String namespace) {
        if ((namespace != null) && (!namespace.trim().equals(""))) {
            this.namespace = namespace;
        }
    }
    
    /**
     * Assign a node name to this node. The input nodeName
     * will be assigned to the private variable of this 
     * node if the nodeName is not null. If the input
     * nodeName contain leading or trailing "space(s)", 
     * the spaces will be trimmed.
     * 
     * @param nodeName The name to set for this node
     */
    public void setNodeName(String nodeName) {
        if (nodeName != null) {
            nodeName.trim();
        }
        this.nodeName = nodeName;
    }
    
    /**
     * Assign a node type to this node
     * 
     * @param nodeType The node type to assign
     */
    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }
    
    /**
     * Assign a node value to this node.
     * 
     * @param nodeValue The node value to assign
     */
    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }
    
    /**
     * Assign a parent node to this node. Although
     * the parent node can be automatically set by
     * addChildNode method, this method is still
     * useful when a variable is needed to be
     * instantiate.<br>
     * <br>
     * In this future, this method may be deprecated.
     * 
     * @param parentNode The parent node to set
     */
    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }
    
    /**
     * Set a uri for this node. The input uri will be
     * assigned to the private variable of this node
     * if the uri string is not null and the uri does
     * not contain just empty "space(s)".<br>
     * <br>
     * This function does not verify the correct of
     * the input uri.
     * 
     * @param uri The uri to set
     */
    public void setUri(String uri) {
        if ((uri != null) && (!uri.trim().equals(""))) {
            this.uri = uri;
        }
    }
    
    /**
     * Convert this node to string by invoking the
     * method "printXML".
     * 
     * @see #printXML()
     */
    public String toString() {
        return this.printXML();
    }
    
    /**
     * Return the uriName of this node. The uriName is
     * composed from the uri and the name of this node. 
     * (If the namespace is nothing, only the name will 
     * be given back. This prevents Java from outputing 
     * the word "null" as a prefix.)
     * 
     * @return Returns the uriName of this node
     */
    public String uriName() {
        return ((this.uri == null)? this.getNodeName() : this.getUri() + "." + this.getNodeName());
    }
    
    /**
     * Find the set of uninstantiated variables in this node.
     * An uninstantiated variable is exactly a variable node.
     * 
     * @return Returns the number of uninstantiates variables in this node.
     */
    public HashSet<String> uninstantiatedVariableSet() {
        return uninstantiatedVariableSet(this);
    }
    
    /**
     * Find the set of uninstantiated variables in a specific node.
     * An uninstantiated variable is exactly a variable node. This
     * function is required by uninstantiatedVariableList() function.
     * 
     * @param n the node to find the number of uninstantiated variables
     * @return Returns the number of uninstantiates variables in a node.
     */
    protected HashSet<String> uninstantiatedVariableSet(Node n) {
        HashSet<String> variableSet = new HashSet<String>();
        if (n.isVariable()) {
            variableSet.add(n.fullName());
        }
        if (n.attributes != null) {
	        for (int i = 0; i < n.attributes.size(); i++) {
	            if (n.isVariable()) {
	                if ((n.getNodeType() == Node.NT_PVAR) ||
	                    (n.getNodeType() == Node.NT_NVAR)) {
	                    variableSet.add(n.fullName());
	                } else if (n.getNodeType() == Node.NT_SVAR) {
	                    variableSet.add(n.getNodeValue());
	                }
	            }
	        }
        }
        if (n.childNodes != null) {
	        for (int i = 0; i < n.childNodes.size(); i++) {
	            HashSet<String> childSet = uninstantiatedVariableSet(n.childNode(i));
	            Iterator<String> it = childSet.iterator();
	            while (it.hasNext()) {
	                variableSet.add(it.next());
	            }
	        }
        }
        return variableSet;
    }
/*
 *  My new modification for the function for get all variables in the node 
 */    
    public HashSet<String> getVariableSet(String suff) {
        return getVariableSet(this,suff);
    }
    
    /**
     * Find the set of uninstantiated variables in a specific node.
     * An uninstantiated variable is exactly a variable node. This
     * function is required by uninstantiatedVariableList() function.
     * 
     * @param n the node to find the number of uninstantiated variables
     * @return Returns the number of uninstantiates variables in a node.
     */
    protected HashSet<String> getVariableSet(Node n, String suff) {
        HashSet<String> variableSet = new HashSet<String>();
        if (n.isVariable()) {
        	n.setNodeName(n.getNodeName() + suff);
            variableSet.add(n.fullName());
           
        }
     
        
        if (n.attributes != null) {
	        for (Map.Entry<String,Node> entry : n.attributes.entrySet() ) {
	            Node attributeNode = entry.getValue() ; 
	        	if (attributeNode.isVariable()) {
	                if ((attributeNode.getNodeType() == Node.NT_PVAR) ||
	                    (attributeNode.getNodeType() == Node.NT_NVAR)) {
	                	attributeNode.setNodeName(attributeNode.getNodeName() + suff);
	                    variableSet.add("Node name: " + n.fullName()+ " -- " + attributeNode.fullName());
	                    
	                } else if (attributeNode.getNodeType() == Node.NT_SVAR) {
	                	attributeNode.setNodeValue( attributeNode.getNodeValue() + suff);
	                    variableSet.add("Node name: " + n.fullName()+ " -- " + attributeNode.getNodeValue());
	                }
	            }
	        }
        }
        
        
        if (n.childNodes != null) {
	        for (int i = 0; i < n.childNodes.size(); i++) {
	            HashSet<String> childSet = getVariableSet(n.childNode(i),suff);
	            Iterator<String> it = childSet.iterator();
	            while (it.hasNext()) {
	                variableSet.add(it.next());
	            }
	        }
        }
        return variableSet;
    }
    public void renameVariables(String suff) {
        renameVariableSet(this,suff);
    }
    
    /**
     * @author Phong
     * Find the set of uninstantiated variables in a specific node.
     * An uninstantiated variable is exactly a variable node. This
     * function is required by uninstantiatedVariableList() function.
     * 
     * @param n the node to find the number of uninstantiated variables
     * @return Returns the number of uninstantiates variables in a node.
     */
    protected void renameVariableSet(Node n, String suff) {
        
        if (n.isVariable()) {
        	n.setNodeName(n.getNodeName() + suff);                
        }
             
        if (n.attributes != null) {
	        for (Map.Entry<String,Node> entry : n.attributes.entrySet() ) {
	            Node attributeNode = entry.getValue() ; 
	        	if (attributeNode.isVariable()) {
	                if ((attributeNode.getNodeType() == Node.NT_PVAR) ||
	                    (attributeNode.getNodeType() == Node.NT_NVAR)) {
	                	attributeNode.setNodeName(attributeNode.getNodeName() + suff);                   
	                    
	                } else if (attributeNode.getNodeType() == Node.NT_SVAR) {
	                	attributeNode.setNodeValue( attributeNode.getNodeValue() + suff);	                    
	                }
	            }
	        }
        }
        
        
        if (n.childNodes != null) {
	        for (int i = 0; i < n.childNodes.size(); i++) {
	            renameVariableSet(n.childNode(i),suff);	            
	            }
	    }
        
        
    }
    
    /**
     * @author Phong
     * This function is used to remove all the attributes of the node
     */
    public void removeAllAtributes()
    {
    	if (this.attributes != null) {
	        for (Map.Entry<String,Node> entry : this.attributes.entrySet() ) {
	            Node attributeNode = entry.getValue() ; 
	        	this.removeAttribute( attributeNode.getNodeName() );
	        }
        }
    }
    
    /**
     * @author Phong
     * This function to remove all the child node
     */
    
    public void removeAllNodes()
    {
//    	if (this.childNodes != null) {
//	        for (int i = 0; i < this.childNodes.size(); i++) {
//	        			this.removeNode (i);
//	            }
//	    }
    	
    	this.childNodes.clear();
    }
    
}