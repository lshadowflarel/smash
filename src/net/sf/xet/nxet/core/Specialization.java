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

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Paramai Supadulchai
 */

public class Specialization implements Cloneable {

	public static final String END_OF_IVAR = "EndOfIvar";

	private HashMap<String, Object> variableValueMap = null;

	public Specialization() {
		variableValueMap = new HashMap<String, Object>();
	}

	public Specialization(HashMap<String, Object> variableValueMap) {
		this.variableValueMap = variableValueMap;
	}

	public void addSpecialization(String variableName, Object value) {
		variableValueMap.put(variableName, value);
	}

	public void clear() {
		variableValueMap.clear();
	}

	public Object clone() {
		Specialization s = new Specialization(
				(HashMap<String, Object>) (variableValueMap.clone()));
		return s;
	}

	public Specialization cloneSpecialization() {
		return (Specialization) (this.clone());
	}

	public boolean containVariable(String variableName) {
		return variableValueMap.containsKey(variableName);
	}

	public HashMap getVariableMap() {
		return this.variableValueMap;
	}

	public Object instantiate(String variableName) {
		return variableValueMap.get(variableName);
	}

	public void instantiateE1var(Node n, Node v) {

		// This is done by transfer all properties of the "v" node
		// All attributes that v node has are also cloned.
		n.setNodeName(v.getNodeName());
		n.setNamespace(v.getNamespace());
		n.setUri(v.getUri());
		n.setNodeType(v.getNodeType());
		n.setNodeValue(v.getNodeValue());

		// Copy attributes
		Iterator attrs = v.attributeKey();
		while (attrs.hasNext()) {
			String attrName = (String) attrs.next();
			n.addAttribute(v.attribute(attrName));
			// n.addAttribute(v.attribute(attrName).cloneNode());
		}

		// Copy all possible child nodes
		for (int i = 0; i < v.numberOfChildNodes(); i++) {
			Node newChildNode = v.childNode(i).cloneNode();
			newChildNode.setParentNode(n);
			n.addChildNode(newChildNode);
			// n.addChildNode(v.childNode(i).cloneNode());
		}

	}

	public void instantiateEvar(Node n, Node v) {

		// Tool.out("Node n = " + n.getNodeName());
		// Tool.out("Node v = " + v.getNodeName());

		// if ((v.getNodeType() == Node.NT_EVAR) && (v.numberOfChildNodes() ==
		// 0)) {

		// This means the target V-variable does not contain anything.
		// Tool.out("Two evar ...");
		// n.setNodeName(v.getNodeName());
		// n.setNamespace(v.getNamespace());
		// return;
		// }
		Node parentNode = n.getParentNode();

		// Tool.out("Get parent node " + parentNode.getNodeName());
		int i = 0;
		if (parentNode.numberOfChildNodes() == 0) {

			for (int j = 0; j < v.numberOfChildNodes(); j++) {
				Node newNode = v.childNode(j).cloneNode();
				newNode.setParentNode(parentNode);
				parentNode.addChildNode(newNode);
			}
			return;
		}

		while (i < parentNode.numberOfChildNodes()) {
			Node currentNode = parentNode.childNode(i);

			if (currentNode.fullName().equals(n.fullName())) {

				// Remove e-variable
				parentNode.removeNode(i);

				// Add new nodes here
				for (int j = 0; j < v.numberOfChildNodes(); j++) {
					Node newNode = v.childNode(j).cloneNode();
					newNode.setParentNode(parentNode);
					parentNode.insertNode(i, newNode);

					i++;
				}
			}
			i++;
		}
	}

	public void instantiateNvar(Node n, String v) {
		n.setNodeName(v);
		n.setNodeType(Node.NT_DEFAULT);
	}

	public void instantiateSvar(Node n, String v) {

		if (!n.isAttributeNode()) {
			n.setNodeName(null);
		}
		n.setNodeType(Node.NT_STRING);
		n.setNodeValue(v);
	}

