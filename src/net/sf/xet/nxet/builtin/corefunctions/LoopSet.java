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
import java.util.Vector;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.World;
import net.sf.xet.nxet.matcher.MatchResult;
import net.sf.xet.nxet.matcher.Matcher;

/**
 * @author Kornschnok Dittawit
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class LoopSet extends Builtin {

	public static final String ATTR_MAP = "Maps";
	public static final String ATTR_SET = "Set";
	public static final String ATTR_EXTRACT = "Extract";
	public static final String ATTR_MODE = "mode";

	/**
	 * A construct to create a builtin object from a builtin atom and an output
	 * redirector.
	 * 
	 * @param builtinAtom
	 *            The builtin atom
	 * @param world
	 *            The world
	 * @param place
	 *            The place the built-in is executed
	 * @param printStream
	 *            The output redirector
	 */
	public LoopSet(Node builtinAtom, World world, Integer place, PrintStream out) {
		super(builtinAtom, world, place, out);
		this.setBuiltinType(Builtin.BT_N);
	}

	/**
	 * A construct to create a builtin object from a builtin atom
	 * 
	 * @param builtinAtom
	 *            The builtin atom
	 * @param world
	 *            The world that this built-in was being executed.
	 * @param place
	 *            The place the built-in is executed
	 */
	public LoopSet(Node builtinAtom, World world, Integer place) {
		super(builtinAtom, world, place);
		this.setBuiltinType(Builtin.BT_N);
	}

	/**
	 * Check if the structure of the builtin atom matches this builtin function.
	 * A built-in function usually requires some parameters. This function
	 * checks if those required parameters exist in the built-in atom or not<br>
	 * This function requires only 3 parameter<br>
	 * - parameter Maps or Set - parameter Extract - parameter Mode
	 * 
	 * @return Returns true if matched
	 * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
	 */
	public Boolean isMatched() {
		return builtinAtom.hasAttributeByLocalName(ATTR_MODE);
	}

	/**
	 * After the structure of the built-in has been verified, The built-in
	 * manager verifies also if the built-in can be executed. A built-in can be
	 * executed when it does not have neccessary variable left uninstantiated.
	 * In some built-in the number of child nodes are also checked.<br>
	 * <br>
	 * 
	 * @return Returns true if the built-in is executable
	 * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
	 */
	public Boolean isExecutable() {

		return builtinAtom.getChildNodes(ATTR_MAP).size()>0?!builtinAtom.getChildNodes(ATTR_MAP).get(0).isVariable():
				builtinAtom.getChildNodes(ATTR_SET).size()>0?!builtinAtom.getChildNodes(ATTR_SET).get(0).isVariable():false;

	}

	/**
	 * Execute the builtin function. There might be some variables instantiated
	 * during the execution. These variables will be stored in a generated
	 * specialization object, which will be included in the returned
	 * ExecutionResult object.<br>
	 * <br>
	 * Each matching result will be stored in the list of matching result
	 * (instances).<br>
	 * If the built-in function cannot find any atom<br>
	 * that matches, it will return FALSE execution success result. Otherwise,
	 * it will return TRUE execution success result.<br>
	 * 
	 * @return Returns an execution result object.
	 */
	public ExecutionResult execute() throws NotExecutableException {
		int mode = Matcher.MODE_SET;

		// Get the mode attribute.
		if (builtinAtom.attributeByLocalName(ATTR_MODE).getNodeValue()
				.equalsIgnoreCase(Matcher.MODE[Matcher.MODE_MAP])) {
			mode = Matcher.MODE_MAP;
		} else if (builtinAtom.attributeByLocalName(ATTR_MODE).getNodeValue()
				.equalsIgnoreCase(Matcher.MODE[Matcher.MODE_SET])) {
			mode = Matcher.MODE_SET;
		}

		ExecutionResult execResult = new ExecutionResult(false);
		Matcher m = new Matcher(world.getConfig());
		Node qexpr = builtinAtom.getChildNodes(ATTR_EXTRACT).get(0)
				.childNode(0);

		switch (mode) {
		case Matcher.MODE_SET:
			Vector<Node> elementVector = builtinAtom.getChildNodes(ATTR_SET)
					.get(0).getChildNodes();

			int j=0;
			for (Node elem : elementVector) {
				MatchResult matchResult = m.match(qexpr, elem, Matcher.MODE_SET);
				matchResult.setFactAtomIndex(j++);
				
				// If they are matched, a new clause must be created
				if (matchResult.isSuccess()) {
					if (matchResult.isMultipleMatchingResults()) {
						execResult.getInstances().addAll(
								matchResult.getInstances());
					} else {
						execResult.addNewInstance(matchResult);
					}

				}
			}
			if (elementVector.size() > 0)
				execResult.setSuccess(true);
			break;
		case Matcher.MODE_MAP:
			Vector<Node> mapVector = builtinAtom.getChildNodes(ATTR_MAP).get(0)
					.getChildNodes();

			int i = 0;
			for (Node map : mapVector) {
				// Match each fact atom with the fact query
				// expression/constructor
				MatchResult matchResult = m.match(qexpr, map, Matcher.MODE_SET);
				// The fact atom index is the index of the current matching fact
				// atom.
				matchResult.setFactAtomIndex(i++);

				// If they are matched, a new clause must be created
				if (matchResult.isSuccess()) {
					if (matchResult.isMultipleMatchingResults()) {
						execResult.getInstances().addAll(
								matchResult.getInstances());
					} else {
						execResult.addNewInstance(matchResult);
					}

				}
			}
			if (mapVector.size() > 0)
				execResult.setSuccess(true);
			break;
		}

		return execResult;

	}

}
