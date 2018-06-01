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
package com.blackducksoftware.integration.eclipse.services.inspector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.ComponentModelVulnerabilityFirstComparator;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubComponentLookupService;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubPreferencesService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ComponentInspectorCacheService {
    private final Logger log = LoggerFactory.getLogger(ComponentInspectorCacheService.class);

    private final Map<String, List<ComponentModel>> inspectorCache;
    private final ComponentInspectorViewService componentInspectorViewService;
    private final HubComponentLookupService hubComponentLookupService;
    private final HubPreferencesService hubPreferencesService;

    public ComponentInspectorCacheService(final ComponentInspectorViewService componentInspectorViewService, final HubComponentLookupService hubComponentLookupService, final HubPreferencesService hubPreferencesService) {
        this.componentInspectorViewService = componentInspectorViewService;
        this.hubComponentLookupService = hubComponentLookupService;
        this.hubPreferencesService = hubPreferencesService;
        this.inspectorCache = new HashMap<>();
    }

    public List<ComponentModel> initializeProject(final String projectName) {
        return inspectorCache.put(projectName, Collections.synchronizedList(new ArrayList<ComponentModel>()));
    }

    public void addComponentToProject(final String projectName, final ExternalId externalId) throws IOException, URISyntaxException {
        final List<ComponentModel> components = inspectorCache.get(projectName);
        if (components != null && hubPreferencesService.canEstablishHubConnection()) {
            try {
                final ComponentModel newComponent = hubComponentLookupService.lookupComponent(externalId);
                components.add(newComponent);
                components.sort(new ComponentModelVulnerabilityFirstComparator());
                inspectorCache.put(projectName, components);
                componentInspectorViewService.resetDisplay();
            } catch (final IntegrationException e) {
                log.warn(String.format("Could not add component %s to project %s.", externalId.createExternalId(), projectName), e);
            }
        }
    }

    public List<ComponentModel> getProjectComponents(final String projectName) {
        final List<ComponentModel> models = inspectorCache.get(projectName);
        return models;
    }

    public void removeProject(final String projectName) {
        inspectorCache.remove(projectName);
        componentInspectorViewService.clearProjectDisplay(projectName);
    }

    public void removeComponentFromProject(final String projectName, final ExternalId externalId) {
        final List<ComponentModel> models = inspectorCache.get(projectName);
        if (models != null) {
            for (final Iterator<ComponentModel> iterator = models.iterator(); iterator.hasNext();) {
                final ComponentModel model = iterator.next();
                if (model.getExternalId().equals(externalId)) {
                    iterator.remove();
                }
            }
            inspectorCache.put(projectName, models);
            componentInspectorViewService.resetDisplay();
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
