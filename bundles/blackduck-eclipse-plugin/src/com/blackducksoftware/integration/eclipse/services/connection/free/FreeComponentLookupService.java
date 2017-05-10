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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBLicenseDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBVulnerabilityDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.CVEVulnerabilityView;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractComponentLookupService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.buildtool.Gav;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.VulnerabilitySeverityEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;

public class FreeComponentLookupService extends AbstractComponentLookupService{
	public final KBVulnerabilityDataService vulnerabilityDataService;

	public final KBLicenseDataService licenseDataService;

	public FreeComponentLookupService(final FreeConnectionService connectionService){
		super(connectionService);
		this.licenseDataService = connectionService.getLicenseDataService();
		this.vulnerabilityDataService = connectionService.getVulnerabilityDataService();
	}

	@Override
	public ComponentModel lookupComponent(final Gav gav) throws IOException, URISyntaxException, IntegrationException {
		ComponentModel component = componentLoadingCache.get(gav);
		if(component == null){
			List<CVEVulnerabilityView> vulnerabilities = null;
			ComplexLicenseView complexLicense = null;
			int premiumVulnerabilities = -1;
			try {
				vulnerabilities = vulnerabilityDataService.getVulnsFromComponentVersion(gav.getNamespace().toLowerCase(), gav.getGroupId(),
						gav.getArtifactId(), gav.getVersion());
				premiumVulnerabilities = vulnerabilityDataService.getPremiumVulnerabilityCount(gav.getNamespace().toLowerCase(), gav.getGroupId(),
						gav.getArtifactId(), gav.getVersion());
				complexLicense = licenseDataService.getComplexLicenseViewFromComponent(gav.getNamespace().toLowerCase(), gav.getGroupId(),
						gav.getArtifactId(), gav.getVersion());
			} catch (final HubIntegrationException e) {
				// Do nothing
			}
			final int[] vulnerabilitySeverityCount = getVulnerabilitySeverityCount(vulnerabilities, premiumVulnerabilities);
			final boolean componentKnown = (vulnerabilities != null);
			component = new ComponentModel(gav, complexLicense, vulnerabilitySeverityCount, componentKnown);
		}
		return component;
	}

	public int[] getVulnerabilitySeverityCount(final List<CVEVulnerabilityView> vulnerabilities, final int premiumVulnerabilities) {
		int high = 0;
		int medium = 0;
		int low = 0;
		if (vulnerabilities == null) {
			return new int[] { 0, 0, 0, premiumVulnerabilities };
		}
		for (final CVEVulnerabilityView vuln : vulnerabilities) {
			switch (VulnerabilitySeverityEnum.valueOf(vuln.severity)) {
			case HIGH:
				high++;
				break;
			case MEDIUM:
				medium++;
				break;
			case LOW:
				low++;
				break;
			default:
				break;
			}
		}
		return new int[] { high, medium, low, premiumVulnerabilities };
	}

}
