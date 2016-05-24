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

import java.io.*;
import java.util.Vector;
import java.util.Properties;
import java.util.logging.Handler;
import net.sf.xet.nxet.builtin.BuiltinManager;
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.RuleHead;
import net.sf.xet.nxet.core.RuleCondition;
import net.sf.xet.nxet.core.RuleBody;
import net.sf.xet.nxet.core.Rule;
import net.sf.xet.nxet.tool.Tool;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The parser to parse all the rule file. An interesting feature is the ability
 * to include other rule files.
 * 
 * @author Paramai Supadulchai
 */
public class RuleParser extends XETParser {

	private static final String TAG_INIT = "Initialization";
	private static final String TAG_INCLUDE = "Include";
	private static final String TAG_URI = "uri";
	private static final String TAG_RULEPRIOR = "RulePriority";
	private static final String TAG_META = "Meta";
	private static final String TAG_NRULE = "Rule";
	private static final String TAG_DRULE = "DRule";
	private static final String TAG_RRULE = "RRule";
	private static final String TAG_RULECOND = "Condition";
	private static final String TAG_RULEEXECSEQ = "ExecutionSequence";
	private static final String TAG_RULEDESC = "RuleDescription";
	private static final String TAG_NAME = "name";
	private static final String TAG_MODE = "mode";
	private static final String TAG_GROUP = "group";
	private static final String TAG_PRIOR = "priority";
	private static final String TAG_EVAR = "E-variable";
	private static final String RULECLASS_DEF = "Default";
	private static final String URI_FILE = "file://";
	private static final int RULECLASS_DEFVALUE = 1;
	private static final int RULECLASS_FACTOR = 1000;
	private static final String TAG_SPECVER = "SpecificationVersion";

	public RuleParser(String xmlFile, String className, Configuration config,
			Handler logHandler) {
		super(xmlFile, className, config, logHandler);
		this.logLevel = Tool
				.level(this.config.get(ConfigKey.LEVEL_RULE_PARSER));
		this.rules = new Vector<Rule>();
		this.ruleGroupRegistry = new Properties();
	}

	public static RuleParser createParser(String xmlFile, Configuration config,
			Handler logHandler) {
		// Use an instance of ourselves as the SAX event handler
		RuleParser ruleParser = new RuleParser(xmlFile,
				RuleParser.class.getName(), config, logHandler);
		ruleParser.assignContentHandler(ruleParser);
		return ruleParser;
	}

	private BuiltinManager bm = null;
	private Vector<Node> childNodes = null;
	private RuleHead currentHead = null;
	private RuleBody currentBody = null;
	private Node currentNode = null;
	private Rule currentRule = null;
	private Node parentNode = null;
	private Vector<Rule> rules = null;
	private String specVersion = null;
	private Properties ruleGroupRegistry = null;

	public void characters(char buf[], int offset, int len) throws SAXException {
		// Create the "text" string
		String text = new String(buf, offset, len).trim();
		if (parentNode != null) {
			if (parentNode.getNodeName().equals(RuleParser.TAG_SPECVER)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// The previous element holds this specification value
				specVersion = text;
			} else if (parentNode.getNodeName().equals(RuleParser.TAG_RULEDESC)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// This is the rule description
				currentRule.setRuleDescription(text);
			} else if (!(text.length() == 0)) {
				// Create a new node for the content of the current element
				Node newNode = new Node();
				newNode.setParentNode(parentNode);
				// The content of the current element could be
				if (text.startsWith(XETParser.VAR_E)) {
					// E-variable,
					newNode.setNodeName(text);
					newNode.setNodeType(Node.NT_EVAR);
				} else if (text.startsWith(XETParser.VAR_E1)) {
					// E1-variable,
					newNode.setNodeName(text);
					newNode.setNodeType(Node.NT_E1VAR);
				} else if (text.startsWith(XETParser.VAR_S)) {
					// S-variable,
					newNode.setNodeName(text);
					newNode.setNodeType(Node.NT_SVAR);
				} else {
					// Normal string.
					newNode.setNodeName(null);
					newNode.setNodeType(Node.NT_STRING);
					newNode.setNodeValue(text.toString());
				}
				// Add this node to the parent node.
				parentNode.addChildNode(newNode);
			}
		}
	}

	public void endElement(String namespaceURI, String lName, String qName)
			throws SAXException {
		if (parentNode != null) {
			if (!((lName.equals(RuleParser.TAG_NRULE) && namespaceURI
					.equals(XETParser.XET_URI))
					|| (lName.equals(RuleParser.TAG_RRULE) && namespaceURI
							.equals(XETParser.XET_URI))
					|| (lName.equals(RuleParser.TAG_DRULE) && namespaceURI
							.equals(XETParser.XET_URI)) || (lName
					.equals(XETParser.TAG_XET) && namespaceURI
					.equals(XETParser.XET_URI)))) {

				parentNode = parentNode.getParentNode();
			}
		}
		String poppedContent = null;
	}

