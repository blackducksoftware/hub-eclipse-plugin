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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.ComponentModelVulnerabilityFirstComparator;
import com.blackducksoftware.integration.eclipse.services.AbstractComponentLookupService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.buildtool.Gav;

public class ComponentInspectorCacheService {
	private final Map<String, List<ComponentModel>> inspectorCache;

	private final ComponentInspectorViewService componentInspectorViewService;

	private final AbstractComponentLookupService componentLookupService;

	public ComponentInspectorCacheService(final ComponentInspectorViewService componentInspectorViewService, final AbstractComponentLookupService componentLookupService) {
		this.componentInspectorViewService = componentInspectorViewService;
		this.componentLookupService = componentLookupService;
		this.inspectorCache = new HashMap<>();
	}

	public List<ComponentModel> initializeProject(final String projectName) {
		return inspectorCache.put(projectName, Collections.synchronizedList(new ArrayList<ComponentModel>()));
	}

	public List<ComponentModel> addProjectComponents(final String projectName, final List<ComponentModel> models) {
		return inspectorCache.put(projectName, models);
	}

	public void addComponentToProject(final String projectName, final Gav gav) throws IOException, URISyntaxException {
		final List<ComponentModel> components = inspectorCache.get(projectName);
		if (components != null) {
			try {
				final ComponentModel newComponent = componentLookupService.lookupComponent(gav);
				components.add(newComponent);
				components.sort(new ComponentModelVulnerabilityFirstComparator());
				inspectorCache.put(projectName, components);
				componentInspectorViewService.refreshComponentInspectorForProject(projectName);
			} catch (final IntegrationException e) {
				/*
				 * Thrown if exception occurs when accessing key gav from cache. If an exception is
				 * thrown, info associated with that gav is inaccessible, and so don't put any
				 * information related to said gav into hashmap associated with the project
				 */
			}
		}
	}

	public List<ComponentModel> getProjectComponents(final String projectName) {
		final List<ComponentModel> models = inspectorCache.get(projectName);
		if (models == null) {
			return null;
		}
		return inspectorCache.get(projectName);
	}

	public void removeProject(final String projectName) {
		inspectorCache.remove(projectName);
		componentInspectorViewService.clearProjectDisplay(projectName);
	}

	public void removeComponentFromProject(final String projectName, final Gav gav) {
		final List<ComponentModel> models = inspectorCache.get(projectName);
		if (models != null) {
			for (final Iterator<ComponentModel> iterator = models.iterator(); iterator.hasNext();) {
				final ComponentModel model = iterator.next();
				if (model.getGav().equals(gav)) {
					iterator.remove();
				}
			}
			inspectorCache.put(projectName, models);
			componentInspectorViewService.refreshComponentInspectorForProject(projectName);
		}
	}

	public boolean containsComponentsFromProject(final String projectName) {
		return inspectorCache.containsKey(projectName);
	}

	public void renameProject(final String oldName, final String newName) {
		final List<ComponentModel> models = inspectorCache.get(oldName);
		inspectorCache.put(newName, models);
		inspectorCache.remove(oldName);
	}

}
