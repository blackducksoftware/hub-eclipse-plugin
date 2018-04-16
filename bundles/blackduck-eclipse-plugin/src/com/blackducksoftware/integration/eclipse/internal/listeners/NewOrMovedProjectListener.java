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
package com.blackducksoftware.integration.eclipse.internal.listeners;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import com.blackducksoftware.integration.eclipse.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;

public class NewOrMovedProjectListener implements IResourceChangeListener {
    private final ComponentInspectorService componentInspectorService;

    private final ProjectInformationService projectInformationService;

    private final ComponentInspectorPreferencesService componentInspectorPreferencesService;

    public NewOrMovedProjectListener(final ComponentInspectorService componentInspectorService, final ProjectInformationService projectInformationService, final ComponentInspectorPreferencesService componentInspectorPreferencesService) {
        super();
        this.componentInspectorService = componentInspectorService;
        this.projectInformationService = projectInformationService;
        this.componentInspectorPreferencesService = componentInspectorPreferencesService;
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        final IResourceDelta eventDelta = event.getDelta();
        if (eventDelta == null) {
            return;
        }
        final IResourceDelta[] childrenDeltas = eventDelta.getAffectedChildren();
        for (final IResourceDelta delta : childrenDeltas) {
            final IResource project = delta.getResource();
            final boolean projectWasAdded = delta.getKind() == IResourceDelta.ADDED;
            final boolean projectWasMoved = delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0;
            if (project instanceof IProject && (projectWasAdded || projectWasMoved)) {
                final String projectName = project.getName();
                if (projectInformationService.isProjectSupported(projectName)) {
                    if (projectWasAdded) {
                        final String oldProjectName = delta.getMovedFromPath().toFile().getName();
                        if (componentInspectorPreferencesService.isProjectMarkedForInspection(oldProjectName)) {
                            componentInspectorPreferencesService.markProjectForInspection(projectName);
                        }
                    } else if (!componentInspectorPreferencesService.projectExists(projectName)){
                        componentInspectorPreferencesService.setProjectMarkedForInspection(projectName, componentInspectorPreferencesService.getInspectByDefault());
                    }
                    if (componentInspectorPreferencesService.isProjectMarkedForInspection(projectName)) {
                        componentInspectorService.inspectProject(projectName);
                    }
                }
            }
        }
    }

}
