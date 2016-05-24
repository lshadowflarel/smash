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

import net.sf.xet.nxet.matcher.Matcher;

/**
 * Head wrapper for a head atom. This is to support rules that
 * have several head.
 * 
 * @author Paramai Supadulchai
 */
public class RuleHead {

    private Node headAtom = null;
    private int matchingMode = 0;
    
    /**
     * The default constructor
     */
    public RuleHead() {
        
    }
    
    /**
     * Create a new head from a head atom
     * 
     * @param headAtom The head atom
     */
    public RuleHead(Node headAtom) {
        this.headAtom = headAtom;
    }
    
    /**
     * Returns the head atom in this head
     * 
     * @return Returns The head atom in this head
     */
    public Node getHeadAtom() {
        return this.headAtom;
    }
    
    /**
     * Print the texture XML representation of this head.
     * 
     * @param out the PrintStream that this function will send outputs to
     */
    public String printHead() {
        StringBuffer sb = new StringBuffer();
        sb.append("  <Head");
        sb.append(" mode=\"" + Matcher.MODE[this.matchingMode] + "\">");
        sb.append(headAtom.printXML(2));
        sb.append("\n  </Head>\n");        
        return sb.toString();
    }
    
    /**
     * Set the head atom for this head
     * 
     * @param headAtom The head atom to set
     */
    public void setHeadAtom(Node headAtom) {
        this.headAtom = headAtom;
    }
    
    /**
     * Returns the matching mode<br>
     * <br>
     * - Mode 0 = Sequential Deterministic (SequenceD)<br>
     * - Mode 1 = Sequential Non-deterministic (SequenceN)<br>
     * - Mode 3 = Set (Set)<br>
     * 
     * @return Returns the matching mode
     */
    public int getMatchingMode() {
        return this.matchingMode;
    }
    
    /**
     * Set the matching mode (in string)<br>
     * <br>
     * - SequenceD -> mode 0<br>
     * - SequenceN -> mode 1<br>
     * - Set -> mode 3<br>
     * 
     * @param matchingMode The matching mode to set
     */
    public void setMatchingMode(String matchingMode) {
        if (matchingMode.equalsIgnoreCase(Matcher.MODE[Matcher.MODE_SEQ_D])) {
            this.matchingMode = Matcher.MODE_SEQ_D;
        } else if (matchingMode.equalsIgnoreCase(Matcher.MODE[Matcher.MODE_SEQ_N])) {
            this.matchingMode = Matcher.MODE_SEQ_N;
        } else {
            this.matchingMode = Matcher.MODE_SET;
        }
    }
    
    public void setMatchingMode(int matchingMode) {       
        this.matchingMode = matchingMode;
    }    
    
    /**
     * This method is a wrapper method, that clones the
     * current rule and cast it back from an object type
     * to the "RuleHead" type
     *
     * @return Returns a cloned rule as a "RuleHead" type
     */
    public RuleHead cloneRuleHead() {
    	RuleHead newRuleHead = new RuleHead();    	
    	newRuleHead.setHeadAtom(this.headAtom.cloneNode());
    	newRuleHead.setMatchingMode(this.matchingMode);
    	
    	return newRuleHead;    	
    }    
}
