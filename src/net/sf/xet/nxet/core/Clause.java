/*
 * Native XML Equivalent Transformation Software Development Kit (NxET)
 * Copyright (C) 2004-2005, Telematics Architecture for Play-based Adaptable System,
 * (TAPAS), Norwegian University of Technology and Science
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

package net.sf.xet.nxet.core;

import java.util.Iterator;
import java.util.Vector;
import java.util.Random;
import java.util.HashSet;

/**
 * Clause is the way to write a sentence from several atoms. It can
 * be written in the form:<br>
 * <br>
 * H <- Bi<br>
 * <br>
 * Where H is the constructor of the clause and Bi are the body of
 * the clause, where i >= 0. The meaning of the clause means that
 * the head of the clause will be derived if all body of the clause
 * holds. For example, consider a following ET clause:<br>
 * <br>
 * Clause A: (ans *X) <-- (rem 8 3 *X).<br>
 * <br>
 * <The example is taken from the ET book chapter 3><br>
 * <br>
 * "Clause A means, "If a reminder of 8 divided by 3 is *X, *X is the
 * answer". In ET, programming, it's the computation to transform
 * the atoms in the right side of a clause. When all atoms are applied
 * and nothing left in the right side, the problem is solved."<br>
 * <br>
 * If a clause does not have a single body (the right side is empty)
 * (or i = 0), the clause is called a unit clause, which represents
 * an unconditional sentence or a fact<br>
 * <br>
 * Here is the example.<br>
 * <br>
 * (ans 2) <--<br>
 * <br>
 * In NxET, a clause can be either a "D-clause" or an "N-clause", but
 * not both at the same time. A D-clause will be further matched only
 * with D-rules. An N-clause will be further matched by both N- and D-
 * rules depending on the structure of an initial N-rule.<br>
 * <br>
 * NxET needs an initial clause called "a query clause". NxET processes
 * a query file and give a result in a query "Node" (see the Node object)
 * let's say Q. NxET then creates a copy of a query node and make a
 * query clause from two identical Q node. Therefore, a query clause
 * can be written in form:<br>
 * <br>
 * Q <-- Q<br>
 * <br>
 * If Q can be matched by one of the head of the rules, the program
 * continues. If Q cannot be matched with any of the rules, NxET
 * terminates. And the answer is given as "Q <--Q".<br>
 * <br>
 * @author Paramai Supadulchai
 */
public class Clause implements Cloneable {

    /**
     * Type of clause D = Deterministic
     */
    public static final int D_CLAUSE = 0;
    /**
     * Type of clause N = Non-deterministic
     */
    public static final int N_CLAUSE = 1;
    // The current active body atom index
    // since NxET 0.3
    private int activeBodyAtomIndex = 0;
    // The list containing all body atoms of this clause
    private Vector<Node> bodyAtoms = null;
    // The name of the clause (for easily debugging)
    private String clauseName = null;
    // The type of this clause (D-clause or N-clause)
    // since NxET 0.2
    private int clauseType = 0;
    // The head atom of this clause
    private Node headAtom = null;
    // The random object
    // since NxET 0.3
    private Random random = null;
    // The number of un-executable body atoms
    // since NxET 0.3
    private int unExecutableAtomNo = 0;

    /**
     * Constructor to create a new clause
     * (The body list will be created.)
     */
    public Clause() {
        random = new Random(System.currentTimeMillis());
        this.bodyAtoms = new Vector<Node>();
    }

    /**
     * Add a new body atom to this clause.
     *
     * @param bodyAtom a new body atom to add
     */
    public void addBodyAtom(Node bodyAtom) {
        this.bodyAtoms.add(bodyAtom);
    }

