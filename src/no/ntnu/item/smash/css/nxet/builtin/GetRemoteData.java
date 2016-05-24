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
package no.ntnu.item.smash.css.nxet.builtin;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;

public class GetRemoteData extends Builtin {

    public final String ATTR_STRING = "provider";
    public final String ATTR_STRING2 = "operation";
    public final String ATTR_PREFIX_PARAM = "param";
    
    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public GetRemoteData(Node builtinAtom, World world, Integer place, PrintStream out) {
        super(builtinAtom, world, place, out);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * A construct to create a builtin object from a builtin atom
     * 
     * @param builtinAtom The builtin atom
     * @param world The world that this built-in was being executed.
     * @param place The place the built-in is executed
     */
    public GetRemoteData(Node builtinAtom, World world, Integer place) {
        super(builtinAtom, world, place);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * Check if the structure of the builtin atom matches
     * this builtin function. A built-in function usually
     * requires some parameters. This function checks if
     * those required parameters exist in the built-in atom
     * or not<br>
     * <br>
     * This built-in function requires two parameters:
     * - parameter provider<br>
     * - parameter operation<br>
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        
        return new Boolean(builtinAtom.hasAttributeByLocalName(ATTR_STRING) &&
        		builtinAtom.hasAttributeByLocalName(ATTR_STRING2));
    
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * Both "provider" and "operation" must be instantiated.<br>
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        return new Boolean(!builtinAtom.attributeByLocalName(ATTR_STRING).isVariable() &&
        		!builtinAtom.attributeByLocalName(ATTR_STRING2).isVariable());
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The result value is replaced with the current time
     * 
     * @return Returns an execution result object.
     */
    public ExecutionResult execute() throws NotExecutableException {
        Specialization spec = new Specialization();
        String provider = builtinAtom.attributeByLocalName(ATTR_STRING).getNodeValue();
        String operation = builtinAtom.attributeByLocalName(ATTR_STRING2).getNodeValue();
        
        // there may be many parameters sent with this data request
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        int paramNo = 1;
        while(builtinAtom.attributeByLocalName(ATTR_PREFIX_PARAM + paramNo)!=null) {
        	String name = builtinAtom.attributeByLocalName(ATTR_PREFIX_PARAM + paramNo).getNodeValue();
        	Object value = builtinAtom.attributeByLocalName(ATTR_PREFIX_PARAM + paramNo + "_val").getNodeValue();
        	parameters.put(name, value);
        	
        	paramNo++;
        }
        
        // get data from the provider
        HashMap<String,Object> results = this.getWorld().getExecutor().getParent().getCSS().getMon().getDataRequester().getRemoteData(provider, operation, parameters);
        Set<String> keys = results.keySet();
        for(String key:keys) {
        	// get the variable name where we need to assign the result to
        	String resultSVarName = builtinAtom.attributeByLocalName("return_" + key).getNodeValue();
        	
        	if(results.get(key) instanceof long[]) {
        		spec.addSpecialization(resultSVarName, Arrays.toString((long[]) results.get(key)));        		
        	} else if(results.get(key) instanceof int[]) {
        		spec.addSpecialization(resultSVarName, Arrays.toString((int[]) results.get(key)));
        	} else if(results.get(key) instanceof double[]) { 
        		spec.addSpecialization(resultSVarName, Arrays.toString((double[]) results.get(key)));
        	} else if(results.get(key) instanceof String[]) {
        		spec.addSpecialization(resultSVarName, Arrays.toString((String[]) results.get(key)));
        	} else {
        		spec.addSpecialization(resultSVarName, ""+results.get(key));
        	}
        }
        
        ExecutionResult execResult = new ExecutionResult(true, spec);
        execResult.setResultDescription(" [" + results.toString() + "]");
	    return execResult;
    }

}
