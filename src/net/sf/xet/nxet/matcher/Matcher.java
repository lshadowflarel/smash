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

package net.sf.xet.nxet.matcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;

/**
 * Matcher matches a body atom with the head atom of a rule.
 * Matcher works in three modes:<br>
 * <br>
 * - Mode "sequential" determistic<br>
 * - Mode "sequantial" non-deterministic<br>
 * - Mode "set"<br>
 * <br>
 * In the set mode, there is only E-variable allowed in the
 * atom. In the sequential mode, more E-variables are allowed.
 * However, determistic means the first E-variable takes more
 * priority. This means that the first E-variable will never
 * take "NULL" value.
 * 
 * @author Paramai Supadulchai
 */
public class Matcher {
    
    public static final int MODE_SEQ_D = 0;
    public static final int MODE_SEQ_N = 1;
    public static final int MODE_SET = 2;
    public static final int MODE_MAP = 3;
    
    public static String[] MODE = new String[] { 
            "SequenceD",
            "SequenceN",
            "Set",
            "Map"
            };
    
    Configuration config = null;
    boolean debug = false;
    
    Level logLevel = null;
    Logger logger = Logger.getLogger(Matcher.class.getName());
    
    /**
     * Constructor to create a new matcher class with a
     * configuration object and a printstream out. The
     * configuration object determines how the output
     * can be logged. The output printstream is where
     * the matcher class print its logging output.
     * 
     * 
     * @param config
     * @param out
     */
    public Matcher(Configuration config) {
        this.config = config;
    }
    
    /**
     * The only public method here is "match". Head is
     * normally a head atom of the rule or parts
     * of a head atom of the rule. The target is a 
     * target body atom to match with the head atom.
     * 
     * @param head The head atom or the part of the head atom
     * @param target The target atom or the part of the target atom
     * @return Returns the match result
     */
    
    /*
     *  change the rule variables to make the different between the same name variables in 
     *  different rules(for recursive definition)
     *  change by add time to the end of the variable name
     */
    public MatchResult match(Node head, Node target, int mode) {
    	switch (head.getNodeType()) {
        	// Match XML elements
        	case Node.NT_NVAR:
        	case Node.NT_DEFAULT: 
        	    return matchDefault(head, target, mode);
        	// Match string nodes
        	case Node.NT_STRING:
        	    return matchString(head, target);
        	// Match S-variable
        	case Node.NT_SVAR:
        	    return matchSvar(head, target);
        	// Match E1-variable
        	case Node.NT_E1VAR:
        	    return matchE1var(head, target);
        	// Match E-variable
        	case Node.NT_EVAR: 
        	    return matchMultipleEvar(head, target, mode);
        	// Match I-variable
        	case Node.NT_IVAR:
        	    return matchIvar(head, target, mode);
        	default:
        	    System.err.println("Matching with unrecognized node type " + head.nodeTypeText());
        }
        return new MatchResult(false);
    }    
        
