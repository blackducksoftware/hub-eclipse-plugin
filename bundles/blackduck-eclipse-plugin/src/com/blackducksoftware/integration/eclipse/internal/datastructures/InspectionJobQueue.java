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
package com.blackducksoftware.integration.eclipse.internal.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import com.blackducksoftware.integration.eclipse.internal.InspectionJob;
import com.blackducksoftware.integration.eclipse.internal.listeners.InspectionJobChangeListener;

public class InspectionJobQueue {
	public final ConcurrentLinkedQueue<InspectionJob> inspectionQueue;

	private InspectionJobChangeListener inspectionJobChangeListener = null;

	private InspectionJob currentInspection = null;

	public InspectionJobQueue(final InspectionJobChangeListener inspectionJobChangeListener) {
		this.inspectionQueue = new ConcurrentLinkedQueue<>();
		this.inspectionJobChangeListener = inspectionJobChangeListener;
		inspectionJobChangeListener.registerInspectionJobQueue(this);
	}

	public boolean enqueueInspection(final InspectionJob inspection) {
		if(inspectionJobChangeListener == null ) {
			return false;
		}
		inspection.addJobChangeListener(inspectionJobChangeListener);
		if (getInspectionIsRunning(inspection.getProjectName()) || getInspectionIsScheduled(inspection.getProjectName())) {
			return false;
		}
		if (currentInspection == null) {
			currentInspection = inspection;
			inspection.schedule();
			return true;
		}
		return inspectionQueue.add(inspection);
	}

	public InspectionJob getCurrentInspection() {
		return currentInspection;
	}

	public List<String> getScheduledInspectionsNames() {
		final ArrayList<String> scheduledInspectionList = new ArrayList<>();
		inspectionQueue.forEach(inspection -> scheduledInspectionList.add(inspection.getName()));
		return scheduledInspectionList;
	}

	public List<String> getRunningInspectionsNames() {
		final IJobManager jobMan = Job.getJobManager();
		final ArrayList<String> inspectionList = new ArrayList<>();
		final Job[] inspections = jobMan.find(InspectionJob.FAMILY);
		for (final Job inspection : inspections) {
			inspectionList.add(inspection.getName());
		}
		return inspectionList;
	}

	public boolean getInspectionIsRunning(final String projectName) {
		return currentInspection != null && currentInspection.getProjectName().equals(projectName);
	}

	public boolean getInspectionIsScheduled(final String projectName) {
		for (final InspectionJob queuedInspection : inspectionQueue) {
			if (queuedInspection.getProjectName().equals(projectName)) {
				return true;
			}
		}
		return false;
	}

	public void currentInspectionDone(){
		currentInspection = null;
		if (!inspectionQueue.isEmpty()) {
			currentInspection = inspectionQueue.poll();
			currentInspection.schedule();
		}
	}

	public void cancelAll(){
		inspectionQueue.forEach(inspection -> {
			inspection.removeJobChangeListener(inspectionJobChangeListener);
			inspection.cancel();
		});
		while (!inspectionQueue.isEmpty()) {
			inspectionQueue.remove();
		}
		if (currentInspection != null) {
			currentInspection.removeJobChangeListener(inspectionJobChangeListener);
			currentInspection.cancel();
			currentInspection = null;
		}
	}
}
