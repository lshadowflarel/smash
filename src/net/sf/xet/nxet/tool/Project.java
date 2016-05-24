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

package net.sf.xet.nxet.tool;

/**
 * The project object containing the information
 * of an NxET project such as rule, query, data
 * source and config file.
 * 
 * @author Paramai Supadulchai
 * @since NxET 0.4
 */
public class Project {

	private String projectName = null;
	private String projectDescription = null;
	private String queryFileUri = null;
	private String ruleFileUri = null;
	private String datasourceFileUri = null;
	private String configFileUri = null;
	
	/**
	 * Returns the uri of the config file
	 * 
	 * @return Returns the uri of the config file
	 */
	public String getConfigFileUri() {
		return configFileUri;
	}
	
	/**
	 * Set the uri of the config file
	 * 
	 * @param configFileUri The uri of the config file to set
	 */
	public void setConfigFileUri(String configFileUri) {
		this.configFileUri = configFileUri;
	}
	
	/**
	 * Returns the uri of the data source file
	 * 
	 * @return Returns the uri of the data source file
	 */
	public String getDatasourceFileUri() {
		return datasourceFileUri;
	}
	
	/**
	 * Set the uri of the data source file
	 * 
	 * @param datasourceFileUri The uri of the data source file to set
	 */
	public void setDatasourceFileUri(String datasourceFileUri) {
		this.datasourceFileUri = datasourceFileUri;
	}
	
	/**
	 * Returns the project description of the project
	 * 
	 * @return Returns the project description of this project
	 */
	public String getProjectDescription() {
		return projectDescription;
	}
	
	/**
	 * Set the project description
	 * 
	 * @param projectDescription the project description to set
	 */
	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}
	
	/**
	 * Returns the project name
	 * 
	 * @return Returns the project name
	 */
	public String getProjectName() {
		return projectName;
	}
	
	/**
	 * Set the project name
	 * 
	 * @param configFileUri The project name to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Returns the uri of the query file
	 * 
	 * @return Returns the uri of the query file
	 */
	public String getQueryFileUri() {
		return queryFileUri;
	}
	
	/**
	 * Set the uri of the query file
	 * 
	 * @param queryFileUri The uri of the query file to set
	 */
	public void setQueryFileUri(String queryFileUri) {
		this.queryFileUri = queryFileUri;
	}
	
	/**
	 * Returns the uri of the rule file
	 * 
	 * @return Returns the uri of the rule file
	 */
	public String getRuleFileUri() {
		return ruleFileUri;
	}
	
	/**
	 * Set the uri of the rule file
	 * 
	 * @param ruleFileUri The uri of the rule file to set
	 */
	public void setRuleFileUri(String ruleFileUri) {
		this.ruleFileUri = ruleFileUri;
	}
	
}
