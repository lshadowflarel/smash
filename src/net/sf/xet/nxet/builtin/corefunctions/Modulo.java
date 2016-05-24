/*
 * Created on 7 Á.¤. 2548
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.xet.nxet.builtin.corefunctions;

import java.io.PrintStream;
import java.text.DecimalFormat;

import org.nfunk.jep.JEP;
import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Modulo extends Builtin {

    public final String ATTR_NUMBER1 = "number1";
    public final String ATTR_NUMBER2 = "number2";
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
    public Modulo(Node builtinAtom, World world, Integer place, PrintStream out) {
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
    public Modulo(Node builtinAtom, World world, Integer place) {
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
		JEP mathParser = new JEP();
		mathParser.setAllowUndeclared(true);
		mathParser.parseExpression(builtinAtom.attributeByLocalName(ATTR_NUMBER1).getNodeValue() + " % " +
								   builtinAtom.attributeByLocalName(ATTR_NUMBER2).getNodeValue());
		// DecimalFormat and setDecimalSeparatorAlwaysShown is to convert for example
		// 12345.00 -> 12345 alone
		Double evaluationResult = mathParser.getValue();
		DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setDecimalSeparatorAlwaysShown(false);
		String result = df.format(evaluationResult);
		spec.addSpecialization(resultVariable, result);
		ExecutionResult execResult = new ExecutionResult(true, spec);
		execResult.setResultDescription(" [" + resultVariable + " <- " + result + " = " +
										builtinAtom.attributeByLocalName(ATTR_NUMBER1).getNodeValue() + " % " +
										builtinAtom.attributeByLocalName(ATTR_NUMBER2).getNodeValue() + "]");
        return execResult;
    }

}
