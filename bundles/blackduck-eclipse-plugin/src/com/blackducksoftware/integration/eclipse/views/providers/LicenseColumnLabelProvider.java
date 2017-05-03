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
package com.blackducksoftware.integration.eclipse.views.providers;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.hub.dataservice.license.ComplexLicenseParser;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;

public class LicenseColumnLabelProvider extends ComponentTableColumnLabelProvider {
	public LicenseColumnLabelProvider(final int width, final int style) {
		super(width, style);
	}

	@Override
	public String getText(final Object input) {
		final ComponentModel model = (ComponentModel) input;
		if (!model.getLicenseIsKnown()) {
			return "";
		}
		final ComplexLicenseView license = model.getLicense();
		final ComplexLicenseParser licenseParser = new ComplexLicenseParser(license);
		final String text = licenseParser.parse();
		return text;
	}

	@Override
	public String getTitle() {
		return "License";
	}

}
