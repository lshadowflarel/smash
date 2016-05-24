package net.sf.xet.nxet.builtin.corefunctions;

import java.io.PrintStream;
import java.util.Map;
import java.util.Vector; 
import java.util.Iterator; 

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;
import net.sf.xet.nxet.matcher.MatchResult;
import net.sf.xet.nxet.matcher.Matcher;

/**
 * @author LUONG VIET PHONG
 * 
 * TODO Unify to Evar varibale
 * Example:<br>
 * <xfn:Unify><br>
 * Evar_E1 <br>
 * Evar_E2 <br>
 * </xfn:Unify> <br>
 * The value of Evar_E2 will be copied to Evar_E1 
 * 
 */
		
public class Unify extends Builtin {
	/**
     * A construct to create a builtin object from a builtin atom
     * and an output redirector.
     * 
     * @param builtinAtom The builtin atom
     * @param world The world
     * @param place The place the built-in is executed
     * @param printStream The output redirector
     */
    public Unify (Node builtinAtom, World world, Integer place, PrintStream out) {
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
    public Unify (Node builtinAtom, World world, Integer place) {
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
        return new Boolean((builtinAtom.numberOfChildNodes() == 2) &&(builtinAtom.childNode(0).childNode( 0).getNodeType()==Node.NT_EVAR ) );
    }
    /**
     *  Try to copy the value of Evar_E2 to Evar_E1
     */
    
    public ExecutionResult execute() throws NotExecutableException {
        
    	Node n1;    	     
        
     	n1 = builtinAtom.childNode(0).childNode( 0);
     	String nodename = n1.fullName () ;
    	
    	n1.removeAllNodes() ;
    	for(int i=0;i<builtinAtom.childNode(1).numberOfChildNodes() ;i++)
    	{
    		Node n2 = builtinAtom.childNode(1).childNode(i);
    		n1.addChildNode(n2.cloneNode()  );
    	}
//    	n1.removeAllAtributes() ;
    	
    //	n1.setNodeType( n2.getNodeType() );
//    	n1.setNodeName( n2.getNodeName( ));
  //n1.setNodeValue( n2.getNodeName() ); 
  /*  	
    	Iterator i = n2.attributeKey() ;
    	while(i.hasNext() )
    	{
    		String name = (String) i.next() ;
    		n1.addAttribute( n2.attribute(name));
    	}
    	for(int j=0; j< n2.numberOfChildNodes() ;j++)
    	{
    		n1.addChildNode(n2.childNode( j).cloneNode()  );
    	}
    	
    */	
    	Specialization spec = new Specialization();
		
		spec.addSpecialization(nodename , n1);
        ExecutionResult execResult = new ExecutionResult(true,spec);
        
        return execResult;
    }

}
