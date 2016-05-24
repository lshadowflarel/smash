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
 */
package net.sf.xet.nxet.core;

import java.io.PrintStream;

/**
 * Condition wrapper for a condition atom. This is to support 
 * rules that have several head.
 * 
 * @author Paramai Supadulchai
 */
public class RuleCondition {

    private Node conditionAtom = null;
    
    /**
     * The default constructor
     */
    public RuleCondition() {
        
    }
    
    /**
     * Create a new condition from a condition atom
     * 
     * @param conditionAtom The condition atom
     */
    public RuleCondition(Node conditionAtom) {
        this.conditionAtom = conditionAtom;
    }
    
    /**
     * Returns the condition atom in this head
     * 
     * @return Returns The condition atom in this head
     */
    public Node getConditionAtom() {
        return this.conditionAtom;
    }
    
    /**
     * Print the texture XML representation of this condition.
     * 
     * @param out the PrintStream that this function will send outputs to
     */
    public void printCondition(PrintStream out) {
        out.println(conditionAtom.printXML(2));
    }
    
    /**
     * Set the condition atom for this condition
     * 
     * @param conditionAtom The condition atom to set
     */
    public void setConditionAtom(Node conditionAtom) {
        this.conditionAtom = conditionAtom;
    }
    
    /**
     * This method is a wrapper method, that clones the
     * current rule and cast it back from an object type
     * to the "RuleBody" type
     *
     * @return Returns a cloned rule as a "RuleBody" type
     */
    public RuleCondition cloneRuleCondition() {    	
    	RuleCondition newRuleCondition = new RuleCondition();    	
    	newRuleCondition.setConditionAtom(this.conditionAtom.cloneNode());
    	
    	return newRuleCondition;    	
    }    
    
}
