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
package com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsRequestService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsResponse;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.KBLicenseView;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.LicenseCodeSharingType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.LicenseView;

public class KBLicenseDataService {
    private final KBDetailsRequestService kbDetailsRequestService;

    public KBLicenseDataService(final KBDetailsRequestService kbDetailsRequestService) {
        this.kbDetailsRequestService = kbDetailsRequestService;
    }

    public ComplexLicenseView getComplexLicenseViewFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
            throws IntegrationException {
        final KBDetailsResponse kbDetails = kbDetailsRequestService.getKBDetailsFromComponentVersion(namespace, groupId, artifactId, version);
        final KBLicenseView license = kbDetails.license;
        try {
            return this.transformKBLicenseViewToComplexLicenseView(license);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ComplexLicenseView transformKBLicenseViewToComplexLicenseView(final KBLicenseView license) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        final ComplexLicenseView wrappingComplexLicenseView = new ComplexLicenseView();
        final Field typeField = wrappingComplexLicenseView.getClass().getDeclaredField("type");
        typeField.setAccessible(true);
        typeField.set(wrappingComplexLicenseView, license.getType());
        final List<ComplexLicenseView> licenses = new ArrayList<>();
        for (final LicenseView licenseView : license.getDetail().values()) {
            final ComplexLicenseView wrappedComplexLicenseView = new ComplexLicenseView();
            final Field codesharingField = wrappedComplexLicenseView.getClass().getDeclaredField("codeSharing");
            codesharingField.setAccessible(true);
            codesharingField.set(wrappedComplexLicenseView, LicenseCodeSharingType.valueOf(licenseView.codeSharing.toString()));
            final Field nameField = wrappedComplexLicenseView.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(wrappedComplexLicenseView, licenseView.name);
            final Field ownershipField = wrappedComplexLicenseView.getClass().getDeclaredField("ownership");
            ownershipField.setAccessible(true);
            ownershipField.set(wrappedComplexLicenseView, LicenseCodeSharingType.valueOf(licenseView.ownership.toString()));
            final Field licensesField = wrappedComplexLicenseView.getClass().getDeclaredField("licenses");
            licensesField.setAccessible(true);
            licensesField.set(wrappedComplexLicenseView, new ArrayList<>());
            licenses.add(wrappedComplexLicenseView);
        }
        final Field licensesField = wrappingComplexLicenseView.getClass().getDeclaredField("licenses");
        licensesField.setAccessible(true);
        licensesField.set(wrappingComplexLicenseView, licenses);
        return wrappingComplexLicenseView;
    }
}
