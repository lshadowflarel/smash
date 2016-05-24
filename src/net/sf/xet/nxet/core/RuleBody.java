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

import java.util.Vector;

/**
 * Body wrapper containing body atoms and an execution sequence. 
 * This is to support rules that have several bodies.
 * 
 * @author Paramai Supadulchai
 */
public class RuleBody {

    // The list containing all body atoms of this clause
    private Vector<Node> bodyAtoms = null;
    private Vector<Node> execSeqAtoms = null;
    
    public RuleBody() {
        this.bodyAtoms = new Vector<Node>();
        this.execSeqAtoms = new Vector<Node>();
    }
    
    public RuleBody (Vector<Node> bodyAtoms, Vector<Node> execSeqAtoms) {
        this.bodyAtoms = bodyAtoms;
        this.execSeqAtoms = execSeqAtoms;
    }
    
    /**
     * Add a new body atom to this body.
     * 
     * @param bodyAtom a new body atom to add
     */
    public void addBodyAtom(Node bodyAtom) {
        this.bodyAtoms.add(bodyAtom);
    }
    
    /**
     * Append a new execution sequence atom to the execution sequence atom vector
     * 
     * @param execSeqAtom the new execution sequence atom object to add
     */
    public void addExecSeqAtom(Node execSeqAtom) {
        execSeqAtoms.add(execSeqAtom);
    }
    
    /**
     * Return a body atom, specified by an index, of this body.
     * 
     * @param atomIndex the index of the body atom to retrieve
     * @return the body atom specified by atomIndex
     */
    public Node bodyAtom(int atomIndex) {
        return bodyAtoms.get(atomIndex);
    }
    
    /**
     * Get a specific execution sequence atom
     * 
     * @param atomNo the index of the execution sequence atom
     * @return the specific execution sequence atom object
     */
    public Node execSeqAtom(int atomNo) {
        return execSeqAtoms.get(atomNo);
    }
    
    /**
     * Get the first body atom of this clause
     * 
     * @return the first body atom
     */
    public Node getFirstBodyAtom() {
        return this.bodyAtom(0);
    }
    
    /**
     * Verify and return true if there are more body atom(s).
     * 
     * @return true if the size of the body atom vector is more than zero
     */
    public boolean hasMoreBodyAtoms() {
        return (this.bodyAtoms.size() > 0);
    }
    
    /**
     * Get the number of the body atoms.
     * 
     * @return the size of the body atom vector
     */
    public int numberOfBodyAtoms() {
        return this.bodyAtoms.size();
    }
    
    /**
     * Return the number of execution sequence atoms
     * 
     * @return the size of the execution sequence atom vector
     */
    public int numberOfExecSeqAtoms() {
        return execSeqAtoms.size();
    }
    
    /**
     * Print the texture XML representation of this body.
     * 
     * @param out the PrintStream that this function will send outputs to
     */
    public String printBody() {
        StringBuffer sb = new StringBuffer();
        if ((this.numberOfBodyAtoms() > 0) || (this.numberOfExecSeqAtoms() > 0)) {
            sb.append("  <Body>");
            if (this.numberOfExecSeqAtoms() > 0) {
                sb.append("\n    <ExecutionSequence>");
                for (int i = 0; i < this.numberOfExecSeqAtoms(); i++) {
                    sb.append(((Node)execSeqAtoms.get(i)).printXML(3));
    	        }
                sb.append("\n    </ExecutionSequence>");
            }
	        for (int i = 0; i < this.numberOfBodyAtoms(); i++) {
	            sb.append(((Node)bodyAtoms.get(i)).printXML(2));
	        }
	        sb.append("\n  </Body>\n");
        }
        return sb.toString();
    }
    
    /**
     * Remove a body atom, specified by an index, from this clause.
     * 
     * @param atomNo the body atom index
     */
    public void removeBodyAtom(int atomNo) {
        bodyAtoms.remove(atomNo);
    }
    
    /**
     * Remove a specific execution sequence atom
     * 
     * @param atomNo the index of the execution sequence atom in the execution sequence atom vector
     */
    public void removeExecSeqAtom(int atomNo) {
        execSeqAtoms.remove(atomNo);
    }
    
    /**
     * Remove the first body atom from the atom list
     */
    public void removeFirstBodyAtom() {
        removeBodyAtom(0);
    }
    
    /**
     * This method is a wrapper method, that clones the
     * current rule and cast it back from an object type
     * to the "RuleBody" type
     *
     * @return Returns a cloned rule as a "RuleBody" type
     */
    public RuleBody cloneRuleBody() {
    	RuleBody newRuleBody = new RuleBody();
    	
    	for(int i=0; i<this.numberOfBodyAtoms(); i++) {
    		newRuleBody.addBodyAtom(this.bodyAtom(i).cloneNode());    		
    	}
    	
    	for(int j=0; j<this.numberOfExecSeqAtoms(); j++) {
    		newRuleBody.addExecSeqAtom(this.execSeqAtom(j).cloneNode());    		
    	}
    	
    	return newRuleBody;    	
    }
}
