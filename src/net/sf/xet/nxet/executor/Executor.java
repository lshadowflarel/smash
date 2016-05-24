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
 */
package net.sf.xet.nxet.executor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.xet.nxet.builtin.Builtin;
import net.sf.xet.nxet.builtin.BuiltinManager;
import net.sf.xet.nxet.builtin.ExecutionResult;
import net.sf.xet.nxet.builtin.NotExecutableException;
import net.sf.xet.nxet.config.ConfigKey;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.config.DefaultConfig;
import net.sf.xet.nxet.core.Clause;
import net.sf.xet.nxet.core.Node;
import net.sf.xet.nxet.core.Rule;
import net.sf.xet.nxet.core.Specialization;
import net.sf.xet.nxet.core.World;
import net.sf.xet.nxet.data.DataSources;
import net.sf.xet.nxet.data.DirectInputs;
import net.sf.xet.nxet.data.InMemoryObjects;
import net.sf.xet.nxet.matcher.MatchResult;
import net.sf.xet.nxet.matcher.Matcher;
import net.sf.xet.nxet.parser.ConfigParser;
import net.sf.xet.nxet.parser.DataSourceParser;
import net.sf.xet.nxet.parser.QueryParser;
import net.sf.xet.nxet.parser.RuleParser;
import net.sf.xet.nxet.tool.Tool;
import no.ntnu.item.smash.css.core.NxETReasoner;

/**
 * Executor is the core NxET's executing environment. This class holds
 * a default world object, a matcher and a built-in manager. Basically,
 * a developer may access this class and use all functionality without
 * having to call the text command-line processor "NxET" class.
 * 
 * @author Paramai Supadulchai
 */
public class Executor {
    
	/**
	 * Add all properties from the adder to the master properties
	 * 
	 * @param master The master properties
	 * @param adder The properties to add to the master
	 */
	private static void addProperties(Properties master, Properties adder) {
		Enumeration entries = adder.keys();
		while (entries.hasMoreElements()) {
			String key = (String)entries.nextElement();
			master.setProperty(key, adder.getProperty(key));
		}
	}
	
	/**
     * Create a new executor from a given world, a matcher and a built-in manager
     * 
     * @param world The world to execute
     * @param matcher The matcher to match
     * @param builtinMan The built-in manager to call built-in
     * @param out The output
     * @return Returns the executing environment
     */
    public static Executor create(World world, Matcher matcher, 
            					  BuiltinManager builtinMan, Handler logHandler) {
        
        Executor executor = new Executor();
        executor.setWorld(world);
        executor.setMatcher(matcher);
        executor.setBuiltinMan(builtinMan);
        executor.setConfig(world.getConfig());
        executor.setLogHandler(logHandler);
        return executor;
    }
    /**
     * Parse a configuration file and return the configuration.
     * If the configuration file is missing, the default configuration
     * is loaded instead.
     * 
     * @param configFile The path to a configuration file
     * @param config The config file
     * @param logHandler The log file handler
     * @return Returns a configuration object
     */
    private static Configuration parseConfigFile(String configFile, Configuration config, Handler logHandler) {
        if (configFile != null) {
            try {
                // Create a configuration file parser
                ConfigParser configParser = ConfigParser.createParser(configFile, config, logHandler);
                // Parse the configuration file
                configParser.parse();
                // Obtain the parsed config
                return configParser.getConfiguration();
           } catch (Exception e) {
               e.printStackTrace();
                // User does not supply any configuration file
            }
        }
        return new DefaultConfig();
    }
    /**
     * Parse a data source file and return the data source pool (DataSources)
     * 
     * @param dataSourceFile The data source file
     * @param config The config file
     * @param logHandler The log file handler
     * @return Returns the data source pool (DataSources)
     */
    private static DataSources parseDataSourceFile(String dataSourceFile, Configuration config, 
                                                   Handler logHandler) {
        if (dataSourceFile != null) {
    	    // Create a data source parser
            DataSourceParser dataSourceParser = DataSourceParser.createParser(dataSourceFile, 
                                                                 config, logHandler);
            // Parse the data file
            dataSourceParser.parse();
            // Obtain the parsed data sources
            return dataSourceParser.getDataSources();
    	} else {
    	    return new DataSources(config, logHandler);
    	}
    }
    /**
     * Parse an input query file and return the query clause
     * 
     * @param queryFile The input query file
     * @param config The config file
     * @param logHandler The log file handler
     * @return Returns the query clause
     */
    private static Clause parseQueryFile(Properties namespaceRegistry, String queryFile, Configuration config, Handler logHandler) {
        // Create a query parser
        QueryParser queryParser = QueryParser.createParser(queryFile, config, logHandler);
		// Parse the query file
        queryParser.parse();
		// Get the namespace registry
		addProperties(namespaceRegistry, queryParser.getUriPrefixMapper());
        // Obtain the parsed query clause
        return queryParser.getQueryClause();
    }
    
	/**
     * Parse a rule file and return the rule list containing all rules
     * 
     * @param ruleFile The rule file
     * @param config The config file
     * @param logHandler The log file handler
     * @param builtinPath The path to builtin files
     * @return Returns The list of all rules (vector)
     */
    private static Vector<Rule> parseRuleFile(Properties namespaceRegistry, String ruleFile, 
											  Configuration config, Handler logHandler, 
											  String builtinPath) {
        // Create a rule parser
		RuleParser ruleParser = RuleParser.createParser(ruleFile, config, logHandler);
		// Assign the built-in path
		ruleParser.createBuiltinManager(builtinPath);
		// Parse the rule file
		ruleParser.parse();
		// Get the namespace registry
		addProperties(namespaceRegistry, ruleParser.getUriPrefixMapper());
		// Sort all the rules
		ruleParser.sortRule();
		// Obtain the parsed rule set (Vector object)
		return ruleParser.getRules();
	}
    
    // The built-in manager
    private BuiltinManager builtinMan = null;
    // The configuration
    private Configuration config = null;
    // The logger
    private Logger logger = Logger.getLogger(Executor.class.getName() + System.currentTimeMillis());
    // The log handler
    private Handler logHandler = null;
    // The log-level
    private Level logLevel = null;
    // The matcher
    private Matcher matcher = null;
    // The semaphore
    private final Semaphore sem = new Semaphore(1, true);
    // The world
    private World world = null;
	// The namespace registry
	private Properties namespaceRegistry = new Properties();
	
