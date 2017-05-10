/**
 * com.blackducksoftware.integration.eclipse.free.connector
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
package com.blackducksoftware.integration.eclipse.services.connection.free;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBLicenseDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBVulnerabilityDataService;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public class FreeConnectionService extends AbstractConnectionService {
	private final IntLogger logger;

	private RestConnection restConnection;

	private KBLicenseDataService licenseDataService;

	private KBVulnerabilityDataService vulnerabilityDataService;

	private FreePreferencesService freePreferencesService;

	private final ComponentInspectorViewService componentInspectorViewService;

	public static final String JOB_GENERATE_URL = "Opening component in the Hub...";

	public FreeConnectionService(final ComponentInspectorViewService componentInspectorViewService){
		this.logger = new IntBufferedLogger();
		this.componentInspectorViewService = componentInspectorViewService;
		this.reloadConnection();
	}

	private RestConnection getHubConnectionFromPreferences() {
		final HubServerConfig hubServerConfig = freePreferencesService.getHubServerConfig();
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

	@Override
	public void reloadConnection(){
		this.freePreferencesService = new FreePreferencesService();
		this.restConnection = this.getHubConnectionFromPreferences();
	}

	public CredentialsRestConnection getCredentialsRestConnection(final HubServerConfig config)
			throws IllegalArgumentException, EncryptionException, HubIntegrationException {
		return new CredentialsRestConnection(logger, config.getHubUrl(), config.getGlobalCredentials().getUsername(), config.getGlobalCredentials().getDecryptedPassword(), config.getTimeout());
	}

	public KBLicenseDataService getLicenseDataService() {
		if (licenseDataService == null) {
			licenseDataService = new KBLicenseDataService(null);
		}
		return licenseDataService;
	}

	public KBVulnerabilityDataService getVulnerabilityDataService() {
		if (vulnerabilityDataService == null) {
			vulnerabilityDataService = new KBVulnerabilityDataService();
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
	public void displayExpandedComponentInformation(final ComponentModel component){
		//TODO: Implement if necessary
	}

	@Override
	public void shutDown(){
		//TODO: Implement if necessary
	}

}
