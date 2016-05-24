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
import net.sf.xet.nxet.data.DataSource;
import net.sf.xet.nxet.data.URINotSupportedException;
import net.sf.xet.nxet.matcher.Matcher;
import net.sf.xet.nxet.matcher.MatchResult;
import net.sf.xet.nxet.core.World;

/**
 * @author paramai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FactQuery extends Builtin {

    public static final String ATTR_NAME = "name";
    public static final String ATTR_URI = "uri";
    public static final String ATTR_RELOAD = "reload";
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
    public FactQuery(Node builtinAtom, World world, Integer place, PrintStream out) {
        super(builtinAtom, world, place, out);
        this.setBuiltinType(Builtin.BT_N);
    }

    /**
     * A construct to create a builtin object from a builtin atom
     * 
     * @param builtinAtom The builtin atom
     * @param world The world that this built-in was being executed.
     * @param place The place the built-in is executed
     */
    public FactQuery(Node builtinAtom, World world, Integer place) {
        super(builtinAtom, world, place);
        this.setBuiltinType(Builtin.BT_N);
    }

    /**
     * Check if the structure of the builtin atom matches
     * this builtin function. A built-in function usually
     * requires some parameters. This function checks if
     * those required parameters exist in the built-in atom
     * or not<br>
     * This function requires only 1 parameter<br>
     * - parameter uri
     * 
     * @return Returns true if matched
     * @see net.sf.xet.nxet.builtin.Builtin#isMatched()
     */
    public Boolean isMatched() {
        return new Boolean(builtinAtom.hasAttributeByLocalName(ATTR_URI));
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
     * parameter has been instantiated:<br>
     * - parameter uri<br>
     * 
     * @return Returns true if the built-in is executable
     * @see net.sf.xet.nxet.builtin.Builtin#isExecutable()
     */
    public Boolean isExecutable() {
        
        return new Boolean(!(builtinAtom.attributeByLocalName(ATTR_URI).isVariable()));
        
    }

    /**
     * Execute the builtin function. There might be some
     * variables instantiated during the execution. 
     * These variables will be stored in a generated 
     * specialization object, which will be included 
     * in the returned ExecutionResult object.<br>
     * <br>
     * Each matching result will be stored in the list  
     * of matching result (instances).<br>
     * If the built-in function cannot find any atom<br>
     * that matches, it will return FALSE execution
     * success result. Otherwise, it will return TRUE 
     * execution success result.<br>
     * 
     * @return Returns an execution result object.
     */
    public ExecutionResult execute() throws NotExecutableException {
        String name = null;
        int mode = 0;
        boolean reload = false;
        
        // Get the name attribute. If it does not exist, use URL as the name
        if (builtinAtom.hasAttributeByLocalName(ATTR_NAME)) {
            name = builtinAtom.attributeByLocalName(ATTR_NAME).getNodeValue();
        } else {
            name = builtinAtom.attributeByLocalName(ATTR_URI).getNodeValue();
        }
        
        // Get the reload attribute. If it does not exist, the default value will be "false"
        if (builtinAtom.hasAttributeByLocalName(ATTR_RELOAD)) {
            reload = Boolean.parseBoolean(builtinAtom.attributeByLocalName(ATTR_RELOAD).getNodeValue());
        }
        
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
        
        // Get the URI attribute. It must exist...
        String uri = builtinAtom.attributeByLocalName(ATTR_URI).getNodeValue();
        
        boolean onTheFly = false;
        
        ExecutionResult execResult = new ExecutionResult(true);
        
        Matcher m = new Matcher(world.getConfig());
        
        int numberOfMatch = 0;
        
        Node fact = null;
        
        // Obtain the fact from the data source
        try {
            if (!world.getDataSources().isDataSourceExist(uri)) {
				world.getDataSources().createDataSource(name, uri, 
														true, true, 
														world.getDataSources().getOriginalPath());
                onTheFly = true;
            }
            DataSource dataSource = world.getDataSources().dataSourceByUri(uri);
	        if (dataSource.isReload() && !onTheFly) {
	            world.getDataSources().reloadDataSourceByUri(dataSource.getUri());
	        }
	        fact = dataSource.getData();
	        
        } catch (URINotSupportedException e) {
            
            e.printStackTrace();
            return execResult;
            
        } catch (NullPointerException e) {
            
            // Probably the user specified a data source that does not exist
            out.println("Cannot obtain the data source " + uri);
            e.printStackTrace();
            return execResult;
            
        }
        
        // A Fact Query Expression or A Fact Query Constructor
		// It is assumed that there is only 1 possible expression between <FactQuery>...</FactQuery>
		// That is, <FactQuery><Name>Svar_Name</Name></FactQuery> is allowed.
		// However, <FactQuery><Name>Svar_Name</Name><Age>Svar_Age</Age></FactQuery> is not allowed.
		Node qexpr = builtinAtom.childNode(0);
		
		// Loop true the fact and check them with the query string from 
		for (int i=0; i < fact.numberOfChildNodes(); i++) {
			
			// Match each fact atom with the fact query expression/constructor
		    MatchResult matchResult = m.match(qexpr, fact.childNode(i), mode);
		    // The fact atom index is the index of the current matching fact atom.
		    matchResult.setFactAtomIndex(i);
		    		    
		    // If they are matched, a new clause must be created
		    if (matchResult.isSuccess()) {
		    	numberOfMatch++;
		        if(matchResult.isMultipleMatchingResults()) {
		        	execResult.getInstances().addAll(matchResult.getInstances());
		        } else {
		        	execResult.addNewInstance(matchResult);
		        }
		        		        
		    }
		    
		}
		
		if (numberOfMatch == 0) {
		    execResult.setSuccess(false);
		}
		
		return execResult;
		
    }

}
