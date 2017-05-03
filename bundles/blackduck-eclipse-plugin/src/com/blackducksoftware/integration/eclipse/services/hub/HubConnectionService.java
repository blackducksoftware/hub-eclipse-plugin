/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.eclipse.services.hub;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.dataservice.license.LicenseDataService;
import com.blackducksoftware.integration.hub.dataservice.phonehome.PhoneHomeDataService;
import com.blackducksoftware.integration.hub.dataservice.vulnerability.VulnerabilityDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;

public class HubConnectionService {
	private HubServicesFactory hubServicesFactory;

	private final IntLogger logger;

	private RestConnection restConnection;

	private LicenseDataService licenseDataService;

	private VulnerabilityDataService vulnerabilityDataService;

	private ComponentDataService componentDataService;

	private MetaService metaService;

	private PhoneHomeDataService phoneHomeDataService;

	private HubVersionRequestService hubVersionRequestService;

	private HubPreferencesService hubPreferencesService;

	public HubConnectionService(){
		this.logger = new IntBufferedLogger();
		this.reloadConnection();
	}

	private RestConnection getHubConnectionFromPreferences() {
		final HubServerConfig hubServerConfig = hubPreferencesService.getHubServerConfig();
		if(hubServerConfig == null){
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

	public void reloadConnection(){
		this.hubPreferencesService = new HubPreferencesService();
		this.restConnection = this.getHubConnectionFromPreferences();
		this.hubServicesFactory = new HubServicesFactory(restConnection);
		try {
			this.phoneHome();
		} catch (final IntegrationException e) {
			//Do nothing
		}
	}

	public CredentialsRestConnection getCredentialsRestConnection(final HubServerConfig config)
			throws IllegalArgumentException, EncryptionException, HubIntegrationException {
		return new CredentialsRestConnection(logger, config.getHubUrl(), config.getGlobalCredentials().getUsername(), config.getGlobalCredentials().getDecryptedPassword(), config.getTimeout());
	}

	public LicenseDataService getLicenseDataService() {
		if (licenseDataService == null) {
			licenseDataService = hubServicesFactory.createLicenseDataService();
		}
		return licenseDataService;
	}

	public VulnerabilityDataService getVulnerabilityDataService() {
		if (vulnerabilityDataService == null) {
			vulnerabilityDataService = hubServicesFactory.createVulnerabilityDataService(logger);
		}
		return vulnerabilityDataService;
	}

	public ComponentDataService getComponentDataService() {
		if (componentDataService == null) {
			componentDataService = hubServicesFactory.createComponentDataService(logger);
		}
		return componentDataService;
	}

	public MetaService getMetaService() {
		if (metaService == null) {
			metaService = hubServicesFactory.createMetaService(logger);
		}
		return metaService;
	}

	public PhoneHomeDataService getPhoneHomeDataService() {
		if (phoneHomeDataService == null) {
			phoneHomeDataService = hubServicesFactory.createPhoneHomeDataService(logger);
		}
		return phoneHomeDataService;
	}

	public HubVersionRequestService getHubVersionRequestService() {
		if (hubVersionRequestService == null) {
			hubVersionRequestService = hubServicesFactory.createHubVersionRequestService();
		}
		return hubVersionRequestService;
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	public boolean hasActiveHubConnection() {
		return restConnection != null;
	}

	public void phoneHome() throws IntegrationException {
		if (!this.hasActiveHubConnection()) {
			return;
		}
		final PhoneHomeDataService phoneHomeService = this.getPhoneHomeDataService();
		final HubVersionRequestService hubVersionRequestService = hubServicesFactory.createHubVersionRequestService();
		final String hubVersion = hubVersionRequestService.getHubVersion();
		final IProduct eclipseProduct = Platform.getProduct();
		final String eclipseVersion = eclipseProduct.getDefiningBundle().getVersion().toString();
		final String pluginVersion = Platform.getBundle(BlackDuckHubPluginActivator.PLUGIN_ID).getVersion().toString();
		final HubServerConfig hubServerConfig = hubPreferencesService.getHubServerConfig();
		final IntegrationInfo integrationInfo = new IntegrationInfo(ThirdPartyName.ECLIPSE, eclipseVersion, pluginVersion);
		phoneHomeService.phoneHome(hubServerConfig, integrationInfo, hubVersion);
	}

}
