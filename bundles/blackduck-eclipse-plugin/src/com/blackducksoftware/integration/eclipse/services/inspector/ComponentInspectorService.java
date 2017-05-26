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
package com.blackducksoftware.integration.eclipse.services.inspector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.InspectionJob;
import com.blackducksoftware.integration.eclipse.internal.datastructures.InspectionJobQueue;
import com.blackducksoftware.integration.eclipse.internal.listeners.InspectionJobChangeListener;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.services.connection.free.FreeConnectionService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.MavenExternalId;

public class ComponentInspectorService {
    private final InspectionJobQueue inspectionQueue;

    private final HubConnectionService hubConnectionService;

    private final FreeConnectionService freeConnectionService;

    private final ComponentInspectorCacheService inspectorCacheService;

    private final ComponentInspectorViewService inspectorViewService;

    private final ComponentInspectorPreferencesService inspectorPreferencesService;

    public ComponentInspectorService(final ComponentInspectorViewService inspectorViewService, final HubConnectionService hubConnectionService, final FreeConnectionService freeConnectionService, final ComponentInspectorPreferencesService componentInspectorPreferencesService, final ComponentInspectorCacheService componentInspectorCacheService){
        final InspectionJobChangeListener inspectionJobChangeListener = new InspectionJobChangeListener(inspectorViewService);
        this.inspectorPreferencesService = componentInspectorPreferencesService;
        this.inspectorCacheService = componentInspectorCacheService;
        this.inspectionQueue = new InspectionJobQueue(inspectionJobChangeListener);
        this.inspectorViewService = inspectorViewService;
        this.hubConnectionService = hubConnectionService;
        this.freeConnectionService = freeConnectionService;
    }

    public void initializeProjectComponents(final String projectName) {
        inspectorCacheService.initializeProject(projectName);
    }

    public boolean addComponentToProject(final String projectName, final MavenExternalId externalId) {
        try {
            inspectorCacheService.addComponentToProject(projectName, externalId);
        } catch (IOException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    public void removeComponentFromProject(final String projectName, final MavenExternalId externalId) {
        inspectorCacheService.removeComponentFromProject(projectName, externalId);
    }

    public boolean inspectProject(final String projectName){
        if ((!hubConnectionService.hasActiveConnection() && !freeConnectionService.hasActiveConnection())
                || !inspectorPreferencesService.isProjectMarkedForInspection(projectName)) {
            return false;
        }
        final InspectionJob inspection = new InspectionJob(projectName, this, inspectorPreferencesService);
        inspectionQueue.enqueueInspection(inspection);
        return true;
    }

    public void inspectAllProjects(){
        final WorkspaceInformationService workspaceInformationService = BlackDuckEclipseServicesFactory.getInstance().getWorkspaceInformationService();
        workspaceInformationService.getSupportedProjectNames().forEach(projectName -> {
            if(inspectorPreferencesService.isProjectMarkedForInspection(projectName)){
                inspectProject(projectName);
            }
        });
    }

    public boolean isProjectInspectionRunning(final String projectName) {
        return inspectionQueue.getInspectionIsRunning(projectName);
    }

    public boolean isProjectInspectionScheduled(final String projectName) {
        return inspectionQueue.getInspectionIsScheduled(projectName);
    }

    public void shutDown() {
        inspectionQueue.cancelAll();
    }

    public void removeProject(final String projectName) {
        inspectorCacheService.removeProject(projectName);
        inspectorViewService.clearProjectDisplay(projectName);
    }

    public List<ComponentModel> getProjectComponents(final String projectName) {
        return inspectorCacheService.getProjectComponents(projectName);
    }

}