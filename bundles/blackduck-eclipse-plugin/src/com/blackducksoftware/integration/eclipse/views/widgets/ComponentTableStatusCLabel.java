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
package com.blackducksoftware.integration.eclipse.views.widgets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.views.ComponentInspectorView;

public class ComponentTableStatusCLabel extends CLabel {
    private final ComponentInspectorPreferencesService componentInspectorPreferencesService;
    private final ProjectInformationService projectInformationService;
    private final TableViewer componentInspectorTableViewer;
    private final ComponentInspectorService componentInspectorService;
    private final HubConnectionService hubConnectionService;

    public static final String NO_SELECTED_PROJECT_STATUS = "No open project selected";
    public static final String PROJECT_INSPECTION_RUNNING_STATUS = "Inspecting project...";
    public static final String PROJECT_INSPECTION_SCHEDULED_STATUS = "Project scheduled for inspection";
    public static final String PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS = "Inspection not activated for current project";
    public static final String PROJECT_NEEDS_INSPECTION_STATUS = "Project has not yet been inspected";
    public static final String CONNECTION_DISCONNECTED_STATUS = "Disconnected";
    public static final String HUB_CONNECTION_OK_STATUS = "Connected to Hub instance - double-click any component to open it in the Hub";
    public static final String HUB_CONNECTION_OK_NO_COMPONENTS_STATUS = "Connected to Hub instance - No components found.";
    public static final String PROJECT_NOT_SUPPORTED_STATUS = "Cannot inspect selected project - either it is not a Java project or no Maven or Gradle nature was detected";

    public ComponentTableStatusCLabel(final Composite parent, final int style, final TableViewer componentInspectorTableViewer, final ComponentInspectorService componentInspectorService) {
        super(parent, style);
        this.componentInspectorPreferencesService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorPreferencesService();
        this.projectInformationService = BlackDuckEclipseServicesFactory.getInstance().getProjectInformationService();
        this.componentInspectorTableViewer = componentInspectorTableViewer;
        this.hubConnectionService = BlackDuckEclipseServicesFactory.getInstance().getHubConnectionService();
        this.componentInspectorService = componentInspectorService;
        this.setText(NO_SELECTED_PROJECT_STATUS);
    }

    public void updateStatus(final String projectName) {
        if (componentInspectorTableViewer != null && componentInspectorTableViewer.getInput() != null) {
            final boolean noComponents = ((ComponentModel[]) componentInspectorTableViewer.getInput()).length == 0;
            final boolean noProjectMapping = componentInspectorService.getProjectComponents(projectName) == null;
            final String statusMessage = determineStatusMessage(noComponents, noProjectMapping, projectName);
            this.setStatus(statusMessage);
        }
    }

    private String determineStatusMessage(final boolean noComponents, final boolean noProjectMapping, final String projectName) {
        String status = "";

        if (projectName.isEmpty()) {
            status = NO_SELECTED_PROJECT_STATUS;
        } else if (!projectInformationService.isProjectSupported(projectName)) {
            status = PROJECT_NOT_SUPPORTED_STATUS;
        } else if (!componentInspectorPreferencesService.isProjectMarkedForInspection(projectName)) {
            status = PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS;
        } else if (!hubConnectionService.hasActiveConnection()) {
            status = CONNECTION_DISCONNECTED_STATUS;
        } else if (componentInspectorService.isProjectInspectionRunning(projectName)) {
            status = PROJECT_INSPECTION_RUNNING_STATUS;
        } else if (componentInspectorService.isProjectInspectionScheduled(projectName)) {
            status = PROJECT_INSPECTION_SCHEDULED_STATUS;
        } else if (noProjectMapping) {
            status = PROJECT_NEEDS_INSPECTION_STATUS;
        } else if (hubConnectionService.hasActiveConnection()) {
            if (noComponents) {
                status = HUB_CONNECTION_OK_NO_COMPONENTS_STATUS;
            } else {
                status = HUB_CONNECTION_OK_STATUS;
            }
        }

        return status;
    }

    private void setStatus(final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!isDisposed() && !message.equals(getText())) {
                    if (message.equals(PROJECT_INSPECTION_RUNNING_STATUS) || message.equals(PROJECT_INSPECTION_SCHEDULED_STATUS)) {
                        final ImageDescriptor imageDescriptor = createImageDescriptorFromImagePath(ComponentInspectorView.WAITING_PNG_PATH);
                        setStatusMessageAndImage(imageDescriptor, message);
                    } else if (message.equals(CONNECTION_DISCONNECTED_STATUS)) {
                        final ImageDescriptor imageDescriptor = createImageDescriptorFromImagePath(ComponentInspectorView.DISCONNECT_PNG_PATH);
                        setStatusMessageAndImage(imageDescriptor, message);
                    } else if (message.equals(PROJECT_NEEDS_INSPECTION_STATUS)) {
                        final ImageDescriptor imageDescriptor = createImageDescriptorFromImagePath(ComponentInspectorView.WARNING_PNG_PATH);
                        setStatusMessageAndImage(imageDescriptor, message);
                    } else {
                        setStatusMessageNoImage(message);
                    }
                }
            }

            private ImageDescriptor createImageDescriptorFromImagePath(final String imagePath) {
                return BlackDuckEclipseActivator.imageDescriptorFromPlugin(BlackDuckEclipseActivator.PLUGIN_ID, imagePath);
            }

            private void setStatusMessageAndImage(final ImageDescriptor imageDescriptor, final String message) {
                final Image image = imageDescriptor.createImage();
                setImage(image);
                setText(message);
            }

            private void setStatusMessageNoImage(final String message) {
                setImage(null);
                setText(message);
            }
        });
    }

}
