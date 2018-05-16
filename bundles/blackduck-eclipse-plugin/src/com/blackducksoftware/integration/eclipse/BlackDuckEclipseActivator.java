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
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class BlackDuckEclipseActivator extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "com.blackducksoftware.integration.eclipse.plugin";
    private static BlackDuckEclipseActivator plugin;
    public static BundleContext CONTEXT;

    private ComponentInspectorService componentInspectorService;
    private ComponentInspectorViewService componentInspectorViewService;
    private ComponentInspectorPreferencesService componentInspectorPreferencesService;
    private ProjectDeletedListener projectDeletedListener;
    private NewOrMovedProjectListener newProjectListener;
    private ProjectComponentsChangedListener projectComponentsChangedListener;
    private ProjectMarkedForInspectionListener projectMarkedForInspectionListener;
    private ProjectInformationService projectInformationService;
    private ComponentInformationService componentInformationService;

    @Override
    public void start(final BundleContext context) {
        plugin = this;
        this.CONTEXT = context;
        final BlackDuckEclipseServicesFactory blackDuckEclipseServicesFactory = BlackDuckEclipseServicesFactory.getInstance();
        componentInspectorViewService = blackDuckEclipseServicesFactory.getComponentInspectorViewService();
        componentInspectorService = blackDuckEclipseServicesFactory.getComponentInspectorService();
        projectInformationService = blackDuckEclipseServicesFactory.getProjectInformationService();
        componentInformationService = blackDuckEclipseServicesFactory.getComponentInformationService();
        componentInspectorPreferencesService = blackDuckEclipseServicesFactory.getComponentInspectorPreferencesService();
        projectMarkedForInspectionListener = new ProjectMarkedForInspectionListener(componentInspectorService, componentInspectorPreferencesService, componentInspectorViewService);
        plugin.getPreferenceStore().addPropertyChangeListener(projectMarkedForInspectionListener);
        projectComponentsChangedListener = new ProjectComponentsChangedListener(componentInspectorService, componentInformationService);
        JavaCore.addElementChangedListener(projectComponentsChangedListener);
        newProjectListener = new NewOrMovedProjectListener(componentInspectorService, projectInformationService, componentInspectorPreferencesService);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(newProjectListener);
        projectDeletedListener = new ProjectDeletedListener(componentInspectorService);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(projectDeletedListener, IResourceChangeEvent.PRE_DELETE);
        componentInspectorService.inspectAllProjects();
        try {
            super.start(context);
        } catch (final Exception e) {
            // TODO: Log properly
        }
    }

    @Override
    public void stop(final BundleContext context) {
        plugin.getPreferenceStore().removePropertyChangeListener(projectMarkedForInspectionListener);
        plugin = null;
        componentInspectorService.shutDown();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(newProjectListener);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectDeletedListener);
        JavaCore.removeElementChangedListener(projectComponentsChangedListener);
        try {
            super.stop(context);
        } catch (final Exception e) {
            // TODO: Log properly
        }
    }

    public static BlackDuckEclipseActivator getDefault() {
        return plugin;
    }

}
