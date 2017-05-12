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
package com.blackducksoftware.integration.eclipse;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.blackducksoftware.integration.eclipse.internal.listeners.NewOrMovedProjectListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectComponentsChangedListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectDeletedListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectMarkedForInspectionListener;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.ComponentInformationService;
import com.blackducksoftware.integration.eclipse.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.services.connection.free.FreeConnectionService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class BlackDuckEclipseActivator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.blackducksoftware.integration.eclipse.plugin";

	private static BlackDuckEclipseActivator plugin;

	private ComponentInspectorService componentInspectorService;

	private ComponentInspectorViewService componentInspectorViewService;

	private ComponentInspectorPreferencesService componentInspectorPreferencesService;

	private HubConnectionService hubConnectionService;

	private FreeConnectionService freeConnectionService;

	private ProjectDeletedListener projectDeletedListener;

	private NewOrMovedProjectListener newProjectListener;

	private ProjectComponentsChangedListener projectComponentsChangedListener;

	private ProjectMarkedForInspectionListener projectMarkedForInspectionListener;

	private ProjectInformationService projectInformationService;

	private ComponentInformationService componentInformationService;

	@Override
	public void start(final BundleContext context) {
		plugin = this;
		hubConnectionService = BlackDuckEclipseServicesFactory.getInstance().getHubConnectionService();
		freeConnectionService = BlackDuckEclipseServicesFactory.getInstance().getFreeConnectionService();
		componentInspectorViewService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorViewService();
		componentInspectorService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorService();
		projectInformationService = BlackDuckEclipseServicesFactory.getInstance().getProjectInformationService();
		componentInformationService = BlackDuckEclipseServicesFactory.getInstance().getComponentInformationService();
		componentInspectorPreferencesService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorPreferencesService();
		projectMarkedForInspectionListener = new ProjectMarkedForInspectionListener(componentInspectorService, componentInspectorViewService);
		plugin.getPreferenceStore().addPropertyChangeListener(projectMarkedForInspectionListener);
		projectComponentsChangedListener = new ProjectComponentsChangedListener(componentInspectorService, componentInformationService);
		JavaCore.addElementChangedListener(projectComponentsChangedListener);
		newProjectListener = new NewOrMovedProjectListener(componentInspectorService, projectInformationService, componentInspectorPreferencesService);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(newProjectListener);
		projectDeletedListener = new ProjectDeletedListener(componentInspectorService);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectDeletedListener, IResourceChangeEvent.PRE_DELETE);
		try {
			super.start(context);
		} catch (final Exception e) {
			//TODO: Log properly
		}
	}

	public void refreshConnection() {
		hubConnectionService.reloadConnection();
		freeConnectionService.reloadConnection();
	}

	@Override
	public void stop(final BundleContext context) {
		plugin.getPreferenceStore().removePropertyChangeListener(projectMarkedForInspectionListener);
		plugin = null;
		componentInspectorService.shutDown();
		hubConnectionService.shutDown();
		freeConnectionService.shutDown();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(newProjectListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectDeletedListener);
		JavaCore.removeElementChangedListener(projectComponentsChangedListener);
		try {
			super.stop(context);
		} catch (final Exception e) {
			//TODO: Log properly
		}
	}

	public static BlackDuckEclipseActivator getDefault() {
		return plugin;
	}

}
