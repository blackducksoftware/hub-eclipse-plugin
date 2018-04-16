/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubComponentLookupService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorCacheService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class BlackDuckEclipseServicesFactory {
    private static BlackDuckEclipseServicesFactory instance = new BlackDuckEclipseServicesFactory();

    private final HubConnectionService hubConnectionService;

    private final ComponentInspectorViewService componentInspectorViewService;

    private final ComponentInformationService componentInformationService;

    private final ProjectInformationService projectInformationService;

    private final WorkspaceInformationService workspaceInformationService;

    private final HubComponentLookupService hubComponentLookupService;

    private final ComponentInspectorPreferencesService componentInspectorPreferencesService;

    private final ComponentInspectorService componentInspectorService;

    private final ComponentInspectorCacheService componentInspectorCacheService;

    private final HubPreferencesService hubPreferencesService;

    protected BlackDuckEclipseServicesFactory() {
        instance = this;
        final BlackDuckPreferencesService blackDuckPreferencesService = new BlackDuckPreferencesService(BlackDuckEclipseActivator.getDefault());
        this.hubPreferencesService = new HubPreferencesService(blackDuckPreferencesService);
        this.componentInspectorPreferencesService = new ComponentInspectorPreferencesService(blackDuckPreferencesService);
        this.componentInformationService = new ComponentInformationService();
        this.projectInformationService = new ProjectInformationService(componentInformationService);
        this.workspaceInformationService = new WorkspaceInformationService(projectInformationService);
        this.componentInspectorViewService = new ComponentInspectorViewService();
        this.hubConnectionService = new HubConnectionService(componentInspectorViewService);
        this.hubComponentLookupService = new HubComponentLookupService(hubConnectionService);
        this.componentInspectorCacheService = new ComponentInspectorCacheService(componentInspectorViewService, hubComponentLookupService);
        this.componentInspectorService = new ComponentInspectorService(componentInspectorViewService, hubConnectionService, componentInspectorPreferencesService, componentInspectorCacheService);
    }

    public static BlackDuckEclipseServicesFactory getInstance() {
        return instance;
    }

    public HubConnectionService getHubConnectionService() {
        return hubConnectionService;
    }

    public ComponentInformationService getComponentInformationService() {
        return componentInformationService;
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

    public HubComponentLookupService getHubComponentLookupService() {
        return hubComponentLookupService;
    }

    public ComponentInspectorCacheService getComponentInspectorCacheService() {
        return componentInspectorCacheService;
    }

    public HubPreferencesService getHubPreferencesService() {
        return hubPreferencesService;
    }

}