    private MatchResult matchAttribute(String attrName, Node head, Node target) {
    	MatchResult matchResult = new MatchResult(true);
        
        try {
            
            // Head's NAME = Target's NAME and both VALUES are S-variables
            if (head.fullName().equals(target.fullName()) && 
                head.getNodeValue().startsWith("Svar") &&
                target.getNodeValue().startsWith("Svar")) {
        	    
                // Add variable-rewriting if the S-variables are not the same
                if (!head.getNodeValue().equals(target.getNodeValue())) {
                				matchResult.getSpecialization().addSpecialization(
                                head.getNodeValue(),
                            	target.getNodeValue()
    	                		);
                }
                matchResult.setSuccess(true);
        	// Head's NAME = Target's NAME and head's VALUE is an S-variable    
        	} else if (head.fullName().equals(target.fullName()) && 
        	        head.getNodeValue().startsWith("Svar")) {
        		// The attribute of the head contains S-variable
        	    matchResult.getSpecialization().addSpecialization(
        	            	head.getNodeValue(), 
        	                target.getNodeValue()
        	                );
        	    matchResult.setSuccess(true);
        	// Head's NAME = Target's NAME and target's VALUE is an S-variable
            } else if (head.fullName().equals(target.fullName()) && 
                       target.getNodeValue().startsWith("Svar")) {
            	
                // The attribute of the head contains S-variable
                matchResult.getSpecialization().addSpecialization(
                            target.getNodeValue(), 
                            head.getNodeValue()
                            );
                matchResult.setSuccess(true);
            // Head's NAME = Target's NAME and head's VALUE = target's VALUE 
            } else if (head.fullName().equals(target.fullName()) &&
                       head.getNodeValue().equals(target.getNodeValue())) {
                
                matchResult.setSuccess(true);
            } else {
                
                // Can quit immediately
                matchResult.setSuccess(false);
                return matchResult;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            // Trying to match an attribute with nothing...
            matchResult.setSuccess(false);
            return matchResult;
        }
        return matchResult;
    }
    
    private MatchResult matchDefault(Node head, Node target, int mode) {
        
        MatchResult matchResult = new MatchResult(false);
        MatchResult newMatchResult = new MatchResult(true);
        
        // This node is a "default" XML expression, which includes
        // 1. The target node is an I-variable
        // 2. XML Element
        // 3. N-Variable
        
    	if (target.getNodeType() == Node.NT_IVAR) { 
    	    
    		return match(target, head, mode);
    		
    	} else if ((head.fullName().equals(target.fullName())) || 
	              ((head.getNodeType() == Node.NT_NVAR) && (target.getNodeType() == Node.NT_DEFAULT)) ||
	              ((head.getNodeType() == Node.NT_NVAR) && (target.getNodeType() == Node.NT_NVAR))) { 
	        
	        matchResult.setSuccess(true);
	        if (head.getNodeType() == Node.NT_NVAR) {
	            if ((target.getNodeType() != Node.NT_NVAR) && (!target.isBuiltin())) {
	                MatchResult attrResult = matchPvar(head, target);
	                if (attrResult.isSuccess()) {
	                    matchResult.getSpecialization().addSpecialization(head.fullName(), target.fullName() );
	                    matchResult.merge(attrResult.getSpecialization());
	                } else {
	                    return attrResult;
	                }
                // Both are N-variables
	            } else if (target.getNodeType() == Node.NT_NVAR) {
	                // Add a variable-rewriting if both variables are not the same
	                MatchResult attrResult = matchPvar(head, target);
	                if (attrResult.isSuccess()) {
	                    matchResult.merge(attrResult.getSpecialization());
	                    if (!head.fullName().equals(target.fullName())) {
	                        matchResult.getSpecialization().addSpecialization(head.fullName(), target.fullName());
		                }
	                } else {
	                    return attrResult;
	                }
	            }
	        } else { 
	        	// Check all attributes
		        MatchResult attrResult = matchPvar(head, target);
		        matchResult.merge(attrResult);
		        
	        }
	        
	        // If the attributes cannot be matched, both nodes also
	        // cannot be matched. The executor also can quit immediately.
	        if (!matchResult.isSuccess()) {
	            return matchResult;
	        }
	        matchResult.setSuccess(true);
	        // Determine whether to work in a sequential mode or not
	        if (mode == Matcher.MODE_SEQ_D) {
	        	// Check all child nodes (sequential mode)
	            int j = 0;
	            if ((head.numberOfChildNodes() == 0) && (target.numberOfChildNodes() != 0)) {
	                matchResult.setSuccess(false);
	                return matchResult;
	            }
	            
	            for (int i = 0; i < head.numberOfChildNodes(); i++) {
		            try {
		            	MatchResult childResult = null;
		            	childResult = match(head.childNode(i), target.childNode(j), mode);
		            	
		            	// Skip some nodes that have been matched by E-variable
		                if ((head.childNode(i).getNodeType() == Node.NT_EVAR) || (head.childNode(i).getNodeType() == Node.NT_IVAR)) {
		                	// In case of EVar matching return multiple results
		                	if(childResult.isMultipleMatchingResults()) {
		                		
			                    if((childResult.getInstances() != null) && (childResult.getInstances().size()>0)) {
			                    	for(int k=0; k<childResult.getInstances().size(); k++) {
			                    		Object evarObject = childResult.getInstances().get(k).getSpecialization().instantiate(
         					   				head.childNode(i).fullName());
			                    		
			                    		if ((evarObject != null) && 
					                        (evarObject.getClass().getSimpleName().equals("Node"))) {
			                    								                    
			                    			Node evarInstantiation = (Node)evarObject;
					                        if (evarInstantiation != null) {
						                        j += evarInstantiation.numberOfChildNodes();
						                    }
					                    } else {
						                    // Otherwise, just inspect the next target node next time
						                	j++;
						                }					                    					                    
			                    	}
			                    }
			                    
			                    // Exit for loop
			                    if ((target.childNode(i).getNodeType() != Node.NT_EVAR) || 
			                    	(head.childNode(i).getNodeType() == Node.NT_IVAR)) {
				                    newMatchResult = new MatchResult(true);
					                if (matchResult.isSuccess()) {
					                	newMatchResult.setMultipleMatchingResults(true);
						                
					                	//matchResult.merge(childResult);
					                	if((childResult.getInstances() != null) && (childResult.getInstances().size()>0)) {
						                    for(int k=0; k<childResult.getInstances().size(); k++) {
					                    		MatchResult matchResultTemp = null;
					                    		
					                    		if(matchResult.isMultipleMatchingResults()) {
					                    			for(int m=0; m<matchResult.getInstances().size(); m++) {
					                    				matchResultTemp = (MatchResult) childResult.getInstances().get(k);
					                    				matchResultTemp.getSpecialization().merge(matchResult.getInstances().get(m).getSpecialization());
					                    				newMatchResult.addNewInstance(matchResultTemp.clone());							                    								                    	
					                    			}	
					                    		} else {
					                    			matchResultTemp = (MatchResult) childResult.getInstances().get(k);
					                    			matchResultTemp.getSpecialization().merge(matchResult.getSpecialization());
					                    			newMatchResult.addNewInstance(matchResultTemp.clone());
					                    		}
						                    }
						                }					                	
					                }
					                
				                    return newMatchResult;
			                    }
			                    			                    
		                	} else {
		                		Object evarObject = childResult.getSpecialization().instantiate(
	 					   				head.childNode(i).fullName());		                	
			                    if ((evarObject != null) && 
			                        (evarObject.getClass().getSimpleName().equals("Node"))) {
			                    	
			                    	Node evarInstantiation = (Node)evarObject;
			                        if (evarInstantiation != null) {
				                        j += evarInstantiation.numberOfChildNodes();
				                    }
			                    } else {
				                    // Otherwise, just inspect the next target node next time
				                	j++;
				                }
			                    
			                    // Exit for loop
			                    if (target.childNode(i).getNodeType() != Node.NT_EVAR) {
				                    
					                if (matchResult.isSuccess()) {
			                    		MatchResult matchResultTemp = null;
			                    		newMatchResult = new MatchResult(true);
			                    		
			                    		if(matchResult.isMultipleMatchingResults()) {
			                    			newMatchResult.setMultipleMatchingResults(true);
			                    			for(int m=0; m<matchResult.getInstances().size(); m++) {
			                    				matchResultTemp = (MatchResult) matchResult.getInstances().get(m);
			                    				matchResultTemp.getSpecialization().merge(childResult.getSpecialization());
			                    				newMatchResult.addNewInstance(matchResultTemp.clone());					                    								                    		
			                    			}
			                    			return newMatchResult;
			                    		} else {
			                    			matchResult.merge(childResult);
			                    			return matchResult;				                    			
			                    		}
					               }
			                    }		                		
		                	}
		                    
		                } else {
		                    // Otherwise, just inspect the next target node next time
		                	j++;
		                }
		                
		                newMatchResult = new MatchResult(true);
		                if(childResult.isMultipleMatchingResults()) {
		                	newMatchResult.setMultipleMatchingResults(true);
		                	
		                	if((childResult.getInstances() != null) && (childResult.getInstances().size()>0)) {
		                		for(int k=0; k<childResult.getInstances().size(); k++) {
		                    		MatchResult matchResultTemp = null;
		                    		
		                    		if(matchResult.isMultipleMatchingResults()) {
		                    			for(int m=0; m<matchResult.getInstances().size(); m++) {
		                    				matchResultTemp = (MatchResult) childResult.getInstances().get(k);
		                    				matchResultTemp.getSpecialization().merge(matchResult.getInstances().get(m).getSpecialization());
		                    				newMatchResult.addNewInstance(matchResultTemp.clone());
				                    	}
		                    			
		                    		} else {
		                    			matchResultTemp = (MatchResult) childResult.getInstances().get(k);
		                    			matchResultTemp.getSpecialization().merge(matchResult.getSpecialization());
		                    			newMatchResult.addNewInstance(matchResultTemp.clone());		                    							                    		
		                    		}
		                    		
				                }					                	
		                		matchResult = newMatchResult.clone();
			                	
		                	}
		                } else {
		                	MatchResult matchResultTemp = null;
		                	
		                	if(matchResult.isMultipleMatchingResults()) {
                    			newMatchResult.setMultipleMatchingResults(true);
                    			for(int m=0; m<matchResult.getInstances().size(); m++) {
                    				matchResultTemp = (MatchResult) matchResult.getInstances().get(m);
                    				matchResultTemp.getSpecialization().merge(childResult.getSpecialization());
                    				newMatchResult.addNewInstance(matchResultTemp.clone());		                    								                    	
                    			}
                    			matchResult = newMatchResult.clone();
                    		} else {
                    			matchResult.merge(childResult);
                    						                    		
                    		}
		                }
		                
		                if (!matchResult.isSuccess()) {
		                	// Can quit immediately
		                    return matchResult;
		               }
		            } catch (ArrayIndexOutOfBoundsException e) {
		                // Evar can match "nothing"
		                if (head.childNode(i).getNodeType() == Node.NT_EVAR) {
		                    matchResult.setSuccess(true);
		                    Node evarHead = new Node();
		                    evarHead.setNodeName(head.childNode(i).getNodeName());
		                    evarHead.setNamespace(head.childNode(i).getNamespace());
		                    evarHead.setUri(head.childNode(i).getUri());
		                    evarHead.setNodeType(Node.NT_EVAR);
		                    matchResult.getSpecialization().addSpecialization(evarHead.fullName(), evarHead);
		                    return matchResult;
		                } else {
		                    matchResult.setSuccess(false);
		                    return matchResult;
		                }
		            } catch (Exception e) {
		                e.printStackTrace();
		                matchResult.setSuccess(false);
		                return matchResult;
		            }
		        }
	        } else { 
	        	// set mode
	            MatchResult setResult = matchEvarSet(head, target, mode);
	            matchResult.merge(setResult);
	        }
	        return matchResult;
	    } else {
	        matchResult.setSuccess(false);
	        return matchResult;
	    }
    }
    
    
    private MatchResult matchE1var(Node head, Node target) {
        
        MatchResult matchResult = new MatchResult(false);
        
        // This node is an XML-variable of type "E1-Variable"
	    // It can be matched with a Node
	    // For example, E1var_aNode1 can be matched with <Node1>Value1</Node1>
	    if (target.getNodeType() == Node.NT_DEFAULT) {
	        try {
	            matchResult.getSpecialization().addSpecialization(head.fullName(), target);
	        } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	// Both are E1-variables
	    } else if (target.getNodeType() == Node.NT_E1VAR) {
	        if (!head.fullName().equals(target.fullName())) {
	            matchResult.getSpecialization().addSpecialization(head.fullName(), target.fullName());
	        }
	        matchResult.setSuccess(true);
	        return matchResult;
	    }
	    matchResult.setSuccess(target.getNodeType() == Node.NT_DEFAULT);
	    return matchResult;
        
    }
    
    /**
     * This function match all child nodes of a head and a target
     * as "SET". Only 1 E-variable is allowed within the head and
     * the target.
     * 
     * @param head The head (more generalized) node
     * @param target The target node
     * @param mode The matching mode ("SET" mode can be assumed though) 
     * @return Returns the matching result
     */
    private MatchResult matchEvarSet(Node head, Node target, int mode) {
        
        MatchResult matchResult = new MatchResult(true);
        
        // Do nothing if the head and the target don't contain any attribute
        if (!head.hasChildNodes() && !target.hasChildNodes()) {
            return matchResult;
        }
        
        Vector<Node> targetChildNodes = new Vector<Node>();
        
        // Re-add all child nodes to the new target child nodes
        for (int i = 0; i < target.numberOfChildNodes(); i++) {
            targetChildNodes.add(target.childNode(i));
        }
        
        Node evarHead = new Node();
        String targetEvarName = null;
        
        // Gradually match the head and the target
        for (int i = 0; i < head.numberOfChildNodes(); i++) {
            
            // If the node is Evar, just keep the name
            if (head.childNode(i).getNodeType() == Node.NT_EVAR) {
                evarHead.setNodeName(head.childNode(i).getNodeName());
                evarHead.setNodeType(head.childNode(i).getNodeType());
                evarHead.setNamespace(head.childNode(i).getNamespace());
                evarHead.setUri(head.childNode(i).getUri());
            } else {
                // Try to match the current head node with all target nodes
                boolean found = false;
                for (int j = 0; j < targetChildNodes.size(); j++) {
                    if (targetChildNodes.get(j).getNodeType() == Node.NT_EVAR) {
                        targetEvarName = targetChildNodes.get(j).fullName();
                        continue;
                    }
                    // Match the current head node with the current target child node
                    MatchResult childResult = match(head.childNode(i), (Node)targetChildNodes.get(j), mode);
                    found = childResult.isSuccess();
                    // If the head node can be matched with the current target child node
                    if (found) {
                        // Merge the specialization
                        matchResult.merge(childResult.getSpecialization());
                        // Remove the current target node from the list because
                        // there is no need to match the current target node again
                        targetChildNodes.remove(j);
                        // break from the loop
                        break;
                    }
                    
                }
                // If the found variable has never been set to TRUE,
                // this means the current head cannot be matched with
                // any target node. So, false can be returned immediately.
                if (!found) {
                    matchResult.setSuccess(false);
                    return matchResult;
                }
            }
        }
        // If all head child node can be matched with all target child nodes
        // The target child nodes left is therefore added to the E-variable
        for (int i = 0; i < targetChildNodes.size(); i++) {
            evarHead.addChildNode(targetChildNodes.get(i).cloneNode());
        }
        // Add the e-variable when it contains some child nodes
        if (evarHead.numberOfChildNodes() > 0) {
            matchResult.getSpecialization().addSpecialization(evarHead.fullName(), evarHead);
        }
        // Add also the variable name rewriting if the target node contains
        // a E-variable child node
        if ((targetEvarName != null) &&(evarHead.getNodeName() != null)) {
            matchResult.getSpecialization().addSpecialization(evarHead.fullName(), targetEvarName);
        }
        matchResult.setSuccess(true);
        return matchResult;
    }
    
    
    private MatchResult matchMultipleEvar(Node head, Node target, int mode) {
        Node headParentSet = head.getParentNode();
        Node targetParentSet = target.getParentNode();
        
        MatchResult matchResultPartial = new MatchResult();
        MatchResult matchResultAll = new MatchResult();
        
        // This should apply to matching head of query with head of rule as well.
        // Both are E-variables
        if ((head.getNodeType() == Node.NT_EVAR) && (target.getNodeType() == Node.NT_EVAR)) {
            if (head.fullName().equals(target.fullName())) {
        		matchResultAll.setSuccess(true);
        		//matchResultAll.getSpecialization().addSpecialization(head.fullName(), target.fullName());
            }
        	
            return matchResultAll;
        } 
        // Only 1 Evar in head should match with all target elements
        else if((head.getNodeType() == Node.NT_EVAR) && (headParentSet.numberOfChildNodes() == 1)) {
        	
        	return matchEvar(head, target, mode);
        }

        int headIndex = 0;
        int targetIndex = 0;
        int noOfHeadElement = headParentSet.numberOfChildNodes();
        boolean foundXmlConst = false;
        boolean lastNodeIsEVar = false;
        
        Vector<Integer> listOfXmlConst = new Vector<Integer>();
        Vector<HashMap> matchResultXmlConstantPartial = new Vector<HashMap>();
        Vector<Vector> matchResultXmlConstantAll = new Vector<Vector>();        
        
        // First, find a set of XML constant
        for(headIndex=0; headIndex<noOfHeadElement; headIndex++) {
        	Node currentHead = headParentSet.childNode(headIndex);
        	
    		// If it is Evar node, do nothing
    		// Note: Need to handle a case of different variable inside a set of Evar 
        	if(!currentHead.isVariable()) {        		
        		foundXmlConst = true;
            	listOfXmlConst.add(new Integer(headIndex));
        	} 
        }
        
        if((headParentSet.childNode(noOfHeadElement-1) != null) && (headParentSet.childNode(noOfHeadElement-1).getNodeType() == Node.NT_EVAR)) {
        	lastNodeIsEVar = true;
        }
        
        headIndex = 0;
        targetIndex = 0;
        // Find a set of possible matching of XML constant
        if(listOfXmlConst.size() > 0) {
        	prematchXmlConstant(headIndex, targetIndex, listOfXmlConst, headParentSet, targetParentSet, matchResultXmlConstantAll, matchResultXmlConstantPartial, lastNodeIsEVar, mode);
    	}
        
        for(int i=0; i<matchResultXmlConstantAll.size(); i++) {
        	Vector matchResult = (Vector) matchResultXmlConstantAll.get(i);
        }
        
        // Find all possible matching of Evar for a particular XML constant matching
        if(foundXmlConst) {
        	
        	Node newEvarHead = new Node();
        	Node newTarget = new Node();
        	Vector<MatchResult> matchResultEvarAll = null;
        	
        	for(int i=0; i<matchResultXmlConstantAll.size(); i++) {

        		Vector matchResultXmlConstant = (Vector) matchResultXmlConstantAll.get(i);
            	
            	int lastXmlConstHeadIndex = -1;
            	int lastXmlConstTargetIndex = -1;
        		int xmlConstHeadIndex = 0;
        		int xmlConstTargetIndex = 0;
            	int noOfMatchedXmlConst = matchResultXmlConstant.size();        		
            	boolean needToGatherLastSet = true;
            	MatchResult matchResultPartialTmp = null;
            	MatchResult matchResultXmlConst = null;
            	MatchResult matchResultAllTmp = null;
            	matchResultEvarAll = new Vector<MatchResult>();
            	
            	for(int xmlConstIndex=0; xmlConstIndex<noOfMatchedXmlConst; xmlConstIndex++) {
            		HashMap matchResultTmp = (HashMap) matchResultXmlConstant.get(xmlConstIndex);
            		matchResultPartialTmp = new MatchResult();
            		matchResultXmlConst = new MatchResult();
            		matchResultAllTmp = new MatchResult();
            		
            		if(matchResultTmp != null) {
                        String xmlConstHeadIndexObj = (String) matchResultTmp.get("HeadIndex");
                        String xmlConstTargetIndexObj = (String) matchResultTmp.get("TargetIndex");                        
                        xmlConstHeadIndex = new Integer(xmlConstHeadIndexObj).intValue();
                        xmlConstTargetIndex = new Integer(xmlConstTargetIndexObj).intValue();
                        
                        matchResultXmlConst.addNewInstance((MatchResult) matchResultTmp.get("MatchResult"));
                       
                        if(xmlConstHeadIndex == noOfHeadElement-1) {
                        	needToGatherLastSet = false;
                        }
                        
                        // Create a new set of Evar
                        newEvarHead = (Node) headParentSet.copySetOfNodes(lastXmlConstHeadIndex+1, xmlConstHeadIndex-1);
                        
                        // Create a new set of Target
                        newTarget = (Node) targetParentSet.copySetOfNodes(lastXmlConstTargetIndex+1, xmlConstTargetIndex-1);                                               
                        
                        // Find all possible match for a set of nodes containing Evar only
                        findAllPossibleMatch(0, newEvarHead, matchResultAllTmp, matchResultPartialTmp, 0, newTarget);
                        
                        // Add match result for Evar
                        matchResultEvarAll.add(matchResultAllTmp.clone());
                        
                        // Add match result for XML constant
                        matchResultEvarAll.add(matchResultXmlConst.clone());
                        
                        for(int z=0; z<matchResultEvarAll.size(); z++) {
                        	MatchResult matchResult = (MatchResult) matchResultEvarAll.get(z);
                        	for(int y=0; y<matchResult.getInstances().size(); y++) {
                        		MatchResult tmp = (MatchResult) matchResult.getInstances().get(y);
                        		}
                        }
                        
                        lastXmlConstHeadIndex = xmlConstHeadIndex;
                        lastXmlConstTargetIndex = xmlConstTargetIndex;
                   }                                       
            	} // End for loop of each set of XML constant match result
            	
            	// Create a last set of XML constant
                if(needToGatherLastSet) {
                	
                	matchResultAllTmp = new MatchResult();
                	
                	// Create a new set of Evar
                    newEvarHead = (Node) headParentSet.copySetOfNodes(lastXmlConstHeadIndex+1, headParentSet.numberOfChildNodes()-1);
                                            
                    // Create a new set of Target
                    newTarget = (Node) targetParentSet.copySetOfNodes(lastXmlConstTargetIndex+1, targetParentSet.numberOfChildNodes()-1);                                               
                    
                    // Find all possible match for a set of nodes containing Evar only
                    findAllPossibleMatch(0, newEvarHead, matchResultAllTmp, matchResultPartialTmp, 0, newTarget);
                    
                    matchResultEvarAll.add((MatchResult)matchResultAllTmp.clone());                    
                }
                
                // Merge all possible results for each result of matching XML constant
                Vector<MatchResult> allCombinationOfMatchResult = genAllMatchResultCombination(matchResultEvarAll);
                matchResultAll.getInstances().addAll(allCombinationOfMatchResult);                
            } // End for loop of all sets of XML constant match result
        	   
        	
        } else {
        	// If a head set is composed of all Evar, we will call findAllPossibleMatch without pre-matching XML element
            
        	findAllPossibleMatch(headIndex, headParentSet, matchResultAll, matchResultPartial, targetIndex, targetParentSet);
        }            
        
        if((matchResultAll!= null) && (matchResultAll.getInstances()!=null)) {
        	matchResultAll.setSuccess(true);
        	matchResultAll.setMultipleMatchingResults(true);
        }
        return matchResultAll;
	}
    
    private void prematchXmlConstant(int xmlConstHeadIndex, int targetIndex, Vector<Integer> listOfXmlConst, Node headParentSet, Node targetParentSet, 
    									Vector<Vector> matchResultAll, Vector<HashMap> matchResultPartial, boolean lastNodeIsEVar, int mode) {
    	
    	HashMap<String, Object> matchResultXmlConstant = new HashMap<String, Object>();
    	int noOfTargetChild = targetParentSet.numberOfChildNodes();    	
    	int noOXmlConst = listOfXmlConst.size();
    	if((xmlConstHeadIndex > (noOXmlConst-1)) || (targetIndex > (noOfTargetChild -1))) {
    		// Add result
            matchResultAll.add((Vector) matchResultPartial.clone());
    		            
    		return;    		
    	} else {
    		int headIndex = ((Integer)listOfXmlConst.get(xmlConstHeadIndex)).intValue();
    		MatchResult matchResultTmp = new MatchResult(false);
    		
    		if((xmlConstHeadIndex == noOXmlConst-1) && (!lastNodeIsEVar)) {
    			matchResultTmp = match(headParentSet.childNode(headIndex), targetParentSet.childNode(noOfTargetChild -1), mode);
    			if (matchResultTmp.isSuccess()) {
        			// Add index of head, target and MatchResult object into partial list
                	matchResultXmlConstant.put("HeadIndex", new Integer(headIndex).toString());                	
                	matchResultXmlConstant.put("TargetIndex", new Integer(noOfTargetChild -1).toString());
                	matchResultXmlConstant.put("MatchResult", matchResultTmp.clone());
                	matchResultPartial.add((HashMap) matchResultXmlConstant.clone());
                	
                	// Add result
                    matchResultAll.add((Vector) matchResultPartial.clone());

                    // Remove
                    matchResultPartial.remove(matchResultPartial.size()-1);
                    
            		return;                               		
                }
    			
    		} else {
	    		for(int targetPointer=targetIndex; targetPointer<noOfTargetChild; targetPointer++) {
	    			    			
	    			matchResultTmp = match(headParentSet.childNode(headIndex), targetParentSet.childNode(targetPointer), mode);
	            	
	    			if (matchResultTmp.isSuccess()) {
	        			// Add index of head, target and MatchResult object into partial list
	                	matchResultXmlConstant.put("HeadIndex", new Integer(headIndex).toString());                	
	                	matchResultXmlConstant.put("TargetIndex", new Integer(targetPointer).toString());
	                	matchResultXmlConstant.put("MatchResult", matchResultTmp.clone());
	                	matchResultPartial.add((HashMap) matchResultXmlConstant.clone());
	                	
	                	prematchXmlConstant(xmlConstHeadIndex+1, targetPointer+1, listOfXmlConst, headParentSet, targetParentSet, matchResultAll, matchResultPartial, lastNodeIsEVar, mode);                	
	        			
	        			// Remove
	                    matchResultPartial.remove(matchResultPartial.size()-1);                              		
	                }
	    		}
    		}
    	}
    	    	
    	return;
    }
    
    private void findAllPossibleMatch(int headIndex, Node headParentSet, MatchResult matchResultAll, 
    									MatchResult matchResultPartial, int tBeginPointer, Node targetParentSet) {
    	int noOfHeadElement = headParentSet.numberOfChildNodes();
        int noOfTargetElement = targetParentSet.numberOfChildNodes();
    	Node currentHead = headParentSet.childNode(headIndex);
    	
    	if(headIndex < noOfHeadElement-1) {
    		for(int tCurrentPointer=tBeginPointer; tCurrentPointer<=noOfTargetElement; tCurrentPointer++) {
    			
    			// Add E=i to partial list    			   			    			    			        			
    			Node evarHead = new Node();
                evarHead.setNodeName(currentHead.getNodeName());
                
                evarHead.setNamespace(currentHead.getNamespace());
                evarHead.setUri(currentHead.getUri());
                evarHead.setNodeType(Node.NT_EVAR);
                
                for (int j=tBeginPointer; j<tCurrentPointer; j++) {
                	evarHead.addChildNode(targetParentSet.childNode(j).cloneNode());
                }
                matchResultPartial.getSpecialization().addSpecialization(currentHead.fullName(), evarHead);
                       
                // Call recursively
                findAllPossibleMatch(headIndex+1, headParentSet, matchResultAll, matchResultPartial, tCurrentPointer, targetParentSet);                                                
                
                // Remove
                matchResultPartial.getSpecialization().removeSpecialization(headParentSet.childNode(headIndex).fullName());                               
    		}
    	} else if(headIndex == noOfHeadElement-1) {
    		
    		// Add E=m to partial list    		
			Node evarHead = new Node();
            evarHead.setNodeName(currentHead.getNodeName());
            
            evarHead.setNamespace(currentHead.getNamespace());
            evarHead.setUri(currentHead.getUri());
            evarHead.setNodeType(Node.NT_EVAR);
            
            for (int j=tBeginPointer; j<noOfTargetElement; j++) {
            	evarHead.addChildNode(targetParentSet.childNode(j).cloneNode());
            }
            
            matchResultPartial.setSuccess(true);
            matchResultPartial.getSpecialization().addSpecialization(currentHead.fullName(), evarHead);
            matchResultAll.addNewInstance(matchResultPartial.clone());
                                 
            // Remove
            matchResultPartial.getSpecialization().removeSpecialization(headParentSet.childNode(headIndex).fullName());            
    	}else {
    		// not match
    	}
	
    	return;
	}
    
	private Vector<MatchResult> genAllMatchResultCombination(Vector<MatchResult> matchResultAllEachEvarSet)
	{
		Vector<MatchResult> allCombinationOfMatchResult = new Vector<MatchResult>();

		allCombinationOfMatchResult.add(new MatchResult());
		
		for(int i=0; i<matchResultAllEachEvarSet.size(); i++)
		{
			Vector<MatchResult> matchResultTmpList = new Vector<MatchResult>();
			MatchResult matchResultForEachEvarSet = (MatchResult) matchResultAllEachEvarSet.get(i);
            for(int m=0; m<matchResultForEachEvarSet.getInstances().size(); m++) {
            	MatchResult matchResult = (MatchResult) matchResultForEachEvarSet.getInstances().get(m);
            }
            
			for (int j=0; j<matchResultForEachEvarSet.getInstances().size(); j++)
			{
				for (int k=0; k<allCombinationOfMatchResult.size(); k++)
				{
					MatchResult matchResultTmp = (MatchResult)((MatchResult)allCombinationOfMatchResult.get(k)).clone();
					matchResultTmp.merge(((MatchResult)matchResultForEachEvarSet.getInstances().get(j)).getSpecialization());
					matchResultTmpList.add(matchResultTmp);
				}
			}
			allCombinationOfMatchResult = matchResultTmpList;
		}

		return allCombinationOfMatchResult;
	}
	
    private MatchResult matchEvar(Node head, Node target, int mode) {
        MatchResult matchResult = new MatchResult(true);
        // Both are E-variables
        if ((head.getNodeType() == Node.NT_EVAR) && (target.getNodeType() == Node.NT_EVAR)) {
            if (!head.fullName().equals(target.fullName())) {
                matchResult.getSpecialization().addSpecialization(head.fullName(), target.fullName());
            }
            return matchResult;
        }
        Vector<Integer> matchedNodes = new Vector<Integer>();
        
        int headIndex = 0;
        int targetIndex = 0;
        
        boolean endOfHead = false;
        boolean beginEvar = false;
        boolean foundEvar = false;
        
        Node headParent = head.getParentNode();
        Node targetParent = target.getParentNode();
        
        int headSiblings = headParent.numberOfChildNodes();
        int targetSiblings = targetParent.numberOfChildNodes();
        
        while ((headIndex < headSiblings) && (targetIndex < targetSiblings)) {
            
            Node currentHead = headParent.childNode(headIndex);
            Node currentTarget = targetParent.childNode(targetIndex);
            
            // Check the current node if it is Evar or not
            if (currentHead.getNodeType() == Node.NT_EVAR) {
                if (! beginEvar) {
                	beginEvar = true;
                }
                // If both nodes are E-variables, then just increment the pointer
                // and move to the next nodes
                if (currentTarget.getNodeType() == Node.NT_EVAR) {
                	headIndex++;
                    targetIndex++;
                    continue;
                }
            } else {
                beginEvar = false;
            }
            Node nextHead = null;
            
            try {
                nextHead = headParent.childNode(headIndex + 1);
                endOfHead = false;
            } catch (Exception e) {
            	endOfHead = true;
            } finally {
                if (nextHead != null) {
                	// This means we have a next head node.
                    if (nextHead.getNodeType() == Node.NT_DEFAULT) {
                    	// Cannot use "matchDefault()" here because there is possibility
                        // for E1-variable here.
                    	MatchResult nextHeadResult = match(nextHead, currentTarget, mode);
                        matchResult.merge(nextHeadResult.getSpecialization());
                        matchResult.setSuccess(nextHeadResult.isSuccess());
                        if (nextHeadResult.isSuccess()) {
                            headIndex++;
	                        matchResult.setSuccess(true);
                            // Now, we have finished matching this E-variable
                            if (foundEvar) {
                                break;
                            }
                        } else {
	                        // Next node is not matched with the current target
	                        if (beginEvar) {
	                            // Now it is still in an Evar
	                            if (currentHead.fullName().equals(head.fullName())) {
		                            foundEvar = true;
	                                //head.addChildNode(currentTarget.cloneNode());
		                            matchedNodes.add(new Integer(targetIndex));
		                        }
	                            targetIndex++;
	                        } else {
	                            // Now we are not in an Evar
	                            //headIndex++;
	                        	targetIndex++;
	                        }
	                        matchResult.setSuccess(false);
	                    }
                    } else {
                    	
                        // The next node's type is not 'Default'
                    	headIndex++;
                        targetIndex++;
                    }
                } else {
                    // This is the last node!
                    if (currentHead.getNodeType() == Node.NT_EVAR) {
                        matchResult.setSuccess(true);
                    }
                    if (beginEvar) {
                        if (currentHead.fullName().equals(head.fullName())) {
	                        //head.addChildNode(currentTarget.cloneNode());
	                        matchedNodes.add(new Integer(targetIndex));
                        }
                    }
                    targetIndex++;
                }
            }
        }
        
        if (matchResult.isSuccess()) {
            Node evarHead = new Node();
            evarHead.setNodeName(head.getNodeName());
            
            evarHead.setNamespace(head.getNamespace());
            evarHead.setUri(head.getUri());
            evarHead.setNodeType(Node.NT_EVAR);
            
            for (int i = 0; i < matchedNodes.size(); i++) {
                
                Integer nodeIndex = (Integer)matchedNodes.get(i);
                evarHead.addChildNode(targetParent.childNode(nodeIndex.intValue()).cloneNode());
            }
            
            matchResult.getSpecialization().addSpecialization(evarHead.fullName(), evarHead);
            
        }
        return matchResult;
	}    
    
    private MatchResult matchIvar(Node head, Node target, int mode) {
    	MatchResult matchResultAll = new MatchResult();
        
        // Both of them are Ivar ... no need to match them
        if (head.fullName() == target.fullName()) {
            matchResultAll = new MatchResult(match(head.childNode(0), target.childNode(0), mode));
            return matchResultAll;
            
        } else if ((head.fullName() != target.fullName()) &&
                   (head.getNodeType() == Node.NT_IVAR) && 
                   (target.getNodeType() == Node.NT_IVAR)) {
        	matchResultAll = new MatchResult(true);
        	matchResultAll.getSpecialization().addSpecialization(head.fullName(), target.fullName());
            return matchResultAll;
            
        }
        
        // Create iVar instance for the specialization
        Node iVar = new Node();
        iVar.setNodeName(head.getNodeName());
        iVar.setNodeType(Node.NT_IVAR);
        iVar.setNamespace(head.getNamespace());
        iVar.setUri(head.getUri());
        
        // Clone all the node to this Ivar
        // If there are sibling nodes, we need to clone those sibling as well
        Node iVarChildNode = new Node();
        Node iVarTargetParent = target.getParentNode();
        
        if((iVarTargetParent != null) && (!"Fact".equals(iVarTargetParent.getNodeName()))) {
        	
        	int noOfSiblings = iVarTargetParent.numberOfChildNodes();
        	for(int i=0; i<noOfSiblings; i++) {
        		iVarChildNode = iVarTargetParent.childNode(i).cloneNode();
        		iVar.addChildNode(iVarTargetParent.childNode(i).cloneNode());
        	}
        	MatchResult matchResultPartial = new MatchResult();
            matchResultPartial.getSpecialization().addSpecialization(iVar.fullName(), iVar);
        } else {
        	iVarChildNode = target.cloneNode();
	        iVarChildNode.setParentNode(iVar);
	        iVar.addChildNode(iVarChildNode);
        }
              
        Node targetParent = new Node();               
    	targetParent = iVar.cloneNode();
    	
    	Vector<Vector> matchResultPosition = new Vector<Vector>();
    	Vector<Integer> currentNodePositionStack = new Vector<Integer>();
    	currentNodePositionStack.add(new Integer(0));
    	searchNode(matchResultPosition, currentNodePositionStack, head, targetParent, mode);		    	
    	
        Node iVarTemp = new Node();
        Node currentNode = new Node();
        MatchResult matchResultTemp = new MatchResult();
    	for(int matchResultIndex=0; matchResultIndex<matchResultPosition.size(); matchResultIndex++) {
    		matchResultTemp = new MatchResult();
    		Vector<Integer> matchResultPositionTemp = matchResultPosition.get(matchResultIndex);
    		iVarTemp = iVar.cloneNode();
    		currentNode = iVarTemp;
	    	for(int nodeLevel=0; nodeLevel<matchResultPositionTemp.size(); nodeLevel++) {
    	        Integer childPosition = matchResultPositionTemp.get(nodeLevel);
    	        currentNode = currentNode.childNode(childPosition.intValue());
    	    }
	    	
	    	if (currentNode.getParentNode() != null) {
                Node endOfIvar = new Node();
                endOfIvar.setNodeName(Specialization.END_OF_IVAR);
                endOfIvar.setParentNode(currentNode.getParentNode());
                endOfIvar.setNamespace(currentNode.getNamespace());
                endOfIvar.setNodeType(Node.NT_DEFAULT);
                endOfIvar.setUri(currentNode.getUri());
                currentNode.replaceSelf(endOfIvar);
            }    	        
	        
	    	matchResultTemp.getSpecialization().addSpecialization(iVar.fullName(), iVarTemp);
			matchResultAll.addNewInstance(matchResultTemp.clone());				    
    	}
    	
	    if((matchResultAll.getInstances()!= null) && (matchResultAll.getInstances().size() > 0)) {
	    	matchResultAll.setSuccess(true);
	    	matchResultAll.setMultipleMatchingResults(true);
	        for(int i=0; i<matchResultAll.getInstances().size(); i++) {
	        	MatchResult matchResult = (MatchResult) matchResultAll.getInstances().get(i);
	        }
	    } else {
	    	matchResultAll.setSuccess(false);
	    	matchResultAll.setMultipleMatchingResults(false);
	    }
	    
	    return matchResultAll;        
        
    }
    
    private void searchNode(Vector<Vector> matchResultAll, Vector<Integer> currentNodePositionStack, Node headParent, Node targetParent, int mode) {
    	Vector<Integer> headIndexList = new Vector<Integer>();
    	MatchResult matchResultTemp = new MatchResult();
    	Node firstHeadNode = headParent.childNode(0);    	    	
    	
    	for(int targetIndex=0; targetIndex<targetParent.numberOfChildNodes(); targetIndex++) {
        	// Put the current node position
        	if(currentNodePositionStack != null) {
        		currentNodePositionStack.set(currentNodePositionStack.size()-1, targetIndex);
        	}
        	
    		Node currentTarget = targetParent.childNode(targetIndex);
    		
    		for(int partialIndex=0; partialIndex<headIndexList.size();) {
        		int headIndex = headIndexList.firstElement().intValue();
    			headIndexList.removeElementAt(0);
    			Node headNode = headParent.childNode(headIndex);
    			
    			matchResultTemp = match(headNode, currentTarget, mode);
            	if(matchResultTemp.isSuccess()) {
            		headIndex++;
            		if(headIndex == headParent.numberOfChildNodes()) {
            			
            			matchResultAll.add((Vector) currentNodePositionStack.clone());            			            	
            		} else {
            			headIndexList.insertElementAt(new Integer(headIndex), 0);
            			targetIndex++;
            		}
            	} else {
            		if(currentTarget.numberOfChildNodes() > 0) {
	            		currentNodePositionStack.add(new Integer(0));
	                	searchNode(matchResultAll, currentNodePositionStack, headParent, currentTarget, mode);
	                	
	                	// remove last position
            			currentNodePositionStack.removeElementAt(currentNodePositionStack.size()-1);

            		}
            	}    			
    		}
    		
    		matchResultTemp = match(firstHeadNode, currentTarget, mode);        	
    		if(matchResultTemp.isSuccess()) {
    			if(headParent.numberOfChildNodes() == 1) {
        			
        			// add matched result and replaceSelfEvar        			       	
        			matchResultAll.add((Vector) currentNodePositionStack.clone());        			     			        		
        		} else {
	    			headIndexList.add(new Integer(1));
	    		}
    			
    		} else {
        		if(currentTarget.numberOfChildNodes() > 0) {
            		currentNodePositionStack.add(new Integer(0));                	
            		searchNode(matchResultAll, currentNodePositionStack, headParent, currentTarget, mode);            		            		
        		}
        	}    	
    	}
    	
    	// remove last position
		currentNodePositionStack.removeElementAt(currentNodePositionStack.size()-1);
		
    	return;
    }
    
    /**
     * Match P-variable
     * 
     * @param head The head pattern
     * @param target The target pattern
     * @return Returns the matching result
     */
    private MatchResult matchPvar(Node head, Node target) {
    	MatchResult matchResult = new MatchResult(true);
        // Do nothing if the head and the target don't contain any attribute
        if (!head.hasAttribute() && !target.hasAttribute()) {
        	return matchResult;
        }
        Vector<Node> targetAttributes = new Vector<Node>();
        Vector<Node> headAttributes = new Vector<Node>();
        // Re-add all child nodes to the new target attribute nodes
        Iterator targetAttributeIterator = target.attributeKey();
        while (targetAttributeIterator.hasNext()) {
            targetAttributes.add(target.attribute((String)targetAttributeIterator.next()));
        }
        Node pvarHead = new Node();
        String headPvarName = null;
        String targetPvarName = null;
        // Gradually match the head and the target
        Iterator headAttributeIterator = head.attributeKey();
        while (headAttributeIterator.hasNext()) {
            String attributeName = (String)headAttributeIterator.next();
            Node headAttribute = head.attribute(attributeName);
            headAttributes.add(head.attribute(attributeName));
            // If the node is Pvar, just keep the name
            if (headAttribute.getNodeType() == Node.NT_PVAR) {
                headPvarName = headAttribute.fullName();
                pvarHead.setNodeName(headAttribute.getNodeName());
                pvarHead.setNodeType(headAttribute.getNodeType());
                pvarHead.setNamespace(headAttribute.getNamespace());
                pvarHead.setUri(headAttribute.getUri());
                pvarHead.setNodeValue("NULL");
            } else {
            	// Try to match the current head node with all target nodes
                boolean found = false;
                for (int j = 0; j < targetAttributes.size(); j++) {
                    if (targetAttributes.get(j).getNodeType() == Node.NT_PVAR) {
                        targetPvarName = ((Node)targetAttributes.get(j)).fullName();
                        continue;
                    }
                    // Match the current head node with the current target attribute
                    MatchResult childResult = matchAttribute(attributeName, headAttribute, (Node)targetAttributes.get(j));
                    found = childResult.isSuccess();
                    // If the head attribute can be matched with the current target attribute
                    if (found) {
                        // Merge the specialization
                        matchResult.merge(childResult.getSpecialization());
                        // Remove the current target attribute from the list because
                        // there is no need to match the current target node again
                        targetAttributes.remove(j);
                        // break from the loop
                        break;
                    }
                    
                }
                // If the found variable has never been set to TRUE,
                // this means the current head cannot be matched with
                // any target node. So, false can be returned immediately.
                if (!found) {
                    if ((headPvarName == null) && (targetPvarName != null)) {
                        return matchPvar(target, head);
                    } else {
                        matchResult.setSuccess(false);
                        return matchResult;
                    }
                }
            }
        }
        // If all head attribute can be matched with all target attributes
        // The target attributes left is therefore added to the P-variable
        for (int i = 0; i < targetAttributes.size(); i++) {
			if (targetAttributes.get(i).getNodeName() != pvarHead.getNodeName()) {
				pvarHead.addChildNode(targetAttributes.get(i).cloneNode());
			}
        }
        // Add the e-variable when it contains some child nodes
        if (pvarHead.numberOfChildNodes() > 0) {
            matchResult.getSpecialization().addSpecialization(pvarHead.fullName(), pvarHead);
        }
        // Add also the variable name rewriting if the target node contains
        // a E-variable child node
        if ((targetPvarName != null) && (pvarHead.getNodeName() != null) && (pvarHead.numberOfChildNodes() != 0)) {
            matchResult.getSpecialization().addSpecialization(pvarHead.fullName(), targetPvarName);
        }
        matchResult.setSuccess(true);
        return matchResult;
    }    
    
    private MatchResult matchString(Node head, Node target) {
        
        MatchResult matchResult = new MatchResult(
                  (target.getNodeType() == Node.NT_SVAR) || 
		          (head.getNodeValue().equals(target.getNodeValue())));
        
        // If the target is an S-variable but the head is just a normal string,
        // add this specialization also.
        if ((matchResult.isSuccess()) && 
            (target.getNodeType() == Node.NT_SVAR) && 
            (head.getNodeType() != Node.NT_SVAR)) {
            
            matchResult.getSpecialization().addSpecialization(target.getNodeName(), head.getNodeValue());
            
        }
        
        return matchResult;
	    
    }
    
    private MatchResult matchSvar(Node head, Node target) {
        
        // This node is an XML-variable of type "S-Variable"
	    // It can be match with any string
        MatchResult matchResult = new MatchResult(false);
        if (target.getNodeType() == Node.NT_STRING) {
	        try {
    	        //matchResult.getSpecialization().addSpecialization(head.fullName(), target.getNodeValue());
                matchResult.getSpecialization().addSpecialization(head.getNodeName(), target.getNodeValue());
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    } 
    	    matchResult.setSuccess(true);
    	    return matchResult;
    	} else if (target.getNodeType() == Node.NT_SVAR) {
	        
	        // Change the variable name of the target to be the variable name of the head
	        //if (target.isAttributeNode()) {
	        //    target.setNodeValue(head.getNodeValue());
	        //} else {
	        //    target.setNodeName(head.getNodeName());
	        //    target.setNamespace(head.getNamespace());
	        //    target.setUri(head.getUri());
	        //}
	        
	        // Head and target can also be the same variable. A new variable-name rewrite
	        // will be added if the head and the taget don't have the same variable.
	        if (!target.fullName().equals(head.fullName())) {
	            matchResult.getSpecialization().addSpecialization(head.fullName(), target.fullName());
	        } 
	        matchResult.setSuccess(true);
	        return matchResult;
	    }
	    matchResult.setSuccess(false);
	    return matchResult;
	   
    }
    
}