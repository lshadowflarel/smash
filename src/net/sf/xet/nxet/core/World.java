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

package net.sf.xet.nxet.core;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Handler;
import net.sf.xet.nxet.data.DataSources;
import net.sf.xet.nxet.executor.Executor;
import net.sf.xet.nxet.config.Configuration;
import net.sf.xet.nxet.config.DefaultConfig;

/**
 * The World class represents an executable environment under the
 * Equivalent Transformation paradigm. World contains set of rules,
 * clauses, answers and a query clause. In addition, World provides
 * primitive functionality to be invoked by other classes such as
 * the Executor class or the Set-Of built-in function.
 * 
 * @author Paramai Supadulchai
 * @since NxET 0.1
 */
public class World {
    
    public static final String WORLD_DEFAULT = "Default World";
    public static final String WORLD_SET_OF = "Set-of World";
    
    private Executor executor;
    
    /**
     * A static "factory" method to create a new world.
     *
     * @param worldName The name of the world to be created
     * @param queryClause The query clause for this world
     * @param ruleFile The list (vector) containing all XET rules
     * @param dataSourceFile The description of all data sources
     * @param config The configuration file
     * @param out The output printstream
     * @param builtinPath The path to built-in
     * @return Returns a newly created world
     */
    public static World create(String worldName, 
            				   Clause queryClause, 
                               Vector<Rule> rules, 
                               DataSources dataSources,
                               Configuration config,
                               Handler logHandler,
                               String builtinPath,
                               Executor executor) {
        
        World world = new World();
        world.setWorldName(worldName);
        world.setQueryClause(queryClause);
        
        for(int i=0; i< world.getClauses().size(); i++)
        	world.setRules(rules);
		
        /*
    	 *  Rename the variable in the rules to avoid the 2 variables in 2 different rules
    	 *   to be considered as only one variable
    	 */
//		boolean initialRule = true;
//		for(int i=0; i< world.numberOfRules(); i++) {
//			world.rule(i).renameVariables(initialRule);
//		}
		world.setDataSources(dataSources);
		world.setConfig(config);
		world.setLogHandler(logHandler);
		world.setBuiltinPath(builtinPath); 
		world.setExecutor(executor);
		
		return world;
	}
    
    // The clause that is being processed.
    private Clause activeClause = null;
    // The index of the active clause (used to refer to the clauses vector)
    private int activeClauseIndex = 0;
    // The list containing all possible answers
    // A clause will be an answer when
    // 1. There is no body left
    // 2. There left-over bodies cannot be further processed
    private Vector<Clause> answers = new Vector<Clause>();
    // The list containing all clauses left to process
    private Vector<Clause> clauses = new Vector<Clause>();
    // The list containing all rules in the system
    private Vector<Rule> rules = null;
    // The source of all data
    private DataSources dataSources = null;
    // The configuration
    private Configuration config = null;
    // The query clause for this world.
    private Clause queryClause = null;
    // World attributes
    // World name is the name of this world. Normally, "default" is used by
    // the NxET class and the Executor class 
    private String worldName = null;
    // Log-level
    private Level logLevel = null;
    // Log-handler
    private Handler logHandler = null;
	// The path to builtin
	private String builtinPath = null;
	
