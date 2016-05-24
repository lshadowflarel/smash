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
package net.sf.xet.nxet.executor;

import java.util.Vector;
import net.sf.xet.nxet.core.Specialization;

/**
 * 
 * 
 * @author Paramai Supadulchai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Result {
    
    protected Vector<Result> instances = null;
    protected Specialization specialization = null;

    protected boolean success = false;

    protected boolean multipleMatchingResults = false;
    
    public Result() {
        this.specialization = new Specialization();
        this.instances = new Vector<Result>();
    }
    
    public Result(boolean success) {
        this.success = success;
        this.specialization = new Specialization();
        this.instances = new Vector<Result>();
    }
    
    public Result(boolean success, Specialization specialization) {
        this.success = success;
        if (specialization != null) {
            this.specialization = specialization;
        } else {
            this.specialization = new Specialization();
        }
        this.instances = new Vector<Result>();
    }
    
    public Result(Result result) {
        this.success = result.isSuccess();
        this.specialization = result.getSpecialization().cloneSpecialization();
        this.instances = new Vector<Result>();
        for (int i = 0; i < result.getInstances().size(); i++) {
            this.instances.add(result.getInstances().get(i));
        }
    }

    public void addNewInstance(Result c) {
        instances.add(c);
    }
    
    public Vector<Result> getInstances() {
        return instances;
    }
    
    /**
     * Get a specialization object.
     * 
     * @return Returns the specialization.
     */
    public Specialization getSpecialization() {
        return specialization;
    }

    /**
     * Get the boolean result value.
     * 
     * @return Returns the result.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Merge a boolean success value using "an" (&&).
     * 
     * @param success Merges a boolean value using "an" (&&).
     */
    public void merge(boolean success) {
        this.success = this.success && success;
    }
    
    /**
     * Merge this Result object with another result object.
     * This function use a "merge(boolean)" with the variable 
     * "success" and also use a "merge(Specialization)" to 
     * merge the spec object of this result. In addition, 
     * clause instances from another result object will also 
     * be merged.
     * 
     * @param result Another result object to merge.
     * @see #merge(boolean)
     * @see #merge(Specialization)
     */
    public void merge(Result result) {
        this.success = this.success && result.isSuccess();
        this.multipleMatchingResults = this.multipleMatchingResults || result.isMultipleMatchingResults();
        this.specialization.merge(result.getSpecialization());
        for (int i = 0; i < result.getInstances().size(); i++) {
            this.instances.add(result.getInstances().get(i));
        }
    }
    
    /**
     * Merge only the specialization from another result
     * object.
     * 
     * @param spec
     */
    public void merge(Specialization spec) {
        this.specialization.merge(spec);
    }
    
    /**
     * Assign a specialization object 
     * 
     * @param specialization The specialization to set.
     */
    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
    
    /**
     * Set a boolean success value for this result object.
     * 
     * @param result The result to set.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public boolean isMultipleMatchingResults() {
		return multipleMatchingResults;
	}

	public void setMultipleMatchingResults(boolean multipleMatchingResults) {
		this.multipleMatchingResults = multipleMatchingResults;
	}    
}
