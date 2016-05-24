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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;

/**
 * Add function adds<br>
 * <br>
 * - parameter number1<br>
 * - parameter number2<br>
 * <br>
 * together<br>
 * The parameter "result" will be added in 
 * the returned specialization object in the 
 * returned execution result. The execution 
 * result' success is TRUE when two numbers 
 * can be added successfully. Otherwise, it 
 * will return FALSE.<br>
 * <br>
 * The Add built-in can be used as a D-atom 
 * or an N-atom.
 * 
 * @author Paramai Supadulchai
 */
public class ExtractTime extends Builtin {
    
    public final String ATTR_NUMBER1 = "time";
    public final String ATTR_NUMBER2 = "aspect"; // y=year,mon=month,d=day,h=hour,min=minute,sec=second
    public final String ATTR_RESULT = "result";

    /**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public ExtractTime(Node builtinAtom, World world, Integer place, PrintStream out) {
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
    public ExtractTime(Node builtinAtom, World world, Integer place) {
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
     * This function requires three parameters as follows:<br>
     * - parameter number1<br>
     * - parameter number2<br>
     * - parameter result<br>
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        
        return new Boolean(builtinAtom.hasAttributeByLocalName(ATTR_NUMBER1) &&
                		   builtinAtom.hasAttributeByLocalName(ATTR_NUMBER2) &&
                		   builtinAtom.hasAttributeByLocalName(ATTR_RESULT));
    
    }

    /**
     * After the structure of the built-in has been verified,
     * The built-in manager verifies also if the built-in
     * can be executed. A built-in can be executed when 
     * it does not have neccessary variable left uninstantiated.
     * In some built-in the number of child nodes are also
     * checked.<br>
     * <br>
     * This built-in function will be executable if the following
     * parameters have been instantiated:<br>
     * - parameter number1<br>
     * - parameter number2<br>
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        
        // TODO Auto-generated method stub
        return new Boolean(!(builtinAtom.attributeByLocalName(ATTR_NUMBER1).isVariable()) && 
                		   !(builtinAtom.attributeByLocalName(ATTR_NUMBER2).isVariable()));
        
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * The result value is added in the variable specified
     * in the "result" parameter. <br> The built-in function
     * return TRUE success result if there is no problem
     * in parsing number parameters and perform the
     * operation on them. Otherwise it will return FALSE.
     * 
     * @return Returns an execution result object.
     */
    public ExecutionResult execute() throws NotExecutableException {
        
        Specialization spec = new Specialization();
        String resultVariable = builtinAtom.attributeByLocalName(ATTR_RESULT).getNodeValue();
        String time = builtinAtom.attributeByLocalName(ATTR_NUMBER1).getNodeValue();
        String aspect = builtinAtom.attributeByLocalName(ATTR_NUMBER2).getNodeValue();
		String result = "";
        
        Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH); //Mon Feb 08 07:00:00 CET 2010
		Date d1;
		try {
			d1 = df.parse(time);
			cal.setTime(d1);
			
			if(aspect.equals("y")) {
				result = ""+cal.get(Calendar.YEAR);
			} else if(aspect.equals("mo")) {
				result = ""+cal.get(Calendar.MONTH);
			} else if(aspect.equals("d")) {
				result = ""+cal.get(Calendar.DAY_OF_MONTH);
			} else if(aspect.equals("h")) {
				result = ""+cal.get(Calendar.HOUR_OF_DAY);
			} else if(aspect.equals("min")) {
				result = ""+cal.get(Calendar.MINUTE);
			} else if(aspect.equals("s")) {
				result = ""+cal.get(Calendar.SECOND);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}      		
		
		spec.addSpecialization(resultVariable, result);
		ExecutionResult execResult = new ExecutionResult(true, spec);
		execResult.setResultDescription(" [" + resultVariable + " <- " + result + " = " +
										builtinAtom.attributeByLocalName(ATTR_NUMBER1).getNodeValue() + " + " +
										builtinAtom.attributeByLocalName(ATTR_NUMBER2).getNodeValue() + "]");
		return execResult;
    }
}
