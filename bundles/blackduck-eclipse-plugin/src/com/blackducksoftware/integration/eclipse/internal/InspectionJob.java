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
package com.blackducksoftware.integration.eclipse.internal;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.blackducksoftware.integration.eclipse.services.ComponentInformationService;
import com.blackducksoftware.integration.eclipse.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.hub.buildtool.Gav;

public class InspectionJob extends Job {
	public static final String FAMILY = "Black Duck Component Inspection";

	public static final String JOB_INSPECT_PROJECT_PREFACE = "Black Duck Component Inspector inspecting ";

	private static final int ONE_HUNDRED_PERCENT = 100000;

	private static final int THIRTY_PERCENT = 30000;

	private static final int SEVENTY_PERCENT = 70000;

	private final String projectName;

	private final ComponentInspectorService componentInspectorService;

	private final ProjectInformationService projectInformationService;

	private final ComponentInformationService componentInformationService;

	public InspectionJob(final String projectName, final ComponentInspectorService componentInspectorService) {
		super(JOB_INSPECT_PROJECT_PREFACE + projectName);
		this.projectName = projectName;
		this.componentInspectorService = componentInspectorService;
		this.projectInformationService = new ProjectInformationService();
		this.componentInformationService = new ComponentInformationService();
		this.setPriority(Job.BUILD);
	}

	public String getProjectName() {
		return projectName;
	}

	@Override
	public boolean belongsTo(final Object family) {

		return family.equals(FAMILY);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		try {
			final ComponentInspectorPreferencesService inspectorPreferencesService = new ComponentInspectorPreferencesService();
			if (!inspectorPreferencesService.isProjectMarkedForInspection(projectName)) {
				return Status.OK_STATUS;
			}
			componentInspectorService.initializeProjectComponents(projectName);
			final SubMonitor subMonitor = SubMonitor.convert(monitor, ONE_HUNDRED_PERCENT);
			subMonitor.setTaskName("Gathering dependencies");
			final List<URL> componentUrls = projectInformationService.getProjectComponentUrls(projectName);
			subMonitor.split(THIRTY_PERCENT).done();
			for (final URL componentUrl : componentUrls) {
				subMonitor.setTaskName(String.format("Inspecting %s", componentUrl));
				final Gav gav = componentInformationService.constructGavFromUrl(componentUrl);
				if (gav != null) {
					componentInspectorService.addComponentToProject(projectName, gav);
					if (componentUrls.size() < SEVENTY_PERCENT) {
						subMonitor.split(SEVENTY_PERCENT / componentUrls.size()).done();
					} else {
						subMonitor.split(SEVENTY_PERCENT).done();
					}
				}
			}
			return Status.OK_STATUS;
		} catch (final Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
	}

}
