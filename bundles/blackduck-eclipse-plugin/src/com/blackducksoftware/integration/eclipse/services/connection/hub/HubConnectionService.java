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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.progress.UIJob;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UriCombiner;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.LicenseService;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

public class HubConnectionService extends AbstractConnectionService {
    private final IntLogger logger;

    private HubServicesFactory hubServicesFactory;
    private RestConnection restConnection;

    private LicenseService licenseService;
    private ComponentService componentService;
    private PhoneHomeService phoneHomeService;
    private HubService hubService;

    private final HubPreferencesService hubPreferencesService;
    private final ComponentInspectorViewService componentInspectorViewService;

    public static final String JOB_GENERATE_URL = "Opening component in the Hub...";

    public HubConnectionService(final ComponentInspectorViewService componentInspectorViewService) {
        this.logger = new IntBufferedLogger();
        this.hubPreferencesService = BlackDuckEclipseServicesFactory.getInstance().getHubPreferencesService();
        this.componentInspectorViewService = componentInspectorViewService;
        this.reloadConnection();
    }

    @Override
    public void reloadConnection() {
        this.restConnection = this.getHubConnectionFromPreferences();
        this.hubServicesFactory = new HubServicesFactory(restConnection);
        try {
            this.phoneHome();
        } catch (final IntegrationException e) {
            // TODO: Log properly
        }
    }

    private RestConnection getHubConnectionFromPreferences() {
        final HubServerConfig hubServerConfig = hubPreferencesService.getHubServerConfig();
        if (hubServerConfig == null) {
            return null;
        }
        CredentialsRestConnection connection;
        try {
            connection = hubServerConfig.createCredentialsRestConnection(logger);
            connection.connect();
        } catch (final IntegrationException e) {
            return null;
        }
        return connection;
    }

    public CredentialsRestConnection getCredentialsRestConnection(final HubServerConfig config) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return new CredentialsRestConnection(logger, config.getHubUrl(), config.getGlobalCredentials().getUsername(), config.getGlobalCredentials().getDecryptedPassword(), config.getTimeout(), config.getProxyInfo(), new UriCombiner());
    }

    public LicenseService getLicenseDataService() {
        if (licenseService == null) {
            licenseService = hubServicesFactory.createLicenseService();
        }
        return licenseService;
    }

    public ComponentService getComponentService() {
        if (componentService == null) {
            componentService = hubServicesFactory.createComponentService();
        }
        return componentService;
    }

    public HubService getHubService() {
        if (hubService == null) {
            hubService = hubServicesFactory.createHubService();
        }
        return hubService;
    }

    public PhoneHomeService getPhoneHomeService() {
        if (phoneHomeService == null) {
            phoneHomeService = hubServicesFactory.createPhoneHomeService();
        }
        return phoneHomeService;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    @Override
    public boolean hasActiveConnection() {
        return restConnection != null;
    }

    public void phoneHome() throws IntegrationException {
        if (!this.hasActiveConnection()) {
            return;
        }
        final PhoneHomeService phoneHomeService = this.getPhoneHomeService();
        final IProduct eclipseProduct = Platform.getProduct();
        final String eclipseVersion = eclipseProduct.getDefiningBundle().getVersion().toString();
        final String pluginVersion = Platform.getBundle(BlackDuckEclipseActivator.PLUGIN_ID).getVersion().toString();
        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("Hub-Eclipse", pluginVersion);
        phoneHomeRequestBodyBuilder.addToMetaData("eclipse.version", eclipseVersion);
        phoneHomeService.phoneHome(phoneHomeRequestBodyBuilder);
    }

    @Override
    public void displayExpandedComponentInformation(final ComponentModel component) {
        final ComponentService componentService = this.getComponentService();
        final Job job = new UIJob(JOB_GENERATE_URL) {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                final ExternalId externalId = component.getExternalId();
                String link;
                try {
                    final ComponentVersionView selectedComponentVersion = componentService.getComponentVersion(externalId);
                    // Final solution, will work once the redirect is set up
                    link = getHubService().getHref(selectedComponentVersion);
                    // But for now...
                    final String versionID = link.substring(link.lastIndexOf("/") + 1);
                    link = getRestConnection().baseUrl.toString();
                    link = link + "/#versions/id:" + versionID + "/view:overview";
                    final IWebBrowser browser = getBrowser();
                    browser.openURL(new URL(link));
                } catch (PartInitException | MalformedURLException | IntegrationException e) {
                    componentInspectorViewService.openError("Could not open Component in Hub instance",
                            String.format("Problem opening %1$s %2$s in %3$s, are you connected to your hub instance?",
                                    externalId.name, externalId.version, getRestConnection().baseUrl),
                            e);
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }

        };
        job.schedule();
    }

    private IWebBrowser getBrowser() throws PartInitException {
        if (PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable()) {
            return PlatformUI.getWorkbench().getBrowserSupport().createBrowser(BlackDuckEclipseActivator.PLUGIN_ID);
        } else {
            return PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
        }
    }

    @Override
    public void shutDown() {
        try {
            final IWebBrowser browser = getBrowser();
            if (browser.getId().equals(BlackDuckEclipseActivator.PLUGIN_ID)) {
                browser.close();
            }
        } catch (final Exception e) {
            // TODO: Log correctly
        }
    }

}