	/**
	 * Instantiate P-variable
	 * 
	 * @param n
	 *            the P-variable to be instaniated
	 * @param v
	 *            the P-variable with the value for instantiation
	 * @since NxET 0.2
	 */
	public void instantiatePvar(Node pVar, Node value) {

		// Get the parent of the current P variable
		Node parentNode = pVar.getParentNode();

		// Remove "P-variable"
		parentNode.removeAttribute(pVar.fullName());

		// Add attributes from the specialization (v node)
		for (int i = 0; i < value.numberOfChildNodes(); i++) {

			Node attribute = new Node();

			attribute.setAttributeNode(true);
			attribute.setNodeName(value.childNode(i).getNodeName());
			attribute.setNamespace(value.childNode(i).getNamespace());
			attribute.setNodeType(value.childNode(i).getNodeType());
			attribute.setNodeValue(value.childNode(i).getNodeValue());
			attribute.setParentNode(parentNode);
			attribute.setUri(value.childNode(i).getUri());

			parentNode.addAttribute(attribute);

		}

	}

	public void instantiateIvar(Node iVar, Node value) {
		Node iVarValue = value.cloneNode();

		// Get the parent of the ivar node
		Node parentOfIvar = iVar.getParentNode();

		if (parentOfIvar == null) {
			parentOfIvar = iVarValue.childNode(0).cloneNode();
		} else {
			Node childOfIvar = new Node();

			for (int i = 0; i < parentOfIvar.numberOfChildNodes(); i++) {
				parentOfIvar.removeNode(i);
			}

			for (int j = 0; j < iVarValue.numberOfChildNodes(); j++) {
				childOfIvar = iVarValue.childNode(j).cloneNode();
				parentOfIvar.insertNode(j, childOfIvar);
			}
		}

		// Search for the "EndOfIvar" from the current I-variable
		Node endOfIvar = searchEndOfIvar(parentOfIvar);

		endOfIvar.replaceSelf(iVar.childNode(iVar.numberOfChildNodes() - 1));
	}

	/**
	 * Search for the node having its fullname "EndOfIvar"
	 * 
	 * @param v
	 *            the node to search
	 * @return the node pointer
	 */
	public Node searchEndOfIvar(Node iVarNode) {

		try {

			if (iVarNode.fullName().equals(Specialization.END_OF_IVAR)) {

				return iVarNode;

			} else {

				for (int i = 0; i < iVarNode.numberOfChildNodes(); i++) {

					Node child = searchEndOfIvar(iVarNode.childNode(i));

					if (child != null) {

						return child;

					}

				}

			}

		} catch (NullPointerException e) {

			return null;

		}

		return null;

	}

	public int instantiateVariables(Node n) {

		int numberOfUninstantiatedVariables = 0;

		if (n.isVariable()) {
			Object v = (n.isAttributeNode() && (n.nodeType != Node.NT_PVAR)) ? instantiate(n
					.getNodeValue())
					: (instantiate(n.fullName()) == null ? instantiate(n
							.getNodeName()) : instantiate(n.fullName()));

			if (v != null) {

				switch (n.getNodeType()) {
				// Instantiate S_variable
				case Node.NT_SVAR:

					String sVarString = (String) v;

					if (sVarString.startsWith("Svar")) {
						if (n.isAttributeNode()) {
							n.setNodeValue(sVarString);
						} else {
							n.setNodeName(sVarString);
						}

						instantiateVariables(n);
					} else {
						instantiateSvar(n, sVarString);
					}
					break;
				// Instantiate N_varaible
				case Node.NT_NVAR:
					String nVarString = (String) v;
					if (nVarString.startsWith("Nvar")) {
						n.setNodeName(nVarString);
						instantiateVariables(n);
					} else {
						instantiateNvar(n, nVarString);
					}
					break;
				// Instantiate E1_variable
				case Node.NT_E1VAR:
					if (v.getClass().getSimpleName().equals("Node")) {
						instantiateE1var(n, (Node) v);
					} else if (v.getClass().getSimpleName().equals("String")) {
						n.setNodeName((String) v);
						instantiateVariables(n);
					}
					break;
				// Instantiate E_varaible
				case Node.NT_EVAR: 
					if (v.getClass().getSimpleName().equals("Node")) {
						instantiateEvar(n, (Node) v);
					} else if (v.getClass().getSimpleName().equals("String")) {
						n.setNodeName((String) v);
						instantiateVariables(n);
					}
					break;
				case Node.NT_PVAR:
					if (v.getClass().getSimpleName().equals("Node")) {
						instantiatePvar(n, (Node) v);
					} else if (v.getClass().getSimpleName().equals("String")) {
						Node newAttribute = n;
						n.getParentNode().removeAttribute(n.fullName());
						n.setNodeName((String) v);
						n.getParentNode().addAttribute(newAttribute);
						instantiateVariables(n);
					}
					break;
				case Node.NT_IVAR:
					if (v.getClass().getSimpleName().equals("Node")) {
						instantiateIvar(n, (Node) v);
					} else if (v.getClass().getSimpleName().equals("String")) {
						n.setNodeName((String) v);
						instantiateVariables(n);
					}
					break;
				default:
					System.out.println("Not yet implemented");
				}

			} else {
				numberOfUninstantiatedVariables++;

			}

		}

		// Instantiate child nodes
		int noOfOriginalChildNode = n.numberOfChildNodes();
		int noOfNewChildNode = n.numberOfChildNodes();
		for (int i = 0; i < noOfNewChildNode; i += (noOfNewChildNode
				- noOfOriginalChildNode + 1)) {
			noOfOriginalChildNode = n.numberOfChildNodes();
			numberOfUninstantiatedVariables += instantiateVariables(n
					.childNode(i));
			noOfNewChildNode = n.numberOfChildNodes();
		}

		// Instantiate all attributes
		Iterator attrs = n.attributeKey();
		String[] attributeKey = new String[n.numberOfAttributes()];
		int i = 0;
		while (attrs.hasNext()) {
			attributeKey[i] = (String) attrs.next();
			i++;
		}

		for (i = 0; i < attributeKey.length; i++) {
			numberOfUninstantiatedVariables += instantiateVariables(n
					.attribute(attributeKey[i]));
		}

		return numberOfUninstantiatedVariables;

	}