	private NxETReasoner main;
    
	/**
     * A default constructor
     */
    public Executor() {}
    
    /**
     * A constructor to create a new executor environment from the a query file, a rule file
     * data source file and a configuration file. Users must specify the rule file and the
     * query file. However, they can leave the data source file and the configuration file.
     * That is, they can be just null.
     * 
     * @param queryFile The file containing query
     * @param ruleFile The file containing all XET rules
     * @param dataSourceFile The file containing a data source file (optional)
     * @param configFile The file containing all configurations (optional)
     * @param Level The output log-level
     * @param builtinPath The path to builtins
     */
    public Executor(String queryFile, String ruleFile, 
                    String dataSourceFile, String configFile,
                    Handler logHandler, String builtinPath) {
        
        // Set log handler
        this.logHandler = logHandler;
        // Get configuration
        config = Executor.parseConfigFile(configFile, new DefaultConfig(), logHandler);
		// Get data source descriptions
		DataSources parsedDataSources = Executor.parseDataSourceFile(dataSourceFile, config, logHandler);
		if (dataSourceFile == null) {
			File ruleF = new File(ruleFile);
			if (ruleF.getParentFile() != null) {
				parsedDataSources.setOriginalPath(new File(ruleFile).getParentFile().getAbsolutePath());
			} else {
				parsedDataSources.setOriginalPath(System.getProperty("user.dir"));
			}
		}
		// Get rules
        Vector<Rule> parsedRuleVector = Executor.parseRuleFile(namespaceRegistry, ruleFile, 
															   config, logHandler, builtinPath);
		// Get query clause
        Clause parsedQueryClause = Executor.parseQueryFile(namespaceRegistry, queryFile, config, logHandler);
        
        // Create a new "default" world
        this.world = World.create(World.WORLD_DEFAULT, 
                				  parsedQueryClause, 
                                  parsedRuleVector, 
                                  parsedDataSources,
                                  config,
                                  logHandler,
                                  builtinPath,
                                  this); 
        // Create a new matcher
        this.matcher = new Matcher(config);
        // Create a new built-in manager
        this.builtinMan = new BuiltinManager(this.world, builtinPath);
        // Assign the active clause
        this.world.assignActiveClause();
 	}
    
    public void setParent(NxETReasoner main) {
    	this.main = main;
    }
    
    public NxETReasoner getParent() {
    	return main;
    }
    
	/**
	 * Print all clauses in the list of answers.
	 */
    public String answers() {
    	StringBuffer sb = new StringBuffer();
        for (int i = 0; i < world.numberOfAnswers(); i++) {
            sb.append("Answer " + (i + 1) + " (" + world.answer(i).getClauseName() +")");
            sb.append(Tool.LINE);
            sb.append(Tool.ESTR);
            sb.append(world.answer(i).printClauseETStyle());
            sb.append(Tool.LINE);
        }
        return sb.toString();
    }
    
    public String[] cleanAnswers() {
    	String[] answers = new String[world.numberOfAnswers()];
    	
    	for(int i=0; i<answers.length; i++) {
    		answers[i] = world.answer(i).printClauseXMLStyle(0);
    	}
    	
    	return answers;
    }
    
    protected void assignActiveClase() {
        world.assignActiveClause();
    }
    
