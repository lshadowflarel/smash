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

import java.util.Iterator;
import java.util.Vector;

import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.executor.Result;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MatchResult extends Result {
    
    private int factAtomIndex = 0;
    
    private int ruleIndex = 0;   

	public MatchResult() {
        super();
    }
    
    public MatchResult(boolean success) {
        super(success);
    }
    
    public MatchResult(boolean success, Specialization specialization) {
        super(success, specialization);
    }
    
    public MatchResult(Result result) {
        super(result);
    }
    
    /**
     * @return Returns the factAtomIndex.
     */
    public int getFactAtomIndex() {
        return factAtomIndex;
    }
    
    /**
     * @return Returns the ruleIndex.
     */
    public int getRuleIndex() {
        return ruleIndex;
    }
    
    /**
     * @param factAtomIndex The factAtomIndex to set.
     */
    public void setFactAtomIndex(int factAtomIndex) {
        this.factAtomIndex = factAtomIndex;
    }
    /**
     * @param ruleIndex The ruleIndex to set.
     */
    public void setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
    }
    
    /**
     * This method clones a match result
     * 
     * @return a match result as an object match result
     */
    public MatchResult clone() {
        
    	MatchResult m = new MatchResult();
        
    	m.setFactAtomIndex(this.getFactAtomIndex());
    	m.setRuleIndex(this.getRuleIndex());
    	m.setSpecialization(this.getSpecialization().cloneSpecialization());
    	m.setMultipleMatchingResults(this.multipleMatchingResults);
    	m.instances = new Vector<Result>();
        for (int i = 0; i < this.getInstances().size(); i++) {
            m.instances.add(this.getInstances().get(i));
        }
    	m.setSuccess(this.success);
        
        return m;
    }    
    
}