	/**
	 * Get all the parsed rules
	 * 
	 * @return The parsed rules
	 */
	public Vector<Rule> getRules() {
		return this.rules;
	}

	/**
	 * Create a builtin manager with the specified path
	 * 
	 * @param builtinPath
	 *            The builtin path to set
	 */
	public void createBuiltinManager(String builtinPath) {
		bm = new BuiltinManager(null, builtinPath);
	}

	public void startDocument() {

	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		// Create a new rule if the current element is Rule node
		// Set the newly created rule to the current rule
		if (lName.equals(XETParser.TAG_XET)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			rules.clear();

		} else if (lName.equals(RuleParser.TAG_INIT)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			// Obtain each "RulePriority"
		} else if (lName.equals(RuleParser.TAG_RULEPRIOR)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode(RuleParser.TAG_RULEPRIOR,
					prefix(namespaceURI), namespaceURI, null, Node.NT_META);
			parseAttribute(attrs, newNode);
			ruleGroupRegistry.setProperty(
					newNode.attributeByLocalName(RuleParser.TAG_GROUP)
							.getNodeValue(),
					newNode.attributeByLocalName(RuleParser.TAG_PRIOR)
							.getNodeValue());
			parentNode = newNode;

		} else if (lName.equals(RuleParser.TAG_META)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			// Determine the version of this specification
		} else if (lName.equals(RuleParser.TAG_SPECVER)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode(RuleParser.TAG_SPECVER,
					prefix(namespaceURI), namespaceURI, null, Node.NT_META);
			parentNode = newNode;
			return;

			// Create a new rule
		} else if (lName.equals(RuleParser.TAG_NRULE)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			currentRule = createRule(Rule.RT_N, prefix(namespaceURI),
					namespaceURI, attrs);
			// Add the rule to the rule database
			rules.add(currentRule);

			// Create a new rule
		} else if (lName.equals(RuleParser.TAG_DRULE)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			currentRule = createRule(Rule.RT_D, prefix(namespaceURI),
					namespaceURI, attrs);

			// Add the rule to the rule database
			rules.add(currentRule);

			// Create a new rule
		} else if (lName.equals(RuleParser.TAG_RRULE)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			currentRule = createRule(Rule.RT_R, prefix(namespaceURI),
					namespaceURI, attrs);
			
			// Add the rule to the rule database
			rules.add(currentRule);
			
			// Include rules from the other file...
		} else if (lName.equals(RuleParser.TAG_INCLUDE)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			include(attrs);

		} else if (lName.equals(RuleParser.TAG_RULEDESC)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode(RuleParser.TAG_RULEDESC,
					prefix(namespaceURI), namespaceURI, null, Node.NT_META);
			parentNode = newNode;
			return;

			// Create a new head from the current element
			// Set the newly created head to the current rule
		} else if (lName.equals(XETParser.TAG_HEAD)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			currentHead = new RuleHead();
			currentRule.addHead(currentHead);
			Node newNode = createNode(XETParser.TAG_HEAD, prefix(namespaceURI),
					namespaceURI, null, Node.NT_HEAD);
			parseAttribute(attrs, newNode);
			if (newNode.hasAttributeByLocalName(RuleParser.TAG_MODE)) {
				currentHead.setMatchingMode(newNode.attributeByLocalName(
						RuleParser.TAG_MODE).getNodeValue());
			}
			parentNode = newNode;

			// Create a body atom place holder
		} else if (lName.equals(XETParser.TAG_BODY)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			currentBody = new RuleBody();
			currentRule.addBody(currentBody);
			Node newNode = createNode(XETParser.TAG_BODY, prefix(namespaceURI),
					namespaceURI, null, Node.NT_BODY);
			parentNode = newNode;

			// Add a condition atom place holder
		} else if (lName.equals(RuleParser.TAG_RULECOND)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode(RuleParser.TAG_RULECOND,
					prefix(namespaceURI), namespaceURI, null, Node.NT_CONDITION);
			parentNode = newNode;

			// Add an execution sequence atom place holder
		} else if (lName.equals(RuleParser.TAG_RULEEXECSEQ)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode(RuleParser.TAG_RULEEXECSEQ,
					prefix(namespaceURI), namespaceURI, parentNode,
					Node.NT_EXECSEQ);
			parentNode = newNode;

			// Allow E-variable declaration in an XML element
			// Example: <xet:E-variable xet:name="Evar_name"/>
			// Added in version 3.0
		} else if (lName.equals(XETParser.TAG_EVAR)
				&& namespaceURI.equals(XETParser.XET_URI)) {

			Node newNode = createNode("", prefix(namespaceURI), namespaceURI,
					parentNode, Node.NT_EVAR);
			parseAttribute(attrs, newNode);
			if (newNode.hasAttributeByLocalName(RuleParser.TAG_NAME)) {
				if (newNode.attributeByLocalName(RuleParser.TAG_NAME)
						.getNodeValue().startsWith(XETParser.VAR_E)) {
					newNode.setNodeName(newNode.attributeByLocalName(
							RuleParser.TAG_NAME).getNodeValue());
				} else {
					logger.severe("[QueryParser] The name of an E-variable must begin with 'Evar'.");
					System.exit(0);
				}
			} else {
				logger.severe("[RuleParser] An E-variable does not have a proper name assigned.");
			}
			parentNode.addChildNode(newNode);
			parentNode = newNode;

		} else {
			int newNodeType;
			if (bm.isBuiltin(namespaceURI, lName)) {
				// The node type is built-in
				newNodeType = Node.NT_BUILTIN;
			} else if (lName.startsWith(XETParser.VAR_N)) {
				// The node type is N-variable
				newNodeType = Node.NT_NVAR;
			} else if (lName.startsWith(XETParser.VAR_I)) {
				// The node type is I-variable
				newNodeType = Node.NT_IVAR;
			} else if (lName.equals("Data")) {
				return;
			} else {
				// The node type is XML-element
				newNodeType = Node.NT_DEFAULT;
			}
			// Create a new node that represents the node type
			Node newNode = createNode(lName, prefix(namespaceURI),
					namespaceURI, parentNode, newNodeType);
			if (parentNode.getNodeName().equals(XETParser.TAG_HEAD)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// This node comes after the "head" of the rule, so it is
				// the head atom
				currentHead.setHeadAtom(newNode);
			} else if (parentNode.getNodeName().equals(XETParser.TAG_BODY)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// This node comes after the "body" of the rule, so it is
				// a body atom
				currentBody.addBodyAtom(newNode);
			} else if (parentNode.getNodeName().equals(RuleParser.TAG_RULECOND)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// This node comes after the "condition" of the rule, so it is
				// a condition atom
				currentRule.addConditionAtom(new RuleCondition(newNode));
			} else if (parentNode.getNodeName().equals(
					RuleParser.TAG_RULEEXECSEQ)
					&& parentNode.getUri().equals(XETParser.XET_URI)) {
				// This node comes after the "execution sequence" of the rule,
				// so it
				// is an execution sequence atom
				currentBody.addExecSeqAtom(newNode);
			} else {
				// This is none of the nodes above. It is just a normal node
				parentNode.addChildNode(newNode);
			}

			// Parse the attributes of the new node
			parseAttribute(attrs, newNode);
			// The new node is now a parent node
			parentNode = newNode;
		}
	}

