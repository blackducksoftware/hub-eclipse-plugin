/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.eclipse.services;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorCacheService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class BlackDuckEclipseServicesFactory {
	protected static BlackDuckEclipseServicesFactory instance = new BlackDuckEclipseServicesFactory();

	protected AbstractConnectionService connectionService;

	protected ComponentInspectorViewService componentInspectorViewService;

	protected ComponentInformationService componentInformationService;

	protected ProjectInformationService projectInformationService;

	protected WorkspaceInformationService workspaceInformationService;

	protected AbstractComponentLookupService componentLookupService;

	protected BlackDuckPreferencesService blackDuckPreferencesService;

	protected ComponentInspectorPreferencesService componentInspectorPreferencesService;

	protected ComponentInspectorService componentInspectorService;

	protected ComponentInspectorCacheService componentInspectorCacheService;

	protected BlackDuckEclipseServicesFactory(){
		//TODO: Implement some default connection service?
		this.connectionService = null;
		this.componentInspectorViewService = new ComponentInspectorViewService();
		this.componentInformationService = new ComponentInformationService();
		this.projectInformationService = new ProjectInformationService(componentInformationService);
		this.workspaceInformationService = new WorkspaceInformationService(projectInformationService);
		this.componentLookupService = null;
		this.blackDuckPreferencesService = new BlackDuckPreferencesService(BlackDuckEclipseActivator.getDefault());
		this.componentInspectorPreferencesService = new ComponentInspectorPreferencesService(blackDuckPreferencesService);
		this.componentInspectorCacheService = new ComponentInspectorCacheService(componentInspectorViewService, componentLookupService);
		this.componentInspectorService = new ComponentInspectorService(componentInspectorViewService, connectionService, componentInspectorPreferencesService, componentInspectorCacheService);
	}

	public static BlackDuckEclipseServicesFactory getInstance(){
		return instance;
	}

	public AbstractConnectionService getConnectionService() {
		return connectionService;
	}

	public BlackDuckPreferencesService getBlackDuckPreferencesService() {
		return blackDuckPreferencesService;
	}

	public ComponentInformationService getComponentInformationService() {
		return componentInformationService ;
	}

	public WorkspaceInformationService getWorkspaceInformationService() {
		return workspaceInformationService;
	}

	public ProjectInformationService getProjectInformationService() {
		return projectInformationService;
	}

	public ComponentInspectorService getComponentInspectorService() {
		return componentInspectorService;
	}

	public ComponentInspectorViewService getComponentInspectorViewService() {
		return componentInspectorViewService;
	}

	public ComponentInspectorPreferencesService getComponentInspectorPreferencesService() {
		return componentInspectorPreferencesService;
	}

	public AbstractComponentLookupService getComponentLookupService() {
		return componentLookupService;
	}

	public ComponentInspectorCacheService getComponentInspectorCacheService() {
		return componentInspectorCacheService;
	}

}
