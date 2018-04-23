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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.services.connection.AbstractComponentLookupService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV2View;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.LicenseService;

public class HubComponentLookupService extends AbstractComponentLookupService {
    public HubComponentLookupService(final HubConnectionService connectionService) {
        super(connectionService);
    }

    @Override
    public ComponentModel lookupComponent(final ExternalId externalId) throws IOException, URISyntaxException, IntegrationException {
        final LicenseService licenseService = ((HubConnectionService) connectionService).getLicenseDataService();
        final ComponentService componentService = ((HubConnectionService) connectionService).getComponentService();
        ComponentModel component = componentLoadingCache.get(externalId);
        if (component == null) {
            List<VulnerabilityV2View> vulnerabilities = null;
            ComplexLicenseView complexLicense = null;
            try {
                vulnerabilities = componentService.getVulnerabilitiesFromComponentVersion(externalId);
                complexLicense = licenseService.getComplexLicenseItemFromComponent(externalId);
            } catch (final IntegrationException e) {
                // Do nothing
                e.printStackTrace();
            }
            final int[] vulnerabilitySeverityCount = getVulnerabilitySeverityCount(vulnerabilities);
            final boolean componentKnown = (vulnerabilities != null);
            component = new ComponentModel(externalId, complexLicense, vulnerabilitySeverityCount, componentKnown);
        }
        return component;
    }

    public int[] getVulnerabilitySeverityCount(final List<VulnerabilityV2View> vulnerabilities) {
        int high = 0;
        int medium = 0;
        int low = 0;
        if (vulnerabilities == null) {
            return new int[] { 0, 0, 0 };
        }
        for (final VulnerabilityV2View vuln : vulnerabilities) {
            switch (vuln.severity) {
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
        return new int[] { high, medium, low };
    }

}