	/**
	 * The idea of this method is to have a common routine to create a new rule
	 * for both D-type and N-type. This is because since NxET 0.2, D-rule and
	 * N-rule will be separated by specifying DRule or Rule explicitly.
	 * 
	 * @param ruleType
	 *            the rule type determined from startElement
	 * @param element
	 *            the element from startElement
	 * @param attrs
	 *            the attrs from startElement
	 * @return a new rule created
	 * @since NxET 0.2
	 */
	public Rule createRule(int ruleType, String namespacePrefix,
			String namespaceURI, Attributes attrs) {

		Rule newRule = new Rule();
		// currentRule = rule;
		newRule.setSpecVersion(specVersion);

		Node newNode = createNode(RuleParser.TAG_RULEPRIOR, namespacePrefix,
				namespaceURI, null, Node.NT_META);
		parseAttribute(attrs, newNode);

		// Determine the name of this rule
		if (newNode.hasAttributeByLocalName(RuleParser.TAG_NAME)) {
			newRule.setRuleName(newNode.attributeByLocalName(
					RuleParser.TAG_NAME).getNodeValue());
		} else {
			newRule.setRuleName("N/A");
		}

		// Determine the rule class
		try {
			newRule.setRuleClass(newNode.attributeByLocalName(
					RuleParser.TAG_GROUP).getNodeValue());
		} catch (NullPointerException e) {
			// User does not specify the rule class
			newRule.setRuleClass(RuleParser.RULECLASS_DEF);
		}
		// Set rule type
		newRule.setRuleType(ruleType);
		// Determine rule priority
		if (newNode.hasAttributeByLocalName(RuleParser.TAG_PRIOR)) {
			newRule.setRulePriority(Integer.parseInt(newNode
					.attributeByLocalName(RuleParser.TAG_PRIOR).getNodeValue()));
		} else {
			newRule.setRulePriority(0);
		}
		// If there is a rule class specified, calculate the rule priority
		if (newNode.hasAttributeByLocalName(RuleParser.TAG_GROUP)
				&& newNode.getUri().equals(XETParser.XET_URI)) {

			String ruleGroupName = newNode.attributeByLocalName(
					RuleParser.TAG_GROUP).getNodeValue();
			int ruleGroupPriority = RuleParser.RULECLASS_DEFVALUE;
			try {
				ruleGroupPriority = Integer.parseInt(ruleGroupRegistry
						.getProperty(ruleGroupName));
			} catch (NumberFormatException e) {
				// Use the rule class default value
			}
			newRule.setRuleClass(ruleGroupName);
			newRule.setRuleClassPriority(ruleGroupPriority
					* RuleParser.RULECLASS_FACTOR);

			// If there is no rule class specified, use the default rule class
			// priority value
		} else {
			newRule.setRuleClass(RuleParser.RULECLASS_DEF);
			newRule.setRuleClassPriority(RuleParser.RULECLASS_DEFVALUE
					* RuleParser.RULECLASS_FACTOR);
		}
		return newRule;

	}

