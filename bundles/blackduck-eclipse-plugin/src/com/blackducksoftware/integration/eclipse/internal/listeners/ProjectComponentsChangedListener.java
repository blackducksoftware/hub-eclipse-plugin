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
package com.blackducksoftware.integration.eclipse.internal.listeners;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.eclipse.services.ComponentInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ProjectComponentsChangedListener implements IElementChangedListener {
    private final Logger log = LoggerFactory.getLogger(ProjectComponentsChangedListener.class);

    private final ComponentInspectorService inspectorService;
    private final ComponentInformationService componentInformationService;

    public ProjectComponentsChangedListener(final ComponentInspectorService inspectorService, final ComponentInformationService componentInformationService) {
        this.inspectorService = inspectorService;
        this.componentInformationService = componentInformationService;
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        this.searchForChangedComponents(event.getDelta());
    }

    public void searchForChangedComponents(final IJavaElementDelta delta) {
        final IJavaElement el = delta.getElement();
        switch (el.getElementType()) {
        case IJavaElement.JAVA_MODEL:
            for (final IJavaElementDelta childDelta : delta.getAffectedChildren()) {
                this.searchForChangedComponents(childDelta);
            }
            break;

        case IJavaElement.JAVA_PROJECT:
            if (0 != (delta.getFlags() & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED))) {
                for (final IJavaElementDelta childDelta : delta.getAffectedChildren()) {
                    this.searchForChangedComponents(childDelta);
                }
            }
            break;

        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
            try {
                final IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) el;
                packageFragmentRoot.getElementName();
                final String projectName = el.getJavaProject().getProject().getDescription().getName();
                final URL componentUrl = el.getPath().toFile().toURI().toURL();
                final Optional<ExternalId> optionalComponentExternalId = componentInformationService.constructMavenExternalIdFromUrl(componentUrl);
                if (optionalComponentExternalId.isPresent()) {
                    if ((delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0 || delta.getKind() == IJavaElementDelta.REMOVED) {
                        inspectorService.removeComponentFromProject(projectName, optionalComponentExternalId.get());
                    }
                    if ((delta.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0 || delta.getKind() == IJavaElementDelta.ADDED) {
                        inspectorService.addComponentToProject(projectName, optionalComponentExternalId.get());
                    }
                }
            } catch (final MalformedURLException | CoreException e) {
                log.warn("Component failed to be added or removed", e);
            }
            break;

        default:
            break;
        }
    }

}