    /**
     * The execution begins with a clause selected as the active clause.
     * The clause type will be determined whether it is a D-Clause or an
     * N-Clause. The proper execution mode (D or N) will be selected from 
     * the clause type automatically. The active clause will be processed
     * differently depending on the execution mode.<br>
     * <br>
     * In D-execution, the first body of the clause will be selected. The
     * processing result easily tells what will happen. The fail result 
     * will fail the clause immediately. This means the active clause will
     * be removed. Since there can be only one clause in D-execution, this
     * means the program will end.<br>
     * <br>
     * In N-execution, a body of the clause will be selected 
     * non-deterministically. The result from each execution of the 
     * selected body has also different semantic from the D-execution.
     * In N-execution, if an execution sequence body is fail, the clause
     * will be fail and a next clause will be selected as the active 
     * clause. If the clause has no body or there is no rule that can 
     * match at least a body of the clause, the clause will be added to
     * the list of answers. If at least a body can be processed, the 
     * clause is said to have a progress and can be further processed.<br>
     * <br>
     * 
     */
    public void execute() {
        long startTime = System.currentTimeMillis();       
        // Obtain the log-level
        if (world.getWorldName().equals(World.WORLD_SET_OF)) {
            this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_EXECUTOR));
        } else {
			if (logHandler.getLevel() == null) {
				this.logLevel = Tool.level(this.config.get(ConfigKey.LEVEL_EXECUTOR));
			} else {
				this.logLevel = logHandler.getLevel();
			}
        }
        // Set log-handler
        if (logger.getHandlers().length == 0) {
            logger.addHandler(this.logHandler);
        }
        logger.setUseParentHandlers(false);
        // Get a clause
        if (world.getActiveClause() == null) {
            world.assignActiveClause();
        }
        logger.log(logLevel, Tool.LINE);
        // Print the execution mode
        if (world.getActiveClause().getClauseType() == Clause.D_CLAUSE) {
            logger.log(logLevel, "Execution mode = Deterministic");
        } else {
            logger.log(logLevel, "Execution mode = Non-deterministic");
        }
        // Loop until there is no clause left in the world
        while (world.numberOfClauses() > 0) {
            // Get a clause
            world.assignActiveClause();
            // Determine the type of the clause 
            if (world.getActiveClause().getClauseType() == Clause.D_CLAUSE) {
            	// Process the deterministic clause
                processActiveDClauseFirstBodyAtom();
            } else {
                // Process the non-deterministic clause
                logger.log(logLevel, Tool.LINE);
                logger.log(logLevel, "Active Clause: " + world.getActiveClause().getClauseName());
                logger.log(logLevel, Tool.LINE);
                logger.log(logLevel, Tool.ESTR);
                logger.log(logLevel, world.getActiveClause().printClauseETStyle());
                if (world.getActiveClause().numberOfBodyAtoms() ==
                    world.getActiveClause().unExecutableAtomNo()) {
                    world.addAnswer(world.getActiveClause());
                    world.removeActiveClause();
                    logger.log(logLevel, "There is no more executable body atom.");
                    continue;
                }
            	
                // Step through the clause
                processActiveNClauseFirstBodyAtom();
                logger.log(logLevel, Tool.LINE);
                logger.log(logLevel, "Number of clauses = " + world.numberOfClauses() + ". " +
        	                   	     "Number of answers = " + world.numberOfAnswers() + ".");
            }
        }
        long endTime = System.currentTimeMillis() - startTime;
        if (!world.getWorldName().equals(World.WORLD_SET_OF)) {
	        logger.log(Level.SEVERE, Tool.LINE);
	        logger.log(Level.SEVERE, "Execution time = " + endTime + " milliseconds");
	        logger.log(Level.SEVERE, Tool.LINE);
        }
    }
    
    /**
     * This function is called from the 
     * 
     * @param builtinNAtom
     * @param spec
     * @return
     */
    protected ExecutionResult executeDBuiltin(Node builtinNAtom, Specialization spec) {
        ExecutionResult execResult = null;
        
        try {
            if (builtinNAtom.isBuiltin()) { 
                execResult = builtinMan.executeBuiltin(builtinNAtom, Builtin.BT_D);
            } else {
            	execResult = processDAtom(world, builtinNAtom);
            }
            // Determine the result
            if (!execResult.isSuccess()) {
                logger.log(logLevel, "Built-in Sequence Atom " + builtinNAtom.fullName() + " FAILED. " + 
						  ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
                // Although the execution sequence has a false value,
    	        // This is still considered a processed "step".
                execResult.setReason(ExecutionResult.BUILTIN_FAILED);
                return execResult;
            } else {
                logger.log(logLevel, "Built-in Sequence Atom " + builtinNAtom.fullName() + " SUCCESS. " +
                           ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
                //executionResult.merge(execSeqResult.getSpecialization());
                // The execution is success
                world.getActiveClause().instantiateVariables(execResult.getSpecialization());
                return execResult;
            }
        } catch (NotExecutableException e) {
			logger.log(logLevel, "Built-in Sequence Atom " + builtinNAtom.fullName() + " FAILED." + 
					  (((execResult != null) && 
					    (execResult.getResultDescription() != null)) ? execResult.getResultDescription() : ""));
            if (e.getReason() == NotExecutableException.BUILTIN_NOTFOUND) {
                logger.log(logLevel, "Reason: Cannot locate builtin \"" + builtinNAtom.fullName() + "\"");
            } else if (e.getReason() == NotExecutableException.DIFFERENT_STRUCTURE) {
                logger.log(logLevel, "Reason: The structure does not match \"" + builtinNAtom.fullName() + "\"");
            } else if (e.getReason() == NotExecutableException.CONTAINS_VARIABLES)  {
                logger.log(logLevel, "Reason: Unresolvable variable(s) in \"" + builtinNAtom.fullName() + "\"");
            } else if (e.getReason() == NotExecutableException.WRONG_PLACE)  {
                logger.log(logLevel, "Reason: \"" + builtinNAtom.fullName() + "\" is used in a wrong place.");
			} else if (e.getReason() == NotExecutableException.INVALID_PARAMETERS)  {
                logger.log(logLevel, "Reason: \"" + builtinNAtom.fullName() + "\" contains one or more invalid parameter(s).");
            } else {
                logger.log(logLevel, "Reason: Unknown reason " + e.getReason());
            }
            logger.log(logLevel, Tool.LINE);
            logger.log(logLevel, Tool.ESTR);
            logger.log(logLevel, world.getActiveClause().printClauseETStyle());
            logger.log(logLevel, Tool.ESTR);
    	    return new ExecutionResult(false, ExecutionResult.BUILTIN_FAILED);   
        }
    }
    
    protected ExecutionResult executeNBuiltin(Node builtinNAtom) throws NotExecutableException { 
    	Vector<Clause> newClauseList = new Vector<Clause>();
        ExecutionResult execResult = null;   
        
        if (builtinNAtom.isBuiltin()) {
        	execResult = builtinMan.executeBuiltin(builtinNAtom, Builtin.BT_N);
        } else {
        	execResult = processDAtom(world, builtinNAtom);
        }
        // Determine the result
        if (!execResult.isSuccess()) {
            logger.log(logLevel, "Built-in Atom " + builtinNAtom.fullName() + " FAILED. " + 
					  ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
            // Although the execution sequence has a false value,
	        // This is still considered a processed "step".
            execResult.setReason(ExecutionResult.BUILTIN_FAILED);
            return execResult;
        } else {
        	logger.log(logLevel, "Built-in Atom " + builtinNAtom.fullName() + " SUCCESS. " + 
					  ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
            
            if (execResult.getInstances().size() > 0) {
                // Get the match object
                MatchResult activeClauseResult = (MatchResult)execResult.getInstances().get(0);
                logger.log(logLevel, Tool.LINE);
                logger.log(logLevel, "Fact atom " + activeClauseResult.getFactAtomIndex() + " MATCHED.");
                
                // Print specialization if not empty
        	    if (!activeClauseResult.getSpecialization().isEmpty()) {
        	        logger.log(logLevel, Tool.LINE);
        	        logger.log(logLevel, "Specialization (Active Clause)");
        		    logger.log(logLevel, Tool.ESTR);
        		    logger.log(logLevel, activeClauseResult.getSpecialization().printSpecialization());
        	    }
                // If there are more than one matching...
                for (int j = 1; j < execResult.getInstances().size(); j++) {
                    MatchResult additionalClauseResult = (MatchResult)execResult.getInstances().get(j);
                    logger.log(logLevel, Tool.LINE);
                    logger.log(logLevel, "Fact atom " + additionalClauseResult.getFactAtomIndex() + " MATCHED.");
                    // Create a new clause.
                    Clause newClause = world.getActiveClause().cloneClause();
                    // Instantiate variables from the other specialization.
                    //newClause.instantiateVariables(spec);
                    // Instantiate variables from the current fact query
                    newClause.instantiateVariables(additionalClauseResult.getSpecialization());
                    // Remove the executed builtin atom
                    newClause.removeActiveBodyAtom();
                    // Add this new clause to the world
                    world.addClause(newClause);
                    // Add this new clause to the new clause list
                    newClauseList.add(newClause);
                    //	Print specialization if not empty
            	    if (!additionalClauseResult.getSpecialization().isEmpty()) {
            	        logger.log(logLevel, Tool.LINE);
            		    logger.log(logLevel, "Specialization (New Clause " + j + ")");
            		    logger.log(logLevel, Tool.ESTR);
            		    logger.log(logLevel, additionalClauseResult.getSpecialization().printSpecialization());
            	    }
                }
                // Merge specialization
                //execResult.merge(activeClauseResult.getSpecialization());
                world.getActiveClause().instantiateVariables(activeClauseResult.getSpecialization());
            } 
            //executionResult.merge(execSeqResult.getSpecialization());
            // The execution is success
            world.getActiveClause().instantiateVariables(execResult.getSpecialization());
        }
            
        /*
        // Process N atoms of the matched rule with the newly created clause
	    for (int i = 0; i < newClauseList.size(); i++) {
	        Clause newClause = (Clause)newClauseList.get(i);
	        rewriteNAtom(newClause, world.rule(ruleIndex), 
	                     execResult.getSpecialization(), bodyIndex);    
		}*/
        
        return execResult;
        
    }
    
    protected String genUniqueVariableName(HashSet<String> variableList, String variableName) {
        int variableNo = 1;
        String uniqueVariableName = variableName;
        while (!variableList.add(uniqueVariableName)) {
            uniqueVariableName = uniqueVariableName + variableName;
            variableNo++;
        }
        return uniqueVariableName;
    }
    
    /**
     * Returns the answer of the associated world
     * 
     * @return Returns the answer of the associated world 
     */
    public Vector<Clause> getAnswers() {
    	return world.getAnswers();
    }
    
    /**
     * Get the built-in manager assigned for the executor.
     * 
     * @return Returns the builtinMan.
     */
    public BuiltinManager getBuiltinMan() {
        return builtinMan;
    }
    /**
     * @return Returns the config.
     */
    public Configuration getConfig() {
        return config;
    }
    /**
     * @return Returns the log-handler.
     */
    public Handler getLogHandler() {
        return this.logHandler;
    }
    /**
     * @return Returns the log-level.
     */
    public Level getLogLevel() {
        return this.logLevel;
    }
    /**
     * Get the matcher assigned for the executor.
     * 
     * @return Returns the matcher.
     */
    public Matcher getMatcher() {
        return matcher;
    }
    /**
     * Get the world assigned for the executor.
     * 
     * @return Returns the world.
     */
    public World getWorld() {
        return world;
    }
	/**
	 * This function matches a body atom with the heads of the rules 
	 * in the executing world. Only the rules with the same ruleType
	 * specified will be matched.
	 * 
	 * @param bodyAtom The testing body atom
	 * @param ruleType The type of the rules to match
	 * @return the result of matching, return "-1" as the rule number if no match
	 */
	protected MatchResult matchRule(World pWorld, Matcher matcher, Node bodyAtom, int ruleType) { 
	    MatchResult matchResult = null; 
	    for (int i = 0; i < pWorld.numberOfRules(); i++) {
	        // Match only those rules with the same rule type (D or N or R)
	        if (ruleType == pWorld.rule(i).getRuleType() || (ruleType == 1 && pWorld.rule(i).getRuleType() == 2)) { 
	        	/*
	        	 *  Rename the variable in the rules to avoid the 2 variables in 2 different rules
	        	 *   to be considered as only one variable
	        	 */
	        	
	        	boolean initialRule = false;
	        	if(pWorld.rule(i).getRuleType()==2)
	        		pWorld.rule(i).renameVariables(initialRule);
	        }	        	
        } 
	    
	    for (int i = 0; i < pWorld.numberOfRules(); i++) {
	        // Match only those rules with the same rule type (D or N)
	        if ((ruleType != pWorld.rule(i).getRuleType()) && !(ruleType == 1 && pWorld.rule(i).getRuleType() == 2)) {
	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" RuleType <Mismatched>.");
	            matchResult = new MatchResult(false);
	        } else {
	        	/*
	        	 *  Rename the variable in the rules to avoid the 2 variables in 2 different rules
	        	 *   to be considered as only one variable
	        	 */
	        	matchResult = matcher.match(pWorld.rule(i).firstHead().getHeadAtom(), 
	                        				bodyAtom, pWorld.rule(i).firstHead().getMatchingMode()); 
	        }
	        if (matchResult.isSuccess()) {
	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + 
	                                 "\" HEAD <SUCCESS>. COND <Atom = " + 
	                                 pWorld.rule(i).numberOfConditions() + ">.");
	            matchResult.setRuleIndex(i);
	                     
	            // Copy and instantiate condition atoms of this rule.
	    	    if (pWorld.rule(matchResult.getRuleIndex()).numberOfConditions() > 0) {
	    	        
	    	        Node[] condAtom = new Node[pWorld.rule(matchResult.getRuleIndex()).numberOfConditions()];
	    	        
	    	        for (int j = 0; j < pWorld.rule(matchResult.getRuleIndex()).numberOfConditions(); j++) {
	    		        
	    	            // Get a condition atom from the rule
	    	            condAtom[j] = pWorld.rule(
	    	                          matchResult.getRuleIndex()).condition(j).getConditionAtom().cloneNode();
	    	            
	    	            // Instantiate the variable
	    	            matchResult.getSpecialization().instantiateVariables(condAtom[j]); 
	                    // Execute the condition atom
	    	            ExecutionResult execResult = null;
	    	            
	    	            try {
	    	                execResult = builtinMan.executeBuiltin(condAtom[j], Builtin.BT_D);
	    	                // Determine the result
	    		            if (!execResult.isSuccess()) {
	    		                logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" COND " + (j+1) + " <FAILED>. " +
										   ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
	    		                matchResult.setRuleIndex(-1);
	    		                matchResult.setSuccess(false);
	    		                break;
		    	    	        //return matchResult;
	    		            } else {
	    		                logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" COND " + (j+1) + " <SUCCESS>. " + 
										   ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
	    		                matchResult.getSpecialization().merge(execResult.getSpecialization()); 
	    		            }
	    	            } catch (NotExecutableException e) {
	    	                
	    	                if (e.getReason() == NotExecutableException.BUILTIN_NOTFOUND) {
	    	    	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" COND " + (j+1) + " <FAILED>. " + 
										   ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
	    	                } else if (e.getReason() == NotExecutableException.CONTAINS_VARIABLES) {
	    	    	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" COND " + (j+1) + " <FAILED>. " + 
										   ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
	    	    	        } else if (e.getReason() == NotExecutableException.INVALID_PARAMETERS)  {
				                logger.log(logLevel, "Reason: \"" + pWorld.rule(i).getRuleName() + "\" COND " + (j+1) + " <FAILED>. " +
										   ((execResult.getResultDescription() != null) ? execResult.getResultDescription() : ""));
	    	    	        }
	    	                matchResult.setRuleIndex(-1);
	    	                matchResult.setSuccess(false);
	    	                break;
	    	            }
	    	        }
	    	        
	    	        if (matchResult.isSuccess()) {
	    	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" <Matched>.");
	    	            return matchResult;
	    		    }
	    	        
	    	    } else { 
	    	        // The number of condition atoms is "zero". 
	    	        return matchResult;
	    	    }
	    	} else {
	    	    
	    	    // If the matching is not success, continue matching with other rules' heads.
	    	    //logger.line();
	            logger.log(logLevel, "Rule \"" + pWorld.rule(i).getRuleName() + "\" HEAD <FAILED>.");
	    	}
	        
	    }
	    
	    // The end of the loop with no rule can be matched
	    logger.log(logLevel, "Rule <No Matched>.");
	    
        if (matchResult == null) {
	        
	        matchResult = new MatchResult(false);
	        
	    }
	    matchResult.setRuleIndex(-1); 
	    return matchResult;
	}
	
	/**
	 * Print all clauses in the list of answers.
	 */
    public void printAnswers(String outputPath) throws IOException {
    	
    	File outputFile = new File(outputPath);
    	FileWriter fw = null;
    	boolean printToFile = outputPath != null;
    	if (printToFile) {
    		fw = new FileWriter(outputFile);
        	fw.write("<xet:XET");
			Enumeration entries = namespaceRegistry.keys();
			while (entries.hasMoreElements()) {
				String key = (String)entries.nextElement();
				fw.write(" xmlns:" + namespaceRegistry.getProperty(key) + "=\"" + key + "\"");
			}
			fw.write(">\n");
    	}
    	for (int i = 0; i < world.numberOfAnswers(); i++) {
    		int clauseIndex = (i + 1);
    		logger.log(Level.SEVERE, "Answer " + clauseIndex + " (" + world.answer(i).getClauseName() +")");
            logger.log(Level.SEVERE, Tool.LINE);
            logger.log(Level.SEVERE, Tool.ESTR);
            logger.log(Level.SEVERE, world.answer(i).printClauseETStyle());
            logger.log(Level.SEVERE, Tool.LINE);
            fw.write(world.answer(i).printClauseXMLStyle(1));
        }
    	if (printToFile) {
    		fw.write("</xet:XET>");
    	}
    	fw.close();
    }
    
    /**
     * This function get the first body atom of the 
     * active clause. An active clause is the clause 
     * that is being inspected. If the active clause 
     * contains no body atom, the active clause cannot
     * be processed and the next clause will be 
     * selected as the active clause. However, in the
     * D-Execution, there is only an active clause.
     * So, the execution ends when there is no body 
     * atom in the active clause.<br>
     * <br>
     * The function calls "processDAtom()" function 
     * and obtain the execution result. Only the 
     * reason from the execution will be returned.
     * 
     * @return Returns the reason of the execution 
     * result of the first body atom
     */
    protected void processActiveDClauseFirstBodyAtom() {
    	// The executor can't do anything because there is no active clause
        // This should not be happening.
	    if (world.getActiveClause() == null) {
	        logger.log(logLevel, "Executor found no clause in its execute() loop.");
    	    logger.log(logLevel, "Serious error... This should never happen");
	    }
	    // The executor can't do anything because there is no body atom.
	    if (world.getActiveClause().numberOfBodyAtoms() == 0) {
	        
	        logger.log(logLevel, "Active Clause <No BodyAtom>");
	        world.addAnswer(world.getActiveClause());
    		world.removeActiveClause();
    		return;
	    
	    }
	    // Obtain the first body atom of the active clause.
	    logger.log(logLevel, "Active Clause BodyAtom <Get>");
	    Node bodyAtom = world.getActiveClause().firstBodyAtom();
	    // Execute the first body atom.
	    logger.log(logLevel, "Active Clause BodyAtom <Executed>" );
	    
	    ExecutionResult result = processDAtom(world, bodyAtom);
	    
	    // Determine the result, remove the first body atom if success.
	    if (result.isSuccess()) {
	        
	        logger.log(logLevel, "Active Clause BodyAtom <Removed>");
	        world.getActiveClause().removeFirstBodyAtom();
		    world.getActiveClause().instantiateVariables(result.getSpecialization());
		     	        
            // Print specialization if not empty
		    if (!result.getSpecialization().isEmpty()) {
		        logger.log(logLevel, Tool.LINE);
			    logger.log(logLevel, "Specialization");
			    logger.log(logLevel, Tool.LINE);
			    logger.log(logLevel, result.getSpecialization().printSpecialization());
			    logger.log(logLevel, Tool.LINE);
		    }
	        // Print the current clause.
		    // The clause can still contains the rest of not-yet-processed body atom(s).
		    logger.log(logLevel, Tool.ESTR);
		    logger.log(logLevel, world.getActiveClause().printClauseETStyle());
	    }
	    switch (result.getReason()) {
	    	// If the condition is success, try the next clause.
	    	// (Actually this should not be possible in D-Mode)
	    	case ExecutionResult.SUCCESS:
	    	    break;
	    	// The clause contains no body atom or
	    	// There is no rule that can match the current body of the clause.
	    	case ExecutionResult.ZERO_BODY_ATOM:
	    	case ExecutionResult.RULE_NO_MATCH:
	    	    world.addAnswer(world.getActiveClause());
	    		world.removeActiveClause();
	    		break;
	        // At least a built-in function is FAILED.
	    	case ExecutionResult.BUILTIN_FAILED:
	    	    world.removeActiveClause();
				break;
	    	default:
	    }
	}

    /**
     * This function get the first body atom of the 
     * active clause. An active clause is the clause 
     * that is being inspected. If the active clause 
     * contains no body atom, the active clause cannot
     * be processed and the next clause will be 
     * selected as the active clause.<br>
     * <br>
     * The first body atom of the active clause can
     * be a built-in atom and the executor will try to 
     * process it. If the result is failed, the whole
     * clause will be removed. However, not every 
     * built-in can be used here. Only the "B" or "N" 
     * built-ins can be used.
     * <br>
     * If the first body atom is not a built-in, the
     * executor tries to match it with a rule. If 
     * no rule can be matched, the clause will be 
     * removed from being "active" and added to the
     * list of answer. If the body can be matched with
     * a rule, the body of the rule will be expanded.
     * <br>
     * The function calls "processDAtom()" function 
     * and obtain the execution result. Only the 
     * reason from the execution will be returned.
     */
    protected void processActiveNClauseFirstBodyAtom() {
    	Specialization spec = new Specialization();
	    // Can't do anything if there is no active clause
	    if (world.getActiveClause() == null) {
	        System.err.println("There is no active clause.");
	    }
	    if (world.getActiveClause().numberOfBodyAtoms() == 0) {
	        logger.log(logLevel, world.getActiveClause().getClauseName() + " --> <ANSWERED>");
	        world.addAnswer(world.getActiveClause());
    		world.removeActiveClause();
	        return;
	    }
	    // Obtain the first body atom of the active clause.
	    //Node bodyAtom = world.getActiveClause().randomActiveBodyAtom();
	    Node bodyAtom = world.getActiveClause().firstBodyAtom();
	    
	    sem.acquireUninterruptibly(); 
	    try {
		    if (bodyAtom.isBuiltin()) {
		    	
		    	ExecutionResult execResult = executeNBuiltin(bodyAtom);
		    	
			    if (execResult.isSuccess()) {
			        world.getActiveClause().removeActiveBodyAtom();
			        world.getActiveClause().clearUnExecutableList();
			        return;
			    } else {
			        logger.log(logLevel, world.getActiveClause().getClauseName() + " <REMOVED>");
			        world.removeActiveClause();
			        return;
			    }
			}
	    } catch (NotExecutableException e) {
	        if (e.getReason() == NotExecutableException.CONTAINS_VARIABLES)  {
                logger.log(logLevel, "Reason: Unresolvable variable(s) in \"" + bodyAtom.fullName() + "\"");
                world.getActiveClause().moveFirstBodyAtomToLastPosition();
                world.getActiveClause().incrementUnExecutableBodyAtomNo();
                bodyAtom = world.getActiveClause().firstBodyAtom();
                //bodyAtom = world.getActiveClause().randomActiveBodyAtom();
                return;
            } else {
	    	    // These errors must be corrected before running the XET program.
	    	    // Users must edit the XET file to correct these problems.
	    	    logger.log(logLevel, "Built-in Atom " + bodyAtom.fullName() + " ABUSED.");
	    	    if (e.getReason() == NotExecutableException.BUILTIN_NOTFOUND) {
	                logger.log(logLevel, "Reason: cannot locate builtin \"" + bodyAtom.fullName() + "\"");
	            } else if (e.getReason() == NotExecutableException.DIFFERENT_STRUCTURE) {
	                logger.log(logLevel, "Reason: The structure does not match \"" + bodyAtom.fullName() + "\"");
	            } else if (e.getReason() == NotExecutableException.WRONG_PLACE)  {
	                logger.log(logLevel, "Reason: \"" + bodyAtom.fullName() + "\" is used in a wrong place.");
	            } else {
	                logger.log(logLevel, "Reason: Unknown reason " + e.getReason());
	            }
	    	    e.printStackTrace();
	    	}
	    	logger.log(logLevel, Tool.LINE);
	    	logger.log(logLevel, world.getActiveClause().printClauseETStyle());
	    	logger.log(logLevel, Tool.ESTR);
	    } finally {
	        sem.release();
	    }
	    // Try to match this atom to the header of a rule
	    sem.acquireUninterruptibly();
	    MatchResult matchResult = null;
	    
	    try {
	    	matchResult = matchRule(world, matcher, bodyAtom, Rule.RT_N); 
    	
	    } finally {
	        sem.release();
	    }
	    if (matchResult.getRuleIndex() == -1) {
	        logger.log(logLevel, "There is no such rule that matches the current body atom.");
	        logger.log(logLevel, Tool.ESTR);
	        sem.acquireUninterruptibly();
	        try {
	            logger.log(logLevel, world.getActiveClause().getClauseName() + " --> <ANSWERED>");
	            world.addAnswer(world.getActiveClause());
	            world.removeActiveClause();
	        } finally {
	            sem.release();
	        }
	        return;
	    } 
	    spec.merge(matchResult.getSpecialization()); ;
	    world.getActiveClause().clearUnExecutableList();
	    logger.log(logLevel, Tool.LINE);
	    logger.log(logLevel, "Matched with \"" + 
	    		world.rule(matchResult.getRuleIndex()).getRuleName() + "\" (" + 
	    		world.rule(matchResult.getRuleIndex()).ruleTypeText() + ")");
	    
	    sem.acquireUninterruptibly();
	    try {
		    // Execute body of the matching rules
		    if (world.rule(matchResult.getRuleIndex()).numberOfBodies() > 1) { 
		        // If there are several bodies, additional clauses will be created
		        for (int i = 1; i < world.rule(matchResult.getRuleIndex()).numberOfBodies(); i++) { 
		            Clause newClause = world.getActiveClause().cloneClause();
		            newClause.instantiateVariables(spec);
		            world.insertClause(newClause, 0);
		        } 
		        // Process each bodies
		        int j = 0;
		        for (int i = 0; i < world.rule(matchResult.getRuleIndex()).numberOfBodies(); i++) {
		        	world.assignActiveClause(j);
		            logger.log(logLevel, Tool.LINE);
		            logger.log(logLevel, world.rule(matchResult.getRuleIndex()).getRuleName() + " Body " + (i+1) + 
		                    			 " --> " + world.getActiveClause().getClauseName());
		            ExecutionResult bodyResult = processBodyNAtoms(matchResult.getRuleIndex(), i, spec);
		            if (!bodyResult.isSuccess()) {
		                switch(bodyResult.getReason()) {
		                case ExecutionResult.BUILTIN_FAILED:
		                    logger.log(logLevel, world.getActiveClause().getClauseName() + " <REMOVED>");
		                    world.removeActiveClause();
		                	j--;
			                break;
		                case ExecutionResult.ZERO_BODY_ATOM:
			            case ExecutionResult.RULE_NO_MATCH:
			                logger.log(logLevel, world.getActiveClause().getClauseName() + " --> <ANSWERED>");
			                world.addAnswer(world.getActiveClause());
			                world.removeActiveClause();
		                	j--;
		                }
		            } else {
		                if (world.getActiveClause().numberOfBodyAtoms() == 0) {
		                    logger.log(logLevel, world.getActiveClause().getClauseName() + " --> <ANSWERED>");
		                    world.addAnswer(world.getActiveClause());
		                    world.removeActiveClause();
		                    j--;
		                }
		            }
		            j++;
		        }
		    } else {
		        ExecutionResult bodyResult = processBodyNAtoms(matchResult.getRuleIndex(), 0, spec); 
		        if (!bodyResult.isSuccess()) {
		            logger.log(logLevel, world.getActiveClause().getClauseName() + " <REMOVED>");
		            world.removeActiveClause();
		        }
		    }
	    } finally {
	        sem.release();
	    }
	}
    
    protected ExecutionResult processBodyNAtoms(int ruleIndex, int bodyIndex, Specialization headSpec) {   
    	Vector newClauseList = new Vector();
	    ExecutionResult execResult = new ExecutionResult();
	    execResult.merge(headSpec);
	    
        // Execute the "execution-sequence"
	    if (world.rule(ruleIndex).body(bodyIndex).numberOfExecSeqAtoms() > 0) { 
	        Node[] execSeqAtom = new Node[world.rule(ruleIndex).body(bodyIndex).numberOfExecSeqAtoms()];
	        for (int i = 0; i < world.rule(ruleIndex).body(bodyIndex).numberOfExecSeqAtoms(); i++) {
	        	// Get a execution sequence atom from the rule
	            execSeqAtom[i] = world.rule(ruleIndex).body(bodyIndex).execSeqAtom(i).cloneNode();
	            
	            // Instantiate the variable
	            execResult.getSpecialization().instantiateVariables(execSeqAtom[i]);
	            // Execute the execution sequence atom
	            ExecutionResult execSeqResult = executeDBuiltin(execSeqAtom[i], execResult.getSpecialization());
	            if (!execSeqResult.isSuccess()) { 
	                execResult.setSuccess(false);
	                execResult.setReason(ExecutionResult.BUILTIN_FAILED);
	                return execResult;
	            } else {
	                // Merge the result
	                execResult.merge(execSeqResult);
	            }
		    }
	    }
	    
	    rewriteNAtom(world.getActiveClause(), world.rule(ruleIndex), 
	                 execResult.getSpecialization(), bodyIndex);
	    execResult.setReason(ExecutionResult.SUCCESS);
	    execResult.setSuccess(true);
	    return execResult;
	}
    
    /**
     * This function processes a D-Atom with all D-rules.
     * The executor selects the first rule that match the
     * input bodyAtom and expands the rule bodies to inspect.
     * The expanded rule bodies will be inspect in an ascending
     * sequence deterministically. If a rule body is a built-in,
     * it will be executed.<br>
     * <br>
     * Should the execution result is
     * FAILD, the input D-Atom will be also failed and the
     * clause that the input body atom belongs to will be
     * also failed. On the other hand, the next body atom
     * will be inspected.<br>
     * <br>
     * If a rule body atom is not a built-in, this 
     * function will be again called recursively until
     * there exists a built-in atom that can give a 
     * TRUE/FALSE value back. 
     * 
     * @param bodyAtom The input body D-Atom
     * @return Returns the execution result including the specialization
     */
    protected ExecutionResult processDAtom(World orgWorld, Node bodyAtom) { 
    	//Clone world from original world
    	World newWorld = orgWorld.cloneWorld();
        // Create a new execution result
        ExecutionResult execResult = new ExecutionResult();
        // Try to match this D-Atom to the header of a rule
    	MatchResult matchResult = matchRule(newWorld, matcher, bodyAtom, Rule.RT_D);
    	 
    	// The D-Atom cannot be matched to the heads of any rules.
	    if (matchResult.getRuleIndex() == -1) {
	        execResult.setSuccess(false);
	        execResult.setReason(ExecutionResult.RULE_NO_MATCH);
	        return execResult;
	    }
	    // The D-Atom can be matched with a rule.
	    // The index of the matching rule can be referred from matchResult.getRuleIndex()
	    execResult.merge(matchResult.getSpecialization());
	    // Execute the body atom of the matching rule	    
	    logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" +
                       	     " BODY <Atom = " + newWorld.rule(matchResult.getRuleIndex()).firstBody().numberOfBodyAtoms() + ">");
        // Check if the rule does contain at least one body.
	    // The rule with no body atom will not be processed and the execution
	    // result be will "true" immediately.
	    if (newWorld.rule(matchResult.getRuleIndex()).firstBody().numberOfBodyAtoms() > 0) {
	        // Get the list contain all body atoms of the matching rule
	    	
	        Node[] ruleBodyAtom = new Node[newWorld.rule(matchResult.getRuleIndex()).firstBody().numberOfBodyAtoms()];    
	        // Loop through all body atoms of the matching rule (specified by the matchResult.getRuleIndex())
		    for (int i = 0; i < newWorld.rule(matchResult.getRuleIndex()).firstBody().numberOfBodyAtoms(); i++) {
		        // Get a rule body atom from the matching rule in an ascending order (deterministically).
	            ruleBodyAtom[i] = newWorld.rule(matchResult.getRuleIndex()).firstBody().bodyAtom(i).cloneNode();
	            // Instantiate the variable to the rule body atom being inspected.
	            execResult.getSpecialization().instantiateVariables(ruleBodyAtom[i]);
	            // Check if the rule body atom is a built-in
	            if (ruleBodyAtom[i].isBuiltin()) { 
	            	// This rule body atom of the rule is a built-in
	                logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" +
	                                     " BODY " + ruleBodyAtom[i].fullName() + " <Builtin>");
	                try {
		                // Execute the built-in rule body atom
		                ExecutionResult builtinExecResult = builtinMan.executeBuiltin(ruleBodyAtom[i], Builtin.BT_D);
		                // The built-in execution result is SUCCESS
			            if (builtinExecResult.isSuccess()) {
			                logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" +
		                                         " BODY " + ruleBodyAtom[i].fullName() + " <SUCCESS>" + 
		                                         ((builtinExecResult.getResultDescription() != null) ? 
		                                           builtinExecResult.getResultDescription() : ""));
			                execResult.merge(builtinExecResult.getSpecialization());
			            // The built-in execution result is FAILED    
			            } else {
			                logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" + 
		                                         " BODY " + ruleBodyAtom[i].fullName() + " <FAILED>");
		                    // For D-processing, this will fail immediately
			                execResult.merge(builtinExecResult);
			                execResult.setReason(ExecutionResult.BUILTIN_FAILED);
			                return execResult;
		                }
		            // The rule body atom is a built-in. But it cannot be executed because... 
			        // Again, for D-processing, this will also fail immediately
		            } catch (NotExecutableException e) {
		                logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" +
		                                     " BODY " + ruleBodyAtom[i].fullName() + " <FAILED>");
		                execResult.setSuccess(false);
		                execResult.setReason(ExecutionResult.BUILTIN_FAILED);
		                return execResult;
		            }
	            // This rule body atom is NOT a built-in. 
	            } else {
	            	logger.log(logLevel, "Rule \"" + newWorld.rule(matchResult.getRuleIndex()).getRuleName() + "\"" +
	                                     " BODY " + ruleBodyAtom[i].fullName() + " <Not Builtin>");
//	                // Print specialization if not empty
//	    		    if (!execResult.getSpecialization().isEmpty()) {
//	    		        logger.log(logLevel, Tool.LINE);
//	    			    logger.log(logLevel, "Specialization");
//	    			    logger.log(logLevel, Tool.LINE);
//	    			    logger.log(logLevel, execResult.getSpecialization().printSpecialization());
//	    			    logger.log(logLevel, Tool.LINE);
//	    		    }
	                logger.log(logLevel, ruleBodyAtom[i].printXML());
	                logger.log(logLevel, Tool.ESTR);
	                // Execute the rule body D-Atom (recursively)
	                recursiveCount = recursiveCount+1;
	                double testNo = Math.random();	            		            
	            	testNo = testNo + 1;
	                ExecutionResult dAtomResult = processDAtom(newWorld, ruleBodyAtom[i]);
	                
	                // Check the execution 
	                if (dAtomResult.isSuccess()) {
	                    // If the execution is success, merge the specialization
	                    execResult.merge(dAtomResult.getSpecialization());
	                } else {
	                    // If the execution is failed, the whole clause will fail
	                    // immediately as the D-execution's semantic.
	                    return (new ExecutionResult(false, dAtomResult.getReason()));
	                } // End the validation result of non-built-in atom execution
	            } // End checking for built-in.
	        } // End the loop through all body atoms of the matching rule.
	        // If none of the body atoms of the matching rule fails,
		    // the rule satisfies the first body atom of the active clause.
		    execResult.setSuccess(true);
	        execResult.setReason(ExecutionResult.SUCCESS);
	        return execResult;
	    } // End checking for the number of the body atom of the matching rule.
	      // The execution will proceed to this point immediately if the rule
	      // contains ZERO body atom.
	    execResult.setSuccess(true);
	    execResult.setReason(ExecutionResult.SUCCESS);
	    return execResult;
    }
    
    protected void rewriteNAtom(Clause clause, Rule rule, Specialization spec, int bodyIndex) {
        // If the rule type is "non-deterministic", expand all the bodies of the rule. 
        Node[] ruleBodyAtom = new Node[rule.body(bodyIndex).numberOfBodyAtoms()];
        // Get the list of the current variable
        //HashSet clauseVariableSet = clause.uninstantiatedVariableSet();
        // Gradually add new body atoms from the matched rule.
        for (int i = 0; i < rule.body(bodyIndex).numberOfBodyAtoms(); i++) { 
            // Get a body atom from the rule
            ruleBodyAtom[i] = rule.body(bodyIndex).bodyAtom(i).cloneNode();
            // Get the list of variable atom from the body atom
            // Instantiate the variable
            spec.instantiateVariables(ruleBodyAtom[i]); 
            // Add all body atoms to the clause
            clause.addBodyAtom(ruleBodyAtom[i]);
        }
        // Remove the current body atom from the active clause.
        clause.removeFirstBodyAtom();
        //clause.removeActiveBodyAtom();
        // Print clause after rewrite
        if (clause.hasMoreBodyAtoms()) {
            logger.log(logLevel, Tool.ESTR);
            logger.log(logLevel, clause.printClauseETStyle());
        }
	}
	
    /**
     * Assign a built-in manager object to the executor.
     * 
     * @param builtinMan The builtinMan to set.
     */
    public void setBuiltinMan(BuiltinManager builtinMan) {
        this.builtinMan = builtinMan;
    }
    /**
     * @param config The config to set.
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }
    /**
     * @param out The log handler to set
     */
    public void setLogHandler(Handler logHandler) {
        this.logHandler = logHandler;
    }
    /**
     * @param out The log level to set
     */
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
    /**
     * Assign a new matcher object to the executor.
     * 
     * @param matcher The matcher to set.
     */
    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }
    /**
     * Assign a new world object to the executor.
     * 
     * @param world The world to set.
     */
    public void setWorld(World world) {
        this.world = world;
    }
    
    private InMemoryObjects inMemoryObjects = new InMemoryObjects();
    private DirectInputs directInputs = new DirectInputs();
    
    /**
     * Assign direct input values (policy inputs)
     */
    public void setInputs(HashMap<String,Object> data) {
    	Set<String> keys = data.keySet();
    	
    	String str = "";
    	
    	for(String key:keys) {    		
    		if(data.get(key) instanceof long[]) {
    			str = Arrays.toString((long[]) data.get(key));        		
        	} else if(data.get(key) instanceof int[]) {
        		str = Arrays.toString((int[]) data.get(key));
        	} else if(data.get(key) instanceof double[]) { 
        		str = Arrays.toString((double[]) data.get(key));
        	} else if(data.get(key) instanceof String[]) {
        		str = Arrays.toString((String[]) data.get(key));
        	} else {
        		str = ""+data.get(key);
        	}
    		
    		directInputs.getDirectInput().put(key, str);
    	}
    }
    
    public InMemoryObjects getInMemoryObjects() {
    	return inMemoryObjects;
    }
    
    public DirectInputs getDirectInputs() {
    	return directInputs;
    }
    
    public static int recursiveCount = 0;
}