    /**
     * Add a new answer to the answer list
     * 
     * @param answer a new answer clause
     */
    public void addAnswer(Clause answer) {
        answers.add(answer);
    }
   /**
     * Add a new clause to the clause list
     * 
     * @param newClause a new clause
     */
    public void addClause(Clause newClause) {
        clauses.add(newClause);
        newClause.setClauseName("Clause " + this.clauses.size());
    }
    /**
     * Add a new rule to the list by specifying the rule index
     * 
     * @param ruleIndex the Index of the rule
     * @param rule a rule object
     * @deprecated
     */
    public void addRule(int ruleIndex, Rule rule) {
        rules.add(ruleIndex, rule);
    }
    /**
     * Add a new rule to the list
     * 
     * @param rule a rule object to be added
     */
    public void addRule(Rule rule) {
        rules.add(rule);
    }
    /**
     * Return an answer clause, which is specified by an index
     * 
     * @param answerIndex the index of an answer in the answer list
     * @return the answer object
     */
    public Clause answer(int answerIndex) {
        return this.answers.get(answerIndex);
    }
    /**
     * Assign the first clause in the clause list to
     * be the active clause (by default)
     * 
     * @see assignActiveClause(int clauseIndex)
     */
    public void assignActiveClause() {
        this.activeClause = clauses.get(0);
        this.activeClauseIndex = 0;
    }
    /**
     * Assign the clause pointed by an index to be
     * the active clause
     * 
     * @param clauseIndex the index of the clause to
     * become the next active clause
     */
    public void assignActiveClause(int clauseIndex) {
        this.activeClause = clauses.get(clauseIndex);
        this.activeClauseIndex = clauseIndex;
    }
    /**
     * Get a clause specified by a clause index
     * 
     * @param clauseIndex the index of the clause
     * @return a clause object
     */
    public Clause clause(int clauseIndex) {
        return this.clauses.get(clauseIndex);
    }
    /**
     * Clear all answers in the answer list
     * 
     * @since NxET 0.2
     */
    public void clearAnswer() {
        answers.clear();
    }
    /**
     * Clear all clauses in the clause list
     * 
     * @since NxET 0.2
     */
    public void clearClauses() {
        clauses.clear();
    }
    /**
     * Remove all rules in the rule list
     */
    public void clearRules() {
        rules.clear();
    }
    /**
     * Assign the active clause from the first clause of the
     * clause list. Also remove the first clause of the clause
     * list at the same time.
     * @deprecated
     */
    public void fetchClause() {
        this.activeClause = clauses.remove(0);
        this.activeClauseIndex = 0;
    }
    
    /**
     * Get the answer list
     * 
     * @return Return the answer list
     */
    public Vector<Clause> getAnswers() {
    	return this.answers;
    }
    
    /**
     * Get an active clause
     * @return the active clause object
     */
    public Clause getActiveClause() {
        return this.activeClause;
    }
	
	/**
	 * Returns the builtin path
	 * 
	 * @return Returns the builtin path
	 * @since NxET 0.4
	 */
	public String getBuiltinPath() {
		return this.builtinPath;
	}
	
