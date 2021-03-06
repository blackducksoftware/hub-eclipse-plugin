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
package com.blackducksoftware.integration.eclipse.views.widgets;

import org.eclipse.swt.widgets.Text;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.service.model.ComplexLicenseParser;

public class ComponentModelFilter {
    private final Text filterBox;

    public ComponentModelFilter(final Text filterBox) {
        this.filterBox = filterBox;
    }

    public boolean filter(final ComponentModel model) {
        if (filterBox == null || filterBox.getText().length() == 0) {
            return true;
        }
        final ExternalId externalId = model.getExternalId();
        if (externalId.toString().contains(filterBox.getText())) {
            return true;
        }
        if (model.getLicenseIsKnown()) {
            final String license = new ComplexLicenseParser(model.getLicense()).parse();
            if (license.contains(filterBox.getText())) {
                return true;
            }
        }
        return false;
    }

}
