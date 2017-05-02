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
package com.blackducksoftware.integration.eclipse.contextmenu.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.eclipse.common.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorService;

public class InspectSelectedProject extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final WorkspaceInformationService workspaceService = new WorkspaceInformationService();
		final List<String> selectedProjects = workspaceService.getAllSelectedProjects();
		final ComponentInspectorPreferencesService componentInspectorPreferencesService = new ComponentInspectorPreferencesService();
		final BlackDuckHubPluginActivator plugin = BlackDuckHubPluginActivator.getDefault();
		final ComponentInspectorService componentInspectorService = plugin.getInspectorService();
		for (final String selectedProject : selectedProjects) {
			if (!componentInspectorPreferencesService.isProjectMarkedForInspection(selectedProject)) {
				componentInspectorPreferencesService.activateProject(selectedProject);
			}
			componentInspectorService.inspectProject(selectedProject);
		}
		return null;
	}

}
