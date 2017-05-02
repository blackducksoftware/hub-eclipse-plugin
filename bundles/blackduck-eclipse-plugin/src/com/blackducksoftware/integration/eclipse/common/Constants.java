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
package com.blackducksoftware.integration.eclipse.common;

public class Constants {
	public static final String GRADLE_NATURE = "org.eclipse.buildship.core.gradleprojectnature";

	public static final String MAVEN_NATURE = "org.eclipse.m2e.core.maven2Nature";

	public static final String[] SUPPORTED_NATURES = {
			GRADLE_NATURE,
			MAVEN_NATURE
	};

	public static final String M2_REPO = "M2_REPO";

	public static final String MAVEN_NAMESPACE = "maven";

	public static final String GRADLE_NAMESPACE = "maven";

	public static final String INITIALIZING = "Initializing component inspector...";

	public static final String NO_SELECTED_PROJECT = "No open project selected";

	public static final String PROJECT_INSPECTION_ACTIVE = "Inspecting project...";

	public static final String PROJECT_INSPECTION_SCHEDULED = "Project scheduled for inspection";

	public static final String PROJECT_INSPECTION_INACTIVE = "Inspection not activated for current project";

	public static final String PROJECT_NEEDS_INSPECTION = "Project has not yet been inspected";

	public static final String CONNECTION_DISCONNECTED = "Cannot connect to Hub instance";

	public static final String CONNECTION_OK = "Connected to Hub instance - double-click any component to open it in the Hub";

	public static final String CONNECTION_OK_NO_COMPONENTS = "Connected to Hub instance - No components found.";

	public static final String PROJECT_NOT_SUPPORTED = "Cannot inspect selected project - either it is not a Java project or no Maven or Gradle nature was detected";

	public static final String BLACK_DUCK_PREFERENCE_ID = "com.blackducksoftware.integration.eclipse.preferencepages";

	public static final String COMPONENT_INSPECTOR_PREFERENCE_ID = "com.blackducksoftware.integration.eclipse.preferencepages.ComponentInspectorSettings";

	public static final String HUB_SETTINGS_PREFERENCE_ID = "com.blackducksoftware.integration.eclipse.preferencepages.HubPreferences";

	public static final String COMPONENT_INSPECTOR_ID = "com.blackducksoftware.integration.eclipse.views.ComponentInspectorView";

}
