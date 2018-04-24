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
package com.blackducksoftware.integration.eclipse.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.progress.UIJob;

import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class OpenComponentInHubJob extends UIJob {
    public static final String JOB_OPEN_COMPONENT_PREFACE = "Black Duck Component Inspector opening ";
    public static final String JOB_OPEN_COMPONENT_SUFFIX = " in the Hub...";

    private final ComponentModel component;
    private final HubConnectionService hubConnectionService;
    private final ComponentInspectorViewService componentInspectorViewService;

    public OpenComponentInHubJob(final HubConnectionService hubConnectionService, final ComponentInspectorViewService componentInspectorViewService, final ComponentModel component) {
        super(JOB_OPEN_COMPONENT_PREFACE + component.toString() + JOB_OPEN_COMPONENT_SUFFIX);
        this.component = component;
        this.hubConnectionService = hubConnectionService;
        this.componentInspectorViewService = componentInspectorViewService;
    }

    @Override
    public IStatus runInUIThread(final IProgressMonitor monitor) {
        final ExternalId externalId = component.getExternalId();
        try {
            final String link = hubConnectionService.getComponentVersionLinkFromExternalId(externalId);
            final IWebBrowser browser = componentInspectorViewService.getBrowser();
            browser.openURL(new URL(link));
        } catch (final MalformedURLException | IntegrationException | PartInitException e) {
            componentInspectorViewService.openError("Could not open Component in Hub instance",
                    String.format("Problem opening %1$s %2$s in %3$s, are you connected to your hub instance?",
                            externalId.name, externalId.version, hubConnectionService.getRestConnection().baseUrl),
                    e);
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }
}