	/**
	 * Sort the rule vector by the effective priority of the rules
	 */
	public void sortRule() {
		int len = (this.rules != null) ? this.rules.size() : 0;
		for (int i = 0; i < len; i++) {
			int count = this.rules.size();
			int j = 0;
			while (j < count) {
				if (this.rules.get(i).effectivePriority() < this.rules.get(j)
						.effectivePriority()) {
					break;
				}
				j++;
			}
			if (i != j) {
				rules.insertElementAt(rules.get(i), j);
				if (i > j) {
					rules.remove(i + 1);
				} else {
					rules.remove(i);
				}
			}
		}
	}

	/**
	 * Include another rule file.
	 * 
	 * @param namespacePrefix
	 *            The namespace prefix used to create a dummy node to extract
	 *            the parameter from the "include" node. The prefix is not used.
	 * @param namespaceURI
	 *            The namespace URI used to create a dummy node to extract the
	 *            parameter from the "include" node. The URI is not used.
	 * @param attrs
	 *            The attributes holding information about the URL of the
	 */
	private void include(Attributes attrs) {
		String uri = null;
		for (int i = 0; i < attrs.getLength(); i++) {
			if (attrs.getLocalName(i).equals(RuleParser.TAG_URI)) {
				uri = attrs.getValue(i);
			}
		}
		if (uri == null) {
			System.err
					.println("The required attribute \"uri\" can not be found.");
		} else if (!uri.startsWith(RuleParser.URI_FILE)) {
			System.err.println("The requested URI " + uri
					+ " is not yet supported.");
		}
		logger.log(logLevel, "Included file   : " + uri);
		String ruleFile = uri.substring(RuleParser.URI_FILE.length());
		// Check if the path to rule file is relative or absolute
		File rf = new File(ruleFile);
		if (!rf.isAbsolute()) {
			File originalRuleFile = new File(xmlFile);
			String originalPath = originalRuleFile.getParentFile()
					.getAbsolutePath();
			ruleFile = originalPath + File.separator + rf.getPath();
		}
		String currentRuleParserConfig = this.config
				.get(ConfigKey.LEVEL_RULE_PARSER);
		this.config.set(ConfigKey.LEVEL_RULE_PARSER, "Finest");
		// Create a rule parser
		RuleParser ruleParser = RuleParser.createParser(ruleFile, this.config,
				this.logHandler);
		// Load built-in
		ruleParser.createBuiltinManager(bm.getBuiltinPath());
		// Parse the rule file
		ruleParser.parse();
		// Obtain the parsed rule set (Vector object)
		this.rules.addAll(ruleParser.getRules());
		this.config.set(ConfigKey.LEVEL_RULE_PARSER, currentRuleParserConfig);
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		uriPrefixMapper.put(uri, prefix);
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void endDocument() throws SAXException {
		StringBuffer sb = new StringBuffer();
		sb.append(Tool.LINE + "\n");
		sb.append("Parsed rule(s) : " + this.xmlFile + "\n");
		sb.append(Tool.LINE);
		for (int i = 0; i < this.rules.size(); i++) {
			sb.append("\n" + ((Rule) this.rules.get(i)).toString());
		}
		logger.log(logLevel, sb.toString());
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
	}

}