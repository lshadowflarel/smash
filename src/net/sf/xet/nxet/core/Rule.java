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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * Rule consists of three main parts: heads, conditions and bodies.
 * A rule can be categorized into two types: D-Rule and N-Rule.
 * A D-Rule has exactly one head, one body and several condition
 * atoms. When a body of a clause matches the head of a D-Rule, 
 * the body atoms of the rule's body is executed sequentially.
 * If a body atom is not built-in, it will recursively matches
 * with the head of another rules.<br>
 * <br>
 * N-Rule can features multiple-head and multiple-body. However, 
 * the multiple-head rule has not yet been implemented. If a rule
 * has multiple bodies, additional copies of the working clause
 * will be duplicated in the same number.
 * 
 * @author Paramai Supadulchai
 */
public class Rule {
    
    /**
     * Rule type "Deterministic"
     */
    public static final int RT_D = 0;
    /**
     * Rule type "Non-deterministic"
     */
    public static final int RT_N = 1;
    /**
     * Rule type "Recursive/Non-deterministic"
     */
    public static final int RT_R = 2;
    
    // Textual representation of rule types
    private static String[] RULE_TYPE = new String[] { 
        "D-Rule",
        "N-Rule", "R-Rule"};
    // The set of bodies of the rule
    private Vector<RuleBody> bodies = new Vector<RuleBody>();
    // the set of condition of the rule
    private Vector<RuleCondition> conditions = new Vector<RuleCondition>();
    
    // The set of heads of the rule
    private Vector<RuleHead> heads = new Vector<RuleHead>();
    // The class of the rule
    private String ruleClass = null;
    // The class priority of the rule
    private int ruleClassPriority = 0;
    // The description of the rule
    private String ruleDescription = null;
    // The name of the rule
    private String ruleName = null;
    // The priority of the rule
    private int rulePriority = 0;
    // The type of the rule
    private int ruleType = 0;
    // The specification version of the rule
    private String specVersion = null;
    // The counting number for recursive call
    private static int recursiveCount = 0;    
    
    /**
     * The default constructor does not do anything
     * special here.
     */
    public Rule() {}
    
    /**
     * Append a new body to the body list (vector)
     * 
     * @param bodyAtom The new body object to add
     */
    public void addBody(RuleBody body) {
        this.bodies.add(body);
    }
    
    /**
     * Append a new condition to the condition list (vector)
     * 
     * @param condition the new condition object to add
     */
    public void addConditionAtom(RuleCondition condition) {
        conditions.add(condition);
    }
   
    /**
     * Add head to the rule
     * 
     * @param head the head object
     * @since NxET 0.3
     */
    public void addHead(RuleHead head) {
        this.heads.add(head);
    }
    
    /**
     * Get a specific body
     * 
     * @param bodyIndex the index of the body
     * @return the specific body object
     */
    public RuleBody body(int bodyIndex) {
        return bodies.get(bodyIndex);
    }
    
    /**
     * Get a specific condition atom
     * 
     * @param atomNo the index of the condition atom
     * @return the specific condition atom object
     */
    public RuleCondition condition(int conditionIndex) {
        return conditions.get(conditionIndex);
    }
    
    /**
     * Get effective priority
     * 
     * @return Returns the effective priority
     * @since NxET 0.2
     */
    public int effectivePriority() {
        return this.ruleClassPriority + this.rulePriority;
    }
    
    /**
     * Returns the first body of the rule
     * 
     * @return Returns the first body of the rule
     * @since NxET 0.3
     */
    public RuleBody firstBody() {
        return this.body(0);
    }
    
    /**
     * Returns the first head of the rule
     *
     * @return Returns the first head of the rule
     * @since NxET 0.3
     */
    public RuleHead firstHead() {
        return this.head(0);
    }
    
    /**
     * Return the class of this rule
     * 
     * @return The class of this rule
     * @since NxET 0.2
     */
    public String getRuleClass() {
        return this.ruleClass;
    }
    
    /**
     * Returns the priority of the class
     * 
     * @return The class priority
     * @since NxET 0.2
     */
    public int getRuleClassPriority() {
        return this.ruleClassPriority;
    }
    
    /**
     * Return the description of this rule
     * 
     * @return The description of this rule
     * @since NxET 0.2
     */
    public String getRuleDescription() {
        return this.ruleDescription;
    }
    
    /**
     * Get the name of the rule
     * 
     * @return The name of this rule
     */
    public String getRuleName() {
        return this.ruleName;
    }
    
    /**
     * Returns the priority of the rule
     * 
     * @return The rule priority
     */
    public int getRulePriority() {
        return this.rulePriority;
    }
    
    /**
     * Returns the type of the rule
     * 
     * @return The rule type
     */
    public int getRuleType() {
        return this.ruleType;
    }
    
    public String getSpecVersion() {
        return this.specVersion;
    }
    
    /**
     * Get a head of this rule specified by an index
     * 
     * @param headIndex The index of the head
     * @return Returns a head specified by an index
     */
    public RuleHead head(int headIndex) {
        return this.heads.get(headIndex);
    }
    
    /**
     * Return the number of bodies
     * 
     * @return the size of the body vector
     */
    public int numberOfBodies() {
        return bodies.size();
    }
    
    /**
     * Return the number of conditions
     * 
     * @return the size of the condition vector
     */
    public int numberOfConditions() {
        return conditions.size();
    }
    
