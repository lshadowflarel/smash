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
package net.sf.xet.nxet.builtin;

import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.executor.Result;

/**
 * An execution result object, which holds a "success" result
 * "true or "false" and associated variable instantiation.<br>
 * <br>
 * The execution result object can also be merged with the
 * MatchResult object by using the "merge" function of the
 * result abstract class.
 * 
 * @author Paramai Supadulchai
 */
public class ExecutionResult extends Result {

    /**
     * The default reason
     */
    public static final int UNKNOWN = -1;
    /**
     * The processing result of this clause - no clause = internal error
     */
    public static final int NO_CLAUSE = 0;
    /**
     * The processing result of this clause - progress = one or more
     * body atom(s) can be further processed
     */
    public static final int SUCCESS = 1;
    /**
     * The processing result of this clause - zero body atom = no body
     * atom can be further processed
     */
    public static final int ZERO_BODY_ATOM = 2;
    /**
     * The processing result of this clause - rule mismatched = there is
     * no rule that has the matching head or condition(s) that can match
     * at least one body of this clause
     */
    public static final int RULE_NO_MATCH = 3;
    /**
     * The processing result of this clause - builtin (n-atom) failed
     */
    public static final int BUILTIN_FAILED = 4;
    /**
     * The processing result of this clause - builtin (d-atom) failed
     */
    public static final int EXECSEQ_FAILED = 5;
    // Reason code
    private int reason = 0;
    // Result description
    private String resultDescription = null;
    
    /**
     * A default constructor. The success is set to false.
     */
    public ExecutionResult() {
        super();
        this.reason = ExecutionResult.UNKNOWN;
    }
    
    /**
     * A constructor with given a success parameter and a reason
     * why a the execution is failed
     * 
     * @param success The boolean value indicates the result 
     * of the execution.
     */
    public ExecutionResult(boolean success) {
        super(success);
        this.reason = ExecutionResult.UNKNOWN;
    }
    
    /**
     * A constructor with given a success parameter and a reason
     * 
     * @param success The boolean value indicates the result 
     * of the execution.
     * @param reason The reason why the execution is failed.
     */
    public ExecutionResult(boolean success, int reason) {
        super(success);
        this.reason = reason;
    }
    
    /**
     * A constructor with given a success parameter as 
     * well as a specialization object
     * 
     * @param success The boolean value indicates the result 
     * of the execution.
     * @param specialization The specialization holding
     * some variable instantiations
     */
    public ExecutionResult(boolean success, Specialization specialization) {
        super(success, specialization);
    }
    
    /**
     * A constructor with given a success parameter as 
     * well as a specialization object
     * 
     * @param success The boolean value indicates the result 
     * of the execution.
     * @param specialization The specialization holding
     * some variable instantiations
     * @param reason The reason why the execution is failed.
     */
    public ExecutionResult(boolean success, Specialization specialization, int reason) {
        super(success, specialization);
        this.reason = reason;
    }
    
    /**
     * A constructor that creates a new execution result
     * based on a result-based object.
     * 
     * @param result An object that is a subclass of "Result"
     */
    public ExecutionResult(Result result) {
        super(result);
    }
    
    /**
     * A constructor that creates a new execution result
     * based on a result-based object.
     * 
     * @param result An object that is a subclass of "Result"
     */
    public ExecutionResult(ExecutionResult result) {
        super(result);
        result.setReason(result.getReason());
    }
    
    /**
     * Returns the success/fail reason
     * 
     * @return Returns the reason.
     */
    public int getReason() {
        return reason;
    }
    /**
     * The success/fail reason to set
     * 
     * @param reason The reason to set.
     */
    public void setReason(int reason) {
        this.reason = reason;
    }
    
    /**
     * Returns the result description
     * 
     * @return Returns the result description
     */
    public String getResultDescription() {
        return this.resultDescription;
    }
    
    /**
     * Set the result description
     * 
     * @param resultDescription The result description
     */
    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }
    
}