    /**
     * Get the clause list
     * 
     * @return the clause list (vector)
     */
    public Vector<Clause> getClauses() {
        return this.clauses;
    }
    /**
     * Get the configuration object
     * 
     * @return Returns the configuration object.
     * @since NxET 0.2
     */
    public Configuration getConfig() {
        if (this.config == null) {
            this.config = new DefaultConfig();
        }
        return this.config;
    }
    /**
     * Get the data sources
     * 
     * @return Returns the dataSources.
     * @since NxET 0.2
     */
    public DataSources getDataSources() {
        if (this.dataSources == null) {
            this.dataSources = new DataSources(this.config, this.logHandler);
        }
        return this.dataSources;
    }
    /**
     * Get the query clause
     * 
     * @return the query clause.
     */
    public Clause getQueryClause() {
        
        return this.queryClause;
        
    }
    /**
     * Get the log-handler
     * 
     * @return Returns the log-handler
     */
    public Handler getLogHandler() {
        return this.logHandler;
    }
    /**
     * Get the log-level
     * 
     * @return Returns the log-level
     */
    public Level getLogLevel() {
        return this.logLevel;
    }
    /**
     * Get the rule list
     * 
     * @return the rule list (vector)
     */
    public Vector<Rule> getRules() {
        return this.rules;
    }
    /**
     * Get the name of this world
     * 
     * @return "default" if the world is created by NxET or Executor class
     */
    public String getWorldName() {
        return this.worldName;
    }
    /**
     * Check if there are still more clauses left in
     * the set of clauses.
     * 
     * @return true if the set of clauses is not empty
     */
    public boolean hasMoreClause() {
        return (this.clauses.size() > 0);
    }
    /**
     * Insert clause at a specific index
     * 
     * @param clause The clause to insert
     * @param index Where to insert the clause
     */
    public void insertClause(Clause clause, int index) {
        this.clauses.insertElementAt(clause, index);
        clause.setClauseName("Clause " + this.clauses.size());
    }
    /**
     * Return the number of all answers in the answer list
     * 
     * @return the number of all answers in the answer list
     */
    public int numberOfAnswers() {
        return this.answers.size();
    }
    /**
     * Return the number of all clauses in the clause list
     * 
     * @return the number of all clauses in the clause list
     */
    public int numberOfClauses() {
        return this.clauses.size();
    }
    /**
     * Return the number of all rules in the rule list
     * 
     * @return the number of all rules in the rule list
     */
    public int numberOfRules() {
        return this.rules.size();
    }
    /**
     * Remote a clause (referred by a clause index).
     * If the clause is an active clause, the active clause
     * will also be removed.
     * 
     * @see getActiveClause()
     */
    public void removeClause(int clauseIndex) {
        clauses.remove(this.activeClauseIndex);
        if (clauseIndex == this.activeClauseIndex) {
            this.activeClause = null;
        }
    }
    /**
     * Remote the active clause (referred by the active clause index).
     * After the active clause is removed, the getActiveClause will
     * return null
     * 
     * @see getActiveClause()
     */
    public void removeActiveClause() {
        clauses.remove(this.activeClauseIndex);
        this.activeClause = null;
    }
    /**
     * Get a rule by specifying the rule index
     * 
     * @param ruleIndex the index of the rule
     * @return the rule object
     */
    public Rule rule(int ruleIndex) {
        return rules.get(ruleIndex);
    }
	
	/**
	 * Set the builtin path
	 * 
	 * @param builtinPath The builtin path to set
	 * @since NxET 0.4
	 */
	public void setBuiltinPath(String builtinPath) {
		this.builtinPath = builtinPath;
	}
	
    /**
     * Set the data sources
     * 
     * @param dataSources The dataSources to set.
     * @since NxET 0.2
     */
    public void setDataSources(DataSources dataSources) {
        this.dataSources = dataSources;
    }
    /**
     * Set the configuration
     * 
     * @param config The configuration to set
     * @since NxET 0.2
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }
    /**
     * Assign a new query clause directly (without
     * assigning a query object). *The query clause
     * will be automatically added to the clause
     * list by this function*.
     * 
     * @param qClause The query clause to assign
     * @see createQueryClause
     */
    public void setQueryClause(Clause queryClause) {
        this.queryClause = queryClause;
        clauses.add(queryClause);
    }
    /**
     * Assign the log handler
     * 
     * @param logHandler The log handler
     */
    public void setLogHandler(Handler logHandler) {
        this.logHandler = logHandler;
    }
    public void setExecutor(Executor executor) {
    	this.executor = executor;
    }
    public Executor getExecutor() {
    	return executor;
    }
    /**
     * Assign the rule list
     * 
     * @param rules the rule list in form of a vector.
     */
    public void setRules(Vector<Rule> rules) {
        this.rules = rules;
    }
    /**
     * Assign the world name
     * 
     * @param worldName the name to be set to this world.
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
    /**
     * Assign the world name
     * 
     * @param worldName the name to be set to this world.
     */
    public World cloneWorld() {
        World newWorld = new World();
        newWorld.setWorldName(this.worldName);
        newWorld.setQueryClause(this.queryClause.cloneClause());
        
        Vector<Rule> ruleTmp = new Vector<Rule>();
		for(int i=0; i< this.numberOfRules(); i++) {
			ruleTmp.add(i, this.rule(i).cloneRule());
		}
		newWorld.setRules(ruleTmp);
		newWorld.setDataSources(this.dataSources);
		newWorld.setConfig(this.config);
		newWorld.setLogHandler(this.logHandler);
		newWorld.setBuiltinPath(this.builtinPath);
		
		return newWorld;
    }
}