	public boolean isEmpty() {

		return this.variableValueMap.isEmpty();

	}

	/**
	 * Merge two specialization object together. All the variable-object pair
	 * from the target specialization will be copied.
	 * 
	 * @param spec
	 *            the specialization to merge with
	 * 
	 */
	public void merge(Specialization spec) {
		Iterator it = spec.getVariableMap().keySet().iterator();
		while (it.hasNext()) {
			String variable = (String) it.next();
			variableValueMap.put(variable, spec.getVariableMap().get(variable));
		}
	}

	public String printSpecialization() {
		StringBuffer sb = new StringBuffer();
		Iterator it = this.variableValueMap.keySet().iterator();
		while (it.hasNext()) {
			String variableName = (String) it.next();
			Object variableValue = variableValueMap.get(variableName);
			if (variableValue.getClass().getSimpleName().equals("Node")) {
				Node nodeValue = (Node) variableValue;
				if ((nodeValue.getNodeType() == Node.NT_EVAR)
						|| (nodeValue.getNodeType() == Node.NT_E1VAR)) {
					sb.append("(" + variableName + " , \n");
					for (int i = 0; i < nodeValue.numberOfChildNodes(); i++) {
						sb.append(nodeValue.childNode(i).toString() + "\n");
					}
					sb.append(")\n");
				} else if (nodeValue.getNodeType() == Node.NT_DEFAULT) {
					sb.append("(" + variableName + " , \n"
							+ nodeValue.toString() + ")\n");
				} else if (nodeValue.getNodeType() == Node.NT_PVAR) {
					sb.append("(" + variableName + " , (");
					for (int i = 0; i < nodeValue.numberOfChildNodes(); i++) {
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(nodeValue.childNode(i).fullName());
						sb.append("=\"");
						sb.append(nodeValue.childNode(i).nodeValue);
						sb.append("\"");
					}
					sb.append("))");
				} else if (nodeValue.getNodeType() == Node.NT_IVAR) {
					sb.append("(" + variableName + " , " + nodeValue.toString()
							+ ")\n");
				} else {
					sb.append("Not support for the variable " + variableName
							+ " which is of " + nodeValue.nodeTypeText()
							+ " type.\n");
				}
			} else {
				sb.append("(" + variableName + " , \""
						+ variableValue.toString() + "\")\n");
			}
		}
		return sb.toString();
	}

	public void removeSpecialization(String variableName) {
		variableValueMap.remove(variableName);
	}

}
