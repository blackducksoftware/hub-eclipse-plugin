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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBLicenseDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices.KBVulnerabilityDataService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.CVEVulnerabilityView;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractComponentLookupService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class FreeComponentLookupService extends AbstractComponentLookupService {
    public FreeComponentLookupService(final FreeConnectionService connectionService) {
        super(connectionService);
    }

    @Override
    public ComponentModel lookupComponent(final ExternalId externalId) throws IOException, URISyntaxException, IntegrationException {
        final KBLicenseDataService licenseDataService = ((FreeConnectionService) connectionService).getLicenseDataService();
        final KBVulnerabilityDataService vulnerabilityDataService = ((FreeConnectionService) connectionService).getVulnerabilityDataService();
        ComponentModel component = componentLoadingCache.get(externalId);
        if (component == null) {
            List<CVEVulnerabilityView> vulnerabilities = null;
            ComplexLicenseView complexLicense = null;
            int premiumVulnerabilities = -1;
            try {
                vulnerabilities = vulnerabilityDataService.getCVEsFromComponentVersion(externalId.forge.toString().toLowerCase(), externalId.group,
                        externalId.name, externalId.version);
                premiumVulnerabilities = vulnerabilityDataService.getPremiumVulnerabilityCount(externalId.forge.toString().toLowerCase(), externalId.group,
                        externalId.name, externalId.version);
                complexLicense = licenseDataService.getComplexLicenseViewFromComponent(externalId.forge.toString().toLowerCase(), externalId.group,
                        externalId.name, externalId.version);
            } catch (final IntegrationException e) {
                // Do nothing
            }
            final int[] vulnerabilitySeverityCount = getVulnerabilitySeverityCount(vulnerabilities, premiumVulnerabilities);
            final boolean componentKnown = (vulnerabilities != null);
            component = new ComponentModel(externalId, complexLicense, vulnerabilitySeverityCount, componentKnown);
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
            switch (vuln.getSeverity().toUpperCase()) {
            case "HIGH":
                high++;
                break;
            case "MEDIUM":
                medium++;
                break;
            case "LOW":
                low++;
                break;
            default:
                break;
            }
        }
        return new int[] { high, medium, low, premiumVulnerabilities };
    }

}
