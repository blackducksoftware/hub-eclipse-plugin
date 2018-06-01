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
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class OpenComponentInHubJob extends UIJob {
    private final Logger log = LoggerFactory.getLogger(OpenComponentInHubJob.class);

    public static final String JOB_OPEN_COMPONENT_PREFACE = "Black Duck Component Inspector opening ";
    public static final String JOB_OPEN_COMPONENT_SUFFIX = " in the Hub...";

    private final ComponentModel component;
    private final HubConnectionService hubConnectionService;
    private final HubPreferencesService hubPreferencesService;
    private final ComponentInspectorViewService componentInspectorViewService;

    public OpenComponentInHubJob(final HubConnectionService hubConnectionService, final HubPreferencesService hubPreferencesService, final ComponentInspectorViewService componentInspectorViewService, final ComponentModel component) {
        super(JOB_OPEN_COMPONENT_PREFACE + component.toString() + JOB_OPEN_COMPONENT_SUFFIX);
        this.hubConnectionService = hubConnectionService;
        this.hubPreferencesService = hubPreferencesService;
        this.component = component;
        this.componentInspectorViewService = componentInspectorViewService;
    }

    @Override
    public IStatus runInUIThread(final IProgressMonitor monitor) {
        final ExternalId externalId = component.getExternalId();
        try {
            final Optional<RestConnection> restConnection = hubPreferencesService.getHubConnectionFromPreferences();
            final String link = hubConnectionService.getComponentVersionLinkFromExternalId(restConnection, externalId);
            final Optional<IWebBrowser> browser = componentInspectorViewService.getBrowser();
            if (browser.isPresent()) {
                browser.get().openURL(new URL(link));
            }
        } catch (final MalformedURLException | IntegrationException | PartInitException e) {
            componentInspectorViewService.openError("Could not open Component in Hub instance",
                    "Problem opening " + externalId.createExternalId() + " in the configured Hub instance",
                    e);
            log.error("Problem opening " + externalId.createExternalId() + " in the configured Hub instance", e);
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }
}
