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
package net.sf.xet.nxet.builtin.corefunctions;

import java.io.PrintStream;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;

/**
 * EmptySet checks the number of the childnodes
 * of an E-variable. If the number of childnodes
 * is empty, the function returns FALSE. Otherwise,
 * it returns TRUE.
 * 
 * @author Paramai Supadulchai
 */
public class EmptySet extends Builtin {

    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public EmptySet(Node builtinAtom, World world, Integer place, PrintStream out) {
        super(builtinAtom, world, place, out);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * A construct to create a builtin object from a builtin atom
     * 
     * @param builtinAtom The builtin atom
     * @param world The world that this built-in was being executed
     * @param place The place the built-in is executed
     */
    public EmptySet(Node builtinAtom, World world, Integer place) {
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
     * This function requires a child node.
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        //return new Boolean(builtinAtom.numberOfChildNodes() == 1);
        return new Boolean(true);
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * This built-in function will be executable if the only
     * childnode is an E-variable<br>
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        //return new Boolean(builtinAtom.childNode(0).getNodeType() == Node.NT_EVAR);
        return new Boolean(true);
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The result value is added in the variable specified
     * in the "result" parameter. <br>
     * <br>
     * The built-in function returns TRUE (success result)
     * if the number of the child nodes of the E-variable
     * is not ZERO. Otherwise it will return FALSE.
     * 
     * @return Returns an execution result object.
     */
    public ExecutionResult execute() throws NotExecutableException {
        boolean result = false;
        try {
	        //result = (builtinAtom.childNode(0).numberOfChildNodes() == 0);
            result = builtinAtom.numberOfChildNodes() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExecutionResult execResult = new ExecutionResult(result, new Specialization());
        return execResult;
    }

}