    /**
     * Return a body atom, specified by an index, of this clause.
     *
     * @param atomIndex the index of the body atom to retrieve
     * @return the body atom specified by atomIndex
     */
    public Node bodyAtom(int atomIndex) {
        return bodyAtoms.get(atomIndex);
    }
    /**
     * Clear the un-executable list
     */
    public void clearUnExecutableList() {
        this.unExecutableAtomNo = 0;
    }
    /**
     * Clone this clause.
     *
     * @return an object copied from this clause
     */
    public Object clone() {

        Clause c = new Clause();
        c.setHeadAtom(this.getHeadAtom().cloneNode());
        c.setClauseType(this.getClauseType());

        for (int i = 0; i< this.bodyAtoms.size(); i++) {
            c.addBodyAtom((this.bodyAtoms.get(i)).cloneNode());
        }
        c.activeBodyAtomIndex = this.activeBodyAtomIndex;
        c.setRandomSeed(System.currentTimeMillis());
        c.unExecutableAtomNo = this.unExecutableAtomNo;
        return c;
    }

    /**
     * Clone this clause and cast it as a clause object.
     *
     * @return a new clause instance copied from this clause
     * @see #clone()
     */
    public Clause cloneClause() {

        return (Clause)(this.clone());

    }

    /**
     * Get the first body atom of this clause
     *
     * @return the first body atom
     */
    public Node firstBodyAtom() {
        return this.bodyAtom(0);
    }
    /**
     * @return Returns the clauseName.
     */
    public String getClauseName() {
        return clauseName;
    }
    /**
     * Return the type of the clause
     * 0 = D-clause, 1 = N-clause
     *
     * @return Returns the clauseType.
     * @since NxET 0.2
     */
    public int getClauseType() {
        return clauseType;
    }

    /**
     * Get the head atom of the clause
     *
     * @return the headAtom of the clause
     */
    public Node getHeadAtom() {
        return this.headAtom;
    }

    /**
     * Verify and return true if there are more body atom(s).
     *
     * @return true if the size of the body atom vector is more than zero
     */
    public boolean hasMoreBodyAtoms() {
        return (this.bodyAtoms.size() > 0);
    }

    public void incrementUnExecutableBodyAtomNo() {
        this.unExecutableAtomNo++;
    }

	/**
     * A private function used to output "spaces" at a specific amount.
     * The function is used by "printXML" to properly indent all the tags.
     * The input parameter "indentLevel" will be multiplied by "  " (two
     * empty spaces).
     *
     * @return Returns a specific amount of spaces.
     */
    private String indent(int indentLevel) {
        StringBuffer sb = new StringBuffer();
        String indent = "  ";
        for (int i = 0; i < indentLevel; i++) {
            sb.append(indent);
        }
        return sb.toString();
    }

    /**
     * Instantiate variables in a specific body atom using the
     * given specialization system
     *
     * @param atomIndex the index of the body atom to be instantiated
     */
    public void instantiateBodyAtom(Specialization spec, int atomIndex) {
        spec.instantiateVariables(this.bodyAtom(atomIndex));
    }

    /**
     * Instantiate variables in the head atom using the given specialization
     *
     * @param spec a specialization containings set of variables to
     *             instantiate head atom
     */
    public void instantiateHeadAtom(Specialization spec) {
        spec.instantiateVariables(this.headAtom);
    }

    /**
     * Instantiate all variables in every atom in this clause.
     *
     * @param spec a specialization containings set of variables to
     *             instantiate the clause
     */
    public void instantiateVariables(Specialization spec) {
    	instantiateHeadAtom(spec);

    	for (int i = 0; i < this.bodyAtoms.size(); i++) {
        	instantiateBodyAtom(spec, i);
        }
    }

    /**
     * Verify and return true if there is no body atom.
     *
     * @return true if the size of the body atom vector is zero
     */
    public boolean isUnitClause() {
        return (this.bodyAtoms.size() == 0);
    }

