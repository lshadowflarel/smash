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
package net.sf.xet.nxet.builtin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.World;

/**
 * Built-in Manager class has three responsibility.<br>
 * <br>
 * 1. Register built-in<br>
 * <br>
 * The fully quantified name of every built-in must be registered 
 * with a Built-in Manager before it can be called and used.
 * In the current version, the built-in registration is still static
 * and hard-coded into the class. Later, this will be better
 * manipulated.<br>
 * <br>
 * 2. Check if an atom is a built-in<br>
 * The built-in manager allows the comparison of the name of an 
 * atom and the name of all registered built-in.<br>
 * <br>
 * 3. Execute a built-in
 * The built-in manager will try to locate the built-in class and
 * execute. If there is error, the built-in manager will throw
 * a NotExecutableException with the reason of the error.
 * 
 * @author Paramai Supadulchai
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BuiltinManager {
    
    // The name of the method "verifyPlace" from the built-in abstract class
    private static final String METHOD_VERIFY_PLACE = "verifyPlace";
    // The name of the method "isMatched" from the built-in abstract class
    private static final String METHOD_IS_MATCHED = "isMatched";
    // The name of the method "isExecutable" from the built-in abstract class
    private static final String METHOD_IS_EXECUTABLE = "isExecutable";
    // The name of the method "execute" from the built-in abstract class
    private static final String METHOD_EXECUTE = "execute";
    // The built-in registry containing all names of the registered built-ins
    private final HashSet<String> builtinList = new HashSet<String>();
    // The built-in namespace registry matches the URI namespace to a Java namespace
    private final HashMap<String, String> builtinNamespace = new HashMap<String, String>();
    // The world that executes a built-in
    private World world = null;
	// The path to builtins
	private String builtinPath = null;
    
    /**
     * A constructor to create a new built-in. The constructor registers also
     * built-ins to the built-in registry.
     * 
     * @param world The world that executes a built-in
     */
    public BuiltinManager(World world, String builtinPath) {
        this.world = world;
		this.builtinPath = builtinPath;
        this.registerBuiltin(this.builtinPath);
    }
    
    /**
     * Create an instance of object from a specified constructor and its arguments
     * 
     * @param constructor a constructor of the class we want to create
     * @param arguments arguments supplied for the constructor to create
     * @return an object created from the constructor specified
     */
    private Object createObject(Constructor constructor, Object[] arguments) throws Exception {
		Object object = null;
		object =  constructor.newInstance(arguments);
		return object;
	}
    
    /**
     * This method will be invoked from other classes when there are needs
     * to execute a builtin atom. "IsBuiltin" method should be used to
     * check the availability of the builtin functions before using this
     * method.
     * 
     * @param builtinAtom a builtin atom to execute
     * @return results in an object of type "ExecutionResult"
     */
    public ExecutionResult executeBuiltin(Node builtinAtom, int place) throws NotExecutableException {
        
        Thread thread = Thread.currentThread();
        
        ClassLoader cl = thread.getContextClassLoader();
       
        try {
        
            // Map the URI namespace to Java namespace
            String javaNamespace = (String)builtinNamespace.get(builtinAtom.getUri());
            
            // Create a built-in full java classname
            String uriName = javaNamespace + "." + builtinAtom.getNodeName();
            
            // Create a "class" object from a given name
            Class builtinClass = cl.loadClass(uriName);
            
            // Parameters for the constructor (object creation)
            Class[] argumentClass = new Class[] {Node.class, World.class, Integer.class};
            
            // Get constructor of the builtin class
            Constructor constructor = builtinClass.getConstructor(argumentClass);
            
            // Parameters for the created object (execution method)
            Object[] argumentParam = new Object[] {builtinAtom, this.world, new Integer(place)};
            
            // Create an object from using a specified constructor and given parameters
            Object builtinObject = createObject(constructor, argumentParam);
            
            /*--------------------------------------------------------------*/
            /* Check if the built-in is used in the right place             */
            /*--------------------------------------------------------------*/
            
            // Check if the builtin is used in the right place
            Method method = findMethod(builtinClass, BuiltinManager.METHOD_VERIFY_PLACE);
            
            // Execute the method and get the result
            Object result = method.invoke(builtinObject, new Object[0]);
            
            if (!((Boolean)result).booleanValue()) {
                
                throw new NotExecutableException(NotExecutableException.WRONG_PLACE);
                
            }
            
            /*--------------------------------------------------------------*/
            /* IsMatched method                                             */
            /*--------------------------------------------------------------*/
            
            // Check if the builtin is matched or not by using a method "isMatched"
            method = findMethod(builtinClass, BuiltinManager.METHOD_IS_MATCHED);
            
            // Execute the method and get the result
            //result = method.invoke(builtinObject, null);
            result = method.invoke(builtinObject, new Object[0]);
            
            if (!((Boolean)result).booleanValue()) {
                
                throw new NotExecutableException(NotExecutableException.DIFFERENT_STRUCTURE);
                
            }
                
            /*--------------------------------------------------------------*/
            /* IsExecutable method                                          */
            /*--------------------------------------------------------------*/
            
            // Check if the builtin is matched or not by using a method "isMatched"
            method = findMethod(builtinClass, BuiltinManager.METHOD_IS_EXECUTABLE);
            
            // Execute the method and get the result
            result = method.invoke(builtinObject, new Object[0]);
            
            if (!((Boolean)result).booleanValue()) {
                
                throw new NotExecutableException(NotExecutableException.CONTAINS_VARIABLES);
                
            }
            
            /*--------------------------------------------------------------*/
            /* Execute method                                               */
            /*--------------------------------------------------------------*/
            
            // Find a method "BUILTIN_METHOD" from the class we want to create
            method = findMethod(builtinClass, BuiltinManager.METHOD_EXECUTE);
            
            // Execute the method and get the result
            result = method.invoke(builtinObject, new Object[0]);
            
            // Return the execution result
            return (ExecutionResult)result;
            
        } catch (ClassNotFoundException e) {
            
            //e.printStackTrace();
            throw new NotExecutableException(NotExecutableException.BUILTIN_NOTFOUND , 
                                             "Cannot locate \"" + builtinAtom.getNodeName() + "\" builtin.");
        } catch (InvocationTargetException e) {
            if (e.getTargetException().getClass().getSimpleName().equals(NotExecutableException.class.getSimpleName())) {
                throw new NotExecutableException(((NotExecutableException)e.getTargetException()).getReason(), 
                                                 e.getTargetException());
            } else {
                e.printStackTrace();
            }
        } catch (InstantiationException e) {
		    e.printStackTrace();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		} catch (IllegalArgumentException e) {
		    e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new NotExecutableException(NotExecutableException.INVALID_BUILTIN , 
                    						 "Builtin \"" + builtinAtom.getNodeName() + "\" is invalid.");
		} catch (NotExecutableException e) {
		    throw e;
		} catch (Exception e) {
		    e.printStackTrace();
		}
        
        return null;
        
    }
    /**
     * Find a method and get the "method description" from a class object
     * 
     * @param builtinClass A class we want to search for a method
     * @param methodName The name of a method we want to find
     * @return Returns the Method reflection object
     * @throws Exception The Java reflection-oriented exception 
     */
    private Method findMethod(Class builtinClass, String methodName) throws Exception {
        Method[] methods = builtinClass.getMethods();
        for (int i=0; i < methods.length; i++) {
	        if (methods[i].getName().equals(methodName)) {
	            return methods[i];
	        }
	    }
	    return null;
	}
	/**
	 * Returns the builtin path
	 * 
	 * @return Returns the builtin path
	 */
	public String getBuiltinPath() {
		return this.builtinPath;
	}
    /**
     * Check if the name of the builtin atom matches any of the available
     * builtin functions.
     * 
     * @param uri The "uri" of a builtin atom to be executed
     * @param  builtinName The local name of the builtin
     * @return Returns true if a name in the builtin list matches the 
     *         builtin atom's name
     */
    public boolean isBuiltin(String uri, String builtinName) {
        String javaNamespace = (String)builtinNamespace.get(uri);
        return builtinList.contains(javaNamespace + "." + builtinName);
    }
    
    /**
     * This functions register built-ins from jar files
     */
    public void registerBuiltin(String builtinPath) { 
    	builtinNamespace.put("http://tapas.item.ntnu.no/NxET/built-in/corefunctions", 
		 					 "net.sf.xet.nxet.builtin.corefunctions");
		builtinNamespace.put("http://tapas.item.ntnu.no/NxET/built-in/string", 
		 					 "net.sf.xet.nxet.builtin.string");
		builtinNamespace.put("http://tapas.item.ntnu.no/NxET/built-in/custom/corefunctions", 
							 "net.sf.xet.nxet.builtin.custom.corefunctions");
		builtinNamespace.put("http://tapas.item.ntnu.no/NxET/built-in/time",
							 "net.sf.xet.nxet.builtin.time");
		builtinNamespace.put("http://smash.item.ntnu.no/NxET/built-in",
							 "no.ntnu.item.smash.css.nxet.builtin");
		
		try {
			File f = new File(builtinPath + File.separator + "lib");
			String[] fileList = f.list();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].startsWith("xfn-") && fileList[i].endsWith(".jar")) {
					JarFile builtinFile = new JarFile(builtinPath + 
							   						  File.separator + "lib" +
							   						  File.separator + fileList[i]);
					Enumeration<JarEntry> e = builtinFile.entries();
					while (e.hasMoreElements()) {
						JarEntry je = e.nextElement();
						String entryName = je.toString();
						if (entryName.endsWith(".class")) {
							String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
							//System.out.println("Class name added = " + className);
							builtinList.add(className);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
}
