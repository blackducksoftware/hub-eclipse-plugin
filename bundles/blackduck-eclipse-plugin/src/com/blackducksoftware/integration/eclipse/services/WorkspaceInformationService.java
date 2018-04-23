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
package com.blackducksoftware.integration.eclipse.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WorkspaceInformationService {
    private final ProjectInformationService projectInformationService;

    public WorkspaceInformationService(final ProjectInformationService projectInformationService) {
        this.projectInformationService = projectInformationService;
    }

    public ProjectInformationService getProjectInformationService() {
        return projectInformationService;
    }

    public List<IProject> getAllProjects() {
        return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
    }

    public List<String> getAllSupportedProjectNames() {
        final List<IProject> allProjects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
        final List<String> names = allProjects
                .stream()
                .filter(project -> projectInformationService.isProjectSupported(project))
                .map(project -> projectInformationService.getProjectNameFromProject(project))
                .filter(projectName -> StringUtils.isNotBlank(projectName))
                .collect(Collectors.toList());

        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public String getFirstProjectNameFromActiveSelection() {
        String projectName = "";
        final Optional<IStructuredSelection> activeSelection = getActiveStructuredSelection();

        if (activeSelection.isPresent()) {
            projectName = getFirstProjectNameFromSelection(activeSelection.get());
        }

        return projectName;
    }

    public List<String> getProjectNamesFromActiveSelection() {
        List<String> names = new ArrayList<>();
        final Optional<IStructuredSelection> activeSelection = getActiveStructuredSelection();

        if (activeSelection.isPresent()) {
            names = getAllProjectNamesFromSelection(activeSelection.get());
        }

        return names;
    }

    public String getFirstProjectNameFromSelection(final IStructuredSelection selection) {
        String projectName = "";
        final Object element = selection.getFirstElement();
        final Optional<IProject> optionalProject = getProjectFromSelectionElement(element);

        if (optionalProject.isPresent()) {
            projectName = projectInformationService.getProjectNameFromProject(optionalProject.get());
        }

        return projectName;
    }

    public List<String> getAllProjectNamesFromSelection(final IStructuredSelection selection) {
        final List<?> selectedObjects = selection.toList();
        return selectedObjects
                .stream()
                .map(element -> getProjectFromSelectionElement(element))
                .filter(optionalProject -> optionalProject.isPresent())
                .map(optionalProject -> optionalProject.get())
                .map(project -> projectInformationService.getProjectNameFromProject(project))
                .filter(projectName -> StringUtils.isNotBlank(projectName))
                .collect(Collectors.toList());
    }

    public String getProjectNameFromEditor(final IEditorPart part) {
        String projectName = "";
        IProject project = null;
        IResource resource = null;

        final IEditorInput input = part.getEditorInput();
        if (input instanceof IFileEditorInput) {
            resource = ((IFileEditorInput) input).getFile();
            project = resource.getProject();
        }

        if (project != null) {
            projectName = projectInformationService.getProjectNameFromProject(project);
        }

        return projectName;
    }

    public Optional<IStructuredSelection> getActiveStructuredSelection() {
        Optional<IStructuredSelection> optionalSelection = Optional.empty();

        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow != null) {
            final ISelectionService selectionService = activeWindow.getSelectionService();
            if (selectionService != null && selectionService.getSelection() != null) {
                final ISelection selection = selectionService.getSelection();
                if (selection instanceof IStructuredSelection) {
                    optionalSelection = Optional.of((IStructuredSelection) selection);
                }
            }
        }

        return optionalSelection;
    }

    public Optional<IEditorPart> getActiveEditorPart() {
        final Optional<IEditorPart> optionalEditorPart = Optional.empty();

        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow != null) {
            final IWorkbenchPage workbenchPage = activeWindow.getActivePage();
            if (workbenchPage != null) {
                return Optional.ofNullable(workbenchPage.getActiveEditor());
            }
        }

        return optionalEditorPart;
    }

    private Optional<IProject> getProjectFromSelectionElement(final Object element) {
        IProject project = null;
        IResource resource = null;

        if (element instanceof IProject) {
            project = (IProject) element;
        } else if (element instanceof IResource || element instanceof IAdaptable) {
            if (element instanceof IResource) {
                resource = ((IResource) element);
            } else {
                final IAdaptable probableProject = ((IAdaptable) element);
                resource = probableProject.getAdapter(IResource.class);
            }

            if (resource != null) {
                project = resource.getProject();
            }
        }

        return Optional.ofNullable(project);
    }

}
