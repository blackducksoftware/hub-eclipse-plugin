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
package com.blackducksoftware.integration.eclipse.internal.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import com.blackducksoftware.integration.eclipse.internal.InspectionJob;
import com.blackducksoftware.integration.eclipse.internal.listeners.InspectionJobChangeListener;

public class InspectionJobQueue {
    public final ConcurrentLinkedQueue<InspectionJob> inspectionQueue;
    private Optional<InspectionJobChangeListener> inspectionJobChangeListener = Optional.empty();
    private Optional<InspectionJob> currentInspection = Optional.empty();

    public InspectionJobQueue(final InspectionJobChangeListener inspectionJobChangeListener) {
        this.inspectionQueue = new ConcurrentLinkedQueue<>();
        this.inspectionJobChangeListener = Optional.ofNullable(inspectionJobChangeListener);
        if (this.inspectionJobChangeListener.isPresent()) {
            this.inspectionJobChangeListener.get().registerInspectionJobQueue(this);
        }
    }

    public boolean enqueueInspection(final InspectionJob inspection) {
        if (inspection == null || !inspectionJobChangeListener.isPresent() || getInspectionIsRunning(inspection.getProjectName()) || getInspectionIsScheduled(inspection.getProjectName())) {
            return false;
        }
        inspection.addJobChangeListener(inspectionJobChangeListener.get());
        if (!currentInspection.isPresent()) {
            currentInspection = Optional.ofNullable(inspection);
            if (currentInspection.isPresent()) {
                currentInspection.get().schedule();
                return true;
            }
        }
        return inspectionQueue.add(inspection);
    }

    public Optional<InspectionJob> getCurrentInspection() {
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
        return currentInspection.isPresent() && currentInspection.get().getProjectName().equals(projectName);
    }

    public boolean getInspectionIsScheduled(final String projectName) {
        for (final InspectionJob queuedInspection : inspectionQueue) {
            if (queuedInspection.getProjectName().equals(projectName)) {
                return true;
            }
        }
        return false;
    }

    public void currentInspectionDone() {
        currentInspection = Optional.empty();
        if (!inspectionQueue.isEmpty()) {
            final InspectionJob nextJob = inspectionQueue.poll();
            currentInspection = Optional.of(nextJob);
            nextJob.schedule();
        }
    }

    public void cancelAll() {
        inspectionQueue.forEach(inspection -> {
            if (inspectionJobChangeListener.isPresent()) {
                inspection.removeJobChangeListener(inspectionJobChangeListener.get());
            }
            inspection.cancel();
        });
        while (!inspectionQueue.isEmpty()) {
            inspectionQueue.remove();
        }
        if (currentInspection.isPresent()) {
            if (inspectionJobChangeListener.isPresent()) {
                currentInspection.get().removeJobChangeListener(inspectionJobChangeListener.get());
            }
            currentInspection.get().cancel();
            currentInspection = Optional.empty();
        }
    }

}
