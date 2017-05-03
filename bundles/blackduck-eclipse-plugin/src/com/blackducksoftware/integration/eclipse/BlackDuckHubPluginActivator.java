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

import com.blackducksoftware.integration.eclipse.common.services.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.eclipse.internal.listeners.NewProjectListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectComponentsChangedListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectDeletedListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.ProjectMarkedForInspectionListener;

public class BlackDuckHubPluginActivator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "black-duck-hub-plugin";

	private static BlackDuckHubPluginActivator plugin;

	private ComponentInspectorService inspectorService;

	private ComponentInspectorViewService inspectorViewService;

	private HubConnectionService hubConnectionService;

	private ProjectDeletedListener projectDeletedListener;

	private NewProjectListener newProjectListener;

	private ProjectComponentsChangedListener projectComponentsChangedListener;

	private ProjectMarkedForInspectionListener projectMarkedForInspectionListener;

	@Override
	public void start(final BundleContext context) {
		try {
			super.start(context);
		} catch (final Exception e) {
			//TODO: Log properly
		}
		plugin = this;
		hubConnectionService = new HubConnectionService();
		inspectorService = new ComponentInspectorService(inspectorViewService, hubConnectionService);
		inspectorViewService = new ComponentInspectorViewService();
		this.reconnectToHub();
		projectMarkedForInspectionListener = new ProjectMarkedForInspectionListener(inspectorService, inspectorViewService);
		plugin.getPreferenceStore().addPropertyChangeListener(projectMarkedForInspectionListener);
		projectComponentsChangedListener = new ProjectComponentsChangedListener(inspectorService);
		JavaCore.addElementChangedListener(projectComponentsChangedListener);
		newProjectListener = new NewProjectListener(inspectorService);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(newProjectListener);
		projectDeletedListener = new ProjectDeletedListener(inspectorService);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectDeletedListener, IResourceChangeEvent.PRE_DELETE);
	}

	public void reconnectToHub() {
		hubConnectionService.reloadConnection();
	}

	@Override
	public void stop(final BundleContext context) {
		plugin.getPreferenceStore().removePropertyChangeListener(projectMarkedForInspectionListener);
		plugin = null;
		inspectorService.shutDown();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(newProjectListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectDeletedListener);
		JavaCore.removeElementChangedListener(projectComponentsChangedListener);
		try {
			super.stop(context);
		} catch (final Exception e) {
			//TODO: Log properly
		}
	}

	public static BlackDuckHubPluginActivator getDefault() {
		return plugin;
	}

	public ComponentInspectorService getInspectorService() {
		return inspectorService;
	}

	public ComponentInspectorViewService getInspectorViewService(){
		return inspectorViewService;
	}

	public HubConnectionService getHubConnectionService(){
		return hubConnectionService;
	}

}
