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
import net.sf.xet.nxet.core.World;
import net.sf.xet.nxet.matcher.Matcher;
import net.sf.xet.nxet.matcher.MatchResult;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MatchD extends Builtin {

    public static final String ATTR_MODE = "mode";
    
    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public MatchD (Node builtinAtom, World world, Integer place, PrintStream out) {
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
    public MatchD (Node builtinAtom, World world, Integer place) {
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
     * This built-in function only requires that the number 
     * of child atoms = 2
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        return new Boolean(builtinAtom.numberOfChildNodes() == 2);
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * This built-in is executable when it has two child nodes.
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        return new Boolean(builtinAtom.numberOfChildNodes() == 2);
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The first child node will be treated as the head
     * parameter of the Matcher.match() function. The 
     * second child node will be treated as the target 
     * parameter.<br> The function returns TRUE if 
     * both nodes can be matched. All variables instantiated 
     * will also be included in the execution result object.
     * 
     * @return Returns an execution result object.
     * @see net.sf.xet.nxet.matcher.Matcher#match(Node, Node, int)
     */
    public ExecutionResult execute() throws NotExecutableException {
        
        int mode = 0;
        // Get the mode attribute. If it does not exist, Sequential-D will be assumed
        if (builtinAtom.hasAttributeByLocalName(ATTR_MODE)) {
            if (builtinAtom.attributeByLocalName(ATTR_MODE).getNodeValue().equalsIgnoreCase(
                    	    Matcher.MODE[Matcher.MODE_SEQ_N])) {
                mode = Matcher.MODE_SEQ_N;
            } else if (builtinAtom.attributeByLocalName(ATTR_MODE).getNodeValue().equalsIgnoreCase(
                    	    Matcher.MODE[Matcher.MODE_SET])) {
                mode = Matcher.MODE_SET;
            } else {
                mode = Matcher.MODE_SEQ_D;
            }
        }
        Matcher matcher = new Matcher(world.getConfig());
        MatchResult matchResult = null;
        // If the left side or the right side is Evar, then just copy the other side
        try {
	        if ((builtinAtom.childNode(0).childNode(0).getNodeType() == Node.NT_EVAR) && (builtinAtom.childNode(0).numberOfChildNodes() == 1)) {
	        	matchResult = new MatchResult();
	        	matchResult.setSuccess(true);
	        	matchResult.getSpecialization().addSpecialization(builtinAtom.childNode(0).childNode(0).getNodeName(), builtinAtom.childNode(1).cloneNode());
	        } else if ((builtinAtom.childNode(1).childNode(0).getNodeType() == Node.NT_EVAR) && (builtinAtom.childNode(1).numberOfChildNodes() == 1)) {
	        	matchResult = new MatchResult();
	        	matchResult.setSuccess(true);
	        	matchResult.getSpecialization().addSpecialization(builtinAtom.childNode(1).childNode(0).getNodeName(), builtinAtom.childNode(0).cloneNode());
	        } else {
	        	matchResult = matcher.match(builtinAtom.childNode(0), builtinAtom.childNode(1), mode);
	        }
        } catch (ArrayIndexOutOfBoundsException e) {
        	matchResult = matcher.match(builtinAtom.childNode(0), builtinAtom.childNode(1), mode);
        }
        ExecutionResult execResult = new ExecutionResult(matchResult); 
        
        return execResult;
    }

}
