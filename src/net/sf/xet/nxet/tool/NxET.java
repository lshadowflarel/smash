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

package net.sf.xet.nxet.tool;

import org.apache.commons.cli.*;
import java.io.IOException;
import java.util.logging.*;

import net.sf.xet.nxet.config.Version;
import net.sf.xet.nxet.executor.Executor;
import net.sf.xet.nxet.tool.LogFormatter;

/**
 * The "NxET" class is a command line debugger & stepper class that
 * operates over the "Executor" class. The NxET class provides
 * functionality similar to those of the ET Interpreter (ETI). That
 * is, it provides a command line interface for users to input
 * a debugging/stepping command to control how the reasoning engine
 * manipulates rules, clauses and data. 
 * 
 * @author Paramai Supadulchai
 */
public class NxET {
	
    protected Logger logger = Logger.getLogger(NxET.class.getName());
    
    /**
     * The only "starting point" of NxET
     * 
     * @param args parameters specified by users
     */
    public static void main(String args[]) {
        NxET nxet = new NxET(args);
    }
    /**
     * The default constructor NxET<br>
     * <br>
     * The constructor gets the parameters specified
     * by users and parse them.
     * 
     * @param args
     */
    public NxET(String[] args) {
        if (parseArgument(args)) {
            if (this.createExecutor()) {
                this.executor.execute();
                if (printOutputFile) {
                	try {
                		this.executor.printAnswers(outputFile);
                	} catch (IOException e) {
                		System.err.println("The specified output file " + outputFile + " is invalid");
                    	System.exit(-1);
                	}
                }
            }
        }
    }
    /**
     * Parse all parameters specified by users.
     * If all required parameters have been specified,
     * the function returns TRUE. Otherwise, it will
     * return false.
     * 
     * @param args The parameters given by users
     * @return true all requires parameters can be obtained.
     */
    public boolean parseArgument(String[] args) {
        // Create user's options
        Options options = new Options();
        options.addOption("r", "rules", true, "(Required) The file containing the user's rules");
        options.addOption("q", "query", true, "(Required) The file containing the user's query");
        options.addOption("c", "config-file", true, "The file containing the user's configuration");
        options.addOption("d", "data", true, "The file containing the user's data source descriptions");
        options.addOption("o", "output", true, "The output file");
        options.addOption("l", "log", true, "The log file");
        options.addOption("s", "silent", false, "NxET will be extra quiet!");
        options.addOption("v", "version", false, "Print NxET's version");
        options.addOption("p", "confirm-params", false, "Print confirmed parameter(s)");
        CommandLineParser cliParser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = cliParser.parse(options, args);
        } catch (ParseException e) {
            System.err.println( "Parsing parameter(s) failed.  Reason: " + e.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "NxET executor", options );
            System.exit(2);
        }
        // Extract the values entered for the various options -- if the
        // options were not specified, the corresponding values will be
        // null.
        
        // Get rule file parameter
        if (cmd.hasOption("r")) {
            ruleFile = cmd.getOptionValue("r");
        }
        // Get query file parameter
        if (cmd.hasOption("q")) {
            queryFile = cmd.getOptionValue("q");
        }
        // Get config file parameter
        if (cmd.hasOption("c")) {
            configFile = cmd.getOptionValue("c");
        }
        // Get data source file parameter
        if (cmd.hasOption("d")) {
            dataSourceFile = cmd.getOptionValue("d");
        }
        // Get data source file parameter
        printOutputFile = cmd.hasOption("o");
        if (cmd.hasOption("o")) {
            outputFile = cmd.getOptionValue("o");
        }
        // Be extra quiet
        extraQuiet = cmd.hasOption("s");
        // Get log file parameter
        if (cmd.hasOption("l")) {
            logFile = cmd.getOptionValue("l");
            if (logFile != null) {
                try {
                    // Create a file handler that uses the custom formatter
                    logHandler = new FileHandler(logFile);
                    logHandler.setFormatter(new LogFormatter());
                    logger.addHandler(logHandler);
                } catch (IOException e) {
					e.printStackTrace();
                } 
            } else {
                logHandler = new ConsoleHandler();
                logHandler.setFormatter(new LogFormatter());
                this.logger.addHandler(logHandler);
            }
        } else {
            logHandler = new ConsoleHandler();
            logHandler.setFormatter(new LogFormatter());
            this.logger.addHandler(logHandler);
        }
        // If this is true, the parent handler will also print the message
        logger.setUseParentHandlers(false);
        // Get data source file parameter
        printVersion = cmd.hasOption("v");
        // Get data source file parameter
        printConfirmedParameters = cmd.hasOption("p");
        
        Level level = null;
        
        if (extraQuiet) {
        	level = Level.FINEST;
		} else {
        	level = Level.INFO;
        }
		logHandler.setLevel(level);
        
    	if (args.length == 1 && printVersion) {
    		logger.log(level, Version.getVersionInfo());
            logger.log(level, Version.getCopyRightInfo());
    		return false;
    	} else if ((ruleFile == null) || (queryFile == null)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "NxET executor", options );
            return false;
        } else {
            // just print out and confirm the option value
            logger.log(level, Version.getVersionInfo());
            logger.log(level, Version.getCopyRightInfo());
            if (printConfirmedParameters) {
	            logger.log(level, "Rule file       : " + (ruleFile       != null ? ruleFile   		: "not specified."));
		        logger.log(level, "Query file      : " + (queryFile  	 != null ? queryFile  		: "not specified."));
		        logger.log(level, "Config file     : " + (configFile     != null ? configFile 		: "not specified."));
		        logger.log(level, "Datasource file : " + (dataSourceFile != null ? dataSourceFile  : "not specified."));
            }
            return true;
        }
    }
    // The log handler
    private Handler logHandler = null;
    // The executor
    private Executor executor = null;
    // Config file parameter
    private String configFile = null;
    // Data source file parameter
    private String dataSourceFile = null;
    // Query file parameter
    private String queryFile = null;
    // Rule file paramter
    private String ruleFile = null;
    // The log file
    private String logFile = null;
    // The output file
    private String outputFile = null;
    // Print version
    private boolean printVersion = false;
    // Print confirm user's parameter(s)
    private boolean printConfirmedParameters = false;
    // Print output file
    private boolean printOutputFile = false;
    // Keep silent
    private boolean extraQuiet = false;
    /**
     * Create a new executor from the parameters provided
     * 
     * @return Returns TRUE if the executor has been successfully created
     */
    public boolean createExecutor() {
        try {
            executor = new Executor(this.queryFile, this.ruleFile, 
                                    this.dataSourceFile, this.configFile,
                                    this.logHandler, System.getProperty("user.dir"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Execute the executor
     */
    public void execute() {
        executor.execute();
    }
}
