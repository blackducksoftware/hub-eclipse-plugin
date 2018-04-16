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
package com.blackducksoftware.integration.eclipse.services.connection.free;

import java.net.MalformedURLException;
import java.net.URL;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsRequestService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBLicenseDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBVulnerabilityDataService;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.hub.rest.UriCombiner;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public class FreeConnectionService extends AbstractConnectionService {
    private final IntLogger logger;

    private RestConnection restConnection;

    private KBLicenseDataService licenseDataService;

    private KBVulnerabilityDataService vulnerabilityDataService;

    private KBDetailsRequestService kbDetailsRequestService;

    private final FreePreferencesService freePreferencesService;

    public static final String JOB_GENERATE_URL = "Opening component in the Hub...";

    public static final String KB_URL = "https://kbtest.blackducksoftware.com/";

    public FreeConnectionService(final ComponentInspectorViewService componentInspectorViewService) {
        this.logger = new IntBufferedLogger();
        this.freePreferencesService = BlackDuckEclipseServicesFactory.getInstance().getFreePreferencesService();
        this.reloadConnection();
    }

    @Override
    public void reloadConnection() {
        this.restConnection = this.getKBRestConnectionFromPreferences();
        this.kbDetailsRequestService = new KBDetailsRequestService(restConnection);
    }

    private RestConnection getKBRestConnectionFromPreferences() {
        final UnauthenticatedRestConnection connection;
        try {
            final ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
            proxyInfoBuilder.setHost(freePreferencesService.getHubProxyHost());
            proxyInfoBuilder.setPort(freePreferencesService.getHubProxyPort());
            proxyInfoBuilder.setUsername(freePreferencesService.getHubProxyUsername());
            proxyInfoBuilder.setPassword(freePreferencesService.getHubProxyPassword());
            final ProxyInfo proxyInfo = proxyInfoBuilder.build();
            connection = new UnauthenticatedRestConnection(logger, new URL(KB_URL), 120, proxyInfo, new UriCombiner());
            connection.connect();
        } catch (final IntegrationException | MalformedURLException e) {
            return null;
        }
        return connection;
    }

    public KBLicenseDataService getLicenseDataService() {
        if (licenseDataService == null) {
            licenseDataService = new KBLicenseDataService(kbDetailsRequestService);
        }
        return licenseDataService;
    }

    public KBVulnerabilityDataService getVulnerabilityDataService() {
        if (vulnerabilityDataService == null) {
            vulnerabilityDataService = new KBVulnerabilityDataService(kbDetailsRequestService);
        }
        return vulnerabilityDataService;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    @Override
    public boolean hasActiveConnection() {
        return restConnection != null;
    }

    @Override
    public void displayExpandedComponentInformation(final ComponentModel component) {
        // TODO: Implement if necessary
    }

    @Override
    public void shutDown() {
        // TODO: Implement if necessary
    }

}