    /**
     * This function moves the first body atom to
     * the last position.
     */
    public void moveFirstBodyAtomToLastPosition() {
        Node firstBodyAtom = (Node)this.bodyAtoms.get(0);
        this.bodyAtoms.insertElementAt(firstBodyAtom, this.bodyAtoms.size());
        this.bodyAtoms.remove(0);
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
     * Print the texture XML representation of this clause using an ET style.
     *
     * @param out the PrintStream that this function will send outputs to
     */
    public String printClauseETStyle() {
        StringBuffer sb = new StringBuffer();
        sb.append(headAtom.printXML());
        sb.append("\n\t\t <- ");
        for (int i = 0; i < this.numberOfBodyAtoms(); i++) {
            sb.append(((i >= 0)? "\n": "") +
                      ((Node)bodyAtoms.get(i)).printXML() +
                      (((i != this.numberOfBodyAtoms()-1) &&
                      (this.numberOfBodyAtoms() > 0))? ",": ""));
        }
        sb.append(".\n");
        return sb.toString();
    }

    /**
     * Print the texture XML representation of this clause using an original XML style.
     * This function will not print the whole clause structure if it is a unit clause.
     *
     * @param level The indentation level
     */
    public String printClauseXMLStyle(int level) {
    	StringBuffer sb = new StringBuffer();
		if (this.isUnitClause()) {
			sb.append(headAtom.printXML(level) + "\n");
		} else {
	    	sb.append(indent(level) + "<xet:Clause>\n");
	    	sb.append(indent(level + 1) + "<xet:Head>");
	        sb.append(headAtom.printXML(level + 2));
	        sb.append("\n" + indent(level + 1) + "</xet:Head>\n");
	        sb.append(indent(level + 1) + "<xet:Body>");
	        for (int i = 0; i < this.numberOfBodyAtoms(); i++) {
	        	sb.append(bodyAtoms.get(i).printXML(level + 2));
	        	//sb.append("\n");
	        }
	        sb.append(indent(level + 1) + "</xet:Body>\n");
	        sb.append(indent(level) + "</xet:Clause>\n");
		}
        return sb.toString();
    }

    /**
     * Get a body atom randomly
     *
     * @return Returns a body atom randomly
     */
    public Node randomActiveBodyAtom() {
        activeBodyAtomIndex = random.nextInt(this.bodyAtoms.size());
        return this.bodyAtom(activeBodyAtomIndex);
    }

    /**
     * Removes the current active body atom.
     * The active body atom is usually selected
     * non-deterministically by the clause.
     */
    public void removeActiveBodyAtom() {
        bodyAtoms.remove(this.activeBodyAtomIndex);
    }

    /**
     * Remove a body atom, specified by an index, from this clause.
     *
     * @param atomIndex the body atom index
     */
    public void removeBodyAtom(int atomIndex) {
        bodyAtoms.remove(atomIndex);
    }

    /**
     * Remove the first body atom from the atom list
     *
     */
    public void removeFirstBodyAtom() {
        removeBodyAtom(0);
    }
    /**
     * @param clauseName The clauseName to set.
     */
    public void setClauseName(String clauseName) {
        this.clauseName = clauseName;
    }
    /**
     * Set clause type ... the valid value is 0 or 1,
     * which represents D-clause and N-clause respectively
     *
     * @param clauseType The clauseType to set.
     * @since NxET 0.2
     */
    public void setClauseType(int clauseType) {
        this.clauseType = clauseType;
    }

    /**
     * Assign the head atom of this clause
     * with a node
     *
     * @param headAtom the headAtom to set
     */
    public void setHeadAtom(Node headAtom) {
        this.headAtom = headAtom;
    }

    /**
     * Set random seed. When the node is cloned,
     * a different seed will be set to the newly
     * closed node.
     *
     * @param seed The seed to set
     */
    public void setRandomSeed(long seed) {
        this.random.setSeed(seed);
    }

    /**
     * Get the size of the un-executable list
     *
     * @return Returns the size of the un-executable list
     */
    public int unExecutableAtomNo() {
        return this.unExecutableAtomNo;
    }

    /**
     * Retrive the full set of the uninstantiated variables
     * in this clause
     *
     * @return The set of the uninstantiated variables in
     * this clause
     */
    public HashSet uninstantiatedVariableSet() {
        HashSet<String> variableSet = this.headAtom.uninstantiatedVariableSet();
        for (int i = 0; i < this.bodyAtoms.size(); i++) {
            HashSet<String> bodySet = this.bodyAtoms.get(i).uninstantiatedVariableSet();
            Iterator<String> it = bodySet.iterator();
            while (it.hasNext()) {
                variableSet.add(it.next());
            }
        }
        return variableSet;
    }
}