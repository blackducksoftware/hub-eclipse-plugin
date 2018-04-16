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

import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ComponentModel {
    private final ExternalId gav;

    private final ComplexLicenseView license;

    private final int[] vulnerabilityCount;

    private final boolean componentIsKnown;

    public ComponentModel(final ExternalId gav, final ComplexLicenseView license, final int[] vulnerabilityCount, final boolean componentIsKnown) {
        this.gav = gav;
        this.license = license;
        this.vulnerabilityCount = vulnerabilityCount;
        this.componentIsKnown = componentIsKnown;
    }

    public ExternalId getExternalId() {
        return gav;
    }

    public ComplexLicenseView getLicense() {
        return license;
    }

    public int[] getVulnerabilityCount() {
        return vulnerabilityCount;
    }

    public boolean getComponentIsKnown() {
        return componentIsKnown;
    }

    public boolean getLicenseIsKnown() {
        return license != null;
    }

}
