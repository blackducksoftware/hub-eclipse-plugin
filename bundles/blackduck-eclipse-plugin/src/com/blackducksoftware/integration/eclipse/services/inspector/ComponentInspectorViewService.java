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
package com.blackducksoftware.integration.eclipse.services.inspector;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.OpenComponentInHubJob;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.views.ComponentInspectorView;

public class ComponentInspectorViewService {
    private ComponentInspectorView componentInspectorView = null;

    public void registerComponentInspectorView(final ComponentInspectorView componentInspectorView) {
        this.componentInspectorView = componentInspectorView;
    }

    public ComponentInspectorView getView() {
        return componentInspectorView;
    }

    public void setProject(final String projectName) {
        if (componentInspectorView != null) {
            componentInspectorView.setLastSelectedProjectName(projectName);

        }
    }

    public void clearProjectDisplay(final String projectName) {
        if (componentInspectorView != null) {
            if (componentInspectorView.getLastSelectedProjectName().equals(projectName)) {
                componentInspectorView.setLastSelectedProjectName("");
            }
        }
    }

    public void resetDisplay() {
        if (componentInspectorView != null) {
            componentInspectorView.refreshStatus();
            componentInspectorView.refreshInput();
        }
    }

    public void refreshProjectStatus(final String projectName) {
        if (componentInspectorView != null) {
            if (componentInspectorView.getLastSelectedProjectName().equals(projectName)) {
                componentInspectorView.refreshStatus();
            }
        }
    }

    public void disposeComponentInspectorView() {
        this.componentInspectorView = null;
    }

    public void openError(final String string, final String format, final Exception e) {
        if (componentInspectorView != null) {
            componentInspectorView.openError(string, format, e);
        }
    }

    public void displayExpandedComponentInformation(final ComponentModel component) {
        final HubConnectionService hubConnectionService = BlackDuckEclipseServicesFactory.getInstance().getHubConnectionService();
        if (hubConnectionService.hasActiveConnection()) {
            final Job job = new OpenComponentInHubJob(hubConnectionService, this, component);
            job.schedule();
        }
    }

    public IWebBrowser getBrowser() throws PartInitException {
        IWebBrowser webBrowser = null;
        if (componentInspectorView != null) {
            webBrowser = componentInspectorView.getBrowser();
        }
        return webBrowser;
    }

}