    /**
     * Print the texture XML representation of this rule in XML
     * 
     * @param out
     */
    public String printRule() {
        StringBuffer sb = new StringBuffer();
        sb.append((this.ruleType == Rule.RT_D) ? "<DRule" : "<Rule");
        if (this.ruleName != null) {
            sb.append(" name=\"" + this.ruleName + "\"");
        }
        sb.append(" priority=\"" + this.rulePriority + "\"");
        if (this.ruleClass != null) {
            sb.append(" group=\"" + this.ruleClass + "\"");
        }
        sb.append(">\n");
        sb.append("  <Meta>\n");
        if (this.ruleDescription != null) {
            sb.append("    <RuleDescription>" + this.getRuleDescription() + "</RuleDescription>\n");
        }
        sb.append("  </Meta>\n");
        for (int i = 0; i < this.heads.size(); i++) {
            sb.append(this.heads.get(i).printHead());
        }
        if (this.numberOfConditions() > 0) {
            sb.append("  <Condition>");
            for (int i = 0; i < this.numberOfConditions(); i++) {
                sb.append(conditions.get(i).getConditionAtom().printXML(2));
	        }
            sb.append("\n  </Condition>\n");
        }
        for (int i = 0; i < this.bodies.size(); i++) {
            sb.append(this.bodies.get(i).printBody());
        }
        sb.append("</Rule>");
        return sb.toString();
    }
    
    /**
     * Remove a specific body
     * 
     * @param atomNo the index of the body in the body vector
     */
    public void removeBody(int bodyIndex) {
        bodies.remove(bodyIndex);
    }
    
    /**
     * Remove a specific condition
     * 
     * @param atomNo the index of the condition in the condition vector
     */
    public void removeConditions(int conditionIndex) {
        conditions.remove(conditionIndex);
    }
    
    /**
     * Return the textual representation of the rule type of this rule
     * 
     * @return the textual representation of the rule type "D-Rule" or "N-Rule"
     */
    public String ruleTypeText() {
        return RULE_TYPE[this.ruleType];
    }
    
    /**
     * Set rule class for this rule
     * 
     * @param ruleClass The rule class to set
     * @since NxET 0.2
     */
    public void setRuleClass(String ruleClass) {
        this.ruleClass = ruleClass;
    }
    
    /**
     * Set the priority of the class for this rule
     * 
     * @param ruleClassPriority The priority to set
     * @since NxET 0.2
     */
    public void setRuleClassPriority(int ruleClassPriority) {
        this.ruleClassPriority = ruleClassPriority;
    }
    
    /**
     * Set rule description for this rule
     * 
     * @param ruleDescription The rule description to set
     * @since NxET 0.2
     */
    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }
    
    /**
     * Set rule name for this rule
     * 
     * @param ruleName The rule name to set
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    /**
     * Set (local) rule priority for this rule
     * 
     * @param rulePriority The rule priority to set
     */
    public void setRulePriority(int rulePriority) {
        this.rulePriority = rulePriority;
    }
    
    /**
     * Set rule type for this rule
     * 
     * @param ruleType The rule type to set
     */
    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }
    
    /**
     * Set specification version for this rule
     * 
     * @param specVersion The rule spec to set
     */
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }
    
    /**
     * Print out the XML representation of the rule
     */
    public String toString() {
        return this.printRule();
    }
    
    public void renameVariables(boolean initialRule)
    {
    	String suffix = "";
    	if(initialRule) {
	    	suffix = this.getRuleName();
    	} else {    		
    		suffix = "*";
    	}
    	Iterator<RuleHead> TempHead = this.heads.iterator();
    	while(TempHead.hasNext() )
    	{
    		Node n = TempHead.next().getHeadAtom();    		  
        	n.renameVariables (suffix);        	
    	}
    	
    	Iterator<RuleCondition> TempCondition = this.conditions.iterator();
    	while(TempCondition.hasNext() )
    	{
    		Node n = TempCondition.next().getConditionAtom() ;    		  
    		n.renameVariables (suffix);
    	}
    	
    	Iterator<RuleBody> TempBody = this.bodies.iterator();
    	while(TempBody.hasNext() )
    	{
    		
    		RuleBody Temp = TempBody.next();
    		for(int i=0; i< Temp.numberOfBodyAtoms() ;i++)
    		{
    			Node n = Temp.bodyAtom(i);
    			n.renameVariables (suffix);
    		}  		    		  
    	}
    	
    }
    
    /**
     * This method is a wrapper method, that clones the
     * current rule and cast it back from an object type
     * to the "Rule" type
     *
     * @return Returns a cloned rule as a "Rule" type
     */
    public Rule cloneRule() {
    	Rule newRule = new Rule();
    	newRule.setRuleClass(this.ruleClass);
    	newRule.setRuleClassPriority(this.ruleClassPriority);
    	newRule.setRuleDescription(this.ruleDescription);
    	newRule.setRuleName(this.ruleName);
    	newRule.setRulePriority(this.rulePriority);
    	newRule.setRuleType(this.ruleType);
    	newRule.setSpecVersion(this.specVersion);
    	
    	for(int i=0; i<this.numberOfBodies(); i++) {
    		newRule.addBody(this.body(i).cloneRuleBody());
    	}
    	
    	for(int j=0; j<this.numberOfConditions(); j++) {
    		newRule.addConditionAtom(this.condition(j).cloneRuleCondition());
    	}
    	
    	for(int k=0; k<this.heads.size(); k++) {
    		newRule.addHead(this.head(k).cloneRuleHead());
    	}
    	
        return newRule;
    }    
       
}