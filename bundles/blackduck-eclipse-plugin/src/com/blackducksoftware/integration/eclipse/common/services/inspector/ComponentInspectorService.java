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
package com.blackducksoftware.integration.eclipse.common.services.inspector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.eclipse.common.Constants;
import com.blackducksoftware.integration.eclipse.common.services.hub.ComponentLookupService;
import com.blackducksoftware.integration.eclipse.common.services.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.internal.InspectionJob;
import com.blackducksoftware.integration.eclipse.internal.datastructures.InspectionJobQueue;
import com.blackducksoftware.integration.eclipse.internal.listeners.InspectionJobChangeListener;
import com.blackducksoftware.integration.hub.buildtool.Gav;
import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.dataservice.vulnerability.VulnerabilityDataService;

public class ComponentInspectorService {
	private final InspectionJobQueue inspectionQueue;
	private final HubConnectionService hubConnectionService;
	private final ComponentInspectorCacheService inspectorCacheService;
	private final ComponentInspectorViewService inspectorViewService;
	private final ComponentInspectorPreferencesService inspectorPreferencesService;

	public ComponentInspectorService(final ComponentInspectorViewService inspectorViewService, final HubConnectionService hubConnectionService){
		final InspectionJobChangeListener inspectionJobChangeListener = new InspectionJobChangeListener(inspectorViewService);
		ComponentLookupService componentLookupService;
		if(hubConnectionService.hasActiveHubConnection()){
			final LicenseDataService licenseDataService = hubConnectionService.getLicenseDataService();
			final VulnerabilityDataService vulnerabilityDataService = hubConnectionService.getVulnerabilityDataService();
			componentLookupService = new ComponentLookupService(licenseDataService, vulnerabilityDataService);
		}else{
			componentLookupService = null;
		}
		this.inspectorPreferencesService = new ComponentInspectorPreferencesService();
		this.inspectorCacheService = new ComponentInspectorCacheService(inspectorViewService, componentLookupService);
		this.inspectionQueue = new InspectionJobQueue(inspectionJobChangeListener);
		this.inspectorViewService = inspectorViewService;
		this.hubConnectionService = hubConnectionService;
	}

	public void initializeProjectComponents(final String projectName) {
		inspectorCacheService.initializeProject(projectName);
	}

	public boolean addComponentToProject(final String projectName, final Gav gav) {
		try {
			inspectorCacheService.addComponentToProject(projectName, gav);
		} catch (IOException | URISyntaxException e) {
			return false;
		}
		return true;
	}

	public void removeComponentFromProject(final String projectName, final Gav gav) {
		inspectorCacheService.removeComponentFromProject(projectName, gav);
	}

	public boolean inspectProject(final String projectName){
		if (!hubConnectionService.hasActiveHubConnection()
				|| !inspectorPreferencesService.isProjectMarkedForInspection(projectName)) {
			return false;
		}
		inspectorViewService.setProjectStatus(projectName, Constants.PROJECT_INSPECTION_SCHEDULED);
		final InspectionJob inspection = new InspectionJob(projectName, this);
		inspectionQueue.enqueueInspection(inspection);
		return true;
	}

	public boolean inspectProjects(final String... projectNames) {
		return inspectProjects(Arrays.asList(projectNames));
	}

	public boolean inspectProjects(final List<String> projectNames) {
		boolean success = true;
		for (final String projectName : projectNames) {
			success = inspectProject(projectName);
		}
		return success;
	}

	public void shutDown() {
		inspectionQueue.cancelAll();
	}

	public void removeProject(final String projectName) {
		inspectorCacheService.removeProject(projectName);
		inspectorViewService.clearProjectDisplay(projectName);
	}

}
