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
package com.blackducksoftware.integration.eclipse.services.connection.hub;

import java.util.Optional;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractConnectionService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

public class HubConnectionService extends AbstractConnectionService {
    private final IntLogger logger;
    private Optional<RestConnection> connection;

    private final HubPreferencesService hubPreferencesService;

    public HubConnectionService(final HubPreferencesService hubPreferencesService) {
        this.logger = new IntBufferedLogger();
        this.hubPreferencesService = hubPreferencesService;
    }

    @Override
    public void reloadConnection() {
        this.connection = this.getHubConnectionFromPreferences();
        try {
            this.phoneHome();
        } catch (final IntegrationException e) {
            // TODO: Log properly
        }
    }

    @Override
    public boolean hasActiveConnection() {
        return connection.isPresent();
    }

    public Optional<RestConnection> getConnection() {
        return connection;
    }

    public String getComponentVersionLinkFromExternalId(final ExternalId externalId) throws IntegrationException {
        if (connection.isPresent()) {
            final HubServicesFactory hubServicesFactory = new HubServicesFactory(connection.get());
            final ComponentService componentService = hubServicesFactory.createComponentService();
            final HubService hubService = hubServicesFactory.createHubService();
            final ComponentVersionView componentVersionView = componentService.getComponentVersion(externalId);
            final String componentVersionLink = hubService.getHref(componentVersionView);
            return componentVersionLink;
        }
        throw new IntegrationException("No active connection with a Hub server");
    }

    private void phoneHome() throws IntegrationException {
        if (hasActiveConnection()) {
            final HubServicesFactory hubServicesFactory = new HubServicesFactory(connection.get());
            final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
            final IProduct eclipseProduct = Platform.getProduct();
            final String eclipseVersion = eclipseProduct.getDefiningBundle().getVersion().toString();
            final String pluginVersion = Platform.getBundle(BlackDuckEclipseActivator.PLUGIN_ID).getVersion().toString();
            final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("hub-eclipse", pluginVersion);
            phoneHomeRequestBodyBuilder.addToMetaData("eclipse.version", eclipseVersion);
            phoneHomeService.phoneHome(phoneHomeRequestBodyBuilder);
        }
    }

    private Optional<RestConnection> getHubConnectionFromPreferences() {
        RestConnection connection = null;

        try {
            final HubServerConfig hubServerConfig = hubPreferencesService.getHubServerConfig();
            connection = hubServerConfig.createCredentialsRestConnection(logger);
            connection.connect();
        } catch (final Exception e) {
        }

        return Optional.ofNullable(connection);
    }

}
