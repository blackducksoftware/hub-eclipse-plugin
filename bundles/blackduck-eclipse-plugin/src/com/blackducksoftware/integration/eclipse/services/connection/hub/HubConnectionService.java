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
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.internal.DecryptedHubServerConfig;
import com.blackducksoftware.integration.eclipse.internal.DecryptedHubServerConfigBuilder;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.SilentLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class HubConnectionService {
    private final Logger log = LoggerFactory.getLogger(HubConnectionService.class);

    public CredentialsRestConnection getCredentialsRestConnection(final String username, final String password, final String hubUrl, final String timeout, final boolean hubAlwaysTrust, final String proxyUsername, final String proxyPassword, final String proxyPort, final String proxyHost) throws IllegalStateException, EncryptionException {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        final DecryptedHubServerConfigBuilder decryptedHubServerConfigBuilder = new DecryptedHubServerConfigBuilder();

        hubServerConfigBuilder.setUsername(username);
        hubServerConfigBuilder.setPassword(password);
        hubServerConfigBuilder.setUrl(hubUrl);
        hubServerConfigBuilder.setTimeout(timeout);
        hubServerConfigBuilder.setTrustCert(hubAlwaysTrust);
        hubServerConfigBuilder.setProxyUsername(proxyUsername);
        hubServerConfigBuilder.setProxyPassword(proxyPassword);
        hubServerConfigBuilder.setProxyPort(proxyPort);
        hubServerConfigBuilder.setProxyHost(proxyHost);

        HubServerConfig hubServerConfig = null;
        DecryptedHubServerConfig decryptedHubServerConfig = null;

        try {
            hubServerConfig = hubServerConfigBuilder.build();
        } catch (final Exception e) {
            decryptedHubServerConfigBuilder.setUsername(username);
            decryptedHubServerConfigBuilder.setPassword(password);
            decryptedHubServerConfigBuilder.setUrl(hubUrl);
            decryptedHubServerConfigBuilder.setTimeout(timeout);
            decryptedHubServerConfigBuilder.setTrustCert(hubAlwaysTrust);
            decryptedHubServerConfigBuilder.setProxyUsername(proxyUsername);
            decryptedHubServerConfigBuilder.setProxyPassword(proxyPassword);
            decryptedHubServerConfigBuilder.setProxyPort(proxyPort);
            decryptedHubServerConfigBuilder.setProxyHost(proxyHost);

            decryptedHubServerConfig = (DecryptedHubServerConfig) decryptedHubServerConfigBuilder.build();

            log.error("Encryption or decryption failed. Your passwords may not be stored in an encrypted format!");
            log.debug("Stack trace:", e);
        }

        final IntLogger logger = new SilentLogger();

        return decryptedHubServerConfig == null ? hubServerConfig.createCredentialsRestConnection(logger) : decryptedHubServerConfig.createCredentialsRestConnection(logger);
    }

    public String getComponentVersionLinkFromExternalId(final Optional<RestConnection> connection, final ExternalId externalId) throws IntegrationException {
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

    public void phoneHome(final Optional<RestConnection> connection) {
        if (connection.isPresent()) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    final HubServicesFactory hubServicesFactory = new HubServicesFactory(connection.get());
                    final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
                    final IProduct eclipseProduct = Platform.getProduct();
                    final String eclipseVersion = eclipseProduct.getDefiningBundle().getVersion().toString();
                    final String pluginVersion = Platform.getBundle(BlackDuckEclipseActivator.PLUGIN_ID).getVersion().toString();
                    final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("hub-eclipse", pluginVersion);
                    phoneHomeRequestBodyBuilder.addToMetaData("eclipse.version", eclipseVersion);
                    phoneHomeService.phoneHome(phoneHomeRequestBodyBuilder);
                }
            });
        }
    }

}
