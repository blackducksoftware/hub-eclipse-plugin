/**
 * hub-eclipse-plugin
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.eclipse.common.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.views.ComponentInspectorView;
import com.blackducksoftware.integration.eclipse.views.widgets.ComponentModelFilter;

public class ComponentTableContentProvider implements ILazyContentProvider {
	private static final ComponentModel[] NOTHING = new ComponentModel[] {};

	private final ComponentInspectorView view;

	private final TableViewer viewer;

	private String inputProject;

	private ComponentModel[] parsedElements;

	private ComponentModelFilter componentFilter = null;

	public ComponentTableContentProvider(final ComponentInspectorView view, final TableViewer viewer) {
		this.view = view;
		this.viewer = viewer;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if (newInput == null) {
			this.parsedElements = new ComponentModel[] {};
		} else {
			this.parsedElements = (ComponentModel[]) newInput;
			if (componentFilter != null) {
				Stream<ComponentModel> componentStream = Arrays.stream(parsedElements);
				componentStream = componentStream.filter(model -> componentFilter.filter(model));
				this.parsedElements = componentStream.toArray(ComponentModel[]::new);
			}
		}
	}

	public ComponentModel[] parseElements(final Object inputElement) {
		final String projectName = (String) inputElement;
		inputProject = projectName;
		if (projectName.equals("")) {
			view.setStatusMessage(ComponentInspectorService.NO_SELECTED_PROJECT_STATUS);
			return NOTHING;
		}
		final BlackDuckHubPluginActivator plugin = BlackDuckHubPluginActivator.getDefault();
		final ComponentInspectorService componentInspectorService = plugin.getInspectorService();
		final ComponentInspectorPreferencesService componentInspectorPreferencesService = new ComponentInspectorPreferencesService();
		final boolean isActivated = componentInspectorPreferencesService.isProjectMarkedForInspection(projectName);
		if (isActivated) {
			if (plugin.getHubConnectionService().hasActiveHubConnection()) {
				final List<ComponentModel> componentModels = componentInspectorService.getProjectComponents(projectName);
				if (componentModels != null) {
					if (componentInspectorService.isProjectInspectionRunning(projectName)) {
						view.setStatusMessage(ComponentInspectorService.PROJECT_INSPECTION_RUNNING_STATUS);
					} else {
						if (componentInspectorService.isProjectInspectionScheduled(projectName)) {
							view.setStatusMessage(ComponentInspectorService.PROJECT_INSPECTION_SCHEDULED_STATUS);
						} else if (componentModels.size() == 0) {
							view.setStatusMessage(ComponentInspectorService.CONNECTION_OK_NO_COMPONENTS_STATUS);
						} else {
							view.setStatusMessage(ComponentInspectorService.CONNECTION_OK_STATUS);
						}
					}
					return componentModels.toArray(new ComponentModel[componentModels.size()]);
				}
				view.setStatusMessage(ComponentInspectorService.PROJECT_NEEDS_INSPECTION_STATUS);
				return NOTHING;
			}
			view.setStatusMessage(ComponentInspectorService.CONNECTION_DISCONNECTED_STATUS);
			return NOTHING;
		}
		final ProjectInformationService projectInformationService = new ProjectInformationService();
		if (projectInformationService.isProjectSupported(projectName)) {
			view.setStatusMessage(ComponentInspectorService.PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS);
		} else {
			view.setStatusMessage(ComponentInspectorService.PROJECT_NOT_SUPPORTED_STATUS);
		}
		return NOTHING;
	}

	public String getInputProject() {
		return inputProject;
	}

	@Override
	public void updateElement(final int index) {
		// TODO: See if there's a graceful way around "Ignored reentrant call while viewer is busy"
		if (parsedElements.length > 0) {
			viewer.setItemCount(parsedElements.length);
			viewer.replace(parsedElements[index], index);
		}
	}

	public void addFilter(final ComponentModelFilter filter) {
		this.componentFilter = filter;
	}

}
