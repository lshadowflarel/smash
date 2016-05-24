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
import net.sf.xet.nxet.builtin.BuiltinManager;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Clause;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;
import net.sf.xet.nxet.executor.Executor;
import net.sf.xet.nxet.matcher.Matcher;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SetOf extends Builtin {

    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param printStream The output redirector
     * @param place The place the built-in is executed
     */
    public SetOf(Node builtinAtom, World world, Integer place, PrintStream out) {
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
    public SetOf(Node builtinAtom, World world, Integer place) {
        super(builtinAtom, world, place);
        this.setBuiltinType(Builtin.BT_B);
    }

    /**
     * Check if the structure of the builtin atom matches
     * this builtin function. A built-in function usually
     * requires some parameters. This function checks if
     * those required parameters exist in the built-in atom
     * or not<br>
     * This function only requires that the number of child
     * nodes = 3 (set-of variable, constructor and condition)
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        return new Boolean(builtinAtom.numberOfChildNodes() == 4);
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * This built-in function will be executable if it has
     * exactly 3 child nodes (set-of variable, constructor 
     * and condition).
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        return new Boolean(builtinAtom.numberOfChildNodes() == 4);
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The set of variable creates a new world and assign
     * a new query clause created from the constructor 
     * and the condition of the set-of built-in atom.<br>
     * <br>
     * Each answer generated from the other world will be
     * added to an E-variable specified in a set-of 
     * built-in parameter.
     */
    public ExecutionResult execute() throws NotExecutableException {
        
        ExecutionResult execResult = null;
        
        String eVarName = "Evar_"+builtinAtom.childNode(0).childNode(0).getNodeName();
        Node setOfSetVariable = builtinAtom.childNode(1).childNode(0);
        Node setOfConstructor = null;
        // If the constructor is a single E-var...
        if (builtinAtom.childNode(2).childNode(0).getNodeType() != Node.NT_DEFAULT) {
        	setOfConstructor = new Node();
        	setOfConstructor.setNodeName("dummy");
        	setOfConstructor.setNodeType(Node.NT_DEFAULT);
        	setOfConstructor.addChildNode(builtinAtom.childNode(1).childNode(0));
        } else {
        	setOfConstructor = builtinAtom.childNode(2).childNode(0);
        }
        Node setOfCondition = builtinAtom.childNode(3).childNode(0);
        
        
        
        
        // Change the query of the world
        Clause queryClause = new Clause();
        
        // Contruct a new query clause from the constructor and the condition
        // of the set-of atom
        queryClause.setClauseType(Clause.N_CLAUSE);
        queryClause.setHeadAtom(setOfConstructor);
        queryClause.addBodyAtom(setOfCondition);
        
        // Create another world to execute this query
        World setOfWorld = World.create(World.WORLD_SET_OF, 
                                        queryClause, 
                                        world.getRules(), 
                                        world.getDataSources(),
                                        world.getConfig(), 
                                        world.getLogHandler(),
                                        world.getBuiltinPath(),
                                        this.getWorld().getExecutor());
        
        // The result atoms of set-of will be added into a list "E-variable",
        // which will be added to the specialization
        Specialization spec = new Specialization();
        
        // Create a new executor
        Executor executor = Executor.create(setOfWorld, 
                                            new Matcher(world.getConfig()), 
                                            new BuiltinManager(setOfWorld, world.getBuiltinPath()),
                                            world.getLogHandler());
        
        // Execute the new world
        executor.execute();
        // Add all the answer of the set-of world back to the set specified in this set-of
        for (int i = 0; i < setOfWorld.numberOfAnswers(); i++) {
            if (setOfWorld.answer(i).isUnitClause()) {
            	if (setOfWorld.answer(i).getHeadAtom().getNodeName() == "dummy") {
            		for (int j = 0; j < setOfWorld.answer(i).getHeadAtom().numberOfChildNodes(); j++) {
            			setOfSetVariable.addChildNode(setOfWorld.answer(i).getHeadAtom().childNode(j));
            		}
            	} else {
            		setOfSetVariable.addChildNode(setOfWorld.answer(i).getHeadAtom());
            	}
            }
        }
        
        // Add the set to the specialization, which will be added as an answer of this set-of builtin
        spec.addSpecialization(eVarName, setOfSetVariable);
        //System.out.println(spec.printSpecialization());
        execResult = new ExecutionResult(true, spec);
        
        return execResult;
        
    }

}
