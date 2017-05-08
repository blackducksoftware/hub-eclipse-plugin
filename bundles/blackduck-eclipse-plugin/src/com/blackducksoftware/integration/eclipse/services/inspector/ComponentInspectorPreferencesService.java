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
package com.blackducksoftware.integration.eclipse.services.inspector;

import com.blackducksoftware.integration.eclipse.services.BlackDuckPreferencesService;

public class ComponentInspectorPreferencesService {
	public static final String INSPECTOR_NODE_ID = "inspector";

	public static final String INSPECT_BY_DEFAULT = "inspectNewProjectsByDefault";

	private final BlackDuckPreferencesService blackDuckPreferencesService;

	public ComponentInspectorPreferencesService(final BlackDuckPreferencesService blackDuckPreferencesService){
		this.blackDuckPreferencesService = blackDuckPreferencesService;
		blackDuckPreferencesService.setPreferenceDefault(INSPECT_BY_DEFAULT, "true");
	}

	public boolean isProjectMarkedForInspection(final String projectName) {
		final String projectActivated = blackDuckPreferencesService.getPreference(projectName);
		if(projectActivated.equals("")){
			return false;
		}
		return Boolean.parseBoolean(projectActivated);
	}

	public String getInspectByDefault(){
		return blackDuckPreferencesService.getPreference(INSPECT_BY_DEFAULT);
	}

	public void activateProject(final String projectName) {
		blackDuckPreferencesService.savePreference(projectName, "true");
	}

	public void deactivateProject(final String projectName) {
		blackDuckPreferencesService.savePreference(projectName, "false");
	}

	public void removeProject(final String projectName) {
		blackDuckPreferencesService.removePreference(projectName);
	}

	public void addProject(final String projectName){
		final String initialValue = blackDuckPreferencesService.getPreference(INSPECT_BY_DEFAULT);
		blackDuckPreferencesService.savePreference(projectName, initialValue);
	}

}
