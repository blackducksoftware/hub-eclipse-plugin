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
package com.blackducksoftware.integration.eclipse.internal.listeners;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

import com.blackducksoftware.integration.eclipse.common.Constants;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.eclipse.internal.InspectionJob;
import com.blackducksoftware.integration.eclipse.internal.datastructures.InspectionJobQueue;

public class InspectionJobChangeListener implements IJobChangeListener{
	private final ComponentInspectorViewService componentInspectorViewService;
	private InspectionJobQueue inspectionJobQueue = null;

	public InspectionJobChangeListener(final ComponentInspectorViewService componentInspectorViewService){
		this.componentInspectorViewService = componentInspectorViewService;
	}

	public void registerInspectionJobQueue(final InspectionJobQueue inspectionJobQueue){
		this.inspectionJobQueue = inspectionJobQueue;
	}

	@Override
	public void aboutToRun(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.setProjectStatus(Constants.PROJECT_INSPECTION_ACTIVE, inspection.getProjectName());
	}

	@Override
	public void awake(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.setProjectStatus(Constants.PROJECT_INSPECTION_ACTIVE, inspection.getProjectName());
	}

	@Override
	public void done(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.resetProjectDisplay(inspection.getProjectName());
		inspectionJobQueue.currentInspectionDone();
	}

	@Override
	public void running(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.setProjectStatus(Constants.PROJECT_INSPECTION_ACTIVE, inspection.getProjectName());
	}

	@Override
	public void scheduled(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.setProjectStatus(Constants.PROJECT_INSPECTION_SCHEDULED, inspection.getProjectName());
	}

	@Override
	public void sleeping(final IJobChangeEvent event) {
		final InspectionJob inspection = (InspectionJob) event.getJob();
		componentInspectorViewService.setProjectStatus(Constants.PROJECT_INSPECTION_SCHEDULED, inspection.getProjectName());
	}

}
